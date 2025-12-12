package com.itheima.activiti.entity;

import java.io.Serializable;

/**
 * 角色实体类
 */
public class SysRole implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    
    // getter and setter methods
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    public String getRoleCode() {
        return roleCode;
    }
    
    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}