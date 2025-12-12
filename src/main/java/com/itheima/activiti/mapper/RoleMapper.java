package com.itheima.activiti.mapper;

import com.itheima.activiti.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色数据访问层
 */
@Mapper
public interface RoleMapper {
    
    /**
     * 根据角色编码查询角色
     */
    SysRole findByRoleCode(@Param("roleCode") String roleCode);
    
    /**
     * 根据角色ID查询角色
     */
    SysRole findById(@Param("id") Long id);
    
    /**
     * 查询所有角色
     */
    List<SysRole> findAll();
    
    /**
     * 根据用户ID查询用户拥有的角色
     */
    List<SysRole> findByUserId(@Param("userId") Long userId);
    
    /**
     * 新增角色
     */
    int insert(SysRole role);
    
    /**
     * 更新角色
     */
    int update(SysRole role);
    
    /**
     * 删除角色
     */
    int delete(@Param("id") Long id);
}