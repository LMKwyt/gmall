package com.atguigu.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.mapper.OrderDetailMapper;
import com.atguigu.gmall.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.utils.ActiveMQUtil;
import com.atguigu.gmall.utils.RedisUtils;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;


@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Reference
    PaymentService paymentService;

    public List<Map> orderSplit(String orderId, List<Map> mapList){
        //1  根据orderId 查询orderInfo
        OrderInfo orderInfo = getOrderInfo(orderId);
        List<Map> subOrderMapList=new ArrayList<>();
        //2  根据 wareSkuMapList +orderInfo 生成 子订单主表 生成新的orderId  附上父订单id
        for (Map wareSkuMap : mapList) {
            //主表
            OrderInfo subOrderInfo = new OrderInfo();

            BeanUtils.copyProperties(orderInfo,subOrderInfo);
            subOrderInfo.setId(null);
            subOrderInfo.setParentOrderId(orderInfo.getId());
            //子订单 明细表
            //3 根据 wareSkuMapList 中的skuIds  生成子订单的明细表
            String wareId = (String) wareSkuMap.get("wareId");
            subOrderInfo.setWareId(wareId);
            List<String> skuIds = ( List<String>)wareSkuMap.get("skuIds");
            List<OrderDetail> subOrderDetailList=new ArrayList<>();
            List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

            for (String skuId : skuIds) {
                for (OrderDetail orderDetail : orderDetailList) {
                    if(skuId.equals(orderDetail.getId())){
                        orderDetail.setId(null);
                        orderDetail.setOrderId(null);
                        subOrderDetailList.add(orderDetail);

                    }

                }
            }
            subOrderInfo.setOrderDetailList(subOrderDetailList);
            //4 保存子订单 主表 + 子表  利用save方法
            saveOrder(subOrderInfo);
            //6 把子订单列表 编程List<Map>结构  返回
            Map subOrderMap = initWareMap(subOrderInfo);
            subOrderMapList.add(subOrderMap);

        }
        //5更新原始订单装态为已拆分
        updateStatus(orderInfo.getId(),ProcessStatus.SPLIT);
        return subOrderMapList;

    }
    public List<OrderInfo> checkExpireOrder() {

        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("processStatus", ProcessStatus.UNPAID.name()).andLessThan("expireTime", new Date());

        List<OrderInfo> orderInfoList = orderInfoMapper.selectByExample(example);
        return orderInfoList;
    }

    @Async
    public void handleExpireOrder(OrderInfo orderInfo) {
        ///
/*        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        System.out.println("处理订单：" + orderInfo.getId());
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOrderId(orderInfo.getId());
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);
        if (paymentInfo == null) {
            return;
        }
        if (paymentInfo.getPaymentStatus() == PaymentStatus.PAID) {
            System.out.println("订单已支付：" + orderInfo.getId());
            updateStatus(orderInfo.getId(), ProcessStatus.PAID);
            //发送库存
        } else {
            System.out.println("订单未支付,关闭订单：" + orderInfo.getId());
            updateStatus(orderInfo.getId(), ProcessStatus.CLOSED);
        }

    }


    public void updateStatus(String orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);

        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);

    }


    public void sendOrderResult(String orderId) {
        OrderInfo orderInfo = getOrderInfo(orderId);
        //装配数据
        Map orderMap = initWareMap(orderInfo);
        //转json
        String wareOrderJson = JSON.toJSONString(orderMap);

        //发送消息
        Connection conn = activeMQUtil.getConn();
        try {
            Session session = conn.createSession(true, Session.SESSION_TRANSACTED);
            Queue orderResultQueue = session.createQueue("ORDER_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(orderResultQueue);
            TextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText(wareOrderJson);

            producer.send(textMessage);
            session.commit();
            session.close();
            conn.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public Map initWareMap(OrderInfo orderInfo) {
        Map orderMap = new HashMap();

        orderMap.put("orderId", orderInfo.getId());
        orderMap.put("consignee", orderInfo.getConsignee());
        orderMap.put("consigneeTel", orderInfo.getConsigneeTel());
        orderMap.put("orderComment", orderInfo.getOrderComment());
        orderMap.put("orderBody", orderInfo.getOrderSubject());
        orderMap.put("deliveryAddress", orderInfo.getDeliveryAddress());
        orderMap.put("wareId",orderInfo.getWareId());
        orderMap.put("paymentWay", "2");
        List<Map> detailList = new ArrayList<>(orderInfo.getOrderDetailList().size());
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap = new HashMap();
            detailMap.put("skuId", orderDetail.getSkuId());
            detailMap.put("skuName", orderDetail.getSkuName());
            detailMap.put("skuNum", orderDetail.getSkuNum());
            detailList.add(detailMap);
        }

        orderMap.put("details", detailList);

        return orderMap;


    }


    public OrderInfo getOrderInfo(String id) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(id);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }

    public List<OrderInfo> getOrderListByUser(String userId) {
        List<OrderInfo> orderListByUser = orderInfoMapper.getOrderListByUser(Long.parseLong(userId));
        return orderListByUser;
    }


    public String saveOrder(OrderInfo orderInfo) {
        //保存时的时间
        orderInfo.setCreateTime(new Date());
        //过期时间
        orderInfo.setExpireTime(DateUtils.addDays(new Date(), 1));

        String outTradeNo = "ATGUIGU-" + System.currentTimeMillis() + "-" + orderInfo.getUserId();
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


    public List<CartInfo> getCheckedCartList(String userId) {
        Jedis jedis = redisUtils.getJedis();
        String checkKey = "user:" + userId + ":checked";
        List<String> cartInfoJson = jedis.hvals(checkKey);
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (cartInfoJson != null && cartInfoJson.size() > 0) {
            for (String s : cartInfoJson) {
                CartInfo cartInfo = JSON.parseObject(s, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
        }
        jedis.close();
        return cartInfoList;
    }


    public String getTradeNo(String userId) {
        //生成一个唯一标识
        Jedis jedis = redisUtils.getJedis();
        String userTradeNoKey = "user:" + userId + ":tradeNo";
        String TradeNo = UUID.randomUUID().toString();

        jedis.setex(userTradeNoKey, 600, TradeNo);
        jedis.close();
        return TradeNo;

    }

    public boolean checkTradeNo(String userId, String TradeNo) {
        Jedis jedis = redisUtils.getJedis();
        String userTradeNoKey = "user:" + userId + ":tradeNo";
        String TradeNoRedis = jedis.get(userTradeNoKey);
        jedis.close();
        if (TradeNoRedis != null && TradeNoRedis.equals(TradeNo)) {
            return true;
        }
        return false;
    }

    public void delTradeNo(String userId) {
        Jedis jedis = redisUtils.getJedis();
        String userTradeNoKey = "user:" + userId + ":tradeNo";
        jedis.del(userTradeNoKey);
        jedis.close();
    }
}
