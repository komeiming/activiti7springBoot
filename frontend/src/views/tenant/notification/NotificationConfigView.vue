<template>
  <div class="notification-config-container">
    <h3 class="section-title">通知配置</h3>
    
    <el-card shadow="hover">
      <el-tabs v-model="activeConfigTab" class="config-tabs">
        <el-tab-pane label="回调配置" name="callback"></el-tab-pane>
        <el-tab-pane label="发送频率限制" name="rate"></el-tab-pane>
      </el-tabs>
      
      <el-form ref="configFormRef" :model="configForm" :rules="rules" label-width="150px" class="config-form" v-loading="loading">
        <!-- 回调配置 -->
        <template v-if="activeConfigTab === 'callback'">
          <el-form-item label="回调地址" prop="callbackUrl">
            <el-input
              v-model="configForm.callbackUrl"
              placeholder="请输入回调地址"
              class="callback-input"
            />
          </el-form-item>
          
          <el-form-item label="回调超时时间（秒）" prop="callbackTimeout">
            <el-input-number
              v-model="configForm.callbackTimeout"
              :min="1"
              :max="60"
              :step="1"
              placeholder="请输入回调超时时间"
            />
          </el-form-item>
          
          <el-form-item label="回调重试次数" prop="callbackRetryCount">
            <el-input-number
              v-model="configForm.callbackRetryCount"
              :min="0"
              :max="5"
              :step="1"
              placeholder="请输入回调重试次数"
            />
          </el-form-item>
          
          <el-form-item label="回调密钥" prop="callbackSecret">
            <el-input
              v-model="configForm.callbackSecret"
              placeholder="系统自动生成的回调密钥"
              readonly
              type="password"
              :show-password="showCallbackSecret"
            >
              <template #append>
                <el-button type="primary" size="small" @click="handleCopyCallbackSecret">
                  <el-icon><CopyDocument /></el-icon> 复制
                </el-button>
                <el-button type="text" size="small" @click="showCallbackSecret = !showCallbackSecret">
                  {{ showCallbackSecret ? '隐藏' : '显示' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
            <el-button @click="handleResetConfig">重置</el-button>
          </el-form-item>
        </template>
        
        <!-- 发送频率限制 -->
        <template v-else-if="activeConfigTab === 'rate'">
          <el-form-item label="每秒最大发送量">
            <el-input-number
              v-model="configForm.rateLimit.perSecond"
              :min="1"
              :max="1000"
              :step="1"
              placeholder="请输入每秒最大发送量"
            />
          </el-form-item>
          
          <el-form-item label="每分钟最大发送量">
            <el-input-number
              v-model="configForm.rateLimit.perMinute"
              :min="1"
              :max="10000"
              :step="1"
              placeholder="请输入每分钟最大发送量"
            />
          </el-form-item>
          
          <el-form-item label="每小时最大发送量">
            <el-input-number
              v-model="configForm.rateLimit.perHour"
              :min="1"
              :max="100000"
              :step="100"
              placeholder="请输入每小时最大发送量"
            />
          </el-form-item>
          
          <el-form-item label="每日最大发送量">
            <el-input-number
              v-model="configForm.rateLimit.perDay"
              :min="1"
              :max="1000000"
              :step="1000"
              placeholder="请输入每日最大发送量"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
            <el-button @click="handleResetConfig">重置</el-button>
          </el-form-item>
        </template>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CopyDocument } from '@element-plus/icons-vue'
import axios from '@/utils/axiosConfig'

// 配置表单
const configForm = reactive({
  callbackUrl: '',
  callbackTimeout: 30,
  callbackRetryCount: 3,
  callbackSecret: '',
  rateLimit: {
    perSecond: 100,
    perMinute: 5000,
    perHour: 300000,
    perDay: 5000000
  }
})

// 标签页
const activeConfigTab = ref('callback')

// 显示回调密钥
const showCallbackSecret = ref(false)

// 加载状态
const loading = ref(false)

// 表单引用
const configFormRef = ref()

// 表单验证规则
const rules = {
  callbackUrl: [
    { required: false, message: '请输入回调地址', trigger: 'blur' },
    { type: 'url', message: '请输入有效的URL地址', trigger: 'blur' }
  ],
  callbackTimeout: [
    { required: true, message: '请输入回调超时时间', trigger: 'blur' },
    { type: 'number', min: 1, max: 60, message: '回调超时时间必须在1-60秒之间', trigger: 'blur' }
  ],
  callbackRetryCount: [
    { required: true, message: '请输入回调重试次数', trigger: 'blur' },
    { type: 'number', min: 0, max: 5, message: '回调重试次数必须在0-5次之间', trigger: 'blur' }
  ]
}

// 加载配置
const loadConfig = async () => {
  try {
    loading.value = true
    const response = await axios.get('/v1/notification/config')
    // 处理后端返回的数据结构，转换为前端表单需要的格式
    configForm.callbackUrl = response.callbackUrl || ''
    configForm.callbackTimeout = response.callbackTimeout || 30
    configForm.callbackRetryCount = response.callbackRetryCount || 3
    configForm.callbackSecret = response.callbackSecret || ''
    configForm.rateLimit.perSecond = response.perSecondLimit || 100
    configForm.rateLimit.perMinute = response.perMinuteLimit || 5000
    configForm.rateLimit.perHour = response.perHourLimit || 300000
    configForm.rateLimit.perDay = response.perDayLimit || 5000000
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error('加载配置失败: ' + (error.message || '请稍后重试'))
  } finally {
    loading.value = false
  }
}

// 保存配置
const handleSaveConfig = async () => {
  if (configFormRef.value) {
    try {
      await configFormRef.value.validate()
      loading.value = true
      
      // 转换前端表单格式为后端需要的格式
      const configData = {
        callbackUrl: configForm.callbackUrl,
        callbackTimeout: configForm.callbackTimeout,
        callbackRetryCount: configForm.callbackRetryCount,
        callbackSecret: configForm.callbackSecret,
        perSecondLimit: configForm.rateLimit.perSecond,
        perMinuteLimit: configForm.rateLimit.perMinute,
        perHourLimit: configForm.rateLimit.perHour,
        perDayLimit: configForm.rateLimit.perDay
      }
      
      await axios.put('/v1/notification/config', configData)
      ElMessage.success('配置保存成功')
    } catch (error) {
      if (error.name === 'Error') {
        console.error('保存配置失败:', error)
        ElMessage.error('保存配置失败: ' + (error.message || '请稍后重试'))
      }
    } finally {
      loading.value = false
    }
  }
}

// 重置配置
const handleResetConfig = () => {
  loadConfig() // 重新加载配置
}

// 复制回调密钥
const handleCopyCallbackSecret = () => {
  navigator.clipboard.writeText(configForm.callbackSecret)
    .then(() => {
      ElMessage.success('回调密钥复制成功')
    })
    .catch(() => {
      ElMessage.error('回调密钥复制失败')
    })
}

// 组件挂载时加载配置
onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.notification-config-container {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.config-tabs {
  margin-bottom: 20px;
}

.config-form {
  padding: 10px 0;
}

.callback-input {
  width: 400px;
}
</style>