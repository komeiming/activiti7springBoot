package com.itheima.activiti.entity;

import java.time.LocalDateTime;

/**
 * API调用日志实体类
 */
public class ApiCallLog {
    
    private String id; // 日志ID
    private String appId; // APP ID
    private String tenantId; // 租户ID
    private String apiPath; // API路径
    private String requestMethod; // 请求方法
    private String requestParams; // 请求参数
    private String responseData; // 响应数据
    private long responseTime; // 响应时间（毫秒）
    private boolean success; // 是否成功
    private String errorCode; // 错误代码
    private String errorMessage; // 错误信息
    private String clientIp; // 客户端IP
    private LocalDateTime callTime; // 调用时间
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getAppId() {
        return appId;
    }
    
    public void setAppId(String appId) {
        this.appId = appId;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getApiPath() {
        return apiPath;
    }
    
    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }
    
    public String getRequestMethod() {
        return requestMethod;
    }
    
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }
    
    public String getRequestParams() {
        return requestParams;
    }
    
    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }
    
    public String getResponseData() {
        return responseData;
    }
    
    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }
    
    public long getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    public LocalDateTime getCallTime() {
        return callTime;
    }
    
    public void setCallTime(LocalDateTime callTime) {
        this.callTime = callTime;
    }
}