<template>
  <div class="tenant-stats-container">
    <h2 class="page-title">租户使用统计</h2>
    
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
        
        <el-form-item label="租户名称">
          <el-input v-model="filterForm.tenantName" placeholder="请输入租户名称" style="width: 200px;" />
        </el-form-item>
        
        <el-form-item label="服务模块">
          <el-select v-model="filterForm.serviceModule" placeholder="请选择服务模块" style="width: 150px;">
            <el-option label="全部" value="all" />
            <el-option label="通知模块" value="notification" />
            <el-option label="工作流模块" value="workflow" />
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
            <div class="overview-value">{{ totalTenants }}</div>
            <div class="overview-label">总租户数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-value">{{ totalApiCalls }}</div>
            <div class="overview-label">总API调用</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-value">{{ avgSuccessRate }}%</div>
            <div class="overview-label">平均成功率</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <div class="overview-value">{{ avgResponseTime }}ms</div>
            <div class="overview-label">平均响应时间</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 租户使用趋势图 -->
    <el-card shadow="hover" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>租户使用趋势</span>
        </div>
      </template>
      <div id="usageTrendChart" class="chart-container"></div>
    </el-card>
    
    <!-- 租户使用排行 -->
    <el-card shadow="hover" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>租户使用排行</span>
        </div>
      </template>
      <div id="usageRankingChart" class="chart-container"></div>
    </el-card>
    
    <!-- 租户详细列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="card-header">
          <span>租户使用详情</span>
        </div>
      </template>
      <el-table :data="tenantStatsList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="tenantId" label="租户ID" width="120" />
        <el-table-column prop="tenantName" label="租户名称" width="150" />
        <el-table-column prop="serviceModule" label="服务模块" width="120" />
        <el-table-column prop="apiCalls" label="API调用次数" width="120" />
        <el-table-column prop="successCount" label="成功次数" width="120" />
        <el-table-column prop="failCount" label="失败次数" width="120" />
        <el-table-column prop="successRate" label="成功率" width="120">
          <template #default="scope">
            <el-progress :percentage="scope.row.successRate" :color="getProgressColor(scope.row.successRate)" :stroke-width="6" />
          </template>
        </el-table-column>
        <el-table-column prop="avgResponseTime" label="平均响应时间(ms)" width="150" />
        <el-table-column prop="maxResponseTime" label="最长响应时间(ms)" width="150" />
        <el-table-column prop="lastCallTime" label="最后调用时间" width="180" />
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
        :total="tenantStatsList.length"
        :page-size="10"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import MonitorService from '../../services/MonitorService'

// 筛选条件
const filterForm = reactive({
  timeRange: [],
  tenantName: '',
  serviceModule: 'all'
})

// 统计数据
const totalTenants = ref(0)
const totalApiCalls = ref(0)
const avgSuccessRate = ref(0)
const avgResponseTime = ref(0)

// 表格数据
const tenantStatsList = ref([])

// 加载状态
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 图表实例
let usageTrendChart = null
let usageRankingChart = null

// 初始化图表
const initCharts = () => {
  // 初始化使用趋势图
  usageTrendChart = echarts.init(document.getElementById('usageTrendChart'))
  // 初始化使用排行图
  usageRankingChart = echarts.init(document.getElementById('usageRankingChart'))
  
  // 初始化空图表配置，不显示默认数据
  const emptyOption = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['API调用量', '租户数'],
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
    yAxis: [
      {
        type: 'value',
        name: 'API调用量',
        axisLabel: {
          formatter: '{value}'
        }
      },
      {
        type: 'value',
        name: '租户数',
        axisLabel: {
          formatter: '{value}个'
        }
      }
    ],
    series: [
      {
        name: 'API调用量',
        type: 'line',
        data: [],
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.1)' }
          ])
        }
      },
      {
        name: '租户数',
        type: 'bar',
        yAxisIndex: 1,
        data: [],
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  }
  
  // 使用空配置初始化图表
  usageTrendChart.setOption(emptyOption)
  
  // 使用空配置初始化排行图
  const emptyRankingOption = {
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
        name: 'API调用次数',
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
  usageRankingChart.setOption(emptyRankingOption)
}

// 更新使用趋势图
const updateUsageTrendChart = (data) => {
  // 只使用从API获取的数据，不使用默认数据
  const dates = data.map(item => item.date)
  const callCounts = data.map(item => item.callCount)
  const tenantCounts = data.map(item => item.tenantCount)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['API调用量', '租户数'],
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
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: 'API调用量',
        axisLabel: {
          formatter: '{value}'
        }
      },
      {
        type: 'value',
        name: '租户数',
        axisLabel: {
          formatter: '{value}个'
        }
      }
    ],
    series: [
      {
        name: 'API调用量',
        type: 'line',
        data: callCounts,
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.1)' }
          ])
        }
      },
      {
        name: '租户数',
        type: 'bar',
        yAxisIndex: 1,
        data: tenantCounts,
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  }
  usageTrendChart.setOption(option)
}

// 更新使用排行图
const updateUsageRankingChart = (data) => {
  // 从租户统计列表中获取排行数据
  const rankingData = [...tenantStatsList.value]
    .sort((a, b) => a.apiCalls - b.apiCalls) // 升序排列，图表显示时会从下到上
    .slice(0, 10) // 只显示前10名
  
  const option = {
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
      data: rankingData.map(item => item.tenantName)
    },
    series: [
      {
        name: 'API调用次数',
        type: 'bar',
        data: rankingData.map(item => item.apiCalls),
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
  usageRankingChart.setOption(option)
}

// 窗口大小改变时，重新调整图表大小
const handleResize = () => {
  usageTrendChart?.resize()
  usageRankingChart?.resize()
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
const handleQuery = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      startDate: filterForm.timeRange[0] ? filterForm.timeRange[0].toISOString() : '',
      endDate: filterForm.timeRange[1] ? filterForm.timeRange[1].toISOString() : '',
      tenantName: filterForm.tenantName,
      serviceModule: filterForm.serviceModule === 'all' ? '' : filterForm.serviceModule
    }
    
    // 调用后端API获取真实数据
    const response = await MonitorService.getTenantStats(params)
    
    // 更新统计数据
    if (response && response.totalTenants !== undefined) {
      totalTenants.value = response.totalTenants
      totalApiCalls.value = response.totalApiCalls || 0
      avgSuccessRate.value = response.avgSuccessRate || 0
      avgResponseTime.value = response.avgResponseTime || 0
      
      // 更新表格数据
      if (response.tenantStatsList) {
        tenantStatsList.value = response.tenantStatsList
      }
      
      // 更新图表数据
      updateUsageRankingChart(tenantStatsList.value)
      
      // 使用从API获取的趋势数据
      if (response.apiTrend && response.apiTrend.length > 0) {
        updateUsageTrendChart(response.apiTrend)
      }
      
      ElMessage.success('查询成功')
    } else {
      ElMessage.warning('未获取到有效数据')
    }
  } catch (error) {
    console.error('获取租户统计数据失败:', error)
    ElMessage.error('获取数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 重置
const handleReset = () => {
  filterForm.timeRange = []
  filterForm.tenantName = ''
  filterForm.serviceModule = 'all'
}

// 导出数据
const handleExport = () => {
  // 模拟导出请求
  ElMessage.success('数据导出成功')
}

// 查看详情
const handleViewDetail = (row) => {
  ElMessage.info(`查看 ${row.tenantName} 的详细信息`)
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
  // 页面加载时自动获取数据
  handleQuery()
})

// 监听筛选条件变化
watch(filterForm, (newVal, oldVal) => {
  // 可以在这里添加防抖逻辑，自动刷新数据
}, { deep: true })
</script>

<style scoped>
.tenant-stats-container {
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
  padding: 20px;
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
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 8px;
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
</style>