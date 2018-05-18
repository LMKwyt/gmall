package com.atguigu.gmall.manage.consts;

public class RedisConst {

    /*
     *redisde前缀
     */
    public static String SKUKEY_SUFFIX="sku:";
    /*
     *redisde后缀
     */
    public static String SKUKEY_PREFIX="info";
    /**
     *redis超时时间
     */
    public static int SKUKEY_TIMEOUT=60;
    /**
     * redis加锁的后缀
     */
    public static String SKULOCK_PREFIX=":lock";
    public static String SKULOCK_EXPIRE_PX="";


}
