<template>
  <div class="process-definition-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程定义管理</span>
          <el-button type="primary" @click="showUploadDialog = true">部署新流程</el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="流程名称">
            <el-input v-model="searchForm.name" placeholder="请输入流程名称"></el-input>
          </el-form-item>
          <el-form-item label="流程版本">
            <el-input-number v-model="searchForm.version" :min="1" placeholder="流程版本"></el-input-number>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 流程定义列表 -->
      <el-table :data="processDefinitions" style="width: 100%">
        <el-table-column prop="id" label="流程定义ID" width="180"></el-table-column>
        <el-table-column prop="name" label="流程名称"></el-table-column>
        <el-table-column prop="key" label="流程标识" width="150"></el-table-column>
        <el-table-column prop="version" label="版本" width="80"></el-table-column>
        <el-table-column prop="deploymentId" label="部署ID" width="180"></el-table-column>
        <el-table-column prop="resourceName" label="资源名称" width="200"></el-table-column>
        <el-table-column prop="diagramResourceName" label="流程图资源" width="200"></el-table-column>
        <el-table-column prop="suspended" label="是否挂起" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.suspended ? 'danger' : 'success'">
              {{ scope.row.suspended ? '已挂起' : '激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="startProcess(scope.row)">
              启动流程
            </el-button>
            <el-button 
              v-if="!scope.row.suspended" 
              type="warning" 
              size="small" 
              @click="suspendProcess(scope.row)"
            >
              挂起
            </el-button>
            <el-button 
              v-else 
              type="success" 
              size="small" 
              @click="activateProcess(scope.row)"
            >
              激活
            </el-button>
            <el-button type="danger" size="small" @click="deleteProcess(scope.row)">
              删除
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

    <!-- 流程部署对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="部署新流程"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form :model="deploymentForm" label-width="100px">
        <el-form-item label="部署名称" prop="name">
          <el-input v-model="deploymentForm.name" placeholder="请输入部署名称"></el-input>
        </el-form-item>
        <el-form-item label="部署描述" prop="description">
          <el-input v-model="deploymentForm.description" type="textarea" rows="3" placeholder="请输入部署描述"></el-input>
        </el-form-item>
        <el-form-item label="BPMN文件" prop="file">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept=".bpmn20.xml,.bpmn"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">拖放文件至此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">
                请上传 .bpmn20.xml 或 .bpmn 格式的文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleDeploy">部署</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 启动流程对话框 -->
    <el-dialog
      v-model="showStartDialog"
      :title="'启动流程：' + selectedProcess?.name"
      width="600px"
    >
      <el-form :model="startForm" label-width="100px">
        <el-form-item label="流程变量">
          <div v-for="(variable, index) in startForm.variables" :key="index" class="variable-item">
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
              <el-option label="数字" value="number"></el-option>
              <el-option label="布尔值" value="boolean"></el-option>
            </el-select>
            <el-button type="danger" icon="el-icon-delete" @click="removeVariable(index)"></el-button>
          </div>
          <el-button type="primary" size="small" @click="addVariable">添加变量</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showStartDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmStartProcess">启动</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { UploadFilled } from '@element-plus/icons-vue'

export default {
  name: 'ProcessDefinitionList',
  components: {
    UploadFilled
  },
  setup() {
    // 响应式数据
    const processDefinitions = ref([])
    const showUploadDialog = ref(false)
    const showStartDialog = ref(false)
    const selectedProcess = ref(null)
    const selectedFile = ref(null)
    
    // 搜索表单
    const searchForm = reactive({
      name: '',
      version: null
    })
    
    // 分页信息
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 部署表单
    const deploymentForm = reactive({
      name: '',
      description: ''
    })
    
    // 启动流程表单
    const startForm = reactive({
      variables: []
    })
    
    // 获取流程定义列表
    const getProcessDefinitions = async () => {
      try {
        const response = await axios.get('/v1/process-definitions', {
          params: {
            name: searchForm.name,
            version: searchForm.version,
            page: pagination.currentPage,
            size: pagination.pageSize
          }
        })
        
        if (response.data.success) {
          processDefinitions.value = response.data.data.data
          pagination.total = response.data.data.total
        } else {
          ElMessage.error('获取流程定义失败: ' + response.data.message)
        }
      } catch (error) {
        console.error('获取流程定义错误:', error)
        ElMessage.error('获取流程定义失败: ' + error.message)
      }
    }
    
    // 上传文件处理
    const handleFileChange = (file) => {
      selectedFile.value = file.raw
    }
    
    // 部署流程
    const handleDeploy = async () => {
      if (!selectedFile.value) {
        ElMessage.warning('请选择要部署的BPMN文件')
        return
      }
      
      try {
        const formData = new FormData()
        formData.append('file', selectedFile.value)
        formData.append('name', deploymentForm.name || selectedFile.value.name)
        formData.append('description', deploymentForm.description)
        
        const response = await axios.post('/v1/process-definitions/deploy', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        if (response.data.success) {
          ElMessage.success('流程部署成功')
          showUploadDialog.value = false
          handleDialogClose()
          getProcessDefinitions()
        } else {
          ElMessage.error('流程部署失败: ' + response.data.message)
        }
      } catch (error) {
        console.error('部署流程错误:', error)
        ElMessage.error('流程部署失败: ' + error.message)
      }
    }
    
    // 启动流程
    const startProcess = (process) => {
      selectedProcess.value = process
      startForm.variables = []
      showStartDialog.value = true
    }
    
    // 确认启动流程
    const confirmStartProcess = async () => {
      try {
        // 构建变量对象
        const variables = {}
        startForm.variables.forEach(v => {
          if (v.name) {
            switch (v.type) {
              case 'number':
                variables[v.name] = Number(v.value)
                break
              case 'boolean':
                variables[v.name] = v.value === 'true' || v.value === true
                break
              default:
                variables[v.name] = v.value
            }
          }
        })
        
        // 自动添加当前用户信息
        const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
        variables.applicant = currentUser.username
        variables.applicantId = currentUser.id
        variables.applicantName = currentUser.fullName || currentUser.username
        
        const response = await axios.post(`/v1/process-definitions/${selectedProcess.value.id}/start`, {
          variables
        })
        
        if (response.data.success) {
          ElMessage.success('流程启动成功')
          showStartDialog.value = false
        } else {
          ElMessage.error('流程启动失败: ' + response.data.message)
        }
      } catch (error) {
        console.error('启动流程错误:', error)
        ElMessage.error('流程启动失败: ' + error.message)
      }
    }
    
    // 添加变量
    const addVariable = () => {
      startForm.variables.push({
        name: '',
        value: '',
        type: 'string'
      })
    }
    
    // 删除变量
    const removeVariable = (index) => {
      startForm.variables.splice(index, 1)
    }
    
    // 挂起流程
    const suspendProcess = async (process) => {
      try {
        const response = await axios.put(`/v1/process-definitions/${process.id}/suspend`)
        if (response.data.success) {
          ElMessage.success('流程已挂起')
          getProcessDefinitions()
        } else {
          ElMessage.error('挂起流程失败: ' + response.data.message)
        }
      } catch (error) {
        console.error('挂起流程错误:', error)
        ElMessage.error('挂起流程失败: ' + error.message)
      }
    }
    
    // 激活流程
    const activateProcess = async (process) => {
      try {
        const response = await axios.put(`/v1/process-definitions/${process.id}/activate`)
        if (response.data.success) {
          ElMessage.success('流程已激活')
          getProcessDefinitions()
        } else {
          ElMessage.error('激活流程失败: ' + response.data.message)
        }
      } catch (error) {
        console.error('激活流程错误:', error)
        ElMessage.error('激活流程失败: ' + error.message)
      }
    }
    
    // 删除流程
    const deleteProcess = async (process) => {
      ElMessageBox.confirm('确定要删除该流程吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await axios.delete(`/v1/process-definitions/${process.id}`)
          if (response.data.success) {
            ElMessage.success('删除成功')
            getProcessDefinitions()
          } else {
            ElMessage.error('删除失败: ' + response.data.message)
          }
        } catch (error) {
          console.error('删除流程错误:', error)
          ElMessage.error('删除失败: ' + error.message)
        }
      }).catch(() => {
        // 取消删除
      })
    }
    
    // 搜索
    const handleSearch = () => {
      pagination.currentPage = 1
      getProcessDefinitions()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchForm.name = ''
      searchForm.version = null
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
    
    // 关闭对话框
    const handleDialogClose = () => {
      deploymentForm.name = ''
      deploymentForm.description = ''
      selectedFile.value = null
    }
    
    // 初始化
    onMounted(() => {
      getProcessDefinitions()
    })
    
    return {
      processDefinitions,
      showUploadDialog,
      showStartDialog,
      selectedProcess,
      searchForm,
      pagination,
      deploymentForm,
      startForm,
      handleFileChange,
      handleDeploy,
      startProcess,
      confirmStartProcess,
      addVariable,
      removeVariable,
      suspendProcess,
      activateProcess,
      deleteProcess,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleDialogClose
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
}
</style>