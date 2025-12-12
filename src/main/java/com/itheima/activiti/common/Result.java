package com.itheima.activiti.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应结果类
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    // 响应状态码
    private int code;
    
    // 响应消息
    private String message;
    
    // 响应数据
    private T data;
    
    // 扩展字段
    private Map<String, Object> extra = new HashMap<>();

    /**
     * 构造方法
     */
    public Result() {
    }

    /**
     * 构造方法
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     */
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 获取状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置状态码
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置数据
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 获取扩展字段
     */
    public Map<String, Object> getExtra() {
        return extra;
    }

    /**
     * 设置扩展字段
     */
    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    /**
     * 添加扩展字段
     */
    public Result<T> addExtra(String key, Object value) {
        this.extra.put(key, value);
        return this;
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage(), null);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message, null);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 构建分页响应
     */
    public static <T> Result<Map<String, Object>> page(T list, long total, int page, int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("pages", total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
        
        return success(data);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }

    /**
     * 响应状态码枚举
     */
    public enum ResultCode {
        // 成功
        SUCCESS(200, "操作成功"),
        
        // 错误
        ERROR(500, "操作失败"),
        
        // 未认证
        UNAUTHORIZED(401, "未认证"),
        
        // 未授权
        FORBIDDEN(403, "未授权"),
        
        // 资源不存在
        NOT_FOUND(404, "资源不存在"),
        
        // 参数错误
        PARAM_ERROR(400, "参数错误"),
        
        // 业务错误
        BUSINESS_ERROR(405, "业务错误"),
        
        // 系统错误
        SYSTEM_ERROR(500, "系统错误");

        private final int code;
        private final String message;

        ResultCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}