package com.itheima.reggie.common;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 */
//去处理其他Controller的异常
@ControllerAdvice(annotations = {RestController.class, Controller.class})
//使返回一个JSON对象传到页面端
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    //处理SQLIntegrityConstraintViolationException类型的异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        //判断出现的异常是否是Duplicate entry异常
        if (ex.getMessage().contains("Duplicate entry")){
            //按照空格分割成一个数组
            String[] s = ex.getMessage().split(" ");
            String msg=s[2]+"已经被占用了！,请使用其他用户名。";
            return R.error(msg);
        }
        return R.error("未知的异常！");
    }

    //处理CustomException(自定义)类型的异常
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}
