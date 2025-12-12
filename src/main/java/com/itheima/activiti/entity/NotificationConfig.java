package com.itheima.activiti.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知配置实体类
 */
@Data
public class NotificationConfig {
    /**
     * 主键ID
     */
    private String id;
    
    /**
     * 租户ID
     */
    private String tenantId;
    
    /**
     * 回调地址
     */
    private String callbackUrl;
    
    /**
     * 回调超时时间（秒）
     */
    private Integer callbackTimeout;
    
    /**
     * 回调重试次数
     */
    private Integer callbackRetryCount;
    
    /**
     * 回调密钥
     */
    private String callbackSecret;
    
    /**
     * 每秒最大发送量
     */
    private Integer perSecondLimit;
    
    /**
     * 每分钟最大发送量
     */
    private Integer perMinuteLimit;
    
    /**
     * 每小时最大发送量
     */
    private Integer perHourLimit;
    
    /**
     * 每日最大发送量
     */
    private Integer perDayLimit;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新人
     */
    private String updatedBy;
}