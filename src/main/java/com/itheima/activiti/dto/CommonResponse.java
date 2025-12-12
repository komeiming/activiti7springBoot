package com.itheima.activiti.dto;

/**
 * 通用响应类
 */
public class CommonResponse<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp;

    public CommonResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    // 手动实现setter和getter方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static <T> CommonResponse<T> success(T data) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static <T> CommonResponse<T> fail(int code, String message) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    public static <T> CommonResponse<T> fail(String message) {
        return fail(500, message);
    }

    public static <T> CommonResponse<T> badRequest(String message) {
        return fail(400, message);
    }

    public static <T> CommonResponse<T> unauthorized(String message) {
        return fail(401, message);
    }

    public static <T> CommonResponse<T> forbidden(String message) {
        return fail(403, message);
    }

    public static <T> CommonResponse<T> notFound(String message) {
        return fail(404, message);
    }
}