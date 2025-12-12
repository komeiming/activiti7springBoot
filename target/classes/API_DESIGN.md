# 前后端分离架构API设计文档

## 1. 架构概述

### 1.1 技术栈
- **后端**: Spring Boot 3.2.5 + Activiti 7.1.0.M6 + MyBatis + Spring Security
- **前端**: Vue.js
- **数据交互**: RESTful API + JSON
- **认证**: JWT (JSON Web Token)

### 1.2 项目结构
```
后端项目：
- activiti7springBoot/
  - src/main/java/com/itheima/activiti/
    - config/          # 配置类
    - controller/      # REST控制器
    - service/         # 业务逻辑层
    - repository/      # 数据访问层
    - entity/          # 实体类
    - dto/             # 数据传输对象
    - exception/       # 异常处理
    - util/            # 工具类

前端项目：
- activiti-frontend/
  - src/
    - components/      # 组件
    - views/           # 页面
    - services/        # API服务
    - router/          # 路由
    - store/           # 状态管理
    - utils/           # 工具类
```

## 2. API接口规范

### 2.1 通用响应格式

所有API接口返回统一格式：

```json
{
  "code": 200,           // 状态码
  "message": "success", // 状态消息
  "data": {}           // 响应数据
}
```

### 2.2 状态码定义
- 200: 成功
- 400: 请求参数错误
- 401: 未授权
- 403: 禁止访问
- 404: 资源不存在
- 500: 服务器内部错误

### 2.3 接口命名规范
- GET: `/api/{resource}` - 获取资源列表
- GET: `/api/{resource}/{id}` - 获取单个资源
- POST: `/api/{resource}` - 创建资源
- PUT: `/api/{resource}/{id}` - 更新资源
- DELETE: `/api/{resource}/{id}` - 删除资源

## 3. 核心API接口设计

### 3.1 用户认证相关
- POST `/api/auth/login` - 用户登录
- POST `/api/auth/logout` - 用户登出
- GET `/api/auth/info` - 获取用户信息

### 3.2 流程管理相关
- GET `/api/processes` - 获取流程定义列表
- POST `/api/processes/deploy` - 部署流程
- GET `/api/processes/{processDefinitionId}` - 获取流程定义详情
- GET `/api/process-instances` - 获取流程实例列表
- POST `/api/process-instances` - 启动流程实例
- GET `/api/process-instances/{processInstanceId}` - 获取流程实例详情
- PUT `/api/process-instances/{processInstanceId}/suspend` - 挂起流程实例
- PUT `/api/process-instances/{processInstanceId}/activate` - 激活流程实例

### 3.3 任务管理相关
- GET `/api/tasks` - 获取任务列表
- GET `/api/tasks/{taskId}` - 获取任务详情
- POST `/api/tasks/{taskId}/complete` - 完成任务
- POST `/api/tasks/{taskId}/claim` - 认领任务
- POST `/api/tasks/{taskId}/unclaim` - 取消认领任务

### 3.4 员工入职流程相关
- POST `/api/onboard` - 提交员工入职申请
- GET `/api/onboard` - 获取入职申请列表
- GET `/api/onboard/{id}` - 获取入职申请详情
- PUT `/api/onboard/{id}` - 更新入职申请

## 4. 数据传输对象（DTO）设计

### 4.1 用户相关DTO
- LoginRequest
- LoginResponse
- UserInfoDTO

### 4.2 流程相关DTO
- ProcessDefinitionDTO
- ProcessInstanceDTO
- TaskDTO

### 4.3 员工入职相关DTO
- EmployeeOnboardRequest
- EmployeeOnboardResponse

## 5. 安全机制

### 5.1 认证机制
- 使用JWT进行身份认证
- Token有效期设置为24小时
- 支持Token刷新机制

### 5.2 授权机制
- 基于角色的访问控制（RBAC）
- 权限细粒度控制到API接口级别

### 5.3 CORS配置
- 配置跨域资源共享，允许前端应用访问API

## 6. 性能优化

### 6.1 缓存策略
- 使用Redis缓存热点数据
- 缓存流程定义和用户权限信息

### 6.2 数据库优化
- 使用索引优化查询性能
- 合理设计表结构，避免大表查询

## 7. 接口版本管理

- 在URL中包含版本信息，如 `/api/v1/{resource}`
- 支持多版本API共存