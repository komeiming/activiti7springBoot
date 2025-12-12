package com.itheima.activiti.controller;

import com.itheima.activiti.dto.CommonResponse;
import com.itheima.activiti.entity.SysUser;
import com.itheima.activiti.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Map<String, Object> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> userInfo = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                userInfo.put("username", userDetails.getUsername());
                userInfo.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
                userInfo.put("authenticated", true);
            } else {
                userInfo.put("username", principal.toString());
                userInfo.put("authenticated", true);
            }
        } else {
            userInfo.put("authenticated", false);
        }
        
        return userInfo;
    }

    /**
     * 查询所有用户
     */
    @GetMapping
    public CommonResponse<List<SysUser>> findAll() {
        try {
            List<SysUser> users = userService.findAll();
            return CommonResponse.success(users);
        } catch (Exception e) {
            return CommonResponse.fail("查询用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public CommonResponse<SysUser> findById(@PathVariable Long id) {
        try {
            SysUser user = userService.findById(id);
            if (user == null) {
                return CommonResponse.notFound("用户不存在");
            }
            return CommonResponse.success(user);
        } catch (Exception e) {
            return CommonResponse.fail("查询用户失败: " + e.getMessage());
        }
    }

    /**
     * 新增用户
     */
    @PostMapping
    public CommonResponse<SysUser> addUser(@RequestBody SysUser user) {
        logger.info("接收到添加用户请求: username={}", user.getUsername());
        try {
            boolean success = userService.addUser(user);
            if (success) {
                // 查找刚刚创建的用户，以便返回完整信息（包括ID）
                SysUser createdUser = userService.findByUsername(user.getUsername());
                logger.info("用户创建成功，ID: {}", createdUser.getId());
                return CommonResponse.success(createdUser);
            } else {
                logger.error("用户创建失败，服务返回false");
                return CommonResponse.fail("用户创建失败");
            }
        } catch (Exception e) {
            logger.error("创建用户异常", e);
            return CommonResponse.fail("创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public CommonResponse<String> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        logger.info("接收到更新用户请求: id={}, username={}, fullName={}", id, user.getUsername(), user.getFullName());
        try {
            // 确保更新的是指定ID的用户
            user.setId(id);
            logger.info("准备调用userService.updateUser，用户数据: {}", user);
            boolean success = userService.updateUser(user);
            if (success) {
                logger.info("用户更新成功: id={}", id);
                return CommonResponse.success("用户更新成功");
            } else {
                logger.error("用户更新失败: id={}", id);
                return CommonResponse.fail("用户更新失败");
            }
        } catch (Exception e) {
            logger.error("更新用户异常: id={}", id, e);
            return CommonResponse.fail("更新用户失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public CommonResponse<String> deleteUser(@PathVariable Long id) {
        logger.info("接收到删除用户请求: id={}", id);
        try {
            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    String currentUsername = userDetails.getUsername();
                    
                    // 检查是否为admin用户
                    if ("admin".equals(currentUsername)) {
                        // 查询要删除的用户信息
                        SysUser userToDelete = userService.findById(id);
                        if (userToDelete != null && "admin".equals(userToDelete.getUsername())) {
                            logger.warn("admin用户尝试删除自己，操作被拒绝");
                            return CommonResponse.fail("不能删除当前登录的管理员账户");
                        }
                    }
                }
            }
            
            logger.info("准备调用userService.deleteUser");
            boolean success = userService.deleteUser(id);
            if (success) {
                logger.info("用户删除成功: id={}", id);
                return CommonResponse.success("用户删除成功");
            } else {
                logger.error("用户删除失败: id={}", id);
                return CommonResponse.fail("用户删除失败");
            }
        } catch (Exception e) {
            logger.error("删除用户异常: id={}", id, e);
            return CommonResponse.fail("删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 根据角色查询用户
     */
    @GetMapping("/by-role/{roleCode}")
    public CommonResponse<List<SysUser>> findByRoleCode(@PathVariable String roleCode) {
        try {
            List<SysUser> users = userService.findByRoleCode(roleCode);
            return CommonResponse.success(users);
        } catch (Exception e) {
            return CommonResponse.fail("查询用户失败: " + e.getMessage());
        }
    }
}