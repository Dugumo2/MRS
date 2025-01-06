package com.epoch.mrs.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import com.epoch.mrs.Inrercepter.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    //拦截器对象
    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 注册Sa-Token的路由拦截器，负责Token校验和自动续期
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/users/register", "/users/login", "/users/email", "/users/IsExists");

       //注册自定义拦截器对象
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/users/register")
                .excludePathPatterns("/users/login")
                .excludePathPatterns("/users/email")
                .excludePathPatterns("/users/IsExists");
    }
}
