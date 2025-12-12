package com.itheima.activiti.service;

import com.itheima.activiti.entity.NotificationConfig;

/**
 * 通知配置服务接口
 */
public interface NotificationConfigService {
    
    /**
     * 获取租户的通知配置
     * @param tenantId 租户ID
     * @return 通知配置
     */
    NotificationConfig getConfigByTenantId(String tenantId);
    
    /**
     * 保存或更新通知配置
     * @param config 通知配置
     * @return 更新后的通知配置
     */
    NotificationConfig saveOrUpdateConfig(NotificationConfig config);
    
    /**
     * 重置租户的通知配置到默认值
     * @param tenantId 租户ID
     * @return 重置后的通知配置
     */
    NotificationConfig resetConfig(String tenantId);
}