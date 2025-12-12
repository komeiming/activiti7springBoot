package com.itheima.activiti.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单翻译实体类
 */
@Data
public class MenuTranslation {
    private Long id;
    private Long menuId; // 关联sys_permission表的id
    private String languageCode; // 关联sys_language表的code
    private String name; // 菜单名称翻译
    private String description; // 菜单描述翻译
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
