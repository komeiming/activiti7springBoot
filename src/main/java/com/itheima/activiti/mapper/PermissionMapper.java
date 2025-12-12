package com.itheima.activiti.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper {
    
    /**
     * 根据用户ID查询权限编码列表
     */
    @Select("SELECT p.code FROM sys_permission p " +
            "JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> findByUserId(Long userId);
    
    /**
     * 查询所有权限
     */
    @Select("SELECT * FROM sys_permission")
    List<com.itheima.activiti.entity.Permission> findAll();
}