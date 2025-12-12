package com.itheima.activiti.service;

import com.itheima.activiti.entity.NotificationRequest;
import com.itheima.activiti.entity.NotificationResponse;

/**
 * 通知发送核心服务接口
 * 用于打破循环依赖，提供纯发送功能
 */
public interface NotificationSenderService {
    
    /**
     * 发送邮件通知
     * @param request 通知请求
     * @return 通知响应
     */
    NotificationResponse sendEmail(NotificationRequest request);
    
    /**
     * 发送短信通知
     * @param request 通知请求
     * @return 通知响应
     */
    NotificationResponse sendSms(NotificationRequest request);
    
    /**
     * 替换模板参数
     * @param templateContent 模板内容
     * @param params 参数映射
     * @return 替换后的内容
     */
    String replaceTemplateParams(String templateContent, java.util.Map<String, Object> params);
}