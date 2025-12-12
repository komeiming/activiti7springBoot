-- 通知模板表
CREATE TABLE IF NOT EXISTS notification_template (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '模板ID（唯一标识）',
    name VARCHAR(255) NOT NULL COMMENT '模板名称',
    code VARCHAR(50) NOT NULL COMMENT '模板编码',
    type VARCHAR(20) NOT NULL COMMENT '模板类型（email/sms）',
    status VARCHAR(20) NOT NULL DEFAULT 'enabled' COMMENT '状态（enabled/disabled）',
    sender VARCHAR(255) COMMENT '发送者（邮箱地址或短信发送号码）',
    subject VARCHAR(255) COMMENT '邮件主题模板',
    content TEXT NOT NULL COMMENT '邮件内容模板或短信内容模板',
    reply_to VARCHAR(255) COMMENT '回复邮箱（可选）',
    signature VARCHAR(100) COMMENT '短信签名标识（可选）',
    description TEXT COMMENT '模板描述',
    created_by VARCHAR(100) COMMENT '创建人',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(100) COMMENT '更新人',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 通知模板表索引
CREATE INDEX IF NOT EXISTS idx_type ON notification_template(type);
CREATE INDEX IF NOT EXISTS idx_status ON notification_template(status);
CREATE INDEX IF NOT EXISTS idx_name ON notification_template(name);

-- 通知发送日志表
CREATE TABLE IF NOT EXISTS notification_log (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '日志ID',
    send_id VARCHAR(36) NOT NULL COMMENT '发送ID（对应响应中的sendId）',
    template_id VARCHAR(36) NOT NULL COMMENT '模板ID',
    template_name VARCHAR(255) COMMENT '模板名称',
    type VARCHAR(20) NOT NULL COMMENT '通知类型（email/sms）',
    receiver VARCHAR(255) NOT NULL COMMENT '接收者（脱敏处理）',
    params TEXT COMMENT '参数替换数据（JSON格式）',
    sender_system VARCHAR(100) COMMENT '调用系统标识',
    request_id VARCHAR(36) COMMENT '请求ID',
    success BOOLEAN NOT NULL COMMENT '发送是否成功',
    error_code VARCHAR(50) COMMENT '错误代码（如果失败）',
    error_message VARCHAR(500) COMMENT '错误信息（如果失败）',
    send_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间'
);

-- 通知发送日志表索引
CREATE INDEX IF NOT EXISTS idx_send_id ON notification_log(send_id);
CREATE INDEX IF NOT EXISTS idx_template_id ON notification_log(template_id);
CREATE INDEX IF NOT EXISTS idx_success ON notification_log(success);
CREATE INDEX IF NOT EXISTS idx_send_time ON notification_log(send_time);

-- 模板操作日志表
CREATE TABLE IF NOT EXISTS template_operation_log (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '日志ID',
    template_id VARCHAR(36) NOT NULL COMMENT '模板ID',
    template_name VARCHAR(255) COMMENT '模板名称',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型（create/update/delete/enable/disable）',
    operator VARCHAR(100) COMMENT '操作人',
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    operation_details TEXT COMMENT '操作详情（JSON格式）'
);

-- 模板操作日志表索引
CREATE INDEX IF NOT EXISTS idx_template_id ON template_operation_log(template_id);
CREATE INDEX IF NOT EXISTS idx_operation_type ON template_operation_log(operation_type);
CREATE INDEX IF NOT EXISTS idx_operation_time ON template_operation_log(operation_time);

-- 待发送通知表
CREATE TABLE IF NOT EXISTS pending_notification (
    id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '待发送ID',
    template_id VARCHAR(36) NOT NULL COMMENT '模板ID',
    template_name VARCHAR(255) COMMENT '模板名称',
    type VARCHAR(20) NOT NULL COMMENT '通知类型（email/sms）',
    receiver VARCHAR(255) NOT NULL COMMENT '接收者（邮箱地址或手机号）',
    subject VARCHAR(255) COMMENT '邮件主题（如果是邮件）',
    content TEXT NOT NULL COMMENT '邮件内容或短信内容',
    params TEXT COMMENT '参数替换数据（JSON格式）',
    sender_system VARCHAR(100) COMMENT '调用系统标识',
    request_id VARCHAR(36) COMMENT '请求ID',
    priority INT DEFAULT 0 COMMENT '优先级（0-低，1-中，2-高）',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态（pending-待发送，sending-发送中，sent-已发送，failed-发送失败）',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retries INT DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time TIMESTAMP COMMENT '下次重试时间',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 待发送通知表索引
CREATE INDEX IF NOT EXISTS idx_status ON pending_notification(status);
CREATE INDEX IF NOT EXISTS idx_type ON pending_notification(type);
CREATE INDEX IF NOT EXISTS idx_next_retry_time ON pending_notification(next_retry_time);
CREATE INDEX IF NOT EXISTS idx_created_time ON pending_notification(created_time);
