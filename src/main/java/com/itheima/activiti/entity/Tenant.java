package com.itheima.activiti.entity;

import java.time.LocalDateTime;

/**
 * 租户实体类
 */
public class Tenant {
    
    private String id; // 租户ID（唯一标识）
    private String appId; // APP ID
    private String secretKey; // 密钥
    private String systemName; // 系统名称
    private String contactName; // 联系人姓名
    private String contactPhone; // 联系人手机号
    private String contactEmail; // 联系人邮箱
    private String enterpriseName; // 企业名称
    private String enterpriseCreditCode; // 企业统一社会信用代码
    private String callbackUrl; // 回调地址
    private String serviceModules; // 所需服务模块（通知模块/工作流模块/两者均需）
    private String callScenarios; // 调用场景
    private String status; // 状态（pending/approved/suspended/cancelled）
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
    private LocalDateTime approvedAt; // 审核通过时间
    private String approvedBy; // 审核人
    private String remark; // 备注
    private String role; // 租户角色（默认根据serviceModules分配）

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getEnterpriseCreditCode() {
        return enterpriseCreditCode;
    }

    public void setEnterpriseCreditCode(String enterpriseCreditCode) {
        this.enterpriseCreditCode = enterpriseCreditCode;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getServiceModules() {
        return serviceModules;
    }

    public void setServiceModules(String serviceModules) {
        this.serviceModules = serviceModules;
    }

    public String getCallScenarios() {
        return callScenarios;
    }

    public void setCallScenarios(String callScenarios) {
        this.callScenarios = callScenarios;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}