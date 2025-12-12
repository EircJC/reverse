package com.yulink.texas.admin.support;

import com.yulink.texas.common.admin.support.BaseLoggerOperation;
import javax.annotation.Resource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggerOperation {

    @Resource
    private BaseLoggerOperation baseLoggerOperation;

    @Pointcut("execution(* com.yulink.texas.admin.api.*.*(..)) && @annotation(com.yulink.texas.common.admin.support.Loggable)")
    public void log() {
    }

    @AfterReturning(value = "log()", returning = "returnValue")
    public void log(JoinPoint point, Object returnValue) {
        baseLoggerOperation.commonRecord(point, returnValue);
    }

}
