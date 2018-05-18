package com.atguigu.gmall.bean;

import java.io.Serializable;
import java.util.List;

public class SkuInfoEsResult implements Serializable

{


    List<SkuInfoEs> skuInfoListEs;

    long total;

    long totalPage;

    List<String> valueIdList;

    public List<SkuInfoEs> getSkuInfoListEs() {
        return skuInfoListEs;
    }

    public void setSkuInfoListEs(List<SkuInfoEs> skuInfoListEs) {
        this.skuInfoListEs = skuInfoListEs;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    public List<String> getValueIdList() {
        return valueIdList;
    }

    public void setValueIdList(List<String> valueIdList) {
        this.valueIdList = valueIdList;
    }
}


