package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

import javax.servlet.http.HttpSession;

public interface OrdersService extends IService<Orders> {
    //订单支付功能
    public void submit(Orders orders, HttpSession session);
}
