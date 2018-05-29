package com.atguigu.gmall.task;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.Executor;

@Component
@EnableScheduling
public class OrderTask {

    @Autowired
    OrderService orderService;

    @Scheduled(cron = "0/2 * * * * ?")
    public void test1() {
        System.out.println("开始检查过期订单！");
        System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());


        //查询未支付订单sy
        long time1 = System.currentTimeMillis();
        List<OrderInfo> orderInfoList = orderService.checkExpireOrder();
        if (orderInfoList==null||orderInfoList.size()==0){

            return;
        }
        for (OrderInfo orderInfo : orderInfoList) {
            //查询支付模块的状态
            //更新订单状态
             orderService.handleExpireOrder(orderInfo);

        }
        long time2 = System.currentTimeMillis();
        System.out.println("执行时间 = " + (time2-time1));

    }



    @Bean
    public Executor executor(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler =new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
       return  threadPoolTaskScheduler;

    }



}
