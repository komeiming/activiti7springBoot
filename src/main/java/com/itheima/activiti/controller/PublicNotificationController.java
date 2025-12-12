package com.itheima.activiti.controller;

import com.itheima.activiti.common.Result;
import com.itheima.activiti.common.TenantContext;
import com.itheima.activiti.entity.NotificationConfig;
import com.itheima.activiti.entity.NotificationRequest;
import com.itheima.activiti.entity.NotificationResponse;
import com.itheima.activiti.entity.NotificationTemplate;
import com.itheima.activiti.service.NotificationConfigService;
import com.itheima.activiti.service.NotificationService;
import com.itheima.activiti.service.NotificationTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知模块公有服务控制器
 */
@RestController
@RequestMapping("/api/v1/notification")
public class PublicNotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(PublicNotificationController.class);
    
    @Autowired
    private NotificationTemplateService notificationTemplateService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private com.itheima.activiti.service.NotificationLogService notificationLogService;
    
    @Autowired
    private NotificationConfigService notificationConfigService;
    
    // ================ 模板管理接口 ================    
    
    /**
     * 创建通知模板
     */
    @PostMapping("/template")
    public Result<NotificationTemplate> createNotificationTemplate(@RequestBody NotificationTemplate template) {
        try {
            logger.info("创建通知模板请求: {}", template);
            
            // 设置当前租户ID
            String tenantId = TenantContext.getTenantId();
            template.setTenantId(tenantId);
            
            NotificationTemplate createdTemplate = notificationTemplateService.createTemplate(template);
            return Result.success("模板创建成功", createdTemplate);
        } catch (Exception e) {
            logger.error("创建通知模板失败:", e);
            return Result.error("模板创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询自身模板
     */
    @GetMapping("/template")
    public Result<Map<String, Object>> getNotificationTemplates(
            @RequestParam(required = false) String templateId,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("查询通知模板请求, templateId: {}, templateName: {}, type: {}, tenantId: {}", 
                       templateId, templateName, type, TenantContext.getTenantId());
            
            // 构建查询条件
            Map<String, Object> params = new HashMap<>();
            // 明确添加租户ID条件，确保只返回当前租户的模板
            String tenantId = TenantContext.getTenantId();
            params.put("tenantId", tenantId);
            logger.info("查询模板时使用的条件: {}", params);
            
            if (templateId != null && !templateId.trim().isEmpty()) {
                params.put("id", templateId);
            }
            if (templateName != null && !templateName.trim().isEmpty()) {
                params.put("name", templateName);
            }
            if (type != null && !type.trim().isEmpty()) {
                params.put("type", type);
            }
            // 计算分页偏移量
            int offset = (page - 1) * size;
            params.put("offset", offset);
            params.put("limit", size);
            
            // 查询模板列表
            java.util.List<NotificationTemplate> templates = notificationTemplateService.getTemplatesByCondition(params);
            logger.info("查询到的模板数量: {}", templates != null ? templates.size() : 0);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("data", templates);
            response.put("total", templates != null ? templates.size() : 0);
            response.put("page", page);
            response.put("size", size);
            
            logger.info("返回的模板列表响应: {}", response);
            return Result.success(response);
        } catch (Exception e) {
            logger.error("查询通知模板失败:", e);
            return Result.error("查询模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改自身模板
     */
    @PutMapping("/template/{templateId}")
    public Result<NotificationTemplate> updateNotificationTemplate(
            @PathVariable String templateId, 
            @RequestBody NotificationTemplate template) {
        try {
            logger.info("修改通知模板请求, templateId: {}, template: {}", templateId, template);
            
            // 设置模板ID和租户ID
            template.setId(templateId);
            template.setTenantId(TenantContext.getTenantId());
            
            NotificationTemplate updatedTemplate = notificationTemplateService.updateTemplate(template);
            return Result.success("模板更新成功", updatedTemplate);
        } catch (Exception e) {
            logger.error("修改通知模板失败:", e);
            return Result.error("模板更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除自身模板
     */
    @DeleteMapping("/template/{templateId}")
    public Result<Boolean> deleteNotificationTemplate(@PathVariable String templateId) {
        try {
            logger.info("删除通知模板请求, templateId: {}", templateId);
            
            // 验证模板是否属于当前租户
            NotificationTemplate template = notificationTemplateService.getTemplateById(templateId);
            if (template == null || !TenantContext.getTenantId().equals(template.getTenantId())) {
                return Result.error("模板不存在或无权限操作");
            }
            
            boolean result = notificationTemplateService.deleteTemplate(templateId);
            if (result) {
                return Result.success("模板删除成功", true);
            } else {
                return Result.error("模板删除失败: 模板不存在");
            }
        } catch (Exception e) {
            logger.error("删除通知模板失败:", e);
            return Result.error("模板删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 模板禁用/启用
     */
    @PutMapping("/template/{templateId}/status")
    public Result<Boolean> updateTemplateStatus(
            @PathVariable String templateId, 
            @RequestParam String status) {
        try {
            logger.info("更新模板状态请求, templateId: {}, status: {}", templateId, status);
            
            // 验证模板是否属于当前租户
            NotificationTemplate template = notificationTemplateService.getTemplateById(templateId);
            if (template == null || !TenantContext.getTenantId().equals(template.getTenantId())) {
                return Result.error("模板不存在或无权限操作");
            }
            
            boolean result = notificationTemplateService.updateTemplateStatus(templateId, status);
            if (result) {
                return Result.success("模板状态更新成功", true);
            } else {
                return Result.error("模板状态更新失败: 模板不存在");
            }
        } catch (Exception e) {
            logger.error("更新模板状态失败:", e);
            return Result.error("模板状态更新失败: " + e.getMessage());
        }
    }
    
    // ================ 通知发送接口 ================    
    
    /**
     * 单个通知发送
     */
    @PostMapping("/send/single")
    public Result<NotificationResponse> sendSingleNotification(@RequestBody NotificationRequest request) {
        try {
            logger.info("发送单个通知请求: {}", request);
            
            // 设置当前租户信息
            request.setTenantId(TenantContext.getTenantId());
            request.setAppId(TenantContext.getAppId());
            
            NotificationResponse response = notificationService.sendNotification(request);
            if (response.isSuccess()) {
                return Result.success(response.getMessage(), response);
            } else {
                return Result.error(Result.ResultCode.BUSINESS_ERROR.getCode(), response.getMessage());
            }
        } catch (Exception e) {
            logger.error("发送单个通知失败:", e);
            return Result.error("发送通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量通知发送
     */
    @PostMapping("/send/batch")
    public Result<NotificationResponse> sendBatchNotification(@RequestBody NotificationRequest request) {
        try {
            logger.info("发送批量通知请求: {}", request);
            
            // 设置当前租户信息
            request.setTenantId(TenantContext.getTenantId());
            request.setAppId(TenantContext.getAppId());
            
            // 批量发送通知，单次最多1000条
            NotificationResponse response = notificationService.sendBatchNotification(request);
            if (response.isSuccess()) {
                return Result.success(response.getMessage(), response);
            } else {
                return Result.error(Result.ResultCode.BUSINESS_ERROR.getCode(), response.getMessage());
            }
        } catch (Exception e) {
            logger.error("发送批量通知失败:", e);
            return Result.error("发送批量通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 定时通知发送
     */
    @PostMapping("/send/scheduled")
    public Result<NotificationResponse> sendScheduledNotification(@RequestBody NotificationRequest request) {
        try {
            logger.info("发送定时通知请求: {}", request);
            
            // 设置当前租户信息
            request.setTenantId(TenantContext.getTenantId());
            request.setAppId(TenantContext.getAppId());
            
            NotificationResponse response = notificationService.sendScheduledNotification(request);
            if (response.isSuccess()) {
                return Result.success(response.getMessage(), response);
            } else {
                return Result.error(Result.ResultCode.BUSINESS_ERROR.getCode(), response.getMessage());
            }
        } catch (Exception e) {
            logger.error("发送定时通知失败:", e);
            return Result.error("发送定时通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询发送日志
     */
    @GetMapping("/log")
    public Result<Map<String, Object>> getNotificationLogs(
            @RequestParam(required = false) String sendId,
            @RequestParam(required = false) String templateId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("查询通知发送日志请求, sendId: {}, templateId: {}, status: {}", 
                       sendId, templateId, status);
            
            // 构建查询条件
            Map<String, Object> params = new HashMap<>();
            params.put("tenantId", TenantContext.getTenantId());
            if (sendId != null && !sendId.trim().isEmpty()) {
                params.put("sendId", sendId);
            }
            if (templateId != null && !templateId.trim().isEmpty()) {
                params.put("templateId", templateId);
            }
            if (status != null && !status.trim().isEmpty()) {
                params.put("success", "success".equals(status));
            }
            if (startTime != null && !startTime.trim().isEmpty()) {
                params.put("startTime", startTime);
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                params.put("endTime", endTime);
            }
            params.put("page", page);
            params.put("size", size);
            
            // 查询日志列表
            java.util.List<com.itheima.activiti.entity.NotificationLog> logs = 
                notificationLogService.getLogList(params);
            
            // 获取日志总数
            int total = notificationLogService.countLogs(params);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("data", logs);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            
            return Result.success(response);
        } catch (Exception e) {
            logger.error("查询通知发送日志失败:", e);
            return Result.error("查询发送日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 重试失败通知
     */
    @PostMapping("/log/{logId}/retry")
    public Result<NotificationResponse> retryFailedNotification(@PathVariable String logId) {
        try {
            logger.info("重试失败通知请求, logId: {}", logId);
            
            NotificationResponse response = notificationService.retryFailedNotification(logId);
            if (response.isSuccess()) {
                return Result.success(response.getMessage(), response);
            } else {
                return Result.error(Result.ResultCode.BUSINESS_ERROR.getCode(), response.getMessage());
            }
        } catch (Exception e) {
            logger.error("重试失败通知失败:", e);
            return Result.error("重试通知失败: " + e.getMessage());
        }
    }
    
    // ================ 通知配置接口 ================
    
    /**
     * 获取租户通知配置
     */
    @GetMapping("/config")
    public Result<NotificationConfig> getNotificationConfig() {
        try {
            logger.info("获取租户通知配置请求");
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            NotificationConfig config = notificationConfigService.getConfigByTenantId(tenantId);
            
            return Result.success(config);
        } catch (Exception e) {
            logger.error("获取通知配置失败:", e);
            return Result.error("获取通知配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存或更新租户通知配置
     */
    @PutMapping("/config")
    public Result<NotificationConfig> saveNotificationConfig(@RequestBody NotificationConfig config) {
        try {
            logger.info("保存通知配置请求: {}", config);
            
            // 设置当前租户ID
            String tenantId = TenantContext.getTenantId();
            config.setTenantId(tenantId);
            
            NotificationConfig updatedConfig = notificationConfigService.saveOrUpdateConfig(config);
            return Result.success("配置保存成功", updatedConfig);
        } catch (Exception e) {
            logger.error("保存通知配置失败:", e);
            return Result.error("保存配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置租户通知配置
     */
    @PostMapping("/config/reset")
    public Result<NotificationConfig> resetNotificationConfig() {
        try {
            logger.info("重置通知配置请求");
            
            // 获取当前租户ID
            String tenantId = TenantContext.getTenantId();
            NotificationConfig config = notificationConfigService.resetConfig(tenantId);
            
            return Result.success("配置重置成功", config);
        } catch (Exception e) {
            logger.error("重置通知配置失败:", e);
            return Result.error("重置配置失败: " + e.getMessage());
        }
    }
}