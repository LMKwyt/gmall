package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderInfo;

import java.util.List;

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
}
