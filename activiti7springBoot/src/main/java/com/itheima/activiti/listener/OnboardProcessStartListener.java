package com.itheima.activiti.listener;

import com.itheima.activiti.entity.EmployeeOnboard;
import com.itheima.activiti.mapper.EmployeeOnboardMapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 员工入职流程启动监听器
 * 用于在流程启动时初始化流程数据
 */
@Component
public class OnboardProcessStartListener implements ExecutionListener {

    @Autowired
    private EmployeeOnboardMapper employeeOnboardMapper;

    @Override
    public void notify(DelegateExecution execution) {
        // 获取流程变量中的员工信息
        String employeeName = (String) execution.getVariable("employeeName");
        String employeeId = (String) execution.getVariable("employeeId");
        String department = (String) execution.getVariable("department");
        String position = (String) execution.getVariable("position");
        String hireDateStr = (String) execution.getVariable("hireDate");
        String managerName = (String) execution.getVariable("managerName");

        // 初始化员工onboard实体
        EmployeeOnboard employeeOnboard = new EmployeeOnboard();
        employeeOnboard.setProcessInstanceId(execution.getProcessInstanceId());
        employeeOnboard.setEmployeeName(employeeName);
        employeeOnboard.setEmployeeId(employeeId);
        employeeOnboard.setDepartment(department);
        employeeOnboard.setPosition(position);
        
        // 转换日期格式
        if (hireDateStr != null) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date hireDate = format.parse(hireDateStr);
                employeeOnboard.setHireDate(hireDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        
        employeeOnboard.setManagerName(managerName);
        // 初始化状态为待处理
        employeeOnboard.setItEquipmentStatus("pending");
        employeeOnboard.setHrDocumentStatus("pending");
        employeeOnboard.setTrainingStatus("pending");
        
        Date now = new Date();
        employeeOnboard.setCreatedTime(now);
        employeeOnboard.setUpdatedTime(now);
        
        // 保存到数据库
        employeeOnboardMapper.insert(employeeOnboard);
        
        // 设置HR任务的处理人为HR部门的用户
        execution.setVariable("hrAssignee", "admin"); // 默认由admin处理，可以根据实际情况修改
    }
}