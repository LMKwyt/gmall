package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuInfoEs;
import com.atguigu.gmall.bean.SkuInfoEsParam;
import com.atguigu.gmall.bean.SkuInfoEsResult;
import com.atguigu.gmall.service.ListService;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import io.searchbox.client.JestClient;


import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;

import org.elasticsearch.search.aggregations.AggregationBuilders;

import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;

import org.elasticsearch.search.sort.SortOrder;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {
    @Autowired
    JestClient jestClient;


    public void saveSkuInfoEs(SkuInfoEs skuInfoEs) {
        Index index = new Index.Builder(skuInfoEs).index("gmall").type("SkuInfo").id(skuInfoEs.getId()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //根据查询条件组合query串

    private    String   makeQueryStringForSearch(SkuInfoEsParam skuInfoEsParam){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //过滤条件
        BoolQueryBuilder boolQueryBuilder =new BoolQueryBuilder();
        if(skuInfoEsParam.getCatalog3Id()!=null&&skuInfoEsParam.getCatalog3Id().length()>0){
            TermQueryBuilder termQueryBuilder= new TermQueryBuilder("catalog3Id",skuInfoEsParam.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        List<String> valueIdList = skuInfoEsParam.getValueIdList();
        if(valueIdList !=null&& valueIdList.size()>0){
            for (String s : valueIdList) {
                TermQueryBuilder termQueryAttrValueBuilder =new TermQueryBuilder("skuAttrValueListEs.valueId",s);

                boolQueryBuilder.filter(termQueryAttrValueBuilder);
            }
      }

        //must条件的MATCH
        if(skuInfoEsParam.getKeyword()!=null&&skuInfoEsParam.getKeyword().length()>0){
            MatchQueryBuilder matchQueryBuilder =new MatchQueryBuilder("skuName",skuInfoEsParam.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);

            //高亮显示
            HighlightBuilder highlightBuilder =new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red;'>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlight(highlightBuilder);
        }
        searchSourceBuilder.query(boolQueryBuilder);
        //分页
        searchSourceBuilder.from((skuInfoEsParam.getPageNo()-1)*skuInfoEsParam.getPageSize());
        searchSourceBuilder.size(skuInfoEsParam.getPageSize());
        //排序
        searchSourceBuilder.sort("hotScore", SortOrder.ASC);
        //聚合
        TermsBuilder termsBuilder = AggregationBuilders.terms("groupby_valueId").field("skuAttrValueListEs.valueId");
        searchSourceBuilder.aggregation(termsBuilder);
        String query = searchSourceBuilder.toString();

        System.out.println("query = " + query);

       return query;
    }
    private SkuInfoEsResult makeResultForSearch(SearchResult searchResult,SkuInfoEsParam skuInfoEsParam){
        SkuInfoEsResult skuInfoEsResult =new SkuInfoEsResult();
        //获取每一个skuinfoes
        List<SearchResult.Hit<SkuInfoEs, Void>> hits = searchResult.getHits(SkuInfoEs.class);
        //新建一个SKUINFOES集合放改完名字之后的SKUINFOES
        int  size= hits.size();
        List<SkuInfoEs> skuInfoEsList= new ArrayList(size);
        for (SearchResult.Hit<SkuInfoEs, Void> hit : hits) {
            SkuInfoEs skuInfoEs = hit.source;
            if(hit.highlight!=null){
                List<String> skuName = hit.highlight.get("skuName");
                skuInfoEs.setSkuName(skuName.get(0));
            }
            skuInfoEsList.add(skuInfoEs);
        }
        skuInfoEsResult.setSkuInfoListEs(skuInfoEsList);
        //获取平台属性值集合
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_valueId = aggregations.getTermsAggregation("groupby_valueId");
        List<TermsAggregation.Entry> buckets = groupby_valueId.getBuckets();
        List<String> attrvaluelist =new ArrayList<>(buckets.size());
        for (TermsAggregation.Entry bucket : buckets) {
            attrvaluelist.add(bucket.getKey());
        }
        skuInfoEsResult.setValueIdList(attrvaluelist);
        Long total = searchResult.getTotal();
        //获取总数
        skuInfoEsResult.setTotal(size);
        //获取总的页码数
        long l = (total + skuInfoEsParam.getPageSize() - 1) / skuInfoEsParam.getPageSize();
        skuInfoEsResult.setTotalPage(l);
        return skuInfoEsResult;
    }
    public SkuInfoEsResult searchSkuInfoList(SkuInfoEsParam skuInfoEsParam){
        //制作查询的字符创
        String query = makeQueryStringForSearch(skuInfoEsParam);

       //开始去ES查询
        Search search = new Search.Builder(query).addIndex("gmall").addType("SkuInfo").build();
        SearchResult searchResult=null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
      //将查询返回的结果封装为自己skuinfo返回值
        SkuInfoEsResult skuInfoEsResult = makeResultForSearch(searchResult,skuInfoEsParam);
        return  skuInfoEsResult;

    }
}
