<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>租户登录</h2>
        <p>使用APP ID和密钥登录</p>
      </div>
      
      <div class="login-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="租户登录" name="tenant">
            <el-form ref="tenantForm" :model="tenantForm" :rules="tenantRules" label-width="100px">
              <el-form-item label="APP ID" prop="appId">
                <el-input v-model="tenantForm.appId" placeholder="请输入APP ID"></el-input>
              </el-form-item>
              <el-form-item label="密钥" prop="secretKey">
                <el-input v-model="tenantForm.secretKey" placeholder="请输入密钥" show-password></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleTenantLogin" :loading="loading">登录</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="用户登录" name="user">
            <el-form ref="userForm" :model="userForm" :rules="userRules" label-width="100px">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="userForm.username" placeholder="请输入用户名"></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="userForm.password" placeholder="请输入密码" show-password></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUserLogin" :loading="loading">登录</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <div class="login-footer">
        <p><a href="/register">注册租户</a></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from '@/utils/axiosConfig'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('tenant')

// 租户登录表单
const tenantForm = reactive({
  appId: '',
  secretKey: ''
})

// 用户登录表单
const userForm = reactive({
  username: '',
  password: ''
})

// 租户登录验证规则
const tenantRules = {
  appId: [
    { required: true, message: '请输入APP ID', trigger: 'blur' }
  ],
  secretKey: [
    { required: true, message: '请输入密钥', trigger: 'blur' }
  ]
}

// 用户登录验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 租户登录处理
const handleTenantLogin = () => {
  loading.value = true
  
  axios.post('/auth/tenant/login', tenantForm)
    .then(response => {
      // 保存登录信息到本地存储
      localStorage.setItem('user', JSON.stringify({
        token: response.token,
        username: response.username,
        role: response.role
      }))
      
      // 保存租户信息
      localStorage.setItem('tenantInfo', JSON.stringify({
        appId: tenantForm.appId,
        secretKey: tenantForm.secretKey
      }))
      
      ElMessage.success('登录成功')
      // 跳转到租户控制台
      router.push('/tenant/dashboard')
    })
    .catch(error => {
      ElMessage.error('登录失败: ' + (error.message || '请稍后重试'))
    })
    .finally(() => {
      loading.value = false
    })
}

// 用户登录处理
const handleUserLogin = () => {
  loading.value = true
  
  axios.post('/auth/login', userForm)
    .then(response => {
      // 保存登录信息到本地存储
      localStorage.setItem('user', JSON.stringify({
        token: response.token,
        username: response.username,
        role: response.role
      }))
      
      ElMessage.success('登录成功')
      // 跳转到系统首页
      router.push('/')
    })
    .catch(error => {
      ElMessage.error('登录失败: ' + (error.message || '请稍后重试'))
    })
    .finally(() => {
      loading.value = false
    })
}
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 500px;
  padding: 30px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #1890ff;
  margin-bottom: 10px;
}

.login-header p {
  color: #86909c;
  font-size: 14px;
}

.login-tabs {
  margin: 20px 0;
}

.login-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
}

.login-footer a {
  color: #1890ff;
  text-decoration: none;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>
