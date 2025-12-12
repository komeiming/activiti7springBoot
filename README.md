# Activiti7 Spring Boot 工作流管理系统

## 项目简介

本项目是基于 Spring Boot 3.2.5 + Activiti 7.1.0.M6 构建的企业级工作流管理系统，提供完整的工作流定义、部署、执行和监控功能。系统采用前后端分离架构，前端使用 Vue 3 + Element Plus + Vite 5 开发，后端基于 Java Spring Boot 框架，集成 Activiti 7 工作流引擎。

## 技术栈

### 后端
- **Spring Boot**: 3.2.5
- **Activiti**: 7.1.0.M6
- **MyBatis**: ORM 框架
- **H2 Database**: 内嵌式数据库（开发环境）
- **MySQL**: 关系型数据库（生产环境）
- **JWT**: 认证授权
- **Swagger/OpenAPI**: API 文档
- **Spring Security**: 安全框架

### 前端
- **Vue**: 3
- **Element Plus**: UI 组件库
- **Vite**: 5 构建工具
- **Axios**: HTTP 客户端
- **Vue Router**: 路由管理
- **Pinia**: 状态管理

## 项目结构

```
activiti7springBoot/
├── activiti7springBoot/        # 后端代码
│   ├── src/main/java/com/itheima/activiti/  # 后端主代码
│   │   ├── common/             # 公共组件
│   │   ├── config/             # 配置类
│   │   ├── controller/         # 控制器
│   │   ├── dto/                # 数据传输对象
│   │   ├── entity/             # 实体类
│   │   ├── exception/          # 异常处理
│   │   ├── listener/           # 监听器
│   │   ├── mapper/             # Mapper 接口
│   │   ├── service/            # 服务层
│   │   └── util/               # 工具类
│   ├── src/main/resources/     # 资源文件
│   │   ├── mapper/             # MyBatis 映射文件
│   │   ├── processes/          # BPMN 流程定义文件
│   │   └── application.properties # 配置文件
│   └── pom.xml                 # Maven 配置
├── frontend/                   # 前端代码
│   ├── src/                    # 前端主代码
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 组件
│   │   ├── router/             # 路由
│   │   ├── services/           # API 服务
│   │   ├── utils/              # 工具类
│   │   ├── views/              # 页面
│   │   ├── App.vue             # 根组件
│   │   └── main.js             # 入口文件
│   ├── index.html              # HTML 模板
│   ├── package.json            # npm 配置
│   └── vite.config.js          # Vite 配置
└── README.md                   # 项目说明文档
```

## 主要功能

### 工作流管理
- 流程定义管理：上传、部署、查看、删除流程定义
- 流程实例管理：启动、挂起、激活、终止流程实例
- 任务管理：待办任务、已办任务、任务审批
- 流程历史：查看流程实例历史记录、活动历史
- 流程变量：设置和获取流程变量

### 权限管理
- 用户管理：用户增删改查
- 角色管理：角色增删改查、角色权限分配
- 权限管理：权限增删改查
- 菜单管理：动态菜单配置

### 通知管理
- 通知模板管理
- 通知发送记录
- 待发送通知管理

### 多租户支持
- 租户管理
- 租户初始化
- 租户隔离

### 监控管理
- API 调用日志
- 系统操作日志
- 工作流执行日志

## 快速开始

### 环境要求
- Java 17+
- Node.js 18+
- Maven 3.6+
- MySQL 8.0+（可选，默认使用 H2）

### 后端启动

1. 进入后端目录：
   ```bash
   cd activiti7springBoot
   ```

2. 编译打包：
   ```bash
   mvn clean package -DskipTests
   ```

3. 启动服务：
   ```bash
   java -jar target/activiti7springboot-1.0-SNAPSHOT.jar
   ```

4. 访问 API 文档：
   ```
   http://localhost:8080/swagger-ui.html
   ```

### 前端启动

1. 进入前端目录：
   ```bash
   cd frontend
   ```

2. 安装依赖：
   ```bash
   npm install
   ```

3. 启动开发服务器：
   ```bash
   npm run dev
   ```

4. 访问前端应用：
   ```
   http://localhost:5173
   ```

## 部署说明

### 后端部署

1. 编译打包：
   ```bash
   mvn clean package -DskipTests
   ```

2. 部署到生产环境：
   ```bash
   java -jar activiti7springboot-1.0-SNAPSHOT.jar --spring.profiles.active=prod
   ```

### 前端部署

1. 构建生产版本：
   ```bash
   npm run build
   ```

2. 将 `dist` 目录下的文件部署到 Nginx 或其他 Web 服务器。

## 配置说明

### 后端配置

主要配置文件：`activiti7springBoot/src/main/resources/application.properties`

关键配置项：

- 数据库配置
- JWT 配置
- Activiti 配置
- 跨域配置

### 前端配置

主要配置文件：`frontend/.env`

关键配置项：

- API 基础 URL
- JWT 配置
- 语言配置

## 开发说明

### 流程定义开发

1. 在 `activiti7springBoot/src/main/resources/processes/` 目录下创建 BPMN 流程定义文件
2. 流程定义中使用 `${assignee}` 或 `${manager}` 等表达式指定任务处理人
3. 启动服务时会自动部署流程定义

### 新增功能开发

1. 后端：
   - 创建实体类
   - 创建 Mapper 接口和映射文件
   - 创建 Service 接口和实现
   - 创建 Controller 类

2. 前端：
   - 创建页面组件
   - 配置路由
   - 创建 API 服务
   - 添加状态管理（如果需要）

## 许可证

MIT License

## 联系方式

如有问题，请联系项目维护人员。
