<template>
  <div class="approval-center-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>审批中心</span>
        </div>
      </template>

      <!-- 审批进度流程展示 -->
      <div class="approval-progress-section">
        <h3 style="margin-bottom: 20px;">审批进度</h3>
        <!-- 空状态处理 -->
        <el-empty 
          v-if="approvalProcesses.length === 0"
          description="暂无审批进度数据"
          :image-size="100"
        >
          <el-button type="primary" @click="loadApprovalData">重新加载</el-button>
        </el-empty>
        <!-- 审批进度 - 分行展示每个流程 -->
        <div v-else>
          <div 
            v-for="(process, processIndex) in approvalProcesses" 
            :key="processIndex"
            class="process-container"
          >
            <h4 class="process-title">{{ process.processName }}</h4>
            <div class="progress-timeline-horizontal">
              <div 
                v-for="(step, index) in process.steps" 
                :key="index"
                class="progress-step-horizontal"
                :class="{
                  'completed': step.status === 'completed',
                  'current': step.status === 'current',
                  'pending': step.status === 'pending'
                }"
              >
                <div class="step-content-horizontal">
                  <div class="step-title">{{ step.title }}</div>
                  <div class="step-time">{{ step.time }}</div>
                  <div class="step-user">{{ step.user }}</div>
                  <div class="step-status" :class="step.status">
                    {{ step.status === 'completed' ? '已完成' : step.status === 'current' ? '当前审批' : '待审批' }}
                  </div>
                </div>
                <div class="step-circle-horizontal">
                  <div class="circle-icon">
                    <el-icon v-if="step.status === 'completed'" size="20">
                      <Check /></el-icon>
                    <el-icon v-else-if="step.status === 'current'" size="20">
                      <Clock /></el-icon>
                    <el-icon v-else size="20">
                      <CircleCheck /></el-icon>
                  </div>
                </div>
                <div class="step-line-horizontal" v-if="index < process.steps.length - 1"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 审批内容列表 -->
      <div class="approval-content-section">
        <div class="section-header">
          <h3>审批内容</h3>
          <el-button 
            type="primary" 
            @click="handleExport"
            :disabled="!approvalContent.taskId"
          >
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </div>
        
        <!-- 空状态处理 -->
        <el-empty 
          v-if="!approvalContent.taskId"
          description="暂无审批内容数据"
          :image-size="100"
        >
          <el-button type="primary" @click="loadApprovalData">重新加载</el-button>
        </el-empty>
        
        <!-- 审批内容详情 -->
        <el-card class="content-card" v-else>
          <div class="content-header">
            <span class="content-title">{{ approvalContent.taskName }}</span>
          </div>
          
          <div class="approval-detail">
            <div class="detail-item">
              <label>任务ID:</label>
              <span>{{ approvalContent.taskId }}</span>
            </div>
            <div class="detail-item">
              <label>流程名称:</label>
              <span>{{ approvalContent.processName }}</span>
            </div>
            <div class="detail-item">
              <label>创建时间:</label>
              <span>{{ approvalContent.createTime }}</span>
            </div>
            <div class="detail-item">
              <label>处理人:</label>
              <span>{{ approvalContent.assignee }}</span>
            </div>
            
            <!-- 审批变量详情 -->
            <div class="detail-section">
              <h4>审批详情</h4>
              <div class="variables-container">
                <div 
                  v-for="(value, key) in approvalContent.variables" 
                  :key="key"
                  class="variable-item"
                >
                  <label>{{ key }}:</label>
                  <span>{{ typeof value === 'object' ? JSON.stringify(value) : value }}</span>
                </div>
                <div v-if="Object.keys(approvalContent.variables).length === 0" class="no-variables">
                  暂无审批详情数据
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Clock, CircleCheck, Download } from '@element-plus/icons-vue'
import TaskService from '../services/TaskService'

// 审批进度步骤 - 初始化为空，从API获取
const approvalProcesses = ref([])

// 审批内容数据 - 初始化为空，从API获取
const approvalContent = reactive({
  taskId: '',
  taskName: '',
  processName: '',
  createTime: '',
  assignee: '',
  variables: {} // 任务变量，存储审批内容
})

// 根据流程类型获取后续步骤
const getProcessStepsByType = (processType) => {
  // 根据不同流程类型返回不同的后续步骤
  const stepsMap = {
    'employeeOnboard': [
      {
        title: '经理审批',
        user: '经理',
        type: 'task'
      },
      {
        title: 'HR确认',
        user: 'HR',
        type: 'task'
      },
      {
        title: '资源准备',
        user: 'ADMIN',
        type: 'task'
      },
      {
        title: '入职培训',
        user: 'TRAINER',
        type: 'task'
      },
      {
        title: '入职完成',
        user: '',
        type: 'end'
      }
    ],
    'leave-process': [
      {
        title: '部门经理审批',
        user: '经理',
        type: 'task'
      },
      {
        title: 'HR审批',
        user: 'HR',
        type: 'task'
      },
      {
        title: '审批完成',
        user: '',
        type: 'end'
      }
    ],
    'onboarding-process': [
      {
        title: '经理审批',
        user: '经理',
        type: 'task'
      },
      {
        title: 'HR确认',
        user: 'HR',
        type: 'task'
      },
      {
        title: '资源准备',
        user: 'ADMIN',
        type: 'task'
      },
      {
        title: '入职培训',
        user: 'TRAINER',
        type: 'task'
      },
      {
        title: '入职完成',
        user: '',
        type: 'end'
      }
    ]
  }
  
  // 默认返回通用审批流程步骤
  return stepsMap[processType] || [
    {
      title: '审批1',
      user: '审批人1',
      type: 'task'
    },
    {
      title: '审批2',
      user: '审批人2',
      type: 'task'
    },
    {
      title: '审批完成',
      user: '',
      type: 'end'
    }
  ]
}

// 加载审批数据
const loadApprovalData = async () => {
  try {
    console.log('开始加载审批数据...');
    // 获取当前用户的待办任务列表
    const response = await TaskService.getTasks({
      page: 1,
      pageSize: 10
    })
    
    console.log('任务列表API响应:', response);
    
    // 清空现有审批流程
    approvalProcesses.value = [];
    
    // 检查响应数据
    if (response && response.data && response.data.length > 0) {
      console.log('待办任务数量:', response.data.length);
      
      // 遍历所有待办任务，为每个任务构建审批进度
      for (const task of response.data) {
        console.log('处理任务:', task);
        
        // 获取任务详情，包含变量
        const taskDetail = await TaskService.getTaskDetail(task.id);
        // 处理不同格式的任务详情响应
        const detailData = taskDetail.task || taskDetail;
        const taskVariables = detailData.variables || taskDetail.variables || {};
        
        console.log('任务详情:', taskDetail);
        console.log('任务变量:', taskVariables);
        
        // 从任务变量中获取申请人信息
        const applicant = taskVariables.applicant || taskVariables.applyUser || taskVariables.createBy || taskVariables.owner || '申请人';
        
        // 获取流程历史，构建真实的审批进度
        let processHistory = [];
        try {
          const historyResponse = await TaskService.getProcessInstanceHistory(task.processInstanceId);
          if (historyResponse && historyResponse.data && historyResponse.data.data) {
            processHistory = historyResponse.data.data;
            console.log('流程历史:', processHistory);
          }
        } catch (historyError) {
          console.error('获取流程历史失败:', historyError);
        }
        
        // 构建审批进度步骤，完全基于流程历史记录
        let completeSteps = [];
        
        // 1. 添加流程发起节点，显示实际申请人
        completeSteps.push({
          title: '流程发起',
          time: task.createTime,
          user: applicant,
          status: 'completed',
          type: 'start'
        });
        
        // 2. 处理流程历史记录，构建真实的审批进度
        // 过滤出任务相关的历史记录
        const taskHistory = processHistory.filter(h => 
          h.type && (h.type === 'task' || h.type === 'complete' || h.type === 'userTask')
        );
        
        // 按照时间顺序排序
        taskHistory.sort((a, b) => {
          const timeA = new Date(a.time || 0).getTime();
          const timeB = new Date(b.time || 0).getTime();
          return timeA - timeB;
        });
        
        console.log('排序后的流程历史:', taskHistory);
        
        // 3. 添加已完成的任务节点
        taskHistory.forEach(historyItem => {
          // 跳过流程发起节点，已经单独添加
          if (historyItem.activityName === '流程发起') {
            return;
          }
          
          // 确定步骤状态
          let stepStatus = 'completed';
          
          // 添加步骤到审批进度
          completeSteps.push({
            title: historyItem.activityName || '未知活动',
            time: historyItem.time,
            user: historyItem.assignee || historyItem.userId || '系统',
            status: stepStatus,
            type: 'task'
          });
        });
        
        // 4. 添加当前任务节点
        completeSteps.push({
          title: task.name,
          time: task.createTime,
          user: task.assignee || '系统',
          status: 'current',
          type: 'task'
        });
        
        // 5. 不再添加待审批节点，只显示已完成和当前节点
        // 用户要求只显示历史流程过程信息，不显示待审批的预测节点
        
        // 添加完整的审批流程到列表中
        const processType = task.processDefinitionKey || task.processDefinitionId?.split(':')[0] || 'general';
        approvalProcesses.value.push({
          processName: task.processDefinitionName || processType,
          processInstanceId: task.processInstanceId,
          steps: completeSteps
        });
      }
      
      console.log('审批流程列表:', approvalProcesses.value);
      
      // 如果有审批流程，默认显示第一个流程的内容
      if (approvalProcesses.value.length > 0) {
        const firstTask = response.data[0];
        
        // 获取第一个任务的详情，包含变量
        const firstTaskDetail = await TaskService.getTaskDetail(firstTask.id);
        // 处理不同格式的任务详情响应
        const firstDetailData = firstTaskDetail.task || firstTaskDetail;
        const taskVariables = firstDetailData.variables || firstTaskDetail.variables || {};
        
        // 设置审批内容
        approvalContent.taskId = firstTask.id;
        approvalContent.taskName = firstTask.name;
        approvalContent.processName = firstTask.processDefinitionName || '';
        approvalContent.createTime = firstTask.createTime;
        approvalContent.assignee = firstTask.assignee || '';
        approvalContent.variables = taskVariables;
        
        console.log('审批内容:', approvalContent);
      }
    } else {
      console.log('没有待办任务');
      // 没有待办任务，显示空状态
      approvalProcesses.value = [];
      
      // 清空审批内容
      Object.assign(approvalContent, {
        taskId: '',
        taskName: '',
        processName: '',
        createTime: '',
        assignee: '',
        variables: {}
      });
    }
  } catch (error) {
    console.error('加载审批数据失败:', error);
    // API调用失败，显示空状态
    approvalProcesses.value = [];
    
    // 清空审批内容
    Object.assign(approvalContent, {
      taskId: '',
      taskName: '',
      processName: '',
      createTime: '',
      assignee: '',
      variables: {}
    });
  }
}

// 导出功能
const handleExport = () => {
  try {
    // 这里可以实现实际的导出功能
    ElMessage.success('导出功能已触发')
    console.log('导出审批内容:', approvalContent)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 初始化
onMounted(() => {
  loadApprovalData()
})
</script>

<style scoped>
.approval-center-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 审批进度样式 - 横向 */
.approval-progress-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 流程容器样式 */
.process-container {
  margin-bottom: 30px;
  padding: 15px;
  background-color: #fafafa;
  border-radius: 6px;
  border-left: 8px solid #409eff;
}

/* 流程标题样式 */
.process-title {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 多个流程之间的分隔 */
.process-container + .process-container {
  margin-top: 25px;
}

.progress-timeline-horizontal {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  padding: 20px 0;
  flex-wrap: wrap;
  min-height: 120px;
}

.progress-step-horizontal {
  display: flex;
  align-items: center;
  flex-direction: column;
  position: relative;
  flex: 1;
  min-width: 150px;
  text-align: center;
  transition: all 0.3s ease;
}

.step-circle-horizontal {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin: 12px 0;
  z-index: 1;
  transition: all 0.3s ease;
  position: relative;
}

.circle-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

/* 已完成步骤样式 */
.progress-step-horizontal.completed .step-circle-horizontal {
  background-color: #67c23a;
  color: white;
  box-shadow: 0 0 0 4px rgba(103, 194, 58, 0.1);
}

.progress-step-horizontal.completed .step-title {
  color: #67c23a;
  font-weight: 700;
}

.progress-step-horizontal.completed .step-status {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #c2e7b0;
}

/* 当前步骤样式 */
.progress-step-horizontal.current .step-circle-horizontal {
  background-color: #409eff;
  color: white;
  transform: scale(1.2);
  box-shadow: 0 0 0 6px rgba(64, 158, 255, 0.15), 0 0 20px rgba(64, 158, 255, 0.3);
}

.progress-step-horizontal.current .step-title {
  color: #409eff;
  font-weight: 700;
  font-size: 16px;
}

.progress-step-horizontal.current .step-status {
  background-color: #ecf5ff;
  color: #409eff;
  border: 1px solid #90caf9;
  font-weight: 600;
}

/* 待审批步骤样式 */
.progress-step-horizontal.pending .step-circle-horizontal {
  background-color: #ffffff;
  color: #909399;
  border: 3px solid #e4e7ed;
}

.progress-step-horizontal.pending .step-title {
  color: #909399;
}

.progress-step-horizontal.pending .step-status {
  background-color: #fafafa;
  color: #909399;
  border: 1px solid #e4e7ed;
}

.step-content-horizontal {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.step-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  font-size: 14px;
  transition: all 0.3s ease;
  line-height: 1.4;
}

.step-time {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  font-family: 'Courier New', Courier, monospace;
}

.step-user {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
  font-weight: 500;
}

/* 步骤状态标签样式 */
.step-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-top: 4px;
}

.step-line-horizontal {
  position: absolute;
  top: 24px;
  left: 50%;
  right: -50%;
  height: 3px;
  background-color: #e4e7ed;
  z-index: 0;
  transition: all 0.3s ease;
}

.progress-step-horizontal.completed .step-line-horizontal {
  background-color: #67c23a;
}

.progress-step-horizontal.current .step-line-horizontal {
  background-color: #409eff;
}

.progress-step-horizontal:last-child .step-line-horizontal {
  display: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .progress-step-horizontal {
    min-width: 100px;
  }
  
  .step-title {
    font-size: 12px;
  }
  
  .step-time, .step-user {
    font-size: 10px;
  }
  
  .step-circle-horizontal {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .step-line-horizontal {
    top: 20px;
  }
}

/* 审批内容样式 */
.approval-content-section {
  margin-top: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.content-card {
  margin-bottom: 20px;
}

.content-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.content-title {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

/* 审批详情样式 */
.approval-detail {
  padding: 20px 0;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.detail-item label {
  width: 100px;
  font-weight: 600;
  color: #606266;
  margin-right: 20px;
  text-align: right;
  flex-shrink: 0;
}

.detail-item span {
  flex: 1;
  color: #303133;
  word-break: break-all;
}

.detail-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px dashed #ebeef5;
}

.detail-section h4 {
  margin-bottom: 15px;
  color: #303133;
  font-size: 15px;
  font-weight: 600;
}

.variables-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.variable-item {
  display: flex;
  flex-direction: column;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.variable-item label {
  font-weight: 600;
  color: #606266;
  margin-bottom: 5px;
  font-size: 13px;
}

.variable-item span {
  color: #303133;
  font-size: 14px;
  word-break: break-all;
}

.no-variables {
  color: #909399;
  text-align: center;
  padding: 20px;
  grid-column: 1 / -1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .approval-center-container {
    padding: 10px;
  }
  
  .progress-timeline {
    padding-left: 20px;
  }
  
  .step-circle {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
  
  .step-line {
    left: 13px;
  }
}
</style>