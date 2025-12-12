package com.itheima.activiti.service.impl;

import com.itheima.activiti.entity.NotificationTemplate;
import com.itheima.activiti.entity.TemplateOperationLog;
import com.itheima.activiti.mapper.NotificationTemplateMapper;
import com.itheima.activiti.service.NotificationTemplateService;
import com.itheima.activiti.service.TemplateOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.itheima.activiti.common.TenantContext;

/**
 * 通知模板服务实现类
 */
@Service
@Transactional
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
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            
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
            // 设置租户ID，只有当未设置时才从上下文获取
            if (template.getTenantId() == null || template.getTenantId().isEmpty()) {
                String tenantId = TenantContext.getTenantId();
                template.setTenantId(tenantId);
            }
            // 如果没有设置状态，默认设为启用
            if (template.getStatus() == null || template.getStatus().isEmpty()) {
                template.setStatus("ENABLED");
            }
            // 总是生成唯一code，不管前端是否传递
            String timestamp = String.valueOf(System.currentTimeMillis());
            String templateNamePart = template.getName() != null ? template.getName().replaceAll("\\s+", "_").toUpperCase() : "UNKNOWN";
            template.setCode("TEMPLATE_" + templateNamePart + "_" + timestamp);
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
            operationLog.setIpAddress("127.0.0.1");
            templateOperationLogService.recordOperationLog(operationLog);
            
            return template;
        } catch (Exception e) {
            System.err.println("创建模板失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional
    public NotificationTemplate updateTemplate(NotificationTemplate template) {
        try {
            // 获取当前登录用户
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // 先查询原模板
            NotificationTemplate oldTemplate = notificationTemplateMapper.selectById(template.getId());
            if (oldTemplate == null) {
                return null;
            }
            
            // 检查权限：只有模板创建者或管理员才能修改模板
            if (!currentUser.equals(oldTemplate.getCreatedBy()) && !isAdmin(currentUser)) {
                throw new RuntimeException("没有权限修改该模板");
            }
            
            // 设置更新时间和更新人
            template.setUpdatedTime(LocalDateTime.now());
            template.setUpdatedBy(currentUser);
            
            // 确保status不为null，如果为null则使用原模板的status
            if (template.getStatus() == null || template.getStatus().isEmpty()) {
                template.setStatus(oldTemplate.getStatus());
            }
            
            // 确保code不为null，如果为null则使用原模板的code
            if (template.getCode() == null || template.getCode().isEmpty()) {
                template.setCode(oldTemplate.getCode());
            }
            
            // 确保租户ID正确，使用原模板的租户ID或当前租户ID
            template.setTenantId(oldTemplate.getTenantId());
            
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
            operationLog.setIpAddress("127.0.0.1");
            templateOperationLogService.recordOperationLog(operationLog);
            
            return template;
        } catch (Exception e) {
            System.err.println("更新模板失败: " + e.getMessage());
            return null;
        }
    }

    // 检查用户是否为管理员
    private boolean isAdmin(String username) {
        // 假设ROLE_ADMIN是管理员角色
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()) || "admin".equals(username));
    }
    
    @Override
    @Transactional
    public boolean deleteTemplate(String id) {
        try {
            // 获取当前登录用户
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // 先查询模板是否存在
            NotificationTemplate template = notificationTemplateMapper.selectById(id);
            if (template == null) {
                return false;
            }
            
            // 检查权限：只有模板创建者或管理员才能删除模板
            if (!currentUser.equals(template.getCreatedBy()) && !isAdmin(currentUser)) {
                throw new RuntimeException("没有权限删除该模板");
            }
            
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
                operationLog.setTemplateName(template.getName());
                operationLog.setOperationType("delete");
                operationLog.setOperator(currentUser);
                operationLog.setOperationTime(LocalDateTime.now());
                operationLog.setOperationDetails("删除模板: " + template.getName());
                operationLog.setIpAddress("127.0.0.1");
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
            // 获取当前租户ID，并添加到查询条件中
            String tenantId = TenantContext.getTenantId();
            condition.put("tenantId", tenantId);
            
            List<NotificationTemplate> templates = notificationTemplateMapper.selectByParams(condition);
            // 转换状态为大写
            if (templates != null) {
                templates.forEach(template -> {
                    if (template.getStatus() != null) {
                        template.setStatus(template.getStatus().toUpperCase());
                    }
                });
            }
            return templates;
        } catch (Exception e) {
            System.err.println("条件查询模板失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<NotificationTemplate> getAllTemplates() {
        try {
            // 获取当前登录用户
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // 使用条件查询方法，添加租户ID条件
            Map<String, Object> condition = new HashMap<>();
            String tenantId = TenantContext.getTenantId();
            condition.put("tenantId", tenantId);
            
            List<NotificationTemplate> templates = notificationTemplateMapper.selectByParams(condition);
            // 转换状态为大写
            if (templates != null) {
                templates.forEach(template -> {
                    if (template.getStatus() != null) {
                        template.setStatus(template.getStatus().toUpperCase());
                    }
                });
                
                // 数据权限控制：如果不是管理员，只返回自己创建的模板
                if (!isAdmin(currentUser)) {
                    templates = templates.stream()
                            .filter(template -> currentUser.equals(template.getCreatedBy()))
                            .collect(Collectors.toList());
                }
            }
            return templates;
        } catch (Exception e) {
            System.err.println("获取所有模板失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<NotificationTemplate> getEnabledTemplates() {
        try {
            // 获取当前登录用户
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // 使用条件查询方法，添加租户ID和状态条件
            Map<String, Object> condition = new HashMap<>();
            String tenantId = TenantContext.getTenantId();
            condition.put("tenantId", tenantId);
            condition.put("status", "enabled");
            
            List<NotificationTemplate> templates = notificationTemplateMapper.selectByParams(condition);
            // 转换状态为大写
            if (templates != null) {
                templates.forEach(template -> {
                    if (template.getStatus() != null) {
                        template.setStatus(template.getStatus().toUpperCase());
                    }
                });
                
                // 数据权限控制：如果不是管理员，只返回自己创建的模板
                if (!isAdmin(currentUser)) {
                    templates = templates.stream()
                            .filter(template -> currentUser.equals(template.getCreatedBy()))
                            .collect(Collectors.toList());
                }
            }
            return templates;
        } catch (Exception e) {
            System.err.println("获取启用的模板失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<NotificationTemplate> getTemplatesByType(String type) {
        try {
            // 获取当前登录用户
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // 使用条件查询方法，添加租户ID和类型条件
            Map<String, Object> condition = new HashMap<>();
            String tenantId = TenantContext.getTenantId();
            condition.put("tenantId", tenantId);
            condition.put("type", type);
            
            List<NotificationTemplate> templates = notificationTemplateMapper.selectByParams(condition);
            // 转换状态为大写
            if (templates != null) {
                templates.forEach(template -> {
                    if (template.getStatus() != null) {
                        template.setStatus(template.getStatus().toUpperCase());
                    }
                });
                
                // 数据权限控制：如果不是管理员，只返回自己创建的模板
                if (!isAdmin(currentUser)) {
                    templates = templates.stream()
                            .filter(template -> currentUser.equals(template.getCreatedBy()))
                            .collect(Collectors.toList());
                }
            }
            return templates;
        } catch (Exception e) {
            System.err.println("根据类型获取模板失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public NotificationTemplate getTemplateById(String id) {
        try {
            // 获取当前登录用户
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            
            NotificationTemplate template = notificationTemplateMapper.selectById(id);
            // 转换状态为大写
            if (template != null && template.getStatus() != null) {
                template.setStatus(template.getStatus().toUpperCase());
                
                // 租户权限控制：只允许查看自己租户的模板
                if (!tenantId.equals(template.getTenantId())) {
                    return null; // 或者抛出异常
                }
                
                // 数据权限控制：如果不是管理员，只允许查看自己创建的模板
                if (!isAdmin(currentUser) && !currentUser.equals(template.getCreatedBy())) {
                    return null; // 或者抛出异常
                }
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
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            
            NotificationTemplate template = notificationTemplateMapper.selectById(id);
            if (template != null) {
                // 检查权限：只有模板创建者或管理员才能更新模板状态
                if (!currentUser.equals(template.getCreatedBy()) && !isAdmin(currentUser)) {
                    throw new RuntimeException("没有权限更新该模板状态");
                }
                
                // 保存旧名称用于日志
                String oldName = template.getName();
                
                // 更新模板状态和更新人
                template.setStatus(status);
                template.setUpdatedTime(LocalDateTime.now());
                template.setUpdatedBy(currentUser);
                
                int count = notificationTemplateMapper.update(template);
                boolean result = count > 0;
                
                if (result) {
                    // 记录启用/禁用操作日志
                    String operationType = "enabled".equals(status) ? "enable" : "disable";
                    TemplateOperationLog operationLog = new TemplateOperationLog();
                    operationLog.setId(String.valueOf(System.currentTimeMillis()));
                    operationLog.setTemplateId(id);
                    operationLog.setTemplateName(oldName); // 使用旧名称确保正确记录
                    operationLog.setOperationType(operationType);
                    operationLog.setOperator(currentUser);
                    operationLog.setOperationTime(LocalDateTime.now());
                    operationLog.setOperationDetails(operationType + " 模板: " + oldName);
                    operationLog.setIpAddress("127.0.0.1");
                    templateOperationLogService.recordOperationLog(operationLog);
                }
                return result;
            }
            return false;
        } catch (Exception e) {
            System.err.println("更新模板状态失败: " + e.getMessage());
            return false;
        }
    }

    // 直接在业务方法中设置日志，不再使用私有方法，避免方法调用可能出现的问题
}