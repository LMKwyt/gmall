package com.atguigu.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.utils.RedisUtils;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    RedisUtils redisUtils;

    public void delCartCheckedList(String userId){
        String checkedKey="user:"+userId+":checked";
        String cartKey="user:"+userId+":cart";
        Jedis jedis = redisUtils.getJedis();
        Set<String> skuIdSet = jedis.hkeys(checkedKey);



        for (String skuId : skuIdSet) {

            CartInfo cartInfoQuery=new CartInfo();
            cartInfoQuery.setUserId(userId);
            cartInfoQuery.setSkuId(skuId);
            cartInfoMapper.delete(cartInfoQuery);

            jedis.hdel(cartKey,skuId);
        }
        jedis.del(checkedKey);

        jedis.close();


    }



    public List<CartInfo> mergeToCart(List<CartInfo> cartInfoListCookie,String userId){
        //从数据库中查询列表数据
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectcartInfoListByUserId(Long.parseLong(userId));
          //以数据库中的列表数据为基础 遍历整个集合
                if(cartInfoListDB!=null&&cartInfoListDB.size()>0){
                    for (CartInfo cartInfoCookie : cartInfoListCookie) {
                        //增加一个标识 如果
                        boolean flag=false;
                        for (CartInfo cartInfoDB : cartInfoListDB) {
                            if(cartInfoCookie.getSkuId().equals(cartInfoDB.getSkuId())){
                                cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfoCookie.getSkuNum());
                                //跟新数据之后更新数据库
                                cartInfoMapper.updateByPrimaryKey(cartInfoDB);
                                flag=true;
                            }
                        }
                        //如果都没有匹配上 false 匹配上标识为
                        if (!flag){
                            //将cookie中的值拿出来 放到数据库中去
                            cartInfoCookie.setUserId(userId);
                            cartInfoMapper.insertSelective(cartInfoCookie);
                        }

                    }

                }

        //2 缓存重新加载   更新redis中的列表数据
        List<CartInfo> cartInfoList = loadCartCache(userId);

        return cartInfoList;
    }
    public List<CartInfo> getCartList(String userId) {
        //1 先从缓存取 购物车列表
        Jedis jedis = redisUtils.getJedis();
        String cartKey = "user:" + userId + ":cart";
        List<String> cartJsonList = jedis.hvals(cartKey);
        jedis.close();
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (cartJsonList != null && cartJsonList.size() > 0) {
            //2 如果能取到 反序列化 返回
            for (String cartJson : cartJsonList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
        } else {
            //3 如果取不到 从数据库中取值 同时加载进缓存
            cartInfoList = loadCartCache(userId);
        }
        cartInfoList.sort(new Comparator<CartInfo>() {// 快排
            @Override
            public int compare(CartInfo o1, CartInfo o2) {
                return o2.getId().compareTo(o1.getId());
            }
        });

        return cartInfoList;


    }

    public List<CartInfo> loadCartCache(String userId) {
        List<CartInfo> cartInfoList = cartInfoMapper.selectcartInfoListByUserId(Long.parseLong(userId));
        Map cartMap = new HashMap(cartInfoList.size());
        if (cartInfoList != null && cartInfoList.size() > 0) {
            for (CartInfo cartInfo : cartInfoList) {
                //将每一个cartinfo变成JSON字符串
                String cartJson = JSON.toJSONString(cartInfo);
                //将每一个cartinfo字符 以ID为key json为value
                cartMap.put(cartInfo.getSkuId(), cartJson);
            }

        }
        //放入缓存更新
        Jedis jedis = redisUtils.getJedis();
        String cartKey = "user:" + userId + ":cart";
        jedis.hmset(cartKey, cartMap);
        String userInfoKey = "user:" + userId + ":info";
        //使用户跟商品列表的时间相同
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(cartKey, ttl.intValue());
        jedis.close();
        return cartInfoList;
    }

    public CartInfo addToCart(CartInfo cartInfo) {
        //插入购物车时先查询购物车中有没有这个购物项目
        CartInfo cartInfoQuery = new CartInfo();
        cartInfoQuery.setUserId(cartInfo.getUserId());
        cartInfoQuery.setSkuId(cartInfo.getSkuId());
        CartInfo cartInfoDB = cartInfoMapper.selectOne(cartInfoQuery);
        if (cartInfoDB != null) {
            //库中有值
            cartInfoDB.setSkuNum(cartInfoDB.getSkuNum() + cartInfo.getSkuNum());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
        } else {
            //库中无值
            cartInfoMapper.insertSelective(cartInfo);

        }
     //刷新缓存在查
        loadCartCache(cartInfo.getUserId());
        //redis 操作
        String userCartKey = "user:" + cartInfo.getUserId() + ":cart";
        Jedis jedis = redisUtils.getJedis();
        //根据用户的ID 和skuId 唯一确定每一个商品列
        String hget = jedis.hget(userCartKey, cartInfo.getSkuId());

        if (hget != null && hget.length() > 0) {
            //redis有值
            CartInfo cartInfoRedis = JSON.parseObject(hget, CartInfo.class);
            cartInfoRedis.setSkuNum(cartInfoRedis.getSkuNum() + cartInfo.getSkuNum());
            String jsonString = JSON.toJSONString(cartInfoRedis);
            jedis.hset(userCartKey, cartInfo.getSkuId(), jsonString);

        } else {
            //redis无值
            String jsonString = JSON.toJSONString(cartInfo);
            jedis.hset(userCartKey, cartInfo.getSkuId(), jsonString);
        }

        jedis.close();

        return cartInfo;

    }


  public void   checkCart(String skuId, String userId, String isChecked){
      // 1 写入勾选的状态，
      Jedis jedis = redisUtils.getJedis();
      String cartKey="user:"+userId+":cart";
      //从缓存中拿到所有的 cart集合
      String cartJson = jedis.hget(cartKey, skuId);
      //将勾选的状态进行更新
      CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);

          cartInfo.setIsChecked(isChecked);
          String cartJsonNew = JSON.toJSONString(cartInfo);
          jedis.hset(cartKey, skuId, cartJsonNew);

      //2 建立有勾选状态的列表或者删除
      String checkedKey="user:"+userId+":checked";
      if("1".equals(isChecked)){
          jedis.hset(checkedKey, skuId,cartJsonNew);
      }else{
          jedis.hdel(checkedKey, skuId);
      }

      jedis.close();
  }


}
