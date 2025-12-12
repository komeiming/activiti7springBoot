// 流程管理页面逻辑
 document.addEventListener('DOMContentLoaded', function() {
    // 检查用户是否已登录且是管理员
    if (!Auth.isAuthenticated() || !Auth.hasRole('ROLE_ACTIVITI_ADMIN')) {
        // 如果不是管理员，重定向到仪表盘
        window.location.href = 'dashboard.html';
        return;
    }
    
    // 显示用户名
    DOMUtils.$('#username-display').textContent = Auth.getUserInfo()?.username || '未知用户';
    
    // 退出登录
    DOMUtils.on(DOMUtils.$('#logout-btn'), 'click', function() {
        Auth.logout();
        window.location.href = 'login.html';
    });
    
    // 元素引用
    const engineInfoContainer = DOMUtils.$('#engine-info-container');
    const uploadForm = DOMUtils.$('#upload-form');
    const uploadMessage = DOMUtils.$('#upload-message');
    const definitionsContainer = DOMUtils.$('#definitions-container');
    const activeInstancesContainer = DOMUtils.$('#active-instances-container');
    const completedInstancesContainer = DOMUtils.$('#completed-instances-container');
    const refreshDefinitionsBtn = DOMUtils.$('#refresh-definitions');
    const startProcessBtn = DOMUtils.$('#start-process');
    
    // 加载流程引擎信息
    async function loadEngineInfo() {
        try {
            const info = await API.getEngineInfo();
            
            engineInfoContainer.innerHTML = `
                <div class="engine-stats">
                    <div style="display: flex; flex-wrap: wrap; gap: 20px;">
                        <div class="stat-item" style="
                            background-color: #f8f9fa;
                            padding: 20px;
                            border-radius: 8px;
                            flex: 1;
                            min-width: 200px;
                        ">
                            <div style="font-size: 14px; color: #666; margin-bottom: 5px;">引擎名称</div>
                            <div style="font-size: 18px; font-weight: 500;">${info.name}</div>
                        </div>
                        <div class="stat-item" style="
                            background-color: #f8f9fa;
                            padding: 20px;
                            border-radius: 8px;
                            flex: 1;
                            min-width: 200px;
                        ">
                            <div style="font-size: 14px; color: #666; margin-bottom: 5px;">流程定义数量</div>
                            <div style="font-size: 18px; font-weight: 500;">${info.processDefinitionsCount}</div>
                        </div>
                        <div class="stat-item" style="
                            background-color: #f8f9fa;
                            padding: 20px;
                            border-radius: 8px;
                            flex: 1;
                            min-width: 200px;
                        ">
                            <div style="font-size: 14px; color: #666; margin-bottom: 5px;">部署数量</div>
                            <div style="font-size: 18px; font-weight: 500;">${info.deploymentsCount}</div>
                        </div>
                    </div>
                </div>
            `;
            
            // 添加统计项动画效果
            DOMUtils.$$('.stat-item', engineInfoContainer).forEach((item, index) => {
                setTimeout(() => {
                    AnimationUtils.pulse(item);
                }, index * 200);
            });
        } catch (error) {
            engineInfoContainer.innerHTML = `
                <div class="alert alert-danger">
                    加载引擎信息失败: ${error.message}
                </div>
            `;
            Notification.error(`加载引擎信息失败: ${error.message}`);
        }
    }
    
    // 加载流程定义列表
    async function loadProcessDefinitions() {
        definitionsContainer.innerHTML = '<div class="loading">加载中...</div>';
        
        try {
            const response = await API.getProcessDefinitions();
            
            if (response.definitions && response.definitions.length > 0) {
                // 渲染流程定义列表
                renderProcessDefinitions(response.definitions);
                
                // 添加进入动画
                const definitionItems = DOMUtils.$$('.process-definition');
                definitionItems.forEach((item, index) => {
                    setTimeout(() => {
                        AnimationUtils.slideInWithTranslate(item);
                    }, index * 100);
                });
            } else {
                definitionsContainer.innerHTML = `
                    <div class="empty-state">
                        <p>暂无流程定义</p>
                        <p style="margin-top: 10px; font-size: 14px; color: #666;">请上传BPMN文件来创建流程定义</p>
                    </div>
                `;
            }
        } catch (error) {
            definitionsContainer.innerHTML = `
                <div class="alert alert-danger">
                    加载流程定义失败: ${error.message}
                </div>
            `;
            Notification.error(`加载流程定义失败: ${error.message}`);
        }
    }
    
    // 渲染流程定义列表
    function renderProcessDefinitions(definitions) {
        const definitionsHtml = definitions.map(def => `
            <div class="process-definition card" style="margin-bottom: 15px; display: none; opacity: 0;">
                <div style="padding: 15px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                        <h4 style="margin: 0;">${def.name || def.key}</h4>
                        <div>
                            <span class="badge" style="
                                background-color: #1a73e8;
                                color: white;
                                padding: 3px 8px;
                                border-radius: 12px;
                                font-size: 12px;
                            ">
                                版本 ${def.version}
                            </span>
                        </div>
                    </div>
                    <div style="margin-bottom: 10px; font-size: 14px; color: #666;">
                        <strong>ID:</strong> ${def.id}
                    </div>
                    <div style="margin-bottom: 10px; font-size: 14px; color: #666;">
                        <strong>密钥:</strong> ${def.key}
                    </div>
                    <div style="display: flex; gap: 10px; margin-top: 15px;">
                        <button class="btn btn-secondary btn-sm" data-definition-id="${def.id}" data-action="start">
                            启动流程
                        </button>
                        <button class="btn btn-danger btn-sm" data-definition-id="${def.id}" data-action="delete">
                            删除定义
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
        
        definitionsContainer.innerHTML = `
            <div class="process-definitions-list">
                <div class="alert alert-info" style="margin-bottom: 15px;">
                    共 ${definitions.length} 个流程定义
                </div>
                ${definitionsHtml}
            </div>
        `;
        
        // 添加按钮事件监听
        addProcessDefinitionListeners();
    }
    
    // 添加流程定义按钮事件监听
    function addProcessDefinitionListeners() {
        DOMUtils.on(DOMUtils.$$('.process-definition button'), 'click', function() {
            const definitionId = this.getAttribute('data-definition-id');
            const action = this.getAttribute('data-action');
            
            if (action === 'start') {
                AnimationUtils.pulse(this, 300);
                startProcess(definitionId);
            } else if (action === 'delete') {
                const result = FormUtils.confirm('确定要删除这个流程定义吗？此操作可能会影响正在运行的流程实例。');
                if (result) {
                    deleteProcessDefinition(definitionId);
                }
            }
        });
    }
    
    // 启动流程
    async function startProcess(definitionId) {
        try {
            // 实际项目中应该传递流程定义ID
            const result = await API.startProcess();
            
            Notification.success(`流程启动成功！流程实例ID: ${result.processInstanceId}`);
            
            // 刷新流程实例列表
            loadActiveInstances();
        } catch (error) {
            Notification.error(`启动流程失败: ${error.message}`);
        }
    }
    
    // 删除流程定义
    async function deleteProcessDefinition(definitionId) {
        try {
            // 调用真实的后端API删除流程定义
            const response = await API.request(`/process/definition/${definitionId}`, {
                method: 'DELETE'
            });
            
            // 查找要删除的元素并添加动画
            const element = DOMUtils.$(`.process-definition button[data-definition-id="${definitionId}"]`).closest('.process-definition');
            if (element) {
                AnimationUtils.fadeOut(element, 300);
            }
            
            Notification.success(response.message || '流程定义删除成功！');
            
            // 刷新流程定义列表
            setTimeout(() => {
                loadProcessDefinitions();
            }, 300);
        } catch (error) {
            Notification.error(`删除流程定义失败: ${error.message}`);
        }
    }
    
    // 加载活跃流程实例
    function loadActiveInstances() {
        // 模拟数据
        const mockInstances = [
            {
                id: 'instance-1',
                processDefinitionId: 'team01:1:abc123',
                processDefinitionName: '团队审批流程',
                startTime: new Date().toISOString(),
                status: 'active'
            },
            {
                id: 'instance-2',
                processDefinitionId: 'team01:1:abc123',
                processDefinitionName: '团队审批流程',
                startTime: new Date(Date.now() - 3600000).toISOString(),
                status: 'active'
            }
        ];
        
        activeInstancesContainer.innerHTML = `
            <div class="process-instances-list">
                <div class="alert alert-info" style="margin-bottom: 15px;">
                    共 ${mockInstances.length} 个活跃流程实例
                </div>
                ${mockInstances.map(instance => `
                    <div class="process-instance card" style="margin-bottom: 15px;">
                        <div style="padding: 15px;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                                <h4 style="margin: 0;">${instance.processDefinitionName}</h4>
                                <span class="badge" style="
                                    background-color: #28a745;
                                    color: white;
                                    padding: 3px 8px;
                                    border-radius: 12px;
                                    font-size: 12px;
                                ">
                                    运行中
                                </span>
                            </div>
                            <div style="margin-bottom: 10px; font-size: 14px; color: #666;">
                                <strong>实例ID:</strong> ${instance.id}
                            </div>
                            <div style="margin-bottom: 10px; font-size: 14px; color: #666;">
                                <strong>开始时间:</strong> ${DateUtils.formatRelative(instance.startTime)}
                            </div>
                            <div style="display: flex; gap: 10px; margin-top: 15px;">
                                <button class="btn btn-secondary btn-sm" data-instance-id="${instance.id}" data-action="suspend">
                                    暂停
                                </button>
                                <button class="btn btn-danger btn-sm" data-instance-id="${instance.id}" data-action="delete">
                                    删除
                                </button>
                            </div>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
        
        // 添加实例操作事件
        addInstanceListeners();
    }
    
    // 加载已完成流程实例
    function loadCompletedInstances() {
        // 模拟数据
        const mockInstances = [
            {
                id: 'completed-1',
                processDefinitionId: 'team01:1:abc123',
                processDefinitionName: '团队审批流程',
                startTime: new Date(Date.now() - 86400000).toISOString(),
                endTime: new Date(Date.now() - 43200000).toISOString(),
                status: 'completed'
            }
        ];
        
        completedInstancesContainer.innerHTML = `
            <div class="process-instances-list">
                <div class="alert alert-info" style="margin-bottom: 15px;">
                    共 ${mockInstances.length} 个已完成流程实例
                </div>
                ${mockInstances.map(instance => `
                    <div class="process-instance card" style="margin-bottom: 15px;">
                        <div style="padding: 15px;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                                <h4 style="margin: 0;">${instance.processDefinitionName}</h4>
                                <span class="badge" style="
                                    background-color: #6c757d;
                                    color: white;
                                    padding: 3px 8px;
                                    border-radius: 12px;
                                    font-size: 12px;
                                ">
                                    已完成
                                </span>
                            </div>
                            <div style="margin-bottom: 10px; font-size: 14px; color: #666;">
                                <strong>实例ID:</strong> ${instance.id}
                            </div>
                            <div style="margin-bottom: 10px; font-size: 14px; color: #666;">
                                <strong>开始时间:</strong> ${DateUtils.format(instance.startTime)}
                            </div>
                            <div style="margin-bottom: 10px; font-size: 14px; color: #666;">
                                <strong>结束时间:</strong> ${DateUtils.format(instance.endTime)}
                            </div>
                            <div style="display: flex; gap: 10px; margin-top: 15px;">
                                <button class="btn btn-primary btn-sm" data-instance-id="${instance.id}" data-action="history">
                                    查看历史
                                </button>
                                <button class="btn btn-danger btn-sm" data-instance-id="${instance.id}" data-action="delete">
                                    删除
                                </button>
                            </div>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
        
        // 添加实例操作事件
        addInstanceListeners();
    }
    
    // 添加实例操作事件
    function addInstanceListeners() {
        DOMUtils.on(DOMUtils.$$('.process-instance button'), 'click', function() {
            const instanceId = this.getAttribute('data-instance-id');
            const action = this.getAttribute('data-action');
            
            if (action === 'suspend') {
                AnimationUtils.pulse(this, 300);
                Notification.info('暂停流程实例功能将在后续版本中实现');
            } else if (action === 'delete') {
                const result = FormUtils.confirm('确定要删除这个流程实例吗？此操作不可撤销。');
                if (result) {
                    // 查找要删除的元素并添加动画
                    const element = this.closest('.process-instance');
                    AnimationUtils.fadeOut(element, 300);
                    Notification.success('流程实例已删除');
                }
            } else if (action === 'history') {
                AnimationUtils.pulse(this, 300);
                Notification.info('查看历史功能将在后续版本中实现');
            }
        });
    }
    
    // 处理BPMN文件上传
    DOMUtils.on(uploadForm, 'submit', async function(event) {
        event.preventDefault();
        
        const fileInput = DOMUtils.$('#bpmn-file', this);
        const file = fileInput.files[0];
        
        if (!file) {
            showUploadMessage('请选择一个BPMN文件', 'error');
            AnimationUtils.shake(fileInput);
            return;
        }
        
        // 验证文件类型
        if (!file.name.endsWith('.bpmn')) {
            showUploadMessage('请上传.bpmn格式的文件', 'error');
            AnimationUtils.shake(fileInput);
            return;
        }
        
        // 显示加载状态
        const submitBtn = DOMUtils.$('button[type="submit"]', this);
        const originalText = submitBtn.textContent;
        DOMUtils.css(submitBtn, { disabled: true });
        submitBtn.textContent = '上传中...';
        
        try {
            const result = await API.uploadBpmn(file);
            
            if (result.success) {
                showUploadMessage(result.message, 'success');
                
                // 清空文件输入
                fileInput.value = '';
                
                // 刷新流程定义列表
                loadProcessDefinitions();
            } else {
                showUploadMessage(result.message || '上传失败', 'error');
            }
        } catch (error) {
            showUploadMessage(`上传失败: ${error.message}`, 'error');
        } finally {
            // 恢复按钮状态
            DOMUtils.css(submitBtn, { disabled: false });
            submitBtn.textContent = originalText;
        }
    });
    
    // 显示上传消息
    function showUploadMessage(message, type = 'info') {
        uploadMessage.className = `alert alert-${type === 'error' ? 'danger' : type}`;
        uploadMessage.textContent = message;
        DOMUtils.css(uploadMessage, { display: 'block' });
        
        // 添加入场动画
        AnimationUtils.fadeIn(uploadMessage);
        
        // 3秒后清除消息（成功时）
        if (type === 'success') {
            setTimeout(() => {
                AnimationUtils.fadeOut(uploadMessage, 500);
                setTimeout(() => {
                    DOMUtils.css(uploadMessage, { display: 'none' });
                }, 500);
            }, 3000);
        }
    }
    
    // 格式化日期函数已由工具类提供
    
    // 刷新流程定义列表，添加点击动画
    DOMUtils.on(refreshDefinitionsBtn, 'click', function() {
        AnimationUtils.spin(this.querySelector('i') || this, 500);
        loadProcessDefinitions();
    });
    
    // 启动流程按钮，添加点击动画
    DOMUtils.on(startProcessBtn, 'click', function() {
        AnimationUtils.pulse(this, 300);
        // 直接启动默认流程
        startProcess();
    });
    
    // 初始加载数据，添加加载顺序
    loadEngineInfo();
    setTimeout(loadProcessDefinitions, 300);
    setTimeout(loadActiveInstances, 600);
    setTimeout(loadCompletedInstances, 900);
});
