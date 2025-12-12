package com.itheima.activiti.listener;

import com.itheima.activiti.mapper.EmployeeOnboardMapper;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * IT任务完成监听器
 * 用于在IT任务完成后更新IT设备状态
 */
@Component
public class ITTaskCompleteListener implements TaskListener {

    private static final Logger logger = LoggerFactory.getLogger(ITTaskCompleteListener.class);

    @Autowired
    private EmployeeOnboardMapper employeeOnboardMapper;

    @Override
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        
        // 获取表单提交的IT设备状态
        String itEquipmentStatus = (String) delegateTask.getVariable("itEquipmentStatus");
        
        if (itEquipmentStatus != null) {
            // 更新数据库中的IT设备状态
            int updatedRows = employeeOnboardMapper.updateItEquipmentStatus(processInstanceId, itEquipmentStatus);
            
            if (updatedRows > 0) {
                logger.info("IT任务完成：已更新员工入职流程的IT设备状态为{}，流程实例ID: {}", 
                        itEquipmentStatus, processInstanceId);
            }
        }
    }
}