package com.epoch.mrs.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.epoch.mrs.annotation.OperationLog;
import com.epoch.mrs.domain.po.Log;
import com.epoch.mrs.service.ILogService;
import com.epoch.mrs.service.IUserService;
import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @Autowired
    private IUserService userService;

    @Autowired
    private ILogService logService;

    /**
     * 定义切点：匹配 @OperationLog 注解的方法
     */
    @Pointcut("@annotation(com.epoch.mrs.annotation.OperationLog)")
    public void operationLogPointCut() {}

    @Around("operationLogPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //1. 获取方法、注解、动作描述等信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        String action = operationLog.value();

        // 2. 先执行目标方法
        Object result;
        Throwable throwable = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable t) {
            // 抛出异常后暂存
            throwable = t;
            result = null;
        }

        // 3. 获取当前登录用户信息
        Long userId = -1L;
        String username = "GUEST";
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
                username = userService.getById(userId).getUsername();
            }
        } catch (Exception e) {
            // 如果获取用户信息失败，也不影响日志落库
            log.error("Get user info failed: {}", e.getMessage());
        }

        //4. 构造日志实体写入数据库
        Log dbLog = new Log()
                .setUserId(userId)
                .setUsername(username)
                .setAction(action)
                .setLogLevel(String.valueOf(throwable == null ? LogLevel.INFO : LogLevel.ERROR))
                .setLogMessage(buildLogMessage(joinPoint, throwable))
                .setTimestamp(LocalDateTime.now());

        logService.save(dbLog);

        // 5. 如果有异常，继续向外抛，以便上层处理
        if (throwable != null) {
            throw throwable;
        }

        return result;
    }

    /**
     * 构造要写入数据库的日志内容
     */
    private String buildLogMessage(ProceedingJoinPoint joinPoint, Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        // 方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        sb.append("Method: ").append(method.getDeclaringClass().getName())
                .append(".").append(method.getName()).append("\n");

        // 拼接参数
        Object[] args = joinPoint.getArgs();
        if (args != null) {
            sb.append("Args: [");
            for (int i = 0; i < args.length; i++) {
                sb.append(args[i]);
                if (i < args.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]\n");
        }

        // 异常信息(如有)
        if (throwable != null) {
            sb.append("Exception: ").append(throwable.getMessage());
        } else {
            sb.append("Status: SUCCESS");
        }
        return sb.toString();
    }
}