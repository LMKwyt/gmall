package com.atguigu.gmall.usermanage.controller;


import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping(value = "user", method = RequestMethod.POST)
    public String addUserInfo(UserInfo userInfo) {
        userService.addUserInfo(userInfo);
        return "success";
    }

    @GetMapping(value = "user")
    public UserInfo addUserInfo(@RequestParam("id") String id) {
        return userService.getUserInfor(id);
    }

    @GetMapping(value = "userlist")
    public List<UserInfo> addUserInfo() {
        return userService.getAll();
    }

    @GetMapping(value = "userbyname")
    public UserInfo addUserInfobyname() {
        return userService.getUserInfoByname();
    }

    @PostMapping(value = "updateUser")
    public String updateUserInfo(UserInfo userInfo) {
        userService.updateUserInfo(userInfo);
        return "ok";
    }

    @GetMapping("deluser")
    public String dleuserinfo() {
        userService.delUserInfo();
        return "ok";
    }

}
