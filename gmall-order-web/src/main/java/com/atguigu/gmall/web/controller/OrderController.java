package com.atguigu.gmall.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.utlis.HttpClientUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    ManageService manageService;


    @GetMapping("trade")
    @LoginRequire
    public String getUserAddressList(HttpServletRequest request) {
        //差用户地址
        String userId = (String)request.getAttribute("userId");
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        request.setAttribute("userAddressList",userAddressList);
        //从redis差选中的列表对象
        List<CartInfo> cartInfoList = orderService.getCheckedCartList(userId);
        request.setAttribute("cartInfoList",cartInfoList);
        //算出总额度来显示
        BigDecimal totalOrderCount =new BigDecimal(0);
        for (CartInfo cartInfo : cartInfoList) {
            totalOrderCount=totalOrderCount.add(cartInfo.getTotalCount()) ;
        }
        request.setAttribute("totalOrderCount",totalOrderCount);
        //设置一个UUID防止重复提交
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);
        return "trade";
    }

    @PostMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        //验重复提交 1验价钱 2验库存  3保存订单成功 4把购物车选中的信息去掉 清空redis选中列表 删除验证码
        String tradeNo = request.getParameter("tradeNo");
        String userId = (String) request.getAttribute("userId");
        // 装了一个Userid
        orderInfo.setUserId(userId);
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        //进程状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        //验重复提交
        if(tradeNo!=null){
            boolean checkTradeNo = orderService.checkTradeNo(userId, tradeNo);
            if(!checkTradeNo){
                request.setAttribute("errMsg","结算页面过期或已失效，请重新结算。");
                return "tradeFail";
            }

        }
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if(orderDetailList!=null){
            for (OrderDetail orderDetail : orderDetailList) {
                SkuInfo skuInfoById = manageService.getSkuInfoById(orderDetail.getSkuId());
                //1验价钱
                if((orderDetail.getOrderPrice().compareTo(skuInfoById.getPrice()))!=0){
                    request.setAttribute("errMsg","商品价钱已变更["+orderDetail.getSkuName()+"]，请重新结算。");
                    return "tradeFail";
                }
                //2验库存
                String hasStock = HttpClientUtil.doGet("http://www.gware.com//hasStock?skuId=" + orderDetail.getSkuId() + "&num=" + orderDetail.getSkuNum());
                if (hasStock==null||hasStock.equals("0")){
                    request.setAttribute("errMsg","商品["+skuInfoById.getSkuName()+"]暂时缺货。");
                    return "tradeFail";
                }


            }
        }

        orderInfo.sumTotalAmount();
        orderService.saveOrder(orderInfo);
        //4把购物车选中的信息去掉
        cartService.delCartCheckedList(userId);
        // 删除验证码
        orderService.delTradeNo(userId);

        return "redirect://payment.gmall.com/index";
    }
}
