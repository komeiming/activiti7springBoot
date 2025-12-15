<template>
  <div class="tenant-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <h1 class="welcome-title">欢迎回来，{{ tenantName }}!</h1>
      <p class="welcome-subtitle">这是您的租户控制台，您可以在这里管理您的服务和配置</p>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <!-- API调用量 -->
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card api-calls">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ apiCalls }}</div>
                <div class="stat-label">API调用量</div>
                <div class="stat-trend positive">+12.5%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 成功调用率 -->
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card success-rate">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ successRate }}%</div>
                <div class="stat-label">成功调用率</div>
                <div class="stat-trend positive">+2.3%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 通知发送量 -->
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card notifications">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Message /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ notificationsSent }}</div>
                <div class="stat-label">通知发送量</div>
                <div class="stat-trend positive">+8.9%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 工作流实例 -->
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card class="stat-card workflows">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ workflowInstances }}</div>
                <div class="stat-label">工作流实例</div>
                <div class="stat-trend negative">-1.5%</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <el-row :gutter="20">
        <!-- 左侧区域：租户信息概览 -->
        <el-col :xs="24" :lg="8">
          <!-- 租户信息卡片 -->
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <h3 class="card-title">租户信息</h3>
                <el-button type="text" size="small" @click="handleEditInfo">
                  <el-icon><Edit /></el-icon> 编辑
                </el-button>
              </div>
            </template>
            <div class="tenant-info">
              <div class="info-item">
                <label class="info-label">租户名称</label>
                <span class="info-value">{{ tenantName }}</span>
              </div>
              <div class="info-item">
                <label class="info-label">APP ID</label>
                <span class="info-value app-id">{{ tenantAppId }}</span>
                <el-button type="primary" size="small" @click="handleCopyAppId">复制</el-button>
              </div>
              <div class="info-item">
                <label class="info-label">服务模块</label>
                <el-tag v-for="module in serviceModules" :key="module" size="small" class="service-tag">{{ module }}</el-tag>
              </div>
              <div class="info-item">
                <label class="info-label">注册时间</label>
                <span class="info-value">{{ registerDate }}</span>
              </div>
              <div class="info-item">
                <label class="info-label">状态</label>
                <el-tag type="success" size="small">已激活</el-tag>
              </div>
            </div>
          </el-card>
          
          <!-- 快捷操作卡片 -->
          <el-card class="quick-actions-card">
            <template #header>
              <div class="card-header">
                <h3 class="card-title">快捷操作</h3>
              </div>
            </template>
            <div class="quick-actions">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-button type="primary" plain class="action-btn" @click="handleSendNotification">
                    <el-icon><Message /></el-icon>
                    <span>发送通知</span>
                  </el-button>
                </el-col>
                <el-col :span="12">
                  <el-button type="primary" plain class="action-btn" @click="handleManageTemplates">
                    <el-icon><Document /></el-icon>
                    <span>管理模板</span>
                  </el-button>
                </el-col>
                <el-col :span="12">
                  <el-button type="primary" plain class="action-btn" @click="handleViewStats">
                    <el-icon><DataAnalysis /></el-icon>
                    <span>查看统计</span>
                  </el-button>
                </el-col>
                <el-col :span="12">
                  <el-button type="primary" plain class="action-btn" @click="handleApiDocs">
                    <el-icon><DocumentChecked /></el-icon>
                    <span>API文档</span>
                  </el-button>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </el-col>
        
        <!-- 右侧区域：最近活动和图表 -->
        <el-col :xs="24" :lg="16">
          <!-- 最近活动 -->
          <el-card class="recent-activities-card">
            <template #header>
              <div class="card-header">
                <h3 class="card-title">最近活动</h3>
                <el-button type="text" size="small" @click="handleViewAllActivities">
                  查看全部
                </el-button>
              </div>
            </template>
            <div class="recent-activities">
              <el-timeline>
                <el-timeline-item
                  v-for="activity in recentActivities"
                  :key="activity.id"
                  :timestamp="activity.time"
                  :type="activity.type"
                >
                  <div class="activity-content">
                    <h4 class="activity-title">{{ activity.title }}</h4>
                    <p class="activity-desc">{{ activity.description }}</p>
                  </div>
                </el-timeline-item>
              </el-timeline>
              <div v-if="recentActivities.length === 0" class="no-activities">
                <el-empty description="暂无活动记录" />
              </div>
            </div>
          </el-card>
          
          <!-- 调用统计图表 -->
          <el-card class="stats-chart-card">
            <template #header>
              <div class="card-header">
                <h3 class="card-title">API调用统计</h3>
                <el-select v-model="chartTimeRange" size="small" class="time-range-select">
                  <el-option label="最近7天" value="7days" />
                  <el-option label="最近30天" value="30days" />
                  <el-option label="最近3个月" value="3months" />
                </el-select>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon class="chart-icon"><DataAnalysis /></el-icon>
                <p>API调用统计图表</p>
                <p class="placeholder-hint">实际项目中这里将显示ECharts图表</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 底部提示 -->
    <div class="tips-section">
      <el-alert
        title="系统提示"
        type="info"
        :closable="false"
        class="system-tip"
      >
        <template #default>
          <p>您当前使用的是测试环境，实际生产环境将提供更多功能和更好的性能</p>
          <p>如有任何问题，请联系技术支持：591462485@qq.com</p>
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Connection, CircleCheck, Message, Document, Edit,
  DataAnalysis, DocumentChecked
} from '@element-plus/icons-vue'

const router = useRouter()

// 租户名称
const tenantName = ref('测试租户')
// APP ID
const tenantAppId = ref('APP_1234567890abcdef')
// 服务模块
const serviceModules = ref(['通知模块', '工作流模块'])
// 注册时间
const registerDate = ref('2025-12-08')

// 统计数据
const apiCalls = ref(1258)
const successRate = ref(98.5)
const notificationsSent = ref(236)
const workflowInstances = ref(45)

// 图表时间范围
const chartTimeRange = ref('7days')

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    title: '通知发送成功',
    description: '向用户发送了一条测试通知，发送成功',
    time: '2025-12-08 15:30',
    type: 'success'
  },
  {
    id: 2,
    title: '工作流实例创建',
    description: '创建了一个新的请假流程实例',
    time: '2025-12-08 14:20',
    type: 'primary'
  },
  {
    id: 3,
    title: 'API调用失败',
    description: 'API调用失败，错误码：403',
    time: '2025-12-08 10:15',
    type: 'danger'
  },
  {
    id: 4,
    title: '服务模块开通',
    description: '成功开通了工作流模块',
    time: '2025-12-07 16:45',
    type: 'warning'
  },
  {
    id: 5,
    title: '租户注册成功',
    description: '租户注册成功，APP ID：APP_1234567890abcdef',
    time: '2025-12-07 15:00',
    type: 'success'
  }
])

// 生命周期钩子
onMounted(() => {
  // 从本地存储获取租户信息
  loadTenantInfo()
})

// 加载租户信息
const loadTenantInfo = () => {
  try {
    const tenantInfoStr = localStorage.getItem('tenantInfo')
    if (tenantInfoStr) {
      const tenantInfo = JSON.parse(tenantInfoStr)
      // 这里可以根据实际需求加载更多租户信息
      tenantAppId.value = tenantInfo.appId || tenantAppId.value
    }
  } catch (error) {
    console.error('加载租户信息失败:', error)
  }
}

// 编辑租户信息
const handleEditInfo = () => {
  router.push('/tenant/info')
}

// 复制APP ID
const handleCopyAppId = () => {
  navigator.clipboard.writeText(tenantAppId.value)
    .then(() => {
      ElMessage.success('APP ID已复制到剪贴板')
    })
    .catch(() => {
      ElMessage.error('复制失败，请手动复制')
    })
}

// 发送通知
const handleSendNotification = () => {
  router.push('/tenant/notification/send')
}

// 管理模板
const handleManageTemplates = () => {
  router.push('/tenant/notification/templates')
}

// 查看统计
const handleViewStats = () => {
  router.push('/tenant/monitor/stats')
}

// API文档
const handleApiDocs = () => {
  ElMessage.info('API文档功能开发中')
}

// 查看全部活动
const handleViewAllActivities = () => {
  router.push('/tenant/monitor/logs')
}
</script>

<style scoped>
.tenant-dashboard {
  padding: 20px 0;
}

.welcome-section {
  margin-bottom: 20px;
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.welcome-subtitle {
  font-size: 14px;
  color: #606266;
  margin: 8px 0 0 0;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
  transition: all 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100px;
}

.stat-icon {
  font-size: 32px;
  margin-right: 16px;
  color: #409eff;
}

.api-calls .stat-icon {
  color: #409eff;
}

.success-rate .stat-icon {
  color: #67c23a;
}

.notifications .stat-icon {
  color: #e6a23c;
}

.workflows .stat-icon {
  color: #f56c6c;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  font-weight: 500;
}

.stat-trend.positive {
  color: #67c23a;
}

.stat-trend.negative {
  color: #f56c6c;
}

.main-content {
  margin-bottom: 20px;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.tenant-info {
  padding: 16px 0;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 100px;
  font-size: 14px;
  color: #606266;
}

.info-value {
  font-size: 14px;
  color: #303133;
  flex: 1;
}

.info-value.app-id {
  font-family: 'Courier New', Courier, monospace;
  margin-right: 8px;
}

.service-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.quick-actions-card {
  margin-bottom: 20px;
}

.quick-actions {
  padding: 16px 0;
}

.action-btn {
  width: 100%;
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.action-btn .el-icon {
  font-size: 20px;
}

.recent-activities-card {
  margin-bottom: 20px;
}

.recent-activities {
  max-height: 300px;
  overflow-y: auto;
}

.activity-content {
  padding: 8px 0;
}

.activity-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.activity-desc {
  font-size: 12px;
  color: #606266;
  margin: 0;
}

.no-activities {
  padding: 40px 0;
  text-align: center;
}

.stats-chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.chart-placeholder {
  text-align: center;
  color: #909399;
}

.chart-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.placeholder-hint {
  font-size: 12px;
  margin-top: 8px;
}

.tips-section {
  margin-bottom: 20px;
}

.system-tip {
  margin: 0;
}

.time-range-select {
  width: 120px;
}
</style>
