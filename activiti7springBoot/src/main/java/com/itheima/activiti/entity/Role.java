package com.itheima.activiti.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Role {
    private Long id;
    private String name;
    private String code;
    private String description;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    
    // 角色拥有的权限列表
    private List<Permission> permissions;
}