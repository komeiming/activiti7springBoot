<template>
  <div class="bpmn-editor-container">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="showCreateProcessDialog = true">新建流程</el-button>
      <el-button @click="openProcess">打开流程</el-button>
      <el-button @click="saveProcess">保存流程</el-button>
      <el-button @click="deployProcess">部署流程</el-button>
      <el-button @click="exportBPMN">导出BPMN</el-button>
      <el-button @click="exportSVG">导出SVG</el-button>
      <el-button @click="undo" icon="el-icon-refresh-right" circle></el-button>
      <el-button @click="redo" icon="el-icon-refresh-left" circle></el-button>
      <el-button @click="zoomIn" icon="el-icon-plus" circle></el-button>
      <el-button @click="zoomOut" icon="el-icon-minus" circle></el-button>
      <el-button @click="fitView" icon="el-icon-full-screen" circle></el-button>
      <el-button @click="centerView" icon="el-icon-location" circle></el-button>
      <div class="process-info">
        <span class="label">流程名称:</span>
        <span class="value">{{ processName || '未设置' }}</span>
        <span class="label">流程标识:</span>
        <span class="value">{{ processKey || '未设置' }}</span>
      </div>
    </div>

    <!-- 新建流程对话框 -->
    <el-dialog
      v-model="showCreateProcessDialog"
      title="新建流程"
      width="500px"
      @close="handleCreateProcessDialogClose"
    >
      <el-form :model="createProcessForm" :rules="createProcessRules" ref="createProcessFormRef" label-width="100px">
        <el-form-item label="流程名称" prop="name">
          <el-input v-model="createProcessForm.name" placeholder="请输入流程名称" maxlength="50" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="流程标识" prop="key">
          <el-input v-model="createProcessForm.key" placeholder="请输入流程标识" maxlength="50" show-word-limit></el-input>
          <div class="el-form-item__help">流程标识用于唯一标识流程，建议使用英文和下划线</div>
        </el-form-item>
        <el-form-item label="流程描述">
          <el-input
            v-model="createProcessForm.description"
            type="textarea"
            placeholder="请输入流程描述"
            :rows="3"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateProcessDialog = false">取消</el-button>
          <el-button type="primary" @click="handleCreateProcess">创建</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- BPMN设计画布 -->
      <div class="canvas-container">
        <div ref="canvas" class="canvas"></div>
      </div>

      <!-- 属性面板 -->
      <div class="properties-panel" ref="propertiesPanel"></div>
    </div>

    <!-- 流程列表对话框 -->
    <el-dialog
      v-model="showProcessListDialog"
      title="选择流程"
      width="800px"
    >
      <el-table
        :data="processList"
        style="width: 100%"
        @row-dblclick="handleProcessSelect"
      >
        <el-table-column prop="id" label="ID" width="180"></el-table-column>
        <el-table-column prop="name" label="流程名称"></el-table-column>
        <el-table-column prop="key" label="流程标识"></el-table-column>
        <el-table-column prop="version" label="版本" width="80"></el-table-column>
        <el-table-column prop="suspended" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.suspended ? 'danger' : 'success'" size="small">
              {{ scope.row.suspended ? '已挂起' : '已激活' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showProcessListDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmProcessSelect">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 部署流程对话框 -->
    <el-dialog
      v-model="showDeployDialog"
      title="部署流程"
      width="500px"
    >
      <el-form :model="deployForm" :rules="deployRules" ref="deployFormRef" label-width="100px">
        <el-form-item label="部署名称" prop="deploymentName">
          <el-input v-model="deployForm.deploymentName" placeholder="请输入部署名称"></el-input>
        </el-form-item>
        <el-form-item label="流程名称" prop="processName">
          <el-input v-model="processName" placeholder="请输入流程名称"></el-input>
        </el-form-item>
        <el-form-item label="流程标识" prop="processKey">
          <el-input v-model="processKey" placeholder="请输入流程标识"></el-input>
        </el-form-item>
        <el-form-item label="部署描述">
          <el-input
            v-model="deployForm.description"
            type="textarea"
            placeholder="请输入部署描述"
            :rows="3"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDeployDialog = false">取消</el-button>
          <el-button type="primary" @click="handleDeploy">部署</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axiosConfig'

// 引入BPMN.js相关模块
import BpmnModeler from 'bpmn-js/lib/Modeler'
import { BpmnPropertiesPanelModule } from 'bpmn-js-properties-panel'
import minimapModule from 'diagram-js-minimap'
import tokenSimulationModule from 'bpmn-js-token-simulation'

// 引入BPMN.js样式
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'

export default {
  name: 'BPMNEditor',
  props: {
    initialXml: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    // 响应式数据
    const canvas = ref(null)
    const propertiesPanel = ref(null)
    const processName = ref('')
    const processKey = ref('')
    const showProcessListDialog = ref(false)
    const showDeployDialog = ref(false)
    const showCreateProcessDialog = ref(false)
    const processList = ref([])
    const selectedProcess = ref(null)
    const deployFormRef = ref(null)
    const createProcessFormRef = ref(null)

    // 新建流程表单
    const createProcessForm = ref({
      name: '',
      key: '',
      description: ''
    })

    // 新建流程表单验证规则
    const createProcessRules = {
      name: [
        { required: true, message: '请输入流程名称', trigger: 'blur' },
        { min: 2, max: 50, message: '流程名称长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      key: [
        { required: true, message: '请输入流程标识', trigger: 'blur' },
        { min: 2, max: 50, message: '流程标识长度在 2 到 50 个字符', trigger: 'blur' },
        { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '流程标识只能包含字母、数字和下划线，且必须以字母开头', trigger: 'blur' }
      ]
    }

    // 部署表单
    const deployForm = ref({
      deploymentName: '',
      description: ''
    })

    // 表单验证规则
    const deployRules = {
      deploymentName: [
        { required: true, message: '请输入部署名称', trigger: 'blur' }
      ],
      processName: [
        { required: true, message: '请输入流程名称', trigger: 'blur' }
      ],
      processKey: [
        { required: true, message: '请输入流程标识', trigger: 'blur' }
      ]
    }

    // BPMN模型器实例
    let bpmnModeler = null

    // 初始化BPMN模型器
    const initModeler = () => {
      bpmnModeler = new BpmnModeler({
        container: canvas.value,
        propertiesPanel: {
          parent: propertiesPanel.value
        },
        additionalModules: [
          BpmnPropertiesPanelModule,
          minimapModule,
          tokenSimulationModule
        ],
        // 配置BPMN.js，抑制内部错误日志
        moddleExtensions: {},
        // 设置日志级别，只记录严重错误
        logger: {
          log: () => {},
          warn: () => {},
          error: (message) => {
            // 只记录非顺序流引用错误
            if (!(message && message.includes('referenced by') && message.includes('not yet drawn'))) {
              console.error(message);
            }
          }
        }
      })

      // 添加事件监听
      bpmnModeler.on('commandStack.changed', () => {
        // 可以在这里添加自动保存逻辑
      })

      bpmnModeler.on('element.changed', (event) => {
        // 元素属性变化时更新流程名称和标识
        const element = event.element
        if (element.type === 'bpmn:Process') {
          processName.value = element.name || ''
          processKey.value = element.id || ''
        }
      })
    }

    // 处理新建流程表单提交
    const handleCreateProcess = async () => {
      try {
        await createProcessFormRef.value.validate()
        
        // 使用表单数据创建新流程
        processName.value = createProcessForm.value.name
        processKey.value = createProcessForm.value.key
        
        const newDiagramXml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" 
    xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
    xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" 
    xmlns:di="http://www.omg.org/spec/DD/20100524/DI" 
    xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" 
    id="sample-diagram" 
    targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn2:process id="${processKey.value}" name="${processName.value}" isExecutable="true">
    <bpmn2:documentation>${createProcessForm.value.description || ''}</bpmn2:documentation>
    <bpmn2:startEvent id="StartEvent_1" name="开始" />
  </bpmn2:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${processKey.value}">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
        <dc:Bounds x="173" y="102" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="181" y="145" width="20" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn2:definitions>`

        renderDiagram(newDiagramXml)
        showCreateProcessDialog.value = false
        ElMessage.success('流程创建成功')
      } catch (error) {
        if (error.name !== 'Error') {
          return
        }
        ElMessage.error('流程创建失败: ' + error.message)
      }
    }

    // 关闭新建流程对话框
    const handleCreateProcessDialogClose = () => {
      createProcessFormRef.value?.resetFields()
    }

    // 创建新流程（内部方法，供组件初始化使用）
    const createNewProcess = () => {
      // 使用默认值创建新流程
      const defaultName = '新流程'
      const defaultKey = 'new-process-' + Date.now()
      
      processName.value = defaultName
      processKey.value = defaultKey
      
      const newDiagramXml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" 
    xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
    xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" 
    xmlns:di="http://www.omg.org/spec/DD/20100524/DI" 
    xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" 
    id="sample-diagram" 
    targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn2:process id="${defaultKey}" name="${defaultName}" isExecutable="true">
    <bpmn2:startEvent id="StartEvent_1" name="开始" />
  </bpmn2:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${defaultKey}">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
        <dc:Bounds x="173" y="102" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="181" y="145" width="20" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn2:definitions>`

      renderDiagram(newDiagramXml)
    }

    // 撤销操作
    const undo = () => {
      const commandStack = bpmnModeler.get('commandStack')
      if (commandStack.canUndo()) {
        commandStack.undo()
      }
    }

    // 重做操作
    const redo = () => {
      const commandStack = bpmnModeler.get('commandStack')
      if (commandStack.canRedo()) {
        commandStack.redo()
      }
    }

    // 放大
    const zoomIn = () => {
      const canvas = bpmnModeler.get('canvas')
      const zoomLevel = canvas.zoom()
      canvas.zoom(Math.min(zoomLevel + 0.1, 2))
    }

    // 缩小
    const zoomOut = () => {
      const canvas = bpmnModeler.get('canvas')
      const zoomLevel = canvas.zoom()
      canvas.zoom(Math.max(zoomLevel - 0.1, 0.2))
    }

    // 适应视图
    const fitView = () => {
      const canvas = bpmnModeler.get('canvas')
      canvas.zoom('fit-viewport')
    }

    // 居中视图
    const centerView = () => {
      const canvas = bpmnModeler.get('canvas')
      const rootElement = canvas.getRootElement()
      if (rootElement) {
        canvas.zoom('fit-viewport')
      }
    }

    // 打开流程
    const openProcess = async () => {
      // 先获取流程列表
      await getProcessDefinitions()
      showProcessListDialog.value = true
    }

    // 获取流程定义列表
    const getProcessDefinitions = async () => {
      try {
        // 检查是否是租户环境
        const tenantStr = localStorage.getItem('tenantInfo')
        let res
        if (tenantStr) {
          // 租户环境下，使用租户隔离的API
          console.log('租户环境，调用API获取流程定义列表')
          res = await axios.get('/api/v1/workflow/process/definition', {
            params: {
              page: 1,
              size: 100
            }
          })
          // 处理租户API返回的数据格式
          console.log('API返回结果:', res)
          if (res && res.rows) {
            processList.value = res.rows || []
            console.log('处理后流程列表:', processList.value)
          } else {
            processList.value = []
            console.log('无法找到流程列表数据')
          }
        } else {
          // 非租户环境下，使用原有API（系统管理员）
          console.log('非租户环境，调用API获取流程定义列表')
          res = await axios.get('/api/process/definition/list', {
            params: {
              page: 1,
              pageSize: 100
            }
          })
          // 处理非租户API返回的数据格式
          console.log('API返回结果:', res)
          if (res) {
            if (res.success) {
              processList.value = res.rows || []
              console.log('处理后流程列表:', processList.value)
            } else {
              processList.value = []
              console.log('API返回失败:', res.message)
            }
          } else {
            processList.value = []
            console.log('无法找到流程列表数据')
          }
        }
      } catch (error) {
        console.error('获取流程列表失败:', error)
        ElMessage.error('获取流程列表失败: ' + error.message)
      }
    }

    // 选择流程
    const handleProcessSelect = (row) => {
      selectedProcess.value = row
      confirmProcessSelect()
    }

    // 确认选择流程
    const confirmProcessSelect = async () => {
      if (!selectedProcess.value) {
        ElMessage.warning('请选择一个流程')
        return
      }

      try {
        // 检查是否是租户环境
        const tenantStr = localStorage.getItem('tenantInfo')
        let res
        let bpmnXml
        
        if (tenantStr) {
          // 租户环境下，使用租户隔离的API
          res = await axios.get(`/api/v1/workflow/process/definition/xml/${selectedProcess.value.deploymentId}`)
          // 响应拦截器会处理success字段，直接使用res.bpmnXml
          bpmnXml = res.bpmnXml
        } else {
          // 非租户环境下，使用原有API
          res = await axios.get(`/api/process/definition/xml/${selectedProcess.value.deploymentId}`)
          if (res && res.success) {
            bpmnXml = res.bpmnXml
          }
        }
        
        if (bpmnXml) {
          renderDiagram(bpmnXml)
          showProcessListDialog.value = false
        } else {
          ElMessage.error('获取流程XML失败: 未找到BPMN XML数据')
        }
      } catch (error) {
        ElMessage.error('获取流程XML失败: ' + error.message)
      }
    }

    // 渲染流程图
    const renderDiagram = async (xml) => {
      // 保存原始控制台error函数
      const originalConsoleError = console.error;
      
      try {
        // 重写控制台error函数，过滤掉特定的BPMN.js顺序流引用错误
        console.error = (message, ...args) => {
          if (typeof message === 'string' && message.includes('failed to import <bpmn:SequenceFlow') && message.includes('referenced by') && message.includes('not yet drawn')) {
            // 忽略BPMN.js顺序流引用错误
            return;
          }
          // 其他错误正常记录
          originalConsoleError.apply(console, [message, ...args]);
        };
        
        // 直接调用importXML，BPMN.js会在内部处理元素绘制顺序和引用关系
        const result = await bpmnModeler.importXML(xml);
        
        const { warnings } = result;
        if (warnings && warnings.length) {
          // 过滤掉顺序流引用警告，只显示其他重要警告
          const importantWarnings = warnings.filter(warning => {
            return !(warning.message && warning.message.includes('referenced by') && warning.message.includes('not yet drawn'));
          });
          
          if (importantWarnings.length > 0) {
            console.warn('BPMN导入警告:', importantWarnings);
          }
        }
        
        // 缩放以适应画布
        bpmnModeler.get('canvas').zoom('fit-viewport');
        
        // 更新流程名称和标识
        const canvas = bpmnModeler.get('canvas');
        const rootElement = canvas.getRootElement();
        if (rootElement && rootElement.businessObject) {
          processName.value = rootElement.businessObject.name || '';
          processKey.value = rootElement.businessObject.id || '';
        }
      } catch (error) {
        // 只处理非顺序流引用错误
        if (!(error.message && error.message.includes('referenced by') && error.message.includes('not yet drawn'))) {
          // 其他错误显示错误消息
          originalConsoleError('BPMN导入错误:', error);
          ElMessage.error('渲染流程图失败: ' + error.message);
        } else {
          // 即使出现顺序流引用错误，也继续执行后续操作
          try {
            // 缩放以适应画布
            bpmnModeler.get('canvas').zoom('fit-viewport');
            
            // 更新流程名称和标识
            const canvas = bpmnModeler.get('canvas');
            const rootElement = canvas.getRootElement();
            if (rootElement && rootElement.businessObject) {
              processName.value = rootElement.businessObject.name || '';
              processKey.value = rootElement.businessObject.id || '';
            }
          } catch (e) {
            originalConsoleError('处理顺序流引用错误时发生异常:', e);
          }
        }
      } finally {
        // 恢复原始控制台error函数
        console.error = originalConsoleError;
      }
    }

    // 保存流程
    const saveProcess = async () => {
      try {
        const { xml } = await bpmnModeler.saveXML({ format: true })
        
        // 检查是否是租户环境
        const tenantStr = localStorage.getItem('tenantInfo')
        
        if (tenantStr) {
          // 租户环境下，调用租户流程保存API
          await axios.post('/api/v1/workflow/process/definition/save', {
            name: processName.value,
            key: processKey.value,
            xml: xml
          })
          ElMessage.success('流程保存成功')
        } else {
          // 非租户环境下，使用原有API
          await axios.post('/api/process/definition/save', {
            name: processName.value,
            key: processKey.value,
            xml: xml
          })
          ElMessage.success('流程保存成功')
        }
      } catch (error) {
        ElMessage.error('流程保存失败: ' + error.message)
      }
    }

    // 部署流程
    const deployProcess = () => {
      deployForm.value.deploymentName = `${processName.value}-${new Date().getTime()}`
      showDeployDialog.value = true
    }

    // 处理部署
    const handleDeploy = async () => {
      try {
        await deployFormRef.value.validate()
        
        const { xml } = await bpmnModeler.saveXML({ format: true })
        
        // 检查是否是租户环境
        const tenantStr = localStorage.getItem('tenantInfo')
        
        if (tenantStr) {
          // 租户环境下，使用租户隔离的API
          const formData = new FormData()
          formData.append('deploymentName', deployForm.value.deploymentName)
          formData.append('description', deployForm.value.description)
          formData.append('name', processName.value)
          formData.append('key', processKey.value)
          formData.append('file', new Blob([xml], { type: 'application/xml' }), `${processKey.value}.bpmn`)

          const res = await axios.post('/api/v1/workflow/process/definition/deploy', formData)
          ElMessage.success('流程部署成功')
          showDeployDialog.value = false
        } else {
          // 非租户环境下，使用原有API
          const formData = new FormData()
          formData.append('deploymentName', deployForm.value.deploymentName)
          formData.append('description', deployForm.value.description)
          formData.append('name', processName.value)
          formData.append('key', processKey.value)
          formData.append('file', new Blob([xml], { type: 'application/xml' }), `${processKey.value}.bpmn`)

          const res = await axios.post('/api/process/definition/deploy', formData)
          if (res.success) {
            ElMessage.success('流程部署成功')
            showDeployDialog.value = false
          } else {
            ElMessage.error('流程部署失败: ' + (res.message || '未知错误'))
          }
        }
      } catch (error) {
        if (error.name !== 'Error') {
          return
        }
        ElMessage.error('流程部署失败: ' + error.message)
      }
    }

    // 导出BPMN
    const exportBPMN = async () => {
      try {
        const { xml } = await bpmnModeler.saveXML({ format: true })
        downloadFile(xml, `${processKey.value || 'diagram'}.bpmn`, 'application/xml')
      } catch (error) {
        ElMessage.error('导出BPMN失败: ' + error.message)
      }
    }

    // 导出SVG
    const exportSVG = async () => {
      try {
        const { svg } = await bpmnModeler.saveSVG()
        downloadFile(svg, `${processKey.value || 'diagram'}.svg`, 'image/svg+xml')
      } catch (error) {
        ElMessage.error('导出SVG失败: ' + error.message)
      }
    }

    // 下载文件
    const downloadFile = (content, filename, contentType) => {
      const blob = new Blob([content], { type: contentType })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = filename
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
    }

    // 初始化
    onMounted(() => {
      initModeler()
      if (props.initialXml) {
        renderDiagram(props.initialXml)
      } else {
        createNewProcess()
      }
    })

    // 组件销毁前清理
    onBeforeUnmount(() => {
      if (bpmnModeler) {
        bpmnModeler.destroy()
      }
    })

    return {
      canvas,
      propertiesPanel,
      processName,
      processKey,
      showProcessListDialog,
      showDeployDialog,
      showCreateProcessDialog,
      processList,
      selectedProcess,
      deployForm,
      deployRules,
      deployFormRef,
      createProcessForm,
      createProcessRules,
      createProcessFormRef,
      createNewProcess,
      handleCreateProcess,
      handleCreateProcessDialogClose,
      openProcess,
      saveProcess,
      deployProcess,
      exportBPMN,
      exportSVG,
      undo,
      redo,
      zoomIn,
      zoomOut,
      fitView,
      centerView,
      handleProcessSelect,
      confirmProcessSelect,
      handleDeploy
    }
  }
}
</script>

<style scoped>
.bpmn-editor-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.toolbar {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.canvas-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  background-color: white;
}

.canvas {
  width: 100%;
  height: 100%;
}

.properties-panel {
  width: 350px;
  background-color: #f5f7fa;
  border-left: 1px solid #e4e7ed;
  overflow-y: auto;
}

/* 调整BPMN.js的样式 */
:deep(.djs-palette) {
  padding: 5px;
}

:deep(.djs-context-pad) {
  z-index: 1000;
}

:deep(.djs-element) {
  cursor: move;
}

/* 流程信息显示样式 */
.process-info {
  margin-left: auto;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.process-info .label {
  margin-right: 5px;
  font-weight: 500;
}

.process-info .value {
  margin-right: 15px;
  font-weight: 600;
  color: #303133;
}

/* 工具栏按钮间距 */
.toolbar .el-button + .el-button {
  margin-left: 10px;
}

/* 表单帮助信息样式 */
.el-form-item__help {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 对话框表单样式 */
.el-dialog__body {
  padding: 20px;
}

/* 流程设计器容器样式增强 */
.bpmn-editor-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background-color: #f5f7fa;
}

/* 工具栏样式增强 */
.toolbar {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* 主要内容区域样式增强 */
.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
  background-color: #fff;
  margin: 10px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 画布容器样式增强 */
.canvas-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  background-color: #fafafa;
  background-image: linear-gradient(to right, #e0e0e0 1px, transparent 1px),
                    linear-gradient(to bottom, #e0e0e0 1px, transparent 1px);
  background-size: 20px 20px;
  border-right: 1px solid #e4e7ed;
}

/* 画布样式增强 */
.canvas {
  width: 100%;
  height: 100%;
}
</style>