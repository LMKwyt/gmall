package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {


    public void addUserInfo(UserInfo userInfo);
    public UserInfo getUserInfor(String id);
    public List<UserInfo> getAll();
    public UserInfo getUserInfoByname();
    public void updateUserInfo(UserInfo userInfo);
    public void delUserInfo();
    public List<UserAddress> getUserAddressList(String userId);
   //从数据库中查询用户 如果查到放到redis中一份 没有null
    public UserInfo login(UserInfo userInfo);

     //判断业务模块某个页面要检查当前用户是否登录时，提交到认证中心，认证中心进行检查校验，返回登录状态、用户Id和用户名称。
    public boolean verify(String userId);
}
