package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfoEsParam;
import com.atguigu.gmall.bean.SkuInfoEsResult;
import com.atguigu.gmall.service.ListService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListController {

    @Reference
    ListService listService;

    @RequestMapping("list")
    public String list(SkuInfoEsParam skuInfoEsParam) {
        SkuInfoEsResult skuInfoEsResult = listService.searchSkuInfoList(skuInfoEsParam);
        String s = JSON.toJSONString(skuInfoEsResult);
        return s;
    }

}
