package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.utils.CookieUtil;
import com.atguigu.gmall.utlis.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    int cookieAge = 3600 * 24 * 7;

    String verifyUrl = "http://passport.atguigu.com/verify";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String newToken = request.getParameter("newToken");
        if (newToken != null && newToken.length() > 0) {
            CookieUtil.setCookie(request, response, "token", newToken, cookieAge, false);
        }
        //1 进行如果能从cookie把token取出来，进行解析，显示页面上。
        String token = CookieUtil.getCookieValue(request, "token", false);
        String userId = null;
        if (token != null) {
            //1 公用 2 私有 3 签名

            /*  解析私有的显示到页面中拿的昵称*/

            String privateToken = StringUtils.substringBetween(token, ".");
            Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
            byte[] decode = base64UrlCodec.decode(privateToken);
            String MapJson = new String(decode, "UTF-8");
            Map map = JSON.parseObject(MapJson, Map.class);
            String nickName = (String) map.get("nickName");
            userId = (String) map.get("userId");
            request.setAttribute("nickName", nickName);
        }
        //3 如果这个请求需要认证登录  把token发送到认证中心 进行校验
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (methodAnnotation != null) {
            if (token != null) {
                String currentIp = request.getHeader("x-forwarded-for");
                String result = HttpClientUtil.doGet(verifyUrl + "?currentIp=" + currentIp + "&token=" + token);
                //4 认证中心返回认证结果  如果是fail 重定向到登录页面 如果是success 把userId 写入到request中
                if ("success".equals(result)) {
                    request.setAttribute("userId", userId);
                } else {

                    if(methodAnnotation.autoRedirect()){
                    String originUrl = request.getRequestURL().toString();
                    originUrl = URLEncoder.encode(originUrl, "utf-8");
                    response.sendRedirect("http://passport.atguigu.com/index?originUrl=" + originUrl);
                    //中断程序流程重定向后应该加
                    return false;
                }
                }
            } else {
                if(methodAnnotation.autoRedirect()) {
                    String originUrl = request.getRequestURL().toString();
                    originUrl = URLEncoder.encode(originUrl, "utf-8");
                    response.sendRedirect("http://passport.atguigu.com/index?originUrl=" + originUrl);
                    //中断程序流程重定向后应该加
                    return  false;
                }
            }
        }
        return true;
    }
}
