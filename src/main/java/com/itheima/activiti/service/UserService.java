package com.itheima.activiti.service;

import com.itheima.activiti.entity.SysUser;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends UserDetailsService {
    
    /**
     * 根据用户名查询用户
     */
    SysUser findByUsername(String username);
    
    /**
     * 根据用户ID查询用户
     */
    SysUser findById(Long id);
    
    /**
     * 查询所有用户
     */
    List<SysUser> findAll();
    
    /**
     * 根据角色编码查询用户
     */
    List<SysUser> findByRoleCode(String roleCode);
    
    /**
     * 新增用户
     */
    boolean addUser(SysUser user);
    
    /**
     * 更新用户
     */
    boolean updateUser(SysUser user);
    
    /**
     * 删除用户
     */
    boolean deleteUser(Long id);
}