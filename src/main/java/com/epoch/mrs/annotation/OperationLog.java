package com.epoch.mrs.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)    // 注解作用于方法级别
@Retention(RetentionPolicy.RUNTIME)  // 注解在运行时有效
public @interface OperationLog {
    /**
     * 操作的简短描述，例如“用户注册”、“发表评论”
     */
    String value();

}