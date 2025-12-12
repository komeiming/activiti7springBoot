package com.itheima.activiti.controller;

import com.itheima.activiti.dto.CommonResponse;
import com.itheima.activiti.entity.Tenant;
import com.itheima.activiti.service.TenantService;
import com.itheima.activiti.service.TenantInitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租户管理控制器（管理员使用）
 */
@RestController
@RequestMapping("/api/admin/tenant")
public class TenantAdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantAdminController.class);
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private TenantInitializationService tenantInitializationService;
    
    /**
     * 获取所有租户列表
     */
    @GetMapping("/list")
    public CommonResponse<List<Tenant>> getAllTenants() {
        try {
            List<Tenant> tenants = tenantService.getAll();
            return CommonResponse.success(tenants);
        } catch (Exception e) {
            logger.error("获取租户列表失败:", e);
            return CommonResponse.fail("获取租户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据状态获取租户列表
     */
    @GetMapping("/list-by-status")
    public CommonResponse<List<Tenant>> getTenantsByStatus(@RequestParam String status) {
        try {
            List<Tenant> tenants = tenantService.getByStatus(status);
            return CommonResponse.success(tenants);
        } catch (Exception e) {
            logger.error("根据状态获取租户列表失败:", e);
            return CommonResponse.fail("根据状态获取租户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取待审批租户列表
     */
    @GetMapping("/pending-list")
    public CommonResponse<List<Tenant>> getPendingTenants() {
        try {
            List<Tenant> tenants = tenantService.getByStatus("pending");
            return CommonResponse.success(tenants);
        } catch (Exception e) {
            logger.error("获取待审批租户列表失败:", e);
            return CommonResponse.fail("获取待审批租户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取租户详情
     */
    @GetMapping("/detail")
    public CommonResponse<Tenant> getTenantDetail(@RequestParam String tenantId) {
        try {
            Tenant tenant = tenantService.getById(tenantId);
            if (tenant == null) {
                return CommonResponse.notFound("租户不存在");
            }
            return CommonResponse.success(tenant);
        } catch (Exception e) {
            logger.error("获取租户详情失败:", e);
            return CommonResponse.fail("获取租户详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 审核通过租户
     */
    @PostMapping("/approve")
    public CommonResponse<Boolean> approveTenant(@RequestBody Map<String, Object> requestBody) {
        try {
            String tenantId = (String) requestBody.get("tenantId");
            String approvedBy = (String) requestBody.get("approvedBy");
            
            logger.info("审核通过租户, tenantId: {}, approvedBy: {}", tenantId, approvedBy);
            
            // 审核租户
            boolean success = tenantService.approveTenant(tenantId, approvedBy);
            if (success) {
                // 初始化租户资源
                tenantInitializationService.initializeTenant(tenantId);
                logger.info("租户审核通过并初始化资源成功, tenantId: {}", tenantId);
            }
            
            return CommonResponse.success(success);
        } catch (Exception e) {
            logger.error("审核通过租户失败:", e);
            return CommonResponse.fail("审核通过租户失败: " + e.getMessage());
        }
    }
    
    /**
     * 拒绝租户申请
     */
    @PostMapping("/reject")
    public CommonResponse<Boolean> rejectTenant(@RequestBody Map<String, Object> requestBody) {
        try {
            String tenantId = (String) requestBody.get("tenantId");
            String approvedBy = (String) requestBody.get("approvedBy");
            String reason = (String) requestBody.get("reason");
            
            logger.info("拒绝租户申请, tenantId: {}, approvedBy: {}, reason: {}", tenantId, approvedBy, reason);
            
            boolean success = tenantService.rejectTenant(tenantId, approvedBy, reason);
            return CommonResponse.success(success);
        } catch (Exception e) {
            logger.error("拒绝租户申请失败:", e);
            return CommonResponse.fail("拒绝租户申请失败: " + e.getMessage());
        }
    }
    
    /**
     * 暂停租户服务
     */
    @PostMapping("/suspend")
    public CommonResponse<Boolean> suspendTenant(@RequestParam String tenantId) {
        try {
            logger.info("暂停租户服务, tenantId: {}", tenantId);
            
            boolean success = tenantService.suspendTenant(tenantId);
            return CommonResponse.success(success);
        } catch (Exception e) {
            logger.error("暂停租户服务失败:", e);
            return CommonResponse.fail("暂停租户服务失败: " + e.getMessage());
        }
    }
    
    /**
     * 恢复租户服务
     */
    @PostMapping("/resume")
    public CommonResponse<Boolean> resumeTenant(@RequestParam String tenantId) {
        try {
            logger.info("恢复租户服务, tenantId: {}", tenantId);
            
            boolean success = tenantService.resumeTenant(tenantId);
            return CommonResponse.success(success);
        } catch (Exception e) {
            logger.error("恢复租户服务失败:", e);
            return CommonResponse.fail("恢复租户服务失败: " + e.getMessage());
        }
    }
}