<template>
  <div class="notification-send-container">
    <h3 class="section-title">通知发送</h3>
    
    <el-card shadow="hover">
      <!-- 发送类型切换 -->
      <el-tabs v-model="activeSendType" class="send-type-tabs">
        <el-tab-pane label="单个发送" name="single"></el-tab-pane>
        <el-tab-pane label="批量发送" name="batch"></el-tab-pane>
        <el-tab-pane label="定时发送" name="scheduled"></el-tab-pane>
      </el-tabs>
      
      <!-- 发送表单 -->
      <el-form ref="sendFormRef" :model="sendForm" :rules="formRules" label-width="120px" class="send-form">
        <!-- 模板选择 -->
        <el-form-item label="模板选择" prop="templateId">
          <el-select
            v-model="sendForm.templateId"
            placeholder="请选择通知模板"
            class="template-select"
            @change="handleTemplateChange"
            filterable
            clearable
          >
            <el-option
              v-for="template in availableTemplates"
              :key="template.id"
              :label="template.name"
              :value="template.id"
            >
              <div class="template-option">
                <div class="template-name">{{ template.name }}</div>
                <div class="template-type">{{ getTemplateTypeName(template.type) }}</div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <!-- 接收人（所有发送类型都需要） -->
        <el-form-item label="接收人" prop="recipient">
          <el-input
            v-model="sendForm.recipient"
            placeholder="请输入接收人（手机号/邮箱/设备ID，支持逗号分隔）"
            class="recipient-input"
            clearable
            @input="(value) => { sendForm.recipient = value; console.log('接收人输入:', value) }"
          />
        </el-form-item>
        
        <!-- 动态变量输入（所有发送类型都需要） -->
        <template v-if="selectedTemplate">
          <el-divider>变量替换</el-divider>
          <template v-if="variables.length > 0">
            <el-form-item
              v-for="(variable, index) in variables"
              :key="index + '_' + variable"
              :label="variable"
              :required="true"
            >
              <el-input
                v-model="sendForm.variables[variable]"
                :placeholder="`请输入${variable}`"
                :disabled="false"
                :readonly="false"
              />
            </el-form-item>
          </template>
          <template v-else>
            <div class="no-variables">该模板没有定义变量</div>
          </template>
        </template>
        
        <!-- 单个发送表单 -->
        <template v-if="activeSendType === 'single'">
          <el-form-item>
            <el-switch v-model="sendForm.needCallback" /> 是否需要回调
          </el-form-item>
        </template>
        
        <!-- 批量发送表单 -->
        <template v-else-if="activeSendType === 'batch'">
          <el-form-item label="变量替换模式">
            <el-radio-group v-model="batchVariableMode" @change="handleVariableModeChange">
              <el-radio label="file">文件导入变量值</el-radio>
              <el-radio label="default">统一默认值</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <!-- 上传Excel按钮 -->
          <el-form-item>
            <el-button type="primary" @click="handleUploadRecipients">
              <el-icon><Upload /></el-icon> 上传Excel
            </el-button>
            <el-button type="warning" @click="handleDownloadTemplate">
              <el-icon><Download /></el-icon> 下载模板
            </el-button>
            <el-button type="danger" @click="handleClearRecipients">
              <el-icon><Delete /></el-icon> 清空
            </el-button>
          </el-form-item>
          
          <el-form-item>
            <el-switch v-model="sendForm.needCallback" /> 是否需要回调
          </el-form-item>
        </template>
        
        <!-- 定时发送表单 -->
        <template v-else-if="activeSendType === 'scheduled'">
          <!-- 发送时间选择 -->
          <el-form-item label="发送时间" prop="sendTime">
            <el-date-picker
              v-model="sendForm.sendTime"
              type="datetime"
              placeholder="选择发送时间"
              style="width: 100%"
              :disabled-date="disabledDate"
            />
          </el-form-item>
          
          <el-form-item label="重复发送">
            <el-switch v-model="sendForm.needRepeat" @change="handleRepeatChange" />
          </el-form-item>
          
          <template v-if="sendForm.needRepeat">
            <el-form-item label="重复频率">
              <el-select v-model="sendForm.repeatFrequency">
                <el-option label="每天" value="daily" />
                <el-option label="每周" value="weekly" />
                <el-option label="每月" value="monthly" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="重复次数">
              <el-input-number v-model="sendForm.repeatCount" :min="1" :max="30" />
            </el-form-item>
          </template>
          
          <el-form-item>
            <el-switch v-model="sendForm.needCallback" /> 是否需要回调
          </el-form-item>
        </template>
        
        <el-divider />
        
        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSend">
            <el-icon><CircleCheck /></el-icon> 发送
          </el-button>
          <el-button @click="handlePreview">
            <el-icon><View /></el-icon> 预览
          </el-button>
          <el-button @click="handleClear">清空</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 预览弹窗 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="通知预览"
      width="500px"
    >
      <div class="preview-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="发送类型">
            {{ getSendTypeName(activeSendType) }}
          </el-descriptions-item>
          <el-descriptions-item label="模板名称">
            {{ selectedTemplate ? selectedTemplate.name : '' }}
          </el-descriptions-item>
          <el-descriptions-item label="接收人">
            {{ sendForm.recipient || '未指定' }}
          </el-descriptions-item>
          <el-descriptions-item label="发送内容">
            <div class="content-preview">{{ generatePreviewContent() }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleConfirmSendFromPreview">确认发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Upload, Download, Delete, CircleCheck, View
} from '@element-plus/icons-vue'
import axios from '@/utils/axiosConfig'

// 发送类型
const activeSendType = ref('single')

// 发送表单
const sendForm = reactive({
  templateId: '',
  recipient: '',
  variables: {},
  needCallback: false,
  sendTime: '',
  needRepeat: false,
  repeatFrequency: 'daily',
  repeatCount: 1
})

// 表单验证规则
const formRules = {
  templateId: [
    { required: true, message: '请选择通知模板', trigger: 'change' }
  ],
  recipient: [
    { required: true, message: '请输入接收人', trigger: 'blur' }
  ]
}

// 可用模板
const availableTemplates = ref([])
const loading = ref(false)

// 选中的模板
const selectedTemplate = ref(null)
const variables = ref([])

// 提取模板中的变量
const extractVariables = (content) => {
  const variableRegex = /\$\{([^}]+)\}/g;
  const matches = content.match(variableRegex);
  const variablesSet = new Set();

  if (matches) {
    matches.forEach(match => {
      const varName = match.substring(2, match.length - 1).trim();
      if (varName) {
        variablesSet.add(varName);
      }
    });
  }

  variables.value = Array.from(variablesSet);
  // 初始化变量对象
  sendForm.variables = {};
  variables.value.forEach(varName => {
    sendForm.variables[varName] = '';
  });
}

// 批量发送相关
const batchVariableMode = ref('file') // 使用字符串类型存储选中的变量替换模式
const recipientCount = ref(0)

// 预览弹窗
const previewDialogVisible = ref(false)

// 从后端获取模板列表
const getTemplates = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/v1/notification/template')
    console.log('模板列表响应:', response)
    // 检查response数据结构，后端返回的数据结构是 {data: [...], total: ..., page: ..., size: ...}
    availableTemplates.value = Array.isArray(response.data) ? response.data : (response.data.data || [])
    ElMessage.success(`模板列表加载成功，共${availableTemplates.value.length}个模板`)
    console.log('可用模板:', availableTemplates.value)
  } catch (error) {
    console.error('获取模板列表失败:', error)
    ElMessage.error('获取模板列表失败: ' + (error.message || '请稍后重试'))
    availableTemplates.value = []
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取模板列表
onMounted(() => {
  getTemplates()
})

// 模板类型映射
const getTemplateTypeName = (type) => {
  const typeMap = {
    sms: '短信',
    email: '邮件',
    app: 'APP推送',
    internal: '站内信'
  }
  return typeMap[type] || type
}

// 发送类型映射
const getSendTypeName = (type) => {
  const typeMap = {
    single: '单个发送',
    batch: '批量发送',
    scheduled: '定时发送'
  }
  return typeMap[type] || type
}

// 模板变化处理
const handleTemplateChange = async () => {
  console.log('模板变化，新模板ID:', sendForm.templateId)
  
  if (sendForm.templateId) {
    try {
      // 从模板列表中查找模板，不需要额外API调用
      const template = availableTemplates.value.find(t => t.id === sendForm.templateId)
      if (template) {
        selectedTemplate.value = template
        console.log('选中模板:', template)
        extractVariables(template.content)
        
        // 重置批量变量模式为默认值
        batchVariableMode.value = 'file'
      }
    } catch (error) {
      console.error('获取模板详情失败:', error)
      ElMessage.error('获取模板详情失败: ' + (error.message || '请稍后重试'))
      selectedTemplate.value = null
      variables.value = []
      sendForm.variables = {}
    }
  } else {
    selectedTemplate.value = null
    variables.value = []
    sendForm.variables = {}
    // 重置批量变量模式为默认值
    batchVariableMode.value = 'file'
  }
}

// 上传接收人
const handleUploadRecipients = () => {
  ElMessage.info('上传接收人功能开发中')
}

// 下载模板
const handleDownloadTemplate = () => {
  ElMessage.info('下载模板功能开发中')
}

// 清空接收人
const handleClearRecipients = () => {
  recipientCount.value = 0
  ElMessage.success('已清空接收人')
}

// 变量模式变化
const handleVariableModeChange = () => {
  // 处理变量模式变化
}

// 发送
const sendFormRef = ref(null)

const handleSend = async () => {
  try {
    // 验证表单
    if (sendFormRef.value) {
      await sendFormRef.value.validate(async (valid) => {
        if (valid) {
          if (!selectedTemplate.value) {
            ElMessage.warning('请选择通知模板')
            return
          }
          
          // 构建请求数据
          const requestData = {
            templateId: selectedTemplate.value.id,
            receiver: sendForm.recipient,
            variables: sendForm.variables,
            needCallback: sendForm.needCallback
          }
          
          // 根据发送类型添加额外参数
          if (activeSendType.value === 'scheduled') {
            requestData.sendTime = sendForm.sendTime
            requestData.needRepeat = sendForm.needRepeat
            requestData.repeatFrequency = sendForm.repeatFrequency
            requestData.repeatCount = sendForm.repeatCount
          }
          
          // 根据发送类型调用对应的API端点
          let apiUrl = '/api/v1/notification/send/single'
          if (activeSendType.value === 'batch') {
            apiUrl = '/api/v1/notification/send/batch'
          } else if (activeSendType.value === 'scheduled') {
            apiUrl = '/api/v1/notification/send/scheduled'
          }
          // 调用后端API发送通知
          const response = await axios.post(apiUrl, requestData)
          ElMessage.success('通知发送成功: ' + (response.message || ''))
          
          // 清空表单
          handleClear()
        }
      })
    }
  } catch (error) {
    console.error('发送通知失败:', error)
    ElMessage.error('发送通知失败: ' + (error.message || '请稍后重试'))
  }
}

// 预览
const handlePreview = () => {
  if (!selectedTemplate.value) {
    ElMessage.warning('请选择通知模板')
    return
  }
  previewDialogVisible.value = true
}

// 清空
const handleClear = () => {
  sendForm.templateId = ''
  sendForm.recipient = ''
  sendForm.variables = {}
  sendForm.needCallback = false
  sendForm.sendTime = ''
  sendForm.needRepeat = false
  sendForm.repeatFrequency = 'daily'
  sendForm.repeatCount = 1
  ElMessage.success('已清空表单')
}

// 生成预览内容
const generatePreviewContent = () => {
  if (!selectedTemplate.value) return ''
  
  let content = selectedTemplate.value.content
  // 替换变量
  Object.keys(sendForm.variables).forEach(key => {
    const value = sendForm.variables[key]
    content = content.replace(new RegExp(`\\$\\{${key}\\}\\`, 'g'), value || `\$\{${key}\}`)
  })
  return content
}

// 确认发送
const handleConfirmSendFromPreview = async () => {
  await handleSend()
  previewDialogVisible.value = false
}

// 禁用日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 重复发送变化
const handleRepeatChange = () => {
  if (!sendForm.needRepeat) {
    sendForm.repeatCount = 1
  }
}
</script>

<style scoped>
.notification-send-container {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.send-type-tabs {
  margin-bottom: 20px;
}

.send-form {
  padding: 10px 0;
}

.filter-input {
  width: 200px;
}

.filter-select {
  width: 150px;
}

.template-select {
  width: 250px;
}

.recipient-input {
  width: 300px;
}

.template-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.template-name {
  font-weight: 600;
}

.template-type {
  font-size: 12px;
  color: #606266;
}

.preview-content {
  padding: 10px 0;
}

.content-preview {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
}
.no-variables {
  color: #909399;
  text-align: center;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

/* 确保表单元素可以正常交互 */
.send-form :deep(.el-input__inner),
.send-form :deep(.el-select__input),
.send-form :deep(.el-switch__core) {
  pointer-events: auto !important;
  cursor: pointer !important;
}

.send-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.send-form :deep(.el-select-dropdown) {
  z-index: 1000;
}

.send-form :deep(.el-dialog) {
  z-index: 2000;
}
</style>