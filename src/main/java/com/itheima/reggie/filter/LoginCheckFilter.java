package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.jar.JarEntry;

/**
 * 检查用户是否完成登入
 * 过滤器
 */
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符匹配
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request= (HttpServletRequest) servletRequest;
        HttpServletResponse response= (HttpServletResponse) servletResponse;
        //1.获取本次请求的URI
        String requestURI=request.getRequestURI();
        log.info("拦截到请求{}",requestURI);
        //定义不需要拦截的请求路径
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        //2.判断本次请求是否需要进行处理
        Boolean check = check(urls, requestURI);
        //3.如果不需要处理，则直接放行
        if (check){
            log.info("本次请求{}不需要处理",requestURI);
            //放行代码
            filterChain.doFilter(request,response);
            return;
        }
        //4-1.判断登入状态，如果已经登入，则直接放行
        if(request.getSession().getAttribute("employee")!=null){
            log.info("用户已经登入，用户id为：{}",request.getSession().getAttribute("employee"));
            //获取当前登入的id值保存到当前线程中通过ThreadLocal
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setId(empId);
            filterChain.doFilter(request,response);
            return;
        }

        //4-2.判断移动端登入状态，如果已经登入，则直接放行
        if(request.getSession().getAttribute("user")!=null){
            log.info("用户已经登入，用户id为：{}",request.getSession().getAttribute("user"));
            //获取当前登入的id值保存到当前线程中通过ThreadLocal
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setId(userId);
            filterChain.doFilter(request,response);
            return;
        }
        log.info("用户未登入");
        //5.如果未登入则返回未登入的结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 路径匹配，检查本次请求是否需要进行放行
     * @param uris
     * @param requestURI
     * @return
     */
    public Boolean check(String[] uris,String requestURI){
        for (String uri : uris) {
            boolean match = PATH_MATCHER.match(uri, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}

