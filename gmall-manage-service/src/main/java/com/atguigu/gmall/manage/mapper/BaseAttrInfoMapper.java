package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {


    public List<BaseAttrInfo> selectattrInfoList(Long catalog3Id);

    public List<BaseAttrInfo> selectattrInfoListforValueId(@Param("ValueId") String valueId);
}
