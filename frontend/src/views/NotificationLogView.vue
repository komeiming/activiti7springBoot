<template>
  <div class="notification-log-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通知日志管理</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="card">
        <!-- 操作日志标签页 -->
        <el-tab-pane label="操作日志" name="operation">
          <div class="log-filter">
            <el-form :inline="true" :model="operationLogQuery" class="filter-form">
              <el-form-item label="操作类型">
                <el-select v-model="operationLogQuery.operationType" placeholder="请选择操作类型">
                  <el-option label="创建" value="CREATE"></el-option>
                  <el-option label="更新" value="UPDATE"></el-option>
                  <el-option label="删除" value="DELETE"></el-option>
                  <el-option label="启用" value="ENABLE"></el-option>
                  <el-option label="禁用" value="DISABLE"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="操作人">
                <el-input v-model="operationLogQuery.operator" placeholder="请输入操作人"></el-input>
              </el-form-item>
              <el-form-item label="模板ID">
                <el-input v-model="operationLogQuery.templateId" placeholder="请输入模板ID"></el-input>
              </el-form-item>
              <el-form-item label="操作时间">
                <el-date-picker
                  v-model="operationLogTimeRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                ></el-date-picker>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="searchOperationLogs">查询</el-button>
                <el-button @click="resetOperationLogQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <el-table :data="operationLogList" style="width: 100%" stripe>
            <el-table-column prop="id" label="日志ID" width="120"></el-table-column>
            <el-table-column prop="templateId" label="模板ID" width="120"></el-table-column>
            <el-table-column prop="templateName" label="模板名称" width="180"></el-table-column>
            <el-table-column prop="operationType" label="操作类型" width="100">
              <template #default="scope">
                <el-tag :type="getOperationTypeTagType(scope.row.operationType)">
                  {{ getOperationTypeText(scope.row.operationType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operator" label="操作人" width="120"></el-table-column>
            <el-table-column prop="operationTime" label="操作时间" width="180"></el-table-column>
            <el-table-column prop="ipAddress" label="IP地址" width="120"></el-table-column>
            <el-table-column prop="operationDetails" label="操作详情" min-width="200">
              <template #default="scope">
                <el-button type="text" size="small" @click="showOperationDetails(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="operationLogPage.currentPage"
              v-model:page-size="operationLogPage.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="operationLogPage.total"
              @size-change="handleOperationLogSizeChange"
              @current-change="handleOperationLogCurrentChange"
            ></el-pagination>
          </div>
        </el-tab-pane>
        
        <!-- 发送日志标签页 -->
        <el-tab-pane label="发送日志" name="sending">
          <div class="log-filter">
            <el-form :inline="true" :model="sendingLogQuery" class="filter-form">
              <el-form-item label="模板ID">
                <el-input v-model="sendingLogQuery.templateId" placeholder="请输入模板ID"></el-input>
              </el-form-item>
              <el-form-item label="接收人">
                <el-input v-model="sendingLogQuery.recipient" placeholder="请输入接收人"></el-input>
              </el-form-item>
              <el-form-item label="通知类型">
                <el-select v-model="sendingLogQuery.notificationType" placeholder="请选择通知类型">
                  <el-option label="邮件" value="EMAIL"></el-option>
                  <el-option label="短信" value="SMS"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="发送状态">
                <el-select v-model="sendingLogQuery.status" placeholder="请选择发送状态">
                  <el-option label="成功" value="SUCCESS"></el-option>
                  <el-option label="失败" value="FAILURE"></el-option>
                  <el-option label="待发送" value="PENDING"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="发送时间">
                <el-date-picker
                  v-model="sendingLogTimeRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                ></el-date-picker>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="searchSendingLogs">查询</el-button>
                <el-button @click="resetSendingLogQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <el-table :data="sendingLogList" style="width: 100%" stripe>
            <el-table-column prop="id" label="日志ID" width="120"></el-table-column>
            <el-table-column prop="templateId" label="模板ID" width="120"></el-table-column>
            <el-table-column prop="templateName" label="模板名称" width="180"></el-table-column>
            <el-table-column prop="type" label="通知类型" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.type === 'EMAIL' ? 'primary' : 'warning'">
                  {{ scope.row.type === 'EMAIL' ? '邮件' : '短信' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="receiver" label="接收人" width="180"></el-table-column>
            <el-table-column prop="success" label="发送状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.success ? 'success' : 'danger'">
                  {{ scope.row.success ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sendTime" label="发送时间" width="180"></el-table-column>
            <el-table-column prop="errorMessage" label="错误信息" min-width="200"></el-table-column>
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="sendingLogPage.currentPage"
              v-model:page-size="sendingLogPage.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="sendingLogPage.total"
              @size-change="handleSendingLogSizeChange"
              @current-change="handleSendingLogCurrentChange"
            ></el-pagination>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 操作详情对话框 -->
    <el-dialog
      v-model="operationDetailVisible"
      title="操作详情"
      width="700px"
    >
      <div v-if="selectedOperationLog" class="log-detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="日志ID">{{ selectedOperationLog.id }}</el-descriptions-item>
          <el-descriptions-item label="模板ID">{{ selectedOperationLog.templateId }}</el-descriptions-item>
          <el-descriptions-item label="模板名称">{{ selectedOperationLog.templateName }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">{{ getOperationTypeText(selectedOperationLog.operationType) }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ selectedOperationLog.operator }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ selectedOperationLog.operationTime }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ selectedOperationLog.ipAddress }}</el-descriptions-item>
          <el-descriptions-item label="详情">
            <pre>{{ formatOperationDetails(selectedOperationLog.operationDetails) }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
    

  </div>
</template>

<script>
import NotificationTemplateService from '../services/NotificationTemplateService'

export default {
  name: 'NotificationLogView',
  data() {
    return {
      activeTab: 'operation',
      // 操作日志相关
      operationLogList: [],
      operationLogQuery: {
        operationType: '',
        operator: '',
        templateId: ''
      },
      operationLogTimeRange: [],
      operationLogPage: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      // 发送日志相关
      sendingLogList: [],
      sendingLogQuery: {
        templateId: '',
        recipient: '',
        notificationType: '',
        status: ''
      },
      sendingLogTimeRange: [],
      sendingLogPage: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      // 对话框相关
      operationDetailVisible: false,
      selectedOperationLog: null
    }
  },
  mounted() {
    this.fetchOperationLogs()
  },
  watch: {
    activeTab(newTab) {
      if (newTab === 'sending' && this.sendingLogList.length === 0) {
        this.fetchSendingLogs()
      }
    }
  },
  methods: {
    // 操作日志相关方法
    fetchOperationLogs() {
      const params = {
        ...this.operationLogQuery,
        pageNum: this.operationLogPage.currentPage,
        pageSize: this.operationLogPage.pageSize
      }
      
      if (this.operationLogTimeRange && this.operationLogTimeRange.length === 2) {
        params.startTime = this.operationLogTimeRange[0]
        params.endTime = this.operationLogTimeRange[1]
      }
      
      NotificationTemplateService.getTemplateOperationLogs(params).then(data => {
        this.operationLogList = data.list || []
        this.operationLogPage.total = data.total || 0
      }).catch(error => {
        console.error('获取操作日志失败:', error)
        this.$message.error('获取操作日志失败')
      })
    },
    
    searchOperationLogs() {
      this.operationLogPage.currentPage = 1
      this.fetchOperationLogs()
    },
    
    resetOperationLogQuery() {
      this.operationLogQuery = {
        operationType: '',
        operator: '',
        templateId: ''
      }
      this.operationLogTimeRange = []
      this.operationLogPage.currentPage = 1
      this.fetchOperationLogs()
    },
    
    handleOperationLogSizeChange(size) {
      this.operationLogPage.pageSize = size
      this.fetchOperationLogs()
    },
    
    handleOperationLogCurrentChange(current) {
      this.operationLogPage.currentPage = current
      this.fetchOperationLogs()
    },
    
    showOperationDetails(log) {
      this.selectedOperationLog = log
      this.operationDetailVisible = true
    },
    
    // 发送日志相关方法
    fetchSendingLogs() {
      const params = {
        ...this.sendingLogQuery,
        pageNum: this.sendingLogPage.currentPage,
        pageSize: this.sendingLogPage.pageSize
      }
      
      if (this.sendingLogTimeRange && this.sendingLogTimeRange.length === 2) {
        params.startTime = this.sendingLogTimeRange[0]
        params.endTime = this.sendingLogTimeRange[1]
      }
      
      NotificationTemplateService.getNotificationLogs(params).then(data => {
        this.sendingLogList = data.list || []
        this.sendingLogPage.total = data.total || 0
      }).catch(error => {
        console.error('获取发送日志失败:', error)
        this.$message.error('获取发送日志失败')
      })
    },
    
    searchSendingLogs() {
      this.sendingLogPage.currentPage = 1
      this.fetchSendingLogs()
    },
    
    resetSendingLogQuery() {
      this.sendingLogQuery = {
        templateId: '',
        recipient: '',
        notificationType: '',
        status: ''
      }
      this.sendingLogTimeRange = []
      this.sendingLogPage.currentPage = 1
      this.fetchSendingLogs()
    },
    
    handleSendingLogSizeChange(size) {
      this.sendingLogPage.pageSize = size
      this.fetchSendingLogs()
    },
    
    handleSendingLogCurrentChange(current) {
      this.sendingLogPage.currentPage = current
      this.fetchSendingLogs()
    },
    
    // 辅助方法
    getOperationTypeTagType(type) {
      const typeMap = {
        CREATE: 'success',
        UPDATE: 'primary',
        DELETE: 'danger',
        ENABLE: 'warning',
        DISABLE: 'info'
      }
      return typeMap[type] || 'default'
    },
    
    getOperationTypeText(type) {
      const typeMap = {
        CREATE: '创建',
        UPDATE: '更新',
        DELETE: '删除',
        ENABLE: '启用',
        DISABLE: '禁用'
      }
      return typeMap[type] || type
    },
    
    formatOperationDetails(details) {
      try {
        if (typeof details === 'string') {
          // 尝试解析为JSON，如果失败则直接返回原始字符串
          try {
            const parsed = JSON.parse(details)
            return JSON.stringify(parsed, null, 2)
          } catch (jsonError) {
            // 如果不是JSON格式，直接返回原始字符串
            return details
          }
        } else if (typeof details === 'object') {
          return JSON.stringify(details, null, 2)
        }
      } catch (e) {
        console.error('格式化详情失败:', e)
      }
      return details || ''
    }
  }
}
</script>

<style scoped>
.notification-log-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-filter {
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.log-detail-content pre {
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.log-detail-content .el-descriptions__label {
  font-weight: 600;
}
</style>