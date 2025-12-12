package com.itheima.activiti.service.impl;

import com.itheima.activiti.mapper.NotificationLogMapper;
import com.itheima.activiti.entity.NotificationLog;
import com.itheima.activiti.service.NotificationLogService;
import com.itheima.activiti.common.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 通知日志服务实现类
 */
@Service
public class NotificationLogServiceImpl implements NotificationLogService {
    
    @Autowired
    private NotificationLogMapper notificationLogMapper;
    
    // 新增基本CRUD方法实现
    @Override
    public List<NotificationLog> list() {
        try {
            return notificationLogMapper.selectAll();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public NotificationLog getById(String id) {
        try {
            return notificationLogMapper.selectById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean save(NotificationLog log) {
        try {
            return notificationLogMapper.insert(log) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 记录通知发送日志
     */
    @Override
    @Transactional
    public void recordNotificationLog(NotificationLog log) {
        // 生成唯一ID
        if (log.getId() == null) {
            log.setId(java.util.UUID.randomUUID().toString());
        }
        // 保存日志信息
        notificationLogMapper.insert(log);
    }
    
    /**
     * 按条件查询日志
     */
    @Override
    public List<NotificationLog> getLogList(Map<String, Object> params) {
        // 获取当前租户ID，并添加到查询条件中
        String tenantId = TenantContext.getTenantId();
        params.put("tenantId", tenantId);
        return notificationLogMapper.selectByParams(params);
    }
    
    /**
     * 根据发送ID查询日志
     */
    @Override
    public NotificationLog findBySendId(String sendId) {
        return notificationLogMapper.findBySendId(sendId);
    }
    
    /**
     * 根据模板ID查询日志
     */
    @Override
    public List<NotificationLog> findByTemplateId(String templateId) {
        return notificationLogMapper.findByTemplateId(templateId);
    }
    
    /**
     * 查询发送失败的日志
     */
    @Override
    public List<NotificationLog> findFailedLogs(Map<String, Object> condition) {
        return notificationLogMapper.findFailedLogs(condition);
    }
    
    /**
     * 统计日志数量
     */
    @Override
    public int countLogs(Map<String, Object> condition) {
        // 获取当前租户ID，并添加到查询条件中
        String tenantId = TenantContext.getTenantId();
        condition.put("tenantId", tenantId);
        return notificationLogMapper.countLogs(condition);
    }
    
    /**
     * 批量记录日志
     */
    @Override
    @Transactional
    public void batchRecordLogs(List<NotificationLog> logs) {
        if (logs != null && !logs.isEmpty()) {
            // 批量保存日志
            notificationLogMapper.batchInsert(logs);
        }
    }
}
