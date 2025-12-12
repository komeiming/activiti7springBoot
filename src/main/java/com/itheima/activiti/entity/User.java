package com.itheima.activiti.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private String position;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    
    // 用户拥有的角色列表
    private List<Role> roles;
    
    // 用户拥有的权限列表
    private List<String> permissions;
}