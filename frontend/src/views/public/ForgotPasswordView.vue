<template>
  <div class="forgot-password-container">
    <div class="forgot-password-form-wrapper">
      <h1 class="form-title">忘记密码</h1>
      <p class="form-subtitle">请输入您的手机号或邮箱，我们将向您发送重置密码的验证码</p>
      
      <el-form ref="forgotPasswordForm" :model="formData" :rules="rules" label-width="0" class="forgot-password-form">
        <!-- 第一步：输入账号 -->
        <el-step-wizard v-model:active="activeStep" :steps="steps" class="step-wizard">
          <!-- 步骤1：输入手机号/邮箱 -->
          <template #step-0>
            <el-form-item prop="account">
              <el-input
                v-model="formData.account"
                placeholder="请输入手机号或邮箱"
                :prefix-icon="accountPrefixIcon"
                clearable
              />
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                :loading="sendingCode"
                @click="handleSendCode"
                class="send-code-btn"
                full-width
              >
                {{ sendingCode ? '发送中...' : '获取验证码' }}
              </el-button>
            </el-form-item>
          </template>
          
          <!-- 步骤2：输入验证码 -->
          <template #step-1>
            <el-form-item prop="verificationCode">
              <el-input
                v-model="formData.verificationCode"
                placeholder="请输入验证码"
                :prefix-icon="CircleCheck"
                clearable
                maxlength="6"
                show-word-limit
              >
                <template #append>
                  <el-button
                    type="text"
                    :disabled="countingDown"
                    @click="handleSendCode"
                  >
                    {{ countingDown ? `${countDownSeconds}s后重新获取` : '重新获取' }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </template>
          
          <!-- 步骤3：重置密码 -->
          <template #step-2>
            <el-form-item prop="newPassword">
              <el-input
                v-model="formData.newPassword"
                placeholder="请输入新密码"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="formData.confirmPassword"
                placeholder="请确认新密码"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>
          </template>
        </el-step-wizard>
      </el-form>
      
      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button
          v-if="activeStep > 0"
          @click="handlePrevStep"
          class="prev-btn"
        >
          上一步
        </el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleNextStep"
          class="next-btn"
          :disabled="!canNext"
        >
          {{ activeStep === steps.length - 1 ? '确认重置' : '下一步' }}
        </el-button>
      </div>
      
      <!-- 跳转登录 -->
      <div class="form-footer">
        <span>想起密码了？</span>
        <el-button type="text" @click="handleLoginRedirect" class="login-link">
          立即登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, CircleCheck } from '@element-plus/icons-vue'
import axios from '@/utils/axiosConfig'

const router = useRouter()

// 表单数据
const formData = ref({
  account: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

// 步骤配置
const activeStep = ref(0)
const steps = [
  { title: '输入账号' },
  { title: '验证身份' },
  { title: '重置密码' }
]

// 加载状态
const sendingCode = ref(false)
const submitting = ref(false)
const countingDown = ref(false)
const countDownSeconds = ref(60)

// 动态图标
const accountPrefixIcon = computed(() => {
  const account = formData.value.account
  if (/^1[3-9]\d{9}$/.test(account)) {
    return User // 手机号使用用户图标
  }
  return CircleCheck // 邮箱使用验证码图标
})

// 表单验证规则
const rules = {
  account: [
    { required: true, message: '请输入手机号或邮箱', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const phoneRegex = /^1[3-9]\d{9}$/
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (phoneRegex.test(value) || emailRegex.test(value)) {
          callback()
        } else {
          callback(new Error('请输入正确的手机号或邮箱'))
        }
      },
      trigger: 'blur'
    }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== formData.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 倒计时功能
const startCountdown = () => {
  countingDown.value = true
  countDownSeconds.value = 60
  const timer = setInterval(() => {
    countDownSeconds.value--
    if (countDownSeconds.value <= 0) {
      clearInterval(timer)
      countingDown.value = false
    }
  }, 1000)
}

// 发送验证码
const handleSendCode = async () => {
  // 只在第一步或第二步发送验证码
  if ((activeStep.value === 0 || activeStep.value === 1) && formData.value.account) {
    sendingCode.value = true
    try {
      // 调用发送验证码API
      await axios.post('/api/v1/public/send-verification-code', {
        account: formData.value.account,
        type: 'PASSWORD_RESET'
      })
      ElMessage.success('验证码发送成功')
      startCountdown()
      // 如果在第一步，自动跳转到第二步
      if (activeStep.value === 0) {
        activeStep.value++
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '验证码发送失败，请稍后重试')
    } finally {
      sendingCode.value = false
    }
  }
}

// 是否可以下一步
const canNext = computed(() => {
  if (activeStep.value === 0) {
    return formData.value.account
  } else if (activeStep.value === 1) {
    return formData.value.verificationCode && formData.value.verificationCode.length === 6
  } else {
    return formData.value.newPassword && formData.value.confirmPassword && 
           formData.value.newPassword === formData.value.confirmPassword
  }
})

// 下一步
const handleNextStep = async () => {
  if (activeStep.value === steps.length - 1) {
    // 最后一步，提交表单
    await handleResetPassword()
  } else {
    // 普通下一步
    activeStep.value++
  }
}

// 上一步
const handlePrevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

// 重置密码
const handleResetPassword = async () => {
  submitting.value = true
  try {
    // 调用重置密码API
    await axios.post('/api/v1/public/reset-password', {
      account: formData.value.account,
      verificationCode: formData.value.verificationCode,
      newPassword: formData.value.newPassword
    })
    
    ElMessage.success('密码重置成功')
    // 重置成功后跳转到登录页
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '密码重置失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 跳转登录
const handleLoginRedirect = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.forgot-password-form-wrapper {
  width: 100%;
  max-width: 400px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.form-title {
  font-size: 20px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 10px;
  color: #1f2329;
}

.form-subtitle {
  font-size: 14px;
  text-align: center;
  color: #86909c;
  margin-bottom: 30px;
}

.step-wizard {
  margin-bottom: 30px;
}

.forgot-password-form {
  margin-top: 20px;
}

.send-code-btn {
  margin-top: 10px;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.form-actions .el-button {
  flex: 1;
}

.form-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #86909c;
}

.login-link {
  color: #1890ff;
}
</style>