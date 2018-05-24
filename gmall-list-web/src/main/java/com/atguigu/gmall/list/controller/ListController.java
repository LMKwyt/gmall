package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;

import com.atguigu.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    ListService listService;
    @Reference
    ManageService manageService;

    @RequestMapping("list")
    public String list(SkuInfoEsParam skuInfoEsParam, Model model) {
        if (skuInfoEsParam.getValueIds() != null) {
            List<String> stringList = skuInfoEsParam.changArray2list();
            skuInfoEsParam.setValueIdList(stringList);
        }

        SkuInfoEsResult skuInfoEsResult = listService.searchSkuInfoList(skuInfoEsParam);
        model.addAttribute("skuInfoEsResult", skuInfoEsResult);
        List<String> valueIdList = skuInfoEsResult.getValueIdList();
        String ValueId = StringUtils.join(valueIdList, ',');

        List<BaseAttrInfo> baseAttrInfoList =new ArrayList<>();
        List<BaseAttrValueEx> SelectedvalueIdListforEX=new ArrayList<>();
        if(skuInfoEsResult.getValueIdList()!=null&&skuInfoEsResult.getValueIdList().size()>0) {
           baseAttrInfoList = manageService.selectattrInfoListforValueId(ValueId);
        }


       //处理已经选中的属性值 删除有的属性值ID
        // //循环已选择中的属性值 与查询结果的属性值进行匹配 如果匹配上 则删除该属性值的属性
        //循环嵌套 1 次数会不会很多 2 操作的复杂性  io  网络

        List<String> valueIdListParam = skuInfoEsParam.getValueIdList();
        for (String valueId : valueIdListParam) {
            for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo BaseAttrInfoResule =  iterator.next();
                List<BaseAttrValue> baseAttrValueList = BaseAttrInfoResule.getBaseAttrValueList();
                for (BaseAttrValue baseAttrValue : baseAttrValueList) {
                    if(valueId.equals(baseAttrValue.getId())){
                        //生成面包屑
                        BaseAttrValueEx baseAttrValueEx = new BaseAttrValueEx();
                        baseAttrValueEx.setWholeName(BaseAttrInfoResule.getAttrName()+":"+baseAttrValue.getValueName());

                        baseAttrValueEx.setCancelUrlParam(makeUrlParam(skuInfoEsParam,valueId));
                        SelectedvalueIdListforEX.add(baseAttrValueEx);
                        //删除选中的平台属性
                        iterator.remove();
                    }
                }
            }
        }



        model.addAttribute("baseAttrInfoList", baseAttrInfoList);
        model.addAttribute("pageNo",skuInfoEsParam.getPageNo());

        model.addAttribute("totalPages",skuInfoEsResult.getTotalPage());

        model.addAttribute("keyword",skuInfoEsParam.getKeyword());

        model.addAttribute("selectedValueList",SelectedvalueIdListforEX);
        String urlParam = makeUrlParam(skuInfoEsParam);
        model.addAttribute("urlParam",urlParam);
        return "list";
    }

    private String makeUrlParam(SkuInfoEsParam skuInfoEsParam,String ... cancelValueIds) {
        String UrlParam = "";
        List<String> UrlList = new ArrayList<>();
        String[] valueIds = skuInfoEsParam.getValueIds();
        String catalog3Id = skuInfoEsParam.getCatalog3Id();
        String keyword = skuInfoEsParam.getKeyword();
        if (catalog3Id != null&&catalog3Id.length()>0) {
            UrlList.add("catalog3Id=" + catalog3Id);
        }
        if (keyword != null && keyword.length() > 0) {
            UrlList.add("keyword=" + keyword);
        }
        if (valueIds != null && valueIds.length > 0) {
            for (int i = 0; i < skuInfoEsParam.getValueIds().length; i++) {

                String valueId= skuInfoEsParam.getValueIds()[i];
                if(cancelValueIds!=null&&cancelValueIds.length==1){
                    if(valueId.equals(cancelValueIds[0])){
                        continue;
                    }

                }

                UrlList.add("valueIds=" + valueId);
            }

        }

        UrlParam = StringUtils.join(UrlList, "&");
        return UrlParam;
    }
}
