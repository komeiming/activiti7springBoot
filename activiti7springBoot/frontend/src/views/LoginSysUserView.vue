<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-container">
          <img src="/bayer-logo.png" alt="系统Logo" class="login-logo">
          <h2>零售门店业务管理平台</h2>
        </div>
        <p>系统用户登录</p>
      </div>
      
      <div class="login-form">
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" :validate-on-rule-change="false">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username"
              placeholder="请输入系统用户名"
              @keyup.enter="handleLogin"
              :validate-event="false"
              clearable
            >
              <template #prefix>
                <el-icon><UserIcon /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              @keyup.enter="handleLogin"
              :validate-event="false"
              show-password
            >
              <template #prefix>
                <el-icon><LockIcon /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <!-- 验证码，连续3次登录失败后显示 -->
          <el-form-item v-if="showCaptcha" prop="captcha">
            <div style="display: flex; gap: 10px;">
              <el-input 
                v-model="loginForm.captcha"
                placeholder="请输入验证码"
                @keyup.enter="handleLogin"
                :validate-event="false"
                style="flex: 1;"
              >
                <template #prefix>
                  <el-icon><CircleCheck /></el-icon>
                </template>
              </el-input>
              <div class="captcha-image" @click="refreshCaptcha">
                {{ captchaText }}
              </div>
            </div>
          </el-form-item>
          
          <el-form-item>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <el-checkbox v-model="loginForm.rememberMe" :validate-event="false">记住我</el-checkbox>
              <el-button type="text" size="small" @click="goToForgotPassword">忘记密码？</el-button>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleLogin"
              :loading="isLoading"
              style="width: 100%"
            >
              系统用户登录
            </el-button>
          </el-form-item>
          
          <el-form-item>
            <el-alert 
              v-if="errorMessage" 
              :message="errorMessage" 
              type="error" 
              show-icon 
              :closable="true"
              @close="errorMessage = ''"
            >
            </el-alert>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="login-footer">
        <p>租户用户？<el-button type="text" @click="goToTenantLogin">前往租户登录</el-button></p>
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
  password: '123456', // 系统用户默认密码
  rememberMe: false,
  captcha: ''
})

// 简化的表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入系统用户名', trigger: 'submit' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'submit' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'submit' }
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

// 跳转到租户登录页面
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
    // 调用系统用户登录API
    const response = await axios.post('/auth/login', {
      username: loginForm.username,
      password: loginForm.password,
      captcha: showCaptcha.value ? loginForm.captcha : undefined
    })
    
    // 登录成功，重置失败计数
    loginFailCount.value = 0
    showCaptcha.value = false
    
    // 创建系统用户信息对象
    const userInfo = {
      username: loginForm.username,
      token: response.token || 'token-' + Date.now(),
      name: response.name || loginForm.username,
      role: response.role || 'admin',
      roles: response.roles || ['ADMIN', 'admin'],
      department: response.department || '技术部',
      position: response.position || '系统管理员',
      loginTime: new Date().toISOString(),
      userType: 'system' // 标记为系统用户
    }
    
    // 保存用户信息到本地存储
    UserService.setCurrentUser(userInfo)
    
    // 如果选择记住我，保存用户名
    if (loginForm.rememberMe) {
      localStorage.setItem('rememberedSysUsername', loginForm.username)
    } else {
      localStorage.removeItem('rememberedSysUsername')
    }
    
    console.log('系统用户登录成功:', userInfo)
    
    // 显示成功消息
    ElMessage.success('登录成功')
    
    // 检查是否有重定向路径
    const redirectPath = sessionStorage.getItem('redirectPath')
    // 系统用户登录后跳转到仪表盘
    router.push(redirectPath || '/dashboard')
  } catch (error) {
    console.error('系统用户登录失败:', error)
    
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

// 组件挂载时检查是否有记住的系统用户名
onMounted(() => {
  // 异步加载本地存储数据，减少初始化阻塞
  setTimeout(() => {
    const savedUsername = localStorage.getItem('rememberedSysUsername')
    if (savedUsername) {
      loginForm.username = savedUsername
      loginForm.rememberMe = true
    }
    
    // 检查是否需要显示验证码（如果之前有连续登录失败记录）
    const savedFailCount = localStorage.getItem('sysLoginFailCount')
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
  font-weight: 500;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
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
