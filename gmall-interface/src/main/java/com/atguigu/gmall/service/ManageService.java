package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface ManageService {

    public List<BaseCatalog1> getAllBaseCatalog1List();
    public List<BaseCatalog2> getAllBaseCatalog2List(String catalog1Id);
    public List<BaseCatalog3> getAllBaseCatalog3List(String catalog2Id);
    public List<BaseAttrInfo> getAttrList(String catalog3Id);
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);
    public List<BaseAttrValue> getAttrValueList(String attrid);
    public void delAttrInfo(BaseAttrInfo baseAttrInfo);
}
