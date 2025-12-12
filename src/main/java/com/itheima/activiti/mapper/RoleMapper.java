package com.itheima.activiti.mapper;

import com.itheima.activiti.entity.SysRole;
import org.apache.ibatis.annotations.*;

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
    
    /**
     * 根据角色ID查询拥有的菜单权限ID列表
     */
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> findMenuIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 删除角色的所有菜单权限
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteRoleMenus(@Param("roleId") Long roleId);
    
    /**
     * 批量添加角色菜单权限
     */
    @Insert("<script>" +
            "INSERT INTO sys_role_permission (role_id, permission_id) VALUES " +
            "<foreach collection=\"menuIds\" item=\"menuId\" separator=\",\">" +
            "(#{roleId}, #{menuId})" +
            "</foreach>" +
            "</script>")
    int insertRoleMenus(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}