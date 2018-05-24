package com.atguigu.gmall.web.handler;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.utils.CookieUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {
    int cartCookieMaxage=3600*24*7;
  public void addToCart(CartInfo cartInfo, HttpServletRequest request,HttpServletResponse response){
      //先从cookie中拿值 拿回来的是商品列表的集合JSON串
      String cookieValue = CookieUtil.getCookieValue(request, "cart", true);
      List<CartInfo> cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
      boolean flag=false;
      if(cartInfoList!=null&&cartInfoList.size()>0){
      for (CartInfo info : cartInfoList) {
          if (info.getSkuId().equals(cartInfo.getSkuId())) {
              info.setSkuNum(info.getSkuNum() + cartInfo.getSkuNum());
              flag = true;
             }
         }

      }else {

          cartInfoList=new ArrayList<>();

          }
      if(!flag){
          cartInfoList.add(cartInfo);
      }
      String cartInfoListNewJson = JSON.toJSONString(cartInfoList);
      CookieUtil.setCookie(request,response,"cart",cartInfoListNewJson,cartCookieMaxage,true);
  }

    public List<CartInfo> getCartList(HttpServletRequest request ){
        String cartListJson = CookieUtil.getCookieValue(request, "cart", true);
        List<CartInfo> cartInfoList = JSON.parseArray(cartListJson, CartInfo.class);
        return cartInfoList;

    }

    public void delCartList(HttpServletRequest request,HttpServletResponse response ) {
        CookieUtil.setCookie(request, response, "cart", null, 0, false);
    }

    public void checkCart(HttpServletRequest request,HttpServletResponse response ,String skuId,String isChecked) {
        List<CartInfo> cartList = getCartList(request);

        for (CartInfo cartInfo : cartList) {
            if (cartInfo.getSkuId().equals(skuId)) {
                cartInfo.setIsChecked(isChecked);
            }
        }
        String jsonString = JSON.toJSONString(cartList);
        CookieUtil.setCookie(request, response, "cart", jsonString, cartCookieMaxage, true);
    }


}
