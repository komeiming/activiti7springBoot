package com.itheima.activiti.entity;

import java.time.LocalDateTime;

/**
 * 模板操作日志实体类
 */
public class TemplateOperationLog {
    
    private String id; // 日志ID
    private String templateId; // 模板ID
    private String templateName; // 模板名称
    private String operationType; // 操作类型（create/update/delete/enable/disable）
    private String operator; // 操作人
    private LocalDateTime operationTime; // 操作时间
    private String ipAddress; // IP地址
    private String operationDetails; // 操作详情（JSON格式）

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getOperationDetails() {
        return operationDetails;
    }

    public void setOperationDetails(String operationDetails) {
        this.operationDetails = operationDetails;
    }
}
