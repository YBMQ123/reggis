package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加到购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        //设置用户id。指定当前是那个用户的购物车数据
        Long CurrentId = BaseContext.getId();
        shoppingCart.setUserId(CurrentId);
        //查询当前菜品或者套餐是否在购物车里面
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, CurrentId);
        //判断加入购物车的是菜品还是套餐
        if (dishId != null) {
            //加入购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //加入购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //判断当前菜品或者套餐是否在购物车里面
        ShoppingCart serviceOne = shoppingCartService.getOne(queryWrapper);
        if (serviceOne != null) {
            //购物车中已经存在，进行数量加一
            Integer number = serviceOne.getNumber();
            serviceOne.setNumber(number + 1);
            shoppingCartService.updateById(serviceOne);
        } else {
            //购物车中不存在，是新增的数据
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            //添加到购物车里面
            shoppingCartService.save(shoppingCart);
            serviceOne = shoppingCart;
        }
        return R.success(serviceOne);
    }

    /**
     * 显示当前购物车的信息
     *
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        //设置条件，通过查询当前用户的id进行条件查询
        Long userId = BaseContext.getId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        log.info(list.toString());
        return R.success(list);
    }

    /**
     * 清空购物车的信息
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        //获取当前用户的id
        Long userID = BaseContext.getId();
        //根据id进行删除，进行条件查询
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userID);
        boolean remove = shoppingCartService.remove(queryWrapper);
        if (remove) {
            return R.success("清空成功");
        }
        throw new CustomException("系统异常，删除失败");
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        System.out.println(shoppingCart.toString());
        //获取当前用户的id
        Long userId = BaseContext.getId();
        //获取当前选择菜品的id
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        Long dishId = shoppingCart.getDishId();
        if (dishId!=null){
            //此时点击的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            //此时点击的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //进行查询
        ShoppingCart shoppOne = shoppingCartService.getOne(queryWrapper);
        log.info(shoppOne.toString());
        //判断数量是否大于1
        if (shoppOne.getNumber()>1){
            shoppOne.setNumber(shoppOne.getNumber()-1);
            shoppingCartService.updateById(shoppOne);
            return R.success(shoppOne);
        }else if (shoppOne.getNumber()==1){
            //从购物车中删除
            shoppingCartService.remove(queryWrapper);
            return R.success(shoppOne);
        }
        throw new CustomException("系统异常");
    }


}
