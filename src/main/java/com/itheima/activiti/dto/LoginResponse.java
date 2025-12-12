package com.itheima.activiti.dto;

/**
 * 登录响应DTO
 */
public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private Long expireTime;

    // 手动实现setter和getter方法
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }
}