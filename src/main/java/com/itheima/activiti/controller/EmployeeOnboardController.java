package com.itheima.activiti.controller;

import com.itheima.activiti.dto.CommonResponse;
import com.itheima.activiti.dto.EmployeeOnboardRequest;
import com.itheima.activiti.dto.EmployeeOnboardResponse;
import com.itheima.activiti.dto.ProcessTaskInfo;
import com.itheima.activiti.dto.TaskActionRequest;
import com.itheima.activiti.entity.EmployeeOnboard;
import com.itheima.activiti.service.EmployeeOnboardService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工入职流程控制器
 */
@RestController
@RequestMapping("/api/v1/onboard")
public class EmployeeOnboardController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeOnboardController.class);

    @Autowired
    private EmployeeOnboardService employeeOnboardService;
    
    /**
     * 部署流程定义
     */
    @PostMapping("/deploy")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<Map<String, String>> deployProcess() {
        try {
            String deploymentId = employeeOnboardService.deployOnboardProcess();
            Map<String, String> result = new HashMap<>();
            result.put("message", "流程部署成功");
            result.put("deploymentId", deploymentId);
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.fail("流程部署失败: " + e.getMessage());
        }
    }
    
    /**
     * 提交员工入职申请
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public CommonResponse<Map<String, Object>> submitOnboardRequest(@RequestBody EmployeeOnboardRequest request) {
        try {
            // 增加详细日志监控
            logger.info("接收到入职流程启动请求");
            
            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            logger.info("当前操作用户: {}", currentUsername);
            
            // 记录请求中的关键参数
            if (request != null) {
                logger.info("员工姓名: {}", request.getEmployeeName());
                logger.info("员工ID: {}", request.getEmployeeId());
                logger.info("部门: {}", request.getDepartment());
                logger.info("职位: {}", request.getPosition());
                logger.info("入职日期: {}", request.getHireDate());
                logger.info("经理姓名: {}", request.getManagerName());
                logger.info("经理ID: {}", request.getManagerId());
                logger.info("HR姓名: {}", request.getHrName());
                logger.info("HR ID: {}", request.getHrId());
            }
            
            // 调用服务层启动流程
            logger.info("开始调用服务层启动入职流程");
            String processInstanceId = employeeOnboardService.startOnboardProcess(request);
            logger.info("入职流程启动成功，流程实例ID: {}", processInstanceId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "入职流程启动成功");
            result.put("processInstanceId", processInstanceId);
            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("提交入职申请失败，错误信息: {}", e.getMessage(), e);
            return CommonResponse.fail("入职流程启动异常: " + e.getMessage());
        }
    }
    
    /**
     * 获取入职申请列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPT_MANAGER', 'HR', 'ADMIN', 'TRAINER')")
    public CommonResponse<Map<String, Object>> getOnboardList() {
        try {
            List<EmployeeOnboard> allOnboards = employeeOnboardService.findAll();
            List<EmployeeOnboardResponse> dtoList = employeeOnboardService.convertToDTOList(allOnboards);
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", dtoList);
            result.put("total", dtoList.size());
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.fail("获取入职申请列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据流程实例ID获取入职申请详情
     */
    @GetMapping("/{processInstanceId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPT_MANAGER', 'HR', 'ADMIN', 'TRAINER')")
    public CommonResponse<EmployeeOnboardResponse> getOnboardDetail(@PathVariable String processInstanceId) {
        try {
            EmployeeOnboard onboard = employeeOnboardService.findByProcessInstanceId(processInstanceId);
            if (onboard != null) {
                EmployeeOnboardResponse response = employeeOnboardService.convertToDTO(onboard);
                return CommonResponse.success(response);
            } else {
                return CommonResponse.fail("未找到对应的入职申请");
            }
        } catch (Exception e) {
            return CommonResponse.fail("获取入职申请详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 挂起流程实例
     */
    @PutMapping("/{processInstanceId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<String> suspendProcess(@PathVariable String processInstanceId) {
        try {
            employeeOnboardService.suspendProcessInstance(processInstanceId);
            return CommonResponse.success("流程实例已成功挂起");
        } catch (Exception e) {
        return CommonResponse.fail("挂起流程实例失败: " + e.getMessage());
    }
    }
    
    /**
     * 激活流程实例
     */
    @PutMapping("/{processInstanceId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<String> activateProcess(@PathVariable String processInstanceId) {
        try {
            employeeOnboardService.activateProcessInstance(processInstanceId);
            return CommonResponse.success("流程实例已成功激活");
        } catch (Exception e) {
        return CommonResponse.fail("激活流程实例失败: " + e.getMessage());
    }
    }
    
    /**
     * 获取部门经理审批任务
     */
    @GetMapping("/tasks/manager")
    @PreAuthorize("hasRole('DEPT_MANAGER') or hasRole('ADMIN')")
    public CommonResponse<Map<String, Object>> getManagerTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Map<String, Object> tasks = employeeOnboardService.getDepartmentManagerTasks(username, pageNum, pageSize);
            return CommonResponse.success(tasks);
        } catch (Exception e) {
            return CommonResponse.fail("获取部门经理任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取HR任务
     */
    @GetMapping("/tasks/hr")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public CommonResponse<Map<String, Object>> getHRTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Map<String, Object> tasks = employeeOnboardService.getHRTasks(username, pageNum, pageSize);
            return CommonResponse.success(tasks);
        } catch (Exception e) {
            return CommonResponse.fail("获取HR任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取资源准备任务
     */
    @GetMapping("/tasks/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<Map<String, Object>> getAdminTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Map<String, Object> tasks = employeeOnboardService.getAdminResourceTasks(username, pageNum, pageSize);
            return CommonResponse.success(tasks);
        } catch (Exception e) {
            return CommonResponse.fail("获取资源准备任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取培训任务
     */
    @GetMapping("/tasks/trainer")
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public CommonResponse<Map<String, Object>> getTrainerTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Map<String, Object> tasks = employeeOnboardService.getTrainerTasks(username, pageNum, pageSize);
            return CommonResponse.success(tasks);
        } catch (Exception e) {
        return CommonResponse.fail("获取培训任务失败: " + e.getMessage());
    }
    }
    
    /**
     * 部门经理审批入职申请
     */
    @PostMapping("/tasks/{taskId}/manager-approve")
    @PreAuthorize("hasRole('DEPT_MANAGER') or hasRole('ADMIN')")
    public CommonResponse<String> managerApprove(
            @PathVariable String taskId,
            @RequestBody TaskActionRequest request) {
        try {
            boolean success = employeeOnboardService.approveOnboardByManager(taskId, request);
            if (success) {
                return CommonResponse.success("审批成功");
            } else {
                return CommonResponse.fail("审批失败");
            }
        } catch (Exception e) {
        return CommonResponse.fail("审批过程发生异常: " + e.getMessage());
    }
    }
    
    /**
     * HR确认入职
     */
    @PostMapping("/tasks/{taskId}/hr-confirm")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public CommonResponse<String> hrConfirm(
            @PathVariable String taskId,
            @RequestBody TaskActionRequest request) {
        try {
            boolean success = employeeOnboardService.confirmOnboardByHR(taskId, request);
            if (success) {
                return CommonResponse.success("确认入职成功");
            } else {
                return CommonResponse.fail("确认入职失败");
            }
        } catch (Exception e) {
        return CommonResponse.fail("确认入职过程发生异常: " + e.getMessage());
    }
    }
    
    /**
     * 完成资源准备
     */
    @PostMapping("/tasks/{taskId}/resource-prepare")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<String> completeResourcePreparation(
            @PathVariable String taskId,
            @RequestBody TaskActionRequest request) {
        try {
            boolean success = employeeOnboardService.completeResourcePreparation(taskId, request);
            if (success) {
                return CommonResponse.success("资源准备完成");
            } else {
                return CommonResponse.fail("资源准备失败");
            }
        } catch (Exception e) {
        return CommonResponse.fail("资源准备过程发生异常: " + e.getMessage());
    }
    }
    
    /**
     * 完成入职培训
     */
    @PostMapping("/tasks/{taskId}/training-complete")
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public CommonResponse<String> completeTraining(
            @PathVariable String taskId,
            @RequestBody TaskActionRequest request) {
        try {
            boolean success = employeeOnboardService.completeOnboardingTraining(taskId, request);
            if (success) {
                return CommonResponse.success("培训完成");
            } else {
                return CommonResponse.fail("培训完成处理失败");
            }
        } catch (Exception e) {
        return CommonResponse.fail("培训完成过程发生异常: " + e.getMessage());
    }
    }
    
    /**
     * 取消入职流程
     */
    @DeleteMapping("/{processInstanceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public CommonResponse<String> cancelOnboardProcess(@PathVariable String processInstanceId) {
        try {
            boolean success = employeeOnboardService.cancelOnboardProcess(processInstanceId);
            if (success) {
                return CommonResponse.success("入职流程已取消");
            } else {
                return CommonResponse.fail("取消入职流程失败");
            }
        } catch (Exception e) {
        return CommonResponse.fail("取消入职流程异常: " + e.getMessage());
    }
    }
    
    /**
     * 获取流程历史
     */
    @GetMapping("/{processInstanceId}/history")
    public CommonResponse<List<ProcessTaskInfo>> getProcessHistory(@PathVariable String processInstanceId) {
        try {
            List<ProcessTaskInfo> history = employeeOnboardService.getOnboardProcessHistory(processInstanceId);
            return CommonResponse.success(history);
        } catch (Exception e) {
        return CommonResponse.fail("获取流程历史失败: " + e.getMessage());
    }
    }
    
    /**
     * 更新入职信息
     */
    @PutMapping("/update/{businessKey}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public CommonResponse<String> updateOnboardInfo(
            @PathVariable String businessKey,
            @RequestBody EmployeeOnboardRequest request) {
        try {
            boolean success = employeeOnboardService.updateOnboardInfo(businessKey, request);
            if (success) {
                return CommonResponse.success("入职信息更新成功");
            } else {
                return CommonResponse.fail("入职信息更新失败");
            }
        } catch (Exception e) {
        return CommonResponse.fail("更新入职信息异常: " + e.getMessage());
    }
    }
    
    /**
     * 获取部门入职申请列表
     */
    @GetMapping("/department/{departmentId}")
    public CommonResponse<Map<String, Object>> getDepartmentOnboardApplications(
            @PathVariable String departmentId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Map<String, Object> result = employeeOnboardService.getDepartmentOnboardApplications(
                    departmentId, pageNum, pageSize);
            return CommonResponse.success(result);
        } catch (Exception e) {
        return CommonResponse.fail("获取部门入职申请失败: " + e.getMessage());
    }
    }
    
    /**
     * 任务签收
     */
    @PostMapping("/tasks/{taskId}/claim")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPT_MANAGER', 'HR', 'ADMIN', 'TRAINER')")
    public CommonResponse<String> claimTask(@PathVariable String taskId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            employeeOnboardService.claimTask(taskId, username);
            return CommonResponse.success("任务签收成功");
        } catch (Exception e) {
        return CommonResponse.fail("任务签收失败: " + e.getMessage());
    }
    }
    
    /**
     * 取消任务签收
     */
    @PostMapping("/tasks/{taskId}/unclaim")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'DEPT_MANAGER', 'HR', 'ADMIN', 'TRAINER')")
    public CommonResponse<String> unclaimTask(@PathVariable String taskId) {
        try {
            employeeOnboardService.unclaimTask(taskId);
            return CommonResponse.success("取消任务签收成功");
        } catch (Exception e) {
        return CommonResponse.fail("取消任务签收失败: " + e.getMessage());
    }
    }
    
    /**
     * 辅助方法：将实体类转换为响应Map
     */
    private Map<String, Object> convertToResponse(EmployeeOnboard entity) {
        Map<String, Object> response = new HashMap<>();
        if (entity.getId() != null) {
            response.put("id", entity.getId());
        }
        response.put("processInstanceId", entity.getProcessInstanceId());
        response.put("employeeName", entity.getEmployeeName());
        response.put("employeeId", entity.getEmployeeId());
        response.put("department", entity.getDepartment());
        response.put("position", entity.getPosition());
        response.put("hireDate", entity.getHireDate());
        response.put("managerName", entity.getManagerName());
        response.put("itEquipmentStatus", entity.getItEquipmentStatus());
        response.put("hrDocumentStatus", entity.getHrDocumentStatus());
        response.put("trainingStatus", entity.getTrainingStatus());
        response.put("createdTime", entity.getCreatedTime());
        response.put("updatedTime", entity.getUpdatedTime());
        
        // 获取当前任务信息
        try {
            List<Task> tasks = employeeOnboardService.findTasksByProcessInstanceId(entity.getProcessInstanceId());
            if (!tasks.isEmpty()) {
                Task currentTask = tasks.get(0);
                Map<String, Object> taskInfo = new HashMap<>();
                taskInfo.put("taskId", currentTask.getId());
                taskInfo.put("taskName", currentTask.getName());
                taskInfo.put("assignee", currentTask.getAssignee());
                taskInfo.put("createTime", currentTask.getCreateTime());
                response.put("currentTask", taskInfo);
            }
        } catch (Exception e) {
            // 忽略获取任务信息的错误，避免影响主要数据返回
        }
        
        return response;
    }
}