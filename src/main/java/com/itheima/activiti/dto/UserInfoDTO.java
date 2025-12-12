package com.itheima.activiti.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户信息DTO
 */
@Data
public class UserInfoDTO {
    private String username;
    private String fullName;
    private String email;
    private String department;
    private List<String> roles;
    private List<String> permissions;
}