package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAtterMapper extends Mapper<SpuSaleAttr> {


    public List<SpuSaleAttr> selectSpuSaleAttrList(Long spuId);


   public  List<SpuSaleAttr> selectSpuSaleAttrListBySku(@Param("spuId") Long spuId,@Param("skuId") Long skuId);

}
