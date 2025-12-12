// 登录页面逻辑
 document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const errorMessage = document.getElementById('error-message');
    
    // 检查用户是否已经登录
    if (Auth.isAuthenticated()) {
        // 如果已登录，重定向到仪表盘
        window.location.href = 'dashboard.html';
        return;
    }
    
    loginForm.addEventListener('submit', async function(event) {
        event.preventDefault();
        
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        
        // 清除之前的错误信息
        errorMessage.textContent = '';
        
        // 显示加载状态
        const loginBtn = this.querySelector('button[type="submit"]');
        const originalText = loginBtn.textContent;
        loginBtn.disabled = true;
        loginBtn.textContent = '登录中...';
        
        try {
            // 执行登录 - 不再需要传递username参数给login函数
            const result = await Auth.login(username, password);
            
            if (result.success) {
                // 登录成功，重定向到仪表盘
                window.location.href = 'dashboard.html';
            } else {
                // 显示错误信息
                errorMessage.textContent = result.error || '登录失败，请检查用户名和密码';
            }
        } catch (error) {
            // 处理异常
            errorMessage.textContent = '登录过程中发生错误: ' + error.message;
        } finally {
            // 恢复按钮状态
            loginBtn.disabled = false;
            loginBtn.textContent = originalText;
        }
    });
    
    // 添加键盘事件处理
    document.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            const focusedElement = document.activeElement;
            if (focusedElement.tagName === 'INPUT') {
                loginForm.dispatchEvent(new Event('submit'));
            }
        }
    });
});
