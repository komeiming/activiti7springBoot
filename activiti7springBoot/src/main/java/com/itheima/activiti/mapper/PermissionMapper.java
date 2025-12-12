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
    List<String> findPermissionCodesByUserId(Long userId);
    
    /**
     * 根据用户ID查询完整权限列表，包括菜单信息
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<com.itheima.activiti.entity.Permission> findPermissionsByUserId(Long userId);
    
    /**
     * 查询所有权限
     */
    @Select("SELECT * FROM sys_permission")
    List<com.itheima.activiti.entity.Permission> findAll();
    
    /**
     * 根据ID查询权限
     */
    @Select("SELECT * FROM sys_permission WHERE id = #{id}")
    com.itheima.activiti.entity.Permission findById(Long id);
    
    /**
     * 插入权限
     */
    @org.apache.ibatis.annotations.Insert("INSERT INTO sys_permission (name, code, url, method, description, parent_id, menu_type, menu_order, icon, path, component, redirect, hidden, always_show, affix, created_time, updated_time) " +
            "VALUES (#{name}, #{code}, #{url}, #{method}, #{description}, #{parentId}, #{menuType}, #{menuOrder}, #{icon}, #{path}, #{component}, #{redirect}, #{hidden}, #{alwaysShow}, #{affix}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(com.itheima.activiti.entity.Permission permission);
    
    /**
     * 更新权限
     */
    @org.apache.ibatis.annotations.Update("UPDATE sys_permission SET name = #{name}, code = #{code}, url = #{url}, method = #{method}, description = #{description}, " +
            "parent_id = #{parentId}, menu_type = #{menuType}, menu_order = #{menuOrder}, icon = #{icon}, path = #{path}, component = #{component}, " +
            "redirect = #{redirect}, hidden = #{hidden}, always_show = #{alwaysShow}, affix = #{affix}, updated_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    int update(com.itheima.activiti.entity.Permission permission);
    
    /**
     * 删除权限
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM sys_permission WHERE id = #{id}")
    int delete(Long id);
    
    /**
     * 批量删除权限
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM sys_permission WHERE id IN (#{ids})")
    int batchDelete(List<Long> ids);
}