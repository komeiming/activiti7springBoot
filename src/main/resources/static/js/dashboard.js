// 仪表盘页面逻辑
 document.addEventListener('DOMContentLoaded', function() {
    // 检查用户是否已登录
    if (!Auth.isAuthenticated()) {
        window.location.href = 'login.html';
        return;
    }
    
    // 显示用户名
    DOMUtils.$('#username-display').textContent = Auth.getUserInfo()?.username || '未知用户';
    
    // 如果是管理员，显示管理员菜单
    if (Auth.hasRole('ROLE_ACTIVITI_ADMIN')) {
        DOMUtils.css(DOMUtils.$('#admin-menu'), { display: 'block' });
    }
    
    // 退出登录
    DOMUtils.on(DOMUtils.$('#logout-btn'), 'click', function() {
        Auth.logout();
        window.location.href = 'login.html';
    });
    
    // 任务相关元素
    const tasksContainer = DOMUtils.$('#tasks-container');
    const refreshTasksBtn = DOMUtils.$('#refresh-tasks');
    const taskDetailModal = DOMUtils.$('#task-detail-modal');
    const closeModalBtn = DOMUtils.$('#close-modal');
    const taskDetailContent = DOMUtils.$('#task-detail-content');
    const completeTaskBtn = DOMUtils.$('#complete-task');
    
    let currentTaskId = null;
    
    // 加载任务列表
    async function loadTasks() {
        tasksContainer.innerHTML = '<div class="loading">加载中...</div>';
        
        try {
            const response = await API.getTasks();
            
            if (response.tasks && response.tasks.length > 0) {
                // 渲染任务列表
                renderTaskList(response.tasks);
                
                // 添加进入动画
                const taskItems = DOMUtils.$$('.task-item');
                taskItems.forEach((item, index) => {
                    setTimeout(() => {
                        AnimationUtils.slideInWithTranslate(item);
                    }, index * 100);
                });
            } else {
                // 显示空状态
                tasksContainer.innerHTML = `
                    <div class="empty-state">
                        <p>暂无待处理任务</p>
                    </div>
                `;
            }
        } catch (error) {
            tasksContainer.innerHTML = `
                <div class="alert alert-danger">
                    加载任务失败: ${error.message}
                </div>
            `;
            Notification.error(`加载任务失败: ${error.message}`);
        }
    }
    
    // 渲染任务列表
    function renderTaskList(tasks) {
        const taskHtml = tasks.map(task => `
            <div class="task-item card" style="margin-bottom: 15px; cursor: pointer; display: none; opacity: 0;" data-task-id="${task.id}">
                <div style="padding: 15px;">
                    <h4 style="margin: 0 0 10px 0;">${task.name || '未命名任务'}</h4>
                    <div style="display: flex; justify-content: space-between; color: #666; font-size: 14px;">
                        <span>流程: ${task.processDefinitionId || '-'}</span>
                        <span>创建时间: ${DateUtils.formatRelative(task.createdDate)}</span>
                    </div>
                </div>
            </div>
        `).join('');
        
        tasksContainer.innerHTML = `
            <div class="task-list">
                <div class="alert alert-info" style="margin-bottom: 15px;">
                    共 ${tasks.length} 个待处理任务
                </div>
                ${taskHtml}
            </div>
        `;
        
        // 添加任务点击事件
        DOMUtils.on(DOMUtils.$$('.task-item'), 'click', function() {
            const taskId = this.getAttribute('data-task-id');
            showTaskDetail(taskId);
        });
    }
    
    // 显示任务详情
    async function showTaskDetail(taskId) {
        currentTaskId = taskId;
        taskDetailContent.innerHTML = '<div class="loading">加载中...</div>';
        
        try {
            const task = await API.getTaskDetail(taskId);
            
            // 渲染任务详情
            taskDetailContent.innerHTML = `
                <div class="task-detail">
                    <div style="margin-bottom: 15px;">
                        <strong>任务名称:</strong>
                        <span>${task.name || '未命名任务'}</span>
                    </div>
                    <div style="margin-bottom: 15px;">
                        <strong>任务ID:</strong>
                        <span>${task.id}</span>
                    </div>
                    <div style="margin-bottom: 15px;">
                        <strong>流程实例ID:</strong>
                        <span>${task.processInstanceId}</span>
                    </div>
                    <div style="margin-bottom: 15px;">
                        <strong>流程定义ID:</strong>
                        <span>${task.processDefinitionId}</span>
                    </div>
                    <div style="margin-bottom: 15px;">
                        <strong>创建时间:</strong>
                        <span>${DateUtils.format(task.createdDate)}</span>
                    </div>
                    ${task.assignee ? `
                        <div style="margin-bottom: 15px;">
                            <strong>负责人:</strong>
                            <span>${task.assignee}</span>
                        </div>
                    ` : ''}
                    <div style="margin-bottom: 15px;">
                        <strong>状态:</strong>
                        <span>${task.status || '待处理'}</span>
                    </div>
                    
                    <!-- 任务表单区域 -->
                    <div style="margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee;">
                        <h4 style="margin-bottom: 15px;">任务表单</h4>
                        <div id="task-form">
                            <!-- 这里可以根据任务类型动态生成表单 -->
                            <div class="form-group">
                                <label for="comment">处理意见:</label>
                                <textarea id="comment" class="form-control" rows="3" placeholder="请输入处理意见"></textarea>
                            </div>
                            <div class="form-group">
                                <label for="outcome">处理结果:</label>
                                <select id="outcome" class="form-control">
                                    <option value="approve">同意</option>
                                    <option value="reject">拒绝</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            
            // 显示模态框并添加动画
            DOMUtils.css(taskDetailModal, { display: 'flex' });
            const modalContent = DOMUtils.$('.modal-content', taskDetailModal);
            AnimationUtils.fadeIn(modalContent);
        } catch (error) {
            taskDetailContent.innerHTML = `
                <div class="alert alert-danger">
                    加载任务详情失败: ${error.message}
                </div>
            `;
            Notification.error(`加载任务详情失败: ${error.message}`);
        }
    }
    
    // 完成任务
    async function completeTask() {
        if (!currentTaskId) return;
        
        // 获取表单数据
        const comment = DOMUtils.$('#comment').value;
        const outcome = DOMUtils.$('#outcome').value;
        
        const variables = {
            comment: comment,
            outcome: outcome,
            completedAt: new Date().toISOString()
        };
        
        // 禁用按钮防止重复提交
        DOMUtils.css(completeTaskBtn, { disabled: true });
        completeTaskBtn.textContent = '处理中...';
        
        try {
            await API.completeTask(currentTaskId, variables);
            
            // 关闭模态框
            const modalContent = DOMUtils.$('.modal-content', taskDetailModal);
            AnimationUtils.fadeOut(modalContent, 200);
            setTimeout(() => {
                DOMUtils.css(taskDetailModal, { display: 'none' });
            }, 200);
            
            // 重新加载任务列表
            loadTasks();
            
            // 显示成功消息
            Notification.success('任务已成功完成！');
        } catch (error) {
            Notification.error(`完成任务失败: ${error.message}`);
            AnimationUtils.shake(DOMUtils.$('#task-form'));
        } finally {
            // 恢复按钮状态
            DOMUtils.css(completeTaskBtn, { disabled: false });
            completeTaskBtn.textContent = '完成任务';
        }
    }
    
    // 格式化日期函数已由工具类提供
    
    // 关闭模态框
    function closeModal() {
        const modalContent = DOMUtils.$('.modal-content', taskDetailModal);
        AnimationUtils.fadeOut(modalContent, 200);
        setTimeout(() => {
            DOMUtils.css(taskDetailModal, { display: 'none' });
        }, 200);
    }
    
    // 点击关闭按钮
    DOMUtils.on(closeModalBtn, 'click', closeModal);
    
    // 点击模态框外部关闭
    DOMUtils.on(taskDetailModal, 'click', function(event) {
        if (event.target === taskDetailModal) {
            closeModal();
        }
    });
    
    // 按下ESC键关闭模态框
    DOMUtils.on(document, 'keydown', function(event) {
        if (event.key === 'Escape' && taskDetailModal.style.display === 'flex') {
            closeModal();
        }
    });
    
    // 完成任务按钮点击事件
    DOMUtils.on(completeTaskBtn, 'click', completeTask);
    
    // 刷新任务列表，添加点击动画
    DOMUtils.on(refreshTasksBtn, 'click', function() {
        AnimationUtils.pulse(this, 500);
        loadTasks();
    });
    
    // 初始加载任务列表
    loadTasks();
});
