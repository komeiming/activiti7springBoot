package com.itheima.activiti.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuOperationLogMapper {
    
    /**
     * 插入菜单操作日志
     */
    @Insert("INSERT INTO sys_menu_operation_log (menu_id, operation_type, operator, operation_time, old_data, new_data, description, ip_address, user_agent) " +
            "VALUES (#{menuId}, #{operationType}, #{operator}, #{operationTime}, #{oldData}, #{newData}, #{description}, #{ipAddress}, #{userAgent})")
    void insert(com.itheima.activiti.entity.MenuOperationLog log);
    
    /**
     * 查询所有菜单操作日志
     */
    @Select("SELECT * FROM sys_menu_operation_log ORDER BY operation_time DESC")
    List<com.itheima.activiti.entity.MenuOperationLog> findAll();
    
    /**
     * 根据菜单ID查询操作日志
     */
    @Select("SELECT * FROM sys_menu_operation_log WHERE menu_id = #{menuId} ORDER BY operation_time DESC")
    List<com.itheima.activiti.entity.MenuOperationLog> findByMenuId(Long menuId);
    
    /**
     * 根据操作类型查询操作日志
     */
    @Select("SELECT * FROM sys_menu_operation_log WHERE operation_type = #{operationType} ORDER BY operation_time DESC")
    List<com.itheima.activiti.entity.MenuOperationLog> findByOperationType(String operationType);
    
    /**
     * 根据操作人查询操作日志
     */
    @Select("SELECT * FROM sys_menu_operation_log WHERE operator = #{operator} ORDER BY operation_time DESC")
    List<com.itheima.activiti.entity.MenuOperationLog> findByOperator(String operator);
}
