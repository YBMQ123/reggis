package com.itheima.reggie.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {
    //处理套餐表和套餐菜品关系表
    public void saveWithDish(SetmealDto setmealDto);
    //根据返回的id进行数据回显
    public SetmealDto getByWithDish(Long id);

    //删除套餐，单独删除或者批量删除
    boolean deleteWithDish(Long id);
}
