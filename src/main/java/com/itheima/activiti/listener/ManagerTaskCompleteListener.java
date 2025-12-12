package com.itheima.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 部门经理任务完成监听器
 * 用于处理部门经理的审核结果
 */
@Component
public class ManagerTaskCompleteListener implements TaskListener {

    private static final Logger logger = LoggerFactory.getLogger(ManagerTaskCompleteListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        
        // 获取表单提交的审核结果
        String managerApproval = (String) delegateTask.getVariable("managerApproval");
        String managerComments = (String) delegateTask.getVariable("managerComments");
        
        // 将审核结果和意见设置为流程变量，供后续使用
        delegateTask.getExecution().setVariable("managerApproval", managerApproval);
        
        if ("approved".equals(managerApproval)) {
            logger.info("部门经理审核通过：流程实例ID: {}, 审核意见: {}", 
                    processInstanceId, managerComments);
        } else if ("rejected".equals(managerApproval)) {
            logger.info("部门经理审核驳回：流程实例ID: {}, 审核意见: {}", 
                    processInstanceId, managerComments);
            // 可以在这里添加驳回通知逻辑
        }
    }
}