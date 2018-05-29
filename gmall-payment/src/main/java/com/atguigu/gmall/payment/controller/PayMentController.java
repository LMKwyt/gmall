package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.PaymentStatus;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PayMentController {

    @Reference
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;

    @RequestMapping("index")
    public String index(@RequestParam("orderId") String orderId, Model model) {
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        model.addAttribute("orderId", orderId);
        model.addAttribute("totalAmount", orderInfo.getTotalAmount());
        return "index";
    }


    @RequestMapping(value = "/alipay/submit", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> paymentAlipay(HttpServletRequest request, HttpServletResponse response) {
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        if (orderInfo == null) {
            return ResponseEntity.badRequest().build();
        }

        PaymentInfo paymentInfo = new PaymentInfo();
        //支付订单创建时间
        paymentInfo.setCreateTime(new Date());
        //支付订单设置外部单号
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        //支付订单设置订单ID
        paymentInfo.setOrderId(orderId);
        //支付订单设置订单总数
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        //支付订单设置开始状态
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        //支付订单设置订单描述
        paymentInfo.setSubject(orderInfo.getOrderSubject());
        //保存
        paymentService.savePaymentInfo(paymentInfo);
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        //设置返回地址
        alipayRequest.setReturnUrl(AlipayConfig.return_order_url);
        //设置异步回调地址
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        //数据封装必须参数
        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", orderInfo.getTotalAmount());
        map.put("subject", orderInfo.getOrderSubject());
        String bizContent = JSON.toJSONString(map);
        alipayRequest.setBizContent(bizContent);


        //阿里支付宝执行
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        paymentService.sendDelayCheck(orderInfo.getOutTradeNo(),3L);
        response.setContentType("text/html;charset=UTF-8");
        return ResponseEntity.ok(form);


    }

    @RequestMapping("/alipay/callback/notify")
    @ResponseBody
    public String callbackNotify(@RequestParam Map<String, String> paramMap, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("开始回调 = " + paramMap);
        //1验证 真伪
        boolean isCheckPass = false;
        System.out.println("验证签名-------------!");
        try {                                      //数据 阿里云公钥，编码机 ，加密算法
            isCheckPass = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (!isCheckPass) {
            System.out.println("验证失败-------------!");
            return "fail";

        }
        // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure

        System.out.println("验证状态-------------!");
        //2 验证状态
        String trade_status = paramMap.get("trade_status");
        if (!"TRADE_SUCCESS".equals(trade_status) && !"TRADE_FINISHED".equals(trade_status)) {
            return "fail";

        }

        //3订单标号和价钱
        System.out.println("支付成功!!!!!!!!!!");
        String totalAmountStr = paramMap.get("total_amount");
        BigDecimal totalAmount = new BigDecimal(totalAmountStr);

        String outTradeNo = paramMap.get("out_trade_no");
        PaymentInfo paymentQuery = new PaymentInfo();
        paymentQuery.setOutTradeNo(outTradeNo);

        PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentQuery);
        //比较价钱
        if (totalAmount.compareTo(paymentInfo.getTotalAmount()) == 0) {
            System.out.println("金额核对成功!!!!!!!!!!");
            //验证通过
            if (paymentInfo.getPaymentStatus() == PaymentStatus.UNPAID) {
                // 更改状态
                paymentInfo.setPaymentStatus(PaymentStatus.PAID);
                //从支付接口中得到支付宝的单号 和支付宝单据的创建时间和描述
                //设置到支付订单中保存起来
                String alipayTradeNo = paramMap.get("trade_no");
                paymentInfo.setAlipayTradeNo(alipayTradeNo);
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setCallbackContent(JSON.toJSONString(paramMap));
                paymentService.updateByPrimaryKey(paymentInfo);

                //通知订单模块
                //消息队列异步发送   更改订单状态
                System.out.println("发送通知!!!!!!!!!!");
                //更新状态 已完成
                System.out.println("返回!!!!!!!!!!");
                return "success";
            } else if (paymentInfo.getPaymentStatus() == PaymentStatus.PAID) {
                //这用来体现接口的   幂等性
                //发生在你这处理订单 但是有发生了一次请求 这个请求应该不被处理 直接返回
                System.out.println("已认证支付通过，直接返回!!!!!!!!!!");
                return "success";
            }

        }

        return "failure";

    }


    @PostMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(String orderId) {
        paymentService.sendPaymentResult(orderId);
        return "send success";
    }

    @GetMapping("/alipay/callback/return")
    public String  callbackReturn(){
        return "redirect://order.gmall.com/list";
    }


}
