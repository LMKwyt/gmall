package com.atguigu.gmall.service.orderweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {
    @Reference
    UserService userService;
    @GetMapping("getUserAddress")
    @ResponseBody
    public String getUserAddressList(@RequestParam("userId") String userId){
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        return JSON.toJSONString(userAddressList);
    }

}
