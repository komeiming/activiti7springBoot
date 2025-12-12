package com.itheima.activiti.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.itheima.activiti.dto.CommonResponse;
import com.itheima.activiti.dto.LoginRequest;
import com.itheima.activiti.dto.LoginResponse;
import com.itheima.activiti.dto.TenantLoginRequest;
import com.itheima.activiti.entity.SysUser;
import com.itheima.activiti.service.TenantService;
import com.itheima.activiti.service.UserService;
import com.itheima.activiti.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证控制器
 */
@RestController
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private TenantService tenantService;

    @Value("${jwt.expiration:3600}")
    private long expiration;

    /**
     * 用户登录（使用数据库验证）
     * 支持两种路径：/api/auth/login（直接访问）和/auth/login（前端代理后）
     */
    @PostMapping({"/api/auth/login", "/auth/login"})
    public CommonResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        logger.info("开始处理登录请求，用户名为: {}", loginRequest.getUsername());
        try {
            // 使用数据库进行用户验证
            SysUser user = userService.findByUsername(loginRequest.getUsername());
            
            // 检查用户是否存在
            if (user == null) {
                logger.warn("用户名不存在: {}", loginRequest.getUsername());
                return CommonResponse.unauthorized("用户名或密码错误");
            }
            
            // 检查用户状态
            if (!user.isEnabled()) {
                logger.warn("用户账号已被禁用: {}", loginRequest.getUsername());
                return CommonResponse.unauthorized("用户账号已被禁用");
            }
            
            String username = user.getUsername();
            
            // 检查密码是否匹配
            boolean passwordMatch = false;
            String passwordType = "未知";
            
            // 由于是演示系统，简化密码验证，允许使用固定密码"123456"登录
            if ("123456".equals(loginRequest.getPassword())) {
                passwordMatch = true;
                passwordType = "演示密码";
            }
            // 优先使用BCrypt密码验证（更安全）
            else if (user.getPassword() != null && (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$"))) {
                // 如果数据库中的密码是BCrypt加密的，使用matches方法验证
                passwordMatch = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
                passwordType = "BCrypt加密";
            }
            // 兼容原有明文密码的情况（为了平滑过渡）
            else if (loginRequest.getPassword().equals(user.getPassword())) {
                passwordMatch = true;
                passwordType = "明文密码";
                // 提示需要升级为加密密码
                logger.warn("用户 {} 仍在使用明文密码，建议升级为BCrypt加密", username);
            }
            
            if (!passwordMatch) {
                logger.warn("密码验证失败: {}", loginRequest.getUsername());
                return CommonResponse.unauthorized("用户名或密码错误");
            }
            // 收集用户的所有角色信息
            List<String> roleList = new ArrayList<>();
            String role = "ROLE_USER"; // 默认角色
            logger.info("用户 {} 登录时的角色数量: {}", username, user.getRoles() != null ? user.getRoles().size() : 0);
            
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                for (com.itheima.activiti.entity.SysRole r : user.getRoles()) {
                    String roleCode = r.getRoleCode();
                    roleList.add(roleCode);
                    logger.info("用户 {} 拥有的角色: ID={}, Code={}", username, r.getId(), roleCode);
                    
                    // 如果用户有部门经理角色，优先使用部门经理角色
                    if ("ROLE_MANAGER".equals(roleCode)) {
                        role = roleCode;
                        logger.info("用户 {} 具有部门经理角色，将使用该角色", username);
                    }
                }
                // 如果没有部门经理角色，才使用第一个角色
                if ("ROLE_USER".equals(role) && !roleList.isEmpty()) {
                    role = roleList.get(0);
                    logger.info("用户 {} 没有部门经理角色，使用第一个角色: {}", username, role);
                }
            } else {
                logger.info("用户 {} 没有分配任何角色，使用默认角色: {}", username, role);
                roleList.add("ROLE_USER");
            }
            
            // 将所有角色信息合并为逗号分隔的字符串
            String rolesStr = String.join(",", roleList);
            
            logger.info("用户 {} 最终使用的角色: {}", username, role);
            logger.debug("用户角色: {}, 选择使用的角色: {}", roleList, role);
            logger.info("用户 {} 的所有角色: {}", username, rolesStr);

            // 生成JWT token，包含用户的角色信息
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", username);
            claims.put("role", role); // 使用优先级最高的角色，已包含ROLE_前缀
            claims.put("roles", rolesStr); // 存入所有角色信息，用于权限验证
            
            // 使用JwtUtil生成JWT token
            logger.debug("开始生成JWT token...");
            String token = jwtUtil.generateToken(username, claims);
            logger.debug("JWT token生成成功，角色信息: {}, 所有角色: {}", role, rolesStr);

            // 构建返回数据
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUsername(username);
            response.setRole(role); // 使用从数据库获取的原始角色，已包含ROLE_前缀
            response.setExpireTime(System.currentTimeMillis() + (expiration * 1000)); // 转换为毫秒
            
            logger.info("用户 {} 使用{}登录成功，角色信息: {}", username, passwordType, role);
            
            // 返回标准的响应数据
            return CommonResponse.success(response);
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage());
            logger.error("异常详情: ", e);
            return CommonResponse.unauthorized("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/api/auth/logout")
    public CommonResponse<String> logout() {
        // 简化处理，直接返回成功
        return CommonResponse.success("登出成功");
    }

    /**
     * 租户登录
     */
    @PostMapping({"/api/auth/tenant/login", "/auth/tenant/login"})
    public CommonResponse<LoginResponse> tenantLogin(@RequestBody TenantLoginRequest loginRequest) {
        logger.info("开始处理租户登录请求，APP ID为: {}", loginRequest.getAppId());
        try {
            // 验证租户凭证，获取详细验证结果
            Object[] validateResult = tenantService.validateWithDetails(loginRequest.getAppId(), loginRequest.getSecretKey());
            boolean isValid = (boolean) validateResult[0];
            String errorMessage = (String) validateResult[1];
            
            if (!isValid) {
                logger.warn("租户登录失败，原因: {}, APP ID: {}", errorMessage, loginRequest.getAppId());
                return CommonResponse.unauthorized(errorMessage);
            }
            
            String appId = loginRequest.getAppId();
            
            // 获取完整的租户信息
            com.itheima.activiti.entity.Tenant tenant = tenantService.getByAppId(appId);
            String role = tenant.getRole();
            
            // 生成JWT token，包含租户信息
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", appId);
            claims.put("role", role);
            
            // 使用JwtUtil生成JWT token
            logger.debug("开始生成租户JWT token...");
            String token = jwtUtil.generateToken(appId, claims);
            logger.debug("租户JWT token生成成功");
            
            // 构建返回数据
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUsername(appId);
            response.setRole(role);
            response.setExpireTime(System.currentTimeMillis() + (expiration * 1000)); // 转换为毫秒
            response.setServiceModules(tenant.getServiceModules());
            response.setName(tenant.getSystemName());
            
            logger.info("租户登录成功，APP ID: {}, 服务模块: {}", appId, tenant.getServiceModules());
            
            // 返回标准的响应数据
            return CommonResponse.success(response);
        } catch (Exception e) {
            logger.error("租户登录失败: {}", e.getMessage());
            logger.error("异常详情: ", e);
            return CommonResponse.unauthorized("租户登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/api/auth/info")
    public CommonResponse<LoginResponse> info(HttpServletRequest request) {
        try {
            // 从请求头中获取token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return CommonResponse.unauthorized("未提供有效的认证信息");
            }
            
            String token = authHeader.substring(7);
            
            // 使用JwtUtil验证token并获取信息
            if (!jwtUtil.validateToken(token)) {
                return CommonResponse.unauthorized("token已过期或无效");
            }
            
            // 从token中获取用户信息
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return CommonResponse.unauthorized("无法从token中获取用户信息");
            }
            
            // 从数据库获取完整的用户信息
            SysUser user = userService.findByUsername(username);
            if (user == null) {
                return CommonResponse.unauthorized("用户不存在");
            }
            
            // 获取用户所有角色信息
            List<String> allRoles = new ArrayList<>();
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                user.getRoles().forEach(r -> allRoles.add(r.getRoleCode()));
            }
            
            // 主角色仍然使用第一个角色
            String role = allRoles.isEmpty() ? "ROLE_USER" : allRoles.get(0);
            
            // 构建响应对象
            LoginResponse response = new LoginResponse();
            response.setUsername(username);
            response.setRole(role); // 角色代码已包含ROLE_前缀
            response.setToken(token);
            response.setExpireTime(System.currentTimeMillis() + (expiration * 1000)); // 转换为毫秒
            
            logger.info("获取用户 {} 信息成功，角色: {}", username, role);
            
            // 返回标准的响应数据
            return CommonResponse.success(response);
        } catch (Exception e) {
            logger.error("获取用户信息失败: {}", e.getMessage());
            return CommonResponse.fail("获取用户信息失败: " + e.getMessage());
        }
    }
}