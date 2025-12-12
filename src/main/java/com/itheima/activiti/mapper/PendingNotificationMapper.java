package com.itheima.activiti.mapper;

import com.itheima.activiti.entity.PendingNotification;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

/**
 * 待发送通知Mapper接口
 */
@Mapper
public interface PendingNotificationMapper {
    
    // 插入待发送通知
    int insert(PendingNotification pendingNotification);
    
    // 更新待发送通知
    int update(PendingNotification pendingNotification);
    
    // 根据ID查询待发送通知
    PendingNotification selectById(String id);
    
    // 根据条件查询待发送通知列表
    List<PendingNotification> selectByParams(Map<String, Object> params);
    
    // 查询待发送的通知（状态为pending且下次重试时间<=当前时间）
    List<PendingNotification> selectPendingNotifications();
    
    // 根据状态查询待发送通知
    List<PendingNotification> selectByStatus(String status);
    
    // 根据类型查询待发送通知
    List<PendingNotification> selectByType(String type);
    
    // 删除待发送通知
    int deleteById(String id);
    
    // 批量删除待发送通知
    int deleteBatch(List<String> ids);
}