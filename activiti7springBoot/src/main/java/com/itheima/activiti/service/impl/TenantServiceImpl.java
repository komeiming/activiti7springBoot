package com.itheima.activiti.service.impl;

import com.itheima.activiti.entity.NotificationRequest;
import com.itheima.activiti.entity.NotificationResponse;
import com.itheima.activiti.entity.Tenant;
import com.itheima.activiti.mapper.TenantMapper;
import com.itheima.activiti.service.NotificationService;
import com.itheima.activiti.service.TenantInitializationService;
import com.itheima.activiti.service.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 租户服务实现类
 */
@Service
public class TenantServiceImpl implements TenantService {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantServiceImpl.class);
    
    @Autowired
    private TenantMapper tenantMapper;
    
    @Autowired
    private TenantInitializationService tenantInitializationService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 注册租户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tenant register(Tenant tenant) {
        // 生成唯一ID
        tenant.setId(UUID.randomUUID().toString());
        
        // 生成APP ID
        tenant.setAppId("APP_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        
        // 生成密钥
        tenant.setSecretKey("SK_" + UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        
        // 设置默认状态
        tenant.setStatus("pending");
        
        // 根据服务模块分配默认角色
        String serviceModules = tenant.getServiceModules();
        String role;
        if (serviceModules != null) {
            if (serviceModules.contains("notification") && serviceModules.contains("workflow")) {
                role = "ROLE_ADMIN"; // 两者均需，分配管理员角色
            } else if (serviceModules.contains("notification")) {
                role = "ROLE_NOTIFICATION_ADMIN"; // 仅通知模块，分配通知管理员角色
            } else if (serviceModules.contains("workflow")) {
                role = "ROLE_WORKFLOW_ADMIN"; // 仅工作流模块，分配工作流管理员角色
            } else {
                role = "ROLE_USER"; // 默认角色
            }
        } else {
            role = "ROLE_USER"; // 默认角色
        }
        tenant.setRole(role);
        // 保存服务模块列表
        tenant.setServiceModules(serviceModules);
        
        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        tenant.setCreatedAt(now);
        tenant.setUpdatedAt(now);
        
        // 插入数据库
        tenantMapper.insert(tenant);
        
        // 手动审核，状态为pending
        // 只有当管理员审核通过后，才会初始化租户资源
        // approveTenant(tenant.getId(), "system");
        // tenantInitializationService.initializeTenant(tenant.getId());
        
        // 发送注册成功通知
        try {
            // 直接构建通知内容，不依赖模板查询
            String subject = "注册成功通知 - " + tenant.getSystemName();
            String content = String.format("尊敬的%s，\n\n您的系统 %s 已成功注册为零售门店业务管理平台的公有服务租户。\n\n" +
                "以下是您的访问凭证，请妥善保管：\n" +
                "APP ID：%s\n" +
                "密钥：%s\n\n" +
                "您可以使用这些凭证登录租户管理后台，开始使用我们的服务。\n\n" +
                "如有任何问题，请联系我们的技术支持团队。\n\n" +
                "祝您使用愉快！\n\n" +
                "零售门店业务管理平台团队",
                tenant.getContactName(), tenant.getSystemName(), tenant.getAppId(), tenant.getSecretKey());
            
            // 准备通知参数
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("subject", subject);
            params.put("content", content);
            
            // 创建通知请求，不使用模板ID，直接发送
            NotificationRequest request = new NotificationRequest();
            request.setReceiver(tenant.getContactEmail());
            request.setType("email");
            request.setParams(params);
            request.setSenderSystem("tenant-service");
            request.setRequestId(tenant.getId());
            
            // 发送通知
            logger.info("准备发送注册成功通知: tenantId={}, contactEmail={}", tenant.getId(), tenant.getContactEmail());
            NotificationResponse response = notificationService.sendNotification(request);
            
            if (response.isSuccess()) {
                logger.info("注册成功通知发送成功: tenantId={}, sendId={}", tenant.getId(), response.getSendId());
            } else {
                logger.error("注册成功通知发送失败: tenantId={}, error={}", tenant.getId(), response.getMessage());
            }
        } catch (Exception e) {
            logger.error("发送注册成功通知时发生异常: tenantId={}", tenant.getId(), e);
            // 通知发送失败不影响注册流程
        }
        
        return tenant;
    }
    
    /**
     * 审核通过租户
     */
    @Override
    public boolean approveTenant(String tenantId, String approvedBy) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant != null) {
            tenant.setStatus("approved");
            tenant.setApprovedAt(LocalDateTime.now());
            tenant.setApprovedBy(approvedBy);
            tenant.setUpdatedAt(LocalDateTime.now());
            return tenantMapper.update(tenant) > 0;
        }
        return false;
    }
    
    /**
     * 拒绝租户申请
     */
    @Override
    public boolean rejectTenant(String tenantId, String approvedBy, String reason) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant != null) {
            tenant.setStatus("rejected");
            tenant.setApprovedAt(LocalDateTime.now());
            tenant.setApprovedBy(approvedBy);
            tenant.setUpdatedAt(LocalDateTime.now());
            if (reason != null) {
                // 使用remark字段存储拒绝原因
                String existingRemark = tenant.getRemark() != null ? tenant.getRemark() + "\n" : "";
                tenant.setRemark(existingRemark + "拒绝原因：" + reason);
            }
            return tenantMapper.update(tenant) > 0;
        }
        return false;
    }
    
    /**
     * 暂停租户服务
     */
    @Override
    public boolean suspendTenant(String tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant != null) {
            tenant.setStatus("suspended");
            tenant.setUpdatedAt(LocalDateTime.now());
            return tenantMapper.update(tenant) > 0;
        }
        return false;
    }
    
    /**
     * 恢复租户服务
     */
    @Override
    public boolean resumeTenant(String tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant != null) {
            tenant.setStatus("approved");
            tenant.setUpdatedAt(LocalDateTime.now());
            return tenantMapper.update(tenant) > 0;
        }
        return false;
    }
    
    /**
     * 注销租户
     */
    @Override
    public boolean cancelTenant(String tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant != null) {
            tenant.setStatus("cancelled");
            tenant.setUpdatedAt(LocalDateTime.now());
            return tenantMapper.update(tenant) > 0;
        }
        return false;
    }
    
    /**
     * 重置密钥
     */
    @Override
    public String resetSecretKey(String tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant != null) {
            String newSecretKey = "SK_" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
            tenant.setSecretKey(newSecretKey);
            tenant.setUpdatedAt(LocalDateTime.now());
            tenantMapper.update(tenant);
            return newSecretKey;
        }
        return null;
    }
    
    /**
     * 根据ID查询租户
     */
    @Override
    public Tenant getById(String id) {
        return tenantMapper.selectById(id);
    }
    
    /**
     * 根据APP ID查询租户
     */
    @Override
    public Tenant getByAppId(String appId) {
        return tenantMapper.selectByAppId(appId);
    }
    
    /**
     * 查询所有租户
     */
    @Override
    public List<Tenant> getAll() {
        return tenantMapper.selectAll();
    }
    
    /**
     * 根据状态查询租户
     */
    @Override
    public List<Tenant> getByStatus(String status) {
        return tenantMapper.selectByStatus(status);
    }
    
    /**
     * 更新租户信息
     */
    @Override
    public boolean update(Tenant tenant) {
        tenant.setUpdatedAt(LocalDateTime.now());
        return tenantMapper.update(tenant) > 0;
    }
    
    /**
     * 验证APP ID和密钥
     */
    @Override
    public boolean validate(String appId, String secretKey) {
        Tenant tenant = tenantMapper.selectByAppId(appId);
        return tenant != null && "approved".equals(tenant.getStatus()) && secretKey.equals(tenant.getSecretKey());
    }
    
    /**
     * 验证APP ID和密钥，并返回详细信息
     */
    @Override
    public Object[] validateWithDetails(String appId, String secretKey) {
        Tenant tenant = tenantMapper.selectByAppId(appId);
        
        if (tenant == null) {
            return new Object[]{false, "无效的APP ID"};
        }
        
        if (!secretKey.equals(tenant.getSecretKey())) {
            return new Object[]{false, "密钥错误"};
        }
        
        if ("pending".equals(tenant.getStatus())) {
            return new Object[]{false, "租户申请正在审批中，请耐心等待"};
        }
        
        if ("rejected".equals(tenant.getStatus())) {
            return new Object[]{false, "租户申请已被拒绝，请联系管理员"};
        }
        
        if ("suspended".equals(tenant.getStatus())) {
            return new Object[]{false, "租户服务已被暂停，请联系管理员"};
        }
        
        if ("cancelled".equals(tenant.getStatus())) {
            return new Object[]{false, "租户账号已被注销"};
        }
        
        if (!"approved".equals(tenant.getStatus())) {
            return new Object[]{false, "租户状态异常，请联系管理员"};
        }
        
        return new Object[]{true, "验证通过"};
    }
}