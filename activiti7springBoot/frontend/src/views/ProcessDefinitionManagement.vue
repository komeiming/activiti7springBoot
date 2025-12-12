<template>
  <div class="process-definition-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程定义管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="showDeployDialog = true">
              <el-icon><Upload /></el-icon>
              部署流程
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="流程名称">
            <el-input v-model="searchForm.name" placeholder="请输入流程名称"></el-input>
          </el-form-item>
          <el-form-item label="流程状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" value=""></el-option>
              <el-option label="激活" value="active"></el-option>
              <el-option label="挂起" value="suspended"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 流程定义列表 -->
      <el-table :data="processDefinitions" style="width: 100%">
        <el-table-column prop="id" label="ID" width="200"></el-table-column>
        <el-table-column prop="name" label="流程名称" min-width="150"></el-table-column>
        <el-table-column prop="key" label="流程标识" min-width="150"></el-table-column>
        <el-table-column prop="version" label="版本" width="80"></el-table-column>
        <el-table-column prop="deploymentId" label="部署ID" width="150"></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.suspended ? 'danger' : 'success'" size="small">
              {{ scope.row.suspended ? '已挂起' : '已激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleStartProcess(scope.row)">
              <el-icon><Operation /></el-icon> 启动流程
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="handleToggleStatus(scope.row)"
              v-if="scope.row.suspended"
            >
              <el-icon><Check /></el-icon> 激活
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="handleToggleStatus(scope.row)"
              v-else
            >
              <el-icon><Close /></el-icon> 挂起
            </el-button>
            <el-button type="danger" size="small" @click="handleDeleteProcess(scope.row)">
              <el-icon><Delete /></el-icon> 删除
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
    </el-card>

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
          <el-button type="primary" @click="handleDeploy">部署</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 启动流程对话框 -->
    <el-dialog
      v-model="showStartDialog"
      title="启动流程实例"
      width="500px"
      @close="handleStartDialogClose"
    >
      <el-form :model="startForm" :rules="startRules" ref="startFormRef" label-width="100px">
        <el-form-item label="业务标识" prop="businessKey">
          <el-input v-model="startForm.businessKey" placeholder="请输入业务标识（可选）"></el-input>
        </el-form-item>
        <el-form-item label="流程变量">
          <div v-for="(item, index) in startForm.variables" :key="index" class="variable-item">
            <el-input v-model="item.name" placeholder="变量名" style="width: 30%; margin-right: 10px;"></el-input>
            <el-input v-model="item.value" placeholder="变量值" style="width: 30%; margin-right: 10px;"></el-input>
            <el-select v-model="item.type" placeholder="类型" style="width: 25%;">
              <el-option label="字符串" value="string"></el-option>
              <el-option label="数字" value="number"></el-option>
              <el-option label="布尔值" value="boolean"></el-option>
            </el-select>
            <el-button type="danger" icon="Delete" circle size="small" @click="removeVariable(index)"></el-button>
          </div>
          <el-button type="primary" plain @click="addVariable">添加变量</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleStartDialogClose">取消</el-button>
          <el-button type="primary" @click="handleStartInstance">启动</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { Upload, Operation, Check, Close, Delete, UploadFilled } from '@element-plus/icons-vue'

export default {
  name: 'ProcessDefinitionManagement',
  components: {
    Upload,
    Operation,
    Check,
    Close,
    Delete,
    UploadFilled
  },
  setup() {
    // 响应式数据
    const processDefinitions = ref([])
    const showDeployDialog = ref(false)
    const showStartDialog = ref(false)
    const deployFormRef = ref(null)
    const startFormRef = ref(null)
    const fileList = ref([])
    const selectedProcess = ref(null)
    
    // 搜索表单
    const searchForm = reactive({
      name: '',
      status: ''
    })
    
    // 分页信息
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 部署表单
    const deployForm = reactive({
      deploymentName: '',
      file: null
    })
    
    // 启动流程表单
    const startForm = reactive({
      businessKey: '',
      variables: []
    })
    
    // 表单验证规则
    const deployRules = {
      deploymentName: [
        { required: true, message: '请输入部署名称', trigger: 'blur' }
      ],
      file: [
        { required: true, message: '请选择BPMN文件', trigger: 'change' }
      ]
    }
    
    const startRules = {
      businessKey: [
        { required: false, message: '请输入业务标识', trigger: 'blur' }
      ]
    }
    
    // 获取流程定义列表
    const getProcessDefinitions = async () => {
      try {
        const res = await axios.get('/api/process/definition/list', {
          params: {
            page: pagination.currentPage,
            pageSize: pagination.pageSize
          }
        })
        
        // 修正响应格式处理 - 后端实际返回的格式是 {total: 5, success: true, rows: [...]}
        if (res.data && res.data.success) {
          processDefinitions.value = res.data.rows || []
          pagination.total = res.data.total || 0
          console.log('成功获取流程定义数据:', processDefinitions.value)
          console.log('数据总数:', pagination.total)
        } else {
          ElMessage.error('获取流程定义列表失败: ' + (res.data?.message || '未知错误'))
        }
      } catch (error) {
        console.error('API请求错误:', error)
        ElMessage.error('获取流程定义列表失败: ' + (error.message || '网络错误'))
      }
    }
    
    // 处理文件变化
    const handleFileChange = (file) => {
      deployForm.file = file.raw
      fileList.value = [file]
    }
    
    // 文件上传前的验证
    const beforeUpload = (file) => {
      // 获取文件扩展名
      const extension = file.name.split('.').pop().toLowerCase();
      // 检查是否为允许的文件类型
      const isAllowed = extension === 'bpmn' || (extension === 'xml' && file.name.includes('.bpmn20.'));
      if (!isAllowed) {
        ElMessage.error('只能上传 .bpmn 或 .bpmn20.xml 格式的文件!');
      }
      return isAllowed;
    }
    
    // 部署流程
    const handleDeploy = async () => {
      try {
        await deployFormRef.value.validate()
        
        const formData = new FormData()
        formData.append('file', deployForm.file)
        formData.append('name', deployForm.deploymentName)
        
        const res = await axios.post('/api/process/definition/deploy', formData)
        
        if (res.data.success) {
          ElMessage.success('流程部署成功')
          showDeployDialog.value = false
          getProcessDefinitions()
        } else {
          ElMessage.error('流程部署失败: ' + (res.data.message || '未知错误'))
        }
      } catch (error) {
        if (error.name !== 'Error') {
          return
        }
        ElMessage.error('流程部署失败: ' + error.message)
      }
    }
    
    // 启动流程
    const handleStartProcess = (process) => {
      selectedProcess.value = process
      startForm.businessKey = ''
      startForm.variables = []
      showStartDialog.value = true
    }
    
    // 添加变量
    const addVariable = () => {
      startForm.variables.push({
        name: '',
        value: '',
        type: 'string'
      })
    }
    
    // 移除变量
    const removeVariable = (index) => {
      startForm.variables.splice(index, 1)
    }
    
    // 启动流程实例
    const handleStartInstance = async () => {
      try {
        // 转换变量类型
        const variables = {}
        startForm.variables.forEach(item => {
          if (item.name) {
            let value = item.value
            switch (item.type) {
              case 'number':
                value = Number(value)
                break
              case 'boolean':
                value = value === 'true' || value === true
                break
            }
            variables[item.name] = value
          }
        })
        
        // 对于员工入职流程，使用专用的API接口
        if (selectedProcess.value.key === 'employeeOnboard') {
          // 员工入职流程专用接口
          const res = await axios.post('/api/v1/onboard', {
            employeeId: variables.employeeId || '',
            employeeName: variables.employeeName || '',
            department: variables.department || '',
            position: variables.position || '',
            hireDate: variables.hireDate || new Date().toISOString().split('T')[0],
            managerName: variables.managerName || ''
          }, {
            headers: {
              'Content-Type': 'application/json'
            }
          })
          
          if (res.data.code === 200) {
            ElMessage.success('流程实例启动成功，实例ID：' + res.data.data.processInstanceId)
            showStartDialog.value = false
          } else {
            ElMessage.error('流程实例启动失败: ' + (res.data.message || '未知错误'))
          }
          return
        }
        
        // 其他流程使用通用接口
        const res = await axios.post('/api/process/definition/start', variables, {
          params: {
            processDefinitionKey: selectedProcess.value.key,
            businessKey: startForm.businessKey
          },
          headers: {
            'Content-Type': 'application/json'
          }
        })
        
        // 处理通用流程接口的响应
        if (res.data.code === 200) {
          ElMessage.success('流程实例启动成功，实例ID：' + res.data.data.processInstanceId)
          showStartDialog.value = false
        } else {
          ElMessage.error('流程实例启动失败: ' + (res.data.message || '未知错误'))
        }
      } catch (error) {
        console.error('启动流程错误详情:', error)
        ElMessage.error('流程实例启动失败: ' + error.message)
      }
    }
    
    // 切换流程状态（挂起/激活）
    const handleToggleStatus = async (process) => {
      try {
        let res
        if (process.suspended) {
          // 激活流程
          res = await axios.put(`/api/process/definition/${process.id}/activate`)
        } else {
          // 挂起流程
          res = await axios.put(`/api/process/definition/${process.id}/suspend`)
        }
        
        if (res.data.success) {
          ElMessage.success(res.data.message)
          // 刷新列表
          getProcessDefinitions()
        } else {
          ElMessage.error((res.data.message || '操作失败'))
        }
      } catch (error) {
        ElMessage.error('操作失败: ' + error.message)
      }
    }
    
    // 删除流程定义
    const handleDeleteProcess = async (process) => {
      ElMessageBox.confirm(
        '确定要删除此流程定义吗？此操作可能会影响正在运行的流程实例。是否同时删除相关的历史数据？',
        '警告',
        {
          confirmButtonText: '级联删除',
          cancelButtonText: '仅删除定义',
          type: 'warning',
          distinguishCancelAndClose: true,
          closeOnClickModal: false
        }
      )
        .then(async () => {
          // 级联删除
          await deleteProcessDefinition(process, true)
        })
        .catch(action => {
          if (action === 'cancel') {
            // 仅删除定义
            deleteProcessDefinition(process, false)
          }
        })
    }
    
    // 执行删除操作
    const deleteProcessDefinition = async (process, cascade) => {
      try {
        const res = await axios.delete(`/api/process/definition/deployment/${process.deploymentId}`, {
          params: { cascade }
        })
        
        if (res.data.success) {
          ElMessage.success(res.data.message)
          getProcessDefinitions()
        } else {
          ElMessage.error((res.data.message || '删除失败'))
        }
      } catch (error) {
        ElMessage.error('删除失败: ' + error.message)
      }
    }
    
    // 关闭部署对话框
    const handleDeployDialogClose = () => {
      showDeployDialog.value = false
      deployFormRef.value?.resetFields()
      fileList.value = []
      deployForm.file = null
    }
    
    // 关闭启动对话框
    const handleStartDialogClose = () => {
      showStartDialog.value = false
      selectedProcess.value = null
    }
    
    // 搜索
    const handleSearch = () => {
      pagination.currentPage = 1
      getProcessDefinitions()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchForm.name = ''
      searchForm.status = ''
      pagination.currentPage = 1
      getProcessDefinitions()
    }
    
    // 分页处理
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      getProcessDefinitions()
    }
    
    const handleCurrentChange = (current) => {
      pagination.currentPage = current
      getProcessDefinitions()
    }
    
    // 初始化
    onMounted(() => {
      getProcessDefinitions()
    })
    
    return {
      processDefinitions,
      showDeployDialog,
      showStartDialog,
      deployFormRef,
      startFormRef,
      deployForm,
      startForm,
      deployRules,
      startRules,
      fileList,
      searchForm,
      pagination,
      handleFileChange,
      beforeUpload,
      handleDeploy,
      handleStartProcess,
      addVariable,
      removeVariable,
      handleStartInstance,
      handleToggleStatus,
      handleDeleteProcess,
      handleDeployDialogClose,
      handleStartDialogClose,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.process-definition-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.variable-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}

:deep(.el-upload-dragger) {
  width: 100% !important;
}
</style>