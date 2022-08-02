package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl  extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味
     *
     * @param dishDto
     */
    @Override
    //事务控制的注解，保真同时操作两张表的结果相同，同时成功或者同时失败
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto);
        Long dishId = dishDto.getId();//菜品的id

        //菜品的口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味数据到菜品口味表
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 获取菜品信息和菜品口味的信息，用于数据回显
     *
     * @param id
     * @return
     */
    //事务控制的注解，保真同时操作两张表的结果相同，同时成功或者同时失败
    @Transactional
    @Override
    public DishDto getWithFlavor(Long id) {
        //封装所有的数据并返回
        DishDto dishDto = new DishDto();
        //查询到菜品的基本信息
        Dish dish = this.getById(id);
        //通过菜品的id在口味表中查询对应的口味
        //设置查询条件
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        //查询到对应菜品的口味集合
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        //把菜品的基本信息拷贝到dishDto中,BeanUtils.copyProperties(a,b) a的值拷贝到b中
        BeanUtils.copyProperties(dish, dishDto);
        //把菜品的口味集合赋值到dishDto中
        dishDto.setFlavors(list);
        return dishDto;
    }

    /**
     * 修改菜品信息并保存
     *
     * @param dishDto
     */
    //事务控制的注解，保真同时操作两张表的结果相同，同时成功或者同时失败
    @Transactional
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //先更新菜品表的基本信息
        this.updateById(dishDto);
        //更新口味的信息
        //先删除掉当前所有的口味信息，设置查询条件
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        //进行删除操作
        dishFlavorService.remove(queryWrapper);
        //在重新添加新的口味信息,获取页面返回的口味信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        //对每一个口味信息对应的菜品id进行赋值
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
        }
        //重新添加口味信息到
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 删除菜品的信息，同时删除对应的口味的数据
     *
     * @param
     */
    //事务控制的注解，保真同时操作两张表的结果相同，同时成功或者同时失败
    @Transactional
    @Override
    public boolean deleteWithFlavor(Long id) {
        //通过id进行判断，要删除的菜品是否出于出售状态，出售状态则不能删除
        Dish dish = this.getById(id);
        Integer status = dish.getStatus();
        if (status == 0) {
            //删除基本的菜品信息
            this.removeById(id);
            //通过菜品的id删除口味表中对应的信息
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId, id);
            dishFlavorService.remove(queryWrapper);
            return true;
        }
        return false;
    }
}



