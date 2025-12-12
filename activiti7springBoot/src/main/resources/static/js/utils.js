// 工具类集合

// DOM操作工具
const DOMUtils = {
    // 选择单个元素
    $: function(selector, context = document) {
        return context.querySelector(selector);
    },
    
    // 选择多个元素
    $$: function(selector, context = document) {
        return Array.from(context.querySelectorAll(selector));
    },
    
    // 设置样式
    css: function(element, styles) {
        if (element && styles) {
            Object.assign(element.style, styles);
        }
        return element;
    },
    
    // 添加事件监听器
    on: function(element, event, handler, options = false) {
        if (Array.isArray(element)) {
            element.forEach(el => {
                if (el && el.addEventListener) {
                    el.addEventListener(event, handler, options);
                }
            });
        } else if (element && element.addEventListener) {
            element.addEventListener(event, handler, options);
        }
        return element;
    },
    
    // 移除事件监听器
    off: function(element, event, handler, options = false) {
        if (Array.isArray(element)) {
            element.forEach(el => {
                if (el && el.removeEventListener) {
                    el.removeEventListener(event, handler, options);
                }
            });
        } else if (element && element.removeEventListener) {
            element.removeEventListener(event, handler, options);
        }
        return element;
    },
    
    // 获取或设置元素文本
    text: function(element, text) {
        if (element) {
            if (text !== undefined) {
                element.textContent = text;
                return element;
            } else {
                return element.textContent;
            }
        }
        return text === undefined ? null : element;
    }
};

// 动画工具
const AnimationUtils = {
    // 滑动进入动画
    slideInWithTranslate: function(element) {
        if (!element) return;
        
        // 重置样式
        element.style.display = 'block';
        element.style.transform = 'translateY(20px)';
        element.style.opacity = '0';
        element.style.transition = 'transform 0.3s ease, opacity 0.3s ease';
        
        // 强制回流
        element.offsetHeight;
        
        // 应用动画
        setTimeout(() => {
            element.style.transform = 'translateY(0)';
            element.style.opacity = '1';
        }, 10);
    }
};

// 日期工具
const DateUtils = {
    // 格式化相对时间
    formatRelative: function(dateString) {
        if (!dateString) return '未知';
        
        try {
            const date = new Date(dateString);
            const now = new Date();
            const diffMs = now - date;
            const diffMins = Math.floor(diffMs / 60000);
            const diffHours = Math.floor(diffMins / 60);
            const diffDays = Math.floor(diffHours / 24);
            
            if (diffMins < 1) {
                return '刚刚';
            } else if (diffMins < 60) {
                return `${diffMins}分钟前`;
            } else if (diffHours < 24) {
                return `${diffHours}小时前`;
            } else if (diffDays < 30) {
                return `${diffDays}天前`;
            } else {
                // 格式化日期
                return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
            }
        } catch (error) {
            return dateString;
        }
    }
};

// 通知工具
const Notification = {
    error: function(message) {
        console.error('[通知]', message);
        // 在实际应用中可以实现更复杂的通知系统
        if (window.alert) {
            // 避免过度使用alert，但为了测试可以使用
            // alert(message);
        }
    }
};

// 全局可用
window.DOMUtils = DOMUtils;
window.AnimationUtils = AnimationUtils;
window.DateUtils = DateUtils;
window.Notification = Notification;