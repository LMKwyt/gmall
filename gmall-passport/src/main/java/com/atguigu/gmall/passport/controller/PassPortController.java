package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.utils.JwtUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassPortController {

    @Reference
    UserService userService;
    @Value("${passport.key}")
    String passPortKey;

    @GetMapping("index")
    public String index(HttpServletRequest request) {

        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl", originUrl);
        return "index";
    }

    @PostMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, Model model, HttpServletRequest request) {
        String remoteAddr = request.getHeader("x-forwarded-for");
        String MD5passwd = DigestUtils.md5Hex(userInfo.getPasswd());
        userInfo.setPasswd(MD5passwd);

        UserInfo loginInfo = userService.login(userInfo);
        String UserId = loginInfo.getId();

        if (UserId == null) {
            return "fail";
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", loginInfo.getId());
            map.put("nickName", loginInfo.getNickName());
            //令牌签发
            String token = JwtUtil.encode(passPortKey, map, remoteAddr);
            return token;

        }


    }


    @RequestMapping(value = "verify")
    @ResponseBody
    public String verify(@RequestParam("token") String token, @RequestParam("currentIp") String currentIp) {


        Map<String, Object> map = JwtUtil.decode(token, passPortKey, currentIp);
        //1 检查token的正确性
        Map userMap = JwtUtil.decode(token, passPortKey, currentIp);
        if (userMap == null) {
            return "fail";
        }
        String userId = (String) userMap.get("userId");

        //2 用userId 去查询后台登录信息
        boolean verify = userService.verify(userId);
        if (verify) {
            //延长用户的redis的时间
            return "success";
        } else {
            return "fail";
        }


    }


}
