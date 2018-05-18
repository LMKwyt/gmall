package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuInfoEs;
import com.atguigu.gmall.bean.SkuInfoEsParam;
import com.atguigu.gmall.bean.SkuInfoEsResult;

public interface ListService {

    public void saveSkuInfoEs(SkuInfoEs skuInfoEs);
    public SkuInfoEsResult searchSkuInfoList(SkuInfoEsParam skuInfoEsParam);
}
