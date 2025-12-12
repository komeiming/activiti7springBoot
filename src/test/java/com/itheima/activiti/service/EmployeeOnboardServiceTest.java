package com.itheima.activiti.service;

import com.itheima.activiti.BaseTest;
import com.itheima.activiti.dto.EmployeeOnboardRequest;
import com.itheima.activiti.dto.EmployeeOnboardResponse;
import com.itheima.activiti.dto.ProcessTaskInfo;
import com.itheima.activiti.dto.TaskActionRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 员工入职服务测试
 */
public class EmployeeOnboardServiceTest extends BaseTest {

    @Autowired
    private EmployeeOnboardService employeeOnboardService;

    private String businessKey;
    private String processInstanceId;
    private String managerTaskId;
    private String hrTaskId;
    private String adminTaskId;
    private String trainerTaskId;

    @BeforeEach
    public void initData() {
        // 清理测试数据
        cleanTestData();
    }

    @Test
    public void testCompleteOnboardingProcess() {
        // 1. 测试启动入职流程
        EmployeeOnboardRequest request = createTestOnboardRequest();
        // 修复返回值类型问题
        Object startResult = employeeOnboardService.startOnboardProcess(request);
        
        Assertions.assertNotNull(startResult, "启动结果不应为null");
        
        // 根据实际返回值类型获取需要的数据
        if (startResult instanceof Map) {
            Map<?, ?> resultMap = (Map<?, ?>) startResult;
            processInstanceId = (String) resultMap.get("processInstanceId");
            businessKey = (String) resultMap.get("businessKey");
        }
        
        Assertions.assertNotNull(processInstanceId, "流程实例ID不应为null");
        Assertions.assertNotNull(businessKey, "业务键不应为null");
        
        // 2. 获取部门经理审批任务
        Map<String, Object> managerTasks = employeeOnboardService.getDepartmentManagerTasks("zhangsan", 1, 10);
        List<Map<String, Object>> managerTaskList = (List<Map<String, Object>>) managerTasks.get("list");
        Assertions.assertFalse(managerTaskList.isEmpty(), "部门经理任务列表不应为空");
        
        managerTaskId = (String) managerTaskList.get(0).get("id");
        Assertions.assertNotNull(managerTaskId, "部门经理任务ID不应为null");
        
        // 3. 部门经理审批
        TaskActionRequest managerAction = new TaskActionRequest();
        managerAction.setActionType("approve");
        managerAction.setComment("同意入职");
        managerAction.setApproved(true);
        managerAction.setAssignee("lisi"); // HR的用户ID
        
        boolean managerApproved = employeeOnboardService.approveOnboardByManager(managerTaskId, managerAction);
        Assertions.assertTrue(managerApproved, "部门经理审批应成功");
        
        // 4. 获取HR确认任务
        waitForAsyncTasks(1000);
        Map<String, Object> hrTasks = employeeOnboardService.getHRTasks("lisi", 1, 10);
        List<Map<String, Object>> hrTaskList = (List<Map<String, Object>>) hrTasks.get("list");
        Assertions.assertFalse(hrTaskList.isEmpty(), "HR任务列表不应为空");
        
        hrTaskId = (String) hrTaskList.get(0).get("id");
        Assertions.assertNotNull(hrTaskId, "HR任务ID不应为null");
        
        // 5. HR确认入职
        TaskActionRequest hrAction = new TaskActionRequest();
        hrAction.setActionType("confirm");
        hrAction.setComment("已确认入职信息");
        hrAction.setAssignee("wangwu"); // 行政的用户ID
        
        boolean hrConfirmed = employeeOnboardService.confirmOnboardByHR(hrTaskId, hrAction);
        Assertions.assertTrue(hrConfirmed, "HR确认应成功");
        
        // 6. 获取行政资源准备任务
        waitForAsyncTasks(1000);
        Map<String, Object> adminTasks = employeeOnboardService.getAdminResourceTasks("wangwu", 1, 10);
        List<Map<String, Object>> adminTaskList = (List<Map<String, Object>>) adminTasks.get("list");
        Assertions.assertFalse(adminTaskList.isEmpty(), "行政任务列表不应为空");
        
        adminTaskId = (String) adminTaskList.get(0).get("id");
        Assertions.assertNotNull(adminTaskId, "行政任务ID不应为null");
        
        // 7. 完成资源准备
        TaskActionRequest adminAction = new TaskActionRequest();
        adminAction.setActionType("complete");
        adminAction.setComment("办公设备已准备就绪");
        adminAction.setAssignee("zhaoliu"); // 培训师的用户ID
        
        boolean resourcePrepared = employeeOnboardService.completeResourcePreparation(adminTaskId, adminAction);
        Assertions.assertTrue(resourcePrepared, "资源准备应成功");
        
        // 8. 获取培训任务
        waitForAsyncTasks(1000);
        Map<String, Object> trainerTasks = employeeOnboardService.getTrainerTasks("zhaoliu", 1, 10);
        List<Map<String, Object>> trainerTaskList = (List<Map<String, Object>>) trainerTasks.get("list");
        Assertions.assertFalse(trainerTaskList.isEmpty(), "培训任务列表不应为空");
        
        trainerTaskId = (String) trainerTaskList.get(0).get("id");
        Assertions.assertNotNull(trainerTaskId, "培训任务ID不应为null");
        
        // 9. 完成入职培训
        TaskActionRequest trainerAction = new TaskActionRequest();
        trainerAction.setActionType("complete");
        trainerAction.setComment("入职培训已完成");
        
        boolean trainingCompleted = employeeOnboardService.completeOnboardingTraining(trainerTaskId, trainerAction);
        Assertions.assertTrue(trainingCompleted, "培训完成应成功");
        
        // 10. 验证流程已结束
        waitForAsyncTasks(1000);
        long activeProcessInstances = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .count();
        Assertions.assertEquals(0, activeProcessInstances, "流程应已结束");
        
        // 11. 获取入职详情
        EmployeeOnboardResponse detail = employeeOnboardService.getOnboardDetailByBusinessKey(businessKey);
        Assertions.assertNotNull(detail, "入职详情不应为null");
        Assertions.assertEquals(businessKey, detail.getBusinessKey(), "业务键应匹配");
        
        // 12. 获取流程历史
        List<ProcessTaskInfo> history = employeeOnboardService.getOnboardProcessHistory(processInstanceId);
        Assertions.assertNotNull(history, "流程历史不应为null");
        Assertions.assertFalse(history.isEmpty(), "流程历史不应为空");
    }

    @Test
    public void testGetDepartmentApplications() {
        // 启动两个不同部门的入职流程
        EmployeeOnboardRequest request1 = createTestOnboardRequest();
        request1.setDepartmentId("HR");
        // 修复返回值类型
        Object result1 = employeeOnboardService.startOnboardProcess(request1);
        
        EmployeeOnboardRequest request2 = createTestOnboardRequest();
        request2.setDepartmentId("IT");
        // 修复返回值类型
        Object result2 = employeeOnboardService.startOnboardProcess(request2);
        
        // 查询特定部门的入职申请
        Map<String, Object> hrApplications = employeeOnboardService.getDepartmentOnboardApplications("HR", 1, 10);
        List<Map<String, Object>> hrAppList = (List<Map<String, Object>>) hrApplications.get("list");
        
        Assertions.assertNotNull(hrAppList, "部门申请列表不应为null");
        Assertions.assertFalse(hrAppList.isEmpty(), "部门申请列表不应为空");
    }

    @Test
    public void testCancelOnboardProcess() {
        // 启动入职流程
        EmployeeOnboardRequest request = createTestOnboardRequest();
        // 修复返回值类型
        Object startResult = employeeOnboardService.startOnboardProcess(request);
        String cancelProcessInstanceId = null;
        if (startResult instanceof Map) {
            cancelProcessInstanceId = (String) ((Map<?, ?>) startResult).get("processInstanceId");
        }
        
        // 取消入职流程
        boolean canceled = employeeOnboardService.cancelOnboardProcess(cancelProcessInstanceId);
        Assertions.assertTrue(canceled, "取消流程应成功");
        
        // 验证流程已取消
        long activeProcessInstances = runtimeService.createProcessInstanceQuery()
                .processInstanceId(cancelProcessInstanceId)
                .count();
        Assertions.assertEquals(0, activeProcessInstances, "流程应已取消");
    }

    @Test
    public void testUpdateOnboardInfo() {
        // 启动入职流程
        EmployeeOnboardRequest request = createTestOnboardRequest();
        // 修复返回值类型
        Object startResult = employeeOnboardService.startOnboardProcess(request);
        String updateBusinessKey = null;
        if (startResult instanceof Map) {
            updateBusinessKey = (String) ((Map<?, ?>) startResult).get("businessKey");
        }
        
        // 更新入职信息
        EmployeeOnboardRequest updateRequest = new EmployeeOnboardRequest();
        updateRequest.setEmployeeName("更新后的员工姓名");
        updateRequest.setPosition("更新后的职位");
        
        boolean updated = employeeOnboardService.updateOnboardInfo(updateBusinessKey, updateRequest);
        Assertions.assertTrue(updated, "更新应成功");
        
        // 验证更新结果
        EmployeeOnboardResponse updatedDetail = employeeOnboardService.getOnboardDetailByBusinessKey(updateBusinessKey);
        Assertions.assertNotNull(updatedDetail, "更新后的详情不应为null");
    }

    /**
     * 创建测试用的入职请求
     */
    private EmployeeOnboardRequest createTestOnboardRequest() {
        EmployeeOnboardRequest request = new EmployeeOnboardRequest();
        request.setEmployeeName("测试员工");
        request.setEmployeeId("EMP" + System.currentTimeMillis());
        request.setDepartmentId("IT");
        request.setDepartmentName("信息技术部");
        request.setPosition("软件工程师");
        request.setHireDate(new Date());
        request.setContractType("正式");
        request.setContractPeriod(3);
        request.setSalary(15000.0);
        request.setProbationPeriod(3);
        request.setReportTo("zhangsan");
        request.setEmail("test@example.com");
        request.setPhone("13800138000");
        request.setAddress("测试地址");
        request.setEmergencyContact("紧急联系人");
        request.setEmergencyPhone("13900139000");
        request.setEducationLevel("本科");
        request.setGraduateSchool("测试大学");
        request.setMajor("计算机科学");
        request.setSkills("Java, Spring Boot");
        request.setRemark("测试入职");
        request.setManagerId("zhangsan");
        request.setManagerName("张三");
        request.setHrId("lisi");
        request.setHrName("李四");
        
        return request;
    }
}