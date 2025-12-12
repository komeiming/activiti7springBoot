<template>
  <div class="generic-process-apply fade-in">
    <h2 class="page-title">流程申请</h2>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ pageTitle }}</span>
        </div>
      </template>

      <!-- 流程类型选择 -->
      <div class="process-selection-section" v-if="!selectedProcessDefinition">
          <el-skeleton v-if="loading" :rows="6" animated></el-skeleton>
        <h3>请选择要申请的流程</h3>
        <el-row :gutter="20" class="process-cards-row">
          <el-col :xs="24" :sm="12" :md="8" :lg="6"
            v-for="processDef in activeProcessDefinitions"
            :key="processDef.id"
          >
            <el-card
              class="process-card"
              @click="selectProcessDefinition(processDef)"
              shadow="hover"
            >
              <div class="process-card-content">
                <el-icon :size="48" class="process-icon"><Document /></el-icon>
                <h4>{{ processDef.name }}</h4>
                <p class="process-version">版本: {{ processDef.version }}</p>
                <p class="process-key">{{ processDef.key }}</p>
                <el-button type="primary" size="small" style="margin-top: 10px;">
                  立即申请
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <div v-if="!loading && activeProcessDefinitions.length === 0" class="no-processes">
          <el-empty description="暂无可用流程，请先部署流程定义"></el-empty>
          <el-button type="primary" @click="navigateToDeploy">去部署流程</el-button>
        </div>
      </div>

      <!-- 流程申请表单 -->
      <div class="process-form-section slide-in" v-else>
        <el-breadcrumb separator="/" style="margin-bottom: 20px;">
          <el-breadcrumb-item @click="clearSelection">流程选择</el-breadcrumb-item>
          <el-breadcrumb-item>{{ selectedProcessDefinition.name }}申请</el-breadcrumb-item>
        </el-breadcrumb>

        <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px" :disabled="loading">
          <!-- 通用字段 -->
          <el-form-item label="申请人" prop="applicantName">
            <el-input v-model="formData.applicantName" disabled></el-input>
          </el-form-item>
          <el-form-item label="所属部门" prop="department">
            <el-input v-model="formData.department" disabled></el-input>
          </el-form-item>
          <el-form-item label="申请日期" prop="applyDate">
            <el-date-picker
              v-model="formData.applyDate"
              type="datetime"
              placeholder="选择申请日期"
              style="width: 100%"
              disabled
            ></el-date-picker>
          </el-form-item>
          
          <!-- 动态表单字段 - 基于流程定义 -->
          <template v-for="field in dynamicFormFields" :key="field.key">
            <el-form-item :label="field.label" :prop="field.key" :rules="getFieldRules(field)">
              <template v-if="field.type === 'input'">
                <el-input
                  v-model="formData[field.key]"
                  :placeholder="`请输入${field.label}`"
                  :type="field.inputType || 'text'"
                  :rows="field.rows || 1"
                  :maxlength="field.maxlength"
                  :show-word-limit="field.maxlength > 0"
                ></el-input>
              </template>
              
              <template v-else-if="field.type === 'select'">
                <el-select
                  v-model="formData[field.key]"
                  :placeholder="`请选择${field.label}`"
                  style="width: 100%"
                >
                  <el-option
                    v-for="option in field.options"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  ></el-option>
                </el-select>
              </template>
              
              <template v-else-if="field.type === 'date'">
                <el-date-picker
                  v-model="formData[field.key]"
                  :type="field.dateType || 'date'"
                  :placeholder="`选择${field.label}`"
                  style="width: 100%"
                ></el-date-picker>
              </template>
              
              <template v-else-if="field.type === 'number'">
                <el-input-number
                  v-model="formData[field.key]"
                  :min="field.min || 0"
                  :max="field.max"
                  :step="field.step || 1"
                  :placeholder="`请输入${field.label}`"
                ></el-input-number>
              </template>
              
              <template v-else-if="field.type === 'textarea'">
                <el-input
                  v-model="formData[field.key]"
                  type="textarea"
                  :rows="field.rows || 4"
                  :placeholder="`请输入${field.label}`"
                  :maxlength="field.maxlength"
                  :show-word-limit="field.maxlength > 0"
                ></el-input>
              </template>
              
              <template v-else-if="field.type === 'userSelect'">
                <el-select
                  v-model="formData[field.key]"
                  :placeholder="`请选择${field.label}`"
                  style="width: 100%"
                >
                  <el-option
                    v-for="user in getUserOptions(field.userType)"
                    :key="user.username"
                    :label="user.fullName || user.username"
                    :value="user.username"
                  ></el-option>
                </el-select>
              </template>
            </el-form-item>
          </template>
          
          <!-- 流程变量添加区域 -->
          <el-form-item label="自定义变量" v-if="showCustomVariables">
            <el-button type="text" @click="addCustomVariable">添加变量</el-button>
            <div
              v-for="(variable, index) in customVariables"
              :key="index"
              class="custom-variable-item"
            >
              <el-input
                v-model="variable.name"
                placeholder="变量名"
                style="width: 20%; margin-right: 10px"
              ></el-input>
              <el-input
                v-model="variable.value"
                placeholder="变量值"
                style="width: 20%; margin-right: 10px"
              ></el-input>
              <el-select
                v-model="variable.type"
                placeholder="类型"
                style="width: 15%; margin-right: 10px"
              >
                <el-option label="字符串" value="string"></el-option>
                <el-option label="数字" value="integer"></el-option>
                <el-option label="布尔值" value="boolean"></el-option>
              </el-select>
              <el-button
                type="danger"
                icon="el-icon-delete"
                circle
                @click="removeCustomVariable(index)"
              ></el-button>
            </div>
          </el-form-item>
        </el-form>

        <div class="form-actions">
          <el-button @click="clearSelection" :disabled="loading">返回</el-button>
          <el-button 
            type="primary" 
            @click="submitForm"
            :loading="loading"
          >
            <el-icon v-if="loading"><Loading /></el-icon>
            <span v-else>提交申请</span>
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Loading } from '@element-plus/icons-vue'
import ProcessDefinitionService from '../services/ProcessDefinitionService'
import UserService from '../services/UserService'

export default {
  name: 'GenericProcessApply',
  components: {
    Document
  },
  setup() {
    const router = useRouter()
    
    // 响应式数据
    const loading = ref(true)
    const formRef = ref(null)
    const selectedProcessDefinition = ref(null)
    const processDefinitions = ref([])
    const allUsers = ref([])
    const managers = ref([])
    const currentUser = ref({})
    const showCustomVariables = ref(false)
    
    // 页面标题
    const pageTitle = computed(() => {
      return selectedProcessDefinition.value ? `${selectedProcessDefinition.value.name}申请` : '流程申请'
    })
    
    // 激活的流程定义（未挂起的）
    const activeProcessDefinitions = computed(() => {
      return processDefinitions.value.filter(process => !process.suspended)
    })
    
    // 表单数据
    const formData = reactive({
      // 通用字段
      applicantName: '',
      department: '',
      applyDate: new Date(),
      
      // 动态字段将根据流程定义添加
    })
    
    // 自定义变量
    const customVariables = ref([])
    
    // 根据流程定义的表单字段
    const dynamicFormFields = computed(() => {
      if (!selectedProcessDefinition.value) return []
      
      const processKey = selectedProcessDefinition.value.key
      const fields = []
      
      // 根据流程类型返回不同的字段配置
      switch (processKey) {
        case 'leave-process':
          return [
            {
              key: 'leaveType',
              label: '请假类型',
              type: 'select',
              required: true,
              options: [
                { label: '年假', value: 'annual' },
                { label: '病假', value: 'sick' },
                { label: '事假', value: 'personal' },
                { label: '婚假', value: 'marriage' },
                { label: '产假', value: 'maternity' },
                { label: '其他', value: 'other' }
              ]
            },
            {
              key: 'startTime',
              label: '开始时间',
              type: 'date',
              dateType: 'datetime',
              required: true
            },
            {
              key: 'endTime',
              label: '结束时间',
              type: 'date',
              dateType: 'datetime',
              required: true
            },
            {
              key: 'days',
              label: '请假天数',
              type: 'number',
              min: 0.5,
              step: 0.5,
              required: true
            },
            {
              key: 'reason',
              label: '请假原因',
              type: 'textarea',
              rows: 4,
              maxlength: 500,
              required: true
            },
            {
              key: 'approver',
              label: '审批人',
              type: 'userSelect',
              userType: 'manager',
              required: true
            }
          ]
          
        case 'onboarding-process':
          return [
            {
              key: 'employeeName',
              label: '新员工姓名',
              type: 'input',
              required: true,
              maxlength: 50
            },
            {
              key: 'position',
              label: '职位',
              type: 'input',
              required: true,
              maxlength: 100
            },
            {
              key: 'hireDate',
              label: '入职日期',
              type: 'date',
              required: true
            },
            {
              key: 'contact',
              label: '联系方式',
              type: 'input',
              required: true,
              maxlength: 20
            },
            {
              key: 'deptManager',
              label: '部门经理',
              type: 'userSelect',
              userType: 'manager',
              required: true
            },
            {
              key: 'itManager',
              label: 'IT负责人',
              type: 'userSelect',
              userType: 'it',
              required: true
            },
            {
              key: 'reason',
              label: '申请原因',
              type: 'textarea',
              rows: 4,
              maxlength: 500,
              required: true
            }
          ]
          
        case 'general-approval':
          return [
            {
              key: 'title',
              label: '申请标题',
              type: 'input',
              required: true,
              maxlength: 100
            },
            {
              key: 'content',
              label: '申请内容',
              type: 'textarea',
              rows: 6,
              maxlength: 1000,
              required: true
            },
            {
              key: 'priority',
              label: '优先级',
              type: 'select',
              required: true,
              options: [
                { label: '普通', value: 'normal' },
                { label: '紧急', value: 'urgent' },
                { label: '非常紧急', value: 'very_urgent' }
              ],
              defaultValue: 'normal'
            },
            {
              key: 'approver',
              label: '审批人',
              type: 'userSelect',
              userType: 'all',
              required: true
            }
          ]
          
        default:
          // 默认字段，适用于任何流程
          return [
            {
              key: 'title',
              label: '申请标题',
              type: 'input',
              required: true,
              maxlength: 100
            },
            {
              key: 'content',
              label: '申请内容',
              type: 'textarea',
              rows: 4,
              maxlength: 500,
              required: true
            },
            {
              key: 'approver',
              label: '审批人',
              type: 'userSelect',
              userType: 'manager',
              required: true
            }
          ]
      }
    })
    
    // 获取字段验证规则
    const getFieldRules = (field) => {
      const rules = []
      if (field.required) {
        rules.push({
          required: true,
          message: `请${field.type === 'select' || field.type === 'userSelect' ? '选择' : '输入'}${field.label}`,
          trigger: field.type === 'select' || field.type === 'userSelect' || field.type === 'date' ? 'change' : 'blur'
        })
      }
      return rules
    }
    
    // 表单验证规则
    const formRules = computed(() => {
      const rules = {}
      dynamicFormFields.value.forEach(field => {
        rules[field.key] = getFieldRules(field)
      })
      return rules
    })
    
    // 加载当前用户信息
    const loadCurrentUser = () => {
      try {
      const userInfoStr = localStorage.getItem('user')
      if (userInfoStr) {
        currentUser.value = JSON.parse(userInfoStr)
        formData.applicantName = currentUser.value.fullName || currentUser.value.username
        formData.department = currentUser.value.department || ''
      }
    } catch (error) {
      console.error('加载用户信息错误:', error)
    }
    }
    
    // 加载用户列表
    const loadUsers = async () => {
      try {
        const response = await UserService.getUsers()
        
        // 安全处理响应数据
        if (Array.isArray(response)) {
          // 直接返回数组格式
          allUsers.value = response
        } else if (typeof response === 'object' && response) {
          // 响应是对象，检查是否有data字段
          if (Array.isArray(response.data)) {
            // response.data是数组
            allUsers.value = response.data
          } else if (response.data && typeof response.data === 'object' && response.data.data) {
            // response.data是包含data字段的对象
            allUsers.value = response.data.data || []
          } else {
            // 其他情况，使用空数组
            allUsers.value = []
          }
        } else {
          // 响应不是数组也不是对象，使用空数组
          allUsers.value = []
        }
        
        // 筛选管理人员和IT人员 - 增强安全检查
        managers.value = allUsers.value.filter(user => 
          user && user.roles && Array.isArray(user.roles) && user.roles.some(role => 
            role && (role.roleCode === 'ROLE_MANAGER' || role.roleCode === 'ROLE_ADMIN')
          )
        )
      } catch (error) {
        console.error('加载用户列表错误:', error)
      }
    }
    
    // 获取用户选项
    const getUserOptions = (userType) => {
      switch (userType) {
        case 'manager':
          return managers.value
        case 'it':
          return allUsers.value.filter(user => 
            user.roles && user.roles.some(role => 
              role.roleCode === 'ROLE_IT' || role.roleName?.includes('IT')
            )
          )
        default:
          return allUsers.value
      }
    }
    
    // 加载流程定义列表
    const loadProcessDefinitions = async () => {
      loading.value = true
      try {
        const response = await ProcessDefinitionService.getProcessDefinitions(
          1, // page
          100, // size (large enough to get all process definitions)
          { latest: true } // filters
        )
        
        // 安全处理不同格式的响应
        if (response && response.data) {
          // 格式1: { code: 200, data: { data: [], total: 0 } }
          if (response.data.code === 200) {
            processDefinitions.value = response.data.data.data || []
          } 
          // 格式2: { data: [], total: 0 }
          else if (response.data.data && Array.isArray(response.data.data)) {
            processDefinitions.value = response.data.data || []
          } 
          // 格式3: { data: [] }
          else if (Array.isArray(response.data)) {
            processDefinitions.value = response.data || []
          }
          
          if (processDefinitions.value.length === 0) {
            ElMessage.info('当前没有可用的流程定义，请先部署流程')
          }
        } 
        // 格式4: 直接返回数组
        else if (response && Array.isArray(response)) {
          processDefinitions.value = response || []
          if (processDefinitions.value.length === 0) {
            ElMessage.info('当前没有可用的流程定义，请先部署流程')
          }
        } else {
          ElMessage.error('加载流程定义失败: 无效的响应数据')
        }
      } catch (error) {
        console.error('加载流程定义错误:', error)
        ElMessage.error('加载流程定义失败: ' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }
    
    // 选择流程定义
    const selectProcessDefinition = (processDefinition) => {
      selectedProcessDefinition.value = processDefinition
      resetForm()
      
      // 设置默认值
      dynamicFormFields.value.forEach(field => {
        if (field.defaultValue !== undefined) {
          formData[field.key] = field.defaultValue
        }
      })
    }
    
    // 清除选择
    const clearSelection = () => {
      selectedProcessDefinition.value = null
      resetForm()
    }
    
    // 重置表单
    const resetForm = () => {
      ElMessageBox.confirm(
        '确定要重置表单吗？当前填写的内容将丢失。',
        '重置确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }
      ).then(() => {
        if (formRef.value && formRef.value.resetFields) {
          formRef.value.resetFields()
        }
        
        // 重置通用字段
        formData.applicantName = currentUser.value.fullName || currentUser.value.username
        formData.department = currentUser.value.department || ''
        formData.applyDate = new Date()
        
        // 清空动态字段
        dynamicFormFields.value.forEach(field => {
          if (field.defaultValue !== undefined) {
            formData[field.key] = field.defaultValue
          } else {
            formData[field.key] = ''
          }
        })
        
        // 清空自定义变量
        customVariables.value = []
        
        ElMessage.success('表单已重置')
      }).catch(() => {
        // 用户取消重置
      })
    }
    
    // 添加自定义变量
    const addCustomVariable = () => {
      customVariables.value.push({
        name: '',
        value: '',
        type: 'string'
      })
    }
    
    // 移除自定义变量
    const removeCustomVariable = (index) => {
      customVariables.value.splice(index, 1)
    }
    
    // 提交表单
    const submitForm = async () => {
      if (!selectedProcessDefinition.value) {
        ElMessage.warning('请先选择流程类型')
        return
      }
      
      try {
        await formRef.value.validate()
        
        // 构建流程变量
        const variables = { ...formData }
        
        // 添加流程所需的额外变量，确保安全访问用户属性
        const user = currentUser.value || {};
        variables.applicant = user.username || user.name || 'zhangsan'
        variables.applicantId = user.id || ''
        variables.applicantUsername = variables.applicant
        variables.applicantName = user.name || user.username || 'zhangsan'
        
        // 添加自定义变量
        customVariables.value.forEach(v => {
          if (v.name) {
            switch (v.type) {
              case 'integer':
                variables[v.name] = parseInt(v.value)
                break
              case 'boolean':
                variables[v.name] = v.value === 'true'
                break
              default:
                variables[v.name] = v.value
            }
          }
        })
        
        // 根据流程类型添加特定变量
        const processKey = selectedProcessDefinition.value.key
        if (processKey === 'leave-process') {
          variables.leaveProcessed = false
          // 计算请假时长（小时）
          if (formData.startTime && formData.endTime) {
            try {
              const start = new Date(formData.startTime)
              const end = new Date(formData.endTime)
              if (!isNaN(start.getTime()) && !isNaN(end.getTime()) && end > start) {
                const duration = (end - start) / (1000 * 60 * 60)
                variables.durationHours = duration
                // 计算请假天数并添加到变量中
                const days = duration / 8 // 假设每天8小时
                variables.leaveDays = Math.ceil(days * 2) / 2 // 四舍五入到0.5天
              }
            } catch (err) {
              console.error('日期计算错误:', err)
            }
          }
        } else if (processKey === 'onboarding-process') {
          variables.onboardingCompleted = false
          variables.itSetupCompleted = false
        }
        
        ElMessageBox.confirm(
          `确定要提交${selectedProcessDefinition.value.name}吗？`, 
          '确认提交', 
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(async () => {
          try {
            loading.value = true
            // 启动流程实例
              const startResponse = await ProcessDefinitionService.startProcessInstance(
                selectedProcessDefinition.value.key, // 使用key而不是id
                '', // 业务键为空
                variables // 直接传递variables对象
              )
            
              if (startResponse) {
                ElMessage.success('流程启动成功')
                
                // 跳转到待办任务列表或流程详情
                ElMessageBox.alert(
                  '流程已成功启动，即将跳转到待办任务页面', 
                  '成功', 
                  {
                    confirmButtonText: '确定',
                    callback: () => {
                      router.push('/task/list')
                    }
                  }
                )
              } else {
                ElMessage.error('流程启动失败')
              }
          } catch (error) {
            console.error('启动流程错误:', error)
            ElMessage.error('流程启动失败: ' + (error.message || '未知错误'))
          } finally {
            loading.value = false
          }
        }).catch(() => {
          // 取消提交
        })
      } catch (error) {
        // 表单验证失败
        console.error('表单验证失败:', error)
      }
    }
    
    // 导航到部署页面
    const navigateToDeploy = () => {
      router.push('/user-process-definition')
    }
    
    // 初始化
    onMounted(async () => {
      loadCurrentUser()
      await loadUsers()
      await loadProcessDefinitions()
    })
    
    return {
      loading,
      formRef,
      selectedProcessDefinition,
      processDefinitions,
      activeProcessDefinitions,
      formData,
      formRules,
      dynamicFormFields,
      customVariables,
      showCustomVariables,
      pageTitle,
      selectProcessDefinition,
      clearSelection,
      resetForm,
      addCustomVariable,
      removeCustomVariable,
      submitForm,
      navigateToDeploy,
      getUserOptions,
      getFieldRules
    }
  }
}
</script>

<style scoped>
.generic-process-apply {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.process-selection-section {
  margin-bottom: 20px;
}

.process-cards-row {
  margin-top: 20px;
}

.process-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.process-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.process-card-content {
  text-align: center;
  padding: 20px 0;
}

.process-icon {
  color: #409eff;
  margin-bottom: 15px;
}

.process-version {
  color: #67c23a;
  font-size: 14px;
  margin: 5px 0;
}

.process-key {
  color: #909399;
  font-size: 12px;
  margin-bottom: 10px;
}

.process-form-section {
  margin-top: 20px;
}

.form-actions {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
}

.form-actions .el-button {
  margin-left: 10px;
}

.no-processes {
  text-align: center;
  padding: 40px 0;
}

.no-processes .el-button {
  margin-top: 20px;
}

.custom-variable-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.custom-variable-item:hover {
  background-color: #ecf5ff;
}

/* 动画效果 */
.fade-in {
  animation: fadeIn 0.5s ease-in-out;
}

.slide-in {
  animation: slideIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .generic-process-apply {
    padding: 10px;
  }
  
  .custom-variable-item {
    flex-direction: column;
    align-items: stretch;
  }
  
  .custom-variable-item .el-input,
  .custom-variable-item .el-select {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 10px;
  }
  
  .custom-variable-item .el-button {
    align-self: flex-start;
  }
}
</style>