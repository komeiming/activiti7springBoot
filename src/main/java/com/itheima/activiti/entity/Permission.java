package com.itheima.activiti.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class Permission {
    private Long id;
    private String name;
    private String code;
    private String url;
    private String method;
    private String description;
    // 菜单相关字段
    private Long parentId;
    private String menuType;
    private Integer menuOrder;
    private String icon;
    private String path;
    private String component;
    private String redirect;
    private Boolean hidden;
    private Boolean alwaysShow;
    private Boolean affix;
    // 子菜单
    private List<Permission> children;
    // 菜单翻译
    private List<MenuTranslation> translations;
    // 用于前端显示的本地化名称和描述
    private transient String localizedName;
    private transient String localizedDescription;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    
    // 方法：根据语言代码获取本地化名称
    public String getLocalizedName(String languageCode) {
        if (translations != null && !translations.isEmpty()) {
            // 查找匹配的语言翻译
            for (MenuTranslation translation : translations) {
                if (translation.getLanguageCode().equals(languageCode)) {
                    return translation.getName();
                }
            }
        }
        // 如果没有找到匹配的翻译，返回默认名称
        return this.name;
    }
    
    // 方法：根据语言代码获取本地化描述
    public String getLocalizedDescription(String languageCode) {
        if (translations != null && !translations.isEmpty()) {
            // 查找匹配的语言翻译
            for (MenuTranslation translation : translations) {
                if (translation.getLanguageCode().equals(languageCode)) {
                    return translation.getDescription();
                }
            }
        }
        // 如果没有找到匹配的翻译，返回默认描述
        return this.description;
    }
    
    // 方法：设置本地化属性
    public void setLocalizedProperties(String languageCode) {
        this.localizedName = getLocalizedName(languageCode);
        this.localizedDescription = getLocalizedDescription(languageCode);
        
        // 递归设置子菜单的本地化属性
        if (children != null && !children.isEmpty()) {
            for (Permission child : children) {
                child.setLocalizedProperties(languageCode);
            }
        }
    }
}