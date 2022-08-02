package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理的控制器
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功!");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        //构造分页构造器
        Page<Category> pageInfo=new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        //添加排序条件，按照sort来排序(升序)
        queryWrapper.orderByAsc(Category::getSort );
        //执行查询
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id进行删除分类
     * @param id
     * @return
     */
    @DeleteMapping()
    public R<String> deleteById(Long id){
        categoryService.remove(id);
//        categoryService.removeById(id);
        return R.success("删除成功！");
    }

    /**
     * 修改分类
     * @param category
     * @return
     */
    @PutMapping
    public R<String> updateById(@RequestBody Category category){
        log.info("需要更新的内容{}",category);
        //调用Service进行删除
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    /**
     * 根据条件查询分类信息
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        //设置查询条件
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //设置排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime);
        //进行查询并返回一个集合
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);

    }


}
