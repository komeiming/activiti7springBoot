package com.itheima.activiti.dto;

import java.util.Map;

/**
 * 任务操作请求DTO
 */
public class TaskActionRequest {
    private String action;                  // 操作类型（approve/reject/confirm/complete等）
    private String comment;                 // 操作意见或评论
    private String assignee;                // 处理人
    private Map<String, Object> variables;  // 流程变量
    private boolean needTraining;           // 是否需要培训（特定场景使用）
    private boolean needResource;           // 是否需要资源（特定场景使用）
    private String nextAssignee;            // 下一处理人
    private String businessKey;             // 业务ID
    private String processInstanceId;       // 流程实例ID
    private String reason;                  // 拒绝或取消原因

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
    
    // 添加缺少的方法
    public void setApproved(boolean approved) {
        // 将approved值添加到variables中
        if (this.variables == null) {
            this.variables = new java.util.HashMap<>();
        }
        this.variables.put("approved", approved);
    }
    
    public void setActionType(String actionType) {
        this.action = actionType; // 使用现有的action字段来存储actionType
    }

    public boolean isNeedTraining() {
        return needTraining;
    }

    public void setNeedTraining(boolean needTraining) {
        this.needTraining = needTraining;
    }

    public boolean isNeedResource() {
        return needResource;
    }

    public void setNeedResource(boolean needResource) {
        this.needResource = needResource;
    }

    public String getNextAssignee() {
        return nextAssignee;
    }

    public void setNextAssignee(String nextAssignee) {
        this.nextAssignee = nextAssignee;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}