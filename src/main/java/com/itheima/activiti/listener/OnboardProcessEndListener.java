package com.itheima.activiti.listener;

import com.itheima.activiti.entity.EmployeeOnboard;
import com.itheima.activiti.mapper.EmployeeOnboardMapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 员工入职流程结束监听器
 * 用于在流程结束时执行通知或记录操作
 */
@Component
public class OnboardProcessEndListener implements ExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(OnboardProcessEndListener.class);

    @Autowired
    private EmployeeOnboardMapper employeeOnboardMapper;

    @Override
    public void notify(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        
        // 更新数据库中的流程状态
        EmployeeOnboard employeeOnboard = employeeOnboardMapper.findByProcessInstanceId(processInstanceId);
        if (employeeOnboard != null) {
            employeeOnboard.setUpdatedTime(new Date());
            employeeOnboardMapper.update(employeeOnboard);
            
            logger.info("员工入职流程已完成，员工: {}, 员工编号: {}", 
                    employeeOnboard.getEmployeeName(), employeeOnboard.getEmployeeId());
            
            // 这里可以添加通知逻辑，例如发送邮件、短信等通知相关人员流程已完成
            // sendNotification(employeeOnboard);
        }
    }
    
    /**
     * 发送通知的方法（示例）
     * @param employeeOnboard 员工入职信息
     */
    private void sendNotification(EmployeeOnboard employeeOnboard) {
        // 实际项目中可以实现具体的通知逻辑
        // 例如调用邮件服务、短信服务等
        logger.info("向HR和IT部门发送通知：员工{}的入职流程已完成", employeeOnboard.getEmployeeName());
    }
}