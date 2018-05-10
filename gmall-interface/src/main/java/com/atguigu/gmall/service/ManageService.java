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
}
