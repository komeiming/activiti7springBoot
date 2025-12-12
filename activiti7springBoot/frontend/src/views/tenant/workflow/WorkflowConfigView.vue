<template>
  <div class="workflow-config-container">
    <h3 class="section-title">工作流配置</h3>
    
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>工作流基础配置</span>
        </div>
      </template>
      
      <el-form ref="configForm" :model="workflowConfig" label-width="120px">
        <el-form-item label="回调地址" prop="webhookUrl">
          <el-input
            v-model="workflowConfig.webhookUrl"
            placeholder="请输入工作流执行结果回调地址"
            maxlength="200"
          />
          <div class="form-help">工作流执行完成后，将通过此地址回调执行结果</div>
        </el-form-item>
        
        <el-form-item label="超时配置(秒)" prop="timeoutSeconds">
          <el-input-number
            v-model="workflowConfig.timeoutSeconds"
            :min="1"
            :max="3600"
            :step="1"
            placeholder="请输入超时时间"
          />
          <div class="form-help">工作流节点执行的超时时间，超过此时间将触发超时处理策略</div>
        </el-form-item>
        
        <el-form-item label="重试次数" prop="retryCount">
          <el-input-number
            v-model="workflowConfig.retryCount"
            :min="0"
            :max="10"
            :step="1"
            placeholder="请输入重试次数"
          />
          <div class="form-help">节点执行失败后的重试次数</div>
        </el-form-item>
        
        <el-form-item label="重试间隔(秒)" prop="retryInterval">
          <el-input-number
            v-model="workflowConfig.retryInterval"
            :min="1"
            :max="300"
            :step="1"
            placeholder="请输入重试间隔"
          />
          <div class="form-help">节点执行失败后的重试间隔时间</div>
        </el-form-item>
        
        <el-form-item label="并发限制" prop="concurrencyLimit">
          <el-input-number
            v-model="workflowConfig.concurrencyLimit"
            :min="1"
            :max="100"
            :step="1"
            placeholder="请输入并发限制"
          />
          <div class="form-help">同时执行的工作流实例数量限制</div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
          <el-button @click="handleResetConfig">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

// 工作流配置
const workflowConfig = reactive({
  webhookUrl: '',
  timeoutSeconds: 30,
  retryCount: 3,
  retryInterval: 5,
  concurrencyLimit: 10
})

// 表单引用
const configForm = ref(null)

// 保存配置
const handleSaveConfig = () => {
  // 模拟保存请求
  ElMessage.success('工作流配置保存成功')
}

// 重置配置
const handleResetConfig = () => {
  workflowConfig.webhookUrl = ''
  workflowConfig.timeoutSeconds = 30
  workflowConfig.retryCount = 3
  workflowConfig.retryInterval = 5
  workflowConfig.concurrencyLimit = 10
}
</script>

<style scoped>
.workflow-config-container {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.card-header {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.form-help {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>