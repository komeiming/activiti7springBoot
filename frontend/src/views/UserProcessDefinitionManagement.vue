<template>
  <div class="process-definition-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程定义管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="openDeployDialog">部署流程</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.key"
              placeholder="流程标识"
              clearable
              @keyup.enter="handleSearch"
            ></el-input>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="searchForm.name"
              placeholder="流程名称"
              clearable
              @keyup.enter="handleSearch"
            ></el-input>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchForm.suspended"
              placeholder="状态"
              clearable
            >
              <el-option label="激活" :value="false"></el-option>
              <el-option label="挂起" :value="true"></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 流程定义列表 -->
      <div class="process-definition-list">
        <el-table
          v-loading="loading"
          :data="processDefinitionsData"
          style="width: 100%"
          border
        >
          <el-table-column prop="id" label="流程定义ID" width="200" show-overflow-tooltip></el-table-column>
          <el-table-column prop="name" label="流程名称" width="180"></el-table-column>
          <el-table-column prop="key" label="流程标识" width="180"></el-table-column>
          <el-table-column prop="version" label="版本" width="80"></el-table-column>
          <el-table-column prop="resourceName" label="资源名称" show-overflow-tooltip></el-table-column>
          <el-table-column prop="deploymentId" label="部署ID" width="180" show-overflow-tooltip></el-table-column>
          <el-table-column prop="suspended" label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.suspended ? 'warning' : 'success'">
                {{ scope.row.suspended ? '挂起' : '激活' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              <el-button
                v-if="!scope.row.suspended"
                size="small"
                type="primary"
                @click="openStartProcessDialog(scope.row)"
              >
                启动流程
              </el-button>
              <el-button
                v-else
                size="small"
                type="primary"
                disabled
              >
                启动流程
              </el-button>
              <el-button
                size="small"
                @click="scope.row.suspended ? activateProcess(scope.row) : suspendProcess(scope.row)"
              >
                {{ scope.row.suspended ? '激活' : '挂起' }}
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="deleteProcessDefinition(scope.row)"
              >
                删除
              </el-button>
              <el-button
                size="small"
                @click="viewProcessImage(scope.row)"
              >
                查看图
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </div>
    </el-card>

    <!-- 部署流程对话框 -->
    <el-dialog
      v-model="deployDialogVisible"
      title="部署流程"
      width="500px"
    >
      <el-form :model="deployForm" label-width="100px">
        <el-form-item label="部署名称" prop="name">
          <el-input v-model="deployForm.name" placeholder="请输入部署名称"></el-input>
        </el-form-item>
        <el-form-item label="流程文件" prop="file">
          <el-upload
            class="upload-demo"
            ref="uploadRef"
            :auto-upload="false"
            :file-list="fileList"
            accept=".bpmn,.bpmn20.xml"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传 .bpmn 或 .bpmn20.xml 格式的文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeDeployDialog">取消</el-button>
          <el-button type="primary" @click="submitDeploy">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 启动流程对话框 -->
    <el-dialog
      v-model="startProcessDialogVisible"
      :title="`启动${selectedProcessDefinition?.name || ''}流程`"
      width="500px"
    >
      <el-form :model="startProcessForm" label-width="100px">
        <el-form-item label="业务标识" prop="businessKey">
          <el-input v-model="startProcessForm.businessKey" placeholder="请输入业务标识（可选）"></el-input>
        </el-form-item>
        <el-form-item label="流程变量">
          <el-button type="text" @click="addProcessVariable">添加变量</el-button>
          <div v-for="(variable, index) in startProcessForm.variables" :key="index" class="process-variable-item">
            <el-input
              v-model="variable.name"
              placeholder="变量名"
              style="width: 30%; margin-right: 10px"
            ></el-input>
            <el-input
              v-model="variable.value"
              placeholder="变量值"
              style="width: 30%; margin-right: 10px"
            ></el-input>
            <el-select
              v-model="variable.type"
              placeholder="类型"
              style="width: 20%; margin-right: 10px"
            >
              <el-option label="字符串" value="string"></el-option>
              <el-option label="数字" value="integer"></el-option>
              <el-option label="布尔值" value="boolean"></el-option>
            </el-select>
            <el-button type="danger" icon="el-icon-delete" circle @click="removeProcessVariable(index)"></el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeStartProcessDialog">取消</el-button>
          <el-button type="primary" @click="submitStartProcess">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 流程图片查看对话框 -->
    <el-dialog
      v-model="processImageDialogVisible"
      :title="`${selectedProcessDefinition?.name || ''}流程图`"
      width="1000px"
    >
      <div class="process-image-container">
        <div v-if="processImageUrl" class="process-image-wrapper">
          <h4 class="section-title">原始流程图</h4>
          <div class="image-content">
            <img :src="processImageUrl" alt="流程图" class="process-image" />
          </div>
        </div>
        <div class="process-chart-wrapper">
          <h4 class="section-title">流转节点图</h4>
          <div class="chart-content">
            <ProcessFlowChart 
              :process-definition-id="selectedProcessDefinition?.id || ''"
              :process-name="selectedProcessDefinition?.name || ''"
              :nodes="processNodes"
              :links="processLinks"
            />
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processImageDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import ProcessDefinitionService from '../services/ProcessDefinitionService'
import UserService from '../services/UserService'
import ProcessFlowChart from '../components/ProcessFlowChart.vue'

export default {
  name: 'UserProcessDefinitionManagement',
  components: {
    ProcessFlowChart
  },
  setup() {
    // 响应式数据
    const loading = ref(false)
    const deployDialogVisible = ref(false)
    const startProcessDialogVisible = ref(false)
    const processImageDialogVisible = ref(false)
    const uploadRef = ref(null)
    const fileList = ref([])
    const processDefinitions = ref([])
    const selectedProcessDefinition = ref(null)
    const processImageUrl = ref('')
    const currentUser = ref({})

    // 搜索表单
    const searchForm = reactive({
      key: '',
      name: '',
      suspended: ''
    })

    // 部署表单
    const deployForm = reactive({
      name: '',
      file: null
    })

    // 启动流程表单
    const startProcessForm = reactive({
      businessKey: '',
      variables: []
    })

    // 分页
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })

    // 计算当前页显示的数据
    const processDefinitionsData = computed(() => {
      return processDefinitions.value
    })

    // 加载当前用户信息
    const loadCurrentUser = () => {
      try {
        // 获取当前用户信息
        const userInfoStr = localStorage.getItem('user')
        if (userInfoStr) {
          currentUser.value = JSON.parse(userInfoStr)
        }
      } catch (error) {
        console.error('加载用户信息错误:', error)
      }
    }

    // 加载流程定义列表
    const loadProcessDefinitions = async () => {
      loading.value = true
      try {
        const response = await ProcessDefinitionService.getProcessDefinitions(
          pagination.currentPage,
          pagination.pageSize,
          {
            key: searchForm.key,
            name: searchForm.name,
            suspended: searchForm.suspended
          }
        )
        
        // 安全检查响应数据
        if (response && response.data) {
          // 处理不同格式的响应
          if (response.data.code === 200) {
            // 格式1: { code: 200, data: { data: [], total: 0 } }
            processDefinitions.value = response.data.data.data || []
            pagination.total = response.data.data.total || 0
          } else if (response.data.data && Array.isArray(response.data.data)) {
            // 格式2: { data: [], total: 0 }
            processDefinitions.value = response.data.data || []
            pagination.total = response.data.total || 0
          } else if (Array.isArray(response.data)) {
            // 格式3: []
            processDefinitions.value = response.data || []
            pagination.total = response.data.length || 0
          } else {
            ElMessage.error('加载流程定义失败: 数据格式错误')
          }
        } else if (response && Array.isArray(response)) {
          // 格式4: 直接返回数组
          processDefinitions.value = response || []
          pagination.total = response.length || 0
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

    // 搜索
    const handleSearch = () => {
      pagination.currentPage = 1
      loadProcessDefinitions()
    }

    // 重置搜索
    const resetSearch = () => {
      searchForm.key = ''
      searchForm.name = ''
      searchForm.suspended = ''
      pagination.currentPage = 1
      loadProcessDefinitions()
    }

    // 分页大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      loadProcessDefinitions()
    }

    // 当前页变化
    const handleCurrentChange = (current) => {
      pagination.currentPage = current
      loadProcessDefinitions()
    }

    // 打开部署对话框
    const openDeployDialog = () => {
      deployForm.name = ''
      fileList.value = []
      deployForm.file = null
      deployDialogVisible.value = true
    }

    // 关闭部署对话框
    const closeDeployDialog = () => {
      deployDialogVisible.value = false
    }

    // 文件变化处理
    const handleFileChange = (file, fileList) => {
      if (file.raw) {
        deployForm.file = file.raw
      }
    }

    // 提交部署
    const submitDeploy = async () => {
      if (!deployForm.file) {
        ElMessage.warning('请选择流程文件')
        return
      }

      try {
        const result = await ProcessDefinitionService.deployProcess(deployForm.file, deployForm.name)
        if (result) {
          ElMessage.success('流程部署成功')
          closeDeployDialog()
          loadProcessDefinitions()
        } else {
          ElMessage.error('流程部署失败')
        }
      } catch (error) {
        console.error('部署流程错误:', error)
        ElMessage.error('流程部署失败: ' + (error.message || '未知错误'))
      }
    }

    // 打开启动流程对话框
    const openStartProcessDialog = (processDefinition) => {
      selectedProcessDefinition.value = processDefinition
      startProcessForm.businessKey = ''
      startProcessForm.variables = []
      startProcessDialogVisible.value = true
    }

    // 关闭启动流程对话框
    const closeStartProcessDialog = () => {
      startProcessDialogVisible.value = false
    }

    // 添加流程变量
    const addProcessVariable = () => {
      startProcessForm.variables.push({
        name: '',
        value: '',
        type: 'string'
      })
    }

    // 移除流程变量
    const removeProcessVariable = (index) => {
      startProcessForm.variables.splice(index, 1)
    }

    // 提交启动流程
    const submitStartProcess = async () => {
      if (!selectedProcessDefinition.value) {
        ElMessage.warning('未选择流程定义')
        return
      }

      // 构建变量对象
      const variables = {}
      startProcessForm.variables.forEach(v => {
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

      // 自动添加当前用户信息
      variables.applicant = currentUser.value.username
      variables.applicantId = currentUser.value.id
      variables.applicantName = currentUser.value.fullName || currentUser.value.username
      variables.applicantDepartment = currentUser.value.department || ''

      try {
        const response = await ProcessDefinitionService.startProcessInstance(
          selectedProcessDefinition.value.id,
          {
            businessKey: startProcessForm.businessKey,
            variables
          }
        )

        if (response && response.success !== false) {
          ElMessage.success('流程启动成功')
          closeStartProcessDialog()
          // 可以跳转到任务列表或流程详情页
          // router.push(`/task/list?processInstanceId=${response.processInstanceId}`)
        } else {
          ElMessage.error('流程启动失败: ' + (response?.message || '未知错误'))
        }
      } catch (error) {
        console.error('启动流程错误:', error)
        ElMessage.error('流程启动失败')
      }
    }

    // 挂起流程
    const suspendProcess = async (processDefinition) => {
      ElMessageBox.confirm(`确定要挂起流程 ${processDefinition.name} 吗？`, '确认操作', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await ProcessDefinitionService.suspendProcessDefinition(processDefinition.id)
          if (response && response.success !== false) {
            ElMessage.success('流程挂起成功')
            loadProcessDefinitions()
          } else {
            ElMessage.error('流程挂起失败: ' + (response?.message || '未知错误'))
          }
        } catch (error) {
          console.error('挂起流程错误:', error)
          ElMessage.error('流程挂起失败')
        }
      }).catch(() => {
        // 取消操作
      })
    }

    // 激活流程
    const activateProcess = async (processDefinition) => {
      try {
        const response = await ProcessDefinitionService.activateProcessDefinition(processDefinition.id)
        if (response && response.success !== false) {
          ElMessage.success('流程激活成功')
          loadProcessDefinitions()
        } else {
          ElMessage.error('流程激活失败: ' + (response?.message || '未知错误'))
        }
      } catch (error) {
        console.error('激活流程错误:', error)
        ElMessage.error('流程激活失败')
      }
    }

    // 删除流程定义
    const deleteProcessDefinition = async (processDefinition) => {
      ElMessageBox.confirm(`确定要删除流程 ${processDefinition.name} 吗？删除后将无法恢复。`, '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'danger'
      }).then(async () => {
        try {
          const response = await ProcessDefinitionService.deleteProcessDefinition(processDefinition.id)
          if (response && response.success !== false) {
            ElMessage.success('流程删除成功')
            loadProcessDefinitions()
          } else {
            ElMessage.error('流程删除失败: ' + (response?.message || '未知错误'))
          }
        } catch (error) {
          console.error('删除流程错误:', error)
          ElMessage.error('流程删除失败')
        }
      }).catch(() => {
        // 取消操作
      })
    }

    // 查看流程图片
    const viewProcessImage = async (processDefinition) => {
      selectedProcessDefinition.value = processDefinition
      processImageDialogVisible.value = true
      
      // 使用getProcessDefinitionDiagram方法获取流程图，该方法会将blob转换为Data URL
      // 该方法已处理所有错误情况，不再抛出异常，而是返回null
      processImageUrl.value = '' // 清空旧图片
      const imageDataUrl = await ProcessDefinitionService.getProcessDefinitionDiagram(processDefinition.id)
      if (imageDataUrl) {
        processImageUrl.value = imageDataUrl
      } else {
        // 预期的流程图不存在情况，不显示错误提示，让前端显示空状态
        console.info('流程图不存在或无法获取，显示空状态')
      }
      
      // 生成模拟的流程节点和连线数据
      generateMockProcessData(processDefinition)
    }
    
    // 生成模拟的流程节点和连线数据
    const generateMockProcessData = (processDefinition) => {
      // 清空旧数据
      processNodes.value = []
      processLinks.value = []
      
      // 模拟不同流程的节点和连线数据
      if (processDefinition && processDefinition.key) {
        const processKey = processDefinition.key.toLowerCase()
        
        // 请假流程
        if (processKey.includes('leave')) {
          processNodes.value = [
            { id: 'start', name: '开始', type: 'start' },
            { id: 'apply', name: '请假申请', type: 'userTask' },
            { id: 'manager', name: '经理审批', type: 'userTask' },
            { id: 'hr', name: 'HR归档', type: 'userTask' },
            { id: 'end', name: '结束', type: 'end' }
          ]
          processLinks.value = [
            { source: 'start', target: 'apply' },
            { source: 'apply', target: 'manager' },
            { source: 'manager', target: 'hr' },
            { source: 'hr', target: 'end' }
          ]
        }
        // 入职流程
        else if (processKey.includes('onboard')) {
          processNodes.value = [
            { id: 'start', name: '开始', type: 'start' },
            { id: 'apply', name: '入职申请', type: 'userTask' },
            { id: 'manager', name: '部门经理审批', type: 'userTask' },
            { id: 'hr', name: 'HR审批', type: 'userTask' },
            { id: 'it', name: 'IT准备', type: 'userTask' },
            { id: 'training', name: '培训安排', type: 'userTask' },
            { id: 'end', name: '结束', type: 'end' }
          ]
          processLinks.value = [
            { source: 'start', target: 'apply' },
            { source: 'apply', target: 'manager' },
            { source: 'manager', target: 'hr' },
            { source: 'hr', target: 'it' },
            { source: 'it', target: 'training' },
            { source: 'training', target: 'end' }
          ]
        }
        // 报销流程
        else if (processKey.includes('expense') || processKey.includes('reimbursement')) {
          processNodes.value = [
            { id: 'start', name: '开始', type: 'start' },
            { id: 'apply', name: '报销申请', type: 'userTask' },
            { id: 'manager', name: '经理审批', type: 'userTask' },
            { id: 'finance', name: '财务审批', type: 'userTask' },
            { id: 'pay', name: '财务支付', type: 'serviceTask' },
            { id: 'end', name: '结束', type: 'end' }
          ]
          processLinks.value = [
            { source: 'start', target: 'apply' },
            { source: 'apply', target: 'manager' },
            { source: 'manager', target: 'finance' },
            { source: 'finance', target: 'pay' },
            { source: 'pay', target: 'end' }
          ]
        }
        // 出差流程
        else if (processKey.includes('trip') || processKey.includes('business')) {
          processNodes.value = [
            { id: 'start', name: '开始', type: 'start' },
            { id: 'apply', name: '出差申请', type: 'userTask' },
            { id: 'manager', name: '部门经理审批', type: 'userTask' },
            { id: 'finance', name: '财务审批', type: 'userTask' },
            { id: 'report', name: '出差报告', type: 'userTask' },
            { id: 'end', name: '结束', type: 'end' }
          ]
          processLinks.value = [
            { source: 'start', target: 'apply' },
            { source: 'apply', target: 'manager' },
            { source: 'manager', target: 'finance' },
            { source: 'finance', target: 'report' },
            { source: 'report', target: 'end' }
          ]
        }
        // 其他流程（根据实际流程key添加更多条件）
        else {
          // 使用流程定义名称生成更有意义的节点名称
          const processName = processDefinition.name || '流程'
          processNodes.value = [
            { id: 'start', name: '开始', type: 'start' },
            { id: 'step1', name: `${processName}-申请`, type: 'userTask' },
            { id: 'step2', name: `${processName}-审批`, type: 'userTask' },
            { id: 'step3', name: `${processName}-执行`, type: 'userTask' },
            { id: 'end', name: '结束', type: 'end' }
          ]
          processLinks.value = [
            { source: 'start', target: 'step1' },
            { source: 'step1', target: 'step2' },
            { source: 'step2', target: 'step3' },
            { source: 'step3', target: 'end' }
          ]
        }
      } else {
        // 没有流程定义，显示默认流程
        processNodes.value = [
          { id: 'start', name: '开始', type: 'start' },
          { id: 'task', name: '任务', type: 'userTask' },
          { id: 'end', name: '结束', type: 'end' }
        ]
        processLinks.value = [
          { source: 'start', target: 'task' },
          { source: 'task', target: 'end' }
        ]
      }
      
      console.info('生成的流程节点数据:', processNodes.value)
      console.info('生成的流程连线数据:', processLinks.value)
    }
    
    // 流程节点数据
    const processNodes = ref([])
    // 流程连线数据
    const processLinks = ref([])

    // 初始化
    onMounted(() => {
      loadCurrentUser()
      loadProcessDefinitions()
    })

    return {
      loading,
      deployDialogVisible,
      startProcessDialogVisible,
      processImageDialogVisible,
      uploadRef,
      fileList,
      processDefinitionsData,
      selectedProcessDefinition,
      processImageUrl,
      searchForm,
      deployForm,
      startProcessForm,
      pagination,
      processNodes,
      processLinks,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      openDeployDialog,
      closeDeployDialog,
      handleFileChange,
      submitDeploy,
      openStartProcessDialog,
      closeStartProcessDialog,
      addProcessVariable,
      removeProcessVariable,
      submitStartProcess,
      suspendProcess,
      activateProcess,
      deleteProcessDefinition,
      viewProcessImage
    }
  }
}
</script>

<style scoped>
.process-definition-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.process-definition-list {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.process-variable-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.process-image-container {
  display: flex;
  justify-content: center;
  overflow: auto;
  max-height: 600px;
}

.process-image {
  max-width: 100%;
  max-height: 600px;
}

/* 流程图和流转节点图并排显示 */
.process-image-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  padding: 10px 0;
}

.process-image-wrapper,
.process-chart-wrapper {
  flex: 1;
  min-width: 400px;
}

.section-title {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.image-content,
.chart-content {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  overflow: auto;
  max-height: 600px;
}

.image-content {
  background-color: #fafafa;
  padding: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.chart-content {
  height: 600px;
}
</style>