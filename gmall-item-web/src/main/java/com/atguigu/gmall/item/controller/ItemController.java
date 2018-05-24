package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    ManageService manageService;

    @Reference
    ListService listService;

    @RequestMapping("{skuId}.html")
    public String  getSkuInfoById(@PathVariable("skuId") String skuId, Model model){
        SkuInfo skuInfo = manageService.getSkuInfoById(skuId);
        model.addAttribute("skuInfo",skuInfo);


        List<SpuSaleAttr> spuSaleAttrList = manageService.getspuSaleAttrListByskuId(skuInfo.getSpuId(),skuId);
        model.addAttribute("spuSaleAttrList",spuSaleAttrList);


        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());


       //建立一个新的字符串
        String valueIdsKey="";

        Map<String,String> valuesSkuMap=new HashMap<>();
       //遍历整个销售属性值得集合
        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            //获取每个销售属性值得实例
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            //如果字符串的长度不等于0就拼接一个
            if(valueIdsKey.length()!=0){
                valueIdsKey= valueIdsKey+"|";
            }
            //将销售属性值的ID拼接进去字符串形成一种格式
            valueIdsKey=valueIdsKey+skuSaleAttrValue.getSaleAttrValueId();
            //i+1的值小于整个集合的长度并且SKU实例的ID跟下一个ID没有发生了改变
            if((i+1)== skuSaleAttrValueListBySpu.size()||!skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i+1).getSkuId())){
             //将拼好的值存到MAP 中
                valuesSkuMap.put(valueIdsKey,skuSaleAttrValue.getSkuId());
                //清空字符下回使用
                valueIdsKey="";
            }

        }

         //把map变成json串
        String valuesSkuJson = JSON.toJSONString(valuesSkuMap);

        model.addAttribute("valuesSkuJson",valuesSkuJson);
        //更新热度评分

        listService.countHotScore(skuId);
        return "item";
    }

}
