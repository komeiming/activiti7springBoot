<template>
  <div class="custom-permission-container">
    <h3 class="section-title">自定义权限申请</h3>
    
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>申请自定义权限</span>
        </div>
      </template>
      
      <el-form ref="customPermissionForm" :model="customPermissionForm" :rules="rules" label-width="120px">
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="customPermissionForm.permissionName" placeholder="请输入申请的权限名称" maxlength="50" />
        </el-form-item>
        
        <el-form-item label="权限描述" prop="permissionDesc">
          <el-input
            v-model="customPermissionForm.permissionDesc"
            type="textarea"
            placeholder="请详细描述您需要的权限功能"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="应用场景" prop="useCase">
          <el-input
            v-model="customPermissionForm.useCase"
            type="textarea"
            placeholder="请详细说明该权限的应用场景"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="权限范围" prop="permissionScope">
          <el-select v-model="customPermissionForm.permissionScope" placeholder="请选择权限范围" multiple>
            <el-option label="通知模块" value="notification" />
            <el-option label="工作流模块" value="workflow" />
            <el-option label="数据分析模块" value="data-analysis" />
            <el-option label="系统管理" value="system" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="预期使用频率" prop="usageFrequency">
          <el-radio-group v-model="customPermissionForm.usageFrequency">
            <el-radio label="低频（每周少于1次）" />
            <el-radio label="中频（每周1-10次）" />
            <el-radio label="高频（每周10次以上）" />
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="附加说明" prop="remark">
          <el-input
            v-model="customPermissionForm.remark"
            type="textarea"
            placeholder="请提供其他相关信息（可选）"
            :rows="3"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交申请</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 申请历史记录 -->
    <el-card shadow="hover" class="mt-20">
      <template #header>
        <div class="card-header">
          <span>申请历史记录</span>
        </div>
      </template>
      
      <el-table :data="applicationHistory" border style="width: 100%">
        <el-table-column prop="permissionName" label="权限名称" width="180" />
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column prop="status" label="审批状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approvalTime" label="审批时间" width="180" />
        <el-table-column prop="approvalRemark" label="审批备注" />
      </el-table>
      
      <el-pagination
        class="mt-20"
        background
        layout="prev, pager, next"
        :total="applicationHistory.length"
        :page-size="10"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

// 自定义权限申请表单
const customPermissionForm = reactive({
  permissionName: '',
  permissionDesc: '',
  useCase: '',
  permissionScope: [],
  usageFrequency: '',
  remark: ''
})

// 表单验证规则
const rules = {
  permissionName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 5, max: 50, message: '权限名称长度在 5 到 50 个字符', trigger: 'blur' }
  ],
  permissionDesc: [
    { required: true, message: '请描述权限功能', trigger: 'blur' },
    { min: 20, max: 200, message: '权限描述长度在 20 到 200 个字符', trigger: 'blur' }
  ],
  useCase: [
    { required: true, message: '请说明应用场景', trigger: 'blur' },
    { min: 50, max: 500, message: '应用场景描述长度在 50 到 500 个字符', trigger: 'blur' }
  ],
  permissionScope: [
    { required: true, message: '请选择权限范围', trigger: 'change' }
  ],
  usageFrequency: [
    { required: true, message: '请选择预期使用频率', trigger: 'change' }
  ]
}

// 申请历史记录
const applicationHistory = ref([
  {
    id: '1',
    permissionName: '自定义通知模板审批',
    applyTime: '2025-01-15 14:30:00',
    status: '已通过',
    approvalTime: '2025-01-16 10:00:00',
    approvalRemark: '该权限申请符合业务需求，已通过审批'
  },
  {
    id: '2',
    permissionName: '高级数据导出',
    applyTime: '2025-01-10 09:15:00',
    status: '已拒绝',
    approvalTime: '2025-01-11 16:30:00',
    approvalRemark: '该权限涉及敏感数据，暂不开放'
  },
  {
    id: '3',
    permissionName: '批量流程实例操作',
    applyTime: '2025-01-05 11:20:00',
    status: '审批中',
    approvalTime: '',
    approvalRemark: ''
  }
])

// 根据审批状态获取标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case '已通过':
      return 'success'
    case '已拒绝':
      return 'danger'
    case '审批中':
      return 'warning'
    default:
      return 'info'
  }
}

// 提交申请
const handleSubmit = () => {
  // 模拟提交请求
  ElMessage.success('自定义权限申请已提交，我们将在3个工作日内处理')
  handleReset()
}

// 重置表单
const handleReset = () => {
  customPermissionForm.permissionName = ''
  customPermissionForm.permissionDesc = ''
  customPermissionForm.useCase = ''
  customPermissionForm.permissionScope = []
  customPermissionForm.usageFrequency = ''
  customPermissionForm.remark = ''
}
</script>

<style scoped>
.custom-permission-container {
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

.mt-20 {
  margin-top: 20px;
}
</style>