<template>
  <div class="leave-application-container">
    <el-card shadow="always">
      <template #header>
        <div class="card-header">
          <span>请假申请</span>
        </div>
      </template>

      <!-- 流程状态提示 -->
      <div class="process-status" v-if="processStatus !== ''">
        <el-alert 
          :title="processStatus" 
          :type="processStatusType" 
          :closable="false"
          show-icon
        />
      </div>
      
      <!-- 手动部署按钮 -->
      <div class="deploy-section" v-if="!hasProcessDefinition">
        <el-button 
          type="primary" 
          @click="manualDeployProcess" 
          :loading="deploying"
          style="margin-bottom: 20px;"
        >
          {{ deploying ? '正在部署...' : '手动部署请假流程' }}
        </el-button>
      </div>
      
      <el-form 
        ref="leaveFormRef" 
        :model="leaveForm" 
        :rules="rules" 
        label-width="120px" 
        class="leave-form"
      >
        <!-- 基本信息 -->
        <el-form-item label="申请人" prop="applicantName">
          <el-input v-model="leaveForm.applicantName" placeholder="请输入姓名" :disabled="true" />
        </el-form-item>

        <el-form-item label="部门" prop="department">
          <el-input v-model="leaveForm.department" placeholder="请输入部门" :disabled="true" />
        </el-form-item>

        <el-form-item label="职位" prop="position">
          <el-input v-model="leaveForm.position" placeholder="请输入职位" :disabled="true" />
        </el-form-item>

        <!-- 请假信息 -->
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="leaveForm.leaveType" placeholder="请选择请假类型" style="width: 100%">
            <el-option label="年假" value="annual" />
            <el-option label="病假" value="sick" />
            <el-option label="事假" value="personal" />
            <el-option label="婚假" value="marriage" />
            <el-option label="产假/陪产假" value="maternity" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="leaveForm.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="leaveForm.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="请假天数" prop="leaveDays">
          <el-input v-model="leaveForm.leaveDays" placeholder="自动计算，也可手动修改" type="number" min="0" />
        </el-form-item>

        <el-form-item label="请假原因" prop="leaveReason">
          <el-input
            v-model="leaveForm.leaveReason"
            type="textarea"
            :rows="4"
            placeholder="请详细描述请假原因"
          />
        </el-form-item>

        <el-form-item label="紧急程度" prop="urgency">
          <el-select v-model="leaveForm.urgency" placeholder="请选择紧急程度" style="width: 100%">
            <el-option label="一般" value="normal" />
            <el-option label="紧急" value="urgent" />
            <el-option label="非常紧急" value="very_urgent" />
          </el-select>
        </el-form-item>

        <el-form-item label="抄送人" prop="ccList">
          <el-select
            v-model="leaveForm.ccList"
            multiple
            placeholder="请选择抄送人（可选）"
            style="width: 100%"
          >
            <el-option
              v-for="user in allUsers"
              :key="user.username"
              :label="`${(user.fullName || user.nickname || user.username)}(${user.username})`"
              :value="user.username"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <div style="display: flex; gap: 12px;">
            <el-button type="primary" @click="submitApplication" :loading="loading">提交申请</el-button>
            <el-button @click="handleCancel">取消</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import ProcessDefinitionService from '../services/ProcessDefinitionService'
import UserService from '../services/UserService'
import axios from '../utils/axiosConfig'
import { ElMessage } from 'element-plus'

export default {
  name: 'LeaveApplicationView',
  data() {
    return {
        leaveForm: {
          applicantName: '',
          department: '',
          position: '',
          leaveType: '',
          startDate: '',
          endDate: '',
          leaveDays: '',
          leaveReason: '',
          urgency: 'normal',
          ccList: []
        },
        rules: {
          leaveType: [
            { required: true, message: '请选择请假类型', trigger: 'change' }
          ],
          startDate: [
            { required: true, message: '请选择开始日期', trigger: 'change' }
          ],
          endDate: [
            { required: true, message: '请选择结束日期', trigger: 'change' },
            {
              validator: this.validateEndDate,
              trigger: 'change'
            }
          ],
          leaveDays: [
            { required: true, message: '请填写请假天数', trigger: 'blur' },
            { type: 'number', min: 0.5, message: '请假天数最少为0.5天', trigger: 'blur' }
          ],
          leaveReason: [
            { required: true, message: '请填写请假原因', trigger: 'blur' },
            { min: 5, message: '请假原因不能少于5个字符', trigger: 'blur' }
          ]
        },
        loading: false,
        deploying: false,
        allUsers: [],
        currentUser: null,
        processStatus: '',
        processStatusType: 'info', // info, success, warning, error
        hasProcessDefinition: false
    }
  },
  created() {
    console.log('[LeaveApplicationView] 组件创建，开始初始化表单...')
    this.initializeForm()
  },
  mounted() {
    console.log('[LeaveApplicationView] 页面加载完成')
    // 页面挂载后尝试查找流程定义
    this.initializeProcessDefinition()
  },
  
  methods: {
    // 初始化流程定义
    async initializeProcessDefinition() {
      console.log('[LeaveApplicationView] 初始化流程定义查找...')
      try {
        // 设置状态提示
        this.processStatus = '正在查找请假流程定义...'
        this.processStatusType = 'info'
        
        // 先尝试查找已存在的流程定义
        const processDefinition = await this.findLeaveProcessDefinition()
        
        if (processDefinition) {
          console.log('[LeaveApplicationView] 找到流程定义，设置状态')
          this.hasProcessDefinition = true
          this.processStatus = '已找到请假流程定义'
          this.processStatusType = 'success'
        } else {
          console.warn('[LeaveApplicationView] 未找到流程定义，尝试部署...')
          this.hasProcessDefinition = false
          this.processStatus = '未找到请假流程定义，请部署流程'
          this.processStatusType = 'warning'
          // 尝试自动部署
          await this.attemptToDeployProcess()
        }
      } catch (error) {
        console.error('[LeaveApplicationView] 初始化流程定义失败:', error)
        this.processStatus = '初始化流程定义出错: ' + (error?.message || '未知错误')
        this.processStatusType = 'error'
      }
    },
    
    // 尝试部署流程
    async attemptToDeployProcess() {
      try {
        console.log('[LeaveApplicationView] 尝试部署请假流程定义...')
        this.processStatus = '正在部署请假流程定义...'
        this.processStatusType = 'info'
        
        const deployResult = await ProcessDefinitionService.deployLeaveProcess()
        console.log('[LeaveApplicationView] 部署结果:', deployResult)
        
        if (deployResult && deployResult.success !== false) {
          console.log('[LeaveApplicationView] 部署成功，再次查找流程定义')
          this.processStatus = '流程部署成功，正在验证...'
          this.processStatusType = 'success'
          
          // 延迟一小段时间，确保部署完全生效
          await new Promise(resolve => setTimeout(resolve, 1000))
          
          // 再次查找流程定义
          const processDefinition = await this.findLeaveProcessDefinition()
          if (processDefinition) {
            this.hasProcessDefinition = true
            this.processStatus = '流程部署成功并已找到'
            this.processStatusType = 'success'
          } else {
            this.hasProcessDefinition = false
            this.processStatus = '流程部署成功但未找到，请手动验证'
            this.processStatusType = 'warning'
          }
        } else {
          this.hasProcessDefinition = false
          this.processStatus = '流程部署失败: ' + (deployResult?.message || '未知错误')
          this.processStatusType = 'error'
        }
      } catch (error) {
        console.error('[LeaveApplicationView] 部署流程失败:', error)
        this.hasProcessDefinition = false
        this.processStatus = '部署流程异常: ' + (error.message || '未知错误')
        this.processStatusType = 'error'
      }
    },
    
    // 手动部署流程
    async manualDeployProcess() {
      console.log('[LeaveApplicationView] 用户手动触发流程部署...')
      this.deploying = true
      try {
        await this.attemptToDeployProcess()
      } finally {
        this.deploying = false
      }
    },
    
    // 初始化表单
    async initializeForm() {
      console.log('[LeaveApplicationView] 开始初始化表单...')
      this.loading = true
      try {
        // 获取当前用户信息
        console.log('[LeaveApplicationView] 获取当前用户信息...')
        this.currentUser = UserService.getCurrentUser() || {}
        console.log('[LeaveApplicationView] 当前用户信息:', this.currentUser)

        // 填充基本信息
        this.leaveForm.applicantName = this.currentUser.name || ''
        this.leaveForm.department = this.currentUser.department || ''
        this.leaveForm.position = this.currentUser.position || ''
        console.log('[LeaveApplicationView] 基本信息填充完成')

        // 获取所有用户列表（用于抄送人选择）
        await this.loadAllUsers()
        
        console.log('[LeaveApplicationView] 表单初始化完成')
      } catch (error) {
        console.error('[LeaveApplicationView] 初始化表单失败:', error)
        ElMessage.error('初始化表单失败，请重试')
      } finally {
        this.loading = false
      }
    },

    // 从数据库获取所有用户，筛选出部门经理用于抄送人选择
    async loadAllUsers() {
      console.log('[LeaveApplicationView] 开始获取用户列表...');
      try {
        // 使用UserService获取真实用户数据
        const users = await UserService.getUsers();
        console.log('[LeaveApplicationView] 获取到的用户列表:', users);
        
        // 过滤出部门经理角色的用户
        // 兼容不同的角色表示方式
        this.allUsers = users.filter(user => {
          // 检查用户是否具有部门经理角色
          if (user.roles && Array.isArray(user.roles)) {
            // 角色是对象数组的情况
            return user.roles.some(role => {
              const roleCode = role.roleCode || role.code || role.name;
              return roleCode && (roleCode.includes('MANAGER') || roleCode.includes('manager'));
            });
          } else if (user.position && (user.position.includes('经理') || user.position.includes('Manager'))) {
            // 根据职位名称判断
            return true;
          } else if (user.role && (user.role.includes('MANAGER') || user.role.includes('manager'))) {
            // 角色是字符串的情况
            return true;
          }
          return false;
        });
        
        // 如果没有找到部门经理，添加系统管理员作为备选
        if (this.allUsers.length === 0) {
          this.allUsers = [
            { username: 'admin', name: '系统管理员', fullName: '系统管理员', department: '技术部', position: '管理员' }
          ];
          console.warn('[LeaveApplicationView] 未找到部门经理，使用系统管理员作为备选');
        }
        
        console.log('[LeaveApplicationView] 用户列表加载完成，共', this.allUsers.length, '个部门经理');
      } catch (error) {
        console.error('[LeaveApplicationView] 获取用户列表失败:', error);
        // 出错时使用默认管理员
        this.allUsers = [
          { username: 'admin', name: '系统管理员', fullName: '系统管理员', department: '技术部', position: '管理员' }
        ];
      }
    },

    // 验证结束日期
    validateEndDate(rule, value, callback) {
      console.log('[LeaveApplicationView] 验证结束日期...')
      if (!value) {
        return callback(new Error('请选择结束日期'))
      }

      const startDate = new Date(this.leaveForm.startDate)
      const endDate = new Date(value)

      if (startDate > endDate) {
        console.warn('[LeaveApplicationView] 结束日期早于开始日期')
        callback(new Error('结束日期不能早于开始日期'))
      } else {
        callback()
        // 自动计算请假天数
        this.calculateLeaveDays()
      }
    },

    // 计算请假天数
    calculateLeaveDays() {
      console.log('[LeaveApplicationView] 计算请假天数...')
      if (this.leaveForm.startDate && this.leaveForm.endDate) {
        const startDate = new Date(this.leaveForm.startDate)
        const endDate = new Date(this.leaveForm.endDate)
        const diffTime = Math.abs(endDate - startDate)
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1
        this.leaveForm.leaveDays = diffDays
        console.log('[LeaveApplicationView] 计算结果：', diffDays, '天')
      }
    },

    // 提交请假申请
    async submitApplication() {
      console.log('[LeaveApplicationView] 开始提交请假申请...')
      this.$refs.leaveFormRef.validate(async (valid) => {
        if (!valid) {
          console.warn('[LeaveApplicationView] 表单验证失败')
          return false
        }
        
        // 设置提交状态
        this.processStatus = '正在提交请假申请...'
        this.processStatusType = 'info'
        
        this.loading = true
        try {
          // 先尝试查找流程定义，避免每次都部署
          console.log('[LeaveApplicationView] 查找流程定义...')
          let processDefinition = await this.findLeaveProcessDefinition();
          let deployAttempted = false;
          
          // 如果没有找到流程定义，再尝试部署
          if (!processDefinition) {
            deployAttempted = true;
            console.log('[LeaveApplicationView] 未找到流程定义，准备部署...')
            this.processStatus = '正在部署请假流程定义...'
            this.processStatusType = 'info'
            const deployResult = await ProcessDefinitionService.deployLeaveProcess();
            console.log('[LeaveApplicationView] 部署结果:', deployResult)
            
            // 部署后再次查找流程定义
            if (deployResult && deployResult.success !== false) {
              console.log('[LeaveApplicationView] 部署成功，再次查找流程定义')
              this.processStatus = '流程部署成功，正在验证...'
              this.processStatusType = 'success'
              // 延迟一小段时间，确保部署完全生效
              await new Promise(resolve => setTimeout(resolve, 1000));
              processDefinition = await this.findLeaveProcessDefinition();
            } else {
              // 部署失败，提供详细信息
              const errorMsg = deployResult && deployResult.message ? deployResult.message : '流程部署失败，可能是后端服务不可用或权限不足';
              console.error('[LeaveApplicationView] 流程部署失败:', errorMsg)
              this.processStatus = '部署流程失败: ' + errorMsg
              this.processStatusType = 'error'
              ElMessage.error(`流程部署失败: ${errorMsg}`);
              this.loading = false;
              return;
            }
          }
          
          // 检查流程定义是否存在
          if (!processDefinition) {
            console.error('[LeaveApplicationView] 未找到请假流程定义')
            // 根据是否尝试过部署提供不同的错误信息
            const errorMsg = deployAttempted 
              ? '流程定义部署失败或部署后无法找到，请联系管理员检查后端配置' 
              : '未找到请假流程定义，部署失败';
            this.processStatus = errorMsg
            this.processStatusType = 'error'
            ElMessage.error(errorMsg)
            return;
          }
          
          // 构建流程变量，确保安全访问用户属性
          const user = this.currentUser || {};
          const processVariables = {
            ...this.leaveForm,
            applicantUsername: user.username || user.name || 'zhangsan',
            applicantName: user.name || user.username || 'zhangsan',
            applicationTime: new Date().toISOString(),
            approvalStatus: 'PENDING',
            ccUsers: this.leaveForm.ccList && this.leaveForm.ccList.join ? this.leaveForm.ccList.join(',') : '',
            // 添加必要的manager和applicant变量以支持流程定义中的表达式
            manager: this.leaveForm.ccList && this.leaveForm.ccList.length > 0 ? this.leaveForm.ccList[0] : 'manager1',
            applicant: user.username || user.name || 'zhangsan'
          }
          
          // 创建businessKey，确保安全访问用户属性
          // 安全获取用户名，避免使用可选链操作符以兼容构建环境
          const username = (this.currentUser && this.currentUser.username) ? this.currentUser.username : 
                         ((this.currentUser && this.currentUser.name) ? this.currentUser.name : 'zhangsan');
          const businessKey = "leave-" + username + "-" + new Date().getTime();
          
          console.log('[LeaveApplicationView] 准备启动流程实例，使用流程定义key:', processDefinition.key)
          console.log('[LeaveApplicationView] 流程变量:', processVariables)
          
          // 启动流程实例
          const processInstance = await ProcessDefinitionService.startProcessInstance(
            processDefinition.key,
            businessKey,
            processVariables
          )
          
          // 严格检查流程实例创建结果
          // 只要返回的对象包含processInstanceId字段就认为流程启动成功
          // 或者success为true也认为成功
          console.log('[LeaveApplicationView] 流程实例创建结果类型检查:', typeof processInstance)
          if (processInstance && typeof processInstance === 'object') {
            console.log('[LeaveApplicationView] 流程实例对象键:', Object.keys(processInstance))
            console.log('[LeaveApplicationView] 完整流程实例对象:', JSON.stringify(processInstance, null, 2))
            
            // 检查成功条件
            if (processInstance.processInstanceId || (processInstance.success === true)) {
              console.log('[LeaveApplicationView] 请假申请提交成功！Process ID:', processInstance.processInstanceId)
              this.processStatus = '请假申请提交成功！流程ID: ' + (processInstance.processInstanceId || '')
              this.processStatusType = 'success'
              ElMessage.success('请假申请提交成功！')
              
              // 显示成功提示并跳转到任务列表
              this.$confirm('请假申请已成功提交，是否前往任务列表查看？', '操作成功', {
                confirmButtonText: '前往查看',
                cancelButtonText: '留在当前页',
                type: 'success'
              }).then(() => {
                this.$router.push('/tasks')
              }).catch(() => {
                // 用户选择留在当前页，可以重置表单或执行其他操作
                this.resetForm()
              })
            } else {
              // 明确处理流程实例创建失败的情况
              // 安全获取错误信息，避免使用可选链操作符
              let errorMessage = '流程实例创建失败'
              if (processInstance && processInstance.message) {
                errorMessage = processInstance.message
              } else if (processInstance && processInstance.error) {
                errorMessage += ': ' + processInstance.error
              }
              
              console.error('[LeaveApplicationView] 流程实例创建失败:', errorMessage)
              console.error('[LeaveApplicationView] 失败详情:', JSON.stringify(processInstance, null, 2))
              
              this.processStatus = '提交失败: ' + errorMessage
              this.processStatusType = 'error'
              ElMessage.error('提交失败: ' + errorMessage)
            }
          } else {
            // 处理返回值不是对象的情况
            console.error('[LeaveApplicationView] 流程实例创建失败: 收到非对象响应', processInstance)
            this.processStatus = '提交失败: 流程实例创建失败，收到非预期响应格式'
            this.processStatusType = 'error'
            ElMessage.error('提交失败: 流程实例创建失败，收到非预期响应格式')
          }
        } catch (error) {
          console.error('[LeaveApplicationView] 提交请假申请异常:', error)
          // 提供更具体的错误信息
          const errorMsg = error.response?.data?.message || error.message || '提交失败，请检查网络或联系管理员'
          this.processStatus = '提交失败: ' + errorMsg
          this.processStatusType = 'error'
          ElMessage.error('提交失败: ' + errorMsg)
        } finally {
          this.loading = false
        }
      })
    },

    // 查找请假流程定义
    async findLeaveProcessDefinition() {
      console.log('[LeaveApplicationView] 查找请假流程定义...')
      try {
        // 首先使用getLatestProcessDefinition方法，它有更完善的查找逻辑
        console.log('[LeaveApplicationView] 尝试通过getLatestProcessDefinition方法获取')
        // 使用正确的流程key 'leave-process'（带连字符）
        let leaveProcess = await ProcessDefinitionService.getLatestProcessDefinition('leave-process')
        console.log('[LeaveApplicationView] getLatestProcessDefinition返回:', leaveProcess)
        
        // 如果没有找到，尝试获取所有流程定义并进行更广泛的匹配
        if (!leaveProcess) {
          console.log('[LeaveApplicationView] 调用ProcessDefinitionService.getProcessDefinitions()进行更广泛查找')
          const response = await ProcessDefinitionService.getProcessDefinitions(1, 100) // 增加size参数
          
          console.log('[LeaveApplicationView] 获取到的流程定义列表响应:', response)
          
          // 智能处理多种返回格式，确保processList是一个数组
          let processList = [];
          
          // 情况1: 如果response本身是数组
          if (Array.isArray(response)) {
            processList = response;
          }
          // 情况2: 检查response.data是否为数组
          else if (response.data && Array.isArray(response.data)) {
            processList = response.data;
          }
          // 情况3: 检查response.rows是否为数组（后端可能直接返回这种格式）
          else if (response.rows && Array.isArray(response.rows)) {
            processList = response.rows;
          }
          // 情况4: 检查response.originalResponse.rows是否为数组
          else if (response.originalResponse && response.originalResponse.rows && Array.isArray(response.originalResponse.rows)) {
            processList = response.originalResponse.rows;
          }
          
          console.log('[LeaveApplicationView] 处理后的流程定义列表:', processList.map(p => ({key: p.key, name: p.name})))
          
          // 查找key为'leave-process'或名称包含'请假'/'leave'的流程定义，使用更宽松的匹配规则
          const keywords = ['leave', '请假', 'leaveProcess', 'leave_process', 'leave-process']
          leaveProcess = processList.find(process => {
            const processKey = process.key || ''
            const processName = process.name || ''
            return keywords.some(keyword => 
              processKey.toLowerCase().includes(keyword.toLowerCase()) || 
              processName.includes(keyword)
            )
          })
        }
        
        // 更新状态
        if (leaveProcess) {
          this.hasProcessDefinition = true
          this.processStatus = '已找到请假流程定义'
          this.processStatusType = 'success'
          console.log('[LeaveApplicationView] 找到请假流程定义:', leaveProcess.key || leaveProcess.name)
          return leaveProcess
        } else {
          this.hasProcessDefinition = false
          this.processStatus = '未找到请假流程定义'
          this.processStatusType = 'warning'
          console.warn('[LeaveApplicationView] 未找到请假流程定义')
          return null
        }
      } catch (error) {
        console.error('[LeaveApplicationView] 查找请假流程定义发生错误:', error)
        this.hasProcessDefinition = false
        this.processStatus = '查找流程定义出错: ' + (error.message || '未知错误')
        this.processStatusType = 'error'
        return null
      }
    },
    
    // 重置表单
    resetForm() {
      console.log('[LeaveApplicationView] 重置表单...')
      if (this.$refs.leaveFormRef) {
        this.$refs.leaveFormRef.resetFields()
        // 重置特定字段，确保安全访问用户属性
        const user = this.currentUser || {};
        this.leaveForm.applicantName = user.name || user.username || 'zhangsan';
        this.leaveForm.department = user.department || '';
        this.leaveForm.position = user.position || '';
      }
      console.log('[LeaveApplicationView] 表单重置完成')
    },
    
    // 取消操作
    handleCancel() {
      console.log('[LeaveApplicationView] 取消操作...')
      this.$confirm('确定要取消当前申请吗？已填写的数据将会丢失。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        console.log('[LeaveApplicationView] 用户确认取消，跳转到任务列表')
        this.$router.push('/tasks')
      }).catch(() => {
        console.log('[LeaveApplicationView] 用户取消操作，留在当前页面')
        // 用户取消操作，留在当前页面
      })
    },
  },
  watch: {
    // 监听日期变化，自动计算请假天数
    'leaveForm.startDate': function() {
      console.log('[LeaveApplicationView] 开始日期变化，检查是否需要计算天数')
      if (this.leaveForm.endDate) {
        this.calculateLeaveDays()
      }
    },
    'leaveForm.endDate': function() {
      console.log('[LeaveApplicationView] 结束日期变化，检查是否需要计算天数')
      if (this.leaveForm.startDate) {
        this.calculateLeaveDays()
      }
    }
  }
}
</script>

<style scoped>
.leave-application-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.leave-form {
  max-width: 800px;
  margin: 0 auto;
}

/* 表单元素间距调整 */
.el-form-item {
  margin-bottom: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .leave-application-container {
    padding: 10px;
  }
  
  .leave-form {
    max-width: 100%;
  }
}
  /* 流程状态提示样式 */
  .process-status {
    margin-bottom: 20px;
  }
  
  /* 手动部署按钮区域样式 */
  .deploy-section {
    margin-bottom: 20px;
    display: flex;
    justify-content: center;
  }
</style>