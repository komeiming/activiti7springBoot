package com.itheima.activiti.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单的测试Controller，用于验证Spring Boot应用是否正常启动
 */
@RestController
public class TestController {

    @RequestMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Spring Boot application is running!");
        result.put("status", "success");
        return result;
    }
}