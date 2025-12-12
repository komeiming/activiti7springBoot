package com.itheima.activiti.service;

/**
 * 租户初始化服务，用于在租户注册成功后初始化相关资源
 */
public interface TenantInitializationService {
    
    /**
     * 初始化租户资源
     * @param tenantId 租户ID
     */
    void initializeTenant(String tenantId);
    
    /**
     * 初始化通知模板
     * @param tenantId 租户ID
     */
    void initializeNotificationTemplates(String tenantId);
    
    /**
     * 初始化工作流模板
     * @param tenantId 租户ID
     */
    void initializeWorkflowTemplates(String tenantId);
}
