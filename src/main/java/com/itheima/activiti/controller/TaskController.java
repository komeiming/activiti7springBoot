package com.itheima.activiti.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itheima.activiti.dto.CommonResponse;
import com.itheima.activiti.dto.TaskDTO;
import com.itheima.activiti.service.TaskService;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    
    @Autowired
    private TaskService taskService;

    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    public CommonResponse<Map<String, Object>> getTaskDetail(@PathVariable String taskId) {
        try {
            Task task = taskService.getTaskById(taskId);
            if (task != null) {
                TaskDTO taskDTO = convertToDTO(task);
                Map<String, Object> variables = taskService.getTaskVariables(taskId);
                List<Comment> comments = taskService.getTaskComments(taskId);
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("task", taskDTO);
                responseData.put("variables", variables);
                responseData.put("comments", comments);
                
                return CommonResponse.success(responseData);
            } else {
                return CommonResponse.notFound("任务不存在");
            }
        } catch (Exception e) {
            return CommonResponse.fail("获取任务详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户的待办任务列表（支持搜索和分页）
     */
    @GetMapping
    public CommonResponse<Map<String, Object>> getMyTasks(
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 获取当前登录用户和认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication.getName();
            
            // 创建任务查询
            TaskQuery taskQuery = taskService.createTaskQuery();
            
            // 根据用户角色进行查询
            // 检查用户是否拥有部门经理角色或是否是dept_manager用户
            boolean isDeptManager = authentication.getAuthorities().stream()
                .anyMatch(auth -> 
                    "ROLE_MANAGER".equals(auth.getAuthority()) || 
                    "ROLE_DEPT_MANAGER".equals(auth.getAuthority()) || 
                    "dept_manager".equals(auth.getAuthority()));
                
            // 处理部门经理权限
            if (isDeptManager || "dept_manager".equals(currentUser)) {
                // 部门经理可以查看分配给自己的任务或部门经理组的候选任务
                taskQuery.taskCandidateGroup("dept_manager").or().taskAssignee(currentUser).endOr();
                logger.info("部门经理{}查询任务列表", currentUser);
            } else {
                // 普通用户只能查询自己的任务
                taskQuery.taskAssignee(currentUser);
            }
            
            // 添加搜索条件
            if (taskName != null && !taskName.isEmpty()) {
                taskQuery.taskNameLike("%" + taskName + "%");
            }
            
            if (assignee != null && !assignee.isEmpty()) {
                taskQuery.taskAssignee(assignee);
            }
            
            // 处理任务状态过滤（如果需要）
            // 注意：Activiti的TaskQuery没有直接的status字段，需要根据业务需求实现
            
            // 分页查询
            long totalCount = taskQuery.count();
            List<Task> tasks = taskQuery
                .listPage((page - 1) * size, size);
            
            // 转换为DTO
            List<TaskDTO> taskDTOs = tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            // 构建返回结果 - 注意嵌套格式与前端期望匹配
            Map<String, Object> dataContent = new HashMap<>();
            dataContent.put("data", taskDTOs);
            dataContent.put("total", totalCount);
            
            return CommonResponse.success(dataContent);
        } catch (Exception e) {
            logger.error("获取待办任务失败:", e);
            return CommonResponse.fail("获取待办任务失败: " + e.getMessage());
        }
    }

    /**
     * 根据流程实例ID查询任务
     */
    @GetMapping("/by-process/{processInstanceId}")
    public CommonResponse<Map<String, Object>> getTasksByProcessInstanceId(@PathVariable String processInstanceId) {
        try {
            List<TaskDTO> tasks = taskService.findTasksByProcessInstanceId(processInstanceId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", tasks);
            result.put("total", tasks.size());
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.fail("根据流程实例ID查询任务失败: " + e.getMessage());
        }
    }

    /**
     * 完成任务
     */
    @PostMapping("/{taskId}/complete")
    public CommonResponse<String> completeTask(@PathVariable String taskId, @RequestBody Map<String, Object> requestBody) {
        try {
            // 验证任务是否存在
            Task task = taskService.getTaskById(taskId);
            if (task != null) {
                // 验证当前用户是否有权限完成任务
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();
                
                if (task.getAssignee() == null || task.getAssignee().equals(username)) {
                    // 从请求体中提取variables
                    Map<String, Object> variables = new HashMap<>();
                    
                    // 处理不同的请求格式
                    if (requestBody.containsKey("variables") && requestBody.get("variables") instanceof Map) {
                        // 如果是嵌套的variables格式
                        variables.putAll((Map<String, Object>) requestBody.get("variables"));
                    } else {
                        // 直接使用请求体作为变量
                        variables.putAll(requestBody);
                    }
                    
                    // 确保approval变量存在（用于流程中的条件判断）
                    if (!variables.containsKey("approval") && variables.containsKey("approved")) {
                        variables.put("approval", variables.get("approved"));
                    }
                    
                    logger.info("完成任务 {}, 用户: {}, 变量: {}", taskId, username, variables);
                    
                    // 执行任务完成
                    boolean success = taskService.completeTask(taskId, variables);
                    if (success) {
                        return CommonResponse.success("任务完成成功");
                    } else {
                        return CommonResponse.fail("任务完成失败");
                    }
                } else {
                    return CommonResponse.fail("您没有权限完成此任务");
                }
            } else {
                return CommonResponse.notFound("任务不存在");
            }
        } catch (Exception e) {
            logger.error("完成任务失败:", e);
            return CommonResponse.fail("完成任务失败: " + e.getMessage());
        }
    }

    /**
     * 认领任务
     */
    @PostMapping("/{taskId}/claim")
    public CommonResponse<String> claimTask(@PathVariable String taskId) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            boolean success = taskService.claimTask(taskId, username);
            if (success) {
                return CommonResponse.success("任务认领成功");
            } else {
                return CommonResponse.fail("任务认领失败");
            }
        } catch (Exception e) {
            return CommonResponse.fail("认领任务失败: " + e.getMessage());
        }
    }

    /**
     * 取消认领任务
     */
    @PostMapping("/{taskId}/unclaim")
    public CommonResponse<String> unclaimTask(@PathVariable String taskId) {
        try {
            // 验证任务是否存在且是否由当前用户认领
            Task task = taskService.getTaskById(taskId);
            if (task != null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();
                
                if (task.getAssignee() != null && task.getAssignee().equals(username)) {
                    boolean success = taskService.unclaimTask(taskId);
                    if (success) {
                        return CommonResponse.success("任务取消认领成功");
                    } else {
                        taskService.unclaim(taskId); // 尝试备用方法
                        return CommonResponse.success("任务取消认领成功");
                    }
                } else {
                    return CommonResponse.fail("您没有权限取消认领此任务");
                }
            } else {
                return CommonResponse.notFound("任务不存在");
            }
        } catch (Exception e) {
            return CommonResponse.fail("取消认领任务失败: " + e.getMessage());
        }
    }

    /**
     * 委派任务
     */
    @PostMapping("/{taskId}/delegate")
    public CommonResponse<String> delegateTask(@PathVariable String taskId, @RequestBody Map<String, String> delegateInfo) {
        try {
            String assignee = delegateInfo.get("assignee");
            if (assignee == null || assignee.trim().isEmpty()) {
                return CommonResponse.fail("委派人员不能为空");
            }
            
            // 验证当前用户是否有权限委派任务
            Task task = taskService.getTaskById(taskId);
            if (task != null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();
                
                if (task.getAssignee() != null && task.getAssignee().equals(username)) {
                    boolean success = taskService.delegateTask(taskId, assignee);
                    if (success) {
                        return CommonResponse.success("任务委派成功");
                    } else {
                        return CommonResponse.fail("任务委派失败");
                    }
                } else {
                    return CommonResponse.fail("您没有权限委派此任务");
                }
            } else {
                return CommonResponse.notFound("任务不存在");
            }
        } catch (Exception e) {
            return CommonResponse.fail("委派任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有任务（仅管理员可访问）
     */
    @GetMapping("/all")
    public CommonResponse<Map<String, Object>> getAllTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 调用分页查询所有任务
            Map<String, Object> result = taskService.getAllTasks(page, size);
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.fail("获取任务列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询已完成任务列表
     */
    @GetMapping("/completed")
    public CommonResponse<Map<String, Object>> getCompletedTasks(
            @RequestParam(required = false) String taskName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = authentication.getName();
            
            Map<String, Object> response = taskService.getCompletedTasks(currentUser, taskName, page, size);
            return CommonResponse.success(response);
        } catch (Exception e) {
            logger.error("查询已完成任务失败:", e);
            return CommonResponse.fail("查询已完成任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 辅助方法：将Task实体转换为DTO
     */
    private TaskDTO convertToDTO(Task task) {
        // 防御性编程检查
        if (task == null) {
            logger.warn("尝试转换null的Task对象");
            return new TaskDTO();
        }
        
        TaskDTO dto = new TaskDTO();
        try {
            // 使用反射安全地设置私有字段
            setPrivateField(dto, "id", task.getId());
            setPrivateField(dto, "name", task.getName());
            setPrivateField(dto, "description", task.getDescription());
            setPrivateField(dto, "assignee", task.getAssignee());
            setPrivateField(dto, "owner", task.getOwner());
            setPrivateField(dto, "createTime", task.getCreateTime());
            setPrivateField(dto, "dueDate", task.getDueDate());
            setPrivateField(dto, "priority", task.getPriority());
            setPrivateField(dto, "category", task.getCategory());
            setPrivateField(dto, "processInstanceId", task.getProcessInstanceId());
            setPrivateField(dto, "processDefinitionId", task.getProcessDefinitionId());
            setPrivateField(dto, "taskDefinitionKey", task.getTaskDefinitionKey());
            setPrivateField(dto, "parentTaskId", task.getParentTaskId());
            
            logger.debug("转换任务到DTO: id={}, name={}, assignee={}", 
                        task.getId(), task.getName(), task.getAssignee());
        } catch (Exception e) {
            logger.error("转换Task到DTO时出错: " + e.getMessage(), e);
        }
        
        return dto;
    }
    
    /**
     * 安全地设置对象的私有字段
     */
    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            logger.warn("设置字段 {} 失败: {}", fieldName, e.getMessage());
        }
    }
}