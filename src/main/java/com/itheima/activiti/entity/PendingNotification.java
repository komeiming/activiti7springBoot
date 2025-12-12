package com.itheima.activiti.entity;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 待发送通知实体类
 */
public class PendingNotification {
    
    private String id; // 待发送ID
    private String tenantId; // 租户ID
    private String templateId; // 模板ID
    private String templateName; // 模板名称
    private String type; // 通知类型（email/sms）
    private String receiver; // 接收者（邮箱地址或手机号）
    private String subject; // 邮件主题（如果是邮件）
    private String content; // 邮件内容或短信内容
    private String params; // 参数替换数据（JSON格式字符串）
    private String senderSystem; // 调用系统标识
    private String requestId; // 请求ID
    private Integer priority; // 优先级（0-低，1-中，2-高）
    private String status; // 状态（pending-待发送，sending-发送中，sent-已发送，failed-发送失败）
    private Integer retryCount; // 重试次数
    private Integer maxRetries; // 最大重试次数
    private LocalDateTime nextRetryTime; // 下次重试时间
    private LocalDateTime createdTime; // 创建时间
    private LocalDateTime updatedTime; // 更新时间

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getSenderSystem() {
        return senderSystem;
    }

    public void setSenderSystem(String senderSystem) {
        this.senderSystem = senderSystem;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public LocalDateTime getNextRetryTime() {
        return nextRetryTime;
    }

    public void setNextRetryTime(LocalDateTime nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}