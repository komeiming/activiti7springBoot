package com.itheima.activiti.service.impl;

import com.itheima.activiti.entity.NotificationTemplate;
import com.itheima.activiti.entity.TemplateOperationLog;
import com.itheima.activiti.mapper.NotificationTemplateMapper;
import com.itheima.activiti.service.NotificationTemplateService;
import com.itheima.activiti.service.TemplateOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通知模板服务实现类
 */
@Service
public class NotificationTemplateServiceImpl implements NotificationTemplateService {
    
    @Autowired
    private NotificationTemplateMapper notificationTemplateMapper;
    
    @Autowired
    private TemplateOperationLogService templateOperationLogService;
    
    @Override
    @Transactional
    public NotificationTemplate createTemplate(NotificationTemplate template) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication != null ? authentication.getName() : "system";
            
            // 生成模板ID
            if (template.getId() == null || template.getId().isEmpty()) {
                template.setId(UUID.randomUUID().toString());
            }
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            template.setCreatedTime(now);
            template.setUpdatedTime(now);
            // 设置创建人和更新人
            template.setCreatedBy(currentUser);
            template.setUpdatedBy(currentUser);
            // 如果没有设置状态，默认设为启用
            if (template.getStatus() == null || template.getStatus().isEmpty()) {
                template.setStatus("enabled");
            }
            // 保存模板
            notificationTemplateMapper.insert(template);
            
            // 记录操作日志
            TemplateOperationLog operationLog = new TemplateOperationLog();
            operationLog.setId(String.valueOf(System.currentTimeMillis()));
            operationLog.setTemplateId(template.getId());
            operationLog.setTemplateName(template.getName());
            operationLog.setOperationType("create");
            operationLog.setOperator(currentUser);
            operationLog.setOperationTime(LocalDateTime.now());
            operationLog.setOperationDetails("创建模板: " + template.getName());
            templateOperationLogService.recordOperationLog(operationLog);
            
            return template;
        } catch (Exception e) {
            System.err.println("创建模板失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    @Transactional
    public NotificationTemplate updateTemplate(NotificationTemplate template) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication != null ? authentication.getName() : "system";
            
            // 先查询原模板
            NotificationTemplate oldTemplate = notificationTemplateMapper.selectById(template.getId());
            if (oldTemplate == null) {
                return null;
            }
            
            // 设置更新时间和更新人
            template.setUpdatedTime(LocalDateTime.now());
            template.setUpdatedBy(currentUser);
            
            // 确保status不为null，如果为null则使用原模板的status
            if (template.getStatus() == null || template.getStatus().isEmpty()) {
                template.setStatus(oldTemplate.getStatus());
            }
            
            // 更新模板
            notificationTemplateMapper.update(template);
            
            // 记录操作日志
            TemplateOperationLog operationLog = new TemplateOperationLog();
            operationLog.setId(String.valueOf(System.currentTimeMillis()));
            operationLog.setTemplateId(template.getId());
            operationLog.setTemplateName(template.getName());
            operationLog.setOperationType("update");
            operationLog.setOperator(currentUser);
            operationLog.setOperationTime(LocalDateTime.now());
            operationLog.setOperationDetails("更新模板: " + template.getName());
            templateOperationLogService.recordOperationLog(operationLog);
            
            return template;
        } catch (Exception e) {
            System.err.println("更新模板失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteTemplate(String id) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication != null ? authentication.getName() : "system";
            
            // 先查询模板是否存在
            NotificationTemplate template = notificationTemplateMapper.selectById(id);
            if (template == null) {
                return false;
            }
            
            NotificationTemplate oldTemplate = new NotificationTemplate();
            oldTemplate.setId(template.getId());
            oldTemplate.setName(template.getName());
            oldTemplate.setStatus(template.getStatus());
            oldTemplate.setType(template.getType());
            
            // 执行逻辑删除
            template.setStatus("disabled");
            template.setUpdatedTime(LocalDateTime.now());
            template.setUpdatedBy(currentUser);
            int count = notificationTemplateMapper.update(template);
            boolean result = count > 0;
            
            // 记录操作日志
            if (result) {
                TemplateOperationLog operationLog = new TemplateOperationLog();
                operationLog.setId(String.valueOf(System.currentTimeMillis()));
                operationLog.setTemplateId(id);
                operationLog.setOperationType("delete");
                operationLog.setOperator(currentUser);
                operationLog.setOperationTime(LocalDateTime.now());
                operationLog.setOperationDetails("删除模板: " + template.getName());
                templateOperationLogService.recordOperationLog(operationLog);
            }
            return result;
        } catch (Exception e) {
            System.err.println("删除模板失败: " + e.getMessage());
            return false;
        }
    }
    

    
    @Override
    public List<NotificationTemplate> getTemplatesByCondition(Map<String, Object> condition) {
        try {
            List<NotificationTemplate> templates = notificationTemplateMapper.selectByParams(condition);
            // 转换状态为大写
            templates.forEach(template -> {
                if (template.getStatus() != null) {
                    template.setStatus(template.getStatus().toUpperCase());
                }
            });
            return templates;
        } catch (Exception e) {
            System.err.println("条件查询模板失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<NotificationTemplate> getAllTemplates() {
        try {
            List<NotificationTemplate> templates = notificationTemplateMapper.selectAll();
            // 转换状态为大写
            templates.forEach(template -> {
                if (template.getStatus() != null) {
                    template.setStatus(template.getStatus().toUpperCase());
                }
            });
            return templates;
        } catch (Exception e) {
            System.err.println("获取所有模板失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<NotificationTemplate> getEnabledTemplates() {
        try {
            List<NotificationTemplate> templates = notificationTemplateMapper.selectByStatus("enabled");
            // 转换状态为大写
            templates.forEach(template -> {
                if (template.getStatus() != null) {
                    template.setStatus(template.getStatus().toUpperCase());
                }
            });
            return templates;
        } catch (Exception e) {
            System.err.println("获取启用的模板失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<NotificationTemplate> getTemplatesByType(String type) {
        try {
            List<NotificationTemplate> templates = notificationTemplateMapper.selectByType(type);
            // 转换状态为大写
            templates.forEach(template -> {
                if (template.getStatus() != null) {
                    template.setStatus(template.getStatus().toUpperCase());
                }
            });
            return templates;
        } catch (Exception e) {
            System.err.println("根据类型获取模板失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public NotificationTemplate getTemplateById(String id) {
        try {
            NotificationTemplate template = notificationTemplateMapper.selectById(id);
            // 转换状态为大写
            if (template != null && template.getStatus() != null) {
                template.setStatus(template.getStatus().toUpperCase());
            }
            return template;
        } catch (Exception e) {
            System.err.println("获取模板失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    @Transactional
    public boolean updateTemplateStatus(String id, String status) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication != null ? authentication.getName() : "system";
            
            NotificationTemplate template = notificationTemplateMapper.selectById(id);
            if (template != null) {
                NotificationTemplate oldTemplate = new NotificationTemplate();
                oldTemplate.setId(template.getId());
                oldTemplate.setName(template.getName());
                oldTemplate.setStatus(template.getStatus());
                
                // 更新模板状态和更新人
                template.setStatus(status);
                template.setUpdatedTime(LocalDateTime.now());
                template.setUpdatedBy(currentUser);
                
                int count = notificationTemplateMapper.update(template);
                boolean result = count > 0;
                
                if (result) {
                    // 记录启用/禁用操作日志
                    String operationType = "enabled".equals(status) ? "enable" : "disable";
                    recordOperationLog(oldTemplate, operationType);
                }
                return result;
            }
            return false;
        } catch (Exception e) {
            System.err.println("更新模板状态失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 记录模板操作日志（单模板版本）
     */
    private void recordOperationLog(NotificationTemplate template, String operationType) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication != null ? authentication.getName() : "system";
            
            TemplateOperationLog log = new TemplateOperationLog();
            log.setTemplateId(template.getId());
            log.setTemplateName(template.getName());
            log.setOperationType(operationType);
            log.setOperator(currentUser);
            log.setOperationTime(LocalDateTime.now());
            
            // 记录操作详情
            log.setOperationDetails("操作模板: " + template.getName());
            
            templateOperationLogService.recordOperationLog(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            System.err.println("记录模板操作日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录模板更新操作日志（包含旧版本）
     */
    private void recordOperationLog(NotificationTemplate oldTemplate, NotificationTemplate newTemplate, String operationType) {
        try {
            TemplateOperationLog log = new TemplateOperationLog();
            log.setTemplateId(newTemplate.getId());
            log.setTemplateName(newTemplate.getName());
            log.setOperationType(operationType);
            log.setOperator(newTemplate.getUpdatedBy());
            log.setOperationTime(LocalDateTime.now());
            
            // 记录操作详情，包含变更前后的信息
            log.setOperationDetails("更新模板: " + oldTemplate.getName() + " -> " + newTemplate.getName());
            
            templateOperationLogService.recordOperationLog(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            System.err.println("记录模板操作日志失败: " + e.getMessage());
        }
    }
}
