-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
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

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    url VARCHAR(200),
    method VARCHAR(20),
    description VARCHAR(200),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
);

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
);

-- 员工入职申请表
CREATE TABLE IF NOT EXISTS employee_onboard (
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
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 初始化数据
-- 对于H2数据库，我们需要使用正确的顺序删除数据以避免外键约束问题
-- 首先删除关联表数据
DELETE FROM sys_role_permission;
DELETE FROM sys_user_role;
-- 然后删除主表数据
DELETE FROM sys_permission;
DELETE FROM sys_user;
DELETE FROM sys_role;

-- 插入角色
INSERT INTO sys_role (id, name, code, description) VALUES
(1, '管理员', 'ROLE_ADMIN', '系统管理员'),
(2, '部门经理', 'ROLE_MANAGER', '部门经理'),
(3, 'HR专员', 'ROLE_HR', '人力资源专员'),
(4, 'IT管理员', 'ROLE_IT', 'IT管理员'),
(5, '普通员工', 'ROLE_USER', '普通用户');

-- 插入权限
INSERT INTO sys_permission (id, name, code, url, method, description) VALUES
(1, '查看用户列表', 'user:list', '/api/users', 'GET', '查看所有用户'),
(2, '创建用户', 'user:create', '/api/users', 'POST', '创建新用户'),
(3, '编辑用户', 'user:update', '/api/users/{id}', 'PUT', '编辑用户信息'),
(4, '删除用户', 'user:delete', '/api/users/{id}', 'DELETE', '删除用户'),
(5, '查看角色列表', 'role:list', '/api/roles', 'GET', '查看所有角色'),
(6, '管理流程定义', 'process:definition:manage', '/api/process-definitions', 'ALL', '管理流程定义'),
(7, '发起流程', 'process:start', '/api/process-instances', 'POST', '发起新流程'),
(8, '查看我的任务', 'task:my', '/api/tasks', 'GET', '查看分配给自己的任务'),
(9, '完成任务', 'task:complete', '/api/tasks/{id}/complete', 'POST', '完成任务'),
(10, '查看所有任务', 'task:all', '/api/tasks/all', 'GET', '查看所有任务'),
(11, '管理员工入职', 'onboard:manage', '/api/onboard', 'ALL', '管理员工入职申请'),
(12, '发起请假流程', 'leave:apply', '/api/process-instances/leave', 'POST', '发起请假申请'),
(13, '查看请假记录', 'leave:history', '/api/leave/history', 'GET', '查看请假历史记录'),
(14, '管理请假流程', 'leave:manage', '/api/leave', 'ALL', '管理请假申请'),
(15, '查看流程历史', 'process:history', '/api/process-instances/{id}/history', 'GET', '查看流程执行历史');

-- 插入管理员和部门经理用户（密码统一为：123456，使用BCrypt加密）
INSERT INTO sys_user (id, username, password, full_name, email, phone, department, position, status) VALUES
(1, 'admin', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '系统管理员', 'admin@example.com', '13800138000', 'IT部门', '系统管理员', 1),
(2, 'hr', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', 'HR专员', 'hr@example.com', '13800138001', '人力资源部', 'HR专员', 1),
(3, 'hr_manager', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', 'HR经理', 'hrmanager@example.com', '13800138002', '人力资源部', 'HR经理', 1),
(4, 'it_manager', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', 'IT经理', 'it@example.com', '13800138003', 'IT部门', 'IT经理', 1),
(5, 'dept_manager', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '部门经理', 'manager@example.com', '13800138004', '研发部', '部门经理', 1),
(101, 'zhangsan', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '张三', 'zhangsan@example.com', '13800138005', '研发部', '软件工程师', 1),
(102, 'lisi', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '李四', 'lisi@example.com', '13800138006', '研发部', '部门经理', 1);

-- 关联用户角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1), -- admin 有管理员角色
(2, 3), -- hr 有HR专员角色
(3, 3), -- hr_manager 有HR专员角色
(3, 2), -- hr_manager 同时有部门经理角色
(4, 4), -- it_manager 有IT管理员角色
(5, 2), -- dept_manager 有部门经理角色
(102, 2); -- lisi 有部门经理角色

-- 关联角色权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
-- 管理员权限
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11),
-- 部门经理权限
(2, 7), (2, 8), (2, 9), (2, 11),
-- HR专员权限
(3, 7), (3, 8), (3, 9), (3, 11),
-- IT管理员权限
(4, 8), (4, 9), (4, 11),
-- 普通用户权限
(5, 8), (5, 9);

-- 插入其他普通员工用户（密码：123456，使用BCrypt加密）
INSERT INTO sys_user (id, username, password, full_name, email, phone, department, position, status) VALUES
(103, 'wangwu', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '王五', 'wangwu@example.com', '13800138006', '市场部', '市场专员', 1),
(104, 'zhaoliu', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '赵六', 'zhaoliu@example.com', '13800138007', '市场部', '市场经理助理', 1),
(105, 'qianqi', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '钱七', 'qianqi@example.com', '13800138008', '财务部', '财务助理', 1),
(106, 'sunba', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '孙八', 'sunba@example.com', '13800138009', '财务部', '会计', 1),
(107, 'zhoujiu', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '周九', 'zhoujiu@example.com', '13800138010', '人事部', '招聘专员', 1),
(108, 'wushi', '$2a$10$JjP1FbYVb3V8qkZ7vJZ6JOMQK5Y6q1K7h5k5Z5m5n5l5j5H5G5F5E', '吴十', 'wushi@example.com', '13800138011', '人事部', '培训专员', 1);

-- 为普通员工分配角色（ROLE_USER，角色ID为5）
INSERT INTO sys_user_role (user_id, role_id) VALUES
(101, 5),
(102, 5), -- lisi 同时拥有普通用户角色
(103, 5),
(104, 5),
(105, 5),
(106, 5),
(107, 5),
(108, 5);

-- 已在前面的用户角色关联部分添加了必要的角色映射