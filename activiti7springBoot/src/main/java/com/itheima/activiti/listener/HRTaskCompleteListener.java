package com.itheima.activiti.listener;

import com.itheima.activiti.mapper.EmployeeOnboardMapper;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * HR任务完成监听器
 * 用于在HR任务完成后更新HR文档状态
 */
@Component
public class HRTaskCompleteListener implements TaskListener {

    private static final Logger logger = LoggerFactory.getLogger(HRTaskCompleteListener.class);

    @Autowired
    private EmployeeOnboardMapper employeeOnboardMapper;

    @Override
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        
        // 获取表单提交的HR文档状态
        String hrDocumentStatus = (String) delegateTask.getVariable("hrDocumentStatus");
        
        if (hrDocumentStatus != null) {
            // 更新数据库中的HR文档状态
            int updatedRows = employeeOnboardMapper.updateHrDocumentStatus(processInstanceId, hrDocumentStatus);
            
            if (updatedRows > 0) {
                logger.info("HR任务完成：已更新员工入职流程的HR文档状态为{}，流程实例ID: {}", 
                        hrDocumentStatus, processInstanceId);
            }
        }
    }
}