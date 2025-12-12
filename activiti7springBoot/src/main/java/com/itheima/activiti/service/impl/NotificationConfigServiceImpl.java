package com.itheima.activiti.service.impl;

import com.itheima.activiti.entity.NotificationConfig;
import com.itheima.activiti.service.NotificationConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 通知配置服务实现类
 */
@Service
public class NotificationConfigServiceImpl implements NotificationConfigService {
    
    /**
     * 获取租户的通知配置
     * 目前使用内存实现，后续可改为数据库实现
     */
    @Override
    public NotificationConfig getConfigByTenantId(String tenantId) {
        // 模拟从数据库获取配置
        NotificationConfig config = new NotificationConfig();
        config.setId(UUID.randomUUID().toString());
        config.setTenantId(tenantId);
        config.setCallbackUrl("");
        config.setCallbackTimeout(30);
        config.setCallbackRetryCount(3);
        config.setCallbackSecret("callback_secret_" + UUID.randomUUID().toString().substring(0, 16));
        config.setPerSecondLimit(100);
        config.setPerMinuteLimit(5000);
        config.setPerHourLimit(300000);
        config.setPerDayLimit(5000000);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        config.setCreatedBy("system");
        config.setUpdatedBy("system");
        return config;
    }
    
    /**
     * 保存或更新通知配置
     * 目前使用内存实现，后续可改为数据库实现
     */
    @Override
    public NotificationConfig saveOrUpdateConfig(NotificationConfig config) {
        // 模拟保存到数据库
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy("system");
        return config;
    }
    
    /**
     * 重置租户的通知配置到默认值
     * 目前使用内存实现，后续可改为数据库实现
     */
    @Override
    public NotificationConfig resetConfig(String tenantId) {
        // 模拟重置配置
        return getConfigByTenantId(tenantId);
    }
}