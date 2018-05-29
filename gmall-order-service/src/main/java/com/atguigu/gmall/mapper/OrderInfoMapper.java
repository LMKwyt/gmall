package com.atguigu.gmall.mapper;

import com.atguigu.gmall.bean.OrderInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface OrderInfoMapper extends Mapper<OrderInfo> {
   List<OrderInfo> getOrderListByUser(Long userId);
}
