package com.atguigu.gmall.utils;

import io.jsonwebtoken.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    String sssss ="1";
    //编译生成特殊的认证中心的令牌
    public static String encode(String key, Map<String,Object> param, String salt){
        //一个JWT由三个部分组成：公共部分、私有部分、签名部分。最后由这三者组合进行base64编码得到JWT。
        //Json Web token
        //判断是否有盐值加密
        if(salt!=null){
            key+=salt;
        }
//        1、	公共部分
//        主要是该JWT的相关配置参数，比如签名的加密算法、格式类型、过期时间等等。
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256,key);
//        2、	私有部分
//        用户自定义的内容，根据实际需要真正要封装的信息。
        jwtBuilder = jwtBuilder.setClaims(param);

//        3、	签名部分
//        根据用户信息+盐值+密钥生成的签名。如果想知道JWT是否是真实的只要把JWT的信息取出来，加上盐值和服务器中的密钥就可以验证真伪。所以不管由谁保存JWT，只要没有密钥就无法伪造。
        String token = jwtBuilder.compact();
        return token;


    }
    //进行解密功能
    public  static Map<String,Object>  decode(String token ,String key,String salt){
        Claims claims =null;
        if(salt!=null){
            key+=salt;
        }
        try {
            claims=Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        }catch (JwtException e){
            e.printStackTrace();
        }

             return  claims;

    }

    @Test
    public void ads(){

        Map map =new HashMap();
        map.put("userId",1);
        map.put("name","asd");
        String encode = JwtUtil.encode(sssss, map, "192");
        System.out.println(encode);
        Map<String, Object> decode = JwtUtil.decode(encode, sssss, "192");
        System.out.println(decode);

    }
}
