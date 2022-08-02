package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
//    添加套餐表和套餐菜品关系表
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //对套餐表进行添加
        this.save(setmealDto);
        //对套餐菜品关系表添加
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //遍历setmealDishes对菜品类中的分类id进行赋值
        setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    //根据返回的id进行数据回显
    @Override
    @Transactional
    public SetmealDto getByWithDish(Long id) {
        //创建一个SetmealDto对象封装数据并返回
        SetmealDto setmealDto=new SetmealDto();
        //查询套餐的基本信息
        Setmeal setmeal = this.getById(id);
        //设置查询条件
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        BeanUtils.copyProperties(setmeal,setmealDto);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    //删除套餐，单独删除或者批量删除
    @Override
    @Transactional
    public boolean deleteWithDish(Long id) {
        //通过id进行判断，要删除的菜品是否出于出售状态，出售状态则不能删除
        Setmeal setmeal = this.getById(id);
        Integer status = setmeal.getStatus();
        if (status == 0) {
            //删除基本的菜品信息
            this.removeById(id);
            //通过菜品的id删除口味表中对应的信息
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getSetmealId, id);
            setmealDishService.remove(queryWrapper);
            return true;
        }
        return false;
    }
}
