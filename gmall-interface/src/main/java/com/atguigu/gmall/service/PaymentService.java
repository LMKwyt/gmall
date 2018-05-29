package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentService {

    //保存支付订单
    public void savePaymentInfo(PaymentInfo paymentInfo);

    //更改支付订单的状态
    public void updateByPrimaryKey(PaymentInfo paymentInfo);

    //获取支付订单
    public PaymentInfo getPaymentInfo(PaymentInfo paymentQuery);

    //发送消息队列到订单模块
    public void sendPaymentResult(String orderId);

    //消息队列延时加载询问支付宝
    public void sendDelayCheck(String outTradeNo, Long checkCount);


    public boolean checkAlipayQuery(PaymentInfo paymentInfo);

}
