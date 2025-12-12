package com.itheima.activiti.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Permission {
    private Long id;
    private String name;
    private String code;
    private String url;
    private String method;
    private String description;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}