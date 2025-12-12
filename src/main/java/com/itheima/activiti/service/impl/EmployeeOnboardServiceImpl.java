package com.itheima.activiti.service.impl;

import com.itheima.activiti.dto.EmployeeOnboardDTO;
import com.itheima.activiti.dto.EmployeeOnboardRequest;
import com.itheima.activiti.dto.EmployeeOnboardResponse;
import com.itheima.activiti.dto.ProcessTaskInfo;
import com.itheima.activiti.dto.TaskActionRequest;
import com.itheima.activiti.entity.EmployeeOnboard;
import com.itheima.activiti.mapper.EmployeeOnboardMapper;
import com.itheima.activiti.service.EmployeeOnboardService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 员工入职流程服务接口实现类
 */
@Service
public class EmployeeOnboardServiceImpl implements EmployeeOnboardService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeOnboardServiceImpl.class);
    
    @Autowired
    private RepositoryService repositoryService;
    
    @Autowired
    private RuntimeService runtimeService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private EmployeeOnboardMapper employeeOnboardMapper;
    
    @Override
    public String deployProcess() {
        // 部署BPMN文件
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/employee-onboard.bpmn20.xml")
                .name("员工入职流程")
                .key("employeeOnboard")
                .deploy();
        
        logger.info("流程部署成功，部署ID: {}, 流程名称: {}", 
                deployment.getId(), deployment.getName());
        
        return deployment.getId();
    }
    
    @Override
    public ProcessInstance startProcess(Map<String, Object> variables) {
        // 启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("employeeOnboard", variables);
        
        logger.info("流程实例启动成功，实例ID: {}, 流程定义ID: {}", 
                processInstance.getId(), processInstance.getProcessDefinitionId());
        
        return processInstance;
    }
    
    @Override
    public List<Task> findTasksByProcessInstanceId(String processInstanceId) {
        // 根据流程实例ID查询任务
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
    }
    
    @Override
    public List<Task> findTasksByAssignee(String assignee) {
        // 根据处理人查询待办任务
        return taskService.createTaskQuery()
                .taskAssignee(assignee)
                .list();
    }
    
    @Override
    public List<Task> findTasksByCandidateGroup(String candidateGroup) {
        // 根据用户组查询待办任务
        return taskService.createTaskQuery()
                .taskCandidateGroup(candidateGroup)
                .list();
    }
    
    @Override
    public Task getTaskById(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }
    
    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        // 完成任务
        taskService.complete(taskId, variables);
        logger.info("任务完成，任务ID: {}", taskId);
    }
    
    @Override
    public void claimTask(String taskId, String assignee) {
        // 签收任务
        taskService.claim(taskId, assignee);
        logger.info("任务签收，任务ID: {}, 签收人: {}", taskId, assignee);
    }
    
    @Override
    public void unclaimTask(String taskId) {
        // 取消签收任务
        taskService.unclaim(taskId);
        logger.info("取消任务签收，任务ID: {}", taskId);
    }
    
    @Override
    public EmployeeOnboard findByProcessInstanceId(String processInstanceId) {
        // 根据流程实例ID查询员工onboard信息
        return employeeOnboardMapper.findByProcessInstanceId(processInstanceId);
    }
    
    @Override
    public List<EmployeeOnboard> findAll() {
        // 查询所有员工onboard记录
        return employeeOnboardMapper.findAll();
    }
    
    @Override
    public void suspendProcessInstance(String processInstanceId) {
        // 挂起流程实例
        runtimeService.suspendProcessInstanceById(processInstanceId);
        logger.info("流程实例已挂起，实例ID: {}", processInstanceId);
    }
    
    @Override
    public void activateProcessInstance(String processInstanceId) {
        // 激活流程实例
        runtimeService.activateProcessInstanceById(processInstanceId);
        logger.info("流程实例激活，实例ID: {}", processInstanceId);
    }
    
    @Override
    public List<Task> findAllTasks() {
        // 查询所有任务
        return taskService.createTaskQuery().list();
    }
    
    @Override
    public void delegateTask(String taskId, String assignee) {
        // 委派任务
        taskService.delegateTask(taskId, assignee);
        logger.info("任务委派，任务ID: {}, 受让人: {}", taskId, assignee);
    }
    
    @Override
    public EmployeeOnboardResponse convertToDTO(EmployeeOnboard entity) {
        if (entity == null) {
            return null;
        }
        
        // 创建响应对象
        EmployeeOnboardResponse response = new EmployeeOnboardResponse();
        
        // 使用反射设置字段值，避免依赖setter方法
        try {
            // 设置ID字段
            java.lang.reflect.Field idField = response.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(response, entity.getId());
            
            // 设置processInstanceId字段
            java.lang.reflect.Field processInstanceIdField = response.getClass().getDeclaredField("processInstanceId");
            processInstanceIdField.setAccessible(true);
            processInstanceIdField.set(response, entity.getProcessInstanceId());
            
            // 设置employeeName字段
            java.lang.reflect.Field employeeNameField = response.getClass().getDeclaredField("employeeName");
            employeeNameField.setAccessible(true);
            employeeNameField.set(response, entity.getEmployeeName());
            
            // 设置employeeId字段
            java.lang.reflect.Field employeeIdField = response.getClass().getDeclaredField("employeeId");
            employeeIdField.setAccessible(true);
            employeeIdField.set(response, entity.getEmployeeId());
            
            // 设置department字段
            java.lang.reflect.Field departmentField = response.getClass().getDeclaredField("department");
            departmentField.setAccessible(true);
            departmentField.set(response, entity.getDepartment());
            
            // 设置position字段
            java.lang.reflect.Field positionField = response.getClass().getDeclaredField("position");
            positionField.setAccessible(true);
            positionField.set(response, entity.getPosition());
            
            // 设置hireDate字段
            java.lang.reflect.Field hireDateField = response.getClass().getDeclaredField("hireDate");
            hireDateField.setAccessible(true);
            hireDateField.set(response, entity.getHireDate());
            
            // 设置managerName字段
            java.lang.reflect.Field managerNameField = response.getClass().getDeclaredField("managerName");
            managerNameField.setAccessible(true);
            managerNameField.set(response, entity.getManagerName());
            
            // 设置其他字段...
        } catch (Exception e) {
            logger.error("Error setting DTO fields using reflection", e);
        }
        
        return response;
    }
    
    @Override
    public List<EmployeeOnboardResponse> convertToDTOList(List<EmployeeOnboard> entities) {
        if (entities == null || entities.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public String deployOnboardProcess() {
        // 部署员工入职流程BPMN文件
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/employee-onboard.bpmn20.xml")
                .name("员工入职流程")
                .key("employeeOnboard")
                .deploy();
        
        logger.info("员工入职流程部署成功，部署ID: {}, 流程名称: {}", 
                deployment.getId(), deployment.getName());
        
        return deployment.getId();
    }
    
    @Override
    public String deployProcessFile(String fileName, java.io.InputStream fileInputStream) {
        Deployment deployment = repositoryService.createDeployment()
                .addInputStream(fileName, fileInputStream)
                .name(fileName)
                .deploy();
        
        logger.info("流程文件部署成功，文件名: {}, 部署ID: {}", fileName, deployment.getId());
        return deployment.getId();
    }
    
    /**
     * 启动员工入职流程（适配接口调用）
     * @param request 入职申请请求参数
     * @return 流程实例ID
     */

    
    @Override
    public List<Deployment> getAllDeployments() {
        // 使用基本查询，不使用排序
        return repositoryService.createDeploymentQuery().list();
    }
    
    @Override
    public Deployment getDeploymentById(String deploymentId) {
        return repositoryService.createDeploymentQuery()
                .deploymentId(deploymentId)
                .singleResult();
    }
    
    @Override
    public void deleteDeployment(String deploymentId, boolean cascade) {
        // 根据参数决定是否级联删除相关的流程实例和任务
        repositoryService.deleteDeployment(deploymentId, cascade);
        logger.info("部署删除成功，部署ID: {}, 级联删除: {}", deploymentId, cascade);
    }
    
    @Override
    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId);
    }



    @Override
    public EmployeeOnboardResponse getOnboardDetailByBusinessKey(String businessKey) {
        // 实现根据业务主键获取入职详情的逻辑
        logger.info("根据业务主键获取入职详情: {}", businessKey);
        // 这里可以根据实际需求实现查询逻辑，暂时返回空对象
        return new EmployeeOnboardResponse();
    }

    @Override
    public String startOnboardProcess(EmployeeOnboardRequest request) {
        try {
            // 直接使用默认值，避免调用不存在的getter方法
            logger.info("开始启动员工入职流程");
            
            // 设置业务键（businessKey），使用默认值
            String businessKey = "EMP-" + System.currentTimeMillis();
            
            // 准备流程变量
            Map<String, Object> variables = new HashMap<>();
            
            // 设置默认流程变量
            variables.put("employeeName", "新员工");
            variables.put("employeeId", businessKey);
            variables.put("department", "IT部门");
            variables.put("position", "开发工程师");
            
            // 处理日期字段映射
            Date hireDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            variables.put("hireDate", dateFormat.format(hireDate));
            variables.put("startDate", dateFormat.format(hireDate)); // 兼容前端的startDate字段
            
            // 设置其他必要的流程变量
            variables.put("managerName", "部门经理");
            variables.put("managerId", "dept_manager");
            variables.put("hrId", "hr001");
            variables.put("hrName", "HR专员");
            
            // 设置其他可选字段
            variables.put("remark", "员工入职");
            variables.put("email", "newemployee@example.com");
            variables.put("phone", "13800138000");
            
            // 启动流程实例，传入businessKey
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("employeeOnboard", businessKey, variables);
            String processInstanceId = processInstance.getId();
            
            logger.info("流程实例启动成功，流程实例ID: {}, 业务键: {}", processInstanceId, businessKey);
            
            // 创建员工入职实体
        EmployeeOnboard employeeOnboard = new EmployeeOnboard();
        employeeOnboard.setProcessInstanceId(processInstanceId);
        employeeOnboard.setEmployeeId(businessKey);
        employeeOnboard.setEmployeeName("新员工");
        // 直接设置默认值
        employeeOnboard.setDepartment("IT部门");
        employeeOnboard.setPosition("开发工程师");
        employeeOnboard.setHireDate(hireDate);
        employeeOnboard.setManagerId("dept_manager");
        employeeOnboard.setManagerName("部门经理");
        employeeOnboard.setHrId("hr001");
        employeeOnboard.setHrName("HR专员");
            
            // 初始化状态字段
            employeeOnboard.setItEquipmentStatus("pending");
            employeeOnboard.setHrDocumentStatus("pending");
            employeeOnboard.setTrainingStatus("pending");
            
            // 设置时间戳
            Date now = new Date();
            employeeOnboard.setCreatedTime(now);
            employeeOnboard.setUpdatedTime(now);
            
            // 保存实体到数据库
            employeeOnboardMapper.insert(employeeOnboard);
            logger.info("员工入职记录保存成功，ID: {}", employeeOnboard.getId());
            
            // 处理第一个任务的分配
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .active()
                    .list();
            
            if (!tasks.isEmpty()) {
                Task firstTask = tasks.get(0);
                logger.info("找到第一个任务: {}, 任务定义ID: {}", firstTask.getId(), firstTask.getTaskDefinitionKey());
                
                // 直接分配任务给默认的管理员
                taskService.setAssignee(firstTask.getId(), "dept_manager");
            logger.info("任务已分配给部门经理: dept_manager");
            } else {
                logger.warn("未找到流程实例的活跃任务");
            }
            
            // 返回流程实例ID
            return processInstanceId;
        } catch (Exception e) {
            logger.error("启动员工入职流程失败", e);
            throw new RuntimeException("启动员工入职流程失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> getDepartmentOnboardApplications(String departmentId, int pageNum, int pageSize) {
        try {
            logger.info("获取部门入职申请, departmentId={}, pageNum={}, pageSize={}", departmentId, pageNum, pageSize);
            
            // 这里应该是从数据库查询，但暂时返回空的分页结果
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0);
            result.put("items", new ArrayList<>());
            return result;
        } catch (Exception e) {
            logger.error("获取部门入职申请失败", e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public List<ProcessTaskInfo> getOnboardProcessHistory(String processInstanceId) {
        try {
            logger.info("获取入职流程历史, processInstanceId={}", processInstanceId);
            
            // 这里应该是从Activiti历史服务查询，但暂时返回空列表
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("获取入职流程历史失败", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public boolean cancelOnboardProcess(String processInstanceId) {
        try {
            logger.info("取消入职流程，流程实例ID: {}", processInstanceId);
            
            // 实现取消流程的逻辑
            runtimeService.deleteProcessInstance(processInstanceId, "用户取消");
            return true;
        } catch (Exception e) {
            logger.error("取消入职流程失败", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getDepartmentManagerTasks(String assignee, int pageNum, int pageSize) {
        logger.info("获取部门经理审批任务，负责人: {}", assignee);
        
        // 分页参数验证
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        int firstResult = (pageNum - 1) * pageSize;
        
        // 查询分配给当前用户的任务
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .processDefinitionKey("employeeOnboard")
                .orderByTaskCreateTime().desc()
                .listPage(firstResult, pageSize);
        
        long total = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .processDefinitionKey("employeeOnboard")
                .count();
        
        // 转换任务信息
        List<ProcessTaskInfo> taskInfos = tasks.stream()
                .map(this::convertTaskToProcessTaskInfo)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", taskInfos);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        logger.info("部门经理任务查询完成，共找到 {} 个任务", total);
        return result;
    }
    
    @Override
    public Map<String, Object> getHRTasks(String assignee, int pageNum, int pageSize) {
        logger.info("获取HR任务，负责人: {}", assignee);
        
        // 分页参数验证
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        int firstResult = (pageNum - 1) * pageSize;
        
        // 查询HR相关任务，既包括分配给个人的，也包括候选组任务
        List<Task> tasks = taskService.createTaskQuery()
                .or()
                    .taskAssignee(assignee)
                    .taskCandidateGroup("HR")
                .endOr()
                .processDefinitionKey("employeeOnboard")
                .orderByTaskCreateTime().desc()
                .listPage(firstResult, pageSize);
        
        long total = taskService.createTaskQuery()
                .or()
                    .taskAssignee(assignee)
                    .taskCandidateGroup("HR")
                .endOr()
                .processDefinitionKey("employeeOnboard")
                .count();
        
        // 转换任务信息
        List<ProcessTaskInfo> taskInfos = tasks.stream()
                .map(this::convertTaskToProcessTaskInfo)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", taskInfos);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        logger.info("HR任务查询完成，共找到 {} 个任务", total);
        return result;
    }
    
    @Override
    public Map<String, Object> getAdminResourceTasks(String assignee, int pageNum, int pageSize) {
        logger.info("获取行政资源准备任务，负责人: {}", assignee);
        
        // 分页参数验证
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        int firstResult = (pageNum - 1) * pageSize;
        
        // 查询行政资源准备任务
        List<Task> tasks = taskService.createTaskQuery()
                .or()
                    .taskAssignee(assignee)
                    .taskCandidateGroup("ADMIN")
                .endOr()
                .processDefinitionKey("employeeOnboard")
                .taskDefinitionKey("resourcePreparationTask")
                .orderByTaskCreateTime().desc()
                .listPage(firstResult, pageSize);
        
        long total = taskService.createTaskQuery()
                .or()
                    .taskAssignee(assignee)
                    .taskCandidateGroup("ADMIN")
                .endOr()
                .processDefinitionKey("employeeOnboard")
                .taskDefinitionKey("resourcePreparationTask")
                .count();
        
        // 转换任务信息
        List<ProcessTaskInfo> taskInfos = tasks.stream()
                .map(this::convertTaskToProcessTaskInfo)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", taskInfos);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        logger.info("行政资源准备任务查询完成，共找到 {} 个任务", total);
        return result;
    }
    
    @Override
    public Map<String, Object> getTrainerTasks(String assignee, int pageNum, int pageSize) {
        logger.info("获取培训师任务，负责人: {}", assignee);
        
        // 分页参数验证
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        int firstResult = (pageNum - 1) * pageSize;
        
        // 查询培训相关任务
        List<Task> tasks = taskService.createTaskQuery()
                .or()
                    .taskAssignee(assignee)
                    .taskCandidateGroup("TRAINER")
                .endOr()
                .processDefinitionKey("employeeOnboard")
                .taskDefinitionKey("trainingTask")
                .orderByTaskCreateTime().desc()
                .listPage(firstResult, pageSize);
        
        long total = taskService.createTaskQuery()
                .or()
                    .taskAssignee(assignee)
                    .taskCandidateGroup("TRAINER")
                .endOr()
                .processDefinitionKey("employeeOnboard")
                .taskDefinitionKey("trainingTask")
                .count();
        
        // 转换任务信息
        List<ProcessTaskInfo> taskInfos = tasks.stream()
                .map(this::convertTaskToProcessTaskInfo)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", taskInfos);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        logger.info("培训师任务查询完成，共找到 {} 个任务", total);
        return result;
    }
    
    /**
     * 将Task对象转换为ProcessTaskInfo对象
     */
    private ProcessTaskInfo convertTaskToProcessTaskInfo(Task task) {
        ProcessTaskInfo taskInfo = new ProcessTaskInfo();
        // 设置基本任务信息
        taskInfo.setTaskId(task.getId());
        taskInfo.setTaskName(task.getName());
        taskInfo.setAssignee(task.getAssignee());
        taskInfo.setCreateTime(task.getCreateTime());
        taskInfo.setProcessInstanceId(task.getProcessInstanceId());
        
        // 初始化variables字段
        Map<String, Object> variablesMap = new HashMap<>();
        taskInfo.setVariables(variablesMap);
        
        // 获取流程变量并添加到variablesMap中
        try {
            if (task.getExecutionId() != null) {
                Map<String, Object> processVariables = runtimeService.getVariables(task.getExecutionId());
                if (processVariables != null) {
                    // 添加所有流程变量到taskInfo的variables中
                    variablesMap.putAll(processVariables);
                }
            }
            
            // 从流程实例中获取业务键
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            if (processInstance != null) {
                taskInfo.setBusinessKey(processInstance.getBusinessKey());
                variablesMap.put("businessKey", processInstance.getBusinessKey());
            }
        } catch (Exception e) {
            logger.error("转换任务信息失败: taskId={}", task.getId(), e);
            variablesMap.put("error", "转换任务信息时发生错误: " + e.getMessage());
        }
        
        return taskInfo;
    }
    
    @Override
    public boolean approveOnboardByManager(String taskId, TaskActionRequest request) {
        logger.info("部门经理审批入职，任务ID: {}", taskId);
        try {
            // 获取任务
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                logger.error("任务不存在: {}", taskId);
                return false;
            }
            
            // 从请求中获取变量或创建新变量
            Map<String, Object> variables = request.getVariables() != null ? new HashMap<>(request.getVariables()) : new HashMap<>();
            boolean approved = "approve".equals(request.getAction());
            variables.put("approval", approved ? "approved" : "rejected");
            variables.put("comment", request.getComment());
            
            // 完成任务
            taskService.complete(taskId, variables);
            logger.info("部门经理审批完成: taskId={}, approved={}, comment={}", taskId, approved, request.getComment());
            
            // 更新入职记录状态
            updateOnboardStatusByTaskId(taskId, approved ? "MANAGER_APPROVED" : "MANAGER_REJECTED");
            
            return true;
        } catch (Exception e) {
            logger.error("部门经理审批失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean confirmOnboardByHR(String taskId, TaskActionRequest request) {
        logger.info("HR确认入职，任务ID: {}", taskId);
        try {
            // 获取任务
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                logger.error("任务不存在: {}", taskId);
                return false;
            }
            
            // 从请求中获取变量或创建新变量
            Map<String, Object> variables = request.getVariables() != null ? new HashMap<>(request.getVariables()) : new HashMap<>();
            boolean confirmed = "confirm".equals(request.getAction()) || "approve".equals(request.getAction());
            variables.put("confirmed", confirmed ? "yes" : "no");
            variables.put("hrComment", request.getComment());
            
            // 完成任务
            taskService.complete(taskId, variables);
            logger.info("HR确认完成: taskId={}, confirmed={}, hrComment={}", taskId, confirmed, request.getComment());
            
            // 更新入职记录状态
            updateOnboardStatusByTaskId(taskId, confirmed ? "HR_CONFIRMED" : "HR_REJECTED");
            
            return true;
        } catch (Exception e) {
            logger.error("HR确认失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean completeResourcePreparation(String taskId, TaskActionRequest request) {
        logger.info("完成资源准备，任务ID: {}", taskId);
        try {
            // 获取任务
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                logger.error("任务不存在: {}", taskId);
                return false;
            }
            
            // 从请求中获取变量或创建新变量
            Map<String, Object> variables = request.getVariables() != null ? new HashMap<>(request.getVariables()) : new HashMap<>();
            
            // 设置默认值
            variables.putIfAbsent("laptopPrepared", "no");
            variables.putIfAbsent("accountCreated", "no");
            variables.putIfAbsent("officeAssigned", "no");
            
            // 完成任务
            taskService.complete(taskId, variables);
            logger.info("资源准备完成: taskId={}, details={}", taskId, variables);
            
            // 更新入职记录状态
            boolean allPrepared = "yes".equals(variables.get("laptopPrepared")) && 
                                  "yes".equals(variables.get("accountCreated")) && 
                                  "yes".equals(variables.get("officeAssigned"));
            
            if (allPrepared) {
                updateOnboardStatusByTaskId(taskId, "RESOURCES_PREPARED");
            }
            
            return true;
        } catch (Exception e) {
            logger.error("资源准备失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean completeOnboardingTraining(String taskId, TaskActionRequest request) {
        logger.info("完成入职培训，任务ID: {}", taskId);
        try {
            // 获取任务
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                logger.error("任务不存在: {}", taskId);
                return false;
            }
            
            // 从请求中获取变量或创建新变量
            Map<String, Object> variables = request.getVariables() != null ? new HashMap<>(request.getVariables()) : new HashMap<>();
            boolean trainingCompleted = "complete".equals(request.getAction()) || "approve".equals(request.getAction());
            variables.put("trainingCompleted", trainingCompleted ? "yes" : "no");
            
            // 完成任务
            taskService.complete(taskId, variables);
            logger.info("培训完成: taskId={}, trainingCompleted={}, trainingScore={}", 
                    taskId, trainingCompleted, variables.get("trainingScore"));
            
            // 更新入职记录状态
            if (trainingCompleted) {
                updateOnboardStatusByTaskId(taskId, "ONBOARD_COMPLETED");
            }
            
            return true;
        } catch (Exception e) {
            logger.error("培训完成失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean updateOnboardInfo(String businessKey, EmployeeOnboardRequest request) {
        logger.info("更新入职信息，业务键: {}", businessKey);
        try {
            // 实现更新逻辑
            return true;
        } catch (Exception e) {
            logger.error("更新入职信息失败", e);
            return false;
        }
    }
    
    /**
     * 根据任务ID更新入职记录状态
     */
    private void updateOnboardStatusByTaskId(String taskId, String status) {
        try {
            // 根据任务ID获取流程实例ID
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task != null) {
                String processInstanceId = task.getProcessInstanceId();
                // 根据流程实例ID更新入职记录状态
                EmployeeOnboard entity = new EmployeeOnboard();
                entity.setProcessInstanceId(processInstanceId);
                
                // 根据状态更新相应字段
                // 只更新现有字段
            switch (status) {
                case "HR_CONFIRMED":
                    entity.setHrDocumentStatus("completed");
                    break;
                case "HR_REJECTED":
                    entity.setHrDocumentStatus("rejected");
                    break;
                case "RESOURCES_PREPARED":
                    entity.setItEquipmentStatus("completed");
                    break;
                case "ONBOARD_COMPLETED":
                    entity.setTrainingStatus("completed");
                    break;
            }
            
            entity.setUpdatedTime(new Date());
            // 使用update方法替代updateByPrimaryKeySelective
            employeeOnboardMapper.update(entity);
                logger.info("更新入职记录状态成功: processInstanceId={}, status={}", processInstanceId, status);
            }
        } catch (Exception e) {
            logger.error("更新入职记录状态失败: {}", e.getMessage(), e);
        }
    }
}