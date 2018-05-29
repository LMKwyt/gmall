package com.atguigu.gmall.payment.consumer;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.PaymentStatus;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentConsumer {
    @Autowired
    PaymentService paymentService;
    @JmsListener(containerFactory = "jmsQueueListener" ,destination = "CHECK_PAYMENT_QUERY_QUEUE")
    public void checkPaymentQuery(MapMessage mapMessage) throws JMSException {

        //获取两个参数
        String outTradeNo = mapMessage.getString("outTradeNo");
        Long checkCount=mapMessage.getLong("checkCount");
        //访问支付宝接口
        System.out.println("checkCount = " + checkCount);
        System.out.println("outTradeNo = " + outTradeNo);

        System.out.println("检查本地数据库 " );
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOutTradeNo(outTradeNo);

        PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);
        if (paymentInfo.getPaymentStatus() == PaymentStatus.PAID || paymentInfo.getPaymentStatus() ==PaymentStatus.ClOSED){
            return  ;

        }

        System.out.println("支付状态 = " + paymentInfo.getPaymentStatus().name());
        boolean ifSuccess = paymentService.checkAlipayQuery(paymentInfo);




        //3 根据结果处理   1成功 更新状态  结束  2 没成功 继续发生延迟队列 次数-1  如果次数<=0 不发送
     if(!ifSuccess){
         System.out.println("未支付成功 " );
         if(checkCount>0){
             checkCount--;
             System.out.println("再次提交延迟队列" );
             paymentService.sendDelayCheck(outTradeNo,checkCount);
         }

     }


    }


}


