package com.itheima.activiti.service.impl;

import com.itheima.activiti.entity.NotificationRequest;
import com.itheima.activiti.entity.NotificationResponse;
import com.itheima.activiti.entity.NotificationTemplate;
import com.itheima.activiti.service.NotificationSenderService;
import com.itheima.activiti.service.NotificationTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 通知发送核心服务实现类
 * 用于打破循环依赖，提供纯发送功能
 */
@Service
public class NotificationSenderServiceImpl implements NotificationSenderService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationSenderServiceImpl.class);
    
    @Autowired
    private NotificationTemplateService notificationTemplateService;
    
    @Autowired
    private JavaMailSender javaMailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Override
    public NotificationResponse sendEmail(NotificationRequest request) {
        NotificationResponse response = new NotificationResponse();
        response.setSendId(UUID.randomUUID().toString());
        response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        try {
            String finalSubject;
            String finalContent;
            String sender;
            
            // 直接使用请求中的subject和content，不依赖模板
            // 这样即使templateId不存在，也能发送通知
            finalSubject = request.getParams() != null && request.getParams().get("subject") != null ? 
                    request.getParams().get("subject").toString() : "测试邮件";
            finalContent = request.getParams() != null && request.getParams().get("content") != null ? 
                    request.getParams().get("content").toString() : "这是一封测试邮件";
            sender = fromEmail;
            
            // 发送真实邮件
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(sender);
            helper.setTo(request.getReceiver());
            helper.setSubject(finalSubject);
            helper.setText(finalContent, true); // true表示HTML内容
            
            // 发送邮件
            javaMailSender.send(message);
            
            logger.info("发送邮件成功 - 发件人: {}, 收件人: {}, 主题: {}", 
                    sender, maskSensitiveInfo(request.getReceiver()), finalSubject);
            logger.info("邮件内容: {}", finalContent);
            
            response.setSuccess(true);
            response.setMessage("邮件发送成功");
            return response;
        } catch (MessagingException e) {
            response.setSuccess(false);
            response.setErrorCode("EMAIL_SEND_FAILED");
            response.setMessage("邮件发送失败: " + e.getMessage());
            logger.error("发送邮件失败", e);
            return response;
        } catch (Exception e) {
            response.setSuccess(false);
            response.setErrorCode("SYSTEM_ERROR");
            response.setMessage("系统错误: " + e.getMessage());
            logger.error("发送邮件时发生系统错误", e);
            return response;
        }
    }
    
    @Override
    public NotificationResponse sendSms(NotificationRequest request) {
        NotificationResponse response = new NotificationResponse();
        response.setSendId(UUID.randomUUID().toString());
        response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        try {
            String finalContent;
            
            // 直接使用请求中的content，不依赖模板
            // 这样即使templateId不存在，也能发送通知
            finalContent = request.getParams() != null && request.getParams().get("content") != null ? 
                    request.getParams().get("content").toString() : "这是一条测试短信";
            
            // 模拟发送短信
            logger.info("[模拟] 发送短信 - 接收号码: {}, 内容: {}", 
                    maskSensitiveInfo(request.getReceiver()), finalContent);
            
            // 在实际项目中，这里应该调用真实的短信发送服务
            // SmsService.send(...);
            
            response.setSuccess(true);
            response.setMessage("短信发送成功（模拟）");
            return response;
        } catch (Exception e) {
            response.setSuccess(false);
            response.setErrorCode("SMS_SEND_FAILED");
            response.setMessage("短信发送失败: " + e.getMessage());
            logger.error("模拟发送短信失败", e);
            return response;
        }
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