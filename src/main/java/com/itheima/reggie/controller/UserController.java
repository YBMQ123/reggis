package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.qcloudsms.httpclient.HTTPException;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.MessageUtil;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送验证码到用户的手机
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取用户的手机号码
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)){
            //生成四位的验证码
            String MessageCode = String.valueOf(ValidateCodeUtils.generateValidateCode(4));
            //将生成的验证码保存到session里面
            session.setAttribute(phone,MessageCode);
            log.info("收到的验证码为{}",MessageCode);
//            //调用腾讯云发送短信
//            try {
//                MessageUtil.senMessage(phone,MessageCode);
//            } catch (HTTPException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            return R.success("短信验证码发送成功");
        }
        return R.error("短信验证码发送失败");
    }

    /**
     * 用户登入
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        log.info(map.toString());
        //获取前台传来的用户号码
        String phone = (String) map.get("phone");
        //获取前台传来用户输入的验证码
        String code = (String) map.get("code");
        //获取保存在session中正确生成的验证码
        String rightCode = (String) session.getAttribute(phone);
        System.out.println("正确的验证码"+rightCode);
        System.out.println("输入的验证码"+code);
        if (code.equals(rightCode)){
            //输入的验证码正确，判断是否为新用户
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if (user==null){
                //是新用户，添加进去
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        throw new CustomException("输入的验证码错误");
    }
}