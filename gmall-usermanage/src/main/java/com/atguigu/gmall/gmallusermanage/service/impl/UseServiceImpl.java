package com.atguigu.gmall.gmallusermanage.service.impl;

import com.atguigu.gmall.gmallusermanage.bean.UserInfo;
import com.atguigu.gmall.gmallusermanage.mapper.UserInfoMapper;
import com.atguigu.gmall.gmallusermanage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UseServiceImpl implements UserService {

    @Autowired
    UserInfoMapper userInfoMapper;

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
}
