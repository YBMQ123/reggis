package com.itheima.reggie.common;

import java.time.LocalDateTime;

/**
 * 基于ThreadLocal封装的工具类，用户保存和获取id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();
    private static ThreadLocal<LocalDateTime> threadLocalTime=new ThreadLocal<>();

    //保存当前线程用户的id
    public static void setId(Long id){
        threadLocal.set(id);
    }
    //获取当前前程的用户id
    public static Long getId(){
        return threadLocal.get();
    }

    //保存当前线程用户的下单时间
    public static void setTime(LocalDateTime nowTime){
        threadLocalTime.set(nowTime);
    }
    //获取当前前程的用户的下单时间
    public static LocalDateTime getTime(){
        return threadLocalTime.get();
    }
}
