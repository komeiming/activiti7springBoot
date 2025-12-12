package com.itheima.activiti.dto;

/**
 * 登录请求DTO
 */
public class LoginRequest {
    private String username;
    private String password;

    // 手动实现getter和setter方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}