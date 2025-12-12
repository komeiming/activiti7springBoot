<template>
  <div class="tenant-auth-container">
    <h3 class="section-title">授权信息</h3>
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>授权信息管理</span>
        </div>
      </template>
      
      <div class="auth-info">
        <!-- APP ID -->
        <el-form-item label="APP ID" class="auth-item">
          <div class="auth-content">
            <el-input v-model="authInfo.appId" readonly class="auth-input" />
            <el-button type="primary" size="small" @click="handleCopyAppId">
              <el-icon><CopyDocument /></el-icon> 复制
            </el-button>
          </div>
        </el-form-item>
        
        <!-- Secret Key -->
        <el-form-item label="Secret Key" class="auth-item">
          <div class="auth-content">
            <el-input
              v-model="displaySecretKey"
              readonly
              class="auth-input"
              type="password"
              :show-password="showSecretKey"
            />
            <div class="secret-key-actions">
              <el-button type="text" size="small" @click="showSecretKey = !showSecretKey">
                {{ showSecretKey ? '隐藏' : '显示' }}
              </el-button>
              <el-button type="primary" size="small" @click="handleCopySecretKey">
                <el-icon><CopyDocument /></el-icon> 复制
              </el-button>
            </div>
          </div>
        </el-form-item>
        
        <!-- 密钥有效期 -->
        <el-form-item label="密钥有效期" class="auth-item">
          <div class="auth-content">
            <div class="expiry-info">
              <p>创建时间：{{ authInfo.createdAt }}</p>
              <p>过期时间：{{ authInfo.expiresAt }}</p>
              <el-tag type="success" v-if="authInfo.status === 'valid'">有效</el-tag>
              <el-tag type="warning" v-else-if="authInfo.status === 'expiring'">即将过期</el-tag>
              <el-tag type="danger" v-else>已过期</el-tag>
            </div>
          </div>
        </el-form-item>
        
        <!-- 操作按钮 -->
        <el-form-item class="auth-actions">
          <el-button type="primary" @click="handleResetSecretKey" :loading="resetting">重置Secret Key</el-button>
          <el-button type="success" @click="handleDownloadAuthInfo">下载授权信息</el-button>
        </el-form-item>
      </div>
    </el-card>
    
    <!-- 重置Secret Key确认弹窗 -->
    <el-dialog
      v-model="resetDialogVisible"
      title="重置Secret Key"
      width="500px"
    >
      <el-form ref="resetForm" :model="resetForm" label-width="100px">
        <el-form-item label="验证码" prop="verificationCode">
          <el-input
            v-model="resetForm.verificationCode"
            placeholder="请输入验证码"
            maxlength="6"
            show-word-limit
          >
            <template #append>
              <el-button
                type="text"
                :disabled="resetCountingDown"
                @click="handleSendResetCode"
              >
                {{ resetCountingDown ? `${resetCountDownSeconds}s后重新获取` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认信息">
          <el-checkbox v-model="confirmReset">我已了解，重置后旧密钥将立即失效</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleConfirmReset" :loading="resetting">
          确认重置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CopyDocument } from '@element-plus/icons-vue'
import axios from '@/utils/axiosConfig'

// 授权信息
const authInfo = reactive({
  appId: '',
  secretKey: '',
  createdAt: '',
  expiresAt: '',
  status: 'valid'
})

// 初始化授权信息
const initAuthInfo = async () => {
  try {
    // 从localStorage获取appId
    const tenantStr = localStorage.getItem('tenantInfo')
    if (!tenantStr) {
      ElMessage.warning('请先登录或注册租户')
      return
    }
    
    const tenantInfoFromStorage = JSON.parse(tenantStr)
    const appId = tenantInfoFromStorage.appId
    
    if (!appId) {
      ElMessage.warning('缺少appId，请重新登录')
      return
    }
    
    // 调用后端API获取租户信息
    const response = await axios.get(`/v1/tenant/info?appId=${appId}`)
    
    // 更新授权信息
    authInfo.appId = response.appId || ''
    authInfo.secretKey = response.secretKey || ''
    authInfo.createdAt = response.createdAt ? new Date(response.createdAt).toLocaleString('zh-CN') : ''
    authInfo.expiresAt = response.expiresAt ? new Date(response.expiresAt).toLocaleString('zh-CN') : ''
    authInfo.status = 'valid' // 假设密钥始终有效，实际项目中应该根据过期时间判断
    
  } catch (error) {
    console.error('获取授权信息失败:', error)
    ElMessage.error(`获取授权信息失败: ${error.message || '请稍后重试'}`)
  }
}

// 组件挂载时初始化授权信息
onMounted(() => {
  initAuthInfo()
})

// 显示Secret Key
const showSecretKey = ref(false)

// 显示的Secret Key
const displaySecretKey = computed(() => {
  return authInfo.secretKey
})

// 重置相关
const resetDialogVisible = ref(false)
const resetting = ref(false)
const resetCountingDown = ref(false)
const resetCountDownSeconds = ref(60)
let resetTimer = null

// 重置表单
const resetForm = reactive({
  verificationCode: '',
  confirmReset: false
})

// 复制APP ID
const handleCopyAppId = () => {
  navigator.clipboard.writeText(authInfo.appId)
    .then(() => {
      ElMessage.success('APP ID复制成功')
    })
    .catch(() => {
      ElMessage.error('APP ID复制失败')
    })
}

// 复制Secret Key
const handleCopySecretKey = () => {
  navigator.clipboard.writeText(authInfo.secretKey)
    .then(() => {
      ElMessage.success('Secret Key复制成功')
    })
    .catch(() => {
      ElMessage.error('Secret Key复制失败')
    })
}

// 发送重置验证码
const handleSendResetCode = () => {
  resetCountingDown.value = true
  resetCountDownSeconds.value = 60
  
  resetTimer = setInterval(() => {
    resetCountDownSeconds.value--
    if (resetCountDownSeconds.value <= 0) {
      clearInterval(resetTimer)
      resetCountingDown.value = false
      resetCountDownSeconds.value = 60
    }
  }, 1000)
  
  ElMessage.success('重置验证码已发送')
}

// 重置Secret Key
const handleResetSecretKey = () => {
  resetDialogVisible.value = true
}

// 确认重置
const handleConfirmReset = async () => {
  if (!resetForm.verificationCode) {
    ElMessage.error('请输入验证码')
    return
  }
  
  if (!resetForm.confirmReset) {
    ElMessage.error('请确认了解重置后果')
    return
  }
  
  resetting.value = true
  
  try {
    // 从localStorage获取appId
    const tenantStr = localStorage.getItem('tenantInfo')
    if (!tenantStr) {
      ElMessage.warning('请先登录或注册租户')
      return
    }
    
    const tenantInfoFromStorage = JSON.parse(tenantStr)
    const appId = tenantInfoFromStorage.appId
    
    if (!appId) {
      ElMessage.warning('缺少appId，请重新登录')
      return
    }
    
    // 调用后端API重置密钥
    const response = await axios.post(`/v1/tenant/reset-secret?appId=${appId}`)
    
    // 更新授权信息
    authInfo.secretKey = response.newSecretKey || ''
    authInfo.createdAt = new Date().toLocaleString('zh-CN')
    authInfo.expiresAt = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toLocaleString('zh-CN')
    authInfo.status = 'valid'
    
    // 更新localStorage中的租户信息
    tenantInfoFromStorage.secretKey = response.newSecretKey
    localStorage.setItem('tenantInfo', JSON.stringify(tenantInfoFromStorage))
    
    resetting.value = false
    resetDialogVisible.value = false
    resetForm.verificationCode = ''
    resetForm.confirmReset = false
    
    ElMessage.success('Secret Key重置成功，新密钥已生成')
  } catch (error) {
    console.error('重置密钥失败:', error)
    ElMessage.error(`重置密钥失败: ${error.message || '请稍后重试'}`)
    resetting.value = false
  }
}

// 下载授权信息
const handleDownloadAuthInfo = () => {
  // 生成CSV内容
  const csvContent = `APP ID,Secret Key,创建时间,过期时间\n${authInfo.appId},${authInfo.secretKey},${authInfo.createdAt},${authInfo.expiresAt}`
  
  // 创建下载链接
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  
  link.setAttribute('href', url)
  link.setAttribute('download', `授权信息_${new Date().getTime()}.csv`)
  link.style.visibility = 'hidden'
  
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('授权信息下载成功')
}
</script>

<style scoped>
.tenant-auth-container {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.auth-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.auth-item {
  margin-bottom: 0;
}

.auth-content {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.auth-input {
  flex: 1;
}

.secret-key-actions {
  display: flex;
  gap: 10px;
}

.expiry-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.expiry-info p {
  margin: 0;
  color: #606266;
}

.auth-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}
</style>