package com.itheima.activiti.entity;

import java.time.LocalDateTime;

/**
 * 通知发送日志实体类
 */
public class NotificationLog {
    
    private String id; // 日志ID
    private String sendId; // 发送ID（对应响应中的sendId）
    private String templateId; // 模板ID
    private String templateName; // 模板名称
    private String type; // 通知类型（email/sms）
    private String receiver; // 接收者（脱敏处理）
    private String params; // 参数替换数据（JSON格式）
    private String senderSystem; // 调用系统标识
    private String requestId; // 请求ID
    private boolean success; // 发送是否成功
    private String errorCode; // 错误代码（如果失败）
    private String errorMessage; // 错误信息（如果失败）
    private LocalDateTime sendTime; // 发送时间

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSendId() {
        return sendId;
    }

    public void setSendId(String sendId) {
        this.sendId = sendId;
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }
}
