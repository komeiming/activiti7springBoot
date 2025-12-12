package com.itheima.activiti.service;

import com.itheima.activiti.entity.NotificationRequest;
import com.itheima.activiti.entity.NotificationResponse;

/**
 * 通知发送服务接口
 */
public interface NotificationService {
    
    // 发送通知
    NotificationResponse sendNotification(NotificationRequest request);
    
    // 发送邮件通知（内部使用）
    NotificationResponse sendEmail(NotificationRequest request);
    
    // 发送短信通知（内部使用）
    NotificationResponse sendSms(NotificationRequest request);
    
    // 替换模板参数
    String replaceTemplateParams(String templateContent, java.util.Map<String, Object> params);
    
    // 批量发送通知
    NotificationResponse sendBatchNotification(NotificationRequest request);
    
    // 定时发送通知
    NotificationResponse sendScheduledNotification(NotificationRequest request);
    
    // 重试失败通知
    NotificationResponse retryFailedNotification(String logId);
}
