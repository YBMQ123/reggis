package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/order")
@RestController
@Slf4j
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 下单功能
     * @param orders
     * @return
     */
    @PostMapping("submit")
    public R<String> submit(@RequestBody Orders orders,HttpSession session){
        log.info(orders.toString());
        ordersService.submit(orders,session);
        return R.success("下单成功");
    }

    /**
     * 订单的分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize,HttpSession session){
        //获取当前用户的id
        Long userId = BaseContext.getId();
        //定义一个订单号变量
        //通过用户id查询订单对象
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,userId);
        List<Orders> ordersList = ordersService.list(queryWrapper);
//        LocalDateTime time= (LocalDateTime) session.getAttribute("nowTime");
        //构造分页构造器
        Page<OrderDetail> pageInfo=new Page<>(page,pageSize);
        System.out.println("订单"+ordersList.toString());
        List<OrderDetail> orderDetailList=new ArrayList<>();
        for (Orders orders : ordersList) {
//            LocalDateTime orderTime = orders.getOrderTime();
//            if (orderTime.isEqual(time)){
                String number = orders.getNumber();
                //构造条件构造器
                LambdaQueryWrapper<OrderDetail> queryWrapperDetail=new LambdaQueryWrapper<>();
                queryWrapperDetail.eq(OrderDetail::getOrderId,Long.valueOf(number));
                //执行查询
                List<OrderDetail> list = orderDetailService.list(queryWrapperDetail);
                orderDetailList.addAll(list);
//            }
        }
        pageInfo.setRecords(orderDetailList);
        System.out.println("当前订单为："+orderDetailList.toString());
        return R.success(pageInfo);
    }
}
