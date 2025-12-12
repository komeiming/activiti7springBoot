package com.itheima.activiti.controller;

import com.itheima.activiti.dto.CommonResponse;
import com.itheima.activiti.entity.SysRole;
import com.itheima.activiti.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 查询所有角色
     */
    @GetMapping
    public CommonResponse<List<SysRole>> findAll() {
        try {
            List<SysRole> roles = roleMapper.findAll();
            return CommonResponse.success(roles);
        } catch (Exception e) {
            return CommonResponse.fail("查询角色列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询角色
     */
    @GetMapping("/{id}")
    public CommonResponse<SysRole> findById(@PathVariable Long id) {
        try {
            SysRole role = roleMapper.findById(id);
            if (role == null) {
                return CommonResponse.notFound("角色不存在");
            }
            return CommonResponse.success(role);
        } catch (Exception e) {
            return CommonResponse.fail("查询角色失败: " + e.getMessage());
        }
    }

    /**
     * 根据角色编码查询角色
     */
    @GetMapping("/by-code/{roleCode}")
    public CommonResponse<SysRole> findByRoleCode(@PathVariable String roleCode) {
        try {
            SysRole role = roleMapper.findByRoleCode(roleCode);
            if (role == null) {
                return CommonResponse.notFound("角色不存在");
            }
            return CommonResponse.success(role);
        } catch (Exception e) {
            return CommonResponse.fail("查询角色失败: " + e.getMessage());
        }
    }

    /**
     * 新增角色
     */
    @PostMapping
    public CommonResponse<String> addRole(@RequestBody SysRole role) {
        try {
            // 检查角色编码是否已存在
            SysRole existingRole = roleMapper.findByRoleCode(role.getRoleCode());
            if (existingRole != null) {
                return CommonResponse.fail("角色编码已存在");
            }
            
            int result = roleMapper.insert(role);
            if (result > 0) {
                return CommonResponse.success("角色创建成功");
            } else {
                return CommonResponse.fail("角色创建失败");
            }
        } catch (Exception e) {
            return CommonResponse.fail("创建角色失败: " + e.getMessage());
        }
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public CommonResponse<String> updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        try {
            // 确保更新的是指定ID的角色
            role.setId(id);
            
            // 检查角色编码是否与其他角色重复
            SysRole existingRole = roleMapper.findByRoleCode(role.getRoleCode());
            if (existingRole != null && !existingRole.getId().equals(id)) {
                return CommonResponse.fail("角色编码已存在");
            }
            
            int result = roleMapper.update(role);
            if (result > 0) {
                return CommonResponse.success("角色更新成功");
            } else {
                return CommonResponse.fail("角色更新失败");
            }
        } catch (Exception e) {
            return CommonResponse.fail("更新角色失败: " + e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public CommonResponse<String> deleteRole(@PathVariable Long id) {
        try {
            // 检查角色是否存在
            SysRole role = roleMapper.findById(id);
            if (role == null) {
                return CommonResponse.notFound("角色不存在");
            }
            
            // 防止删除系统关键角色
            if ("ROLE_ADMIN".equals(role.getRoleCode())) {
                return CommonResponse.fail("无法删除管理员角色");
            }
            
            int result = roleMapper.delete(id);
            if (result > 0) {
                return CommonResponse.success("角色删除成功");
            } else {
                return CommonResponse.fail("角色删除失败");
            }
        } catch (Exception e) {
            return CommonResponse.fail("删除角色失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询用户拥有的角色
     */
    @GetMapping("/by-user/{userId}")
    public CommonResponse<List<SysRole>> findByUserId(@PathVariable Long userId) {
        try {
            List<SysRole> roles = roleMapper.findByUserId(userId);
            return CommonResponse.success(roles);
        } catch (Exception e) {
            return CommonResponse.fail("查询用户角色失败: " + e.getMessage());
        }
    }
}