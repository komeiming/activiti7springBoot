package com.itheima.activiti.service.impl;

import com.itheima.activiti.entity.NotificationLog;
import com.itheima.activiti.entity.NotificationRequest;
import com.itheima.activiti.entity.NotificationResponse;
import com.itheima.activiti.entity.NotificationTemplate;
import com.itheima.activiti.entity.PendingNotification;
import com.itheima.activiti.service.NotificationLogService;
import com.itheima.activiti.service.NotificationService;
import com.itheima.activiti.service.NotificationTemplateService;
import com.itheima.activiti.service.PendingNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 通知发送服务实现类
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    @Autowired
    private NotificationTemplateService notificationTemplateService;
    
    @Autowired
    private NotificationLogService notificationLogService;
    
    @Autowired
    private PendingNotificationService pendingNotificationService;
    
    // 用于JSON序列化
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public NotificationResponse sendNotification(NotificationRequest request) {
        logger.info("收到通知发送请求: templateId={}, receiver={}, type={}, senderSystem={}", 
                request.getTemplateId(), maskSensitiveInfo(request.getReceiver()), request.getType(), request.getSenderSystem());
        
        // 生成发送ID
        String sendId = UUID.randomUUID().toString();
        NotificationResponse response = new NotificationResponse();
        response.setSendId(sendId);
        response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        NotificationTemplate template = null;
        try {
            logger.info("开始处理通知发送请求: sendId={}", sendId);
            
            // 验证请求参数
            if (request.getReceiver() == null) {
                response.setSuccess(false);
                response.setErrorCode("INVALID_PARAMS");
                response.setMessage("缺少必要参数：receiver");
                logger.error("发送通知失败: {}", response.getMessage());
                return response;
            }
            
            // 确定通知类型
            String notificationType = request.getType();
            if (notificationType == null || notificationType.isEmpty()) {
                if (request.getTemplateId() != null) {
                    // 如果有templateId，从模板获取类型
                    template = notificationTemplateService.getTemplateById(request.getTemplateId());
                    if (template != null) {
                        notificationType = template.getType();
                    }
                } else {
                    // 如果没有templateId和type，默认使用EMAIL
                    notificationType = "EMAIL";
                }
            }
            
            logger.info("确定通知类型: {}", notificationType);
            
            // 验证接收者格式
            if (notificationType.equalsIgnoreCase("email")) {
                // 验证邮箱格式
                if (!validateEmail(request.getReceiver())) {
                    response.setSuccess(false);
                    response.setErrorCode("INVALID_EMAIL");
                    response.setMessage("邮箱格式不正确");
                    logger.error("发送邮件失败: 邮箱格式不正确 - {}", maskSensitiveInfo(request.getReceiver()));
                    return response;
                }
            } else if (notificationType.equalsIgnoreCase("sms")) {
                // 验证手机号格式
                if (!validatePhone(request.getReceiver())) {
                    response.setSuccess(false);
                    response.setErrorCode("INVALID_PHONE");
                    response.setMessage("手机号格式不正确");
                    logger.error("发送短信失败: 手机号格式不正确 - {}", maskSensitiveInfo(request.getReceiver()));
                    return response;
                }
            } else {
                response.setSuccess(false);
                response.setErrorCode("UNSUPPORTED_TYPE");
                response.setMessage("不支持的通知类型");
                logger.error("发送通知失败: 不支持的通知类型 - {}", notificationType);
                return response;
            }
            
            // 创建待发送通知
            PendingNotification pendingNotification = new PendingNotification();
            String finalSubject = null;
            String finalContent = null;
            
            if (request.getTemplateId() != null) {
                logger.info("使用模板发送通知: templateId={}", request.getTemplateId());
                // 使用模板发送
                template = notificationTemplateService.getTemplateById(request.getTemplateId());
                if (template == null) {
                    response.setSuccess(false);
                    response.setErrorCode("TEMPLATE_NOT_FOUND");
                    response.setMessage("模板不存在");
                    logger.error("发送通知失败: 模板ID={} 不存在", request.getTemplateId());
                    return response;
                }
                
                // 检查模板状态
                if (template.getStatus() == null || !template.getStatus().equalsIgnoreCase("enabled")) {
                    response.setSuccess(false);
                    response.setErrorCode("TEMPLATE_DISABLED");
                    response.setMessage("模板已禁用");
                    logger.error("发送通知失败: 模板ID={} 已禁用", request.getTemplateId());
                    return response;
                }
                
                // 替换模板参数
                if (template.getType() != null && template.getType().equalsIgnoreCase("email")) {
                    finalSubject = replaceTemplateParams(template.getSubject(), request.getParams());
                    finalContent = replaceTemplateParams(template.getContent(), request.getParams());
                } else {
                    finalContent = replaceTemplateParams(template.getContent(), request.getParams());
                }
                
                pendingNotification.setTemplateId(template.getId());
                pendingNotification.setTemplateName(template.getName());
                pendingNotification.setType(template.getType());
            } else {
                logger.info("直接发送测试通知，不使用模板");
                // 直接发送测试通知，不使用模板
                pendingNotification.setTemplateId("test");
                pendingNotification.setTemplateName("测试模板");
                pendingNotification.setType(notificationType);
                
                if (notificationType.equalsIgnoreCase("email")) {
                    finalSubject = request.getParams() != null && request.getParams().get("subject") != null ? 
                            request.getParams().get("subject").toString() : "测试邮件";
                    finalContent = request.getParams() != null && request.getParams().get("content") != null ? 
                            request.getParams().get("content").toString() : "这是一封测试邮件";
                } else {
                    finalContent = request.getParams() != null && request.getParams().get("content") != null ? 
                            request.getParams().get("content").toString() : "这是一条测试短信";
                }
            }
            
            // 设置待发送通知的其他属性
            pendingNotification.setReceiver(request.getReceiver());
            pendingNotification.setSubject(finalSubject);
            pendingNotification.setContent(finalContent);
            // 将params转换为JSON字符串
            if (request.getParams() != null) {
                try {
                    String paramsJson = objectMapper.writeValueAsString(request.getParams());
                    pendingNotification.setParams(paramsJson);
                } catch (Exception e) {
                    logger.error("参数序列化失败", e);
                    pendingNotification.setParams(null);
                }
            }
            pendingNotification.setSenderSystem(request.getSenderSystem());
            pendingNotification.setRequestId(request.getRequestId());
            
            logger.info("保存待发送通知到数据库: sendId={}, receiver={}", sendId, maskSensitiveInfo(request.getReceiver()));
            
            // 保存到待发送通知表
            pendingNotificationService.savePendingNotification(pendingNotification);
            
            // 返回成功响应
            response.setSuccess(true);
            response.setMessage("通知已添加到待发送队列");
            logger.info("通知已添加到待发送队列: sendId={}, templateId={}", sendId, request.getTemplateId());
            return response;
        } catch (Exception e) {
            response.setSuccess(false);
            response.setErrorCode("SYSTEM_ERROR");
            response.setMessage("系统错误: " + e.getMessage());
            logger.error("发送通知时发生系统错误", e);
            return response;
        } finally {
            logger.info("通知发送请求处理完成: sendId={}, success={}", sendId, response.isSuccess());
            // 记录日志
            recordLog(request, template, response);
        }
    }
    
    @Override
    public NotificationResponse sendEmail(NotificationRequest request) {
        // 设置通知类型为邮件
        request.setType("EMAIL");
        // 调用统一的发送方法
        return sendNotification(request);
    }
    
    @Override
    public NotificationResponse sendSms(NotificationRequest request) {
        // 设置通知类型为短信
        request.setType("SMS");
        // 调用统一的发送方法
        return sendNotification(request);
    }
    
    @Override
    public String replaceTemplateParams(String templateContent, java.util.Map<String, Object> params) {
        if (templateContent == null || params == null) {
            return templateContent;
        }
        
        // 支持 {paramName} 格式的参数替换
        String result = templateContent;
        for (java.util.Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
    
    /**
     * 记录通知发送日志
     */
    private void recordLog(NotificationRequest request, NotificationTemplate template, NotificationResponse response) {
        if (response == null || request == null) {
            return;
        }
        
        try {
            NotificationLog log = new NotificationLog();
            log.setSendId(response.getSendId());
            // 设置templateId，确保不为NULL
            String templateId = request.getTemplateId();
            if (templateId == null || templateId.isEmpty()) {
                templateId = UUID.randomUUID().toString();
            }
            log.setTemplateId(templateId);
            if (template != null) {
                log.setTemplateName(template.getName());
                log.setType(template.getType());
            } else {
                // 当template为NULL时，设置默认值
                log.setTemplateName("未知模板");
                log.setType("unknown");
            }
            // 对接收者进行脱敏处理，确保receiver不为NULL
            String receiver = request.getReceiver();
            if (receiver == null) {
                receiver = "未知接收者";
            }
            log.setReceiver(maskReceiver(receiver));
            // 将参数转换为JSON字符串
            if (request.getParams() != null) {
                try {
                    log.setParams(objectMapper.writeValueAsString(request.getParams()));
                } catch (Exception e) {
                    logger.error("参数序列化失败", e);
                }
            }
            log.setSenderSystem(request.getSenderSystem());
            log.setRequestId(request.getRequestId());
            log.setSuccess(response.isSuccess());
            log.setErrorCode(response.getErrorCode());
            log.setErrorMessage(response.getMessage());
            log.setSendTime(LocalDateTime.now());
            
            // 保存日志
            notificationLogService.recordNotificationLog(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            logger.error("记录通知日志失败", e);
        }
    }
    
    /**
     * 对接收者进行脱敏处理
     */
    private String maskReceiver(String receiver) {
        if (receiver == null) {
            return null;
        }
        
        // 邮箱脱敏处理
        if (receiver.contains("@")) {
            int atIndex = receiver.indexOf('@');
            if (atIndex > 3) {
                String username = receiver.substring(0, atIndex);
                String domain = receiver.substring(atIndex);
                String maskedUsername = username.substring(0, 3) + "****" + username.substring(Math.min(username.length(), 7));
                return maskedUsername + domain;
            }
        }
        // 手机号脱敏处理
        else if (receiver.matches("\\d{11}")) {
            return receiver.substring(0, 3) + "****" + receiver.substring(7);
        }
        
        return receiver;
    }
    
    // 验证邮箱格式
    private boolean validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    
    // 验证手机号格式（简单验证中国大陆手机号）
    private boolean validatePhone(String phone) {
        String phoneRegex = "^1[3-9]\\d{9}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }
    
    // 敏感信息脱敏
    private String maskSensitiveInfo(String info) {
        if (info == null) return null;
        
        // 如果是手机号，隐藏中间4位
        if (info.matches("^1[3-9]\\d{9}$")) {
            return info.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        }
        
        // 如果是邮箱，隐藏用户名部分
        if (info.contains("@")) {
            int atIndex = info.indexOf('@');
            if (atIndex > 2) {
                return info.substring(0, 2) + "****" + info.substring(atIndex);
            }
        }
        
        return info;
    }
}
