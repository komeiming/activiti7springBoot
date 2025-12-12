<template>
  <div class="call-stats-container">
    <h3 class="section-title">调用统计</h3>
    
    <!-- 筛选区 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="filterForm" label-position="left">
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
        
        <el-form-item label="服务模块">
          <el-select v-model="filterForm.serviceModule" placeholder="请选择服务模块">
            <el-option label="全部" value="all" />
            <el-option label="通知模块" value="notification" />
            <el-option label="工作流模块" value="workflow" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="接口名称">
          <el-select v-model="filterForm.apiName" placeholder="请选择接口名称">
            <el-option label="全部" value="all" />
            <el-option label="发送通知接口" value="send-notification" />
            <el-option label="创建模板接口" value="create-template" />
            <el-option label="查询模板接口" value="get-template" />
            <el-option label="创建流程实例接口" value="create-instance" />
            <el-option label="查询流程实例接口" value="get-instance" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出报表</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 统计图表区 -->
    <div class="charts-section">
      <!-- 调用量趋势图 -->
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>调用量趋势</span>
          </div>
        </template>
        <div id="callTrendChart" class="chart-container"></div>
      </el-card>
      
      <!-- 接口调用排行 -->
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>接口调用排行</span>
          </div>
        </template>
        <div id="apiRankChart" class="chart-container"></div>
      </el-card>
      
      <!-- 成功率趋势图 -->
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>成功率趋势</span>
          </div>
        </template>
        <div id="successRateChart" class="chart-container"></div>
      </el-card>
      
      <!-- 响应时间分布 -->
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>响应时间分布</span>
          </div>
        </template>
        <div id="responseTimeChart" class="chart-container"></div>
      </el-card>
    </div>
    
    <!-- 数据表格区 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="card-header">
          <span>接口调用明细</span>
        </div>
      </template>
      
      <el-table :data="interfaceData" border style="width: 100%">
        <el-table-column prop="apiName" label="接口名称" width="200" />
        <el-table-column prop="callCount" label="调用次数" width="120" />
        <el-table-column prop="successCount" label="成功次数" width="120" />
        <el-table-column prop="failCount" label="失败次数" width="120" />
        <el-table-column prop="successRate" label="成功率" width="120">
          <template #default="scope">
            <el-progress :percentage="scope.row.successRate" :color="getProgressColor(scope.row.successRate)" :stroke-width="6" />
          </template>
        </el-table-column>
        <el-table-column prop="avgResponseTime" label="平均响应时间(ms)" width="150" />
        <el-table-column prop="maxResponseTime" label="最长响应时间(ms)" width="150" />
        <el-table-column prop="minResponseTime" label="最短响应时间(ms)" width="150" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleViewLogs(scope.row)">
              查看日志
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        class="mt-20"
        background
        layout="total, prev, pager, next, jumper"
        :total="interfaceData.length"
        :page-size="10"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

// 筛选表单
const filterForm = reactive({
  timeRange: [],
  serviceModule: 'all',
  apiName: 'all'
})

// 接口数据
const interfaceData = ref([
  {
    apiName: '发送通知接口',
    callCount: 1250,
    successCount: 1230,
    failCount: 20,
    successRate: 98.4,
    avgResponseTime: 150,
    maxResponseTime: 500,
    minResponseTime: 50
  },
  {
    apiName: '创建模板接口',
    callCount: 890,
    successCount: 875,
    failCount: 15,
    successRate: 98.3,
    avgResponseTime: 120,
    maxResponseTime: 350,
    minResponseTime: 40
  },
  {
    apiName: '查询模板接口',
    callCount: 2100,
    successCount: 2090,
    failCount: 10,
    successRate: 99.5,
    avgResponseTime: 80,
    maxResponseTime: 200,
    minResponseTime: 20
  },
  {
    apiName: '创建流程实例接口',
    callCount: 650,
    successCount: 640,
    failCount: 10,
    successRate: 98.5,
    avgResponseTime: 200,
    maxResponseTime: 600,
    minResponseTime: 60
  },
  {
    apiName: '查询流程实例接口',
    callCount: 1800,
    successCount: 1780,
    failCount: 20,
    successRate: 98.9,
    avgResponseTime: 100,
    maxResponseTime: 300,
    minResponseTime: 30
  }
])

// 图表实例
let callTrendChart = null
let apiRankChart = null
let successRateChart = null
let responseTimeChart = null

// 初始化图表
const initCharts = () => {
  // 调用量趋势图
  callTrendChart = echarts.init(document.getElementById('callTrendChart'))
  const callTrendOption = {
    title: {
      text: '',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['调用量'],
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
      data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '调用量',
        type: 'line',
        data: [120, 132, 101, 134, 90, 230, 210, 250, 220, 180, 160, 150],
        smooth: true,
        areaStyle: {}
      }
    ]
  }
  callTrendChart.setOption(callTrendOption)
  
  // 接口调用排行图
  apiRankChart = echarts.init(document.getElementById('apiRankChart'))
  const apiRankOption = {
    title: {
      text: '',
      left: 'center'
    },
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
      data: ['查询模板接口', '发送通知接口', '查询流程实例接口', '创建模板接口', '创建流程实例接口']
    },
    series: [
      {
        name: '调用次数',
        type: 'bar',
        data: [2100, 1250, 1800, 890, 650],
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
  apiRankChart.setOption(apiRankOption)
  
  // 成功率趋势图
  successRateChart = echarts.init(document.getElementById('successRateChart'))
  const successRateOption = {
    title: {
      text: '',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>成功率: {c}%'
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
      data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00']
    },
    yAxis: {
      type: 'value',
      min: 90,
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: '成功率',
        type: 'line',
        data: [99.2, 98.8, 99.5, 99.0, 98.5, 98.0, 98.5, 99.0, 99.2, 98.8, 99.0, 99.1],
        smooth: true,
        lineStyle: {
          color: '#52c41a'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.1)' }
          ])
        }
      }
    ]
  }
  successRateChart.setOption(successRateOption)
  
  // 响应时间分布
  responseTimeChart = echarts.init(document.getElementById('responseTimeChart'))
  const responseTimeOption = {
    title: {
      text: '',
      left: 'center'
    },
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
        data: [
          { value: 45, name: '50ms以下' },
          { value: 30, name: '50-100ms' },
          { value: 15, name: '100-200ms' },
          { value: 8, name: '200-500ms' },
          { value: 2, name: '500ms以上' }
        ]
      }
    ]
  }
  responseTimeChart.setOption(responseTimeOption)
}

// 窗口大小改变时，重新调整图表大小
const handleResize = () => {
  callTrendChart?.resize()
  apiRankChart?.resize()
  successRateChart?.resize()
  responseTimeChart?.resize()
}

// 获取进度条颜色
const getProgressColor = (percentage) => {
  if (percentage >= 95) {
    return '#52c41a'
  } else if (percentage >= 90) {
    return '#faad14'
  } else {
    return '#ff4d4f'
  }
}

// 查询
const handleQuery = () => {
  getCallStats()
}

// 获取调用统计数据
const getCallStats = async () => {
  try {
    loading.value = true
    
    // 构建请求参数
    const params = {
      serviceModule: filterForm.serviceModule === 'all' ? undefined : filterForm.serviceModule,
      apiName: filterForm.apiName === 'all' ? undefined : filterForm.apiName
    }
    
    // 添加时间范围参数
    if (filterForm.timeRange && filterForm.timeRange.length === 2) {
      params.startTime = filterForm.timeRange[0]
      params.endTime = filterForm.timeRange[1]
    }
    
    // 调用后端API获取调用统计数据
    const response = await axios.get('/v1/monitor/call-stats', { params })
    
    // 更新数据
    interfaceData.value = response.interfaceData || []
    
    // 更新图表数据
    updateCharts(response)
    
    ElMessage.success('查询成功')
  } catch (error) {
    console.error('获取调用统计数据失败:', error)
    ElMessage.error('获取调用统计数据失败: ' + (error.message || '请稍后重试'))
  } finally {
    loading.value = false
  }
}

// 更新图表数据
const updateCharts = (data) => {
  // 更新调用量趋势图
  if (callTrendChart && data.callTrendData) {
    const callTrendOption = callTrendChart.getOption()
    if (callTrendOption && callTrendOption.xAxis && callTrendOption.series) {
      callTrendOption.xAxis[0].data = data.callTrendData.map(item => item.time)
      callTrendOption.series[0].data = data.callTrendData.map(item => item.callCount)
      callTrendChart.setOption(callTrendOption)
    }
  }
  
  // 更新成功率趋势图
  if (successRateChart && data.successRateData) {
    const successRateOption = successRateChart.getOption()
    if (successRateOption && successRateOption.xAxis && successRateOption.series) {
      successRateOption.xAxis[0].data = data.successRateData.map(item => item.time)
      successRateOption.series[0].data = data.successRateData.map(item => item.successRate)
      successRateChart.setOption(successRateOption)
    }
  }
  
  // 更新接口调用排行图
  if (apiRankChart && data.interfaceData) {
    const apiRankOption = apiRankChart.getOption()
    if (apiRankOption && apiRankOption.yAxis && apiRankOption.series) {
      // 按调用次数排序
      const sortedData = [...data.interfaceData].sort((a, b) => b.callCount - a.callCount)
      apiRankOption.yAxis[0].data = sortedData.map(item => item.apiName)
      apiRankOption.series[0].data = sortedData.map(item => item.callCount)
      apiRankChart.setOption(apiRankOption)
    }
  }
  
  // 更新响应时间分布图
  if (responseTimeChart && data.responseTimeData) {
    const responseTimeOption = responseTimeChart.getOption()
    if (responseTimeOption && responseTimeOption.series) {
      responseTimeOption.series[0].data = data.responseTimeData.map(item => ({
        value: item.count,
        name: item.range
      }))
      responseTimeChart.setOption(responseTimeOption)
    }
  }
}

// 重置
const handleReset = () => {
  filterForm.timeRange = []
  filterForm.serviceModule = 'all'
  filterForm.apiName = 'all'
}

// 导出报表
const handleExport = () => {
  // 模拟导出请求
  ElMessage.success('报表导出成功')
}

// 查看日志
const handleViewLogs = (row) => {
  // 跳转到日志查询页面
  ElMessage.info(`查看 ${row.apiName} 的日志`)
}

// 生命周期钩子
onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
})

// 监听筛选条件变化
watch(filterForm, (newVal, oldVal) => {
  // 可以在这里添加防抖逻辑，自动刷新数据
}, { deep: true })
</script>

<style scoped>
.call-stats-container {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 0;
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
  margin-bottom: 0;
}

.mt-20 {
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
}
</style>