<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-container">
          <img src="/bayer-logo.png" alt="系统Logo" class="login-logo">
          <h2>零售门店业务管理平台</h2>
        </div>
        <p>请选择登录方式</p>
      </div>
      
      <div class="login-selection">
        <!-- 系统用户登录卡片 -->
        <div class="login-card" @click="goToSysLogin">
          <div class="login-card-icon">
            <el-icon><User /></el-icon>
          </div>
          <h3>系统用户登录</h3>
          <p>适用于内部系统管理员和企业员工</p>
          <el-button type="primary" size="small">前往登录</el-button>
        </div>
        
        <!-- 租户用户登录卡片 -->
        <div class="login-card" @click="goToTenantLogin">
          <div class="login-card-icon tenant-icon">
            <el-icon><Building /></el-icon>
          </div>
          <h3>租户用户登录</h3>
          <p>适用于第三方企业租户用户</p>
          <el-button type="success" size="small">前往登录</el-button>
        </div>
      </div>
      
      <div class="login-footer">
        <p style="font-size: 12px; color: #999; margin-top: 10px;">技术支持：contact@example.com</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User as UserIcon, Lock as LockIcon, CircleCheck } from '@element-plus/icons-vue'
import axios from '../utils/axiosConfig'
import UserService from '../services/UserService'

const router = useRouter()
const loginFormRef = ref()

// 登录表单
const loginForm = reactive({
  username: 'admin',
  password: '123456', // 使用演示系统的固定密码
  rememberMe: false,
  captcha: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名/手机号/邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 登录状态
const isLoading = ref(false)
const errorMessage = ref('')

// 验证码相关
const showCaptcha = ref(false)
const loginFailCount = ref(0)
const captchaText = ref('')

// 生成随机验证码
const generateCaptcha = () => {
  const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  let result = ''
  for (let i = 0; i < 6; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

// 刷新验证码
const refreshCaptcha = () => {
  captchaText.value = generateCaptcha()
}

// 跳转到忘记密码页面
const goToForgotPassword = () => {
  router.push('/forgot-password')
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}

// 跳转到系统用户登录页面
const goToSysLogin = () => {
  router.push('/login-sys')
}

// 跳转到租户用户登录页面
const goToTenantLogin = () => {
  router.push('/login-tenant')
}

// 处理登录
const handleLogin = async () => {
  // 表单验证
  await loginFormRef.value.validate().catch(err => {
    console.error('表单验证失败:', err)
    return
  })
  
  isLoading.value = true
  errorMessage.value = ''
  
  try {
    // 调用后端登录API
    const response = await axios.post('/auth/login', {
      username: loginForm.username,
      password: loginForm.password,
      captcha: showCaptcha.value ? loginForm.captcha : undefined
    })
    
    // 登录成功，重置失败计数
    loginFailCount.value = 0
    showCaptcha.value = false
    
    // 创建用户信息对象
    const userInfo = {
      username: loginForm.username,
      token: response.token || 'token-' + Date.now(),
      name: response.name || loginForm.username,
      role: response.role || 'admin',
      roles: ['ADMIN', 'admin'], // 确保admin用户有正确的角色数组
      department: response.department || '技术部',
      position: response.position || '开发工程师',
      loginTime: new Date().toISOString()
    }
    
    // 保存用户信息到本地存储
    UserService.setCurrentUser(userInfo)
    
    // 如果选择记住我，保存用户名
    if (loginForm.rememberMe) {
      localStorage.setItem('rememberedUsername', loginForm.username)
    } else {
      localStorage.removeItem('rememberedUsername')
    }
    
    console.log('登录成功:', userInfo)
    
    // 显示成功消息
    ElMessage.success('登录成功')
    
    // 检查是否有重定向路径
    const redirectPath = sessionStorage.getItem('redirectPath')
    // 如果有重定向路径，则跳转到该路径，否则跳转到默认的仪表盘
    router.push(redirectPath || '/dashboard')
  } catch (error) {
    console.error('登录失败:', error)
    
    // 增加登录失败计数
    loginFailCount.value++
    
    // 连续3次登录失败，显示验证码
    if (loginFailCount.value >= 3) {
      showCaptcha.value = true
      refreshCaptcha()
    }
    
    // 提供更具体的错误信息
    errorMessage.value = error.response?.data?.message || '登录失败，请检查用户名和密码'
    ElMessage.error(errorMessage.value)
  } finally {
    isLoading.value = false
  }
}

// 组件挂载时检查是否有记住的用户名
onMounted(() => {
  // 异步加载本地存储数据，减少初始化阻塞
  setTimeout(() => {
    const savedUsername = localStorage.getItem('rememberedUsername')
    if (savedUsername) {
      loginForm.username = savedUsername
      loginForm.rememberMe = true
    }
    
    // 检查是否需要显示验证码（如果之前有连续登录失败记录）
    const savedFailCount = localStorage.getItem('loginFailCount')
    if (savedFailCount) {
      loginFailCount.value = parseInt(savedFailCount)
      if (loginFailCount.value >= 3) {
        showCaptcha.value = true
        refreshCaptcha()
      }
    }
  }, 100)
})
</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  background-color: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px;
  transition: transform 0.3s ease;
}

.login-box:hover {
  transform: translateY(-5px);
}

.login-header {
  margin-bottom: 30px;
}

.logo-container {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #00b42a 0%, #1296db 100%);
  padding: 20px 10px;
  border-radius: 8px 8px 0 0;
  margin: -40px -40px 20px -40px;
}

.login-logo {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  margin-right: 5px;
}

.login-header h2 {
  color: white;
  font-size: 20px;
  margin: 0;
  font-weight: 600;
}

.login-header p {
  color: #64748b;
  margin: 0;
  font-size: 14px;
  text-align: center;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.form-control {
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.form-control:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
}

.remember-me input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #3b82f6;
}

.remember-me label {
  font-size: 14px;
  color: #64748b;
  font-weight: normal;
}

.login-btn {
  padding: 14px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.login-btn:hover:not(:disabled) {
  background-color: #2563eb;
}

.login-btn:disabled {
  background-color: #94a3b8;
  cursor: not-allowed;
}

.error-message {
  background-color: #fee2e2;
  color: #dc2626;
  padding: 12px;
  border-radius: 6px;
  font-size: 14px;
  text-align: center;
}

/* 验证码样式 */
.captcha-image {
  width: 120px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 2px;
  border-radius: 8px;
  cursor: pointer;
  user-select: none;
  transition: transform 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.captcha-image:hover {
  transform: scale(1.05);
}

/* 登录页脚样式 */
.login-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #64748b;
}

.login-footer p {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-box {
    padding: 30px 20px;
  }
  
  .login-header h2 {
    font-size: 20px;
  }
  
  .captcha-image {
    width: 100px;
    height: 36px;
    font-size: 16px;
  }
}
</style>