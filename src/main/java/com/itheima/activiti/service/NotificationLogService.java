package com.itheima.activiti.service;

import com.itheima.activiti.entity.NotificationLog;
import java.util.List;
import java.util.Map;

/**
 * 通知日志服务接口
 */
public interface NotificationLogService {
    
    // 基本CRUD方法
    List<NotificationLog> list();
    NotificationLog getById(String id);
    boolean save(NotificationLog log);
    
    // 记录通知发送日志
    void recordNotificationLog(NotificationLog log);
    
    // 按条件查询日志
    List<NotificationLog> getLogList(Map<String, Object> params);
    
    // 根据发送ID查询日志
    NotificationLog findBySendId(String sendId);
    
    // 根据模板ID查询日志
    List<NotificationLog> findByTemplateId(String templateId);
    
    // 查询发送失败的日志
    List<NotificationLog> findFailedLogs(Map<String, Object> condition);
    
    // 统计日志数量
    int countLogs(Map<String, Object> condition);
    
    // 批量记录日志
    void batchRecordLogs(List<NotificationLog> logs);
}
