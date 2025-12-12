package com.itheima.activiti.service.impl;

import com.itheima.activiti.entity.SysRole;
import com.itheima.activiti.entity.SysUser;
import com.itheima.activiti.mapper.UserMapper;
import com.itheima.activiti.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务接口实现类
 */
@Service
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public SysUser findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    @Override
    public SysUser findById(Long id) {
        return userMapper.findById(id);
    }
    
    @Override
    public List<SysUser> findAll() {
        return userMapper.findAll();
    }
    
    @Override
    public List<SysUser> findByRoleCode(String roleCode) {
        return userMapper.findByRoleCode(roleCode);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(SysUser user) {
        logger.info("执行addUser方法: username={}", user.getUsername());
        try {
            // 显式设置ID为null，确保使用数据库自动递增生成的ID，避免主键冲突
            if (user.getId() != null) {
                logger.warn("新增用户时不应包含ID，将重置为null: username={}, 原始ID={}", 
                           user.getUsername(), user.getId());
                user.setId(null);
            }
            
            boolean result = userMapper.insert(user) > 0;
            logger.info("userMapper.insert执行结果: {}, 用户ID: {}", result, user.getId());
            
            // 如果用户对象中包含角色信息且不为空，则插入新的角色关联
            if (result && user.getRoles() != null && !user.getRoles().isEmpty()) {
                logger.info("用户包含角色信息，数量: {}", user.getRoles().size());
                for (SysRole role : user.getRoles()) {
                    int insertCount = jdbcTemplate.update(
                        "INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", 
                        user.getId(), role.getId()
                    );
                    logger.info("插入用户角色关联结果: {}", insertCount);
                }
            } else if (user.getRoles() == null || user.getRoles().isEmpty()) {
                logger.info("用户没有角色信息或角色列表为空");
            }
            
            return result;
        } catch (Exception e) {
            logger.error("添加用户失败: username={}", user.getUsername(), e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        logger.info("执行updateUser方法: user.id={}, username={}, fullName={}", user.getId(), user.getUsername(), user.getFullName());
        try {
            boolean result = userMapper.update(user) > 0;
            logger.info("userMapper.update执行结果: {}", result);
            
            if (result) {
                // 总是删除原有的角色关联
                int deleteCount = jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", user.getId());
                logger.info("删除用户角色关联数量: {}", deleteCount);
                
                // 如果用户对象中包含角色信息且不为空，则插入新的角色关联
                if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                    logger.info("用户包含角色信息，数量: {}", user.getRoles().size());
                    for (SysRole role : user.getRoles()) {
                        int insertCount = jdbcTemplate.update(
                            "INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", 
                            user.getId(), role.getId()
                        );
                        logger.info("插入用户角色关联结果: {}", insertCount);
                    }
                } else {
                    logger.info("用户没有角色信息或角色列表为空");
                }
            }
            
            return result;
        } catch (Exception e) {
            logger.error("更新用户失败: user.id={}", user.getId(), e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        logger.info("执行deleteUser方法: id={}", id);
        try {
            // 先删除用户角色关联
            int deleteRoleCount = jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", id);
            logger.info("删除用户角色关联数量: {}", deleteRoleCount);
            
            // 再删除用户
            int deleteUserCount = userMapper.delete(id);
            logger.info("删除用户数量: {}", deleteUserCount);
            
            return deleteUserCount > 0;
        } catch (Exception e) {
            logger.error("删除用户失败: id={}", id, e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库中查询用户
        SysUser sysUser = findByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 转换用户角色为Spring Security需要的权限格式
        // 注意：数据库中的角色代码已经包含了"ROLE_"前缀
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (sysUser.getRoles() != null) {
            sysUser.getRoles().forEach(role -> {
                // 直接使用角色代码，不再添加"ROLE_"前缀
                authorities.add(new SimpleGrantedAuthority(role.getRoleCode()));
            });
        }
        
        // 返回Spring Security的User对象
        return new org.springframework.security.core.userdetails.User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                sysUser.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }
}