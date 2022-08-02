package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    /**
     * 判断是否关联了其他套餐并删除
     * @param id
     */
    public void remove(Long id);
}
