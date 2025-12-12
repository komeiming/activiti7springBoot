package com.itheima.activiti.service;

import com.itheima.activiti.entity.PendingNotification;
import java.util.List;
import java.util.Map;

/**
 * 待发送通知服务接口
 */
public interface PendingNotificationService {
    
    // 保存待发送通知
    PendingNotification savePendingNotification(PendingNotification pendingNotification);
    
    // 更新待发送通知
    PendingNotification updatePendingNotification(PendingNotification pendingNotification);
    
    // 根据ID查询待发送通知
    PendingNotification getPendingNotificationById(String id);
    
    // 根据条件查询待发送通知列表
    List<PendingNotification> getPendingNotificationsByParams(Map<String, Object> params);
    
    // 查询待发送的通知（状态为pending且下次重试时间<=当前时间）
    List<PendingNotification> getPendingNotifications();
    
    // 根据状态查询待发送通知
    List<PendingNotification> getPendingNotificationsByStatus(String status);
    
    // 根据类型查询待发送通知
    List<PendingNotification> getPendingNotificationsByType(String type);
    
    // 删除待发送通知
    boolean deletePendingNotification(String id);
    
    // 批量删除待发送通知
    boolean deletePendingNotifications(List<String> ids);
    
    // 处理待发送通知
    boolean processPendingNotification(String id);
    
    // 批量处理待发送通知
    int processPendingNotifications();
}