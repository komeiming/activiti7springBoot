<template>
  <div class="tenant-service-container">
    <h3 class="section-title">服务管理</h3>
    
    <!-- 已开通服务 -->
    <el-card shadow="hover" class="service-card">
      <template #header>
        <div class="card-header">
          <span>已开通服务</span>
        </div>
      </template>
      
      <el-table :data="activatedServicesList" border style="width: 100%">
        <el-table-column prop="name" label="服务名称" width="180" />
        <el-table-column prop="description" label="服务描述" />
        <el-table-column prop="status" label="服务状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === '正常' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="activatedAt" label="开通时间" width="180" />
        <el-table-column prop="action" label="操作" width="150">
          <template #default="scope">
            <el-button
          size="small"
          :type="scope.row.status === '正常' ? 'warning' : 'success'"
          @click="handleServiceAction(scope.row)"
        >
          {{ scope.row.status === '正常' ? '暂停服务' : '恢复服务' }}
        </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 可开通服务 -->
    <el-card shadow="hover" class="service-card mt-20">
      <template #header>
        <div class="card-header">
          <span>可开通服务</span>
        </div>
      </template>
      
      <el-table :data="availableServicesList" border style="width: 100%">
        <el-table-column prop="name" label="服务名称" width="180" />
        <el-table-column prop="description" label="服务描述" />
        <el-table-column prop="action" label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleActivateService(scope.row)">
              开通
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 租户注销区域 -->
    <el-card shadow="hover" class="service-card mt-20 danger-card">
      <template #header>
        <div class="card-header">
          <span>租户注销</span>
        </div>
      </template>
      
      <div class="cancel-section">
        <div class="cancel-warning">
          <el-icon class="warning-icon"><WarningFilled /></el-icon>
          <div class="warning-content">
            <h4>警告：租户注销不可逆</h4>
            <p>注销后，您的所有数据将保留90天，到期后自动删除。</p>
            <p>注销后，您将无法继续使用平台提供的任何服务。</p>
          </div>
        </div>
        
        <el-button type="danger" @click="handleCancelTenant">
          注销租户
        </el-button>
      </div>
    </el-card>
    
    <!-- 开通服务确认弹窗 -->
    <el-dialog
      v-model="activateDialogVisible"
      title="开通服务"
      width="500px"
    >
      <div v-if="selectedService">
        <p>您确定要开通 {{ selectedService.name }} 服务吗？</p>
        <p class="service-desc">{{ selectedService.description }}</p>
      </div>
      <template #footer>
        <el-button @click="activateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmActivate">确认开通</el-button>
      </template>
    </el-dialog>
    
    <!-- 暂停/恢复服务确认弹窗 -->
    <el-dialog
      v-model="actionDialogVisible"
      :title="actionDialogTitle"
      width="500px"
    >
      <div v-if="selectedService">
        <p>{{ actionDialogTitle }} {{ selectedService.name }} 服务吗？</p>
        <p class="service-desc">
          {{ actionDialogTitle === '暂停' ? '暂停后将无法调用该模块的API，已创建的模板/流程将保留。' : '恢复后可正常使用该服务。' }}
        </p>
      </div>
      <template #footer>
        <el-button @click="actionDialogVisible = false">取消</el-button>
        <el-button :type="actionDialogTitle === '暂停' ? 'warning' : 'success'" @click="handleConfirmAction">
          {{ actionDialogTitle === '暂停' ? '确认暂停' : '确认恢复' }}
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 注销租户确认弹窗 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="注销租户"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="cancel-dialog-content">
        <el-alert
          title="警告：租户注销不可逆"
          type="error"
          :closable="false"
          show-icon
          class="cancel-alert"
        />
        <p>请确认您已了解以下事项：</p>
        <el-checkbox-group v-model="cancelConfirmItems">
          <el-checkbox label="1">我已备份所有重要数据</el-checkbox>
          <el-checkbox label="2">我已了解注销后数据将保留90天</el-checkbox>
          <el-checkbox label="3">我已了解注销后无法恢复</el-checkbox>
        </el-checkbox-group>
        
        <div class="verification-section">
          <el-input
            v-model="cancelVerificationCode"
            placeholder="请输入验证码"
            maxlength="6"
            show-word-limit
            class="verification-input"
          >
            <template #append>
              <el-button
                type="text"
                :disabled="cancelCountingDown"
                @click="handleSendCancelCode"
              >
                {{ cancelCountingDown ? `${cancelCountDownSeconds}s后重新获取` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleConfirmCancel" :disabled="!canCancel">
          确认注销
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import axios from '@/utils/axiosConfig'

// 已开通服务列表
const activatedServicesList = ref([])

// 可开通服务列表
const availableServicesList = ref([])

// 初始化服务列表
const initServices = async () => {
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
    
    // 根据租户的serviceModules字段，初始化已开通服务列表
    // 假设serviceModules是逗号分隔的字符串，如"notification,workflow"
    const serviceModules = response.serviceModules || ''
    const modules = serviceModules.split(',')
    
    // 模拟服务列表数据
    const allServices = [
      { id: 'notification', name: '通知模块', description: '提供短信、邮件、APP推送等多种通知方式' },
      { id: 'workflow', name: '工作流模块', description: '提供流程设计、执行、监控等功能' },
      { id: 'data-analysis', name: '数据分析模块', description: '提供数据统计、分析、报表等功能' }
    ]
    
    // 初始化已开通服务列表
    activatedServicesList.value = allServices
      .filter(service => modules.includes(service.id))
      .map(service => ({
        ...service,
        status: '正常',
        activatedAt: response.createdAt ? new Date(response.createdAt).toLocaleString('zh-CN') : ''
      }))
    
    // 初始化可开通服务列表
    availableServicesList.value = allServices.filter(service => !modules.includes(service.id))
    
  } catch (error) {
    console.error('获取服务列表失败:', error)
    ElMessage.error(`获取服务列表失败: ${error.message || '请稍后重试'}`)
  }
}

// 组件挂载时初始化服务列表
onMounted(() => {
  initServices()
})

// 开通服务相关
const activateDialogVisible = ref(false)
const selectedService = ref(null)

// 服务操作相关
const actionDialogVisible = ref(false)
const actionDialogTitle = ref('')

// 注销租户相关
const cancelDialogVisible = ref(false)
const cancelConfirmItems = ref([])
const cancelVerificationCode = ref('')
const cancelCountingDown = ref(false)
const cancelCountDownSeconds = ref(60)
let cancelTimer = null

// 是否可以注销
const canCancel = computed(() => {
  return cancelConfirmItems.value.length === 3 && cancelVerificationCode.value.length === 6
})

// 开通服务
const handleActivateService = (service) => {
  selectedService.value = service
  activateDialogVisible.value = true
}

// 确认开通服务
const handleConfirmActivate = () => {
  if (selectedService.value) {
    // 模拟开通请求
    ElMessage.success(`已开通${selectedService.value.name}服务`)
    // 将服务添加到已开通列表
    activatedServicesList.value.push({
      ...selectedService.value,
      status: '正常',
      activatedAt: new Date().toLocaleString('zh-CN')
    })
    // 从可开通列表中移除
    const index = availableServicesList.value.findIndex(item => item.id === selectedService.value.id)
    if (index !== -1) {
      availableServicesList.value.splice(index, 1)
    }
    activateDialogVisible.value = false
    selectedService.value = null
  }
}

// 服务操作（暂停/恢复）
const handleServiceAction = (service) => {
  selectedService.value = service
  actionDialogTitle.value = service.status === '正常' ? '暂停' : '恢复'
  actionDialogVisible.value = true
}

// 确认服务操作
const handleConfirmAction = () => {
  if (selectedService.value) {
    // 模拟操作请求
    selectedService.value.status = selectedService.value.status === '正常' ? '已暂停' : '正常'
    ElMessage.success(`${actionDialogTitle.value}服务成功`)
    actionDialogVisible.value = false
    selectedService.value = null
  }
}

// 注销租户
const handleCancelTenant = () => {
  cancelDialogVisible.value = true
}

// 发送注销验证码
const handleSendCancelCode = () => {
  cancelCountingDown.value = true
  cancelCountDownSeconds.value = 60
  
  cancelTimer = setInterval(() => {
    cancelCountDownSeconds.value--
    if (cancelCountDownSeconds.value <= 0) {
      clearInterval(cancelTimer)
      cancelCountingDown.value = false
      cancelCountDownSeconds.value = 60
    }
  }, 1000)
  
  ElMessage.success('注销验证码已发送')
}

// 确认注销租户
const handleConfirmCancel = () => {
  if (canCancel.value) {
    // 模拟注销请求
    ElMessageBox.success('租户注销申请已提交，我们将在3个工作日内处理')
    cancelDialogVisible.value = false
    // 重置状态
    cancelConfirmItems.value = []
    cancelVerificationCode.value = ''
    cancelCountingDown.value = false
    cancelCountDownSeconds.value = 60
    if (cancelTimer) {
      clearInterval(cancelTimer)
      cancelTimer = null
    }
  }
}
</script>

<style scoped>
.tenant-service-container {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.service-card {
  margin-bottom: 0;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.danger-card {
  border-color: #ff4d4f;
}

.cancel-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
}

.cancel-warning {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  color: #ff4d4f;
}

.warning-icon {
  font-size: 24px;
  margin-top: 2px;
}

.warning-content {
  flex: 1;
}

.warning-content h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: 600;
}

.warning-content p {
  margin: 5px 0;
  font-size: 14px;
}

.service-desc {
  color: #606266;
  margin: 10px 0;
}

.cancel-dialog-content {
  padding: 10px 0;
}

.cancel-alert {
  margin-bottom: 20px;
}

.verification-section {
  margin-top: 20px;
}

.verification-input {
  width: 100%;
}
</style>