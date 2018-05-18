package com.atguigu.gmall.utils;

import redis.clients.jedis.Jedis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtils {

    private JedisPool jedisPool;

    public  void initRedis(String host,int port) {
        JedisPoolConfig jedisPoolConfig =new JedisPoolConfig();
        //最大连接数
        jedisPoolConfig.setMaxTotal(20);
        //最小的空闲连接
        jedisPoolConfig.setMinIdle(5);
        //最多的空闲连接
        jedisPoolConfig.setMaxIdle(15);
        //发生错误是否等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        //测试连接
        jedisPoolConfig.setTestOnBorrow(true);
        //最大的等待时长
        jedisPoolConfig.setMaxWaitMillis(300);
        jedisPool =  new JedisPool(jedisPoolConfig,host,port,500 );

    }

    public  Jedis getJedis(){
        Jedis jedisPoolResource = jedisPool.getResource();
        System.err.println("jedisPool.getNumActive() = " + jedisPool.getNumActive());
        System.err.println("jedisPool.getNumIdle() = " + jedisPool.getNumIdle());
        return jedisPoolResource;
    }
}
