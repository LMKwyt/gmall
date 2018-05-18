package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.ManageService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SkuInfoController {


    @Reference
    ManageService manageService;

    @RequestMapping(value = "saveSku",method = RequestMethod.POST)
    public String saveSkuInfo(SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return "success";
    }
    @RequestMapping(value="skuInfoListBySpu")
    public List<SkuInfo>  getSkuInfoListBySpu(@RequestParam("spuId") String spuId){
        List<SkuInfo> skuInfoList = manageService.getSkuInfoListBySpu(spuId);
        return skuInfoList;
    }

    @RequestMapping(value = "onSale",method = RequestMethod.POST)
    public String onSave(String skuId){

        manageService.onSale(skuId);
        return "success";

    }
}
