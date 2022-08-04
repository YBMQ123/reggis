package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 套餐的新增功能
     * @param setmealDto
     * @return
     */
    //当进行新增一个套餐的时候，删除所有的缓存
//    value = "setmealCache"：删除缓存名称为这一类的缓存
//    allEntries = true：删除所有的缓存
    @CacheEvict(value = "setmealCache",allEntries = true)
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("添加套餐成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
//        Page<Setmeal> setmealPage = new Page<>(page,pageSize);
//        Page<SetmealDto> dtoPage = new Page<>(page,pageSize);
//        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
//        // 根据name 进行 like模糊查询
//        queryWrapper.like(name != null,Setmeal::getName,name);
//        setmealService.page(setmealPage,queryWrapper);
//        BeanUtils.copyProperties(setmealPage,dtoPage,"records");
//        List<Setmeal> records = setmealPage.getRecords();
//        List<SetmealDto> dtoList = records.stream().map((record) -> {
//            SetmealDto setmealDto = new SetmealDto();
//            BeanUtils.copyProperties(record, setmealDto);
//            // 根据分类id查询 分类对象
//            Category category = categoryService.getById(record.getCategoryId());
//            if (category != null) {
//                setmealDto.setCategoryName(category.getName());
//            }
//            return setmealDto;
//        }).collect(Collectors.toList());
//        dtoPage.setRecords(dtoList);
//        return R.success(dtoPage);
        log.info("page-{},pageSize-{},name-{}",page,pageSize,name);
        //构造分页构造器
        Page<Setmeal> pageInfo=new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage=new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        //添加过滤条件，按照名字进行查询（模糊匹配）
        //StringUtils.isNotEmpty(name)当这个条件为true的时候,就会执行模糊匹配
        queryWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);
        //添加排序条件，按照更新时间来排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        setmealService.page(pageInfo, queryWrapper);
        //对象拷贝,但是要忽略records,因为protected List<T> records里面的泛型T不相同
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        System.out.println("...."+pageInfo.getRecords());
        //循环遍历，通过套餐分类的id查询到套餐分类的名称保存到dtoPage中
        List<SetmealDto> setmealDtoList=pageInfo.getRecords().stream().map((item)->{
            SetmealDto setmealDto=new SetmealDto();
            //把除了套餐分类之外的数据拷贝到setmealDto中
            BeanUtils.copyProperties(item,setmealDto);
            //获取分类的id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category=categoryService.getById(categoryId);
            String categoryName=category.getName();
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(setmealDtoList);
        return R.success(dtoPage);
    }

    //根据返回的id进行数据回显
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        SetmealDto byWithDish = setmealService.getByWithDish(id);
        return R.success(byWithDish);
    }

    /**
     * 修改套餐的信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    //当进行修改某一个套餐的时候，删除所有的缓存
//    value = "setmealCache"：删除缓存名称为这一类的缓存
//    allEntries = true：删除所有的缓存
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateById(setmealDto);
        return R.success("修改成功");
    }

    /**
     * 批量或者单独设置售卖状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateSaleStatus(@PathVariable("status") Integer status,@RequestParam List<Long> ids){
        //  菜品具体的售卖状态 由前端修改并返回，该方法传入的status是 修改之后的售卖状态，可以直接根据一个或多个菜品id进行查询并修改售卖即可
        log.info("ids :"+ids);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null,Setmeal::getId,ids);
        List<Setmeal> list = setmealService.list(queryWrapper);
        if (list != null){
            for (Setmeal setmeal : list) {
                setmeal.setStatus(status);
                setmealService.updateById(setmeal);
            }
            return R.success("套餐的售卖状态已更改！");
        }
        return R.error("售卖状态不可更改,请联系管理员或客服！");
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    //当进行删除某一个套餐的时候，删除所有的缓存
//    value = "setmealCache"：删除缓存名称为这一类的缓存
//    allEntries = true：删除所有的缓存
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> deleteById(@PathVariable String ids){
        if (ids.contains(",")) {
            String[] split = ids.split(",");
            for (String s : split) {
                Long id= Long.valueOf(s);
                boolean flag = setmealService.deleteWithDish(id);
                if (flag)
                    return R.success("删除成功！");
                else
                    throw new CustomException("套餐正在售卖中，不能删除");
            }
        }
        Long singleId= Long.valueOf(ids);
        boolean flag = setmealService.deleteWithDish(singleId);
        if (flag)
            return R.success("删除成功！");
        else
            throw new CustomException("套餐正在售卖中，不能删除");
    }

    @GetMapping("/list")
    //将返回的数据缓存到redis中
//    value = "setmealCache"设置缓存的名称
//    key = "#setmeal.categoryId+'_'+#setmeal.status":设置缓存的键
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //获取当前套餐的id
        Long categoryId = setmeal.getCategoryId();
        Integer status = setmeal.getStatus();
        //设置查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,categoryId);
        queryWrapper.eq(Setmeal::getStatus,status);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        System.out.println(setmealList.toString());
        return R.success(setmealList);
    }
//@GetMapping("/list")  // 在消费者端 展示套餐信息
////@Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_' +#setmeal.status")
//public R<List<Setmeal>> list(Setmeal setmeal){
//    LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
//    Long categoryId = setmeal.getCategoryId();
//    Integer status = setmeal.getStatus();
//    queryWrapper.eq(categoryId != null,Setmeal::getCategoryId,categoryId);
//    queryWrapper.eq(status != null,Setmeal::getStatus,status);
//    queryWrapper.orderByDesc(Setmeal::getUpdateTime);
//    List<Setmeal> setmeals = setmealService.list(queryWrapper);
//    return R.success(setmeals);
//}
}
