package com.itheima.activiti.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleTestController {
    
    @GetMapping("/simple-test")
    public String simpleTest() {
        return "Simple Test Controller is working!";
    }
}