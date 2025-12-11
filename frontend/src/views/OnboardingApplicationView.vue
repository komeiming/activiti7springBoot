<template>
  <div class="onboarding-application-container">
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <h1>新员工入职申请</h1>
          <el-breadcrumb separator-class="el-icon-arrow-right">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/process-apply' }">流程申请</el-breadcrumb-item>
            <el-breadcrumb-item>入职申请</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </template>
      
      <el-form 
        :model="formData" 
        ref="onboardingForm" 
        label-width="100px" 
        :rules="rules"
      >
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申请人" prop="applicantName">
              <el-input v-model="formData.applicantName" placeholder="请输入申请人姓名" disabled>
                <template #prefix><i class="el-icon-user"></i></template>
              </el-input>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="申请部门" prop="department">
              <el-input v-model="formData.department" placeholder="请输入申请部门" disabled>
                <template #prefix><i class="el-icon-office-building"></i></template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">新员工信息</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="employeeName">
              <el-input v-model="formData.employeeName" placeholder="请输入新员工姓名">
                <template #prefix><i class="el-icon-user"></i></template>
              </el-input>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="formData.idCard" placeholder="请输入身份证号码">
                <template #prefix><i class="el-icon-document-copy"></i></template>
              </el-input>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话">
                <template #prefix><i class="el-icon-phone"></i></template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入职日期" prop="startDate">
              <el-date-picker
                v-model="formData.startDate"
                type="date"
                placeholder="选择入职日期"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="期望薪资" prop="expectedSalary">
              <el-input v-model.number="formData.expectedSalary" placeholder="请输入期望薪资">
                <template #prefix><i class="el-icon-money"></i></template>
                <template #suffix>元</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="应聘职位" prop="position">
              <el-select v-model="formData.position" placeholder="请选择职位" style="width: 100%">
                <el-option v-for="pos in positions" :key="pos.value" :label="pos.label" :value="pos.value" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="所属部门" prop="targetDepartment">
              <el-select v-model="formData.targetDepartment" placeholder="请选择部门" style="width: 100%">
                <el-option v-for="dept in departments" :key="dept.value" :label="dept.label" :value="dept.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学历" prop="education">
              <el-select v-model="formData.education" placeholder="请选择学历" style="width: 100%">
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
                <el-option label="专科" value="专科" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="紧急联系人" prop="emergencyContact">
              <el-input v-model="formData.emergencyContact" placeholder="请输入紧急联系人">
                <template #prefix><i class="el-icon-user-solid"></i></template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="紧急联系人电话" prop="emergencyPhone">
              <el-input v-model="formData.emergencyPhone" placeholder="请输入紧急联系人电话">
                <template #prefix><i class="el-icon-phone-outline"></i></template>
              </el-input>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="入职方式" prop="entryType">
              <el-select v-model="formData.entryType" placeholder="请选择入职方式" style="width: 100%">
                <el-option label="全职" value="全职" />
                <el-option label="兼职" value="兼职" />
                <el-option label="实习" value="实习" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">入职所需物品</el-divider>
        
        <el-form-item label="办公设备">
          <el-checkbox-group v-model="formData.officeEquipment">
            <el-checkbox label="笔记本电脑">笔记本电脑</el-checkbox>
            <el-checkbox label="显示器">显示器</el-checkbox>
            <el-checkbox label="键盘鼠标">键盘鼠标</el-checkbox>
            <el-checkbox label="办公用品套装">办公用品套装</el-checkbox>
            <el-checkbox label="其他">其他</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="办公账号">
          <el-checkbox-group v-model="formData.accountAccess">
            <el-checkbox label="企业邮箱">企业邮箱</el-checkbox>
            <el-checkbox label="内部系统账号">内部系统账号</el-checkbox>
            <el-checkbox label="VPN账号">VPN账号</el-checkbox>
            <el-checkbox label="代码仓库权限">代码仓库权限</el-checkbox>
            <el-checkbox label="其他">其他</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="其他需求">
          <el-input
            v-model="formData.otherRequirements"
            type="textarea"
            placeholder="请输入其他入职相关需求"
            rows="3"
          />
        </el-form-item>
        
        <el-divider content-position="left">申请信息</el-divider>
        
        <el-form-item label="申请原因" prop="reason">
          <el-input
            v-model="formData.reason"
            type="textarea"
            placeholder="请描述招聘原因"
            rows="3"
          />
        </el-form-item>
        
        <el-form-item label="抄送人" prop="ccList">
          <el-select
            v-model="formData.ccList"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择抄送人"
            style="width: 100%"
          >
            <el-option
              v-for="user in potentialCcs"
              :key="user.value"
              :label="user.label"
              :value="user.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            placeholder="请输入备注信息"
            rows="2"
          />
        </el-form-item>
        
        <div class="form-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="submitForm">提交申请</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import UserService from '../services/UserService'
import ProcessDefinitionService from '../services/ProcessDefinitionService'
import { ElMessage, ElLoading } from 'element-plus'

export default {
  name: 'OnboardingApplicationView',
  data() {
    return {
      formData: {
        applicantName: '',
        department: '',
        employeeName: '',
        gender: '男',
        idCard: '',
        phone: '',
        startDate: '',
        expectedSalary: null,
        position: '',
        targetDepartment: '',
        education: '',
        emergencyContact: '',
        emergencyPhone: '',
        entryType: '全职',
        officeEquipment: [],
        accountAccess: [],
        otherRequirements: '',
        reason: '',
        ccList: [],
        remark: ''
      },
      
      // 当前用户信息
      currentUser: null,
      
      // 职位选项
      positions: [
        { label: '前端开发工程师', value: '前端开发工程师' },
        { label: '后端开发工程师', value: '后端开发工程师' },
        { label: '测试工程师', value: '测试工程师' },
        { label: '产品经理', value: '产品经理' },
        { label: 'UI设计师', value: 'UI设计师' },
        { label: '项目经理', value: '项目经理' },
        { label: '运维工程师', value: '运维工程师' },
        { label: '数据分析师', value: '数据分析师' },
        { label: '销售经理', value: '销售经理' },
        { label: '人力资源专员', value: '人力资源专员' },
        { label: '财务专员', value: '财务专员' }
      ],
      
      // 部门选项
      departments: [
        { label: '技术部', value: '技术部' },
        { label: '产品部', value: '产品部' },
        { label: '设计部', value: '设计部' },
        { label: '销售部', value: '销售部' },
        { label: '市场部', value: '市场部' },
        { label: '人力资源部', value: '人力资源部' },
        { label: '财务部', value: '财务部' },
        { label: '运营部', value: '运营部' },
        { label: '行政部', value: '行政部' },
        { label: '法务部', value: '法务部' }
      ],
      
      // 潜在抄送人
      potentialCcs: [
        { label: '张三 (技术部经理)', value: 'zhangsan' },
        { label: '李四 (产品部经理)', value: 'lisi' },
        { label: '王五 (人力资源总监)', value: 'wangwu' },
        { label: '赵六 (CEO)', value: 'zhaoliu' }
      ],
      
      // 表单验证规则
      rules: {
        employeeName: [
          { required: true, message: '请输入新员工姓名', trigger: 'blur' }
        ],
        idCard: [
          { required: true, message: '请输入身份证号码', trigger: 'blur' },
          { pattern: /^\d{17}[\dX]$/, message: '请输入有效的身份证号码', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
        ],
        startDate: [
          { required: true, message: '请选择入职日期', trigger: 'change' }
        ],
        expectedSalary: [
          { required: true, message: '请输入期望薪资', trigger: 'blur' },
          { type: 'number', message: '薪资必须为数字', trigger: 'blur' }
        ],
        position: [
          { required: true, message: '请选择应聘职位', trigger: 'change' }
        ],
        targetDepartment: [
          { required: true, message: '请选择所属部门', trigger: 'change' }
        ],
        education: [
          { required: true, message: '请选择学历', trigger: 'change' }
        ],
        emergencyContact: [
          { required: true, message: '请输入紧急联系人', trigger: 'blur' }
        ],
        emergencyPhone: [
          { required: true, message: '请输入紧急联系人电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
        ],
        reason: [
          { required: true, message: '请描述招聘原因', trigger: 'blur' }
        ]
      }
    }
  },
  
  async created() {
    // 加载当前用户信息
    await this.loadCurrentUserInfo()
  },
  
  methods: {
    async loadCurrentUserInfo() {
      try {
        const user = await UserService.getCurrentUser()
        if (user) {
          this.currentUser = user
          this.formData.applicantName = user.username || ''
          this.formData.department = user.department || ''
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    
    resetForm() {
      this.$refs.onboardingForm.resetFields()
      // 重置用户信息（不随表单重置）
      this.formData.applicantName = this.currentUser?.username || ''
      this.formData.department = this.currentUser?.department || ''
    },
    
    async submitForm() {
      this.$refs.onboardingForm.validate(async (valid) => {
        if (valid) {
          try {
            // 使用Element Plus的loading组件正确方式
            const loading = ElLoading.service({
              lock: true,
              text: '提交中...',
              background: 'rgba(0, 0, 0, 0.7)'
            });
            
            // 准备流程变量
            const variables = {
              ...this.formData,
              applicant: this.formData.applicantName,
              processType: 'onboarding'
            }
            
            // 提交流程申请 - 正确调用startProcessInstance方法，第二个参数是businessKey，第三个参数是variables
            const result = await ProcessDefinitionService.startProcessInstance('employeeOnboard', '', variables)
            
            if (result && (result.processInstanceId || result.data?.processInstanceId)) {
              ElMessage.success('入职申请已成功提交！')
              
              // 提交成功后跳转到任务列表
              setTimeout(() => {
                this.$router.push('/tasks')
              }, 1500)
            } else {
              ElMessage.error('提交失败，未返回流程实例ID')
            }
          } catch (error) {
            console.error('提交入职申请失败:', error)
            ElMessage.error('提交失败，请稍后重试')
          } finally {
            // 关闭loading
            loading.close();
          }
        } else {
          ElMessage.warning('请完整填写所有必填信息')
          return false
        }
      })
    }
  }
}
</script>

<style scoped>
.onboarding-application-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.main-card {
  border-radius: 8px;
  overflow: hidden;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.card-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.el-form {
  background-color: white;
  padding: 24px;
  border-radius: 8px;
}

.el-divider {
  margin: 24px 0;
}

.el-divider__text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-actions {
  margin-top: 32px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

/* 表单元素样式优化 */
.el-input__inner,
.el-select__wrapper {
  border-radius: 4px;
}

.el-form-item__label {
  font-weight: 500;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .onboarding-application-container {
    padding: 16px;
  }
  
  .el-form {
    padding: 16px;
  }
  
  .card-header h1 {
    font-size: 20px;
  }
  
  .el-col {
    width: 100% !important;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style>