<template>
  <div class="alert-config-container">
    <h3 class="section-title">告警配置</h3>
    
    <!-- 标签页切换 -->
    <el-card shadow="hover" class="tab-card">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="告警规则配置" name="alert-rules">
          <div class="alert-rules-content">
            <!-- 告警规则列表 -->
            <div class="rules-header">
              <h4>告警规则列表</h4>
              <el-button type="primary" @click="showAddRuleDialog = true">新增告警规则</el-button>
            </div>
            
            <el-table :data="alertRules" border style="width: 100%">
              <el-table-column prop="ruleName" label="规则名称" width="200" />
              <el-table-column prop="alertType" label="告警类型" width="150" />
              <el-table-column prop="triggerCondition" label="触发条件" />
              <el-table-column prop="alertMethod" label="告警方式" width="150">
                <template #default="scope">
                  <el-tag v-for="method in scope.row.alertMethod" :key="method" size="small" style="margin-right: 5px;">
                    {{ method === 'sms' ? '短信' : method === 'email' ? '邮箱' : '回调地址' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template #default="scope">
                  <el-switch v-model="scope.row.status" @change="handleToggleStatus(scope.row)" :active-value="'启用'" :inactive-value="'禁用'" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="handleEditRule(scope.row)">
                    编辑
                  </el-button>
                  <el-button type="danger" size="small" @click="handleDeleteRule(scope.row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 分页 -->
            <el-pagination
              class="mt-20"
              background
              layout="total, prev, pager, next, jumper"
              :total="alertRules.length"
              :page-size="10"
            />
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="告警历史" name="alert-history">
          <div class="alert-history-content">
            <!-- 筛选条件 -->
            <el-form :inline="true" :model="historyFilter" label-position="left" class="history-filter">
              <el-form-item label="告警类型">
                <el-select v-model="historyFilter.alertType" placeholder="请选择告警类型">
                  <el-option label="全部" value="all" />
                  <el-option label="调用失败率超限" value="failure-rate" />
                  <el-option label="响应时间超限" value="response-time" />
                  <el-option label="调用量突增" value="call-spike" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="处理状态">
                <el-select v-model="historyFilter.status" placeholder="请选择处理状态">
                  <el-option label="全部" value="all" />
                  <el-option label="未处理" value="pending" />
                  <el-option label="已处理" value="processed" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="告警时间">
                <el-date-picker
                  v-model="historyFilter.alertTime"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 300px;"
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="handleQueryHistory">查询</el-button>
                <el-button @click="handleResetHistory">重置</el-button>
                <el-button type="success" @click="handleExportHistory">导出历史</el-button>
              </el-form-item>
            </el-form>
            
            <!-- 告警历史列表 -->
            <el-table :data="alertHistory" border style="width: 100%" v-loading="historyLoading">
              <el-table-column prop="alertId" label="告警ID" width="180" />
              <el-table-column prop="ruleName" label="规则名称" width="200" />
              <el-table-column prop="alertType" label="告警类型" width="150" />
              <el-table-column prop="triggerTime" label="触发时间" width="180" />
              <el-table-column prop="triggerCondition" label="触发条件" />
              <el-table-column prop="alertContent" label="告警内容" />
              <el-table-column prop="status" label="处理状态" width="120">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'pending' ? 'warning' : 'success'">
                    {{ scope.row.status === 'pending' ? '未处理' : '已处理' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="handleViewAlertDetail(scope.row)">
                    查看详情
                  </el-button>
                  <el-button v-if="scope.row.status === 'pending'" type="success" size="small" @click="handleMarkProcessed(scope.row)">
                    标记已处理
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 分页 -->
            <el-pagination
              class="mt-20"
              background
              layout="total, prev, pager, next, jumper"
              :total="alertHistory.length"
              :page-size="10"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 新增/编辑告警规则弹窗 -->
    <el-dialog
      v-model="showAddRuleDialog"
      :title="isEditRule ? '编辑告警规则' : '新增告警规则'"
      width="600px"
    >
      <el-form ref="ruleForm" :model="ruleForm" :rules="ruleFormRules" label-width="120px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" maxlength="50" />
        </el-form-item>
        
        <el-form-item label="告警类型" prop="alertType">
          <el-select v-model="ruleForm.alertType" placeholder="请选择告警类型">
            <el-option label="调用失败率超限" value="failure-rate" />
            <el-option label="响应时间超限" value="response-time" />
            <el-option label="调用量突增" value="call-spike" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="触发条件" prop="triggerCondition">
          <el-input v-model="ruleForm.triggerCondition" type="textarea" placeholder="请输入触发条件" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
        
        <el-form-item label="告警方式" prop="alertMethod">
          <el-checkbox-group v-model="ruleForm.alertMethod">
            <el-checkbox label="sms">短信</el-checkbox>
            <el-checkbox label="email">邮箱</el-checkbox>
            <el-checkbox label="webhook">回调地址</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="接收人" prop="recipients" v-if="ruleForm.alertMethod.includes('sms') || ruleForm.alertMethod.includes('email')">
          <el-input v-model="ruleForm.recipients" placeholder="请输入接收人，多个用逗号分隔" maxlength="200" />
        </el-form-item>
        
        <el-form-item label="回调地址" prop="webhookUrl" v-if="ruleForm.alertMethod.includes('webhook')">
          <el-input v-model="ruleForm.webhookUrl" placeholder="请输入回调地址" maxlength="200" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddRuleDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRule">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 告警详情弹窗 -->
    <el-dialog
      v-model="showAlertDetailDialog"
      title="告警详情"
      width="600px"
    >
      <div v-if="selectedAlert" class="alert-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="告警ID">{{ selectedAlert.alertId }}</el-descriptions-item>
          <el-descriptions-item label="规则名称">{{ selectedAlert.ruleName }}</el-descriptions-item>
          <el-descriptions-item label="告警类型">{{ selectedAlert.alertType }}</el-descriptions-item>
          <el-descriptions-item label="触发条件">{{ selectedAlert.triggerCondition }}</el-descriptions-item>
          <el-descriptions-item label="触发时间">{{ selectedAlert.triggerTime }}</el-descriptions-item>
          <el-descriptions-item label="告警内容">{{ selectedAlert.alertContent }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">{{ selectedAlert.status === 'pending' ? '未处理' : '已处理' }}</el-descriptions-item>
          <el-descriptions-item label="处理时间" v-if="selectedAlert.processedTime">{{ selectedAlert.processedTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <el-button @click="showAlertDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 活动标签页
const activeTab = ref('alert-rules')

// 告警规则相关
const alertRules = ref([
  {
    ruleId: 'RULE_001',
    ruleName: '调用失败率超限',
    alertType: 'failure-rate',
    triggerCondition: '调用失败率>5%持续10分钟',
    alertMethod: ['sms', 'email'],
    status: '启用'
  },
  {
    ruleId: 'RULE_002',
    ruleName: '响应时间超限',
    alertType: 'response-time',
    triggerCondition: '平均响应时间>500ms持续5分钟',
    alertMethod: ['email'],
    status: '禁用'
  },
  {
    ruleId: 'RULE_003',
    ruleName: '调用量突增',
    alertType: 'call-spike',
    triggerCondition: '调用量环比增长>200%',
    alertMethod: ['sms', 'webhook'],
    status: '启用'
  }
])

// 新增/编辑规则弹窗
const showAddRuleDialog = ref(false)
const isEditRule = ref(false)
const ruleFormRef = ref(null)
const ruleForm = reactive({
  ruleName: '',
  alertType: '',
  triggerCondition: '',
  alertMethod: [],
  recipients: '',
  webhookUrl: ''
})

// 表单验证规则
const ruleFormRules = {
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { min: 5, max: 50, message: '规则名称长度在 5 到 50 个字符', trigger: 'blur' }
  ],
  alertType: [
    { required: true, message: '请选择告警类型', trigger: 'change' }
  ],
  triggerCondition: [
    { required: true, message: '请输入触发条件', trigger: 'blur' },
    { min: 10, max: 200, message: '触发条件长度在 10 到 200 个字符', trigger: 'blur' }
  ],
  alertMethod: [
    { required: true, message: '请选择告警方式', trigger: 'change' }
  ],
  recipients: [
    { required: true, message: '请输入接收人', trigger: 'blur', validator: validateRecipients }
  ],
  webhookUrl: [
    { required: true, message: '请输入回调地址', trigger: 'blur', validator: validateWebhookUrl }
  ]
}

// 自定义验证函数
const validateRecipients = (rule, value, callback) => {
  if ((ruleForm.alertMethod.includes('sms') || ruleForm.alertMethod.includes('email')) && !value) {
    callback(new Error('请输入接收人'))
  } else {
    callback()
  }
}

const validateWebhookUrl = (rule, value, callback) => {
  if (ruleForm.alertMethod.includes('webhook') && !value) {
    callback(new Error('请输入回调地址'))
  } else {
    callback()
  }
}

// 监听告警方式变化，动态更新表单验证
watch(() => ruleForm.alertMethod, () => {
  // 重置表单验证
  ruleFormRules.recipients[0].required = ruleForm.alertMethod.includes('sms') || ruleForm.alertMethod.includes('email')
  ruleFormRules.webhookUrl[0].required = ruleForm.alertMethod.includes('webhook')
}, {
  deep: true
})

// 告警历史相关
const alertHistory = ref([
  {
    alertId: 'ALERT_001',
    ruleName: '调用失败率超限',
    alertType: 'failure-rate',
    triggerCondition: '调用失败率>5%持续10分钟',
    triggerTime: '2025-01-15 14:30:00',
    alertContent: '调用失败率达到6.5%，超过阈值5%',
    status: 'pending'
  },
  {
    alertId: 'ALERT_002',
    ruleName: '响应时间超限',
    alertType: 'response-time',
    triggerCondition: '平均响应时间>500ms持续5分钟',
    triggerTime: '2025-01-15 13:20:00',
    alertContent: '平均响应时间达到650ms，超过阈值500ms',
    status: 'processed',
    processedTime: '2025-01-15 13:30:00'
  }
])

// 历史筛选条件
const historyFilter = reactive({
  alertType: 'all',
  status: 'all',
  alertTime: []
})

const historyLoading = ref(false)

// 告警详情弹窗
const showAlertDetailDialog = ref(false)
const selectedAlert = ref(null)

// 切换规则状态
const handleToggleStatus = (rule) => {
  ElMessage.success(`${rule.ruleName}已${rule.status === '启用' ? '禁用' : '启用'}`)
}

// 编辑规则
const handleEditRule = (rule) => {
  isEditRule.value = true
  ruleForm.value = JSON.parse(JSON.stringify(rule))
  showAddRuleDialog.value = true
}

// 删除规则
const handleDeleteRule = (rule) => {
  ElMessageBox.confirm(`确定要删除规则${rule.ruleName}吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 模拟删除
    const index = alertRules.value.findIndex(item => item.ruleId === rule.ruleId)
    if (index !== -1) {
      alertRules.value.splice(index, 1)
    }
    ElMessage.success('规则删除成功')
  }).catch(() => {
    // 取消删除
  })
}

// 保存规则
const handleSaveRule = () => {
  ruleFormRef.value.validate((valid) => {
    if (valid) {
      // 模拟保存
      if (isEditRule.value) {
        // 编辑规则
        const index = alertRules.value.findIndex(item => item.ruleId === ruleForm.ruleId)
        if (index !== -1) {
          alertRules.value[index] = { ...ruleForm.value }
        }
        ElMessage.success('规则编辑成功')
      } else {
        // 新增规则
        ruleForm.value.ruleId = `RULE_${Date.now().toString().slice(-6)}`
        ruleForm.value.status = '启用'
        alertRules.value.push({ ...ruleForm.value })
        ElMessage.success('规则新增成功')
      }
      showAddRuleDialog.value = false
      // 重置表单
      ruleForm.value = {
        ruleName: '',
        alertType: '',
        triggerCondition: '',
        alertMethod: [],
        recipients: '',
        webhookUrl: ''
      }
      isEditRule.value = false
    }
  })
}

// 查询告警历史
const handleQueryHistory = () => {
  historyLoading.value = true
  // 模拟查询
  setTimeout(() => {
    historyLoading.value = false
    ElMessage.success('查询成功')
  }, 500)
}

// 重置历史筛选条件
const handleResetHistory = () => {
  historyFilter.alertType = 'all'
  historyFilter.status = 'all'
  historyFilter.alertTime = []
}

// 导出历史
const handleExportHistory = () => {
  ElMessage.success('告警历史导出成功')
}

// 查看告警详情
const handleViewAlertDetail = (alert) => {
  selectedAlert.value = alert
  showAlertDetailDialog.value = true
}

// 标记已处理
const handleMarkProcessed = (alert) => {
  alert.status = 'processed'
  alert.processedTime = new Date().toLocaleString('zh-CN')
  ElMessage.success('已标记为已处理')
}
</script>

<style scoped>
.alert-config-container {
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

/* 告警规则样式 */
.alert-rules-content {
  padding: 10px 0;
}

.rules-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.rules-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

/* 告警历史样式 */
.alert-history-content {
  padding: 10px 0;
}

.history-filter {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

/* 详情样式 */
.alert-detail {
  padding: 10px 0;
}

.mt-20 {
  margin-top: 20px;
  text-align: right;
}
</style>