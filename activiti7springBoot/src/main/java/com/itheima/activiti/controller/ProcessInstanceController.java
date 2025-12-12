package com.itheima.activiti.controller;

import com.itheima.activiti.dto.CommonResponse;
import com.itheima.activiti.service.ProcessDefinitionService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程实例管理控制器
 */
@RestController
@RequestMapping("/api/v1/process-instances")
public class ProcessInstanceController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceController.class);

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * 启动流程实例
     */
    @PostMapping("/start")
    public CommonResponse<Map<String, Object>> startProcessInstance(
            @RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("启动流程实例请求参数: {}", requestBody);

            // 参数验证
            String processDefinitionKey = (String) requestBody.get("processDefinitionKey");
            if (processDefinitionKey == null || processDefinitionKey.trim().isEmpty()) {
                return CommonResponse.fail("流程定义key不能为空");
            }

            // 获取业务key
            String businessKey = (String) requestBody.get("businessKey");

            // 获取流程变量
            Map<String, Object> variables = new HashMap<>();
            if (requestBody.containsKey("variables") && requestBody.get("variables") instanceof Map) {
                variables.putAll((Map<String, Object>) requestBody.get("variables"));
            } else {
                // 排除不是variables的字段
                for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
                    if (!"processDefinitionKey".equals(entry.getKey()) && !"businessKey".equals(entry.getKey())) {
                        variables.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication.getName();
            logger.info("当前登录用户: {}", currentUser);
            
            // 将当前用户信息添加到流程变量中
            variables.put("initiatorId", currentUser);
            variables.put("applicant", currentUser);
            variables.put("startUserId", currentUser);

            logger.info("准备启动流程实例，processDefinitionKey: {}, businessKey: {}, variables: {}", 
                        processDefinitionKey, businessKey, variables);

            // 启动流程实例
            String processInstanceId = processDefinitionService.startProcessInstance(
                    processDefinitionKey, businessKey, variables);

            logger.info("流程实例启动成功，processInstanceId: {}", processInstanceId);

            // 获取流程实例信息
            ProcessInstance processInstance = processDefinitionService.getProcessInstanceById(processInstanceId);

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("processInstanceId", processInstanceId);
            result.put("processDefinitionId", processInstance.getProcessDefinitionId());
            result.put("businessKey", processInstance.getBusinessKey());
            result.put("isActive", !processInstance.isSuspended());

            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("启动流程实例失败:", e);
            return CommonResponse.fail("启动流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 获取流程实例详情
     */
    @GetMapping("/{processInstanceId}")
    public CommonResponse<Map<String, Object>> getProcessInstance(
            @PathVariable String processInstanceId) {
        try {
            logger.info("获取流程实例详情，processInstanceId: {}", processInstanceId);
            
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()) || "admin".equals(currentUser));
            // 添加部门经理角色检查 - 检查所有经理角色
            boolean isManager = authentication.getAuthorities().stream()
                    .anyMatch(auth -> {
                        String authority = auth.getAuthority();
                        return authority.equals("ROLE_MANAGER") || 
                               authority.equals("ROLE_DEV_MANAGER") ||
                               authority.equals("ROLE_TEST_MANAGER") ||
                               authority.equals("ROLE_OPS_MANAGER") ||
                               authority.equals("ROLE_SALES_MANAGER") ||
                               authority.equals("ROLE_MARKET_MANAGER") ||
                               authority.equals("ROLE_HR_MANAGER") ||
                               authority.equals("ROLE_FINANCE_MANAGER");
                    });

            ProcessInstance processInstance = processDefinitionService.getProcessInstanceById(processInstanceId);
            HistoricProcessInstance historicProcessInstance = null;
            
            // 管理员和部门经理可以查看所有流程实例
            boolean hasPermission = isAdmin || isManager;
            
            if (processInstance == null) {
                // 尝试从历史流程实例中查询
                historicProcessInstance = processDefinitionService.getHistoricProcessInstanceById(processInstanceId);
                if (historicProcessInstance == null) {
                    return CommonResponse.notFound("流程实例不存在");
                }
                
                // 检查权限：如果不是管理员和部门经理，只能查看自己发起或参与的流程
                if (!isAdmin && !isManager) {
                    // 1. 检查是否是流程发起人
                    if (currentUser.equals(historicProcessInstance.getStartUserId())) {
                        hasPermission = true;
                    } else {
                        // 2. 检查是否是流程参与者
                        List<Map<String, Object>> historyActivities = processDefinitionService.getProcessInstanceHistory(processInstanceId);
                        hasPermission = historyActivities.stream()
                                .anyMatch(activity -> currentUser.equals(activity.get("assignee")));
                    }
                }
            } else {
                // 检查权限：如果不是管理员和部门经理，只能查看自己发起或参与的流程
                if (!isAdmin && !isManager) {
                    // 1. 检查是否是流程发起人
                    if (currentUser.equals(processInstance.getStartUserId())) {
                        hasPermission = true;
                    } else {
                        // 2. 检查是否是流程参与者
                        List<Map<String, Object>> historyActivities = processDefinitionService.getProcessInstanceHistory(processInstanceId);
                        hasPermission = historyActivities.stream()
                                .anyMatch(activity -> currentUser.equals(activity.get("assignee")));
                    }
                }
            }
            
            // 权限检查失败
            if (!hasPermission) {
                return CommonResponse.forbidden("没有权限查看该流程实例");
            }

            if (historicProcessInstance != null) {
                // 构建历史流程实例信息
                Map<String, Object> result = new HashMap<>();
                result.put("id", historicProcessInstance.getId());
                result.put("processDefinitionId", historicProcessInstance.getProcessDefinitionId());
                result.put("processDefinitionKey", historicProcessInstance.getProcessDefinitionKey());
                result.put("processDefinitionName", historicProcessInstance.getProcessDefinitionName());
                result.put("businessKey", historicProcessInstance.getBusinessKey());
                result.put("startTime", historicProcessInstance.getStartTime());
                result.put("endTime", historicProcessInstance.getEndTime());
                result.put("durationInMillis", historicProcessInstance.getDurationInMillis());
                result.put("startUserId", historicProcessInstance.getStartUserId());
                result.put("deleteReason", historicProcessInstance.getDeleteReason());
                result.put("isActive", false);

                return CommonResponse.success(result);
            }

            // 构建活跃流程实例信息
            Map<String, Object> result = new HashMap<>();
            result.put("id", processInstance.getId());
            result.put("processDefinitionId", processInstance.getProcessDefinitionId());
            result.put("businessKey", processInstance.getBusinessKey());
            result.put("isSuspended", processInstance.isSuspended());
            result.put("isActive", !processInstance.isSuspended());

            // 获取流程变量
            Map<String, Object> variables = processDefinitionService.getProcessInstanceVariables(processInstanceId);
            result.put("variables", variables);

            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("获取流程实例详情失败:", e);
            return CommonResponse.fail("获取流程实例详情失败: " + e.getMessage());
        }
    }

    /**
     * 挂起流程实例
     */
    @PutMapping("/{processInstanceId}/suspend")
    public CommonResponse<String> suspendProcessInstance(
            @PathVariable String processInstanceId) {
        try {
            logger.info("挂起流程实例，processInstanceId: {}", processInstanceId);

            boolean success = processDefinitionService.suspendProcessInstance(processInstanceId);
            if (success) {
                return CommonResponse.success("流程实例已挂起");
            } else {
                return CommonResponse.fail("流程实例挂起失败");
            }
        } catch (Exception e) {
            logger.error("挂起流程实例失败:", e);
            return CommonResponse.fail("挂起流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 激活流程实例
     */
    @PutMapping("/{processInstanceId}/activate")
    public CommonResponse<String> activateProcessInstance(
            @PathVariable String processInstanceId) {
        try {
            logger.info("激活流程实例，processInstanceId: {}", processInstanceId);

            boolean success = processDefinitionService.activateProcessInstance(processInstanceId);
            if (success) {
                return CommonResponse.success("流程实例已激活");
            } else {
                return CommonResponse.fail("流程实例激活失败");
            }
        } catch (Exception e) {
            logger.error("激活流程实例失败:", e);
            return CommonResponse.fail("激活流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 终止流程实例
     */
    @DeleteMapping("/{processInstanceId}")
    public CommonResponse<String> deleteProcessInstance(
            @PathVariable String processInstanceId,
            @RequestParam(required = false) String deleteReason) {
        try {
            logger.info("终止流程实例，processInstanceId: {}, deleteReason: {}", 
                        processInstanceId, deleteReason);

            boolean success = processDefinitionService.deleteProcessInstance(processInstanceId, deleteReason);
            if (success) {
                return CommonResponse.success("流程实例已终止");
            } else {
                return CommonResponse.fail("流程实例终止失败");
            }
        } catch (Exception e) {
            logger.error("终止流程实例失败:", e);
            return CommonResponse.fail("终止流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取活跃的流程实例列表
     */
    @GetMapping("/active")
    public CommonResponse<Map<String, Object>> getActiveProcessInstances(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("获取用户活跃流程实例，userId: {}, page: {}, size: {}", userId, page, size);

            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication.getName();
            // 如果userId为空，使用当前登录用户的用户名作为默认值
            if (userId == null || userId.trim().isEmpty()) {
                userId = currentUser;
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()) || "admin".equals(currentUser));
            boolean isManagerOrHR = authentication.getAuthorities().stream()
                    .anyMatch(auth -> {
                        String authority = auth.getAuthority();
                        return authority.equals("ROLE_MANAGER") || 
                               authority.equals("ROLE_DEV_MANAGER") ||
                               authority.equals("ROLE_TEST_MANAGER") ||
                               authority.equals("ROLE_OPS_MANAGER") ||
                               authority.equals("ROLE_SALES_MANAGER") ||
                               authority.equals("ROLE_MARKET_MANAGER") ||
                               authority.equals("ROLE_HR_MANAGER") ||
                               authority.equals("ROLE_FINANCE_MANAGER") ||
                               authority.equals("ROLE_HR");
                    });

            Map<String, Object> result = processDefinitionService.getActiveProcessInstancesByUserId(userId, page, size);
            
            // 如果不是管理员或部门经理/HR，只返回当前用户发起的流程实例
            if (!isAdmin && !isManagerOrHR) {
                List<Map<String, Object>> filteredData = new ArrayList<>();
                if (result.get("data") instanceof List) {
                    for (Map<String, Object> instance : (List<Map<String, Object>>) result.get("data")) {
                        if (userId.equals(instance.get("startUserId"))) {
                            filteredData.add(instance);
                        }
                    }
                }
                result.put("data", filteredData);
                result.put("total", filteredData.size());
            }
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("获取活跃流程实例失败:", e);
            return CommonResponse.fail("获取活跃流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取已完成的流程实例列表
     */
    @GetMapping("/completed")
    public CommonResponse<Map<String, Object>> getCompletedProcessInstances(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("获取用户已完成流程实例，userId: {}, page: {}, size: {}", userId, page, size);

            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication.getName();
            // 如果userId为空，使用当前登录用户的用户名作为默认值
            if (userId == null || userId.trim().isEmpty()) {
                userId = currentUser;
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()) || "admin".equals(currentUser));
            boolean isManagerOrHR = authentication.getAuthorities().stream()
                    .anyMatch(auth -> {
                        String authority = auth.getAuthority();
                        return authority.equals("ROLE_MANAGER") || 
                               authority.equals("ROLE_DEV_MANAGER") ||
                               authority.equals("ROLE_TEST_MANAGER") ||
                               authority.equals("ROLE_OPS_MANAGER") ||
                               authority.equals("ROLE_SALES_MANAGER") ||
                               authority.equals("ROLE_MARKET_MANAGER") ||
                               authority.equals("ROLE_HR_MANAGER") ||
                               authority.equals("ROLE_FINANCE_MANAGER") ||
                               authority.equals("ROLE_HR");
                    });

            Map<String, Object> result = processDefinitionService.getCompletedProcessInstancesByUserId(userId, page, size);
            
            // 如果不是管理员或部门经理/HR，只返回当前用户发起的流程实例
            if (!isAdmin && !isManagerOrHR) {
                List<Map<String, Object>> filteredData = new ArrayList<>();
                if (result.get("data") instanceof List) {
                    for (Map<String, Object> instance : (List<Map<String, Object>>) result.get("data")) {
                        if (userId.equals(instance.get("startUserId"))) {
                            filteredData.add(instance);
                        }
                    }
                }
                result.put("data", filteredData);
                result.put("total", filteredData.size());
            }
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("获取已完成流程实例失败:", e);
            return CommonResponse.fail("获取已完成流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 获取流程实例的历史活动
     */
    @GetMapping("/{processInstanceId}/history")
    public CommonResponse<Map<String, Object>> getProcessInstanceHistory(
            @PathVariable String processInstanceId) {
        try {
            logger.info("获取流程实例历史活动，processInstanceId: {}", processInstanceId);
            
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()) || "admin".equals(currentUser));
            
            // 检查用户是否有权限查看该流程实例
            // 1. 获取流程实例信息
            ProcessInstance processInstance = processDefinitionService.getProcessInstanceById(processInstanceId);
            HistoricProcessInstance historicProcessInstance = null;
            
            if (processInstance == null) {
                // 尝试从历史流程实例中查询
                historicProcessInstance = processDefinitionService.getHistoricProcessInstanceById(processInstanceId);
                if (historicProcessInstance == null) {
                    return CommonResponse.notFound("流程实例不存在");
                }
            }
            
            // 2. 检查权限：管理员、部门经理、HR可以查看所有流程，普通用户只能查看自己发起或参与的流程
            boolean isManagerOrHR = authentication.getAuthorities().stream()
                    .anyMatch(auth -> {
                        String authority = auth.getAuthority();
                        return authority.equals("ROLE_MANAGER") || 
                               authority.equals("ROLE_DEV_MANAGER") ||
                               authority.equals("ROLE_TEST_MANAGER") ||
                               authority.equals("ROLE_OPS_MANAGER") ||
                               authority.equals("ROLE_SALES_MANAGER") ||
                               authority.equals("ROLE_MARKET_MANAGER") ||
                               authority.equals("ROLE_HR_MANAGER") ||
                               authority.equals("ROLE_FINANCE_MANAGER") ||
                               authority.equals("ROLE_HR");
                    });
            
            if (!isAdmin && !isManagerOrHR) {
                boolean hasPermission = false;
                
                // 检查是否是流程发起人
                if (processInstance != null && currentUser.equals(processInstance.getStartUserId())) {
                    hasPermission = true;
                } else if (historicProcessInstance != null && currentUser.equals(historicProcessInstance.getStartUserId())) {
                    hasPermission = true;
                }
                
                // 检查是否是流程参与者
                if (!hasPermission) {
                    // 检查流程历史活动中是否有该用户作为办理人
                    List<Map<String, Object>> historyActivities = processDefinitionService.getProcessInstanceHistory(processInstanceId);
                    hasPermission = historyActivities.stream()
                            .anyMatch(activity -> currentUser.equals(activity.get("assignee")));
                }
                
                if (!hasPermission) {
                    return CommonResponse.forbidden("没有权限查看该流程实例的历史活动");
                }
            }

            List<Map<String, Object>> historyActivities = processDefinitionService.getProcessInstanceHistory(processInstanceId);

            Map<String, Object> result = new HashMap<>();
            result.put("data", historyActivities);
            result.put("total", historyActivities.size());

            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("获取流程实例历史活动失败:", e);
            return CommonResponse.fail("获取流程实例历史活动失败: " + e.getMessage());
        }
    }

    /**
     * 设置流程变量
     */
    @PutMapping("/{processInstanceId}/variables")
    public CommonResponse<String> setProcessVariables(
            @PathVariable String processInstanceId,
            @RequestBody Map<String, Object> variables) {
        try {
            logger.info("设置流程变量，processInstanceId: {}, variables: {}", processInstanceId, variables);

            processDefinitionService.setProcessVariables(processInstanceId, variables);
            return CommonResponse.success("流程变量设置成功");
        } catch (Exception e) {
            logger.error("设置流程变量失败:", e);
            return CommonResponse.fail("设置流程变量失败: " + e.getMessage());
        }
    }

    /**
     * 获取流程实例变量
     */
    @GetMapping("/{processInstanceId}/variables")
    public CommonResponse<Map<String, Object>> getProcessVariables(
            @PathVariable String processInstanceId) {
        try {
            logger.info("获取流程实例变量，processInstanceId: {}", processInstanceId);

            Map<String, Object> variables = processDefinitionService.getProcessInstanceVariables(processInstanceId);
            return CommonResponse.success(variables);
        } catch (Exception e) {
            logger.error("获取流程实例变量失败:", e);
            return CommonResponse.fail("获取流程实例变量失败: " + e.getMessage());
        }
    }
}