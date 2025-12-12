package com.itheima.activiti.common;

/**
 * 租户上下文工具类，用于存储和获取当前租户信息
 */
public class TenantContext {
    
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> APP_ID = new ThreadLocal<>();
    
    /**
     * 设置当前租户ID
     */
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }
    
    /**
     * 获取当前租户ID
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }
    
    /**
     * 设置当前APP ID
     */
    public static void setAppId(String appId) {
        APP_ID.set(appId);
    }
    
    /**
     * 获取当前APP ID
     */
    public static String getAppId() {
        return APP_ID.get();
    }
    
    /**
     * 清除上下文信息
     */
    public static void clear() {
        TENANT_ID.remove();
        APP_ID.remove();
    }
}