package com.itheima.activiti.controller;

import com.itheima.activiti.dto.CommonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {

    private static final Logger logger = LoggerFactory.getLogger(UserRoleController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 给用户分配角色
     */
    @PostMapping("/assign")
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse<String> assignRoles(@RequestBody Map<String, Object> request) {
        logger.info("接收到角色分配请求: {}", request);
        try {
            // 参数验证
            if (request == null) {
                logger.error("请求参数不能为空");
                return CommonResponse.fail("请求参数不能为空");
            }
            
            // 检查并转换userId
            Object userIdObj = request.get("userId");
            if (userIdObj == null) {
                logger.error("用户ID不能为空");
                return CommonResponse.fail("用户ID不能为空");
            }
            
            Long userId;
            try {
                if (userIdObj instanceof Number) {
                    userId = ((Number) userIdObj).longValue();
                } else {
                    userId = Long.valueOf(userIdObj.toString());
                }
                logger.info("用户ID: {}", userId);
            } catch (NumberFormatException e) {
                logger.error("用户ID格式不正确: {}", userIdObj);
                return CommonResponse.fail("用户ID格式不正确");
            }
            
            // 检查并处理roleIds
            Object roleIdsObj = request.get("roleIds");
            List<Long> roleIds = null;
            
            if (roleIdsObj != null) {
                if (roleIdsObj instanceof List) {
                    roleIds = new ArrayList<>();
                    for (Object obj : (List<?>) roleIdsObj) {
                        if (obj instanceof Number) {
                            roleIds.add(((Number) obj).longValue());
                        } else if (obj != null) {
                            try {
                                roleIds.add(Long.valueOf(obj.toString()));
                            } catch (NumberFormatException e) {
                                // 忽略格式不正确的roleId
                                logger.warn("忽略格式不正确的roleId: {}", obj);
                            }
                        }
                    }
                    logger.info("角色ID列表: {}", roleIds);
                } else {
                    logger.warn("roleIds格式不正确，不是List类型: {}", roleIdsObj);
                }
            } else {
                logger.info("角色ID列表为null");
            }
            
            // 先删除用户原有的角色关联
            logger.info("删除用户原有角色关联");
            jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
            
            // 插入新的角色关联
            if (roleIds != null && !roleIds.isEmpty()) {
                logger.info("插入新的角色关联，共{}个角色", roleIds.size());
                for (Long roleId : roleIds) {
                    jdbcTemplate.update(
                        "INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)", 
                        userId, roleId
                    );
                    logger.debug("已分配角色: {} 给用户: {}", roleId, userId);
                }
            } else {
                logger.info("没有分配新角色给用户: {}", userId);
            }
            
            logger.info("角色分配成功");
            return CommonResponse.success("角色分配成功");
        } catch (Exception e) {
            logger.error("角色分配请求处理异常", e);
            // 抛出异常以便Spring事务管理器能够捕获并回滚事务
            throw new RuntimeException("角色分配失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取用户已分配的角色ID列表
     */
    @GetMapping("/user/{userId}")
    public CommonResponse<List<Long>> getUserRoleIds(@PathVariable Long userId) {
        try {
            String sql = "SELECT role_id FROM sys_user_role WHERE user_id = ?";
            List<Long> roleIds = jdbcTemplate.queryForList(sql, Long.class, userId);
            return CommonResponse.success(roleIds);
        } catch (Exception e) {
            return CommonResponse.fail("获取用户角色失败: " + e.getMessage());
        }
    }

    /**
     * 解除用户的角色分配
     */
    @DeleteMapping("/user/{userId}/role/{roleId}")
    public CommonResponse<String> removeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        try {
            int result = jdbcTemplate.update(
                "DELETE FROM sys_user_role WHERE user_id = ? AND role_id = ?", 
                userId, roleId
            );
            if (result > 0) {
                return CommonResponse.success("角色解除成功");
            } else {
                return CommonResponse.fail("角色解除失败或不存在该关联");
            }
        } catch (Exception e) {
            return CommonResponse.fail("解除角色失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户角色关联统计信息
     */
    @GetMapping("/statistics")
    public CommonResponse<Map<String, Object>> getStatistics() {
        try {
            String userCountSql = "SELECT COUNT(*) FROM sys_user";
            String roleCountSql = "SELECT COUNT(*) FROM sys_role";
            String relationCountSql = "SELECT COUNT(*) FROM sys_user_role";
            
            int userCount = jdbcTemplate.queryForObject(userCountSql, Integer.class);
            int roleCount = jdbcTemplate.queryForObject(roleCountSql, Integer.class);
            int relationCount = jdbcTemplate.queryForObject(relationCountSql, Integer.class);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("userCount", userCount);
            statistics.put("roleCount", roleCount);
            statistics.put("relationCount", relationCount);
            
            return CommonResponse.success(statistics);
        } catch (Exception e) {
            return CommonResponse.fail("获取统计信息失败: " + e.getMessage());
        }
    }
}