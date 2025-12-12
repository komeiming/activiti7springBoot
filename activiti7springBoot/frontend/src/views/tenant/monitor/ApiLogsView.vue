<template>
  <div class="api-logs-container">
    <h3 class="section-title">日志查询</h3>
    
    <!-- 标签页切换 -->
    <el-card shadow="hover" class="tab-card">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="调用日志" name="call-logs">
          <div class="log-content">
            <!-- 筛选条件 -->
            <el-form :inline="true" :model="callLogFilter" label-position="left" class="log-filter">
              <el-form-item label="接口名称">
                <el-select v-model="callLogFilter.apiName" placeholder="请选择接口名称">
                  <el-option label="全部" value="all" />
                  <el-option label="发送通知接口" value="send-notification" />
                  <el-option label="创建模板接口" value="create-template" />
                  <el-option label="查询模板接口" value="get-template" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="请求方式">
                <el-select v-model="callLogFilter.method" placeholder="请选择请求方式">
                  <el-option label="全部" value="all" />
                  <el-option label="GET" value="GET" />
                  <el-option label="POST" value="POST" />
                  <el-option label="PUT" value="PUT" />
                  <el-option label="DELETE" value="DELETE" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="响应状态码">
                <el-input v-model="callLogFilter.statusCode" placeholder="请输入响应状态码" maxlength="3" />
              </el-form-item>
              
              <el-form-item label="调用时间">
                <el-date-picker
                  v-model="callLogFilter.timeRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 350px;"
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="handleCallLogQuery">查询</el-button>
                <el-button @click="handleCallLogReset">重置</el-button>
              </el-form-item>
            </el-form>
            
            <!-- 调用日志列表 -->
            <el-table :data="callLogs" border style="width: 100%" v-loading="callLogsLoading">
              <el-table-column prop="logId" label="日志ID" width="180" />
              <el-table-column prop="apiName" label="接口名称" width="150" />
              <el-table-column prop="method" label="请求方式" width="100">
                <template #default="scope">
                  <el-tag :type="getMethodTagType(scope.row.method)">
                    {{ scope.row.method }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="requestParams" label="请求参数" width="200">
                <template #default="scope">
                  <el-button type="text" size="small" @click="handleViewRequestParams(scope.row)">
                    查看
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column prop="responseResult" label="响应结果" width="200">
                <template #default="scope">
                  <el-button type="text" size="small" @click="handleViewResponseResult(scope.row)">
                    查看
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column prop="statusCode" label="响应状态码" width="120">
                <template #default="scope">
                  <el-tag :type="getStatusCodeTagType(scope.row.statusCode)">
                    {{ scope.row.statusCode }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="responseTime" label="响应时间(ms)" width="120" />
              <el-table-column prop="callTime" label="调用时间" width="180" />
              <el-table-column prop="appId" label="APP ID" width="150" />
            </el-table>
            
            <!-- 分页 -->
            <el-pagination
              class="mt-20"
              background
              layout="total, prev, pager, next, jumper"
              :total="callLogs.length"
              :page-size="10"
            />
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="操作日志" name="operation-logs">
          <div class="log-content">
            <!-- 筛选条件 -->
            <el-form :inline="true" :model="operationLogFilter" label-position="left" class="log-filter">
              <el-form-item label="操作类型">
                <el-select v-model="operationLogFilter.operationType" placeholder="请选择操作类型">
                  <el-option label="全部" value="all" />
                  <el-option label="模板创建" value="template-create" />
                  <el-option label="模板编辑" value="template-edit" />
                  <el-option label="模板删除" value="template-delete" />
                  <el-option label="流程启动" value="process-start" />
                  <el-option label="流程终止" value="process-terminate" />
                  <el-option label="权限变更" value="permission-change" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="操作结果">
                <el-select v-model="operationLogFilter.operationResult" placeholder="请选择操作结果">
                  <el-option label="全部" value="all" />
                  <el-option label="成功" value="success" />
                  <el-option label="失败" value="fail" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="操作人员">
                <el-input v-model="operationLogFilter.operator" placeholder="请输入操作人员" maxlength="20" />
              </el-form-item>
              
              <el-form-item label="操作时间">
                <el-date-picker
                  v-model="operationLogFilter.timeRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 350px;"
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="handleOperationLogQuery">查询</el-button>
                <el-button @click="handleOperationLogReset">重置</el-button>
              </el-form-item>
            </el-form>
            
            <!-- 操作日志列表 -->
            <el-table :data="operationLogs" border style="width: 100%" v-loading="operationLogsLoading">
              <el-table-column prop="logId" label="日志ID" width="180" />
              <el-table-column prop="operationType" label="操作类型" width="150" />
              <el-table-column prop="operationDesc" label="操作描述" />
              <el-table-column prop="operationResult" label="操作结果" width="120">
                <template #default="scope">
                  <el-tag :type="scope.row.operationResult === 'success' ? 'success' : 'danger'">
                    {{ scope.row.operationResult === 'success' ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="operator" label="操作人员" width="120" />
              <el-table-column prop="operationTime" label="操作时间" width="180" />
              <el-table-column prop="ipAddress" label="IP地址" width="150" />
            </el-table>
            
            <!-- 分页 -->
            <el-pagination
              class="mt-20"
              background
              layout="total, prev, pager, next, jumper"
              :total="operationLogs.length"
              :page-size="10"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 导出按钮 -->
      <div class="export-section">
        <el-button type="success" @click="handleExportLogs">导出日志</el-button>
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </el-card>
    
    <!-- 请求参数详情弹窗 -->
    <el-dialog
      v-model="requestParamsVisible"
      title="请求参数"
      width="600px"
    >
      <pre v-if="selectedCallLog" class="json-pre">{{ JSON.stringify(JSON.parse(selectedCallLog.requestParams), null, 2) }}</pre>
      <template #footer>
        <el-button @click="requestParamsVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleCopyParams">复制</el-button>
      </template>
    </el-dialog>
    
    <!-- 响应结果详情弹窗 -->
    <el-dialog
      v-model="responseResultVisible"
      title="响应结果"
      width="600px"
    >
      <pre v-if="selectedCallLog" class="json-pre">{{ JSON.stringify(JSON.parse(selectedCallLog.responseResult), null, 2) }}</pre>
      <template #footer>
        <el-button @click="responseResultVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleCopyResult">复制</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

// 活动标签页
const activeTab = ref('call-logs')

// 调用日志相关
const callLogFilter = reactive({
  apiName: 'all',
  method: 'all',
  statusCode: '',
  timeRange: []
})

const callLogs = ref([
  {
    logId: 'CALL_LOG_001',
    apiName: '发送通知接口',
    method: 'POST',
    requestParams: '{"templateId": "TEMP_001", "receivers": ["13800138000"], "variables": {"name": "张三"}}',
    responseResult: '{"code": 200, "message": "发送成功", "taskId": "TASK_001"}',
    statusCode: 200,
    responseTime: 150,
    callTime: '2025-01-15 14:30:00',
    appId: 'APP_ID_1234567890'
  },
  {
    logId: 'CALL_LOG_002',
    apiName: '创建模板接口',
    method: 'POST',
    requestParams: '{"name": "测试模板", "content": "Hello {{name}}", "type": "sms"}',
    responseResult: '{"code": 200, "message": "创建成功", "templateId": "TEMP_002"}',
    statusCode: 200,
    responseTime: 120,
    callTime: '2025-01-15 14:25:00',
    appId: 'APP_ID_1234567890'
  },
  {
    logId: 'CALL_LOG_003',
    apiName: '查询模板接口',
    method: 'GET',
    requestParams: '{"templateId": "TEMP_001"}',
    responseResult: '{"code": 404, "message": "模板不存在"}',
    statusCode: 404,
    responseTime: 80,
    callTime: '2025-01-15 14:20:00',
    appId: 'APP_ID_1234567890'
  }
])

const callLogsLoading = ref(false)

// 操作日志相关
const operationLogFilter = reactive({
  operationType: 'all',
  operationResult: 'all',
  operator: '',
  timeRange: []
})

const operationLogs = ref([
  {
    logId: 'OP_LOG_001',
    operationType: 'template-create',
    operationDesc: '创建了通知模板：测试模板',
    operationResult: 'success',
    operator: 'admin',
    operationTime: '2025-01-15 14:25:00',
    ipAddress: '192.168.1.100'
  },
  {
    logId: 'OP_LOG_002',
    operationType: 'process-start',
    operationDesc: '启动了流程实例：请假流程',
    operationResult: 'success',
    operator: 'admin',
    operationTime: '2025-01-15 14:15:00',
    ipAddress: '192.168.1.100'
  },
  {
    logId: 'OP_LOG_003',
    operationType: 'permission-change',
    operationDesc: '修改了用户权限：添加了模板管理权限',
    operationResult: 'fail',
    operator: 'admin',
    operationTime: '2025-01-15 14:10:00',
    ipAddress: '192.168.1.100'
  }
])

const operationLogsLoading = ref(false)

// 详情弹窗相关
const requestParamsVisible = ref(false)
const responseResultVisible = ref(false)
const selectedCallLog = ref(null)

// 根据请求方式获取标签类型
const getMethodTagType = (method) => {
  switch (method) {
    case 'GET':
      return 'success'
    case 'POST':
      return 'primary'
    case 'PUT':
      return 'warning'
    case 'DELETE':
      return 'danger'
    default:
      return 'info'
  }
}

// 根据状态码获取标签类型
const getStatusCodeTagType = (statusCode) => {
  if (statusCode >= 200 && statusCode < 300) {
    return 'success'
  } else if (statusCode >= 300 && statusCode < 400) {
    return 'info'
  } else if (statusCode >= 400 && statusCode < 500) {
    return 'warning'
  } else {
    return 'danger'
  }
}

// 调用日志查询
const handleCallLogQuery = () => {
  callLogsLoading.value = true
  // 模拟查询请求
  setTimeout(() => {
    callLogsLoading.value = false
    ElMessage.success('调用日志查询成功')
  }, 500)
}

// 调用日志重置
const handleCallLogReset = () => {
  callLogFilter.apiName = 'all'
  callLogFilter.method = 'all'
  callLogFilter.statusCode = ''
  callLogFilter.timeRange = []
}

// 操作日志查询
const handleOperationLogQuery = () => {
  operationLogsLoading.value = true
  // 模拟查询请求
  setTimeout(() => {
    operationLogsLoading.value = false
    ElMessage.success('操作日志查询成功')
  }, 500)
}

// 操作日志重置
const handleOperationLogReset = () => {
  operationLogFilter.operationType = 'all'
  operationLogFilter.operationResult = 'all'
  operationLogFilter.operator = ''
  operationLogFilter.timeRange = []
}

// 导出日志
const handleExportLogs = () => {
  // 模拟导出请求
  ElMessage.success('日志导出成功')
}

// 刷新日志
const handleRefresh = () => {
  if (activeTab.value === 'call-logs') {
    handleCallLogQuery()
  } else {
    handleOperationLogQuery()
  }
}

// 查看请求参数
const handleViewRequestParams = (row) => {
  selectedCallLog.value = row
  requestParamsVisible.value = true
}

// 查看响应结果
const handleViewResponseResult = (row) => {
  selectedCallLog.value = row
  responseResultVisible.value = true
}

// 复制请求参数
const handleCopyParams = () => {
  if (selectedCallLog.value) {
    navigator.clipboard.writeText(selectedCallLog.value.requestParams)
      .then(() => {
        ElMessage.success('请求参数复制成功')
      })
      .catch(() => {
        ElMessage.error('请求参数复制失败')
      })
  }
}

// 复制响应结果
const handleCopyResult = () => {
  if (selectedCallLog.value) {
    navigator.clipboard.writeText(selectedCallLog.value.responseResult)
      .then(() => {
        ElMessage.success('响应结果复制成功')
      })
      .catch(() => {
        ElMessage.error('响应结果复制失败')
      })
  }
}
</script>

<style scoped>
.api-logs-container {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.tab-card {
  margin-bottom: 0;
}

.log-content {
  padding: 10px 0;
}

.log-filter {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.export-section {
  margin-top: 20px;
  text-align: right;
}

.mt-20 {
  margin-top: 20px;
}

.json-pre {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
  max-height: 400px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  line-height: 1.5;
}
</style>