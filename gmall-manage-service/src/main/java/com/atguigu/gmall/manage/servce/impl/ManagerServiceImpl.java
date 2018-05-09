package com.atguigu.gmall.manage.servce.impl;

import com.alibaba.dubbo.config.annotation.Service;

import com.atguigu.gmall.bean.*;

import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ManagerServiceImpl implements ManageService {
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    public List<BaseCatalog1> getAllBaseCatalog1List() {
        List<BaseCatalog1> baseCatalog1List = baseCatalog1Mapper.selectAll();
        return baseCatalog1List;
    }

    public List<BaseCatalog2> getAllBaseCatalog2List(String catalog1Id) {
        BaseCatalog2 baseCatalog2query = new BaseCatalog2();
        baseCatalog2query.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2query);
        return baseCatalog2List;

    }

    public List<BaseCatalog3> getAllBaseCatalog3List(String catalog2Id) {
        BaseCatalog3 baseCatalog3query = new BaseCatalog3();
        baseCatalog3query.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3query);
        return baseCatalog3List;
    }

    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

        BaseAttrInfo baseAttrInfo=new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.select(baseAttrInfo);
        return baseAttrInfoList;
    }

     public void saveAttrInfo(BaseAttrInfo baseAttrInfo){
        //判断baseAttrInfo的主键ID是否有


         if(baseAttrInfo.getId()!=null&&baseAttrInfo.getId().length()>0){
             //有执行修改
             baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
         }else {
             //没有的执行添加
             baseAttrInfoMapper.insertSelective(baseAttrInfo);
         }

         //把原属性值全部清空
         BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
         baseAttrValue4Del.setAttrId(baseAttrInfo.getId());
         baseAttrValueMapper.delete(baseAttrValue4Del);
        //才能使主键重复的值写入进去
         List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getBaseAttrValueList();
         String id = baseAttrInfo.getId();
         for (BaseAttrValue baseAttrValue : baseAttrValueList) {
             //防止主键被赋上一个空字符串
             if(baseAttrValue.getId()!=null&&baseAttrValue.getId().length()==0){
                 baseAttrValue.setId(null);
             }

             baseAttrValue.setAttrId(id);
             baseAttrValueMapper.insertSelective(baseAttrValue);
         }
     }

     public List<BaseAttrValue> getAttrValueList(String attrid){
         BaseAttrValue baseAttrValueQuery =new BaseAttrValue();
         baseAttrValueQuery.setAttrId(attrid);
         List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValueQuery);
         return baseAttrValueList;

     }

     public void delAttrInfo(BaseAttrInfo baseAttrInfo){
                baseAttrInfoMapper.deleteByPrimaryKey(baseAttrInfo);
         BaseAttrValue baseAttrValueQuery =new BaseAttrValue();
         baseAttrValueQuery.setAttrId(baseAttrInfo.getId());
         Example example=new Example(BaseAttrValue.class);
         example.createCriteria().andEqualTo("attrId",baseAttrInfo.getId());
               baseAttrValueMapper.deleteByExample(example);
     }
}
