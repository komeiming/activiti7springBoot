<template>
  <div class="dashboard-container">
    <el-card :shadow="'never'" class="main-card">
      <template #header>
        <div class="card-header">
          <h1>仪表盘</h1>
          <div class="header-info">
            <span>欢迎，{{ currentUser ? currentUser.username : '' }}</span>
            <span class="date-info">{{ currentDate }}</span>
            <el-button 
              type="primary" 
              :icon="Refresh" 
              size="small"
              @click="refreshData"
              :loading="loading"
              class="refresh-btn"
            >
              刷新数据
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 快捷操作区 -->
      <div class="quick-actions">
        <el-card class="action-card" :shadow="'hover'" @click="goTo('/process-apply', { processType: 'leave' })">
          <div class="action-icon primary">
            <Plus /></div>
          <div class="action-text">
            <h3>发起请假</h3>
            <p>快速提交请假申请</p>
          </div>
          <el-button 
            type="primary" 
            size="small"
            style="position: absolute; bottom: 10px; right: 10px;"
            @click.stop="goTo('/process-apply', { processType: 'leave' })"
          >
            立即申请
          </el-button>
        </el-card>
        
        <el-card class="action-card" :shadow="'hover'" @click="goTo('/process-apply')">
          <div class="action-icon success">
            <Upload /></div>
          <div class="action-text">
            <h3>申请流程</h3>
            <p>发起各类流程申请</p>
          </div>
        </el-card>
        
        <el-card class="action-card" :shadow="'hover'" @click="goTo('/enhanced-task-list')">
          <div class="action-icon warning">
            <Ticket /></div>
          <div class="action-text">
            <h3>待办任务</h3>
            <p>查看和处理待办任务</p>
          </div>
        </el-card>
        
        <el-card class="action-card" :shadow="'hover'" @click="goTo('/completed-tasks')">
          <div class="action-icon info">
            <Check /></div>
          <div class="action-text">
            <h3>已办任务</h3>
            <p>查看历史处理记录</p>
          </div>
        </el-card>
        
        <el-card class="action-card" :shadow="'hover'" @click="navigateTo('/process-history')">
            <div class="action-icon danger">
              <View /></div>
            <div class="action-text">
              <h3>流程历史</h3>
              <p>查看所有流程历史记录</p>
            </div>
          </el-card>
      </div>
      
      <!-- 任务统计区 -->
      <div class="stats-section">
        <h2 class="section-title">任务统计</h2>
        <div class="stats-grid">
          <el-card class="stat-card" :shadow="'hover'" :class="{ 'danger': stats.pendingTasks > 0 }" @click="goTo('/enhanced-task-list')">
            <div class="stat-content">
              <div class="stat-icon">
                <Clock /></div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.pendingTasks }}</div>
                <div class="stat-label">待处理任务</div>
              </div>
              <div class="stat-badge" v-if="stats.pendingTasks > 0">
                <el-badge :value="stats.pendingTasks" type="danger" />
              </div>
            </div>
          </el-card>
          
          <el-card class="stat-card" :shadow="'hover'" :class="{ 'warning': stats.overdueTasks > 0 }" @click="goTo('/enhanced-task-list', { filter: 'overdue' })" style="cursor: pointer;">
            <div class="stat-content">
              <div class="stat-icon">
                <Warning /></div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.overdueTasks }}</div>
                <div class="stat-label">逾期任务</div>
              </div>
              <div class="stat-badge" v-if="stats.overdueTasks > 0">
                <el-badge :value="stats.overdueTasks" type="warning" />
              </div>
            </div>
          </el-card>
          
          <el-card class="stat-card" :shadow="'hover'" @click="goTo('/completed-tasks')" style="cursor: pointer;">
            <div class="stat-content">
              <div class="stat-icon">
                <Check /></div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.completedTasks }}</div>
                <div class="stat-label">已完成任务</div>
              </div>
            </div>
          </el-card>
          
          <el-card class="stat-card" :shadow="'hover'" @click="goTo('/process-history')" style="cursor: pointer;">
            <div class="stat-content">
              <div class="stat-icon">
                <Document /></div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.myApplications }}</div>
                <div class="stat-label">我的申请</div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
      
      <!-- 最近待办任务 -->
      <div class="recent-tasks-section">
        <div class="section-header">
          <h2 class="section-title">最近待办任务</h2>
          <el-button type="text" @click="goTo('/enhanced-task-list')">查看全部</el-button>
        </div>
        
        <el-card class="tasks-card">
            <div class="task-list">
              <div v-if="loading && recentTasks.length === 0" class="loading-container">
                <el-skeleton :animated="true" :rows="3" />
              </div>
              <div v-else-if="recentTasks.length === 0" class="empty-state">
                <el-empty 
                  description="暂无待办任务"
                  :image-size="100"
                >
                  <el-button type="primary" @click="goTo('/enhanced-task-list')">查看全部任务</el-button>
                </el-empty>
              </div>
              <div 
                v-else
                v-for="(task, index) in recentTasks" 
                :key="task.id || `task-${index}`" 
                class="task-item" 
                @click="task.id ? goTo(`/enhanced-task-list/${task.id}`) : null"
              >
                <div class="task-header">
                  <h3 class="task-name">{{ task.name || '未命名任务' }}</h3>
                  <el-tag :type="getTaskPriorityType(task.priority)">
                    {{ getTaskPriorityText(task.priority) }}
                  </el-tag>
              </div>
              <div class="task-meta">
                <span class="task-time">创建时间: {{ formatDate(task.created) }}</span>
                <span v-if="task.dueDate" class="task-due" :class="{ 'overdue': isOverdue(task.dueDate) }">
                  截止时间: {{ formatDate(task.dueDate) }}
                </span>
              </div>
              <div class="task-footer">
                <el-button type="primary" size="small" @click.stop="task.id ? goTo(`/enhanced-task-list/${task.id}`) : null">处理任务</el-button>
              </div>
            </div>
            <div v-if="recentTasks.length === 0" class="empty-state">
                <el-empty :description="'暂无待办任务'" />
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 系统通知 -->
      <div class="notifications-section">
        <div class="section-header">
          <h2 class="section-title">系统通知</h2>
          <el-button type="text" @click="markAllAsRead">全部已读</el-button>
        </div>
        
        <el-card class="notifications-card">
          <div class="notification-list">
            <div 
              v-for="notification in notifications" 
              :key="notification.id" 
              class="notification-item" 
              :class="{ 'unread': !notification.read }"
            >
              <div class="notification-icon">
                <Bell />
              </div>
              <div class="notification-content">
                <p class="notification-text">{{ notification.content }}</p>
                <span class="notification-time">{{ formatDate(notification.time) }}</span>
              </div>
              <el-button 
                v-if="!notification.read" 
                type="text" 
                size="small" 
                @click.stop="markAsRead(notification.id)"
              >
                标记已读
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import UserService from '../services/UserService'
import TaskService from '../services/TaskService'
import {
  Refresh,
  Plus,
  Upload,
  Ticket,
  Check,
  View,
  Clock,
  Warning,
  Document,
  Bell
} from '@element-plus/icons-vue'

export default {
  name: 'DashboardView',
  setup() {
    const router = useRouter()
    const currentUser = ref(null)
    const stats = ref({
      pendingTasks: 0,
      overdueTasks: 0,
      completedTasks: 0,
      myApplications: 0
    })
    const recentTasks = ref([])
    const notifications = ref([
      {
        id: 1,
        content: '您的请假申请已获批',
        time: new Date().toISOString(),
        read: false
      },
      {
        id: 2,
        content: '有新的任务需要您处理',
        time: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
        read: true
      },
      {
        id: 3,
        content: '系统将于明日凌晨2点进行维护',
        time: new Date(Date.now() - 48 * 60 * 60 * 1000).toISOString(),
        read: true
      }
    ])
    const loading = ref(false)
    
    // 定义本地的showNotification方法
    const showNotification = (title, message, type = 'info') => {
      ElMessage({
        title,
        message,
        type,
        duration: 3000
      })
    }
    
    // 定义本地的showLoading方法
    let loadingInstance = null
    const showLoading = () => {
      loadingInstance = ElLoading.service({
        lock: true,
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
    }
    
    // 定义本地的hideLoading方法
    const hideLoading = () => {
      if (loadingInstance) {
        loadingInstance.close()
        loadingInstance = null
      }
    }
    
    const currentDate = computed(() => {
      return new Date().toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      })
    })
    
    const goTo = (path, params = {}) => {
      if (Object.keys(params).length > 0) {
        router.push({ path, query: params })
      } else {
        router.push(path)
      }
    }
    
    const navigateTo = (path) => {
      router.push(path)
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    const isOverdue = (dueDate) => {
      if (!dueDate) return false
      const now = new Date()
      const due = new Date(dueDate)
      return now > due
    }
    
    const getTaskPriorityType = (priority) => {
      switch (priority) {
        case 100:
          return 'danger'
        case 50:
          return 'warning'
        case 30:
          return 'info'
        default:
          return 'default'
      }
    }
    
    const getTaskPriorityText = (priority) => {
      switch (priority) {
        case 100:
          return '最高'
        case 50:
          return '高'
        case 30:
          return '中'
        case 10:
          return '低'
        default:
          return '普通'
      }
    }
    
    const markAsRead = (notificationId) => {
      const notification = notifications.value.find(n => n.id === notificationId)
      if (notification) {
        notification.read = true
        showNotification('操作成功', '已标记为已读', 'success')
      }
    }
    
    const markAllAsRead = () => {
      notifications.value.forEach(notification => {
        notification.read = true
      })
      showNotification('操作成功', '全部标记为已读', 'success')
    }
    
    const loadDashboardData = async () => {
      try {
        loading.value = true
        // 获取当前用户信息
        currentUser.value = await UserService.getCurrentUser()
        
        // 单独处理每个API调用，避免一个失败影响所有
        try {
          // 获取任务统计数据
          const taskStats = await UserService.getUserTaskStatistics()
          stats.value = {
            pendingTasks: taskStats.pending || 0,
            overdueTasks: taskStats.overdue || 0,
            completedTasks: taskStats.completed || 0,
            myApplications: taskStats.myApplications || 0
          }
        } catch (statsError) {
          console.error('获取任务统计失败:', statsError)
          // 即使统计获取失败，也使用默认值继续
          stats.value = {
            pendingTasks: 0,
            overdueTasks: 0,
            completedTasks: 0,
            myApplications: 0
          }
        }
        
        try {
          // 获取最近待办任务
          const tasks = await TaskService.getTasks(1, 5)
          recentTasks.value = Array.isArray(tasks.data) ? tasks.data : []
        } catch (tasksError) {
          console.error('获取任务列表失败:', tasksError)
          recentTasks.value = []
          // 不显示错误消息，因为我们已经有了友好的空状态显示
        }
        
        // 成功加载后，如果一切正常，可以显示一个短暂的成功提示
        console.log('仪表盘数据加载成功')
      } catch (error) {
        console.error('加载仪表盘数据失败:', error)
        // 只在严重错误时显示通知，避免过多干扰
        // 重置统计数据，确保界面显示正常
        stats.value = {
          pendingTasks: 0,
          overdueTasks: 0,
          completedTasks: 0,
          myApplications: 0
        }
        recentTasks.value = []
      } finally {
        loading.value = false
      }
    }
    
    const refreshData = async () => {
      showLoading()
      try {
        await loadDashboardData()
        showNotification('刷新成功', '数据已更新', 'success')
      } finally {
        hideLoading()
      }
    }
    
    onMounted(() => {
      showLoading()
      loadDashboardData().finally(() => {
        hideLoading()
      })
    })
    
    return {
      currentUser,
      stats,
      recentTasks,
      notifications,
      currentDate,
      loading,
      goTo,
      navigateTo,
      formatDate,
      isOverdue,
      getTaskPriorityType,
      getTaskPriorityText,
      markAsRead,
      markAllAsRead,
      refreshData,
      // 图标组件
      Refresh,
      Plus,
      Upload,
      Ticket,
      Check,
      View,
      Clock,
      Warning,
      Document,
      Bell
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.main-card {
  border-radius: 8px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.header-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.date-info {
  font-size: 14px;
  color: #909399;
}

/* 快捷操作区 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12) !important;
}

.action-card .el-card__body {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.action-icon.primary {
  background-color: #409eff;
}

.action-icon.success {
  background-color: #67c23a;
}

.action-icon.warning {
  background-color: #e6a23c;
}

.action-icon.info {
  background-color: #909399;
}

.action-text h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.action-text p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

/* 统计区域 */
.stats-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.stat-card {
  transition: all 0.3s ease;
  border-radius: 8px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12) !important;
}

.stat-card.danger .el-card__body {
  border-left: 8px solid #f56c6c;
}

.stat-card.warning .el-card__body {
  border-left: 8px solid #e6a23c;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #606266;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-top: 4px;
}

.stat-badge {
  flex-shrink: 0;
}

/* 最近任务 */
.recent-tasks-section,
.notifications-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.tasks-card,
.notifications-card {
  border-radius: 8px;
}

.task-list,
.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.task-item,
.notification-item {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.task-item:hover {
  background-color: #f5f7fa;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #f0f9ff;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0;
}

.task-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 14px;
}

.task-time {
  color: #606266;
}

.task-due {
  color: #606266;
}

.task-due.overdue {
  color: #f56c6c;
  font-weight: 500;
}

.task-footer {
  display: flex;
  justify-content: flex-end;
}

.notification-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-text {
  margin: 0;
  font-size: 14px;
  color: #303133;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-info {
    align-items: flex-start;
  }
  
  .quick-actions,
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .task-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style>