import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  // 移除baseURL，因为vite.config.js中的代理已经处理了路径映射
  // baseURL: '/api',
  timeout: 10000, // 请求超时时间
  // 不设置默认的Content-Type，让axios根据请求数据类型自动设置
})

/**
 * 生成HMAC-SHA256签名
 * @param {string} secretKey 密钥
 * @param {Object} params 请求参数
 * @param {string} timestamp 时间戳
 * @returns {string} 签名结果
 */
function generateSignature(secretKey, params, timestamp) {
  try {
    // 将参数按字典顺序排序
    const sortedParams = Object.keys(params)
      .sort()
      .reduce((obj, key) => {
        obj[key] = params[key]
        return obj
      }, {})
    
    // 构建待签名字符串
    let signatureStr = secretKey
    for (const [key, value] of Object.entries(sortedParams)) {
      signatureStr += key + value
    }
    signatureStr += timestamp
    
    // 使用Web Crypto API生成HMAC-SHA256签名
    const encoder = new TextEncoder()
    const keyData = encoder.encode(secretKey)
    const messageData = encoder.encode(signatureStr)
    
    return crypto.subtle.importKey(
      'raw',
      keyData,
      { name: 'HMAC', hash: 'SHA-256' },
      false,
      ['sign']
    )
    .then(key => {
      return crypto.subtle.sign('HMAC', key, messageData)
    })
    .then(signature => {
      // 将ArrayBuffer转换为Base64字符串
      const byteArray = new Uint8Array(signature)
      let binary = ''
      for (let i = 0; i < byteArray.byteLength; i++) {
        binary += String.fromCharCode(byteArray[i])
      }
      return btoa(binary)
    })
  } catch (error) {
    console.error('生成签名失败:', error)
    throw error
  }
}

// 请求拦截器
service.interceptors.request.use(
  async config => {
    // 尝试从多个可能的localStorage键获取token，确保兼容
    let token = null
    
    // 尝试从'userInfo'键获取（ProcessApply.vue使用的键）
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      try {
        const user = JSON.parse(userInfoStr)
        if (user.token) {
          token = user.token
        }
      } catch (error) {
        console.error('解析userInfo失败:', error)
      }
    }
    
    // 如果未找到token，尝试从'user'键获取（原有代码使用的键）
    if (!token) {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          const user = JSON.parse(userStr)
          if (user.token) {
            token = user.token
          }
        } catch (error) {
          console.error('解析user失败:', error)
        }
      }
    }
    
    // 还尝试直接从'token'键获取（UserService使用的键）
    if (!token) {
      token = localStorage.getItem('token')
    }
    
    // 如果找到token，添加到请求头
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 添加租户API鉴权支持
    // 检查是否是公有服务API路径，包括/api/v1/开头的请求
    if (config.url && (config.url.startsWith('/v1/') || config.url.startsWith('/api/v1/'))) {
      // 不要移除baseURL添加的/api前缀，因为vite.config.js中的代理会处理好路径映射
      // 尝试获取租户信息
      const tenantStr = localStorage.getItem('tenantInfo')
      if (tenantStr) {
        try {
          const tenantInfo = JSON.parse(tenantStr)
          if (tenantInfo.appId && tenantInfo.secretKey) {
            // 添加APP ID
            config.headers['X-APP-ID'] = tenantInfo.appId
            
            // 生成时间戳
            const timestamp = Date.now().toString()
            config.headers['X-TIMESTAMP'] = timestamp
            
            // 收集请求参数
            const params = {}
            
            // 处理GET请求的URL参数
            if (config.params) {
              Object.assign(params, config.params)
            }
            
            // 处理POST/PUT请求的body参数
            if (config.method && (config.method.toLowerCase() === 'post' || config.method.toLowerCase() === 'put') && config.data) {
              if (typeof config.data === 'object' && config.data !== null && !(config.data instanceof FormData)) {
                // 对于POST/PUT请求，直接使用JSON字符串作为body参数
                // 这是最可靠的方式，确保与后端的处理方式一致
                params.body = JSON.stringify(config.data)
              }
            }
            
            // 生成HMAC-SHA256签名
            console.log('生成签名的参数:', params);
            console.log('生成签名的时间戳:', timestamp);
            console.log('生成签名的密钥:', tenantInfo.secretKey);
            const signature = await generateSignature(tenantInfo.secretKey, params, timestamp)
            console.log('生成的签名:', signature);
            config.headers['X-SIGN'] = signature
          }
        } catch (error) {
          console.error('解析tenantInfo或生成签名失败:', error)
        }
      }
    }
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 对响应数据进行处理
    const res = response.data
    
    // 检查响应状态 - 允许200和201状态码通过
    if (response.status !== 200 && response.status !== 201) {
      const errorMsg = res.message || '未知错误'
      // 将错误信息附加到error对象上，但不在这里显示
      const error = new Error('请求失败: ' + errorMsg)
      error.responseData = res
      return Promise.reject(error)
    }
    
    // 检查业务状态码
    // 兼容CommonResponse格式
    if (res.code !== undefined) {
      if (res.code === 200 || res.code === 0) {
        // CommonResponse成功，返回data内容
        return res.data || res
      } else {
        // CommonResponse失败
        const errorMsg = res.message || '操作失败'
        const error = new Error(errorMsg)
        error.responseData = res
        return Promise.reject(error)
      }
    } else if (res.success === false) {
      // 兼容旧的success字段格式
      const errorMsg = res.message || '操作失败'
      const error = new Error(errorMsg)
      error.responseData = res
      return Promise.reject(error)
    }
    
    // 其他情况，直接返回响应
    return res
  },
  error => {
    // 处理响应错误
    console.error('响应错误:', error)
    
    // 检查是否是流程图请求
    const isDiagramRequest = error.config && error.config.url && error.config.url.includes('/diagram')
    
    // 处理网络错误
    if (!error.response) {
      if (!isDiagramRequest) {
        ElMessage.error('网络错误，请检查您的网络连接')
      }
      return Promise.reject(error)
    }
    
    // 处理401未授权错误
    if (error.response.status === 401) {
      // 检查是否是租户登录请求
      const isTenantLoginRequest = error.config && error.config.url && error.config.url.includes('/auth/tenant/login')
      // 检查是否是工作流或通知API请求
      const isApiRequest = error.config && error.config.url && (error.config.url.includes('/v1/workflow') || error.config.url.includes('/v1/notification'))
      
      if (isTenantLoginRequest || isApiRequest) {
        // 租户登录请求或API请求，不清除用户信息，不跳转，返回具体错误信息
        const errorMsg = error.response.data.message || '请求失败，请检查您的权限或配置'
        ElMessage.error(errorMsg)
        const enhancedError = new Error(errorMsg)
        enhancedError.response = error.response
        enhancedError.responseData = error.response.data
        return Promise.reject(enhancedError)
      } else {
        // 其他请求，正常处理401错误
        // 清除所有可能的用户信息存储
        localStorage.removeItem('user')
        localStorage.removeItem('userInfo')
        
        // 提供更友好的错误提示
        ElMessage.error('登录已过期或未授权，请重新登录')
        
        // 重定向到登录页面
        setTimeout(() => {
          window.location.href = '/login'
        }, 1000)
        return Promise.reject(error)
      }
    }
    
    // 处理403禁止访问错误
    if (error.response.status === 403) {
      if (!isDiagramRequest) {
        ElMessage.error('没有权限访问此资源')
      }
      return Promise.reject(error)
    }
    
    // 处理404资源不存在错误
    if (error.response.status === 404) {
      // 检查是否是流程图请求，如果是则不显示错误提示
      if (!isDiagramRequest) {
        ElMessage.error('请求的资源不存在')
      }
      return Promise.reject(error)
    }
    
    // 处理500服务器错误
    if (error.response.status === 500) {
      if (!isDiagramRequest) {
        ElMessage.error('服务器内部错误，请稍后重试')
      }
      return Promise.reject(error)
    }
    
    // 处理其他错误 - 不在这里显示错误消息，让调用组件决定如何显示
    const errorMsg = error.response.data.message || '请求失败'
    const enhancedError = new Error(errorMsg)
    enhancedError.response = error.response
    enhancedError.responseData = error.response.data
    return Promise.reject(enhancedError)
  }
)

export default service