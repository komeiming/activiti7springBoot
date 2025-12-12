package com.itheima.activiti.exception;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {

    private String errorCode;
    private String errorMessage;

    /**
     * 构造方法
     * @param message 异常消息
     */
    public BusinessException(String message) {
        super(message);
        this.errorMessage = message;
        this.errorCode = "BUSINESS_ERROR";
    }

    /**
     * 构造方法
     * @param errorCode 错误编码
     * @param message 异常消息
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    /**
     * 构造方法
     * @param message 异常消息
     * @param cause 异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
        this.errorCode = "BUSINESS_ERROR";
    }

    /**
     * 构造方法
     * @param errorCode 错误编码
     * @param message 异常消息
     * @param cause 异常原因
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    /**
     * 获取错误编码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 设置错误编码
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 获取错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 设置错误消息
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}