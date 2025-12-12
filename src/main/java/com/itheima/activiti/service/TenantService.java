package com.itheima.activiti.service;

import com.itheima.activiti.entity.Tenant;

import java.util.List;

/**
 * 租户服务接口
 */
public interface TenantService {
    
    /**
     * 注册租户
     */
    Tenant register(Tenant tenant);
    
    /**
     * 审核通过租户
     */
    boolean approveTenant(String tenantId, String approvedBy);
    
    /**
     * 拒绝租户申请
     */
    boolean rejectTenant(String tenantId, String approvedBy, String reason);
    
    /**
     * 暂停租户服务
     */
    boolean suspendTenant(String tenantId);
    
    /**
     * 恢复租户服务
     */
    boolean resumeTenant(String tenantId);
    
    /**
     * 注销租户
     */
    boolean cancelTenant(String tenantId);
    
    /**
     * 重置密钥
     */
    String resetSecretKey(String tenantId);
    
    /**
     * 根据ID查询租户
     */
    Tenant getById(String id);
    
    /**
     * 根据APP ID查询租户
     */
    Tenant getByAppId(String appId);
    
    /**
     * 查询所有租户
     */
    List<Tenant> getAll();
    
    /**
     * 根据状态查询租户
     */
    List<Tenant> getByStatus(String status);
    
    /**
     * 更新租户信息
     */
    boolean update(Tenant tenant);
    
    /**
     * 验证APP ID和密钥
     */
    boolean validate(String appId, String secretKey);
    
    /**
     * 验证APP ID和密钥，并返回详细信息
     * @return 返回数组，第一个元素是boolean表示验证结果，第二个元素是String表示错误信息
     */
    Object[] validateWithDetails(String appId, String secretKey);
}