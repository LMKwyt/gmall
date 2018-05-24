package com.atguigu.gmall.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.web.handler.CartCookieHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {

    @Reference
    CartService cartService;
    @Reference
    ManageService manageService;
    @Autowired
    CartCookieHandler cartCookieHandler;

    @RequestMapping(value = "checkCart",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public String checkCart(HttpServletRequest request,HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        if(userId!=null) {

            cartService.checkCart(skuId, userId, isChecked);
        }else{
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }

        return "success";
    }

    @GetMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        String userId =(String) request.getAttribute("userId");
        List<CartInfo> cartCookieList = cartCookieHandler.getCartList(request);
        if(cartCookieList!=null&&cartCookieList.size()>0) {
            List<CartInfo> cartList = cartService.mergeToCart( cartCookieList,userId );
            for (CartInfo cartInfoCookie : cartCookieList) {
                if(cartInfoCookie.getIsChecked().equals("1")){
                    //将COOKIE中的选中的放入refis 列表
                    cartService.checkCart(cartInfoCookie.getSkuId(), userId, cartInfoCookie.getIsChecked());
                }
            }
            cartCookieHandler.delCartList(request,response);
        }

        return "redirect://order.gmall.com/trade";
    }



    @RequestMapping(value = "addToCart",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response, CartInfo cartInfo){

        SkuInfo skuInfoById = manageService.getSkuInfoById(cartInfo.getSkuId());
       String userId = (String)request.getAttribute("userId");
        cartInfo.setSkuName(skuInfoById.getSkuName());
        cartInfo.setImgUrl(skuInfoById.getSkuDefaultImg());
        cartInfo.setCartPrice(skuInfoById.getPrice());
        if(userId!=null){
            cartInfo.setUserId(userId);
             cartService.addToCart(cartInfo);
        }else {
             cartCookieHandler.addToCart( cartInfo,request,response);
        }
         request.setAttribute("cartInfo",cartInfo);

         return "success";
    }
    @GetMapping("cartList")
    @LoginRequire(autoRedirect = false )
    public String getCartList(HttpServletRequest request,HttpServletResponse response){
        String userId =(String) request.getAttribute("userId");
        List<CartInfo> cartInfoList=null;
        //从cookie 中拿到cartinfo 集合
        List<CartInfo> cartInfoListCookie= cartCookieHandler.getCartList(request);

        if(userId!=null){
            if(cartInfoListCookie==null||cartInfoListCookie.size()==0){
                cartInfoList=  cartService.getCartList(userId);
            }else{
                cartInfoList= cartService.mergeToCart(cartInfoListCookie,userId);
                cartCookieHandler.delCartList(request,response);
            }

        }else {
            cartInfoList = cartInfoListCookie;
        }
        request.setAttribute("cartList",cartInfoList);
        return "cartList";
    }


}
