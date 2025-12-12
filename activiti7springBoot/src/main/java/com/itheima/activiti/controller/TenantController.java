package com.itheima.activiti.controller;

import com.itheima.activiti.dto.CommonResponse;
import com.itheima.activiti.entity.Tenant;
import com.itheima.activiti.service.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 租户管理控制器
 */
@RestController
@RequestMapping("/api/v1/tenant")
public class TenantController {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);
    
    @Autowired
    private TenantService tenantService;
    
    /**
     * 租户注册接口
     */
    @PostMapping("/register")
    @ResponseBody
    public Map<String, Object> registerTenant(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("租户注册请求: {}", requestBody);
            
            // 转换请求参数为Tenant对象
            Tenant tenant = new Tenant();
            tenant.setSystemName((String) requestBody.get("systemName"));
            tenant.setEnterpriseName((String) requestBody.get("enterpriseName"));
            tenant.setEnterpriseCreditCode((String) requestBody.get("enterpriseCreditCode"));
            tenant.setContactName((String) requestBody.get("contactName"));
            tenant.setContactPhone((String) requestBody.get("contactPhone"));
            tenant.setContactEmail((String) requestBody.get("contactEmail"));
            tenant.setServiceModules((String) requestBody.get("serviceModules"));
            tenant.setCallScenarios((String) requestBody.get("callScenarios"));
            
            // 调用服务层执行注册逻辑
            Tenant registeredTenant = tenantService.register(tenant);
            
            // 构建响应数据
            Map<String, Object> tenantData = new HashMap<>();
            tenantData.put("systemName", registeredTenant.getSystemName());
            tenantData.put("enterpriseName", registeredTenant.getEnterpriseName());
            tenantData.put("appId", registeredTenant.getAppId());
            tenantData.put("secretKey", registeredTenant.getSecretKey());
            tenantData.put("status", registeredTenant.getStatus());
            
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("data", tenantData);
            return response;
        } catch (Exception e) {
            logger.error("租户注册失败:", e);
            response.put("success", false);
            response.put("errorCode", "SYSTEM_ERROR");
            response.put("message", "系统内部错误：" + e.getMessage());
            // 添加异常堆栈信息
            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace.append(element.toString()).append("\n");
            }
            response.put("stackTrace", stackTrace.toString());
            return response;
        }
    }
    
    /**
     * 根据APP ID查询租户信息
     */
    @GetMapping("/info")
    public CommonResponse<Tenant> getTenantInfo(@RequestParam String appId) {
        try {
            logger.info("查询租户信息, appId: {}", appId);
            
            Tenant tenant = tenantService.getByAppId(appId);
            if (tenant == null) {
                return CommonResponse.notFound("租户不存在");
            }
            
            return CommonResponse.success(tenant);
        } catch (Exception e) {
            logger.error("查询租户信息失败:", e);
            return CommonResponse.fail("查询租户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置租户密钥
     */
    @PostMapping("/reset-secret")
    public CommonResponse<Map<String, String>> resetSecretKey(@RequestParam String appId) {
        try {
            logger.info("重置租户密钥, appId: {}", appId);
            
            Tenant tenant = tenantService.getByAppId(appId);
            if (tenant == null) {
                return CommonResponse.notFound("租户不存在");
            }
            
            String newSecretKey = tenantService.resetSecretKey(tenant.getId());
            
            Map<String, String> result = new HashMap<>();
            result.put("appId", appId);
            result.put("newSecretKey", newSecretKey);
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("重置租户密钥失败:", e);
            return CommonResponse.fail("重置租户密钥失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新租户回调地址
     */
    @PutMapping("/callback-url")
    public CommonResponse<String> updateCallbackUrl(@RequestParam String appId, @RequestParam String callbackUrl) {
        try {
            logger.info("更新租户回调地址, appId: {}, callbackUrl: {}", appId, callbackUrl);
            
            Tenant tenant = tenantService.getByAppId(appId);
            if (tenant == null) {
                return CommonResponse.notFound("租户不存在");
            }
            
            tenant.setCallbackUrl(callbackUrl);
            tenantService.update(tenant);
            
            return CommonResponse.success("回调地址更新成功");
        } catch (Exception e) {
            logger.error("更新租户回调地址失败:", e);
            return CommonResponse.fail("更新租户回调地址失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新租户基本信息
     */
    @PutMapping("/info")
    public CommonResponse<Tenant> updateTenantInfo(@RequestParam String appId, @RequestBody Tenant tenantUpdate) {
        try {
            logger.info("更新租户基本信息, appId: {}, updateData: {}", appId, tenantUpdate);
            
            // 根据appId查询租户
            Tenant tenant = tenantService.getByAppId(appId);
            if (tenant == null) {
                return CommonResponse.notFound("租户不存在");
            }
            
            // 更新基本信息
            if (tenantUpdate.getSystemName() != null && !tenantUpdate.getSystemName().trim().isEmpty()) {
                tenant.setSystemName(tenantUpdate.getSystemName().trim());
            }
            if (tenantUpdate.getContactName() != null && !tenantUpdate.getContactName().trim().isEmpty()) {
                tenant.setContactName(tenantUpdate.getContactName().trim());
            }
            if (tenantUpdate.getContactPhone() != null && !tenantUpdate.getContactPhone().trim().isEmpty()) {
                tenant.setContactPhone(tenantUpdate.getContactPhone().trim());
            }
            if (tenantUpdate.getContactEmail() != null && !tenantUpdate.getContactEmail().trim().isEmpty()) {
                tenant.setContactEmail(tenantUpdate.getContactEmail().trim());
            }
            
            // 保存更新
            tenantService.update(tenant);
            
            logger.info("租户基本信息更新成功, appId: {}", appId);
            
            return CommonResponse.success(tenant);
        } catch (Exception e) {
            logger.error("更新租户基本信息失败:", e);
            return CommonResponse.fail("更新租户基本信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 开通服务
     */
    @PostMapping("/service/enable")
    public CommonResponse<String> enableService(@RequestParam String appId, @RequestParam String serviceModule) {
        try {
            logger.info("开通服务请求, appId: {}, serviceModule: {}", appId, serviceModule);
            
            // 根据appId查询租户
            Tenant tenant = tenantService.getByAppId(appId);
            if (tenant == null) {
                return CommonResponse.notFound("租户不存在");
            }
            
            // 获取当前服务模块
            String currentModules = tenant.getServiceModules();
            String[] modules = currentModules.split(",");
            
            // 检查服务是否已经开通
            for (String module : modules) {
                if (module.trim().equals(serviceModule)) {
                    return CommonResponse.success("服务已开通");
                }
            }
            
            // 添加新服务模块
            String newModules = currentModules + "," + serviceModule;
            tenant.setServiceModules(newModules);
            
            // 保存更新
            tenantService.update(tenant);
            
            logger.info("服务开通成功, appId: {}, serviceModule: {}", appId, serviceModule);
            
            return CommonResponse.success("服务开通成功");
        } catch (Exception e) {
            logger.error("开通服务失败:", e);
            return CommonResponse.fail("开通服务失败: " + e.getMessage());
        }
    }
    
    /**
     * 暂停服务
     */
    @PostMapping("/service/disable")
    public CommonResponse<String> disableService(@RequestParam String appId, @RequestParam String serviceModule) {
        try {
            logger.info("暂停服务请求, appId: {}, serviceModule: {}", appId, serviceModule);
            
            // 根据appId查询租户
            Tenant tenant = tenantService.getByAppId(appId);
            if (tenant == null) {
                return CommonResponse.notFound("租户不存在");
            }
            
            // 获取当前服务模块
            String currentModules = tenant.getServiceModules();
            String[] modules = currentModules.split(",");
            
            // 移除指定服务模块
            StringBuilder newModules = new StringBuilder();
            boolean found = false;
            for (String module : modules) {
                if (!module.trim().equals(serviceModule)) {
                    if (newModules.length() > 0) {
                        newModules.append(",");
                    }
                    newModules.append(module.trim());
                } else {
                    found = true;
                }
            }
            
            if (!found) {
                return CommonResponse.success("服务未开通");
            }
            
            // 更新服务模块
            tenant.setServiceModules(newModules.toString());
            
            // 保存更新
            tenantService.update(tenant);
            
            logger.info("服务暂停成功, appId: {}, serviceModule: {}", appId, serviceModule);
            
            return CommonResponse.success("服务暂停成功");
        } catch (Exception e) {
            logger.error("暂停服务失败:", e);
            return CommonResponse.fail("暂停服务失败: " + e.getMessage());
        }
    }
    
    /**
     * 暂停租户所有服务
     */
    @PostMapping("/suspend")
    public CommonResponse<String> suspendTenant(@RequestParam String appId) {
        try {
            logger.info("暂停租户服务请求, appId: {}", appId);
            
            // 根据appId查询租户
            Tenant tenant = tenantService.getByAppId(appId);
            if (tenant == null) {
                return CommonResponse.notFound("租户不存在");
            }
            
            // 暂停租户服务
            boolean success = tenantService.suspendTenant(tenant.getId());
            if (success) {
                logger.info("租户服务暂停成功, appId: {}", appId);
                return CommonResponse.success("租户服务暂停成功");
            } else {
                return CommonResponse.fail("租户服务暂停失败");
            }
        } catch (Exception e) {
            logger.error("暂停租户服务失败:", e);
            return CommonResponse.fail("暂停租户服务失败: " + e.getMessage());
        }
    }
    
    /**
     * 恢复租户所有服务
     */
    @PostMapping("/resume")
    public CommonResponse<String> resumeTenant(@RequestParam String appId) {
        try {
            logger.info("恢复租户服务请求, appId: {}", appId);
            
            // 根据appId查询租户
            Tenant tenant = tenantService.getByAppId(appId);
            if (tenant == null) {
                return CommonResponse.notFound("租户不存在");
            }
            
            // 恢复租户服务
            boolean success = tenantService.resumeTenant(tenant.getId());
            if (success) {
                logger.info("租户服务恢复成功, appId: {}", appId);
                return CommonResponse.success("租户服务恢复成功");
            } else {
                return CommonResponse.fail("租户服务恢复失败");
            }
        } catch (Exception e) {
            logger.error("恢复租户服务失败:", e);
            return CommonResponse.fail("恢复租户服务失败: " + e.getMessage());
        }
    }
}