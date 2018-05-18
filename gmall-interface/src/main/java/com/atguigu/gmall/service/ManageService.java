package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface ManageService {
    //查询一级分类的集合
    public List<BaseCatalog1> getAllBaseCatalog1List();
    //查询二级分类的集合
    public List<BaseCatalog2> getAllBaseCatalog2List(String catalog1Id);
    //查询三级分类的集合
    public List<BaseCatalog3> getAllBaseCatalog3List(String catalog2Id);
    //查询平台属性的集合 根据三级分类的ID
    public List<BaseAttrInfo> getAttrList(String catalog3Id);
    //保存添加的平台属性  添加按钮
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);
    //获取平台属性值的集合根据平台属性的ID
    public List<BaseAttrValue> getAttrValueList(String attrid);
    //删除平台属性按钮
    public void delAttrInfo(BaseAttrInfo baseAttrInfo);
    //获取SPU的集合 一类的商品信息
    public List<SpuInfo> getSpuList(String catalog3Id);
    //获取有数据库管理员设置的SPU的销售属性列表
    public List<BaseSaleAttr> getBaseSaleAttrList();
    //保存spu实例
    public void saveSpuInfo(SpuInfo spuInfo);
   //根据spuId 获取SPu实例对象
    public SpuInfo getSpuinfo(String spuId);

    //根据spuId 获取SpuImageList
    public List<SpuImage> getspuImageList( String spuId);

//根据销售属性ID 来查找商品属性值的集合
    public List<SpuSaleAttrValue> getspuSaleAttrValue(String SaleAttrId ,String spuId);
//根据SPUID来查找商品属性的集合 涵商品属性值得集合用到了xml文件
    public List<SpuSaleAttr> getspuSaleAttrList(String spuId);


////查询平台属性的集合 根据三级分类的ID  包涵平台属性值得集合用到了xml文件
    public List<BaseAttrInfo> getattrInfoList(String catalog3Id);
    //保存商品单元储存单位SKU
    public void saveSkuInfo(SkuInfo skuInfo);
    //根据SPUid来查到素有的SKU集合
    public List<SkuInfo> getSkuInfoListBySpu(String spuId);

    //根据SKUid 查询SkuInfo实例
    public SkuInfo getSkuInfoById(String skuId);
     //根据SKUid 和SPUid 里面有isCHecked字段
    public List<SpuSaleAttr> getspuSaleAttrListByskuId(String spuId ,String skuId);

   //根据spuID 来查询sku的实际的销售属性值
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);


   //点击商品上架之后将SKU的值添加到ES数据库中以便以后查询
    public void onSale(String skuId);
}
