-- 零售门店业务管理平台初始化脚本
-- 整合所有表的创建和初始化语句

-- ===============================
-- 1. 删除所有表（按依赖关系逆序删除）
-- ===============================
DROP TABLE IF EXISTS api_call_log;
DROP TABLE IF EXISTS workflow_execution_log;
DROP TABLE IF EXISTS workflow_instance;
DROP TABLE IF EXISTS workflow_template;
DROP TABLE IF EXISTS sys_menu_operation_log;
DROP TABLE IF EXISTS sys_menu_translation;
DROP TABLE IF EXISTS sys_language;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS pending_notification;
DROP TABLE IF EXISTS notification_log;
DROP TABLE IF EXISTS template_operation_log;
DROP TABLE IF EXISTS notification_template;
DROP TABLE IF EXISTS employee_onboard;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS tenant;

-- ===============================
-- 2. 创建表（按依赖关系顺序创建）
-- ===============================

-- 2.1 基础表（无外键依赖）
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE sys_language (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    department VARCHAR(50),
    position VARCHAR(50),
    status TINYINT DEFAULT 1,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2.2 权限和菜单表
CREATE TABLE sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    url VARCHAR(200),
    method VARCHAR(20),
    description VARCHAR(200),
    parent_id BIGINT DEFAULT 0,
    menu_type VARCHAR(20),
    menu_order INT DEFAULT 0,
    icon VARCHAR(50),
    path VARCHAR(200),
    component VARCHAR(200),
    redirect VARCHAR(200),
    hidden BOOLEAN DEFAULT FALSE,
    always_show BOOLEAN DEFAULT FALSE,
    affix BOOLEAN DEFAULT FALSE,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2.3 关联表
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
);

CREATE TABLE sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
);

CREATE TABLE sys_menu_translation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_id BIGINT NOT NULL,
    language_code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (menu_id) REFERENCES sys_permission(id) ON DELETE CASCADE
);

-- 2.4 租户表
CREATE TABLE tenant (
    id VARCHAR(36) PRIMARY KEY COMMENT '租户ID',
    app_id VARCHAR(64) UNIQUE NOT NULL COMMENT 'APP ID',
    secret_key VARCHAR(128) NOT NULL COMMENT '密钥',
    system_name VARCHAR(128) NOT NULL COMMENT '系统名称',
    contact_name VARCHAR(64) NOT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(32) COMMENT '联系人手机号',
    contact_email VARCHAR(128) NOT NULL COMMENT '联系人邮箱',
    enterprise_name VARCHAR(256) COMMENT '企业名称',
    enterprise_credit_code VARCHAR(64) COMMENT '企业统一社会信用代码',
    callback_url VARCHAR(256) COMMENT '回调地址',
    service_modules VARCHAR(128) NOT NULL COMMENT '所需服务模块',
    call_scenarios VARCHAR(256) NOT NULL COMMENT '调用场景',
    status VARCHAR(32) NOT NULL COMMENT '状态(pending/approved/suspended/cancelled)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    approved_at TIMESTAMP COMMENT '审核通过时间',
    approved_by VARCHAR(64) COMMENT '审核人',
    remark VARCHAR(512) COMMENT '备注',
    role VARCHAR(64) NOT NULL DEFAULT 'ROLE_USER' COMMENT '租户角色'
);

-- 2.5 菜单操作日志表
CREATE TABLE sys_menu_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_id BIGINT,
    operation_type VARCHAR(20) NOT NULL,
    operator VARCHAR(50) NOT NULL,
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    old_data TEXT,
    new_data TEXT,
    description VARCHAR(200),
    ip_address VARCHAR(50),
    user_agent VARCHAR(200)
);

-- 2.6 通知相关表
CREATE TABLE notification_template (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '模板ID',
    tenant_id VARCHAR(36) COMMENT '租户ID',
    name VARCHAR(255) NOT NULL COMMENT '模板名称',
    code VARCHAR(50) NOT NULL COMMENT '模板编码',
    type VARCHAR(20) NOT NULL COMMENT '模板类型',
    status VARCHAR(20) NOT NULL DEFAULT 'enabled' COMMENT '状态',
    sender VARCHAR(255) COMMENT '发送者',
    subject VARCHAR(255) COMMENT '邮件主题模板',
    content TEXT NOT NULL COMMENT '模板内容',
    reply_to VARCHAR(255) COMMENT '回复邮箱',
    signature VARCHAR(100) COMMENT '短信签名标识',
    description TEXT COMMENT '模板描述',
    created_by VARCHAR(100) COMMENT '创建人',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(100) COMMENT '更新人',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

CREATE TABLE notification_log (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '日志ID',
    tenant_id VARCHAR(36) COMMENT '租户ID',
    send_id VARCHAR(36) NOT NULL COMMENT '发送ID',
    template_id VARCHAR(36) NOT NULL COMMENT '模板ID',
    template_name VARCHAR(255) COMMENT '模板名称',
    type VARCHAR(20) NOT NULL COMMENT '通知类型',
    receiver VARCHAR(255) NOT NULL COMMENT '接收者',
    params TEXT COMMENT '参数替换数据',
    sender_system VARCHAR(100) COMMENT '调用系统标识',
    request_id VARCHAR(36) COMMENT '请求ID',
    success BOOLEAN NOT NULL COMMENT '发送是否成功',
    error_code VARCHAR(50) COMMENT '错误代码',
    error_message VARCHAR(500) COMMENT '错误信息',
    send_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间'
);

CREATE TABLE template_operation_log (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '日志ID',
    tenant_id VARCHAR(36) COMMENT '租户ID',
    template_id VARCHAR(36) NOT NULL COMMENT '模板ID',
    template_name VARCHAR(255) COMMENT '模板名称',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型',
    operator VARCHAR(100) COMMENT '操作人',
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    operation_details TEXT COMMENT '操作详情'
);

CREATE TABLE pending_notification (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '待发送ID',
    tenant_id VARCHAR(36) COMMENT '租户ID',
    template_id VARCHAR(36) NOT NULL COMMENT '模板ID',
    template_name VARCHAR(255) COMMENT '模板名称',
    type VARCHAR(20) NOT NULL COMMENT '通知类型',
    receiver VARCHAR(255) NOT NULL COMMENT '接收者',
    subject VARCHAR(255) COMMENT '邮件主题',
    content TEXT NOT NULL COMMENT '通知内容',
    params TEXT COMMENT '参数替换数据',
    sender_system VARCHAR(100) COMMENT '调用系统标识',
    request_id VARCHAR(36) COMMENT '请求ID',
    priority INT DEFAULT 0 COMMENT '优先级',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retries INT DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time TIMESTAMP COMMENT '下次重试时间',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 2.7 工作流相关表
CREATE TABLE workflow_template (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '模板ID',
    tenant_id VARCHAR(36) NOT NULL COMMENT '租户ID',
    name VARCHAR(255) NOT NULL COMMENT '模板名称',
    description TEXT COMMENT '模板描述',
    process_json TEXT NOT NULL COMMENT '流程JSON定义',
    status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '模板状态',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(36) COMMENT '创建人',
    updated_by VARCHAR(36) COMMENT '更新人'
);

CREATE TABLE workflow_instance (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '实例ID',
    template_id VARCHAR(36) NOT NULL COMMENT '模板ID',
    tenant_id VARCHAR(36) NOT NULL COMMENT '租户ID',
    app_id VARCHAR(36) COMMENT '应用ID',
    status VARCHAR(20) NOT NULL DEFAULT 'running' COMMENT '实例状态',
    current_node VARCHAR(36) COMMENT '当前节点ID',
    variables TEXT COMMENT '流程变量',
    callback_url VARCHAR(255) COMMENT '回调地址',
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '启动时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    created_by VARCHAR(36) COMMENT '创建人'
);

CREATE TABLE workflow_execution_log (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '日志ID',
    instance_id VARCHAR(36) NOT NULL COMMENT '实例ID',
    tenant_id VARCHAR(36) NOT NULL COMMENT '租户ID',
    node_id VARCHAR(36) NOT NULL COMMENT '节点ID',
    node_name VARCHAR(255) NOT NULL COMMENT '节点名称',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型',
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    operation_result TEXT COMMENT '操作结果',
    error_message TEXT COMMENT '错误信息',
    operator VARCHAR(36) COMMENT '操作人'
);

CREATE TABLE api_call_log (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '日志ID',
    app_id VARCHAR(36) COMMENT '应用ID',
    tenant_id VARCHAR(36) COMMENT '租户ID',
    api_path VARCHAR(255) NOT NULL COMMENT 'API路径',
    request_method VARCHAR(10) NOT NULL COMMENT '请求方法',
    request_params TEXT COMMENT '请求参数',
    response_data TEXT COMMENT '响应数据',
    response_time BIGINT NOT NULL COMMENT '响应时间',
    success BOOLEAN NOT NULL COMMENT '是否成功',
    error_code VARCHAR(20) COMMENT '错误代码',
    error_message TEXT COMMENT '错误信息',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    call_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间'
);

-- 2.8 员工入职申请表
CREATE TABLE employee_onboard (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    process_instance_id VARCHAR(100),
    employee_name VARCHAR(100) NOT NULL,
    employee_id VARCHAR(50) NOT NULL,
    department VARCHAR(50) NOT NULL,
    position VARCHAR(50) NOT NULL,
    hire_date DATE NOT NULL,
    manager_name VARCHAR(100),
    manager_id VARCHAR(50),
    it_equipment_status VARCHAR(20) DEFAULT 'PENDING',
    hr_document_status VARCHAR(20) DEFAULT 'PENDING',
    training_status VARCHAR(20) DEFAULT 'PENDING',
    status VARCHAR(20) DEFAULT 'SUBMITTED',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    hr_id VARCHAR(255),
    hr_name VARCHAR(255)
);

-- ===============================
-- 2. 创建索引
-- ===============================

-- 2.1 租户表索引
CREATE INDEX IF NOT EXISTS idx_app_id ON tenant(app_id);
CREATE INDEX IF NOT EXISTS idx_status ON tenant(status);
CREATE INDEX IF NOT EXISTS idx_contact_phone ON tenant(contact_phone);
CREATE INDEX IF NOT EXISTS idx_contact_email ON tenant(contact_email);

-- 2.2 通知模板表索引
CREATE INDEX IF NOT EXISTS idx_type ON notification_template(type);
CREATE INDEX IF NOT EXISTS idx_status ON notification_template(status);
CREATE INDEX IF NOT EXISTS idx_name ON notification_template(name);
CREATE INDEX IF NOT EXISTS idx_notification_template_tenant_id ON notification_template(tenant_id);

-- 2.3 通知日志表索引
CREATE INDEX IF NOT EXISTS idx_send_id ON notification_log(send_id);
CREATE INDEX IF NOT EXISTS idx_template_id ON notification_log(template_id);
CREATE INDEX IF NOT EXISTS idx_success ON notification_log(success);
CREATE INDEX IF NOT EXISTS idx_send_time ON notification_log(send_time);
CREATE INDEX IF NOT EXISTS idx_notification_log_tenant_id ON notification_log(tenant_id);

-- 2.4 模板操作日志表索引
CREATE INDEX IF NOT EXISTS idx_template_id ON template_operation_log(template_id);
CREATE INDEX IF NOT EXISTS idx_operation_type ON template_operation_log(operation_type);
CREATE INDEX IF NOT EXISTS idx_operation_time ON template_operation_log(operation_time);
CREATE INDEX IF NOT EXISTS idx_template_operation_log_tenant_id ON template_operation_log(tenant_id);

-- 2.5 待发送通知表索引
CREATE INDEX IF NOT EXISTS idx_status ON pending_notification(status);
CREATE INDEX IF NOT EXISTS idx_type ON pending_notification(type);
CREATE INDEX IF NOT EXISTS idx_next_retry_time ON pending_notification(next_retry_time);
CREATE INDEX IF NOT EXISTS idx_created_time ON pending_notification(created_time);
CREATE INDEX IF NOT EXISTS idx_pending_notification_tenant_id ON pending_notification(tenant_id);

-- ===============================
-- 3. 插入初始化数据
-- ===============================

-- 3.1 插入角色数据
INSERT INTO sys_role (name, code, description) VALUES
('系统管理员', 'ROLE_ADMIN', '系统管理员，拥有最高权限'),
('CEO', 'ROLE_CEO', '首席执行官'),
('CTO', 'ROLE_CTO', '首席技术官'),
('CFO', 'ROLE_CFO', '首席财务官'),
('COO', 'ROLE_COO', '首席运营官'),
('部门经理', 'ROLE_MANAGER', '部门经理'),
('开发经理', 'ROLE_DEV_MANAGER', '开发部门经理'),
('测试经理', 'ROLE_TEST_MANAGER', '测试部门经理'),
('运维经理', 'ROLE_OPS_MANAGER', '运维部门经理'),
('销售经理', 'ROLE_SALES_MANAGER', '销售部门经理'),
('市场经理', 'ROLE_MARKET_MANAGER', '市场部门经理'),
('HR经理', 'ROLE_HR_MANAGER', 'HR部门经理'),
('财务经理', 'ROLE_FINANCE_MANAGER', '财务部门经理'),
('HR专员', 'ROLE_HR', '人力资源专员'),
('IT管理员', 'ROLE_IT', 'IT管理员'),
('高级架构师', 'ROLE_SENIOR_ARCHITECT', '高级架构师'),
('开发工程师', 'ROLE_DEVELOPER', '开发工程师'),
('测试工程师', 'ROLE_TESTER', '测试工程师'),
('运维工程师', 'ROLE_OPS', '运维工程师'),
('销售代表', 'ROLE_SALES', '销售代表'),
('市场专员', 'ROLE_MARKET', '市场专员'),
('财务专员', 'ROLE_FINANCE', '财务专员'),
('行政专员', 'ROLE_ADMINISTRATIVE', '行政专员'),
('普通员工', 'ROLE_USER', '普通员工'),
-- 第三方系统集成相关角色
('第三方系统管理员', 'ROLE_THIRD_PARTY_ADMIN', '管理第三方系统集成配置、权限等'),
('通知服务用户', 'ROLE_NOTIFICATION_SERVICE', '访问通知模块API，包括模板管理、通知发送等'),
('流程服务用户', 'ROLE_PROCESS_SERVICE', '访问流程部署模块API，包括流程部署、查询等'),
('综合服务用户', 'ROLE_INTEGRATED_SERVICE', '同时访问通知和流程服务API');

-- 3.2 插入语言数据
INSERT INTO sys_language (code, name, is_default) VALUES
('zh-CN', '中文', TRUE),
('en-US', 'English', FALSE);

-- 3.3 插入权限和菜单
-- 重置自增序列
ALTER TABLE sys_permission ALTER COLUMN id RESTART WITH 1;

-- 插入根目录和菜单项
INSERT INTO sys_permission (id, name, code, url, method, description, parent_id, menu_type, menu_order, icon, path, component, redirect, hidden, always_show, affix) VALUES
-- 1. 仪表盘 (根菜单项)
(1, '仪表盘', 'dashboard', '/dashboard', 'GET', '系统仪表盘', 0, 'menu', 1, 'DataLine', '/dashboard', 'views/DashboardView.vue', null, FALSE, FALSE, TRUE),

-- 2. 任务管理 (目录)
(2, '任务管理', 'task:manage', null, null, '任务管理', 0, 'directory', 2, 'Ticket', '/tasks', null, null, FALSE, FALSE, FALSE),
  -- 2.1 我的待办 (任务管理的子菜单)
  (3, '我的待办', 'task:my', '/api/tasks', 'GET', '查看分配给自己的任务', 2, 'menu', 1, 'DocumentCopy', '/enhanced-task-list', 'views/EnhancedTaskList.vue', null, FALSE, FALSE, FALSE),
  -- 2.2 已办任务 (任务管理的子菜单)
  (4, '已办任务', 'task:completed', '/api/tasks/completed', 'GET', '查看已完成的任务', 2, 'menu', 2, 'Check', '/completed-tasks', 'views/CompletedTasksView.vue', null, FALSE, FALSE, FALSE),

-- 3. 系统管理 (目录)
(5, '系统管理', 'system:manage', null, null, '系统管理', 0, 'directory', 3, 'Setting', '/system', null, null, FALSE, FALSE, FALSE),
  -- 3.1 用户管理 (系统管理的子菜单)
  (6, '用户管理', 'user:manage', '/api/users', 'GET', '用户管理', 5, 'menu', 1, 'User', '/user-management', 'views/UserManagement.vue', null, FALSE, FALSE, FALSE),
  -- 3.2 角色管理 (系统管理的子菜单)
  (7, '角色管理', 'role:manage', '/api/roles', 'GET', '角色管理', 5, 'menu', 2, 'Avatar', '/role-management', 'views/RoleManagement.vue', null, FALSE, FALSE, FALSE),
  -- 3.3 角色菜单权限 (系统管理的子菜单)
  (8, '角色菜单权限', 'role:menu:permission', '/api/roles/{roleId}/menus', 'GET', '角色菜单权限配置', 5, 'menu', 3, 'Grid', '/role-menu-permission', 'views/RoleMenuPermission.vue', null, FALSE, FALSE, FALSE),
  -- 3.4 菜单管理 (系统管理的子菜单)
  (9, '菜单管理', 'menu:manage', '/api/menus', 'GET', '菜单管理', 5, 'menu', 4, 'Grid', '/menu-management', 'views/MenuManagement.vue', null, FALSE, FALSE, FALSE),
  -- 3.5 多语言配置 (系统管理的子菜单)
  (10, '多语言配置', 'language:manage', '/api/languages', 'GET', '多语言配置', 5, 'menu', 5, 'Translate', '/language-management', 'views/LanguageManagement.vue', null, FALSE, FALSE, FALSE),
  -- 3.6 通知模板管理 (系统管理的子菜单)
  (11, '通知模板管理', 'notification:template:manage', '/api/notification-templates', 'GET', '通知模板管理', 5, 'menu', 6, 'Message', '/notification-templates', 'views/NotificationTemplateManagement.vue', null, FALSE, FALSE, FALSE),
  -- 3.7 通知测试 (系统管理的子菜单)
  (12, '通知测试', 'notification:test', '/api/notification-templates/test', 'POST', '通知测试', 5, 'menu', 7, 'ChatRound', '/notification-templates-test', 'views/NotificationTestView.vue', null, FALSE, FALSE, FALSE),
  -- 3.8 通知日志 (系统管理的子菜单)
  (13, '通知日志', 'notification:log', '/api/notification-logs', 'GET', '通知日志', 5, 'menu', 8, 'Document', '/notification-logs', 'views/NotificationLogView.vue', null, FALSE, FALSE, FALSE),
  -- 3.9 待发送通知 (系统管理的子菜单)
  (14, '待发送通知', 'notification:pending', '/api/pending-notifications', 'GET', '待发送通知', 5, 'menu', 9, 'Clock', '/pending-notifications', 'views/PendingNotificationView.vue', null, FALSE, FALSE, FALSE),
  -- 3.10 租户审批 (系统管理的子菜单)
  (29, '租户审批', 'tenant:approval', '/api/admin/tenant/list', 'GET', '租户审批管理', 5, 'menu', 10, 'Avatar', '/tenant-approval', 'views/TenantApprovalView.vue', null, FALSE, FALSE, FALSE),

-- 4. 申请管理 (目录)
(15, '申请管理', 'apply:manage', null, null, '申请管理', 0, 'directory', 4, 'List', '/apply', null, null, FALSE, FALSE, FALSE),
  -- 4.1 请假申请 (申请管理的子菜单)
  (16, '请假申请', 'leave:apply', '/api/process-instances/leave', 'POST', '发起请假申请', 15, 'menu', 1, 'Calendar', '/leave-application', 'views/LeaveApplicationView.vue', null, FALSE, FALSE, FALSE),
  -- 4.2 入职申请 (申请管理的子菜单)
  (17, '入职申请', 'onboard:apply', '/api/process-instances/onboard', 'POST', '发起入职申请', 15, 'menu', 2, 'UserFilled', '/onboarding-application', 'views/OnboardingApplicationView.vue', null, FALSE, FALSE, FALSE),

-- 5. 流程管理 (目录)
(18, '流程管理', 'process:manage', null, null, '流程管理', 0, 'directory', 5, 'Setting', '/process', null, null, FALSE, FALSE, FALSE),
  -- 5.1 我的流程定义 (流程管理的子菜单)
  (19, '我的流程定义', 'process:definition:my', '/api/process-definitions/my', 'GET', '我的流程定义', 18, 'menu', 1, 'Setting', '/user-process-definition', 'views/UserProcessDefinitionManagement.vue', null, FALSE, FALSE, FALSE),
  -- 5.2 流程申请 (流程管理的子菜单)
  (20, '流程申请', 'process:apply', '/api/process-instances', 'POST', '发起新流程', 18, 'menu', 2, 'List', '/generic-process-apply', 'views/GenericProcessApply.vue', null, FALSE, FALSE, FALSE),
  -- 5.3 流程历史 (流程管理的子菜单)
  (21, '流程历史', 'process:history', '/api/process-instances/{id}/history', 'GET', '查看流程执行历史', 18, 'menu', 3, 'View', '/process-history', 'views/ProcessHistory.vue', null, FALSE, FALSE, FALSE),
  -- 5.4 审批中心 (流程管理的子菜单)
  (22, '审批中心', 'task:approval', '/api/tasks/approval', 'GET', '审批中心', 18, 'menu', 4, 'Check', '/approval-center', 'views/ApprovalCenter.vue', null, FALSE, FALSE, FALSE),

-- 6. 监控与日志 (目录)
(30, '监控与日志', 'monitor:manage', null, null, '监控与日志', 0, 'directory', 6, 'Monitor', '/monitor', null, null, FALSE, FALSE, FALSE),
  -- 6.1 租户使用统计 (监控与日志的子菜单)
  (31, '租户使用统计', 'monitor:tenant:stats', '/api/monitor/tenant-stats', 'GET', '查看各租户使用统计数据', 30, 'menu', 1, 'TrendCharts', '/tenant-stats', 'views/monitor/TenantStatsView.vue', null, FALSE, FALSE, FALSE),
  -- 6.2 API调用统计 (监控与日志的子菜单)
  (32, 'API调用统计', 'monitor:api:stats', '/api/monitor/api-stats', 'GET', '查看API调用统计数据', 30, 'menu', 2, 'DataAnalysis', '/api-stats', 'views/monitor/ApiStatsView.vue', null, FALSE, FALSE, FALSE),
  -- 6.3 系统操作日志 (监控与日志的子菜单)
  (33, '系统操作日志', 'monitor:system:logs', '/api/monitor/system-logs', 'GET', '查看系统操作日志', 30, 'menu', 3, 'Document', '/system-logs', 'views/monitor/SystemLogsView.vue', null, FALSE, FALSE, FALSE),
  -- 6.4 告警管理 (监控与日志的子菜单)
  (34, '告警管理', 'monitor:alerts', '/api/monitor/alerts', 'GET', '管理系统告警', 30, 'menu', 4, 'Warning', '/alerts', 'views/monitor/AlertManagementView.vue', null, FALSE, FALSE, FALSE);

-- 插入按钮权限
INSERT INTO sys_permission (id, name, code, url, method, description, parent_id, menu_type, menu_order, icon, path, component, redirect, hidden, always_show, affix) VALUES
-- 用户管理按钮 (parent_id=6，指向用户管理菜单)
(23, '创建用户', 'user:create', '/api/users', 'POST', '创建新用户', 6, 'button', 1, null, null, null, null, TRUE, FALSE, FALSE),
(24, '编辑用户', 'user:update', '/api/users/{id}', 'PUT', '编辑用户信息', 6, 'button', 2, null, null, null, null, TRUE, FALSE, FALSE),
(25, '删除用户', 'user:delete', '/api/users/{id}', 'DELETE', '删除用户', 6, 'button', 3, null, null, null, null, TRUE, FALSE, FALSE),
-- 菜单管理按钮 (parent_id=9，指向菜单管理菜单)
(26, '创建菜单', 'menu:create', '/api/menus', 'POST', '创建新菜单', 9, 'button', 1, null, null, null, null, TRUE, FALSE, FALSE),
(27, '编辑菜单', 'menu:update', '/api/menus/{id}', 'PUT', '编辑菜单信息', 9, 'button', 2, null, null, null, null, TRUE, FALSE, FALSE),
(28, '删除菜单', 'menu:delete', '/api/menus/{id}', 'DELETE', '删除菜单', 9, 'button', 3, null, null, null, null, TRUE, FALSE, FALSE);

-- 重置自增序列为下一个可用值 (35 = 34个菜单项 + 6个按钮权限)
ALTER TABLE sys_permission ALTER COLUMN id RESTART WITH 35;

-- 3.4 插入管理员和高管用户（密码统一为：123456，使用BCrypt加密）
INSERT INTO sys_user (username, password, full_name, email, phone, department, position, status) VALUES
('admin', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '系统管理员', 'admin@example.com', '13800138000', 'IT部门', '系统管理员', 1),
('ceo', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '约翰·史密斯', 'ceo@example.com', '13800138001', '高管', '首席执行官', 1),
('cto', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '艾米丽·王', 'cto@example.com', '13800138002', '技术部', '首席技术官', 1),
('cfo', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '大卫·李', 'cfo@example.com', '13800138003', '财务部', '首席财务官', 1),
('coo', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '莎拉·陈', 'coo@example.com', '13800138004', '运营部', '首席运营官', 1),
('hr_manager', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '丽莎·布朗', 'hrmanager@example.com', '13800138005', '人力资源部', 'HR经理', 1),
('dev_manager', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '迈克尔·约翰逊', 'devmanager@example.com', '13800138006', '开发部', '开发经理', 1),
('test_manager', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '杰西卡·戴维斯', 'testmanager@example.com', '13800138007', '测试部', '测试经理', 1),
('ops_manager', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '罗伯特·威尔逊', 'opsmanager@example.com', '13800138008', '运维部', '运维经理', 1),
('sales_manager', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '帕特里夏·泰勒', 'salesmanager@example.com', '13800138009', '销售部', '销售经理', 1),
('market_manager', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '丹尼尔·安德森', 'marketmanager@example.com', '13800138010', '市场部', '市场经理', 1),
('finance_manager', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '南希·托马斯', 'financemanager@example.com', '13800138011', '财务部', '财务经理', 1);

-- 3.5 插入普通员工用户（密码统一为：123456，使用BCrypt加密）
INSERT INTO sys_user (username, password, full_name, email, phone, department, position, status) VALUES
('hr', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '玛丽·克拉克', 'hr@example.com', '13800138012', '人力资源部', 'HR专员', 1),
('it_admin', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '保罗·刘易斯', 'it_admin@example.com', '13800138013', 'IT部门', 'IT管理员', 1),
('senior_arch', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '琳达·沃克', 'senior_arch@example.com', '13800138014', '技术部', '高级架构师', 1),
('dev1', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '理查德·霍尔', 'dev1@example.com', '13800138015', '开发部', '开发工程师', 1),
('dev2', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '芭芭拉·艾伦', 'dev2@example.com', '13800138016', '开发部', '开发工程师', 1),
('test1', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '凯文·杨', 'test1@example.com', '13800138017', '测试部', '测试工程师', 1),
('test2', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '伊丽莎白·金', 'test2@example.com', '13800138018', '测试部', '测试工程师', 1),
('ops1', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '詹妮弗·斯科特', 'ops1@example.com', '13800138019', '运维部', '运维工程师', 1),
('ops2', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '杰森·格林', 'ops2@example.com', '13800138020', '运维部', '运维工程师', 1),
('sales1', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '马修·亚当斯', 'sales1@example.com', '13800138021', '销售部', '销售代表', 1),
('sales2', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '艾米·巴斯', 'sales2@example.com', '13800138022', '销售部', '销售代表', 1),
('market1', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '史蒂文·贝茨', 'market1@example.com', '13800138023', '市场部', '市场专员', 1),
('finance1', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '金伯利·罗斯', 'finance1@example.com', '13800138024', '财务部', '财务专员', 1),
('admin1', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '乔舒亚·赖特', 'admin1@example.com', '13800138025', '行政部', '行政专员', 1),
('user1', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '克莉丝汀·菲利普斯', 'user1@example.com', '13800138026', '行政部', '行政助理', 1),
-- 插入第三方系统测试用户（密码统一为：123456，使用BCrypt加密）
('third_party_admin', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '第三方系统管理员', 'third_party_admin@example.com', '13800138100', '第三方系统', '系统管理员', 1),
('notification_user', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '通知服务用户', 'notification_user@example.com', '13800138101', '第三方系统', '通知服务用户', 1),
('process_user', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '流程服务用户', 'process_user@example.com', '13800138102', '第三方系统', '流程服务用户', 1),
('integrated_user', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '综合服务用户', 'integrated_user@example.com', '13800138103', '第三方系统', '综合服务用户', 1);

-- 3.6 关联用户角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1), -- admin - 系统管理员
(2, 2), -- ceo - CEO
(3, 3), -- cto - CTO
(4, 4), -- cfo - CFO
(5, 5), -- coo - COO
(6, 12), -- hr_manager - HR经理
(7, 7), -- dev_manager - 开发经理
(8, 8), -- test_manager - 测试经理
(9, 9), -- ops_manager - 运维经理
(10, 10), -- sales_manager - 销售经理
(11, 11), -- market_manager - 市场经理
(12, 13), -- finance_manager - 财务经理
(13, 14), -- hr - HR专员
(14, 15), -- it_admin - IT管理员
(15, 16), -- senior_arch - 高级架构师
(16, 17), -- dev1 - 开发工程师
(17, 17), -- dev2 - 开发工程师
(18, 18), -- test1 - 测试工程师
(19, 18), -- test2 - 测试工程师
(20, 19), -- ops1 - 运维工程师
(21, 19), -- ops2 - 运维工程师
(22, 20), -- sales1 - 销售代表
(23, 20), -- sales2 - 销售代表
(24, 21), -- market1 - 市场专员
(25, 22), -- finance1 - 财务专员
(26, 23), -- admin1 - 行政专员
(27, 24), -- user1 - 普通员工
-- 关联第三方系统用户角色
(28, 25), -- third_party_admin - 第三方系统管理员
(29, 26), -- notification_user - 通知服务用户
(30, 27), -- process_user - 流程服务用户
(31, 28); -- integrated_user - 综合服务用户

-- 3.7 关联角色权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
-- 系统管理员权限（所有权限）
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11),
(1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18), (1, 19), (1, 20), (1, 21), (1, 22), (1, 23), (1, 24), (1, 25), (1, 26), (1, 27), (1, 28), (1, 29),
(1, 30), (1, 31), (1, 32), (1, 33), (1, 34),

-- HR经理权限
(12, 1), (12, 2), (12, 3), (12, 4), (12, 11), (12, 12), (12, 13), (12, 14),
(12, 15), (12, 16), (12, 17), (12, 21), (12, 22),
-- HR专员权限
(14, 1), (14, 2), (14, 3), (14, 4), (14, 11), (14, 12), (14, 13), (14, 14),
(14, 15), (14, 16), (14, 17), (14, 21), (14, 22),

-- 开发经理权限（部门经理角色）
(7, 1), (7, 2), (7, 3), (7, 4), (7, 15), (7, 16), (7, 21), (7, 22),
-- 测试经理权限（部门经理角色）
(8, 1), (8, 2), (8, 3), (8, 4), (8, 15), (8, 16), (8, 21), (8, 22),
-- 运维经理权限（部门经理角色）
(9, 1), (9, 2), (9, 3), (9, 4), (9, 15), (9, 16), (9, 21), (9, 22),
-- 销售经理权限（部门经理角色）
(10, 1), (10, 2), (10, 3), (10, 4), (10, 15), (10, 16), (10, 21), (10, 22),
-- 市场经理权限（部门经理角色）
(11, 1), (11, 2), (11, 3), (11, 4), (11, 15), (11, 16), (11, 21), (11, 22),
-- 财务经理权限（部门经理角色）
(13, 1), (13, 2), (13, 3), (13, 4), (13, 15), (13, 16), (13, 21), (13, 22),

-- 开发工程师权限
(17, 1), (17, 2), (17, 3), (17, 4), (17, 15), (17, 16), (17, 18), (17, 19), (17, 20), (17, 21), (17, 22),
-- 测试工程师权限
(18, 1), (18, 2), (18, 3), (18, 4), (18, 15), (18, 16), (18, 18), (18, 19), (18, 20), (18, 21), (18, 22),

-- 其他角色权限（基础权限）
-- CEO权限
(2, 1), (2, 2), (2, 3), (2, 4), (2, 15), (2, 16), (2, 21), (2, 22),
-- CTO权限
(3, 1), (3, 2), (3, 3), (3, 4), (3, 15), (3, 16), (3, 21), (3, 22),
-- CFO权限
(4, 1), (4, 2), (4, 3), (4, 4), (4, 15), (4, 16), (4, 21), (4, 22),
-- COO权限
(5, 1), (5, 2), (5, 3), (5, 4), (5, 15), (5, 16), (5, 21), (5, 22),
-- 部门经理权限（通用）
(6, 1), (6, 2), (6, 3), (6, 4), (6, 15), (6, 16), (6, 21), (6, 22),
-- IT管理员权限
(15, 1), (15, 2), (15, 3), (15, 4), (15, 15), (15, 16), (15, 21), (15, 22),
-- 高级架构师权限
(16, 1), (16, 2), (16, 3), (16, 4), (16, 15), (16, 16), (16, 18), (16, 19), (16, 20), (16, 21), (16, 22),
-- 运维工程师权限
(19, 1), (19, 2), (19, 3), (19, 4), (19, 15), (19, 16), (19, 18), (19, 19), (19, 20), (19, 21), (19, 22),
-- 销售代表权限
(20, 1), (20, 2), (20, 3), (20, 4), (20, 15), (20, 16), (20, 21), (20, 22),
-- 市场专员权限
(21, 1), (21, 2), (21, 3), (21, 4), (21, 15), (21, 16), (21, 21), (21, 22),
-- 财务专员权限
(22, 1), (22, 2), (22, 3), (22, 4), (22, 15), (22, 16), (22, 21), (22, 22),
-- 行政专员权限
(23, 1), (23, 2), (23, 3), (23, 4), (23, 15), (23, 16), (23, 21), (23, 22),
-- 普通员工权限
(24, 1), (24, 2), (24, 3), (24, 4), (24, 15), (24, 16), (24, 21), (24, 22),
-- 第三方系统角色权限映射
-- 第三方系统管理员权限（默认只有基础权限，其他权限由管理员配置）
(25, 1), (25, 2), (25, 3), (25, 4),
-- 通知服务用户权限
(26, 1), (26, 2), (26, 3), (26, 4), (26, 11), (26, 12), (26, 13), (26, 14),
(26, 15), (26, 16), (26, 21), (26, 22),
-- 流程服务用户权限
(27, 1), (27, 2), (27, 3), (27, 4), (27, 18), (27, 19), (27, 20), (27, 21), (27, 22),
-- 综合服务用户权限
(28, 1), (28, 2), (28, 3), (28, 4), (28, 11), (28, 12), (28, 13), (28, 14),
(28, 15), (28, 16), (28, 18), (28, 19), (28, 20), (28, 21), (28, 22);

-- 3.8 插入菜单翻译数据
-- 仪表盘
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(1, 'zh-CN', '仪表盘', '系统仪表盘'),
(1, 'en-US', 'Dashboard', 'System Dashboard');

-- 任务管理
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(2, 'zh-CN', '任务管理', '任务管理'),
(2, 'en-US', 'Task Management', 'Task Management');

-- 我的待办
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(3, 'zh-CN', '我的待办', '查看分配给自己的任务'),
(3, 'en-US', 'My Tasks', 'View tasks assigned to me');

-- 已办任务
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(4, 'zh-CN', '已办任务', '查看已完成的任务'),
(4, 'en-US', 'Completed Tasks', 'View completed tasks');

-- 系统管理
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(5, 'zh-CN', '系统管理', '系统管理'),
(5, 'en-US', 'System Management', 'System Management');

-- 用户管理
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(6, 'zh-CN', '用户管理', '用户管理'),
(6, 'en-US', 'User Management', 'User Management');

-- 角色管理
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(7, 'zh-CN', '角色管理', '角色管理'),
(7, 'en-US', 'Role Management', 'Role Management');

-- 角色菜单权限
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(8, 'zh-CN', '角色菜单权限', '角色菜单权限配置'),
(8, 'en-US', 'Role Menu Permission', 'Role Menu Permission Configuration');

-- 菜单管理
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(9, 'zh-CN', '菜单管理', '菜单管理'),
(9, 'en-US', 'Menu Management', 'Menu Management');

-- 多语言配置
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(10, 'zh-CN', '多语言配置', '多语言配置'),
(10, 'en-US', 'Language Management', 'Language Management');

-- 申请管理 (id=15)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(15, 'zh-CN', '申请管理', '申请管理'),
(15, 'en-US', 'Application Management', 'Application Management');

-- 请假申请 (id=16)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(16, 'zh-CN', '请假申请', '发起请假申请'),
(16, 'en-US', 'Leave Application', 'Submit leave application');

-- 入职申请 (id=17)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(17, 'zh-CN', '入职申请', '发起入职申请'),
(17, 'en-US', 'Onboarding Application', 'Submit onboarding application');

-- 流程管理 (id=18)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(18, 'zh-CN', '流程管理', '流程管理'),
(18, 'en-US', 'Process Management', 'Process Management');

-- 我的流程定义 (id=19)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(19, 'zh-CN', '我的流程定义', '我的流程定义'),
(19, 'en-US', 'My Process Definitions', 'My Process Definitions');

-- 流程申请 (id=20)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(20, 'zh-CN', '流程申请', '发起新流程'),
(20, 'en-US', 'Process Application', 'Submit new process');

-- 流程历史 (id=21)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(21, 'zh-CN', '流程历史', '查看流程执行历史'),
(21, 'en-US', 'Process History', 'View process execution history');

-- 审批中心 (id=22)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(22, 'zh-CN', '审批中心', '审批中心'),
(22, 'en-US', 'Approval Center', 'Approval Center');

-- 通知模板管理 (id=11)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(11, 'zh-CN', '通知模板管理', '通知模板管理'),
(11, 'en-US', 'Notification Template Management', 'Notification Template Management');

-- 通知测试 (id=12)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(12, 'zh-CN', '通知测试', '通知测试'),
(12, 'en-US', 'Notification Test', 'Notification Test');

-- 通知日志 (id=13)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(13, 'zh-CN', '通知日志', '通知日志'),
(13, 'en-US', 'Notification Log', 'Notification Log');

-- 待发送通知 (id=14)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(14, 'zh-CN', '待发送通知', '待发送通知'),
(14, 'en-US', 'Pending Notifications', 'Pending Notifications');

-- 租户审批 (id=29)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(29, 'zh-CN', '租户审批', '租户审批管理'),
(29, 'en-US', 'Tenant Approval', 'Tenant Approval Management');

-- 监控与日志 (id=30)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(30, 'zh-CN', '监控与日志', '监控与日志'),
(30, 'en-US', 'Monitoring & Logs', 'Monitoring & Logs');

-- 租户使用统计 (id=31)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(31, 'zh-CN', '租户使用统计', '查看各租户使用统计数据'),
(31, 'en-US', 'Tenant Usage Statistics', 'View tenant usage statistics');

-- API调用统计 (id=32)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(32, 'zh-CN', 'API调用统计', '查看API调用统计数据'),
(32, 'en-US', 'API Call Statistics', 'View API call statistics');

-- 系统操作日志 (id=33)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(33, 'zh-CN', '系统操作日志', '查看系统操作日志'),
(33, 'en-US', 'System Operation Logs', 'View system operation logs');

-- 告警管理 (id=34)
INSERT INTO sys_menu_translation (menu_id, language_code, name, description) VALUES
(34, 'zh-CN', '告警管理', '管理系统告警'),
(34, 'en-US', 'Alert Management', 'Manage system alerts');

-- ===============================
-- 4. 初始化完成
-- ===============================
SELECT '初始化完成' AS status, NOW() AS finish_time;