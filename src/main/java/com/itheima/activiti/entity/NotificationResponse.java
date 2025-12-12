package com.itheima.activiti.entity;

/**
 * 通知发送响应实体类
 */
public class NotificationResponse {
    
    private String sendId; // 发送ID（用于日志查询）
    private boolean success; // 发送是否成功
    private String message; // 响应消息
    private String errorCode; // 错误代码（如果失败）
    private String timestamp; // 发送时间戳

    public String getSendId() {
        return sendId;
    }

    public void setSendId(String sendId) {
        this.sendId = sendId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
