package com.itheima.activiti.mapper;

import com.itheima.activiti.entity.NotificationLog;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

/**
 * 通知发送日志Mapper接口
 */
@Mapper
public interface NotificationLogMapper {
    
    List<NotificationLog> selectAll();
    
    NotificationLog selectById(String id);
    
    int insert(NotificationLog log);
    
    // 按条件查询日志
    List<NotificationLog> selectByParams(Map<String, Object> params);
    
    // 批量插入方法
    int batchInsert(List<NotificationLog> logs);
    
    // 根据发送ID查询日志
    NotificationLog findBySendId(String sendId);
    
    // 根据模板ID查询日志
    List<NotificationLog> findByTemplateId(String templateId);
    
    // 查询发送失败的日志
    List<NotificationLog> findFailedLogs(Map<String, Object> condition);
    
    // 统计日志数量
    int countLogs(Map<String, Object> condition);
}
