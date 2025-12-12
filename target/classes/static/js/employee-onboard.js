// 员工入职流程前端交互逻辑

// 等待DOM加载完成
document.addEventListener('DOMContentLoaded', function() {
    // 初始化用户信息
    initUserInfo();
    
    // 初始化标签页切换
    initTabs();
    
    // 初始化启动流程表单
    initStartProcessForm();
    
    // 初始化任务列表
    initTaskList();
    
    // 初始化流程列表
    initProcessList();
    
    // 初始化任务详情模态框
    initTaskDetailModal();
    
    // 初始化退出登录
    initLogout();
});

// 初始化用户信息
function initUserInfo() {
    API.getUserInfo().then(data => {
        if (data.success) {
            document.getElementById('username-display').textContent = data.data.username || '用户';
            
            // 显示管理员菜单
            const roles = data.data.roles || [];
            if (roles.includes('ADMIN') || roles.includes('HR_MANAGER')) {
                document.getElementById('admin-menu').style.display = 'block';
            }
        }
    }).catch(error => {
        console.error('获取用户信息失败:', error);
        Auth.logout();
        window.location.href = 'login.html';
    });
}

// 初始化标签页切换
function initTabs() {
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');
            
            // 更新按钮状态
            tabBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            
            // 更新内容显示
            tabContents.forEach(content => {
                content.classList.remove('active');
                if (content.id === tabId) {
                    content.classList.add('active');
                    
                    // 如果是任务列表，自动刷新
                    if (tabId === 'my-tasks') {
                        loadMyTasks();
                    }
                    
                    // 如果是流程列表，自动刷新
                    if (tabId === 'process-list') {
                        loadProcessList();
                    }
                }
            });
        });
    });
}

// 初始化启动流程表单
function initStartProcessForm() {
    const form = document.getElementById('onboard-form');
    
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const formData = new FormData(form);
        const processVariables = {};
        
        // 转换表单数据为流程变量
        formData.forEach((value, key) => {
            processVariables[key] = value;
        });
        
        // 添加表单提交的加载状态
        const submitBtn = this.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;
        submitBtn.textContent = '提交中...';
        submitBtn.disabled = true;
        
        // 调用API启动流程
        API.startProcess('employee-onboard', processVariables)
            .then(response => {
                if (response.success) {
                    showSuccessMessage('流程启动成功！');
                    form.reset();
                } else {
                    showErrorMessage(response.message || '流程启动失败');
                }
            })
            .catch(error => {
                console.error('启动流程失败:', error);
                showErrorMessage('流程启动失败: ' + error.message);
            })
            .finally(() => {
                // 恢复按钮状态
                submitBtn.textContent = originalText;
                submitBtn.disabled = false;
            });
    });
}

// 初始化任务列表
function initTaskList() {
    const refreshBtn = document.getElementById('refresh-my-tasks');
    refreshBtn.addEventListener('click', loadMyTasks);
    
    // 初始加载
    loadMyTasks();
}

// 加载我的任务列表
function loadMyTasks() {
    const container = document.getElementById('my-tasks-container');
    container.innerHTML = '<div class="loading">加载中...</div>';
    
    API.getMyTasks()
        .then(response => {
            if (response.success) {
                const tasks = response.data || [];
                
                if (tasks.length === 0) {
                    container.innerHTML = '<div class="empty-state">暂无待办任务</div>';
                    return;
                }
                
                // 创建任务列表
                let html = '<table class="task-table">';
                html += '<thead>';
                html += '<tr>';
                html += '<th>任务名称</th>';
                html += '<th>流程实例ID</th>';
                html += '<th>创建时间</th>';
                html += '<th>操作</th>';
                html += '</tr>';
                html += '</thead>';
                html += '<tbody>';
                
                tasks.forEach(task => {
                    const createTime = task.createTime ? new Date(task.createTime).toLocaleString() : '';
                    
                    html += '<tr>';
                    html += `<td>${task.name}</td>`;
                    html += `<td>${task.processInstanceId}</td>`;
                    html += `<td>${createTime}</td>`;
                    html += `<td><button class="btn btn-small view-task" data-task-id="${task.id}">查看详情</button></td>`;
                    html += '</tr>';
                });
                
                html += '</tbody>';
                html += '</table>';
                
                container.innerHTML = html;
                
                // 绑定查看任务详情事件
                document.querySelectorAll('.view-task').forEach(btn => {
                    btn.addEventListener('click', function() {
                        const taskId = this.getAttribute('data-task-id');
                        showTaskDetail(taskId);
                    });
                });
            } else {
                container.innerHTML = `<div class="error-message">加载失败: ${response.message}</div>`;
            }
        })
        .catch(error => {
            console.error('加载任务失败:', error);
            container.innerHTML = `<div class="error-message">加载失败: ${error.message}</div>`;
        });
}

// 初始化流程列表
function initProcessList() {
    const refreshBtn = document.getElementById('refresh-process-list');
    const searchBtn = document.getElementById('search-process');
    
    refreshBtn.addEventListener('click', loadProcessList);
    searchBtn.addEventListener('click', function() {
        const keyword = document.getElementById('employee-search').value.trim();
        loadProcessList(keyword);
    });
    
    // 按回车键搜索
    document.getElementById('employee-search').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const keyword = this.value.trim();
            loadProcessList(keyword);
        }
    });
}

// 加载流程列表
function loadProcessList(keyword = '') {
    const container = document.getElementById('process-list-container');
    container.innerHTML = '<div class="loading">加载中...</div>';
    
    API.getOnboardProcessList(keyword)
        .then(response => {
            if (response.success) {
                const processes = response.data || [];
                
                if (processes.length === 0) {
                    container.innerHTML = '<div class="empty-state">暂无入职流程记录</div>';
                    return;
                }
                
                // 创建流程列表
                let html = '<table class="process-table">';
                html += '<thead>';
                html += '<tr>';
                html += '<th>员工姓名</th>';
                html += '<th>员工ID</th>';
                html += '<th>部门</th>';
                html += '<th>入职日期</th>';
                html += '<th>状态</th>';
                html += '<th>创建时间</th>';
                html += '</tr>';
                html += '</thead>';
                html += '<tbody>';
                
                processes.forEach(process => {
                    const createTime = process.createTime ? new Date(process.createTime).toLocaleString() : '';
                    const startDate = process.startDate ? new Date(process.startDate).toLocaleDateString() : '';
                    const statusText = getStatusText(process.status);
                    
                    html += '<tr>';
                    html += `<td>${process.employeeName}</td>`;
                    html += `<td>${process.employeeId}</td>`;
                    html += `<td>${process.department}</td>`;
                    html += `<td>${startDate}</td>`;
                    html += `<td><span class="status-badge ${process.status}">${statusText}</span></td>`;
                    html += `<td>${createTime}</td>`;
                    html += '</tr>';
                });
                
                html += '</tbody>';
                html += '</table>';
                
                container.innerHTML = html;
            } else {
                container.innerHTML = `<div class="error-message">加载失败: ${response.message}</div>`;
            }
        })
        .catch(error => {
            console.error('加载流程列表失败:', error);
            container.innerHTML = `<div class="error-message">加载失败: ${error.message}</div>`;
        });
}

// 获取状态文本
function getStatusText(status) {
    const statusMap = {
        'INIT': '初始化',
        'HR_DONE': 'HR已处理',
        'IT_DONE': 'IT已处理',
        'MANAGER_APPROVED': '经理已审批',
        'TRAINING_DONE': '培训已完成',
        'COMPLETED': '已完成',
        'REJECTED': '已拒绝'
    };
    
    return statusMap[status] || '未知状态';
}

// 初始化任务详情模态框
function initTaskDetailModal() {
    const modal = document.getElementById('task-detail-modal');
    const closeBtn = document.getElementById('close-modal');
    const completeBtn = document.getElementById('complete-task');
    
    // 关闭模态框
    closeBtn.addEventListener('click', function() {
        modal.style.display = 'none';
    });
    
    // 点击模态框外部关闭
    window.addEventListener('click', function(event) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
    
    // 完成任务
    completeBtn.addEventListener('click', function() {
        const taskId = this.getAttribute('data-task-id');
        completeCurrentTask(taskId);
    });
}

// 显示任务详情
function showTaskDetail(taskId) {
    const modal = document.getElementById('task-detail-modal');
    const contentDiv = document.getElementById('task-detail-content');
    const completeBtn = document.getElementById('complete-task');
    
    contentDiv.innerHTML = '<div class="loading">加载中...</div>';
    modal.style.display = 'flex';
    completeBtn.setAttribute('data-task-id', taskId);
    
    API.getTaskDetail(taskId)
        .then(task => {
            // 根据任务定义key生成不同的表单
            const taskKey = task.name;
            let formHtml = '';
            
            // 基本信息
            formHtml += '<div class="task-info">';
            formHtml += `<h4>任务: ${task.name}</h4>`;
            formHtml += `<p><strong>流程实例ID:</strong> ${task.processInstanceId}</p>`;
            formHtml += `<p><strong>创建时间:</strong> ${new Date(task.created).toLocaleString()}</p>`;
            formHtml += '</div>';
            
            // 根据任务类型生成不同的表单
            if (taskKey.includes('HR') || taskKey.includes('人事')) {
                // HR填写信息表单
                formHtml += createHRForm(task.variables);
            } else if (taskKey.includes('IT') || taskKey.includes('设备')) {
                // IT准备设备表单
                formHtml += createITForm(task.variables);
            } else if (taskKey.includes('经理') || taskKey.includes('审批')) {
                // 部门经理审批表单
                formHtml += createManagerForm(task.variables);
            } else if (taskKey.includes('培训')) {
                // 培训安排表单
                formHtml += createTrainingForm(task.variables);
            } else {
                // 通用表单
                formHtml += createGenericForm(task.variables);
            }
            
            contentDiv.innerHTML = formHtml;
        })
        .catch(error => {
            console.error('获取任务详情失败:', error);
            contentDiv.innerHTML = `<div class="error-message">加载失败: ${error.message}</div>`;
        });
}

// 创建HR表单
function createHRForm(variables = {}) {
    let html = '<div class="task-form">';
    html += '<h5>人事信息确认</h5>';
    html += '<form id="hr-form">';
    
    html += '<div class="form-group">';
    html += '<label>员工姓名</label>';
    html += `<input type="text" name="employeeName" value="${variables.employeeName || ''}" readonly>`;
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>员工ID</label>';
    html += `<input type="text" name="employeeId" value="${variables.employeeId || ''}" readonly>`;
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>HR文档是否已准备</label>';
    html += '<select name="hrDocumentsReady" required>';
    html += '<option value="">请选择</option>';
    html += '<option value="true">是</option>';
    html += '<option value="false">否</option>';
    html += '</select>';
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>备注</label>';
    html += '<textarea name="hrComments" rows="3"></textarea>';
    html += '</div>';
    
    html += '</form>';
    html += '</div>';
    
    return html;
}

// 创建IT表单
function createITForm(variables = {}) {
    let html = '<div class="task-form">';
    html += '<h5>IT设备准备</h5>';
    html += '<form id="it-form">';
    
    html += '<div class="form-group">';
    html += '<label>员工姓名</label>';
    html += `<input type="text" name="employeeName" value="${variables.employeeName || ''}" readonly>`;
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>电脑已准备</label>';
    html += '<select name="computerReady" required>';
    html += '<option value="">请选择</option>';
    html += '<option value="true">是</option>';
    html += '<option value="false">否</option>';
    html += '</select>';
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>邮箱已设置</label>';
    html += '<select name="emailSetup" required>';
    html += '<option value="">请选择</option>';
    html += '<option value="true">是</option>';
    html += '<option value="false">否</option>';
    html += '</select>';
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>账号已创建</label>';
    html += '<select name="accountCreated" required>';
    html += '<option value="">请选择</option>';
    html += '<option value="true">是</option>';
    html += '<option value="false">否</option>';
    html += '</select>';
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>IT备注</label>';
    html += '<textarea name="itComments" rows="3"></textarea>';
    html += '</div>';
    
    html += '</form>';
    html += '</div>';
    
    return html;
}

// 创建经理审批表单
function createManagerForm(variables = {}) {
    let html = '<div class="task-form">';
    html += '<h5>部门经理审批</h5>';
    html += '<form id="manager-form">';
    
    html += '<div class="form-group">';
    html += '<label>员工姓名</label>';
    html += `<input type="text" name="employeeName" value="${variables.employeeName || ''}" readonly>`;
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>所属部门</label>';
    html += `<input type="text" name="department" value="${variables.department || ''}" readonly>`;
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>职位</label>';
    html += `<input type="text" name="position" value="${variables.position || ''}" readonly>`;
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>审批结果</label>';
    html += '<div class="radio-group">';
    html += '<label><input type="radio" name="approvalResult" value="approved" required> 批准</label>';
    html += '<label><input type="radio" name="approvalResult" value="rejected" required> 拒绝</label>';
    html += '</div>';
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>审批意见</label>';
    html += '<textarea name="managerComments" rows="3" required></textarea>';
    html += '</div>';
    
    html += '</form>';
    html += '</div>';
    
    return html;
}

// 创建培训表单
function createTrainingForm(variables = {}) {
    let html = '<div class="task-form">';
    html += '<h5>入职培训安排</h5>';
    html += '<form id="training-form">';
    
    html += '<div class="form-group">';
    html += '<label>员工姓名</label>';
    html += `<input type="text" name="employeeName" value="${variables.employeeName || ''}" readonly>`;
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>培训日期</label>';
    html += '<input type="date" name="trainingDate" required>';
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>培训地点</label>';
    html += '<input type="text" name="trainingLocation" required>';
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>培训内容</label>';
    html += '<textarea name="trainingContent" rows="3" required></textarea>';
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>培训负责人</label>';
    html += '<input type="text" name="trainer" required>';
    html += '</div>';
    
    html += '<div class="form-group">';
    html += '<label>培训是否完成</label>';
    html += '<select name="trainingCompleted" required>';
    html += '<option value="">请选择</option>';
    html += '<option value="true">是</option>';
    html += '<option value="false">否</option>';
    html += '</select>';
    html += '</div>';
    
    html += '</form>';
    html += '</div>';
    
    return html;
}

// 创建通用表单
function createGenericForm(variables = {}) {
    let html = '<div class="task-form">';
    html += '<h5>任务处理</h5>';
    html += '<form id="generic-form">';
    
    // 显示现有变量
    if (Object.keys(variables).length > 0) {
        html += '<div class="variables-section">';
        html += '<h6>流程变量</h6>';
        
        Object.entries(variables).forEach(([key, value]) => {
            html += `<p><strong>${key}:</strong> ${value}</p>`;
        });
        
        html += '</div>';
    }
    
    html += '<div class="form-group">';
    html += '<label>处理备注</label>';
    html += '<textarea name="comments" rows="3"></textarea>';
    html += '</div>';
    
    html += '</form>';
    html += '</div>';
    
    return html;
}

// 完成当前任务
function completeCurrentTask(taskId) {
    // 查找表单元素
    const forms = ['hr-form', 'it-form', 'manager-form', 'training-form', 'generic-form'];
    let formElement = null;
    
    for (const formId of forms) {
        formElement = document.getElementById(formId);
        if (formElement) break;
    }
    
    if (!formElement) {
        showErrorMessage('未找到任务表单');
        return;
    }
    
    // 验证表单
    if (!formElement.checkValidity()) {
        formElement.reportValidity();
        return;
    }
    
    // 收集表单数据
    const formData = new FormData(formElement);
    const variables = {};
    
    formData.forEach((value, key) => {
        // 转换布尔值
        if (value === 'true') variables[key] = true;
        else if (value === 'false') variables[key] = false;
        else variables[key] = value;
    });
    
    // 添加任务完成的加载状态
    const completeBtn = document.getElementById('complete-task');
    const originalText = completeBtn.textContent;
    completeBtn.textContent = '处理中...';
    completeBtn.disabled = true;
    
    // 调用API完成任务
    API.completeTask(taskId, variables)
        .then(response => {
            if (response.success) {
                showSuccessMessage('任务完成成功！');
                
                // 关闭模态框
                document.getElementById('task-detail-modal').style.display = 'none';
                
                // 刷新任务列表
                loadMyTasks();
            } else {
                showErrorMessage(response.message || '任务完成失败');
            }
        })
        .catch(error => {
            console.error('完成任务失败:', error);
            showErrorMessage('任务完成失败: ' + error.message);
        })
        .finally(() => {
            // 恢复按钮状态
            completeBtn.textContent = originalText;
            completeBtn.disabled = false;
        });
}

// 初始化退出登录
function initLogout() {
    document.getElementById('logout-btn').addEventListener('click', function() {
        Auth.logout();
        window.location.href = 'login.html';
    });
}

// 显示成功消息
function showSuccessMessage(message) {
    showNotification(message, 'success');
}

// 显示错误消息
function showErrorMessage(message) {
    showNotification(message, 'error');
}

// 显示通知
function showNotification(message, type = 'info') {
    // 创建通知元素
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    // 添加到页面
    document.body.appendChild(notification);
    
    // 设置样式（如果CSS中没有定义）
    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.padding = '15px 20px';
    notification.style.borderRadius = '5px';
    notification.style.color = 'white';
    notification.style.zIndex = '9999';
    notification.style.boxShadow = '0 2px 10px rgba(0,0,0,0.2)';
    notification.style.transition = 'opacity 0.3s';
    
    // 根据类型设置背景色
    if (type === 'success') {
        notification.style.backgroundColor = '#4CAF50';
    } else if (type === 'error') {
        notification.style.backgroundColor = '#f44336';
    } else {
        notification.style.backgroundColor = '#2196F3';
    }
    
    // 自动关闭
    setTimeout(() => {
        notification.style.opacity = '0';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}