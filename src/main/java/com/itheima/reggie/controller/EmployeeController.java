package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.Service;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;


    /**
     * @RequestBody Employee employee:保存从页面传过来的用户名和密码
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的密码password进行MD5加密处理
        String password= employee.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据页面提交的用户名到数据库进行查询
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //3.如果没有查询到则返回登入失败结果
        if (emp==null){
            return R.error("用户名不存在,登入失败");
        }
        //4.密码比对，如果不一致则返回登入失败结果
        if (!emp.getPassword().equals(password)){
            return R.error("密码错误,登入失败");
        }
        //5.查看员工状态，如果为禁用状态，则返回登入失败
        if (emp.getStatus()==0){
            return R.error("账号被停用,登入失败");
        }
        //6.登入成功，将员工id传到Session并返回登入结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出功能
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登入员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("添加的数据为{}",employee.toString());
        //设置初始密码123456，使用MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //设置创建的时间
//        employee.setCreateTime(LocalDateTime.now());
//        //设置更新的时间
//        employee.setUpdateTime(LocalDateTime.now());
        //获得当前登入用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");
        //设置创建人的id和修改人的id
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 员工分页查询功能
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page-{},pageSize-{},name-{}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo=new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        //添加过滤条件，按照名字进行查询（模糊匹配）
        //StringUtils.isNotEmpty(name)当这个条件为true的时候,就会执行模糊匹配
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件，按照更新时间来排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工的信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        //获取当前登入的用户id
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        BaseContext.setId(empId);
        //更新下当前的更新时间
//        employee.setUpdateTime(LocalDateTime.now());
        //更新下当前的更新用户
//        employee.setUpdateUser(empId);
        //修改数据
        employeeService.updateById(employee);
        return R.success("信息修改成功！");
    }

    /**
     * 修改员工信息，并数据回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("启动修改员工信息");
        Employee employee = employeeService.getById(id);
        if (employee!=null) {
            return R.success(employee);
        }
        return R.error("员工不存在");
    }
}
