package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);
    //获取菜品信息和菜品口味的信息，用于数据回显
    DishDto getWithFlavor(Long id);
    //修改菜品信息并保存
    void updateWithFlavor(DishDto dishDto);
    //删除菜品的信息
    boolean deleteWithFlavor(Long id);
}
