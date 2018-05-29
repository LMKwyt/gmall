package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {
    //查询选中的集合列表从redis中
    public List<CartInfo> getCheckedCartList(String userId);

    //删除页面标识
    public void delTradeNo(String userId);

    //检查页面标识
    public boolean checkTradeNo(String userId, String TradeNo);

    //生成页面标识
    public String getTradeNo(String userId);

    //保存订单 里面有订单详情
    public String saveOrder(OrderInfo orderInfo);

    //根据用户ID 查询所有的订单集合
    public List<OrderInfo>   getOrderListByUser(String userId);

   //根据订单主键查询订单
    public OrderInfo getOrderInfo(String id);



   //更改订单状态
    public void updateStatus(String orderId, ProcessStatus processStatus);


   //发送给库存的消息队列
    public void sendOrderResult(String orderId);

   //；轮训查看订单状态
    public List<OrderInfo> checkExpireOrder();

 //执行每个订单 用异步的方法
    public void handleExpireOrder(OrderInfo orderInfo);

    //进行拆单处理
    public List<Map> orderSplit(String orderId, List<Map> mapList);
}
