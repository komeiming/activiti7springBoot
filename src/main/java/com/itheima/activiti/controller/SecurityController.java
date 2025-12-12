package com.itheima.activiti.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 安全控制器，提供简单的用户信息
 */
@RestController
public class SecurityController {

    /**
     * 模拟用户登录，返回当前用户信息
     */
    @GetMapping("/login")
    public Map<String, Object> login(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("username", username);
        result.put("message", "Login successful as " + username);
        
        // 根据用户名判断权限级别
        if (username.equals("admin")) {
            result.put("role", "ROLE_ACTIVITI_ADMIN");
            result.put("privileges", "Admin privileges");
        } else {
            result.put("role", "ROLE_ACTIVITI_USER");
            result.put("privileges", "User privileges");
        }
        
        return result;
    }

    /**
     * 测试端点
     */
    @GetMapping("/test/permission")
    public String testPermission(@RequestParam String username) {
        return "Permission test: Logged in as " + username + ". Try accessing /user/info.";
    }
}
