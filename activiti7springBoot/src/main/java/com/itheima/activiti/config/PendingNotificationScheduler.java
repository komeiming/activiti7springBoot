package com.itheima.activiti.config;

import com.itheima.activiti.service.PendingNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 待发送通知定时任务
 */
@Component
@EnableScheduling
public class PendingNotificationScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(PendingNotificationScheduler.class);
    
    @Autowired
    private PendingNotificationService pendingNotificationService;
    
    @Value("${notification.scheduler.fixed-rate:30000}")
    private long fixedRate;
    
    /**
     * 处理待发送通知
     */
    @Scheduled(fixedRateString = "${notification.scheduler.fixed-rate:30000}")
    public void processPendingNotifications() {
        logger.info("开始处理待发送通知...");
        long startTime = System.currentTimeMillis();
        int processedCount = pendingNotificationService.processPendingNotifications();
        long endTime = System.currentTimeMillis();
        logger.info("待发送通知处理完成，共处理 {} 条通知，耗时 {} 毫秒", processedCount, (endTime - startTime));
    }
}