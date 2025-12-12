package com.itheima.activiti.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 员工onboard流程实体类
 */
public class EmployeeOnboard implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String processInstanceId;
    private String employeeName;
    private String employeeId;
    private String department;
    private String position;
    private Date hireDate;
    private String managerName;
    private String managerId;
    private String hrId;
    private String hrName;
    private String itEquipmentStatus;
    private String hrDocumentStatus;
    private String trainingStatus;
    private Date createdTime;
    private Date updatedTime;
    
    // getter and setter methods
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProcessInstanceId() {
        return processInstanceId;
    }
    
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
    
    public String getEmployeeName() {
        return employeeName;
    }
    
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public Date getHireDate() {
        return hireDate;
    }
    
    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }
    
    public String getManagerName() {
        return managerName;
    }
    
    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }
    
    public String getManagerId() {
        return managerId;
    }
    
    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }
    
    public String getHrId() {
        return hrId;
    }
    
    public void setHrId(String hrId) {
        this.hrId = hrId;
    }
    
    public String getHrName() {
        return hrName;
    }
    
    public void setHrName(String hrName) {
        this.hrName = hrName;
    }
    
    public String getItEquipmentStatus() {
        return itEquipmentStatus;
    }
    
    public void setItEquipmentStatus(String itEquipmentStatus) {
        this.itEquipmentStatus = itEquipmentStatus;
    }
    
    public String getHrDocumentStatus() {
        return hrDocumentStatus;
    }
    
    public void setHrDocumentStatus(String hrDocumentStatus) {
        this.hrDocumentStatus = hrDocumentStatus;
    }
    
    public String getTrainingStatus() {
        return trainingStatus;
    }
    
    public void setTrainingStatus(String trainingStatus) {
        this.trainingStatus = trainingStatus;
    }
    
    public Date getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    
    public Date getUpdatedTime() {
        return updatedTime;
    }
    
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}