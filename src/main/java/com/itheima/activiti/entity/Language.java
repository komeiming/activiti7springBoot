package com.itheima.activiti.entity;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 语言实体类
 */
@Data
public class Language {
    private Long id;
    private String code; // 语言代码，如zh-CN, en-US
    private String name; // 语言名称，如中文，English
    private Boolean isDefault; // 是否默认语言
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
