package com.itheima.activiti.mapper;

import com.itheima.activiti.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户数据访问层
 */
@Mapper
@Repository
public interface UserMapper {
    
    /**
     * 根据用户名查询用户
     */
    SysUser findByUsername(@Param("username") String username);
    
    /**
     * 根据用户ID查询用户
     */
    SysUser findById(@Param("id") Long id);
    
    /**
     * 查询所有用户
     */
    List<SysUser> findAll();
    
    /**
     * 根据角色编码查询用户列表
     */
    List<SysUser> findByRoleCode(@Param("roleCode") String roleCode);
    
    /**
     * 新增用户
     */
    int insert(SysUser user);
    
    /**
     * 更新用户
     */
    int update(SysUser user);
    
    /**
     * 删除用户
     */
    int delete(@Param("id") Long id);
}