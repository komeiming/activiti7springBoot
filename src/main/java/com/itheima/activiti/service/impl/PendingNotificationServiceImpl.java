package com.itheima.activiti.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.activiti.entity.PendingNotification;
import com.itheima.activiti.entity.NotificationRequest;
import com.itheima.activiti.entity.NotificationResponse;
import com.itheima.activiti.mapper.PendingNotificationMapper;
import com.itheima.activiti.service.PendingNotificationService;
import com.itheima.activiti.service.NotificationSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 待发送通知服务实现类
 */
@Service
public class PendingNotificationServiceImpl implements PendingNotificationService {
    
    @Autowired
    private PendingNotificationMapper pendingNotificationMapper;
    
    @Autowired
    private NotificationSenderService notificationSenderService;
    
    @Override
    @Transactional
    public PendingNotification savePendingNotification(PendingNotification pendingNotification) {
        try {
            // 生成ID
            if (pendingNotification.getId() == null || pendingNotification.getId().isEmpty()) {
                pendingNotification.setId(UUID.randomUUID().toString());
            }
            
            // 设置默认值
            if (pendingNotification.getStatus() == null || pendingNotification.getStatus().isEmpty()) {
                pendingNotification.setStatus("pending");
            }
            if (pendingNotification.getRetryCount() == null) {
                pendingNotification.setRetryCount(0);
            }
            if (pendingNotification.getMaxRetries() == null) {
                pendingNotification.setMaxRetries(3);
            }
            if (pendingNotification.getPriority() == null) {
                pendingNotification.setPriority(0);
            }
            if (pendingNotification.getCreatedTime() == null) {
                pendingNotification.setCreatedTime(LocalDateTime.now());
            }
            if (pendingNotification.getUpdatedTime() == null) {
                pendingNotification.setUpdatedTime(LocalDateTime.now());
            }
            if (pendingNotification.getNextRetryTime() == null) {
                pendingNotification.setNextRetryTime(LocalDateTime.now());
            }
            
            // 保存待发送通知
            pendingNotificationMapper.insert(pendingNotification);
            return pendingNotification;
        } catch (Exception e) {
            System.err.println("保存待发送通知失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    @Transactional
    public PendingNotification updatePendingNotification(PendingNotification pendingNotification) {
        try {
            // 设置更新时间
            pendingNotification.setUpdatedTime(LocalDateTime.now());
            
            // 更新待发送通知
            pendingNotificationMapper.update(pendingNotification);
            return pendingNotification;
        } catch (Exception e) {
            System.err.println("更新待发送通知失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public PendingNotification getPendingNotificationById(String id) {
        try {
            return pendingNotificationMapper.selectById(id);
        } catch (Exception e) {
            System.err.println("查询待发送通知失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<PendingNotification> getPendingNotificationsByParams(Map<String, Object> params) {
        try {
            return pendingNotificationMapper.selectByParams(params);
        } catch (Exception e) {
            System.err.println("条件查询待发送通知失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<PendingNotification> getPendingNotifications() {
        try {
            return pendingNotificationMapper.selectPendingNotifications();
        } catch (Exception e) {
            System.err.println("查询待发送通知失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<PendingNotification> getPendingNotificationsByStatus(String status) {
        try {
            return pendingNotificationMapper.selectByStatus(status);
        } catch (Exception e) {
            System.err.println("根据状态查询待发送通知失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<PendingNotification> getPendingNotificationsByType(String type) {
        try {
            return pendingNotificationMapper.selectByType(type);
        } catch (Exception e) {
            System.err.println("根据类型查询待发送通知失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    @Transactional
    public boolean deletePendingNotification(String id) {
        try {
            return pendingNotificationMapper.deleteById(id) > 0;
        } catch (Exception e) {
            System.err.println("删除待发送通知失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deletePendingNotifications(List<String> ids) {
        try {
            return pendingNotificationMapper.deleteBatch(ids) > 0;
        } catch (Exception e) {
            System.err.println("批量删除待发送通知失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean processPendingNotification(String id) {
        try {
            // 查询待发送通知
            PendingNotification pendingNotification = pendingNotificationMapper.selectById(id);
            if (pendingNotification == null) {
                return false;
            }
            
            // 更新状态为发送中
            pendingNotification.setStatus("sending");
            pendingNotification.setUpdatedTime(LocalDateTime.now());
            pendingNotificationMapper.update(pendingNotification);
            
            // 构造通知请求
            NotificationRequest request = new NotificationRequest();
            request.setTemplateId(pendingNotification.getTemplateId());
            request.setReceiver(pendingNotification.getReceiver());
            // 将String类型的params转换为Map<String, Object>类型
            if (pendingNotification.getParams() != null && !pendingNotification.getParams().isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> paramsMap = objectMapper.readValue(pendingNotification.getParams(), Map.class);
                    request.setParams(paramsMap);
                } catch (Exception e) {
                    System.err.println("参数反序列化失败: " + e.getMessage());
                    request.setParams(null);
                }
            } else {
                request.setParams(null);
            }
            request.setSenderSystem(pendingNotification.getSenderSystem());
            request.setRequestId(pendingNotification.getRequestId());
            
            // 发送通知
            NotificationResponse response;
            if ("email".equalsIgnoreCase(pendingNotification.getType())) {
                response = notificationSenderService.sendEmail(request);
            } else if ("sms".equalsIgnoreCase(pendingNotification.getType())) {
                response = notificationSenderService.sendSms(request);
            } else {
                response = new NotificationResponse();
                response.setSuccess(false);
                response.setErrorCode("UNSUPPORTED_TYPE");
                response.setMessage("不支持的通知类型: " + pendingNotification.getType());
            }
            
            // 更新待发送通知状态
            if (response.isSuccess()) {
                pendingNotification.setStatus("sent");
            } else {
                // 发送失败，更新重试次数和下次重试时间
                int retryCount = pendingNotification.getRetryCount() + 1;
                pendingNotification.setRetryCount(retryCount);
                
                if (retryCount >= pendingNotification.getMaxRetries()) {
                    // 超过最大重试次数，标记为失败
                    pendingNotification.setStatus("failed");
                } else {
                    // 未超过最大重试次数，计算下次重试时间（指数退避）
                    pendingNotification.setStatus("pending");
                    pendingNotification.setNextRetryTime(LocalDateTime.now().plusSeconds((long) Math.pow(2, retryCount)));
                }
            }
            pendingNotification.setUpdatedTime(LocalDateTime.now());
            pendingNotificationMapper.update(pendingNotification);
            
            return response.isSuccess();
        } catch (Exception e) {
            System.err.println("处理待发送通知失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    @Transactional
    public int processPendingNotifications() {
        try {
            // 查询待发送的通知
            List<PendingNotification> pendingNotifications = pendingNotificationMapper.selectPendingNotifications();
            if (pendingNotifications == null || pendingNotifications.isEmpty()) {
                return 0;
            }
            
            int processedCount = 0;
            // 处理每个待发送通知
            for (PendingNotification pendingNotification : pendingNotifications) {
                if (processPendingNotification(pendingNotification.getId())) {
                    processedCount++;
                }
            }
            
            return processedCount;
        } catch (Exception e) {
            System.err.println("批量处理待发送通知失败: " + e.getMessage());
            return 0;
        }
    }
}