package com.atguigu.gmall.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:disabled}")
    String host;
    @Value("${spring.redis.port:0}")
    int port;
    @Bean
    public RedisUtils getRedisUtils(){
        if(host.equals("disabled")){
            return null;
        }
        RedisUtils redisUtils=new RedisUtils();
        redisUtils.initRedis(host,port);
        return redisUtils;
    }

}

