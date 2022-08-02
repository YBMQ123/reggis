package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 进行分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name){
        //构造分页构造器
        Page<Dish> pageInfo=new Page<>(page,pageSize);
        Page<DishDto> pageInfo1=new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name!=null,Dish::getName,name);
        //添加排序条件，按照更新时间来排序(升序)
        queryWrapper.orderByDesc(Dish::getUpdateTime );
        //执行查询
        dishService.page(pageInfo,queryWrapper);
        //对象的拷贝
        BeanUtils.copyProperties(pageInfo,pageInfo1,"records");
        List<Dish> records=pageInfo.getRecords();

        List<DishDto> list=records.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            //获取分类的id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象

            Category category=categoryService.getById(categoryId);
            String categoryName=category.getName();
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());
        pageInfo1.setRecords(list);
        return R.success(pageInfo1);
    }

    /**
     * 添加菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
//        dishService.save(dishDto);
        dishService.saveWithFlavor(dishDto);
        return R.success("添加菜品成功");
    }

    /**
     * 查询你要修改的菜品信息，作为数据回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){
        DishDto withFlavor = dishService.getWithFlavor(id);
        return R.success(withFlavor);
    }

    /**
     * 修改菜品信息并保存
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> updateById(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
    }

    /**
     * 删除菜品的信息
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    public R<String> deleteById(@PathVariable String ids){
        if (ids.contains(",")) {
            String[] split = ids.split(",");
            for (String s : split) {
                Long id= Long.valueOf(s);
                boolean flag = dishService.deleteWithFlavor(id);
                if (flag)
                    return R.success("删除成功！");
                else
                    throw new CustomException("套餐正在售卖中，不能删除");
            }
        }
        Long singleId= Long.valueOf(ids);
        boolean flag = dishService.deleteWithFlavor(singleId);
        if (flag)
            return R.success("删除成功！");
        else
            throw new CustomException("套餐正在售卖中，不能删除");
    }

    /**
     * 单个或多个菜品停售
     * @param
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateSaleStatus(@PathVariable("status") Integer status,@RequestParam List<Long> ids){
        //  菜品具体的售卖状态 由前端修改并返回，该方法传入的status是 修改之后的售卖状态，可以直接根据一个或多个菜品id进行查询并修改售卖即可
        log.info("ids :"+ids);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null,Dish::getId,ids);
        List<Dish> list = dishService.list(queryWrapper);
        if (list != null){
            for (Dish dish : list) {
                dish.setStatus(status);
                dishService.updateById(dish);
            }
            return R.success("菜品的售卖状态已更改！");
        }
        return R.error("售卖状态不可更改,请联系管理员或客服！");

    }

    /**
     * 查询套餐新增菜品中对应菜品分类的所有菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //设置条件查询
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        List<DishDto> dishDtoList=list.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            //获取分类的id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category=categoryService.getById(categoryId);
            String categoryName=category.getName();
            dishDto.setCategoryName(categoryName);
            //获取菜品的id
            Long dishId = item.getId();
            //条件查询出改菜品对应的口味
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);
            //把dishFlavors赋值到dishDto
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtoList);
    }
}
