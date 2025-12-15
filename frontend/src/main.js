import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './assets/styles/global.css'
import App from './App.vue'
import router from './router'
// 导入Element Plus组件库
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 导入axios用于网络请求错误拦截
import axios from 'axios'

// 全局日志工具
function logger(logLevel, module, message, data) {
  const timestamp = new Date().toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    fractionalSecondDigits: 3
  });
  
  const logPrefix = `[${logLevel.toUpperCase()}] [${module}] [${timestamp}]`;
  
  // 直接使用console.log/info/warn/error，让浏览器原生处理对象序列化
  // 避免JSON.stringify导致的循环引用问题
  switch (logLevel) {
    case 'info': console.info(logPrefix, message, data); break;
    case 'warn': console.warn(logPrefix, message, data); break;
    case 'error': console.error(logPrefix, message, data); break;
    default: console.log(logPrefix, message, data);
  }
}

// 暴露日志工具到全局
window.appLogger = {
  info: (module, message, data) => logger('info', module, message, data),
  warn: (module, message, data) => logger('warn', module, message, data),
  error: (module, message, data) => logger('error', module, message, data)
};

logger('info', 'App', '开始初始化Vue应用...');

// 创建Vue应用实例
const app = createApp(App);

// 添加全局错误处理
app.config.errorHandler = (err, instance, info) => {
  const errorInfo = {
    error: {
      message: err.message || '未知错误',
      stack: err.stack || '无堆栈信息',
      name: err.name || 'Error'
    },
    component: instance ? {
      type: instance.type?.name || 'UnknownComponent',
      props: instance.props || {}
    } : null,
    lifecycle: info
  };
  
  logger('error', 'App', '全局Vue错误:', errorInfo);
  
  // 显示友好的错误提示
  if (window.ElMessage) {
    window.ElMessage.error('应用出现错误: ' + (err.message || '请刷新页面重试'));
  }
};

// 设置Vue运行时警告处理器
app.config.warnHandler = (msg, instance, trace) => {
  logger('warn', 'App', 'Vue运行时警告:', {
    message: msg,
    component: instance ? instance.type?.name : 'Unknown',
    trace: trace
  });
};

// 设置全局未捕获错误处理器
window.addEventListener('error', (event) => {
  // 忽略ResizeObserver循环错误，这是浏览器级别的警告，不会影响功能
  if (event.message === 'ResizeObserver loop completed with undelivered notifications.') {
    return;
  }
  
  logger('error', 'App', '未捕获的JavaScript错误:', {
    message: event.message,
    filename: event.filename,
    lineno: event.lineno,
    colno: event.colno,
    error: event.error
  });
});

// 设置Promise拒绝处理器
window.addEventListener('unhandledrejection', (event) => {
  logger('error', 'App', '未处理的Promise拒绝:', {
    reason: event.reason,
    promise: event.promise
  });
});

// 设置axios拦截器
axios.interceptors.request.use(
  (config) => {
    // 请求配置中添加唯一标识，用于跟踪请求
    config.requestId = Date.now().toString(36) + Math.random().toString(36).substr(2);
    
    logger('info', 'Axios', '请求发送:', {
      method: config.method?.toUpperCase(),
      url: config.url,
      requestId: config.requestId,
      params: config.params,
      hasData: !!config.data
    });
    
    // 设置请求超时
    config.timeout = config.timeout || 30000; // 默认30秒超时
    
    return config;
  },
  (error) => {
    logger('error', 'Axios', '请求配置错误:', error);
    return Promise.reject(error);
  }
);

axios.interceptors.response.use(
  (response) => {
    const config = response.config;
    logger('info', 'Axios', '请求成功:', {
      requestId: config?.requestId,
      url: config?.url,
      status: response.status,
      statusText: response.statusText
    });
    return response;
  },
  (error) => {
    const config = error.config || {};
    const errorInfo = {
      requestId: config.requestId,
      url: config.url,
      method: config.method,
      timeout: config.timeout,
      hasResponse: !!error.response,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      message: error.message,
      stack: error.stack
    };
    
    logger('error', 'Axios', '请求失败:', errorInfo);
    
    // 显示友好的错误提示
    let errorMessage = '网络请求失败';
    
    if (error.response) {
      // 服务器返回错误状态码
      switch (error.response.status) {
        case 401:
          errorMessage = '未授权访问，请重新登录';
          // 可以在这里处理登出逻辑
          break;
        case 403:
          errorMessage = '没有权限执行此操作';
          break;
        case 404:
          errorMessage = '请求的资源不存在';
          break;
        case 500:
          errorMessage = '服务器内部错误';
          break;
        default:
          errorMessage = error.response.data?.message || error.response.statusText || `请求失败 (${error.response.status})`;
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      if (error.message?.includes('timeout')) {
        errorMessage = '请求超时，请检查网络连接';
      } else {
        errorMessage = '服务器无响应，请稍后重试';
      }
    } else {
      // 请求配置出错
      errorMessage = error.message || '请求配置错误';
    }
    
    // 使用全局消息组件显示错误
    if (window.ElMessage) {
      window.ElMessage.error(errorMessage);
    }
    
    return Promise.reject(error);
  }
);

// 使用路由
app.use(router);
// 使用Element Plus
app.use(ElementPlus);
// 使用Pinia状态管理
const pinia = createPinia();
app.use(pinia);

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

// 挂载应用到DOM
app.mount('#app');

logger('info', 'App', 'Vue应用已挂载到#app元素');

// 暴露到全局以便调试
window.vueApp = app;
window.vueRouter = router;

// 引入调试脚本
logger('info', 'App', '加载调试脚本');
import('../debug.js')
  .then(() => {
    logger('info', 'App', '调试脚本加载完成');
  })
  .catch(err => {
    logger('error', 'App', '调试脚本加载失败:', err);
  });

// 延迟获取Element Plus的Message组件，确保它已经被正确初始化
setTimeout(() => {
  try {
    // 从Element Plus中直接导入Message组件
    const { ElMessage } = require('element-plus');
    window.ElMessage = ElMessage;
  } catch (error) {
    logger('warn', 'App', '无法全局访问Element Plus Message组件:', error);
  }
}, 100);
