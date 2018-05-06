package com.atguigu.gmall.gmallusermanage.service;

import com.atguigu.gmall.gmallusermanage.bean.UserInfo;

import java.util.List;

public interface UserService {


    public void addUserInfo(UserInfo userInfo);
    public UserInfo getUserInfor(String id);
    public List<UserInfo> getAll();
    public UserInfo getUserInfoByname();
    public void updateUserInfo(UserInfo userInfo);
    public void delUserInfo();
}
