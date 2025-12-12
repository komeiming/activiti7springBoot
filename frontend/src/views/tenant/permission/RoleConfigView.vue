<template>
  <div class="role-config-container">
    <h3 class="section-title">角色配置</h3>
    
    <!-- 当前角色信息 -->
    <el-card shadow="hover" class="role-card">
      <template #header>
        <div class="card-header">
          <span>当前角色信息</span>
        </div>
      </template>
      
      <div class="role-info">
        <div class="role-basic">
          <div class="role-name">
            <h4>{{ currentRole.name }}</h4>
            <el-tag :type="currentRole.level === 'admin' ? 'success' : 'info'">
              {{ currentRole.level === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
          <p class="role-desc">{{ currentRole.description }}</p>
        </div>
        
        <div class="role-permissions">
          <h5>权限列表</h5>
          <el-checkbox-group v-model="currentRolePermissions" :disabled="true">
            <el-checkbox v-for="permission in allPermissions" :key="permission.id" :label="permission.name">
              {{ permission.name }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
    </el-card>
    
    <!-- 权限升级申请 -->
    <el-card shadow="hover" class="role-card mt-20">
      <template #header>
        <div class="card-header">
          <span>权限升级申请</span>
        </div>
      </template>
      
      <div class="permission-upgrade">
        <p class="upgrade-desc">
          如果您需要更多权限，可以提交权限升级申请，我们的管理员将在24小时内处理您的申请。
        </p>
        
        <el-button type="primary" @click="showApplyDialog = true">
          申请权限升级
        </el-button>
      </div>
    </el-card>
    
    <!-- 权限升级申请弹窗 -->
    <el-dialog
      v-model="showApplyDialog"
      title="权限升级申请"
      width="600px"
    >
      <el-form ref="applyForm" :model="applyForm" :rules="applyRules" label-width="120px">
        <el-form-item label="申请理由" prop="reason">
          <el-input
            v-model="applyForm.reason"
            type="textarea"
            placeholder="请详细说明您需要升级权限的原因"
            :rows="4"
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>
        
        <el-form-item label="所需权限" prop="requiredPermissions">
          <el-checkbox-group v-model="applyForm.requiredPermissions">
            <el-checkbox v-for="permission in availablePermissions" :key="permission.id" :label="permission.id">
              {{ permission.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="附加说明" prop="remark">
          <el-input
            v-model="applyForm.remark"
            type="textarea"
            placeholder="请提供其他相关信息（可选）"
            :rows="3"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'

// 当前角色信息
const currentRole = ref({
  id: 'role-001',
  name: '通知模块管理员',
  level: 'admin',
  description: '负责通知模块的所有管理操作',
  permissions: ['template_manage', 'notification_send', 'send_logs']
})

// 所有权限列表
const allPermissions = ref([
  { id: 'template_manage', name: '模板管理' },
  { id: 'notification_send', name: '通知发送' },
  { id: 'send_logs', name: '发送日志查询' },
  { id: 'workflow_template', name: '流程模板管理' },
  { id: 'workflow_instance', name: '流程实例管理' },
  { id: 'execution_logs', name: '执行日志查询' }
])

// 当前角色拥有的权限名称
const currentRolePermissions = computed(() => {
  return currentRole.value.permissions.map(permId => {
    const permission = allPermissions.value.find(p => p.id === permId)
    return permission ? permission.name : permId
  })
})

// 可用权限（当前角色未拥有的权限）
const availablePermissions = computed(() => {
  return allPermissions.value.filter(perm => !currentRole.value.permissions.includes(perm.id))
})

// 权限升级申请相关
const showApplyDialog = ref(false)
const applyForm = reactive({
  reason: '',
  requiredPermissions: [],
  remark: ''
})

// 表单验证规则
const applyRules = {
  reason: [
    { required: true, message: '请输入申请理由', trigger: 'blur' },
    { min: 50, max: 500, message: '申请理由长度在 50 到 500 个字符', trigger: 'blur' }
  ],
  requiredPermissions: [
    { required: true, message: '请选择所需权限', trigger: 'change' }
  ]
}

// 提交权限升级申请
const submitApply = () => {
  // 模拟提交请求
  ElMessage.success('权限升级申请已提交，我们将在24小时内处理')
  showApplyDialog.value = false
  
  // 重置表单
  applyForm.reason = ''
  applyForm.requiredPermissions = []
  applyForm.remark = ''
}
</script>

<style scoped>
.role-config-container {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.role-card {
  margin-bottom: 0;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.role-info {
  padding: 10px 0;
}

.role-name {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.role-name h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.role-desc {
  color: #606266;
  margin: 0 0 20px 0;
}

.role-permissions h5 {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: 600;
}

.permission-upgrade {
  padding: 10px 0;
}

.upgrade-desc {
  color: #606266;
  margin: 0 0 20px 0;
  line-height: 1.5;
}
</style>