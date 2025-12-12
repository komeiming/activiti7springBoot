<template>
  <div class="process-apply-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程申请</span>
        </div>
      </template>

      <!-- 快速申请区域 -->
      <div class="quick-apply-section">
        <h3 style="margin-bottom: 20px;">快速申请</h3>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="24" :md="12" :lg="8">
            <el-card class="quick-apply-card" shadow="hover">
              <div class="quick-apply-content">
                <el-icon :size="48" class="quick-apply-icon"><Calendar /></el-icon>
                <div class="quick-apply-info">
                  <h4>请假申请</h4>
                  <p>快速申请各类假期</p>
                </div>
                <el-button type="primary" @click="selectProcessType(processTypes[0])">立即申请</el-button>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="24" :md="12" :lg="8">
            <el-card class="quick-apply-card" shadow="hover">
              <div class="quick-apply-content">
                <el-icon :size="48" class="quick-apply-icon"><User /></el-icon>
                <div class="quick-apply-info">
                  <h4>入职申请</h4>
                  <p>提交新员工入职审批</p>
                </div>
                <el-button type="primary" @click="selectProcessType(processTypes[1])">立即申请</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 流程类型选择 -->
      <div class="process-type-section">
        <h3 style="margin-bottom: 20px;">所有流程类型</h3>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="processType in processTypes" :key="processType.key">
            <el-card 
              class="process-type-card" 
              @click="selectProcessType(processType)"
              :class="{ 'selected': selectedProcessType?.key === processType.key }"
            >
              <div class="process-type-content">
                <el-icon :size="48" class="process-type-icon"><component :is="processType.icon" /></el-icon>
                <h4>{{ processType.name }}</h4>
                <p class="process-type-desc">{{ processType.description }}</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 流程表单 -->
      <div class="process-form-section" v-if="selectedProcessType">
        <el-divider>
          <h3>{{ selectedProcessType.name }}申请</h3>
        </el-divider>
        
        <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
          <!-- 请假流程表单 -->
          <template v-if="selectedProcessType.key === 'leave'">
            <el-form-item label="申请人" prop="applicantName">
              <el-input v-model="formData.applicantName" disabled></el-input>
            </el-form-item>
            <el-form-item label="部门" prop="department">
              <el-input v-model="formData.department" disabled></el-input>
            </el-form-item>
            <el-form-item label="请假类型" prop="leaveType">
              <el-select v-model="formData.leaveType" placeholder="请选择请假类型">
                <el-option label="年假" value="annual"></el-option>
                <el-option label="病假" value="sick"></el-option>
                <el-option label="事假" value="personal"></el-option>
                <el-option label="婚假" value="marriage"></el-option>
                <el-option label="产假" value="maternity"></el-option>
                <el-option label="其他" value="other"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="formData.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
              ></el-date-picker>
            </el-form-item>
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="formData.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
              ></el-date-picker>
            </el-form-item>
            <el-form-item label="请假天数" prop="days">
              <el-input-number v-model="formData.days" :min="0.5" :step="0.5" placeholder="请假天数"></el-input-number>
            </el-form-item>
            <el-form-item label="请假原因" prop="reason">
              <el-input v-model="formData.reason" type="textarea" rows="4" placeholder="请输入请假原因"></el-input>
            </el-form-item>
            <el-form-item label="审批人" prop="approver">
              <el-select v-model="formData.approver" placeholder="请选择审批人">
                <el-option 
                  v-for="manager in managers" 
                  :key="manager.username" 
                  :label="manager.fullName || manager.username" 
                  :value="manager.username"
                ></el-option>
              </el-select>
            </el-form-item>
          </template>

          <!-- 入职流程表单 -->
          <template v-else-if="selectedProcessType.key === 'onboarding'">
            <el-form-item label="申请人" prop="applicantName">
              <el-input v-model="formData.applicantName" disabled></el-input>
            </el-form-item>
            <el-form-item label="申请部门" prop="department">
              <el-input v-model="formData.department" disabled></el-input>
            </el-form-item>
            <el-form-item label="新员工姓名" prop="employeeName">
              <el-input v-model="formData.employeeName" placeholder="请输入新员工姓名"></el-input>
            </el-form-item>
            <el-form-item label="职位" prop="position">
              <el-input v-model="formData.position" placeholder="请输入职位"></el-input>
            </el-form-item>
            <el-form-item label="入职日期" prop="hireDate">
              <el-date-picker
                v-model="formData.hireDate"
                type="date"
                placeholder="选择入职日期"
                style="width: 100%"
              ></el-date-picker>
            </el-form-item>
            <el-form-item label="联系方式" prop="contact">
              <el-input v-model="formData.contact" placeholder="请输入联系方式"></el-input>
            </el-form-item>
            <el-form-item label="部门经理" prop="deptManager">
              <el-select v-model="formData.deptManager" placeholder="请选择部门经理">
                <el-option 
                  v-for="manager in managers" 
                  :key="manager.username" 
                  :label="manager.fullName || manager.username" 
                  :value="manager.username"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="IT负责人" prop="itManager">
              <el-select v-model="formData.itManager" placeholder="请选择IT负责人">
                <el-option 
                  v-for="it in itStaff" 
                  :key="it.username" 
                  :label="it.fullName || it.username" 
                  :value="it.username"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="申请原因" prop="reason">
              <el-input v-model="formData.reason" type="textarea" rows="4" placeholder="请输入申请原因"></el-input>
            </el-form-item>
          </template>

          <!-- 通用审批流程表单 -->
          <template v-else-if="selectedProcessType.key === 'approval'">
            <el-form-item label="申请人" prop="applicantName">
              <el-input v-model="formData.applicantName" disabled></el-input>
            </el-form-item>
            <el-form-item label="部门" prop="department">
              <el-input v-model="formData.department" disabled></el-input>
            </el-form-item>
            <el-form-item label="申请标题" prop="title">
              <el-input v-model="formData.title" placeholder="请输入申请标题"></el-input>
            </el-form-item>
            <el-form-item label="申请内容" prop="content">
              <el-input v-model="formData.content" type="textarea" rows="6" placeholder="请输入申请内容"></el-input>
            </el-form-item>
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="formData.priority" placeholder="请选择优先级">
                <el-option label="普通" value="normal"></el-option>
                <el-option label="紧急" value="urgent"></el-option>
                <el-option label="非常紧急" value="very_urgent"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="审批人" prop="approver">
              <el-select v-model="formData.approver" placeholder="请选择审批人">
                <el-option 
                  v-for="user in allUsers" 
                  :key="user.username" 
                  :label="user.fullName || user.username" 
                  :value="user.username"
                ></el-option>
              </el-select>
            </el-form-item>
          </template>
        </el-form>

        <div class="form-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="submitForm">提交申请</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '../utils/axiosConfig'
import UserService from '../services/UserService'
import processDefinitionService from '../services/ProcessDefinitionService'
import { Calendar, User, Document } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'

// 响应式数据
const selectedProcessType = ref(null)
const formRef = ref(null)
const allUsers = ref([])
const managers = ref([])
const itStaff = ref([])
const currentUser = ref({})
const route = useRoute()

// 流程类型定义
const processTypes = [
  {
    key: 'leave',
    name: '请假申请',
    description: '申请各类假期，如年假、病假、事假等',
    icon: Calendar,
    processKey: 'leave-process'
  },
  {
    key: 'onboarding',
    name: '入职申请',
    description: '为新员工申请入职流程',
    icon: User,
    processKey: 'onboarding-process'
  },
  {
    key: 'approval',
    name: '通用审批',
    description: '发起各类需要审批的申请',
    icon: Document,
    processKey: 'general-approval'
  }
]

// 表单数据
const formData = reactive({
      // 通用字段
      applicantName: '',
      department: '',
      reason: '',
      
      // 请假流程字段
      leaveType: '',
      startTime: '',
      endTime: '',
      days: '',
      approver: '',
      
      // 入职流程字段
      employeeName: '',
      position: '',
      hireDate: '',
      contact: '',
      deptManager: '',
      itManager: '',
      
      // 通用审批字段
      title: '',
      content: '',
      priority: 'normal'
})

// 表单验证规则
const formRules = computed(() => {
      if (selectedProcessType.value?.key === 'leave') {
        return {
          leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
          startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
          endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
          days: [{ required: true, message: '请输入请假天数', trigger: 'blur' }],
          reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }],
          approver: [{ required: true, message: '请选择审批人', trigger: 'change' }]
        }
      } else if (selectedProcessType.value?.key === 'onboarding') {
        return {
          employeeName: [{ required: true, message: '请输入新员工姓名', trigger: 'blur' }],
          position: [{ required: true, message: '请输入职位', trigger: 'blur' }],
          hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
          contact: [{ required: true, message: '请输入联系方式', trigger: 'blur' }],
          deptManager: [{ required: true, message: '请选择部门经理', trigger: 'change' }],
          itManager: [{ required: true, message: '请选择IT负责人', trigger: 'change' }],
          reason: [{ required: true, message: '请输入申请原因', trigger: 'blur' }]
        }
      } else if (selectedProcessType.value?.key === 'approval') {
        return {
          title: [{ required: true, message: '请输入申请标题', trigger: 'blur' }],
          content: [{ required: true, message: '请输入申请内容', trigger: 'blur' }],
          priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
          approver: [{ required: true, message: '请选择审批人', trigger: 'change' }]
        }
      }
      return {}
})

// 临时模拟用户数据，用于在API调用失败时提供备用数据
const mockUsers = [
  {
    id: '1',
    username: 'zhangsan',
    fullName: '张三',
    department: '研发部',
    position: '软件工程师',
    roles: [{ roleCode: 'ROLE_USER', roleName: '普通用户' }]
  },
  {
    id: '2',
    username: 'lisi',
    fullName: '李四',
    department: '研发部',
    position: '部门经理',
    roles: [{ roleCode: 'ROLE_MANAGER', roleName: '部门经理' }]
  },
  {
    id: '3',
    username: 'wangwu',
    fullName: '王五',
    department: 'IT运维部',
    roles: [{ roleCode: 'ROLE_IT', roleName: 'IT负责人' }]
  },
  {
    id: '4',
    username: 'zhaoliu',
    fullName: '赵六',
    department: '财务部',
    roles: [{ roleCode: 'ROLE_USER', roleName: '普通用户' }]
  }
]

// 加载用户数据
const loadUserData = async () => {
      try {
        // 首先尝试从localStorage获取当前用户信息
        const userInfoStr = localStorage.getItem('userInfo')
        if (userInfoStr) {
          try {
            const userData = JSON.parse(userInfoStr)
            if (userData && typeof userData === 'object') {
              currentUser.value = userData
              // 安全地设置表单字段，提供默认值
              formData.applicantName = userData.fullName || userData.username || '未知用户'
              formData.department = userData.department || '未设置部门'
            } else {
              console.warn('用户信息格式错误，尝试从服务获取')
              // 尝试从UserService获取当前用户
              await loadUserFromService()
            }
          } catch (parseError) {
            console.error('解析用户信息失败:', parseError)
            localStorage.removeItem('userInfo') // 移除错误的用户信息
            await loadUserFromService()
          }
        } else {
          // localStorage中没有用户信息，尝试从服务获取
          console.warn('本地未找到用户信息，尝试从服务获取')
          await loadUserFromService()
          
          // 如果从服务也获取失败，使用默认用户信息
          if (!currentUser.value || !currentUser.value.username) {
            console.warn('从服务获取用户信息失败，使用默认用户')
            currentUser.value = {
              id: 'current',
              username: 'currentuser',
              fullName: '当前用户',
              department: '默认部门'
            }
            formData.applicantName = '当前用户'
            formData.department = '默认部门'
          }
        }
        
        // 使用UserService获取所有用户
        console.log('开始获取用户列表...')
        const usersData = await UserService.getUsers()
        
        // 处理获取到的用户数据
        if (Array.isArray(usersData) && usersData.length > 0) {
          allUsers.value = usersData
          console.log('成功从API获取用户列表，数量:', usersData.length)
        } else {
          // API获取失败或返回空数据，使用模拟数据
          console.warn('API获取用户列表失败或返回空数据，使用模拟数据')
          allUsers.value = mockUsers
          // 显示提示信息但不阻止用户操作
          ElMessage.warning('使用备用用户数据，可能不是最新的')
        }
        
        // 筛选管理人员和IT人员 - 增强安全检查和兼容性
        managers.value = allUsers.value.filter(user => {
          if (!user || typeof user !== 'object') return false
          
          // 支持不同的角色表示方式
          const roles = user.roles || user.role || []
          if (!Array.isArray(roles)) return false
          
          return roles.some(role => {
            if (!role) return false
            // 检查各种可能的角色表示形式
            return role === 'ROLE_MANAGER' || 
                   role === 'ROLE_ADMIN' ||
                   role === 'MANAGER' ||
                   role === 'ADMIN' ||
                   (role.roleCode && (role.roleCode === 'ROLE_MANAGER' || role.roleCode === 'ROLE_ADMIN')) ||
                   (role.name && (role.name.includes('经理') || role.name.includes('管理员')))
          })
        })
        
        itStaff.value = allUsers.value.filter(user => {
          if (!user || typeof user !== 'object') return false
          
          // 支持不同的角色表示方式
          const roles = user.roles || user.role || []
          if (!Array.isArray(roles)) return false
          
          return roles.some(role => {
            if (!role) return false
            // 检查各种可能的角色表示形式
            return role === 'ROLE_IT' ||
                   role === 'IT' ||
                   (role.roleCode && role.roleCode === 'ROLE_IT') ||
                   (role.name && (role.name.includes('IT') || role.name.includes('技术')))
          })
        })
        
        // 验证筛选结果，如果没有管理员或IT人员，从模拟数据中补充
        if (managers.value.length === 0) {
          console.warn('未找到管理人员，从模拟数据中补充')
          managers.value = mockUsers.filter(u => u.username === 'zhangsan' || u.username === 'lisi')
        }
        
        if (itStaff.value.length === 0) {
          console.warn('未找到IT人员，从模拟数据中补充')
          itStaff.value = mockUsers.filter(u => u.username === 'wangwu')
        }
        
        console.log('用户列表加载完成，管理人员:', managers.value.length, 'IT人员:', itStaff.value.length)
      } catch (error) {
        console.error('加载用户数据错误:', error)
        
        // 出错时使用模拟数据确保界面可用
        console.warn('发生错误，使用模拟数据确保功能可用')
        allUsers.value = mockUsers
        managers.value = mockUsers.filter(u => u.username === 'zhangsan' || u.username === 'lisi')
        itStaff.value = mockUsers.filter(u => u.username === 'wangwu')
        
        // 使用更友好的错误提示
        ElMessage.error('获取用户数据时出现问题，已使用备用数据')
      }
}

// 从服务获取当前用户信息
const loadUserFromService = async () => {
  try {
    // 尝试使用UserService的方法获取当前用户
    const currentUserData = await UserService.getCurrentUserInfo() || UserService.getCurrentUser()
    if (currentUserData && typeof currentUserData === 'object') {
      currentUser.value = currentUserData
      formData.applicantName = currentUserData.fullName || currentUserData.username || '未知用户'
      formData.department = currentUserData.department || '未设置部门'
      // 保存到localStorage以备后续使用
      localStorage.setItem('userInfo', JSON.stringify(currentUserData))
    } else {
      console.warn('无法获取当前用户信息')
      // 设置默认用户信息以避免应用崩溃
      currentUser.value = {
        id: 'default',
        username: 'default',
        fullName: '默认用户',
        department: '默认部门'
      }
      formData.applicantName = '默认用户'
      formData.department = '默认部门'
    }
  } catch (error) {
    console.error('从服务获取用户信息失败:', error)
  }
}

// 选择流程类型
const selectProcessType = (processType) => {
      selectedProcessType.value = processType
      resetForm()
}

// 重置表单
const resetForm = () => {
      // 安全检查：确保formRef.value存在且有resetFields方法
      if (formRef.value && typeof formRef.value.resetFields === 'function') {
        formRef.value.resetFields()
      }
      
      // 重置基本信息
      formData.applicantName = currentUser.value.fullName || currentUser.value.username
      formData.department = currentUser.value.department || ''
      
      // 根据流程类型重置相应字段
      if (selectedProcessType.value?.key === 'leave') {
        formData.leaveType = ''
        formData.startTime = ''
        formData.endTime = ''
        formData.days = ''
        formData.reason = ''
        formData.approver = ''
      } else if (selectedProcessType.value?.key === 'onboarding') {
        formData.employeeName = ''
        formData.position = ''
        formData.hireDate = ''
        formData.contact = ''
        formData.deptManager = ''
        formData.itManager = ''
        formData.reason = ''
      } else if (selectedProcessType.value?.key === 'approval') {
        formData.title = ''
        formData.content = ''
        formData.priority = 'normal'
        formData.approver = ''
      }
    }
    
// 提交表单
const submitForm = async () => {
  if (!selectedProcessType.value) {
    ElMessage.warning('请先选择流程类型')
    return
  }
  
  try {
    // 确保formRef.value存在
    if (!formRef.value || typeof formRef.value.validate !== 'function') {
      ElMessage.error('表单初始化失败')
      return
    }
    
    await formRef.value.validate()
    
    // 构建流程变量
    const process = processTypes.find(p => p.key === selectedProcessType.value.key)
    const variables = { ...formData }
    
    // 添加申请人信息，确保使用正确的属性名
    if (currentUser.value) {
      variables.applicantUsername = currentUser.value.username || currentUser.value.name || 'unknown'
      variables.applicantName = currentUser.value.name || currentUser.value.username || 'unknown'
    }
    
    // 根据流程类型添加特定变量
    if (selectedProcessType.value.key === 'leave') {
      variables.leaveProcessed = false
      // 计算请假时长（小时）
      if (formData.startTime && formData.endTime) {
        const duration = (new Date(formData.endTime) - new Date(formData.startTime)) / (1000 * 60 * 60)
        variables.durationHours = duration
      }
    } else if (selectedProcessType.value.key === 'onboarding') {
      variables.onboardingCompleted = false
      variables.itSetupCompleted = false
    }
    
    // 显示确认对话框
    const confirmResult = await ElMessageBox.confirm(
      `确定要提交${selectedProcessType.value.name}吗？`, 
      '确认提交', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirmResult !== 'confirm') {
      return
    }
    
    // 使用ProcessDefinitionService启动流程实例
    // 注意：ProcessDefinitionService内部已处理用户信息和基础错误消息
    const businessKey = `申请_${selectedProcessType.value.key}_${new Date().getTime()}`
    const startResponse = await processDefinitionService.startProcessInstance(
      process.processKey,
      businessKey,
      variables
    )
    
    // 根据服务返回结果执行后续操作
    if (startResponse && startResponse.success !== false) {
      // 服务内部已显示成功消息，这里只执行重置操作
      resetForm()
      // 如果需要，可以在这里执行其他操作，如页面跳转等
    } else if (startResponse === null) {
      // 服务内部已处理错误消息，这里可以不做额外处理
      console.warn('流程启动失败，错误信息已在服务层显示')
    }
  } catch (error) {
    // 处理各种错误情况
    console.error('提交表单时发生错误:', error)
    
    if (error === 'cancel') {
      // 用户取消提交
      console.log('用户取消提交')
    } else if (error.name === 'ValidationError' || error.message?.includes('validation')) {
      // 表单验证失败
      console.error('表单验证失败:', error)
      ElMessage.warning('表单数据不完整，请检查并填写必填项')
    } else {
      // 提交流程错误 - 提供更详细的错误信息
      let errorMsg = '提交失败'
      
      if (error.response) {
        // 服务器返回错误响应
        errorMsg = `提交失败: ${error.response.data?.message || error.response.statusText || '服务器错误'}`
        console.error('服务器响应错误:', error.response.data)
      } else if (error.request) {
        // 请求已发送但未收到响应
        errorMsg = '提交失败: 网络连接问题，请检查网络后重试'
        console.error('网络请求错误:', error.request)
      } else {
        // 其他类型的错误
        errorMsg = `提交失败: ${error.message || '未知错误'}`
      }
      
      ElMessage.error(errorMsg)
    }
  }
}
    
// 计算请假天数
const calculateDays = () => {
  if (formData.startTime && formData.endTime) {
    const start = new Date(formData.startTime)
    const end = new Date(formData.endTime)
    if (end > start) {
      const diffTime = Math.abs(end - start)
      const diffDays = diffTime / (1000 * 60 * 60 * 24)
      // 更精确的天数计算，考虑工作时间
      formData.days = Math.ceil(diffDays * 2) / 2 // 四舍五入到0.5天
    }
  }
}

// 正确监听时间变化，自动计算天数
watch(() => [formData.startTime, formData.endTime], () => {
  if (selectedProcessType.value?.key === 'leave') {
    calculateDays()
  }
}, { deep: true })

// 初始化
onMounted(() => {
  loadUserData()
  // 处理URL参数，从Dashboard跳转过来时自动选择流程类型
  handleRouteParams()
})

// 处理URL参数
const handleRouteParams = async () => {
  try {
    const processTypeParam = route.query.processType
    if (processTypeParam) {
      // 查找对应的流程类型
      const foundProcessType = processTypes.find(p => p.key === processTypeParam)
      if (foundProcessType) {
        // 如果是请假流程，尝试先部署流程定义
        if (processTypeParam === 'leave') {
          console.log('检测到请假流程参数，尝试部署请假流程定义...')
          const deployResult = await processDefinitionService.deployLeaveProcess()
          // 无论部署是否成功，都选择流程类型，让用户可以继续操作
          console.log('流程部署结果:', deployResult)
        }
        
        selectedProcessType.value = foundProcessType
        console.log('从URL参数自动选择流程类型:', foundProcessType.name)
      } else {
        console.warn('URL参数中指定的流程类型不存在:', processTypeParam)
      }
    }
  } catch (error) {
    console.error('处理URL参数时发生错误:', error)
    // 即使出错，也不要阻止用户操作
  }
}

// 暴露所有需要在模板中使用的变量
defineExpose({
  selectedProcessType,
  processTypes,
  formRef,
  formData,
  formRules,
  allUsers,
  managers,
  itStaff,
  selectProcessType,
  resetForm,
  submitForm,
  calculateDays,
  handleRouteParams
})
</script setup>

<style scoped>
.process-apply-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-apply-section {
  margin-bottom: 40px;
}

.quick-apply-card {
  transition: all 0.3s ease;
}

.quick-apply-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.quick-apply-content {
  display: flex;
  align-items: center;
  padding: 15px 0;
}

.quick-apply-icon {
  color: #409eff;
  margin-right: 20px;
}

.quick-apply-info {
  flex: 1;
}

.quick-apply-info h4 {
  margin: 0 0 5px 0;
  color: #303133;
}

.quick-apply-info p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.process-type-section {
  margin-bottom: 30px;
}

.process-type-card {
  height: 100%;
  cursor: pointer;
  transition: all 0.3s ease;
}

.process-type-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.process-type-card.selected {
  border-color: #409eff;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

.process-type-content {
  text-align: center;
  padding: 20px 0;
}

.process-type-icon {
  color: #409eff;
  margin-bottom: 10px;
}

.process-type-desc {
  color: #606266;
  font-size: 12px;
  margin-top: 5px;
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
</style>