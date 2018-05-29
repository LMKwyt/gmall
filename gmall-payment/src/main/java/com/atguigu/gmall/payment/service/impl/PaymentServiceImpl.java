package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.PaymentStatus;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.utils.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;



import javax.jms.*;
import java.util.Date;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;


    public boolean checkAlipayQuery(PaymentInfo paymentInfo){
        System.out.println("开始查询支付宝！！ "  );
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\""+paymentInfo.getOutTradeNo()+ "\" }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
            if( "TRADE_SUCCESS".equals( response.getTradeStatus())){
                System.out.println("支付成功");
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setCallbackContent(response.getBody());
                paymentInfo.setAlipayTradeNo(response.getTradeNo());
                paymentInfo.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoMapper.updateByPrimaryKeySelective(paymentInfo);
                return true;
            }
        } else {
            System.out.println("调用失败");
        }
        return false;
    }

    public void savePaymentInfo(PaymentInfo paymentInfo){
        //必须保证每个订单只有唯一的支付信息，所以如果之前已经有了该笔订单的支付信息，那么只更新时间
        PaymentInfo paymentInfoQuery=new PaymentInfo();
        paymentInfoQuery.setOrderId(paymentInfo.getOrderId());

        PaymentInfo paymentInfoExists = paymentInfoMapper.selectOne(paymentInfoQuery);
        if(paymentInfoExists!=null){
            paymentInfoExists.setCreateTime(new Date());
            paymentInfoMapper.updateByPrimaryKey(paymentInfoExists);
            return;
        }

        paymentInfo.setCreateTime(new Date());
        paymentInfoMapper.insertSelective(paymentInfo);

    }
    public void updateByPrimaryKey(PaymentInfo paymentInfo) {
        paymentInfoMapper.updateByPrimaryKeySelective(paymentInfo);
    }

    public PaymentInfo getPaymentInfo(PaymentInfo paymentQuery){
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(paymentQuery);
        return paymentInfo;
    }

    public void sendPaymentResult(String orderId){
        //获取连接
        Connection conn = activeMQUtil.getConn();
        try {
            //一次会话管道
            Session session = conn.createSession(true, Session.SESSION_TRANSACTED);
            //创建一个消息队列
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_QUEUE");
            //创建一个消息生产者
            MessageProducer producer = session.createProducer(paymentResultQueue);
            MapMessage mapMessage=new ActiveMQMapMessage();
            mapMessage.setString("orderId",orderId);
            mapMessage.setString("result","success");
            producer.send(mapMessage);
            session.commit();

            session.close();
            conn.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void sendDelayCheck(String outTradeNo,Long checkCount){
        //获取连接
        Connection conn = activeMQUtil.getConn();
        try {
            //一次会话管道
            Session session = conn.createSession(true, Session.SESSION_TRANSACTED);
            //创建一个消息队列
            Queue paymentResultQueue = session.createQueue("CHECK_PAYMENT_QUERY_QUEUE");
            //创建一个消息生产者
            MessageProducer producer = session.createProducer(paymentResultQueue);
            MapMessage mapMessage=new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo",outTradeNo);
            mapMessage.setLong("checkCount",checkCount);
            //延时加载的时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,10*1000);
            producer.send(mapMessage);
            session.commit();

            session.close();
            conn.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
