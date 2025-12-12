package com.itheima.activiti.dto;

import lombok.Data;

import java.util.Date;

/**
 * 员工入职响应DTO
 */
@Data
public class EmployeeOnboardResponse {
    private Long id;
    private String processInstanceId;
    private String employeeName;
    private String employeeId;
    private String department;
    private String position;
    private Date hireDate;
    private String managerName;
    private String itEquipmentStatus;
    private String hrDocumentStatus;
    private String trainingStatus;
    private Date createdTime;
    private Date updatedTime;
    private String currentTaskName;
    private String currentAssignee;
    
    // 添加缺少的getBusinessKey方法，返回employeeId作为业务键
    public String getBusinessKey() {
        return this.employeeId;
    }
}