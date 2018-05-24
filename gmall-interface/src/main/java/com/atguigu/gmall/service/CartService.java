package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {


    // 用户登录的时候查询数据库查询缓存 更新数据库更新缓存
    public CartInfo addToCart(CartInfo cartInfo);

    public List<CartInfo> getCartList(String userId);

    //cookie和数据库中的cartinfo中的List合并
    public List<CartInfo> mergeToCart(List<CartInfo> cartInfoList,String userId);
    //点击购物车中每个商品的勾选按钮 在redis中维护的
    public void   checkCart(String skuId, String userId, String isChecked);

    //订单保存后清数据库 和缓存中的内容
    public void delCartCheckedList(String userId);

}
