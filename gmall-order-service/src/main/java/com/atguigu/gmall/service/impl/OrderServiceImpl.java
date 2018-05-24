package com.atguigu.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.mapper.OrderDetailMapper;
import com.atguigu.gmall.service.mapper.OrderInfoMapper;
import com.atguigu.gmall.utils.RedisUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    RedisUtils redisUtils;
    public  String saveOrder(OrderInfo orderInfo){
        //保存时的时间
        orderInfo.setCreateTime(new Date());
        //过期时间
        orderInfo.setExpireTime(DateUtils.addDays(new Date(),1));

        String outTradeNo="ATGUIGU-"+System.currentTimeMillis()+"-"+orderInfo.getUserId();
        //订单号！
        orderInfo.setOutTradeNo(outTradeNo);

        orderInfoMapper.insertSelective(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        return orderInfo.getId();

    }


    public List<CartInfo> getCheckedCartList(String userId){
        Jedis jedis = redisUtils.getJedis();
        String checkKey="user:"+userId+":checked";
        List<String> cartInfoJson = jedis.hvals(checkKey);
        List<CartInfo> cartInfoList=new ArrayList<>();
   if(cartInfoJson!=null&&cartInfoJson.size()>0) {
       for (String s : cartInfoJson) {
           CartInfo cartInfo = JSON.parseObject(s, CartInfo.class);
           cartInfoList.add(cartInfo);
       }
   }
        jedis.close();
        return cartInfoList;
    }


    public String getTradeNo(String userId){
        //生成一个唯一标识
        Jedis jedis = redisUtils.getJedis();
        String userTradeNoKey="user:"+userId+":tradeNo";
        String TradeNo = UUID.randomUUID().toString();

        jedis.setex(userTradeNoKey,600,TradeNo);
        jedis.close();
        return TradeNo;

    }

    public boolean checkTradeNo(String userId ,String TradeNo){
        Jedis jedis = redisUtils.getJedis();
        String userTradeNoKey="user:"+userId+":tradeNo";
        String TradeNoRedis = jedis.get(userTradeNoKey);
        jedis.close();
        if(TradeNoRedis!=null&&TradeNoRedis.equals(TradeNo)){
            return true;
        }
        return false;
    }
    public void delTradeNo(String userId){
        Jedis jedis = redisUtils.getJedis();
        String userTradeNoKey="user:"+userId+":tradeNo";
        jedis.del(userTradeNoKey);
        jedis.close();
    }
}
