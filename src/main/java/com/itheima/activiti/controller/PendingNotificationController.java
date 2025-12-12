package com.itheima.activiti.controller;

import com.itheima.activiti.common.Result;
import com.itheima.activiti.entity.PendingNotification;
import com.itheima.activiti.service.PendingNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 待发送通知控制器
 */
@RestController
@RequestMapping("/api/notification/pending")
public class PendingNotificationController {
    
    @Autowired
    private PendingNotificationService pendingNotificationService;
    
    /**
     * 查询待发送通知列表（支持分页）
     */
    @GetMapping
    public Result<Map<String, Object>> getPendingNotifications(@RequestParam Map<String, Object> params) {
        try {
            // 获取待发送通知列表
            List<PendingNotification> pendingNotifications = pendingNotificationService.getPendingNotificationsByParams(params);
            
            // 构建分页响应
            Map<String, Object> response = new HashMap<>();
            response.put("list", pendingNotifications);
            response.put("total", pendingNotifications != null ? pendingNotifications.size() : 0);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("查询待发送通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询待发送通知
     */
    @GetMapping("/{id}")
    public Result<PendingNotification> getPendingNotificationById(@PathVariable String id) {
        try {
            PendingNotification pendingNotification = pendingNotificationService.getPendingNotificationById(id);
            if (pendingNotification != null) {
                return Result.success(pendingNotification);
            } else {
                return Result.error("待发送通知不存在");
            }
        } catch (Exception e) {
            return Result.error("查询待发送通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据状态查询待发送通知
     */
    @GetMapping("/status/{status}")
    public Result<List<PendingNotification>> getPendingNotificationsByStatus(@PathVariable String status) {
        try {
            List<PendingNotification> pendingNotifications = pendingNotificationService.getPendingNotificationsByStatus(status);
            return Result.success(pendingNotifications);
        } catch (Exception e) {
            return Result.error("查询待发送通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据类型查询待发送通知
     */
    @GetMapping("/type/{type}")
    public Result<List<PendingNotification>> getPendingNotificationsByType(@PathVariable String type) {
        try {
            List<PendingNotification> pendingNotifications = pendingNotificationService.getPendingNotificationsByType(type);
            return Result.success(pendingNotifications);
        } catch (Exception e) {
            return Result.error("查询待发送通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理待发送通知
     */
    @PostMapping("/process/{id}")
    public Result<Boolean> processPendingNotification(@PathVariable String id) {
        try {
            boolean result = pendingNotificationService.processPendingNotification(id);
            if (result) {
                return Result.success("处理待发送通知成功", true);
            } else {
                return Result.error("处理待发送通知失败");
            }
        } catch (Exception e) {
            return Result.error("处理待发送通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量处理待发送通知
     */
    @PostMapping("/process/batch")
    public Result<Integer> processPendingNotifications() {
        try {
            int processedCount = pendingNotificationService.processPendingNotifications();
            return Result.success("批量处理待发送通知成功", processedCount);
        } catch (Exception e) {
            return Result.error("批量处理待发送通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除待发送通知
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deletePendingNotification(@PathVariable String id) {
        try {
            boolean result = pendingNotificationService.deletePendingNotification(id);
            if (result) {
                return Result.success("删除待发送通知成功", true);
            } else {
                return Result.error("删除待发送通知失败");
            }
        } catch (Exception e) {
            return Result.error("删除待发送通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除待发送通知
     */
    @DeleteMapping("/batch")
    public Result<Boolean> deletePendingNotifications(@RequestBody List<String> ids) {
        try {
            boolean result = pendingNotificationService.deletePendingNotifications(ids);
            if (result) {
                return Result.success("批量删除待发送通知成功", true);
            } else {
                return Result.error("批量删除待发送通知失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除待发送通知失败: " + e.getMessage());
        }
    }
}