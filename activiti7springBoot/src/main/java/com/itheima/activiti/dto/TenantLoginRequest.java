package com.itheima.activiti.dto;

/**
 * 租户登录请求DTO
 */
public class TenantLoginRequest {
    
    private String appId;
    private String secretKey;
    
    public String getAppId() {
        return appId;
    }
    
    public void setAppId(String appId) {
        this.appId = appId;
    }
    
    public String getSecretKey() {
        return secretKey;
    }
    
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
