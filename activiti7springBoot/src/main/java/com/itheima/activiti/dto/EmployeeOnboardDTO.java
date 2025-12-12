package com.itheima.activiti.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 员工入职流程DTO
 */
@Data
public class EmployeeOnboardDTO {
    private String id;
    private String processInstanceId;
    private String name;
    private String employeeId;
    private String department;
    private String position;
    private Date hireDate;
    private String manager;
    private String status;
    private Map<String, Object> variables;
}