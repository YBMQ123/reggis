package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.AddressBookService;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;
    /**
     * 订单支付功能
     * @param orders
     */
    @Override
    //多表的事务同步
    @Transactional
    public void submit(Orders orders,HttpSession session) {
        //定义一个用户的订单表封装数据
        Orders ordersUser=new Orders();
        //1.获取当前登入用户的id
        Long userId = BaseContext.getId();
        //2.查询当前用户的购物车信息
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        log.info("购物车信息"+list.toString());
        //定义一个变量保存用户点餐的数量
        Double number=0.0;
        for (ShoppingCart shoppingCart : list) {
            number=shoppingCart.getNumber()+number;
        }
        log.info(list.toString());
        //查询用户个人的信息
        LambdaQueryWrapper<AddressBook> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(AddressBook::getUserId,userId);
        List<AddressBook> addressBookList = addressBookService.list(queryWrapper1);
        //设置订单的id
        String orderId = String.valueOf(IdWorker.getId());
        for (AddressBook addressBook : addressBookList) {
            Integer isDefault = addressBook.getIsDefault();
            if (isDefault==1){
                //是用户设置的默认地址
                //保存用户的收货人和用户名
                ordersUser.setConsignee(addressBook.getConsignee());
                ordersUser.setUserName(addressBook.getConsignee());
                //保存订单号
                ordersUser.setNumber(orderId);
                //保存地址id
                ordersUser.setAddressBookId(orders.getAddressBookId());
                //保存当前登入的用户id
                ordersUser.setUserId(userId);
                //保存下单的时间和支付的时间
                ordersUser.setOrderTime(LocalDateTime.now());
                session.setAttribute("nowTime",LocalDateTime.now());
                //把当前线程的下单时间保存起来
                ordersUser.setCheckoutTime(LocalDateTime.now());
                //保存用户的支付方式
                ordersUser.setPayMethod(orders.getPayMethod());
                //保存用户的备注
                ordersUser.setRemark(orders.getRemark());
                //用户收货的电话
                ordersUser.setPhone(addressBook.getPhone());
                //用户收货的地址
                ordersUser.setAddress(addressBook.getDetail());
                //订单的状态，默认为2待派送
                ordersUser.setStatus(2);
                //用户点餐的总金额
                ordersUser.setAmount(number);
            }
        }
        log.info(addressBookList.toString());
        //3.向订单表中插入数据，一次订单一条数据
        ordersService.save(ordersUser);
        //4.向订单明细表插入数据，多条数据
        for (ShoppingCart shoppingCart : list) {
            //创建OrderDetail封装订单的详细信息
            OrderDetail orderDetail=new OrderDetail();
            log.info(shoppingCart.toString());
            //名称
            orderDetail.setName(shoppingCart.getName());
            //订单id
            orderDetail.setOrderId(Long.valueOf(orderId));
            //菜品id
            orderDetail.setDishId(shoppingCart.getDishId());
            //套餐id
            orderDetail.setSetmealId(shoppingCart.getSetmealId());
            //口味
            orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            //数量
            orderDetail.setNumber(shoppingCart.getNumber());
            //金额
            orderDetail.setAmount(shoppingCart.getAmount());
            //图片
            orderDetail.setImage(shoppingCart.getImage());
            log.info("订单明细"+orderDetail.toString());
            orderDetailService.save(orderDetail);
            System.out.println("订单明细表"+orderDetailService.list().toString());
        }
        log.info("购物车信息"+shoppingCartService.list(queryWrapper).toString());
        //5.清空该用户的购物车
        shoppingCartService.remove(queryWrapper);
        System.out.println("订单明细表"+orderDetailService.list().toString());
    }
}
