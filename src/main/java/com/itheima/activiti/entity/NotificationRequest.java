package com.itheima.activiti.entity;

import java.util.Map;

/**
 * 通知发送请求实体类
 */
public class NotificationRequest {
    
    private String templateId; // 模板ID
    private String receiver; // 接收者（邮箱地址或手机号）
    private Map<String, Object> params; // 参数替换数据
    private String senderSystem; // 调用系统标识
    private String requestId; // 请求ID（用于跟踪）
    private String type; // 通知类型（EMAIL或SMS）

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
