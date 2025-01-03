package com.epoch.mrs.aspect;

import com.epoch.mrs.annotation.OperationLog;

import com.epoch.mrs.service.IUserService;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import cn.dev33.satoken.stp.StpUtil;

import java.lang.reflect.Method;

@Aspect
@Component
public class OperationLogAspect {


    @Autowired
    private IUserService userService;

    @Pointcut("@annotation(com.epoch.mrs.annotation.OperationLog)")
    public void operationLogPointCut() {}

    @Around("operationLogPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        String action = operationLog.value();

        Long userId = StpUtil.getLoginIdAsLong();
        String username = "SYSTEM";
        if (userId != -1L) {
            username = userService.getById(userId).getUsername();
        }


        ThreadContext.put("userId", userId != null ? String.valueOf(userId) : "NULL");
        ThreadContext.put("username", username != null ? username : "NULL");
        ThreadContext.put("action", action);


        try {
            return joinPoint.proceed();
        } finally {
            ThreadContext.remove("userId");
            ThreadContext.remove("username");
            ThreadContext.remove("action");
        }
    }
}