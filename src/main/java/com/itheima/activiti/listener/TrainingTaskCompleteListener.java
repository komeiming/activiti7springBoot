package com.itheima.activiti.listener;

import com.itheima.activiti.mapper.EmployeeOnboardMapper;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 培训任务完成监听器
 * 用于在培训任务完成后更新培训状态
 */
@Component
public class TrainingTaskCompleteListener implements TaskListener {

    private static final Logger logger = LoggerFactory.getLogger(TrainingTaskCompleteListener.class);

    @Autowired
    private EmployeeOnboardMapper employeeOnboardMapper;

    @Override
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        
        // 获取表单提交的培训状态
        String trainingStatus = (String) delegateTask.getVariable("trainingStatus");
        
        if (trainingStatus != null) {
            // 更新数据库中的培训状态
            int updatedRows = employeeOnboardMapper.updateTrainingStatus(processInstanceId, trainingStatus);
            
            if (updatedRows > 0) {
                logger.info("培训任务完成：已更新员工入职流程的培训状态为{}，流程实例ID: {}", 
                        trainingStatus, processInstanceId);
            }
        }
    }
}