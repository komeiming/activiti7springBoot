package com.itheima.activiti.service;

import com.itheima.activiti.entity.NotificationTemplate;
import java.util.List;
import java.util.Map;

/**
 * 通知模板服务接口
 */
public interface NotificationTemplateService {
    
    // 创建模板
    NotificationTemplate createTemplate(NotificationTemplate template);
    
    // 更新模板
    NotificationTemplate updateTemplate(NotificationTemplate template);
    
    // 删除模板（逻辑删除）
    boolean deleteTemplate(String id);
    
    // 根据ID查询模板
    NotificationTemplate getTemplateById(String id);
    
    // 根据条件查询模板列表
    List<NotificationTemplate> getTemplatesByCondition(Map<String, Object> condition);
    
    // 获取所有模板
    List<NotificationTemplate> getAllTemplates();
    
    // 获取启用的模板
    List<NotificationTemplate> getEnabledTemplates();
    
    // 根据类型获取模板
    List<NotificationTemplate> getTemplatesByType(String type);
    
    // 更新模板状态
    boolean updateTemplateStatus(String id, String status);
}
