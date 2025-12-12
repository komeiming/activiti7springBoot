package com.itheima.activiti.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.itheima.activiti.entity.NotificationTemplate;

/**
 * 通知模板Mapper接口
 */
@Mapper
public interface NotificationTemplateMapper {
    
    List<NotificationTemplate> selectAll();
    
    NotificationTemplate selectById(String id);
    
    int deleteById(String id);
    
    int insert(NotificationTemplate template);
    
    int update(NotificationTemplate template);
    
    int updateStatus(@Param("id") String id, @Param("status") String status);
    
    List<NotificationTemplate> selectByStatus(String status);
    
    List<NotificationTemplate> selectByType(String type);
    
    List<NotificationTemplate> selectByParams(Map<String, Object> params);
}
