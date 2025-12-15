<template>
  <div class="workflow-template-container">
    <h3 class="section-title">流程模板管理</h3>
    
    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
      <!-- 已部署流程Tab -->
      <el-tab-pane label="已部署流程" name="deployed">
        <el-card shadow="hover" class="filter-card">
          <div class="filter-section">
            <div class="filter-left">
              <el-button type="success" @click="showDeployDialog = true">
                <el-icon><Upload /></el-icon> 部署流程
              </el-button>
              <el-button @click="getProcessDefinitions">
                <el-icon><Refresh /></el-icon> 刷新
              </el-button>
              <el-button type="warning" @click="goToProcessDesigner">
                <el-icon><Edit /></el-icon> 流程设计
              </el-button>
            </div>
          </div>
        </el-card>
        
        <el-card shadow="hover" class="template-card">
          <el-table
            :data="processDefinitions"
            border
            style="width: 100%"
            v-loading="processDefLoading"
          >
            <el-table-column prop="id" label="流程ID" min-width="200" show-overflow-tooltip />
            <el-table-column prop="name" label="流程名称" min-width="150" />
            <el-table-column prop="key" label="流程标识" min-width="150" />
            <el-table-column prop="version" label="版本" width="80" />
            <el-table-column prop="deploymentId" label="部署ID" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.suspended ? 'danger' : 'success'" size="small">
                  {{ scope.row.suspended ? '已挂起' : '已激活' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewProcessDiagram(scope.row)">
                  <el-icon><Picture /></el-icon> 流程图
                </el-button>
                <el-button 
                  type="warning" 
                  size="small" 
                  @click="handleToggleProcessStatus(scope.row)"
                >
                  <el-icon><component :is="scope.row.suspended ? 'Check' : 'Close'" /></el-icon>
                  {{ scope.row.suspended ? '激活' : '挂起' }}
                </el-button>
                <el-button type="danger" size="small" @click="handleDeleteProcess(scope.row)">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination">
            <el-pagination
              v-model:current-page="processDefPage"
              v-model:page-size="processDefPageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="processDefTotal"
              @size-change="handleProcessDefSizeChange"
              @current-change="handleProcessDefPageChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 流程模板Tab -->
      <el-tab-pane label="流程模板" name="templates">
        <el-card shadow="hover" class="filter-card">
          <div class="filter-section">
            <div class="filter-left">
              <el-button type="primary" @click="handleCreateTemplate">
                <el-icon><Plus /></el-icon> 创建流程模板
              </el-button>
              <el-button type="warning" @click="goToProcessDesigner">
                <el-icon><Edit /></el-icon> 流程设计
              </el-button>
              <el-button type="danger" @click="handleBatchDelete" :disabled="selectedTemplates.length === 0">
                <el-icon><Delete /></el-icon> 批量删除
              </el-button>
            </div>
            
            <div class="filter-right">
              <el-input
                v-model="filterForm.templateName"
                placeholder="模板名称"
                clearable
                class="filter-input"
                @keyup.enter="handleSearch"
              />
              
              <el-select
                v-model="filterForm.status"
                placeholder="状态"
                class="filter-select"
                clearable
              >
                <el-option label="未发布" value="draft" />
                <el-option label="已发布" value="published" />
                <el-option label="已下架" value="offline" />
              </el-select>
              
              <el-date-picker
                v-model="filterForm.timeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                class="filter-date"
              />
              
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
              
              <el-button @click="handleResetFilter">重置</el-button>
            </div>
          </div>
        </el-card>
        
        <el-card shadow="hover" class="template-card">
          <el-table
            :data="templates"
            border
            style="width: 100%"
            @selection-change="handleSelectionChange"
            @row-click="handleRowClick"
            row-key="id"
            v-loading="loading"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="模板ID" width="120" />
            <el-table-column prop="name" label="模板名称" width="200" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getTemplateStatusTagType(scope.row.status)" size="small">
                  {{ getTemplateStatusName(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180" />
            <el-table-column prop="updatedAt" label="最后修改时间" width="180" />
            <el-table-column prop="action" label="操作" width="320">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleEditTemplate(scope.row)">
                  <el-icon><Edit /></el-icon> 编辑
                </el-button>
                <el-button
                  type="success"
                  size="small"
                  @click="handlePublish(scope.row)"
                  :disabled="scope.row.status !== 'draft'"
                >
                  <el-icon><Check /></el-icon> 发布
                </el-button>
                <el-button
                  type="warning"
                  size="small"
                  @click="handleOffline(scope.row)"
                  :disabled="scope.row.status !== 'published'"
                >
                  <el-icon><Close /></el-icon> 下架
                </el-button>
                <el-button type="danger" size="small" @click="handleDeleteTemplate(scope.row)">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
                <el-button type="info" size="small" @click="handlePreview(scope.row)">
                  <el-icon><View /></el-icon> 预览
                </el-button>
                <el-button type="primary" size="small" @click="handleViewDiagram(scope.row)" v-if="scope.row.processDefinitionId">
                  <el-icon><Picture /></el-icon> 流程图
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="totalTemplates"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 创建/编辑模板对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form :model="templateForm" label-width="100px" :rules="rules" ref="templateFormRef">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input v-model="templateForm.description" placeholder="请输入模板描述" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="流程JSON" prop="processJson">
          <el-input v-model="templateForm.processJson" placeholder="请输入流程JSON" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 预览模板对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="模板预览"
      width="600px"
    >
      <div class="preview-content">
        <div class="preview-item">
          <label>模板名称：</label>
          <span>{{ previewTemplate.name }}</span>
        </div>
        <div class="preview-item">
          <label>模板状态：</label>
          <el-tag :type="getTemplateStatusTagType(previewTemplate.status)" size="small">
            {{ getTemplateStatusName(previewTemplate.status) }}
          </el-tag>
        </div>
        <div class="preview-item">
          <label>创建时间：</label>
          <span>{{ previewTemplate.createdAt }}</span>
        </div>
        <div class="preview-item">
          <label>最后修改：</label>
          <span>{{ previewTemplate.updatedAt }}</span>
        </div>
        <div class="preview-item">
          <label>模板描述：</label>
          <p>{{ previewTemplate.description || '无描述' }}</p>
        </div>
        <div class="preview-item">
          <label>流程JSON：</label>
          <pre>{{ previewTemplate.processJson }}</pre>
        </div>
      </div>
    </el-dialog>
    
    <!-- 部署流程对话框 -->
    <el-dialog
      v-model="showDeployDialog"
      title="部署流程定义"
      width="500px"
      @close="handleDeployDialogClose"
    >
      <el-form :model="deployForm" :rules="deployRules" ref="deployFormRef" label-width="100px">
        <el-form-item label="部署名称" prop="deploymentName">
          <el-input v-model="deployForm.deploymentName" placeholder="请输入部署名称"></el-input>
        </el-form-item>
        <el-form-item label="BPMN文件" prop="file">
          <el-upload
              v-model:file-list="fileList"
              class="upload-demo"
              :auto-upload="false"
              :on-change="handleFileChange"
              accept="application/xml,.bpmn,.bpmn20.xml"
              :before-upload="beforeUpload"
              drag
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                点击或拖拽文件到此区域上传
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  只能上传 .bpmn 或 .bpmn20.xml 格式的文件
                </div>
              </template>
            </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDeployDialogClose">取消</el-button>
          <el-button type="primary" @click="handleDeploy" :loading="deployLoading">部署</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 流程图预览对话框 -->
    <el-dialog
      v-model="diagramVisible"
      :title="`${selectedProcessDef?.name || ''}流程图`"
      width="900px"
      top="5vh"
    >
      <div class="diagram-container" v-loading="diagramLoading">
        <div class="process-chart-wrapper">
          <h4 class="section-subtitle">流转节点图</h4>
          <div class="chart-content">
            <ProcessFlowChart 
              :process-definition-id="selectedProcessDef?.id || ''"
              :process-name="selectedProcessDef?.name || ''"
              :nodes="processNodes"
              :links="processLinks"
            />
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="diagramVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Delete, Search, Edit, Check, Close, View, Upload, UploadFilled, Picture, Refresh 
} from '@element-plus/icons-vue'
import axios from '@/utils/axiosConfig'
import ProcessFlowChart from '@/components/ProcessFlowChart.vue'

// 路由实例
const router = useRouter()

// Tab切换
const activeTab = ref('deployed')

// ========== 已部署流程相关数据 ==========
const processDefinitions = ref([])
const processDefLoading = ref(false)
const processDefPage = ref(1)
const processDefPageSize = ref(10)
const processDefTotal = ref(0)

// 筛选条件
const filterForm = reactive({
  templateName: '',
  status: '',
  timeRange: []
})

// 模板列表数据
const templates = ref([])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const totalTemplates = ref(0)
const selectedTemplates = ref([])
const loading = ref(false)

// 对话框相关
const dialogVisible = ref(false)
const previewVisible = ref(false)
const dialogTitle = ref('')
const templateFormRef = ref()
const isEditMode = ref(false)

// 部署流程相关
const showDeployDialog = ref(false)
const deployFormRef = ref(null)
const fileList = ref([])
const deployLoading = ref(false)
const deployForm = reactive({
  deploymentName: '',
  file: null
})
const deployRules = {
  deploymentName: [
    { required: true, message: '请输入部署名称', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请选择BPMN文件', trigger: 'change' }
  ]
}

// 流程图预览相关
const diagramVisible = ref(false)
const diagramUrl = ref('')
const diagramLoading = ref(false)
const selectedProcessDef = ref(null)
const processNodes = ref([])
const processLinks = ref([])

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 1, max: 50, message: '模板名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  processJson: [
    { required: true, message: '请输入流程JSON', trigger: 'blur' }
  ]
}

// 模板表单数据
const templateForm = reactive({
  id: '',
  name: '',
  description: '',
  processJson: '',
  status: 'draft'
})

// 预览模板数据
const previewTemplate = reactive({
  id: '',
  name: '',
  status: '',
  createdAt: '',
  updatedAt: '',
  description: '',
  processJson: ''
})

// 获取模板列表
const getTemplates = async () => {
  try {
    loading.value = true
    
    // 构建请求参数
    const params = {
      templateName: filterForm.templateName,
      status: filterForm.status,
      page: currentPage.value,
      size: pageSize.value
    }
    
    // 添加时间范围参数
    if (filterForm.timeRange && filterForm.timeRange.length === 2) {
      params.startTime = filterForm.timeRange[0]
      params.endTime = filterForm.timeRange[1]
    }
    
    // 调用后端API获取模板列表
    const response = await axios.get('/api/v1/workflow/template', { params })
    
    // 更新模板数据 - 响应拦截器返回的是success字段为true的data，失败时会抛出错误
    templates.value = response.data || []
    totalTemplates.value = response.total || 0
    
  } catch (error) {
    console.error('获取模板列表失败:', error)
    ElMessage.error('获取模板列表失败: ' + (error.message || '请稍后重试'))
  } finally {
    loading.value = false
  }
}

// 跳转到流程设计器页面
const goToProcessDesigner = () => {
  router.push('/tenant/workflow/designer')
}

// 组件初始化
onMounted(() => {
  if (activeTab.value === 'deployed') {
    getProcessDefinitions()
  } else {
    getTemplates()
  }
})

// Tab切换处理
const handleTabChange = (tabName) => {
  if (tabName === 'deployed') {
    getProcessDefinitions()
  } else {
    getTemplates()
  }
}

// ========== 已部署流程相关方法 ==========

// 获取已部署的流程定义列表
const getProcessDefinitions = async () => {
  try {
    processDefLoading.value = true
    
    const response = await axios.get('/api/v1/workflow/process/definition', {
      params: {
        page: processDefPage.value,
        size: processDefPageSize.value
      }
    })
    
    // 处理响应数据
    if (response.rows) {
      processDefinitions.value = response.rows
      processDefTotal.value = response.total || 0
    } else if (response.data) {
      processDefinitions.value = response.data
      processDefTotal.value = response.total || 0
    } else if (Array.isArray(response)) {
      processDefinitions.value = response
      processDefTotal.value = response.length
    } else {
      processDefinitions.value = []
      processDefTotal.value = 0
    }
    
  } catch (error) {
    console.error('获取流程定义列表失败:', error)
    ElMessage.error('获取流程定义列表失败: ' + (error.message || '请稍后重试'))
  } finally {
    processDefLoading.value = false
  }
}

// 流程定义分页处理
const handleProcessDefSizeChange = (size) => {
  processDefPageSize.value = size
  processDefPage.value = 1
  getProcessDefinitions()
}

const handleProcessDefPageChange = (page) => {
  processDefPage.value = page
  getProcessDefinitions()
}

// 查看流程定义的流程图
const handleViewProcessDiagram = async (row) => {
  try {
    diagramLoading.value = true
    diagramVisible.value = true
    selectedProcessDef.value = row
    
    // 根据流程定义生成节点和连线数据
    generateProcessData(row)
    
  } catch (error) {
    console.error('获取流程图失败:', error)
    ElMessage.error('获取流程图失败: ' + (error.message || '请稍后重试'))
  } finally {
    diagramLoading.value = false
  }
}

// 根据流程定义生成节点和连线数据
const generateProcessData = (processDefinition) => {
  // 清空旧数据
  processNodes.value = []
  processLinks.value = []
  
  if (processDefinition && processDefinition.key) {
    const processKey = processDefinition.key.toLowerCase()
    
    // 请假流程
    if (processKey.includes('leave')) {
      processNodes.value = [
        { id: 'start', name: '开始', type: 'start' },
        { id: 'apply', name: '提交申请', type: 'userTask' },
        { id: 'manager', name: '经理审批', type: 'userTask' },
        { id: 'hr', name: 'HR审批', type: 'userTask' },
        { id: 'end', name: '结束', type: 'end' }
      ]
      processLinks.value = [
        { source: 'start', target: 'apply' },
        { source: 'apply', target: 'manager' },
        { source: 'manager', target: 'hr' },
        { source: 'hr', target: 'end' }
      ]
    }
    // 员工入职流程
    else if (processKey.includes('onboard') || processKey.includes('employee')) {
      processNodes.value = [
        { id: 'start', name: '开始', type: 'start' },
        { id: 'submit', name: '提交入职申请', type: 'userTask' },
        { id: 'hr_check', name: 'HR审核', type: 'userTask' },
        { id: 'dept_approve', name: '部门经理审批', type: 'userTask' },
        { id: 'it_setup', name: 'IT账号开通', type: 'userTask' },
        { id: 'end', name: '结束', type: 'end' }
      ]
      processLinks.value = [
        { source: 'start', target: 'submit' },
        { source: 'submit', target: 'hr_check' },
        { source: 'hr_check', target: 'dept_approve' },
        { source: 'dept_approve', target: 'it_setup' },
        { source: 'it_setup', target: 'end' }
      ]
    }
    // 报销流程
    else if (processKey.includes('expense') || processKey.includes('reimburse')) {
      processNodes.value = [
        { id: 'start', name: '开始', type: 'start' },
        { id: 'apply', name: '提交报销', type: 'userTask' },
        { id: 'manager', name: '经理审批', type: 'userTask' },
        { id: 'finance', name: '财务审核', type: 'userTask' },
        { id: 'pay', name: '财务打款', type: 'userTask' },
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
    // 其他流程
    else {
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
  }
}

// 切换流程定义状态（挂起/激活）
const handleToggleProcessStatus = async (process) => {
  try {
    let res
    if (process.suspended) {
      // 激活流程
      res = await axios.put(`/api/v1/workflow/process/definition/${process.id}/activate`)
    } else {
      // 挂起流程
      res = await axios.put(`/api/v1/workflow/process/definition/${process.id}/suspend`)
    }
    
    ElMessage.success(process.suspended ? '流程已激活' : '流程已挂起')
    getProcessDefinitions()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败: ' + (error.message || '请稍后重试'))
  }
}

// 删除流程定义
const handleDeleteProcess = (process) => {
  ElMessageBox.confirm(
    '确定要删除此流程定义吗？此操作可能会影响正在运行的流程实例。',
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await axios.delete(`/api/v1/workflow/process/definition/${process.id}`, {
        params: { cascade: true }
      })
      ElMessage.success('流程定义已删除')
      getProcessDefinitions()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '请稍后重试'))
    }
  }).catch(() => {})
}

// 模板状态映射
const getTemplateStatusName = (status) => {
  const statusMap = {
    draft: '未发布',
    published: '已发布',
    offline: '已下架'
  }
  return statusMap[status] || status
}

// 模板状态标签样式
const getTemplateStatusTagType = (status) => {
  const statusMap = {
    draft: 'info',
    published: 'success',
    offline: 'warning'
  }
  return statusMap[status] || 'info'
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getTemplates()
}

// 重置筛选条件
const handleResetFilter = () => {
  filterForm.templateName = ''
  filterForm.status = ''
  filterForm.timeRange = []
  getTemplates()
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedTemplates.value.length === 0) {
    ElMessage.warning('请选择要删除的模板')
    return
  }
  
  // 弹出确认对话框
  ElMessageBox.confirm('确定要删除选中的模板吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 批量删除模板
      for (const template of selectedTemplates.value) {
        await axios.delete(`/api/v1/workflow/template/${template.id}`)
      }
      
      ElMessage.success(`已删除${selectedTemplates.value.length}个模板`)
      selectedTemplates.value = []
      getTemplates() // 刷新列表
    } catch (error) {
      console.error('批量删除模板失败:', error)
      ElMessage.error('批量删除模板失败: ' + (error.message || '请稍后重试'))
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 选择模板
const handleSelectionChange = (selection) => {
  selectedTemplates.value = selection
}

// 行点击
const handleRowClick = (row) => {
  // 可以在这里处理行点击事件
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getTemplates()
}

// 页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  getTemplates()
}

// 创建模板
const handleCreateTemplate = () => {
  // 重置表单
  resetForm()
  dialogTitle.value = '创建流程模板'
  isEditMode.value = false
  dialogVisible.value = true
}

// 编辑模板
const handleEditTemplate = (row) => {
  // 填充表单数据
  Object.assign(templateForm, row)
  dialogTitle.value = '编辑流程模板'
  isEditMode.value = true
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  templateForm.id = ''
  templateForm.name = ''
  templateForm.description = ''
  templateForm.processJson = ''
  templateForm.status = 'draft'
  if (templateFormRef.value) {
    templateFormRef.value.resetFields()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!templateFormRef.value) return
  
  try {
    await templateFormRef.value.validate()
    
    if (isEditMode.value) {
      // 更新模板
      await axios.put(`/api/v1/workflow/template/${templateForm.id}`, templateForm)
      ElMessage.success('模板更新成功')
    } else {
      // 创建模板
      await axios.post('/api/v1/workflow/template', templateForm)
      ElMessage.success('模板创建成功')
    }
    
    // 关闭对话框
    dialogVisible.value = false
    // 刷新列表
    getTemplates()
  } catch (error) {
    if (error.name === 'Error') {
      // 表单验证通过，但API请求失败
      console.error(isEditMode.value ? '更新模板失败:' : '创建模板失败:', error)
      ElMessage.error((isEditMode.value ? '更新模板失败: ' : '创建模板失败: ') + (error.message || '请稍后重试'))
    }
    // 表单验证失败会自动处理，不需要额外处理
  }
}

// 发布模板
const handlePublish = (row) => {
  // 弹出确认对话框
  ElMessageBox.confirm(`确定要发布模板"${row.name}"吗？`, '发布确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
        await axios.post(`/api/v1/workflow/template/${row.id}/publish`)
        ElMessage.success(`已发布模板：${row.name}`)
        getTemplates() // 刷新列表
      } catch (error) {
        console.error('发布模板失败:', error)
        ElMessage.error('发布模板失败: ' + (error.message || '请稍后重试'))
      }
  }).catch(() => {
    // 用户取消发布
  })
}

// 下架模板
const handleOffline = (row) => {
  // 弹出确认对话框
  ElMessageBox.confirm(`确定要下架模板"${row.name}"吗？`, '下架确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
        await axios.post(`/api/v1/workflow/template/${row.id}/offline`)
        ElMessage.success(`已下架模板：${row.name}`)
        getTemplates() // 刷新列表
      } catch (error) {
        console.error('下架模板失败:', error)
        ElMessage.error('下架模板失败: ' + (error.message || '请稍后重试'))
      }
  }).catch(() => {
    // 用户取消下架
  })
}

// 删除模板
const handleDeleteTemplate = (row) => {
  // 弹出确认对话框
  ElMessageBox.confirm(`确定要删除模板"${row.name}"吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
        await axios.delete(`/api/v1/workflow/template/${row.id}`)
        ElMessage.success(`已删除模板：${row.name}`)
        getTemplates() // 刷新列表
      } catch (error) {
        console.error('删除模板失败:', error)
        ElMessage.error('删除模板失败: ' + (error.message || '请稍后重试'))
      }
  }).catch(() => {
    // 用户取消删除
  })
}

// 预览模板
const handlePreview = (row) => {
  // 填充预览数据
  Object.assign(previewTemplate, row)
  previewVisible.value = true
}

// ========== 部署流程相关方法 ==========

// 处理文件变化
const handleFileChange = (file) => {
  deployForm.file = file.raw
  fileList.value = [file]
}

// 文件上传前的验证
const beforeUpload = (file) => {
  const extension = file.name.split('.').pop().toLowerCase()
  const isAllowed = extension === 'bpmn' || (extension === 'xml' && file.name.includes('.bpmn20.'))
  if (!isAllowed) {
    ElMessage.error('只能上传 .bpmn 或 .bpmn20.xml 格式的文件!')
  }
  return isAllowed
}

// 部署流程
const handleDeploy = async () => {
  try {
    await deployFormRef.value.validate()
    
    if (!deployForm.file) {
      ElMessage.warning('请选择BPMN文件')
      return
    }
    
    deployLoading.value = true
    
    const formData = new FormData()
    formData.append('file', deployForm.file)
    formData.append('name', deployForm.deploymentName)
    
    const res = await axios.post('/api/v1/workflow/process/definition/deploy', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    ElMessage.success('流程部署成功')
    showDeployDialog.value = false
    handleDeployDialogClose()
    getProcessDefinitions() // 刷新已部署流程列表
  } catch (error) {
    console.error('部署流程失败:', error)
    ElMessage.error('流程部署失败: ' + (error.message || '未知错误'))
  } finally {
    deployLoading.value = false
  }
}

// 关闭部署对话框
const handleDeployDialogClose = () => {
  showDeployDialog.value = false
  deployFormRef.value?.resetFields()
  fileList.value = []
  deployForm.deploymentName = ''
  deployForm.file = null
}

// ========== 查看流程图相关方法 ==========

// 查看流程图（流程模板）
const handleViewDiagram = async (row) => {
  try {
    diagramLoading.value = true
    diagramVisible.value = true
    
    // 构造一个类似流程定义的对象
    selectedProcessDef.value = {
      id: row.processDefinitionId || row.id,
      key: row.processDefinitionKey || row.key || '',
      name: row.name || '流程模板'
    }
    
    // 根据流程定义生成节点和连线数据
    generateProcessData(selectedProcessDef.value)
    
  } catch (error) {
    console.error('获取流程图失败:', error)
    ElMessage.error('获取流程图失败: ' + (error.message || '请稍后重试'))
  } finally {
    diagramLoading.value = false
  }
}
</script>

<style scoped>
.workflow-template-container {
  padding: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-left {
  display: flex;
  gap: 10px;
}

.filter-right {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-input {
  width: 200px;
}

.filter-select {
  width: 150px;
}

.filter-date {
  width: 300px;
}

.template-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 预览对话框样式 */
.preview-content {
  padding: 10px 0;
}

.preview-item {
  margin-bottom: 15px;
  line-height: 1.5;
}

.preview-item label {
  font-weight: 500;
  margin-right: 8px;
  color: #303133;
}

.preview-item pre {
  margin: 8px 0 0 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.4;
}

.preview-item p {
  margin: 0;
  word-break: break-word;
}

/* 部署对话框样式 */
:deep(.el-upload-dragger) {
  width: 100% !important;
}

.upload-demo {
  width: 100%;
}

/* 流程图预览样式 */
.diagram-container {
  min-height: 500px;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.process-chart-wrapper {
  width: 100%;
}

.section-subtitle {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.chart-content {
  height: 500px;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  background-color: #fafafa;
}

.diagram-image {
  max-width: 100%;
  height: auto;
}
</style>