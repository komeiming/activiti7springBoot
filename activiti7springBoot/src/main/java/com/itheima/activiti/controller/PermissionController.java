package com.itheima.activiti.controller;

import com.itheima.activiti.dto.CommonResponse;
import com.itheima.activiti.entity.Language;
import com.itheima.activiti.entity.MenuTranslation;
import com.itheima.activiti.entity.Permission;
import com.itheima.activiti.entity.SysRole;
import com.itheima.activiti.mapper.LanguageMapper;
import com.itheima.activiti.mapper.MenuTranslationMapper;
import com.itheima.activiti.mapper.PermissionMapper;
import com.itheima.activiti.mapper.RoleMapper;
import com.itheima.activiti.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限和菜单管理控制器
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private UserService userService;
    
    @Autowired
    private LanguageMapper languageMapper;
    
    @Autowired
    private MenuTranslationMapper menuTranslationMapper;
    
    @Autowired
    private RoleMapper roleMapper;

    /**
     * 获取所有权限和菜单
     */
    @GetMapping
    public CommonResponse<List<Permission>> getAllPermissions(@RequestParam(required = false) String languageCode) {
        try {
            List<Permission> permissions = permissionMapper.findAll();
            // 设置菜单翻译
            setMenuTranslations(permissions, languageCode);
            // 构建树形结构
            List<Permission> menuTree = buildMenuTree(permissions);
            return CommonResponse.success(menuTree);
        } catch (Exception e) {
            return CommonResponse.fail("获取权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的菜单
     */
    @GetMapping("/user-menus")
    public CommonResponse<List<Permission>> getUserMenus(@RequestParam(required = false) String languageCode) {
        try {
            // 获取当前登录用户的用户名
            String username = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                username = authentication.getName();
            } else {
                // 对于匿名访问或测试环境，返回固定的管理员菜单
                username = "admin";
            }
            
            // 获取用户所有角色的菜单权限
            List<Long> userMenuIds = new ArrayList<>();
            
            // 获取当前用户的完整信息，包括角色
            com.itheima.activiti.entity.SysUser user = userService.findByUsername(username);
            if (user != null && user.getRoles() != null) {
                // 用户直接从UserService获取的包含角色信息
                for (com.itheima.activiti.entity.SysRole role : user.getRoles()) {
                    List<Long> menuIds = roleMapper.findMenuIdsByRoleId(role.getId());
                    if (menuIds != null) {
                        userMenuIds.addAll(menuIds);
                    }
                }
            }
            
            // 获取所有菜单
            List<Permission> allPermissions = permissionMapper.findAll();
            
            // 过滤出用户有权限的菜单
            Set<Long> userMenuIdSet = new java.util.HashSet<>(userMenuIds);
            List<Permission> allMenuPermissions = allPermissions.stream()
                    .filter(p -> "directory".equals(p.getMenuType()) || "menu".equals(p.getMenuType()))
                    .collect(Collectors.toList());
            
            // 过滤出用户有权限的菜单及其父目录
            List<Permission> userMenuPermissions = filterUserMenuPermissions(allMenuPermissions, userMenuIdSet);
            
            // 设置菜单翻译
            setMenuTranslations(userMenuPermissions, languageCode);
            
            // 构建树形结构
            List<Permission> menuTree = buildMenuTree(userMenuPermissions);
            
            return CommonResponse.success(menuTree);
        } catch (Exception e) {
            return CommonResponse.fail("获取用户菜单失败: " + e.getMessage());
        }
    }
    
    /**
     * 过滤用户有权限的菜单及其父目录和子菜单
     */
    private List<Permission> filterUserMenuPermissions(List<Permission> allMenuPermissions, Set<Long> userMenuIdSet) {
        // 构建菜单Map，便于查找父菜单和子菜单，并初始化children列表
        Map<Long, Permission> menuMap = allMenuPermissions.stream()
                .collect(Collectors.toMap(Permission::getId, permission -> {
                    permission.setChildren(new ArrayList<>());
                    return permission;
                }));
        
        // 构建父菜单到子菜单的映射
        Map<Long, List<Permission>> parentToChildrenMap = new java.util.HashMap<>();
        for (Permission menu : allMenuPermissions) {
            Long parentId = menu.getParentId();
            if (parentId != null && parentId != 0) {
                parentToChildrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(menu);
            }
        }
        
        // 找出所有需要保留的菜单ID（包括用户有权限的菜单及其所有父目录和子菜单）
        Set<Long> retainedMenuIds = new java.util.HashSet<>();
        
        // 递归添加所有子菜单
        java.util.function.Consumer<Long> addChildrenRecursively = new java.util.function.Consumer<Long>() {
            @Override
            public void accept(Long menuId) {
                if (!retainedMenuIds.contains(menuId) && menuMap.containsKey(menuId)) {
                    retainedMenuIds.add(menuId);
                    // 递归添加所有子菜单
                    List<Permission> children = parentToChildrenMap.get(menuId);
                    if (children != null) {
                        for (Permission child : children) {
                            this.accept(child.getId());
                        }
                    }
                }
            }
        };
        
        // 递归添加所有父目录
        java.util.function.Consumer<Long> addParentsRecursively = new java.util.function.Consumer<Long>() {
            @Override
            public void accept(Long menuId) {
                if (menuId != null && menuId != 0 && menuMap.containsKey(menuId) && !retainedMenuIds.contains(menuId)) {
                    retainedMenuIds.add(menuId);
                    // 递归添加所有父目录
                    Permission menu = menuMap.get(menuId);
                    this.accept(menu.getParentId());
                }
            }
        };
        
        // 添加用户直接有权限的菜单及其父目录和子菜单
        for (Permission menu : allMenuPermissions) {
            if (userMenuIdSet.contains(menu.getId())) {
                // 添加当前菜单
                retainedMenuIds.add(menu.getId());
                
                // 递归添加所有父目录
                addParentsRecursively.accept(menu.getParentId());
                
                // 递归添加所有子菜单
                addChildrenRecursively.accept(menu.getId());
            }
        }
        
        // 过滤出需要保留的菜单
        return allMenuPermissions.stream()
                .filter(menu -> retainedMenuIds.contains(menu.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有支持的语言
     */
    @GetMapping("/languages")
    public CommonResponse<List<Language>> getLanguages() {
        try {
            List<Language> languages = languageMapper.findAll();
            return CommonResponse.success(languages);
        } catch (Exception e) {
            return CommonResponse.fail("获取语言列表失败: " + e.getMessage());
        }
    }

    /**
     * 构建菜单树结构
     */
    private List<Permission> buildMenuTree(List<Permission> permissions) {
        // 将过滤后的菜单权限转换为Map，便于查找父节点，并初始化children列表
        Map<Long, Permission> permissionMap = new java.util.HashMap<>();
        
        // 首先去重，确保每个菜单只出现一次
        List<Permission> uniquePermissions = permissions.stream()
                .distinct()
                .collect(Collectors.toList());
        
        // 构建菜单Map
        for (Permission permission : uniquePermissions) {
            Permission menu = new Permission();
            menu.setId(permission.getId());
            menu.setName(permission.getName());
            menu.setCode(permission.getCode());
            menu.setUrl(permission.getUrl());
            menu.setMethod(permission.getMethod());
            menu.setDescription(permission.getDescription());
            menu.setParentId(permission.getParentId());
            menu.setMenuType(permission.getMenuType());
            menu.setMenuOrder(permission.getMenuOrder());
            menu.setIcon(permission.getIcon());
            menu.setPath(permission.getPath());
            menu.setComponent(permission.getComponent());
            menu.setRedirect(permission.getRedirect());
            menu.setHidden(permission.getHidden());
            menu.setAlwaysShow(permission.getAlwaysShow());
            menu.setAffix(permission.getAffix());
            menu.setChildren(new ArrayList<>());
            menu.setTranslations(permission.getTranslations());
            menu.setLocalizedName(permission.getLocalizedName());
            menu.setLocalizedDescription(permission.getLocalizedDescription());
            menu.setCreatedTime(permission.getCreatedTime());
            menu.setUpdatedTime(permission.getUpdatedTime());
            permissionMap.put(menu.getId(), menu);
        }

        // 构建菜单树
        List<Permission> menuTree = new ArrayList<>();
        // 先处理所有子节点，将它们添加到父节点的children列表中
        for (Permission permission : uniquePermissions) {
            if (permission.getParentId() != null && permission.getParentId() != 0) {
                // 子节点，添加到父节点的children列表中
                Permission parent = permissionMap.get(permission.getParentId());
                Permission child = permissionMap.get(permission.getId());
                if (parent != null && child != null) {
                    // 检查子节点是否已存在于父节点的children列表中
                    boolean exists = parent.getChildren().stream()
                            .anyMatch(c -> c.getId().equals(child.getId()));
                    if (!exists) {
                        parent.getChildren().add(child);
                    }
                }
            }
        }
        // 再处理所有根节点，将它们添加到菜单树中
        for (Permission permission : uniquePermissions) {
            if (permission.getParentId() == null || permission.getParentId() == 0) {
                // 根节点，直接添加到菜单树
                menuTree.add(permissionMap.get(permission.getId()));
            }
        }

        // 按menuOrder排序根菜单
        menuTree.sort((m1, m2) -> {
            Integer order1 = m1.getMenuOrder() != null ? m1.getMenuOrder() : 0;
            Integer order2 = m2.getMenuOrder() != null ? m2.getMenuOrder() : 0;
            return order1.compareTo(order2);
        });

        // 递归排序子菜单
        sortChildren(menuTree);

        return menuTree;
    }
    
    /**
     * 递归排序子菜单
     */
    private void sortChildren(List<Permission> menus) {
        for (Permission menu : menus) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                menu.getChildren().sort((m1, m2) -> {
                    Integer order1 = m1.getMenuOrder() != null ? m1.getMenuOrder() : 0;
                    Integer order2 = m2.getMenuOrder() != null ? m2.getMenuOrder() : 0;
                    return order1.compareTo(order2);
                });
                sortChildren(menu.getChildren());
            }
        }
    }
    
    /**
     * 设置菜单翻译
     */
    private void setMenuTranslations(List<Permission> permissions, String languageCode) {
        // 如果没有指定语言代码，使用默认语言
        if (languageCode == null || languageCode.isEmpty()) {
            try {
                Language defaultLanguage = languageMapper.findDefault();
                languageCode = defaultLanguage != null ? defaultLanguage.getCode() : "zh-CN";
            } catch (Exception e) {
                // 如果查询默认语言失败，使用默认的中文
                languageCode = "zh-CN";
            }
        }
        
        // 查询所有菜单翻译，添加异常处理
        Map<Long, List<MenuTranslation>> translationsByMenuId = new java.util.HashMap<>();
        try {
            List<MenuTranslation> allTranslations = menuTranslationMapper.findAll();
            // 将翻译按菜单ID分组
            translationsByMenuId = allTranslations.stream()
                    .collect(Collectors.groupingBy(MenuTranslation::getMenuId));
        } catch (Exception e) {
            // 如果查询翻译失败，使用空映射
            translationsByMenuId = new java.util.HashMap<>();
        }
        
        // 为每个菜单设置翻译
        for (Permission permission : permissions) {
            List<MenuTranslation> translations = translationsByMenuId.get(permission.getId());
            if (translations != null && !translations.isEmpty()) {
                permission.setTranslations(translations);
                // 设置本地化属性
                try {
                    permission.setLocalizedProperties(languageCode);
                } catch (Exception e) {
                    // 如果设置本地化属性失败，忽略，使用默认值
                }
            }
        }
    }
}
