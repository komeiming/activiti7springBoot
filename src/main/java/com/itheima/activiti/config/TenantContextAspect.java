package com.itheima.activiti.config;

import com.itheima.activiti.common.TenantContext;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 租户上下文管理切面，用于在请求开始时设置和请求结束时清除租户上下文
 */
@Aspect
@Component
public class TenantContextAspect {

    /**
     * 定义切点：所有Controller方法和RestController方法
     */
    @Pointcut("@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)")
    public void controllerPointcut() {}

    /**
     * 在请求处理前设置租户上下文（如果需要的话）
     */
    @Before("controllerPointcut()")
    public void beforeRequest() {
        // 可以在这里添加请求前的租户上下文设置逻辑
        // 不过目前主要在AppIdSecretKeyAuthenticationFilter中设置
    }

    /**
     * 在请求成功处理后清除租户上下文
     */
    @AfterReturning("controllerPointcut()")
    public void afterRequest() {
        // 清除租户上下文，避免内存泄漏
        TenantContext.clear();
    }

    /**
     * 定义切点：所有API接口方法
     */
    @Pointcut("execution(* com.itheima.activiti.controller..*.*(..))")
    public void apiPointcut() {}

    /**
     * 在API方法成功执行后清除租户上下文
     */
    @AfterReturning("apiPointcut()")
    public void afterApiMethod() {
        // 清除租户上下文，避免内存泄漏
        TenantContext.clear();
    }
}
