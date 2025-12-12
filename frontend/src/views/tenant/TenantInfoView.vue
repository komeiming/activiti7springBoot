<template>
  <div class="tenant-info-container">
    <h3 class="section-title">基本信息</h3>
    <el-card shadow="hover">
      <el-form ref="infoForm" :model="tenantInfo" label-width="120px">
        <el-form-item label="系统名称" prop="systemName">
          <el-input v-model="tenantInfo.systemName" placeholder="请输入系统名称" maxlength="50" />
        </el-form-item>
        
        <el-form-item label="企业名称" prop="enterpriseName">
          <el-input v-model="tenantInfo.enterpriseName" placeholder="请输入企业名称" maxlength="100" disabled />
        </el-form-item>
        
        <el-form-item label="统一社会信用代码" prop="creditCode">
          <el-input v-model="tenantInfo.creditCode" placeholder="请输入统一社会信用代码" maxlength="18" disabled />
        </el-form-item>
        
        <el-form-item label="联系人姓名" prop="contactName">
          <el-input v-model="tenantInfo.contactName" placeholder="请输入联系人姓名" maxlength="20" />
        </el-form-item>
        
        <el-form-item label="联系人手机号" prop="contactPhone">
          <el-input v-model="tenantInfo.contactPhone" placeholder="请输入联系人手机号" maxlength="11">
            <template #append>
              <el-button type="primary" size="small" @click="handleSendSmsCode" :disabled="countingDown">
                {{ countingDown ? `${countDownSeconds}s后重新获取` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="短信验证码" prop="smsCode">
          <el-input v-model="tenantInfo.smsCode" placeholder="请输入短信验证码" maxlength="6" />
        </el-form-item>
        
        <el-form-item label="联系人邮箱" prop="contactEmail">
          <el-input v-model="tenantInfo.contactEmail" placeholder="请输入联系人邮箱" maxlength="50" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存修改</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '@/utils/axiosConfig'

// 租户信息
const tenantInfo = reactive({
  systemName: '',
  enterpriseName: '',
  creditCode: '',
  contactName: '',
  contactPhone: '',
  smsCode: '',
  contactEmail: ''
})

// 倒计时相关
const countingDown = ref(false)
const countDownSeconds = ref(60)
let countDownTimer = null

// 初始化租户信息
const initTenantInfo = async () => {
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
    
    // 更新表单数据
    tenantInfo.systemName = response.systemName || ''
    tenantInfo.enterpriseName = response.enterpriseName || ''
    tenantInfo.creditCode = response.enterpriseCreditCode || ''
    tenantInfo.contactName = response.contactName || ''
    tenantInfo.contactPhone = response.contactPhone || ''
    tenantInfo.contactEmail = response.contactEmail || ''
    
    // 更新localStorage中的租户信息
    tenantInfoFromStorage.systemName = response.systemName
    tenantInfoFromStorage.enterpriseName = response.enterpriseName
    tenantInfoFromStorage.enterpriseCreditCode = response.enterpriseCreditCode
    tenantInfoFromStorage.contactName = response.contactName
    tenantInfoFromStorage.contactPhone = response.contactPhone
    tenantInfoFromStorage.contactEmail = response.contactEmail
    localStorage.setItem('tenantInfo', JSON.stringify(tenantInfoFromStorage))
    
  } catch (error) {
    console.error('获取租户信息失败:', error)
    ElMessage.error(`获取租户信息失败: ${error.message || '请稍后重试'}`)
  }
}

// 组件挂载时初始化租户信息
onMounted(() => {
  initTenantInfo()
})

// 发送短信验证码
const handleSendSmsCode = () => {
  // 验证手机号格式
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(tenantInfo.contactPhone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  
  // 模拟发送短信
  countingDown.value = true
  countDownSeconds.value = 60
  
  countDownTimer = setInterval(() => {
    countDownSeconds.value--
    if (countDownSeconds.value <= 0) {
      clearInterval(countDownTimer)
      countingDown.value = false
      countDownSeconds.value = 60
    }
  }, 1000)
  
  ElMessage.success('短信验证码已发送')
}

// 保存修改
const handleSave = async () => {
  // 验证表单
  if (!tenantInfo.systemName) {
    ElMessage.error('请输入系统名称')
    return
  }
  
  if (!tenantInfo.contactName) {
    ElMessage.error('请输入联系人姓名')
    return
  }
  
  if (!tenantInfo.contactEmail) {
    ElMessage.error('请输入联系人邮箱')
    return
  }
  
  // 从localStorage获取appId
  const tenantStr = localStorage.getItem('tenantInfo')
  if (!tenantStr) {
    ElMessage.error('请先登录')
    return
  }
  
  const tenantInfoFromStorage = JSON.parse(tenantStr)
  const appId = tenantInfoFromStorage.appId
  
  if (!appId) {
    ElMessage.error('缺少appId，请重新登录')
    return
  }
  
  try {
    // 调用后端API保存信息
    const response = await axios.put(`/v1/tenant/info?appId=${appId}`, {
      systemName: tenantInfo.systemName,
      contactName: tenantInfo.contactName,
      contactPhone: tenantInfo.contactPhone,
      contactEmail: tenantInfo.contactEmail
    })
    
    // 更新成功
    ElMessage.success('基本信息保存成功')
    
    // 更新localStorage中的租户信息
    tenantInfoFromStorage.systemName = response.systemName
    tenantInfoFromStorage.contactName = response.contactName
    tenantInfoFromStorage.contactPhone = response.contactPhone
    tenantInfoFromStorage.contactEmail = response.contactEmail
    localStorage.setItem('tenantInfo', JSON.stringify(tenantInfoFromStorage))
    
  } catch (error) {
    // 更新失败
    ElMessage.error(`保存失败: ${error.message || '请稍后重试'}`)
    console.error('保存租户信息失败:', error)
  }
}

// 取消修改
const handleCancel = () => {
  // 从localStorage获取最新的租户信息
  const tenantStr = localStorage.getItem('tenantInfo')
  if (tenantStr) {
    const tenantInfoFromStorage = JSON.parse(tenantStr)
    tenantInfo.systemName = tenantInfoFromStorage.systemName || ''
    tenantInfo.enterpriseName = tenantInfoFromStorage.enterpriseName || ''
    tenantInfo.creditCode = tenantInfoFromStorage.creditCode || ''
    tenantInfo.contactName = tenantInfoFromStorage.contactName || ''
    tenantInfo.contactPhone = tenantInfoFromStorage.contactPhone || ''
  } else {
    // 如果localStorage中没有租户信息，重置为默认值
    tenantInfo.systemName = '测试系统'
    tenantInfo.enterpriseName = '测试企业'
    tenantInfo.creditCode = '91110101MA00123456'
    tenantInfo.contactName = '张三'
    tenantInfo.contactPhone = '13800138000'
  }
  
  // 清空验证码
  tenantInfo.smsCode = ''
  
  ElMessage.info('已取消修改')
}
</script>

<style scoped>
.tenant-info-container {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}
</style>