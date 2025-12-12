<template>
  <div class="api-stats-container">
    <h2 class="page-title">API调用统计</h2>
    
    <!-- 筛选条件 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="filterForm" label-position="left" class="filter-form">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 350px;"
          />
        </el-form-item>
        
        <el-form-item label="API名称">
          <el-select v-model="filterForm.apiName" placeholder="请选择API名称" style="width: 200px;">
            <el-option label="全部" value="all" />
            <el-option label="发送通知接口" value="send-notification" />
            <el-option label="创建模板接口" value="create-template" />
            <el-option label="查询模板接口" value="get-template" />
            <el-option label="创建流程实例接口" value="create-instance" />
            <el-option label="查询流程实例接口" value="get-instance" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="请求方式">
          <el-select v-model="filterForm.method" placeholder="请选择请求方式" style="width: 120px;">
            <el-option label="全部" value="all" />
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" style="width: 120px;">
            <el-option label="全部" value="all" />
            <el-option label="成功" value="success" />
            <el-option label="失败" value="fail" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出数据</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 统计概览 -->
    <el-card shadow="hover" class="stats-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-value">{{ totalApiCalls }}</div>
            <div class="overview-label">总调用次数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-value">{{ totalSuccessCalls }}</div>
            <div class="overview-label">成功次数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-value">{{ totalFailCalls }}</div>
            <div class="overview-label">失败次数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-value">{{ successRate }}%</div>
            <div class="overview-label">成功率</div>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 10px;">
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-value">{{ avgResponseTime }}ms</div>
            <div class="overview-label">平均响应时间</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-value">{{ maxResponseTime }}ms</div>
            <div class="overview-label">最长响应时间</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-value">{{ minResponseTime }}ms</div>
            <div class="overview-label">最短响应时间</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-value">{{ activeApis }}</div>
            <div class="overview-label">活跃API数量</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 调用量趋势图 -->
    <el-card shadow="hover" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>API调用量趋势</span>
        </div>
      </template>
      <div id="apiCallTrendChart" class="chart-container"></div>
    </el-card>
    
    <!-- API调用排行 -->
    <el-card shadow="hover" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>API调用排行</span>
        </div>
      </template>
      <div id="apiCallRankingChart" class="chart-container"></div>
    </el-card>
    
    <!-- 响应时间分布 -->
    <el-card shadow="hover" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>响应时间分布</span>
        </div>
      </template>
      <div id="responseTimeDistributionChart" class="chart-container"></div>
    </el-card>
    
    <!-- API调用详情 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="card-header">
          <span>API调用详情</span>
        </div>
      </template>
      <el-table :data="apiCallLogs" border style="width: 100%" v-loading="loading">
        <el-table-column prop="logId" label="日志ID" width="150" show-overflow-tooltip />
        <el-table-column prop="apiPath" label="API路径" width="200" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方式" width="100">
          <template #default="scope">
            <el-tag :type="getRequestMethodTagType(scope.row.requestMethod)">
              {{ scope.row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusCode" label="响应状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusCodeTagType(scope.row.statusCode)">
              {{ scope.row.statusCode }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="success" label="是否成功" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.success ? 'success' : 'danger'">
              {{ scope.row.success ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseTime" label="响应时间(ms)" width="120" />
        <el-table-column prop="clientIp" label="客户端IP" width="120" />
        <el-table-column prop="callTime" label="调用时间" width="180" />
        <el-table-column prop="tenantId" label="租户ID" width="150" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        class="mt-20"
        background
        layout="total, prev, pager, next, jumper"
        :total="apiCallLogs.length"
        :page-size="10"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
    
    <!-- API调用详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="API调用详情"
      width="800px"
    >
      <div v-if="selectedLog" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="日志ID">{{ selectedLog.logId }}</el-descriptions-item>
          <el-descriptions-item label="API路径">{{ selectedLog.apiPath }}</el-descriptions-item>
          <el-descriptions-item label="请求方式">{{ selectedLog.requestMethod }}</el-descriptions-item>
          <el-descriptions-item label="请求参数">
            <pre class="json-pre">{{ formatJson(selectedLog.requestParams) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="响应数据">
            <pre class="json-pre">{{ formatJson(selectedLog.responseData) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="响应状态码">{{ selectedLog.statusCode }}</el-descriptions-item>
          <el-descriptions-item label="是否成功">{{ selectedLog.success ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="响应时间">{{ selectedLog.responseTime }}ms</el-descriptions-item>
          <el-descriptions-item label="客户端IP">{{ selectedLog.clientIp }}</el-descriptions-item>
          <el-descriptions-item label="调用时间">{{ selectedLog.callTime }}</el-descriptions-item>
          <el-descriptions-item label="租户ID">{{ selectedLog.tenantId }}</el-descriptions-item>
          <el-descriptions-item label="错误信息" v-if="selectedLog.errorMessage">
            <pre class="json-pre">{{ selectedLog.errorMessage }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import MonitorService from '../../services/MonitorService'
import axios from '@/utils/axiosConfig'

// 筛选条件
const filterForm = reactive({
  timeRange: [],
  apiName: 'all',
  method: 'all',
  status: 'all'
})

// 统计数据
const totalApiCalls = ref(12500)
const totalSuccessCalls = ref(12312)
const totalFailCalls = ref(188)
const successRate = ref(98.5)
const avgResponseTime = ref(120)
const maxResponseTime = ref(1200)
const minResponseTime = ref(20)
const activeApis = ref(25)

// API调用日志列表
const apiCallLogs = ref([
  {
    logId: 'LOG_001',
    apiPath: '/api/v1/notification/template',
    requestMethod: 'POST',
    requestParams: '{"name": "测试模板", "content": "Hello {{name}}", "type": "sms"}',
    responseData: '{"code": 200, "message": "模板创建成功", "data": {"templateId": "TEMP_001"}}',
    statusCode: 200,
    success: true,
    responseTime: 150,
    clientIp: '192.168.1.100',
    callTime: '2025-12-10 14:30:00',
    tenantId: 'TENANT_001',
    errorMessage: ''
  },
  {
    logId: 'LOG_002',
    apiPath: '/api/v1/workflow/template',
    requestMethod: 'GET',
    requestParams: '{"templateName": "请假流程", "page": 1, "size": 10}',
    responseData: '{"code": 200, "message": "查询成功", "data": [{"id": "TEMP_002", "name": "请假流程"}]}',
    statusCode: 200,
    success: true,
    responseTime: 80,
    clientIp: '192.168.1.101',
    callTime: '2025-12-10 14:25:00',
    tenantId: 'TENANT_002',
    errorMessage: ''
  },
  {
    logId: 'LOG_003',
    apiPath: '/api/v1/notification/send',
    requestMethod: 'POST',
    requestParams: '{"templateId": "TEMP_001", "receivers": ["13800138000"], "variables": {"name": "张三"}}',
    responseData: '{"code": 500, "message": "发送失败：短信服务不可用"}',
    statusCode: 500,
    success: false,
    responseTime: 300,
    clientIp: '192.168.1.102',
    callTime: '2025-12-10 14:20:00',
    tenantId: 'TENANT_001',
    errorMessage: '短信服务不可用'
  }
])

// 加载状态
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 图表实例
let apiCallTrendChart = null
let apiCallRankingChart = null
let responseTimeDistributionChart = null

// 详情弹窗
const detailVisible = ref(false)
const selectedLog = ref(null)

// 格式化JSON
const formatJson = (jsonStr) => {
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch (e) {
    return jsonStr
  }
}

// 获取请求方式标签类型
const getRequestMethodTagType = (method) => {
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

// 获取状态码标签类型
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

// 初始化图表
const initCharts = () => {
  // API调用量趋势图
  apiCallTrendChart = echarts.init(document.getElementById('apiCallTrendChart'))
  const apiCallTrendOption = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['总调用量', '成功', '失败'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: {
      type: 'value',
      name: '调用量'
    },
    series: [
      {
        name: '总调用量',
        type: 'line',
        data: [],
        smooth: true
      },
      {
        name: '成功',
        type: 'line',
        data: [],
        smooth: true
      },
      {
        name: '失败',
        type: 'line',
        data: [],
        smooth: true
      }
    ]
  }
  apiCallTrendChart.setOption(apiCallTrendOption)
  
  // API调用排行图
  apiCallRankingChart = echarts.init(document.getElementById('apiCallRankingChart'))
  const apiCallRankingOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: []
    },
    series: [
      {
        name: '调用次数',
        type: 'bar',
        data: [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }
  apiCallRankingChart.setOption(apiCallRankingOption)
  
  // 响应时间分布
  responseTimeDistributionChart = echarts.init(document.getElementById('responseTimeDistributionChart'))
  const responseTimeDistributionOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      bottom: 0
    },
    series: [
      {
        name: '响应时间分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: []
      }
    ]
  }
  responseTimeDistributionChart.setOption(responseTimeDistributionOption)
}

// 窗口大小改变时，重新调整图表大小
const handleResize = () => {
  apiCallTrendChart?.resize()
  apiCallRankingChart?.resize()
  responseTimeDistributionChart?.resize()
}

// 更新API调用量趋势图
const updateApiCallTrendChart = (data) => {
  if (!apiCallTrendChart || !data) return
  
  const trendData = data.trend || []
  const dates = trendData.map(item => item.date)
  const totalCalls = trendData.map(item => item.totalCalls || 0)
  const successCalls = trendData.map(item => item.successCalls || 0)
  const failCalls = trendData.map(item => item.failCalls || 0)
  
  const option = {
    xAxis: { data: dates },
    series: [
      { name: '总调用量', data: totalCalls },
      { name: '成功', data: successCalls },
      { name: '失败', data: failCalls }
    ]
  }
  apiCallTrendChart.setOption(option)
}

// 更新API调用排行图
const updateApiCallRankingChart = (data) => {
  if (!apiCallRankingChart || !data) return
  
  const rankingData = data.ranking || []
  const apiNames = rankingData.map(item => item.apiName)
  const callCounts = rankingData.map(item => item.callCount || 0)
  
  const option = {
    yAxis: { data: apiNames },
    series: [{ data: callCounts }]
  }
  apiCallRankingChart.setOption(option)
}

// 更新响应时间分布图
const updateResponseTimeDistributionChart = (data) => {
  if (!responseTimeDistributionChart || !data) return
  
  const distributionData = data.distribution || []
  
  const option = {
    series: [{ data: distributionData }]
  }
  responseTimeDistributionChart.setOption(option)
}

// 查询API调用日志
const handleQuery = async () => {
  loading.value = true
  try {
    // 构建请求参数
    const params = {
      apiName: filterForm.apiName === 'all' ? undefined : filterForm.apiName,
      method: filterForm.method === 'all' ? undefined : filterForm.method,
      status: filterForm.status === 'all' ? undefined : filterForm.status
    }
    
    // 添加时间范围参数
    if (filterForm.timeRange && filterForm.timeRange.length === 2) {
      params.startTime = filterForm.timeRange[0] ? filterForm.timeRange[0].toISOString() : '',
      params.endTime = filterForm.timeRange[1] ? filterForm.timeRange[1].toISOString() : ''
    }
    
    // 调用后端API获取真实数据
    const response = await MonitorService.getApiStats(params)
    
    // 提取响应数据
    const result = response.data || response
    
    // 更新基础统计数据
    totalApiCalls.value = result.totalApiCalls || 0
    totalSuccessCalls.value = result.totalSuccessCalls || 0
    totalFailCalls.value = result.totalFailCalls || 0
    successRate.value = result.successRate || 0
    avgResponseTime.value = result.avgResponseTime || 0
    maxResponseTime.value = result.maxResponseTime || 0
    minResponseTime.value = result.minResponseTime || 0
    activeApis.value = result.activeApis || 0
    
    // 更新表格数据
    apiCallLogs.value = result.apiCallLogs || []
    
    // 更新图表数据
    updateApiCallTrendChart(result)
    updateApiCallRankingChart(result)
    updateResponseTimeDistributionChart(result)
    
    ElMessage.success('查询成功')
  } catch (error) {
    console.error('获取API调用日志失败:', error)
    ElMessage.error('获取API调用日志失败: ' + (error.message || '请稍后重试'))
  } finally {
    loading.value = false
  }
}

// 重置
const handleReset = () => {
  filterForm.timeRange = []
  filterForm.apiName = 'all'
  filterForm.method = 'all'
  filterForm.status = 'all'
}

// 导出数据
const handleExport = () => {
  // 模拟导出请求
  ElMessage.success('数据导出成功')
}

// 查看详情
const handleViewDetail = (row) => {
  selectedLog.value = row
  detailVisible.value = true
}

// 分页
const handleSizeChange = (size) => {
  pageSize.value = size
}

const handleCurrentChange = (current) => {
  currentPage.value = current
}

// 生命周期钩子
onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
  // 初始加载数据
  handleQuery()
})

// 监听筛选条件变化
watch(filterForm, (newVal, oldVal) => {
  // 可以在这里添加防抖逻辑，自动刷新数据
}, { deep: true })
</script>

<style scoped>
.api-stats-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  padding: 10px;
}

.stats-overview {
  margin-bottom: 20px;
}

.overview-item {
  text-align: center;
  padding: 15px;
  background-color: #f0f2f5;
  border-radius: 8px;
  transition: all 0.3s;
}

.overview-item:hover {
  background-color: #e4e7ed;
  transform: translateY(-2px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.overview-value {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 5px;
}

.overview-label {
  font-size: 14px;
  color: #606266;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.card-header {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.table-card {
  margin-bottom: 20px;
}

.mt-20 {
  margin-top: 20px;
  text-align: right;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.json-pre {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
  line-height: 1.5;
  max-height: 300px;
}
</style>