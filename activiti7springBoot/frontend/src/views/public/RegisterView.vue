<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h2>租户注册</h2>
        <p>注册成为零售门店业务管理平台的公有服务租户</p>
      </div>
      
      <el-steps :active="activeStep" simple>
        <el-step title="基本信息"></el-step>
        <el-step title="联系人信息"></el-step>
        <el-step title="服务选择"></el-step>
      </el-steps>
      
      <div class="register-content">
        <!-- 第一步：基本信息 -->
        <el-form v-if="activeStep === 0" ref="basicForm" :model="form" :rules="basicRules" label-width="120px">
          <el-form-item label="系统名称" prop="systemName">
            <el-input v-model="form.systemName" placeholder="请输入系统名称" maxlength="50"></el-input>
          </el-form-item>
          <el-form-item label="企业名称" prop="enterpriseName">
            <el-input v-model="form.enterpriseName" placeholder="请输入企业名称" maxlength="100"></el-input>
          </el-form-item>
          <el-form-item label="统一社会信用代码" prop="creditCode">
            <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" maxlength="18"></el-input>
          </el-form-item>
        </el-form>
        
        <!-- 第二步：联系人信息 -->
        <el-form v-if="activeStep === 1" ref="contactForm" :model="form" :rules="contactRules" label-width="120px">
          <el-form-item label="联系人姓名" prop="contactName">
            <el-input v-model="form.contactName" placeholder="请输入联系人姓名" maxlength="20"></el-input>
          </el-form-item>
          <el-form-item label="联系人手机号" prop="contactPhone">
            <el-input v-model="form.contactPhone" placeholder="请输入联系人手机号（可选）" maxlength="11"></el-input>
          </el-form-item>
          <!-- 短信验证码字段暂时不显示 -->
          <el-form-item label="短信验证码" prop="smsCode" v-if="false">
            <el-input v-model="form.smsCode" placeholder="请输入短信验证码" maxlength="6"></el-input>
          </el-form-item>
          <el-form-item label="联系人邮箱" prop="contactEmail">
            <el-input v-model="form.contactEmail" placeholder="请输入联系人邮箱" maxlength="50"></el-input>
          </el-form-item>
        </el-form>
        
        <!-- 第三步：服务选择 -->
        <el-form v-if="activeStep === 2" ref="serviceForm" :model="form" :rules="serviceRules" label-width="120px">
          <el-form-item label="所需服务模块" prop="serviceModules">
            <el-checkbox-group v-model="form.serviceModules">
              <el-checkbox label="notification">通知模块</el-checkbox>
              <el-checkbox label="workflow">工作流模块</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="调用场景描述" prop="callScenarios">
            <el-input v-model="form.callScenarios" type="textarea" placeholder="请描述您将如何使用我们的服务" :rows="4" maxlength="200"></el-input>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="register-footer">
        <el-button v-if="activeStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < 2" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="activeStep === 2" type="success" @click="submitRegister">提交注册</el-button>
        <el-button @click="resetForm">重置</el-button>
      </div>
    </div>
    
    <div class="register-footer-link">
      <p>已有账号？<a href="/login">前往登录</a></p>
      <p>查看<a href="#">用户协议</a>和<a href="#">隐私政策</a></p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axiosConfig'

const router = useRouter()

// 当前步骤
const activeStep = ref(0)

// 表单数据
const form = reactive({
  systemName: '',
  enterpriseName: '',
  creditCode: '',
  contactName: '',
  contactPhone: '',
  smsCode: '',
  contactEmail: '',
  serviceModules: [],
  callScenarios: ''
})

// 表单引用
const basicForm = ref(null)
const contactForm = ref(null)
const serviceForm = ref(null)

// 短信验证码相关
const smsDisabled = ref(false)
const smsCountdown = ref(60)
const smsText = ref('获取验证码')
let smsTimer = null

// 表单验证规则
const basicRules = {
  systemName: [
    { required: true, message: '请输入系统名称', trigger: 'blur' },
    { min: 2, max: 50, message: '系统名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  enterpriseName: [
    { required: true, message: '请输入企业名称', trigger: 'blur' },
    { min: 2, max: 100, message: '企业名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  creditCode: []
}

const contactRules = {
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '联系人姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  contactPhone: [],
  smsCode: [],
  contactEmail: [
    { required: true, message: '请输入联系人邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const serviceRules = {
  serviceModules: [
    { required: true, message: '请选择至少一个服务模块', trigger: 'change' }
  ],
  callScenarios: [
    { required: true, message: '请描述调用场景', trigger: 'blur' },
    { min: 10, max: 200, message: '调用场景描述长度在 10 到 200 个字符', trigger: 'blur' }
  ]
}

// 发送短信验证码
const sendSmsCode = () => {
  // 验证手机号格式
  const phoneRule = contactRules.contactPhone[1]
  if (!phoneRule.pattern.test(form.contactPhone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  
  // 模拟发送短信
  smsDisabled.value = true
  smsText.value = `${smsCountdown.value}秒后重新获取`
  
  // 开始倒计时
  smsTimer = setInterval(() => {
    smsCountdown.value--
    smsText.value = `${smsCountdown.value}秒后重新获取`
    
    if (smsCountdown.value <= 0) {
      clearInterval(smsTimer)
      smsDisabled.value = false
      smsText.value = '获取验证码'
      smsCountdown.value = 60
    }
  }, 1000)
  
  ElMessage.success('短信验证码已发送')
}

// 下一步
const nextStep = () => {
  if (activeStep.value === 0) {
    // 验证基本信息表单
    basicForm.value.validate((valid) => {
      if (valid) {
        activeStep.value++
        saveFormData()
      } else {
        ElMessage.error('请完善基本信息')
        return false
      }
    })
  } else if (activeStep.value === 1) {
    // 验证联系人信息表单
    contactForm.value.validate((valid) => {
      if (valid) {
        activeStep.value++
        saveFormData()
      } else {
        ElMessage.error('请完善联系人信息')
        return false
      }
    })
  }
}

// 上一步
const prevStep = () => {
  activeStep.value--
}

// 提交注册
const submitRegister = () => {
  // 验证服务选择表单
  serviceForm.value.validate((valid) => {
    if (valid) {
      // 提交注册请求
      ElMessageBox.confirm('确定提交注册申请吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        // 准备提交数据，确保与后端实体类字段匹配
        const submitData = {
          systemName: form.systemName,
          enterpriseName: form.enterpriseName,
          enterpriseCreditCode: form.creditCode, // 转换为后端期望的字段名
          contactName: form.contactName,
          contactPhone: form.contactPhone,
          contactEmail: form.contactEmail,
          serviceModules: form.serviceModules.join(','), // 将数组转换为字符串
          callScenarios: form.callScenarios
        }
        
        console.log('提交注册数据:', submitData)
        
        // 调用注册API
        axios.post('/v1/tenant/register', submitData)
          .then(response => {
            ElMessage.success('注册申请已提交，审核结果将通过短信/邮箱通知您')
            // 跳转到登录页面
            router.push('/login')
          })
          .catch(error => {
            ElMessage.error('注册失败: ' + (error.message || '请稍后重试'))
          })
      }).catch(() => {
        // 取消提交
      })
    } else {
      ElMessage.error('请完善服务选择信息')
      return false
    }
  })
}

// 重置表单
const resetForm = () => {
  activeStep.value = 0
  Object.keys(form).forEach(key => {
    if (Array.isArray(form[key])) {
      form[key] = []
    } else {
      form[key] = ''
    }
  })
  // 清空表单验证
  basicForm.value?.resetFields()
  contactForm.value?.resetFields()
  serviceForm.value?.resetFields()
  // 清除本地存储的表单数据
  localStorage.removeItem('registerFormData')
}

// 保存表单数据到本地存储
const saveFormData = () => {
  localStorage.setItem('registerFormData', JSON.stringify(form))
}

// 从本地存储恢复表单数据
const loadFormData = () => {
  const savedData = localStorage.getItem('registerFormData')
  if (savedData) {
    try {
      const data = JSON.parse(savedData)
      Object.assign(form, data)
      ElMessage.info('已恢复上次填写的数据')
    } catch (error) {
      console.error('解析表单数据失败:', error)
    }
  }
}

// 生命周期钩子
onMounted(() => {
  // 加载保存的表单数据
  loadFormData()
})

onBeforeUnmount(() => {
  // 清除定时器
  if (smsTimer) {
    clearInterval(smsTimer)
  }
  // 保存当前表单数据
  saveFormData()
})
</script>

<style scoped>
.register-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.register-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 600px;
  padding: 30px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #1890ff;
  margin-bottom: 10px;
}

.register-header p {
  color: #86909c;
  font-size: 14px;
}

.register-content {
  margin: 20px 0;
}

.register-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
}

.register-footer-link {
  margin-top: 20px;
  text-align: center;
  color: #86909c;
  font-size: 14px;
}

.register-footer-link a {
  color: #1890ff;
  text-decoration: none;
  margin: 0 5px;
}

.register-footer-link a:hover {
  text-decoration: underline;
}
</style>