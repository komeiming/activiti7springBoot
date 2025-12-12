package com.itheima.activiti.controller;

import com.itheima.activiti.common.Result;
import com.itheima.activiti.entity.NotificationRequest;
import com.itheima.activiti.entity.NotificationResponse;
import com.itheima.activiti.entity.NotificationTemplate;
import com.itheima.activiti.service.NotificationService;
import com.itheima.activiti.service.NotificationTemplateService;
import com.itheima.activiti.service.TemplateOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知服务控制器
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationTemplateService notificationTemplateService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private TemplateOperationLogService templateOperationLogService;
    
    @Autowired
    private com.itheima.activiti.service.NotificationLogService notificationLogService;
    
    // ================ 模板管理接口 ================
    
    /**
     * 创建模板
     */
    @PostMapping("/templates")
    public Result<NotificationTemplate> createTemplate(@RequestBody NotificationTemplate template) {
        try {
            NotificationTemplate createdTemplate = notificationTemplateService.createTemplate(template);
            if (createdTemplate == null) {
                return Result.error("模板创建失败: 服务层处理失败");
            }
            return Result.success("模板创建成功", createdTemplate);
        } catch (Exception e) {
            return Result.error("模板创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新模板
     */
    @PutMapping("/templates/{id}")
    public Result<NotificationTemplate> updateTemplate(@PathVariable String id, @RequestBody NotificationTemplate template) {
        try {
            // 确保ID一致
            if (template != null) {
                template.setId(id);
                NotificationTemplate updatedTemplate = notificationTemplateService.updateTemplate(template);
                return Result.success("模板更新成功", updatedTemplate);
            } else {
                return Result.error("模板对象不能为空");
            }
        } catch (Exception e) {
            return Result.error("模板更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除模板（逻辑删除）
     */
    @DeleteMapping("/templates/{id}")
    public Result<Boolean> deleteTemplate(@PathVariable String id) {
        try {
            boolean result = notificationTemplateService.deleteTemplate(id);
            if (result) {
                return Result.success("模板删除成功", true);
            } else {
                return Result.error("模板删除失败: 模板不存在");
            }
        } catch (Exception e) {
            return Result.error("模板删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询模板
     */
    @GetMapping("/templates/{id}")
    public Result<NotificationTemplate> getTemplateById(@PathVariable String id) {
        try {
            NotificationTemplate template = notificationTemplateService.getTemplateById(id);
            if (template != null) {
                return Result.success(template);
            } else {
                return Result.error("模板不存在");
            }
        } catch (Exception e) {
            return Result.error("查询模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 按条件查询模板列表（支持分页）
     */
    @GetMapping("/templates")
    public Result<Map<String, Object>> getTemplates(@RequestParam Map<String, Object> params) {
        try {
            // 处理分页参数：将page和size转换为offset和limit
            if (params.containsKey("page") && params.containsKey("size")) {
                try {
                    int page = Integer.parseInt(params.get("page").toString());
                    int size = Integer.parseInt(params.get("size").toString());
                    int offset = (page - 1) * size;
                    params.put("offset", offset);
                    params.put("limit", size);
                } catch (NumberFormatException e) {
                    // 忽略无效的分页参数
                }
            }
            
            // 获取模板列表
            java.util.List<NotificationTemplate> templates = notificationTemplateService.getTemplatesByCondition(params);
            
            // 构建分页响应
            Map<String, Object> response = new HashMap<>();
            response.put("list", templates);
            response.put("total", templates != null ? templates.size() : 0);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("查询模板列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新模板状态 - 启用
     */
    @PutMapping("/templates/{id}/enable")
    public Result<Boolean> enableTemplate(@PathVariable String id) {
        try {
            boolean result = notificationTemplateService.updateTemplateStatus(id, "ENABLED");
            if (result) {
                return Result.success("模板启用成功", true);
            } else {
                return Result.error("模板启用失败: 模板不存在");
            }
        } catch (Exception e) {
            return Result.error("模板启用失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新模板状态 - 禁用
     */
    @PutMapping("/templates/{id}/disable")
    public Result<Boolean> disableTemplate(@PathVariable String id) {
        try {
            boolean result = notificationTemplateService.updateTemplateStatus(id, "DISABLED");
            if (result) {
                return Result.success("模板禁用成功", true);
            } else {
                return Result.error("模板禁用失败: 模板不存在");
            }
        } catch (Exception e) {
            return Result.error("模板禁用失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有启用的模板
     */
    @GetMapping("/templates/enabled")
    public Result<java.util.List<NotificationTemplate>> getEnabledTemplates() {
        try {
            java.util.List<NotificationTemplate> templates = notificationTemplateService.getEnabledTemplates();
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("查询启用模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据类型获取模板
     */
    @GetMapping("/templates/type/{type}")
    public Result<java.util.List<NotificationTemplate>> getTemplatesByType(@PathVariable String type) {
        try {
            java.util.List<NotificationTemplate> templates = notificationTemplateService.getTemplatesByType(type);
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("查询模板失败: " + e.getMessage());
        }
    }
    
    // ================ 通知发送接口 ================
    
    /**
     * 发送通知
     */
    @PostMapping("/send")
    public Result<NotificationResponse> sendNotification(@RequestBody NotificationRequest request) {
        try {
            NotificationResponse response = notificationService.sendNotification(request);
            if (response.isSuccess()) {
                return Result.success(response.getMessage(), response);
            } else {
                return Result.error(Result.ResultCode.BUSINESS_ERROR.getCode(), response.getMessage());
            }
        } catch (Exception e) {
            return Result.error("发送通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试发送邮件（用于前端测试）
     */
    @PostMapping("/test/email")
    public Result<NotificationResponse> testSendEmail(@RequestBody NotificationRequest request) {
        try {
            // 确保params不为null
            if (request.getParams() == null) {
                request.setParams(new HashMap<>());
            }
            logger.info("调用notificationService.sendEmail(request)");
            NotificationResponse response = notificationService.sendEmail(request);
            logger.info("notificationService.sendEmail(request)返回: {}", response);
            if (response.isSuccess()) {
                return Result.success("测试发送成功", response);
            } else {
                return Result.error(response.getMessage());
            }
        } catch (Exception e) {
            logger.error("testSendEmail方法发生异常: ", e);
            return Result.error("测试发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试发送短信（用于前端测试）
     */
    @PostMapping("/test/sms")
    public Result<NotificationResponse> testSendSms(@RequestBody NotificationRequest request) {
        try {
            // 确保params不为null
            if (request.getParams() == null) {
                request.setParams(new HashMap<>());
            }
            NotificationResponse response = notificationService.sendSms(request);
            if (response.isSuccess()) {
                return Result.success("测试发送成功", response);
            } else {
                return Result.error(response.getMessage());
            }
        } catch (Exception e) {
            return Result.error("测试发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模板操作日志
     */
    @GetMapping("/template-logs")
    public Result<Map<String, Object>> getTemplateOperationLogs(@RequestParam Map<String, Object> params) {
        try {
            // 获取模板操作日志列表
            java.util.List<com.itheima.activiti.entity.TemplateOperationLog> logs = templateOperationLogService.getLogList(params);
            
            // 获取日志总数
            int total = templateOperationLogService.countLogs(params);
            
            // 构建分页响应
            Map<String, Object> response = new HashMap<>();
            response.put("list", logs);
            response.put("total", total);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("获取模板操作日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取通知发送日志
     */
    @GetMapping("/logs")
    public Result<Map<String, Object>> getNotificationLogs(@RequestParam Map<String, Object> params) {
        try {
            // 获取通知发送日志列表
            java.util.List<com.itheima.activiti.entity.NotificationLog> logs = notificationLogService.getLogList(params);
            
            // 获取日志总数
            int total = notificationLogService.countLogs(params);
            
            // 构建分页响应
            Map<String, Object> response = new HashMap<>();
            response.put("list", logs);
            response.put("total", total);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("获取通知发送日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 重试发送失败的通知
     */
    @PutMapping("/logs/{id}/retry")
    public Result<NotificationResponse> retryFailedNotification(@PathVariable String id) {
        try {
            NotificationResponse response = notificationService.retryFailedNotification(id);
            if (response.isSuccess()) {
                return Result.success(response.getMessage(), response);
            } else {
                return Result.error(response.getMessage());
            }
        } catch (Exception e) {
            return Result.error("重试发送通知失败: " + e.getMessage());
        }
    }
}
