package com.atguigu.gmall.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.usermanage.mapper.UserInfoMapper;
import com.atguigu.gmall.usermanage.mapper.UserAddressMapper;

import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UseServiceImpl implements UserService {

    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;
    public void addUserInfo(UserInfo userInfo){

        userInfoMapper.insertSelective(userInfo);
    }

    public UserInfo getUserInfor(String id){
         return userInfoMapper.selectByPrimaryKey(id);
    }

    public List<UserInfo> getAll(){
        return userInfoMapper.selectAll();

    }

    public UserInfo getUserInfoByname(){
//UserInfo userQuery =new UserInfo();

    //userQuery.setName("张三");
        Example e=new Example(UserInfo.class);
        e.createCriteria().andLike("name", "张%");
        return  userInfoMapper.selectOneByExample(e);


    }


    public void updateUserInfo(UserInfo userInfo){
        Example e=new Example(UserInfo.class);
        e.createCriteria().andEqualTo("name","婷婷婷");
        userInfoMapper.updateByExampleSelective(userInfo,e);
    }


    public void delUserInfo(){
        Example e=new Example(UserInfo.class);
        e.createCriteria().andEqualTo("name","李四");
        userInfoMapper.deleteByExample(e);
    }


    public List<UserAddress> getUserAddressList(String userId){
        UserAddress userAddressQuery =new UserAddress();
        userAddressQuery.setUserId(userId);
        return userAddressMapper.select(userAddressQuery);
    }
}
