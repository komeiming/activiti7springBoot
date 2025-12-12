<template>
  <div class="notification-test-view">
    <div class="page-header">
      <h1>通知发送测试</h1>
    </div>

    <div class="test-container">
      <el-tabs v-model="activeTab" type="card">
        <!-- 邮件测试标签页 -->
        <el-tab-pane label="邮件测试" name="email">
          <el-form
            ref="emailFormRef"
            :model="emailForm"
            :rules="emailRules"
            label-width="120px"
            class="test-form"
          >
            <el-form-item label="接收邮箱" prop="email">
              <el-input v-model="emailForm.email" placeholder="请输入接收邮箱地址，支持多个邮箱以逗号分隔" />
            </el-form-item>
            <el-form-item label="邮件主题" prop="subject">
              <el-input v-model="emailForm.subject" placeholder="请输入邮件主题" />
            </el-form-item>
            <el-form-item label="邮件内容" prop="content">
              <rich-text-editor
                v-model="emailForm.content"
                :placeholder="'请输入邮件内容，支持${变量名}格式的占位符'"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleTestEmail">发送测试邮件</el-button>
              <el-button @click="resetEmailForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 短信测试标签页 -->
        <el-tab-pane label="短信测试" name="sms">
          <el-form
            ref="smsFormRef"
            :model="smsForm"
            :rules="smsRules"
            label-width="120px"
            class="test-form"
          >
            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="smsForm.phone" placeholder="请输入手机号码，支持多个手机号以逗号分隔" />
            </el-form-item>
            <el-form-item label="短信内容" prop="content">
              <sms-editor
                v-model="smsForm.content"
                :placeholder="'请输入短信内容，支持${变量名}格式的占位符'"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleTestSms">发送测试短信</el-button>
              <el-button @click="resetSmsForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 模板测试标签页 -->
        <el-tab-pane label="模板测试" name="template">
          <el-form
            ref="templateTestFormRef"
            :model="templateTestForm"
            :rules="templateTestRules"
            label-width="120px"
            class="test-form"
          >
            <el-form-item label="选择模板" prop="templateId">
              <el-select
                v-model="templateTestForm.templateId"
                placeholder="请选择通知模板"
                filterable
                @change="handleTemplateChange"
              >
                <el-option
                  v-for="template in templates"
                  :key="template.id"
                  :label="`${template.name} (${template.type === 'EMAIL' ? '邮件' : '短信'})`"
                  :value="template.id"
                />
              </el-select>
            </el-form-item>

            <template v-if="selectedTemplate">
              <el-form-item
                :label="selectedTemplate.type === 'EMAIL' ? '接收邮箱' : '手机号码'"
                :prop="selectedTemplate.type === 'EMAIL' ? 'receiverEmail' : 'receiverPhone'"
              >
                <el-input
                  v-model="templateTestForm[selectedTemplate.type === 'EMAIL' ? 'receiverEmail' : 'receiverPhone']"
                  :placeholder="`请输入${selectedTemplate.type === 'EMAIL' ? '接收邮箱' : '手机号码'}，支持多个以逗号分隔`"
                />
              </el-form-item>

              <el-form-item label="变量参数">
                <div v-if="variables.length > 0" class="variables-container">
                  <div v-for="(variable, index) in variables" :key="index" class="variable-item">
                    <el-input
                      v-model="templateVariables[variable]"
                      :placeholder="`请输入${variable}的值`"
                      :prepend="variable"
                    />
                  </div>
                </div>
                <div v-else class="no-variables">
                  <el-empty description="当前模板不包含变量参数" />
                </div>
              </el-form-item>
            </template>

            <el-form-item>
              <el-button type="primary" @click="handleTestTemplate" :disabled="!selectedTemplate">
                使用模板发送测试
              </el-button>
              <el-button @click="resetTemplateTestForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import NotificationTemplateService from '../services/NotificationTemplateService';
import RichTextEditor from '../components/RichTextEditor.vue';
import SmsEditor from '../components/SmsEditor.vue';

export default {
  name: 'NotificationTestView',
  components: {
    RichTextEditor,
    SmsEditor
  },
  data() {
    return {
      activeTab: 'email',
      loading: false,
      templates: [],
      selectedTemplate: null,
      variables: [],
      templateVariables: {},

      // 邮件测试表单
      emailForm: {
        email: '',
        subject: '测试邮件',
        content: '<p>这是一封测试邮件</p>'
      },
      emailRules: {
        email: [
          { required: true, message: '请输入接收邮箱', trigger: 'blur' },
          { validator: this.validateMultipleEmails, trigger: 'blur' }
        ],
        subject: [
          { required: true, message: '请输入邮件主题', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入邮件内容', trigger: 'blur' }
        ]
      },

      // 短信测试表单
      smsForm: {
        phone: '',
        content: '这是一条测试短信'
      },
      smsRules: {
        phone: [
          { required: true, message: '请输入手机号码', trigger: 'blur' },
          { validator: this.validateMultiplePhones, trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入短信内容', trigger: 'blur' }
        ]
      },

      // 模板测试表单
      templateTestForm: {
        templateId: '',
        receiverEmail: '',
        receiverPhone: ''
      },
      templateTestRules: {
        templateId: [
          { required: true, message: '请选择通知模板', trigger: 'change' }
        ],
        receiverEmail: [
          { required: true, message: '请输入接收邮箱', trigger: 'blur', validator: this.validateEmailOrPhone }
        ],
        receiverPhone: [
          { required: true, message: '请输入手机号码', trigger: 'blur', validator: this.validateEmailOrPhone }
        ]
      }
    };
  },
  async mounted() {
    await this.loadTemplates();
  },
  methods: {
    // 加载模板列表
    async loadTemplates() {
      try {
        const response = await NotificationTemplateService.getTemplates();
        // 只显示启用的模板 - 兼容不同的响应格式
        let templates = [];
        if (Array.isArray(response)) {
          // 直接返回数组
          templates = response;
        } else if (typeof response === 'object' && response) {
          // 响应是对象，检查是否有list、data或直接是数组
          if (Array.isArray(response.list)) {
            templates = response.list;
          } else if (Array.isArray(response.data)) {
            templates = response.data;
          } else if (Array.isArray(response.templates)) {
            templates = response.templates;
          }
        }
        this.templates = templates.filter(template => template.status === 'ENABLED');
      } catch (error) {
        console.error('加载模板列表失败:', error);
        this.$message.error('加载模板列表失败');
      }
    },

    // 处理模板选择变化
    async handleTemplateChange(templateId) {
      if (templateId) {
        try {
          const template = await NotificationTemplateService.getTemplate(templateId);
          this.selectedTemplate = template;
          this.extractVariables(template.content);
        } catch (error) {
          this.$message.error('获取模板详情失败');
        }
      } else {
        this.selectedTemplate = null;
        this.variables = [];
        this.templateVariables = {};
      }
    },

    // 提取模板中的变量
    extractVariables(content) {
      const variableRegex = /\$\{([^}]+)\}/g;
      const matches = content.match(variableRegex);
      const variablesSet = new Set();

      if (matches) {
        matches.forEach(match => {
          const varName = match.substring(2, match.length - 1).trim();
          if (varName) {
            variablesSet.add(varName);
          }
        });
      }

      this.variables = Array.from(variablesSet);
      // 初始化变量对象
      this.templateVariables = {};
      this.variables.forEach(varName => {
        this.templateVariables[varName] = '';
      });
    },

    // 验证多个邮箱（支持逗号分隔）
    validateMultipleEmails(rule, value, callback) {
      if (!value) {
        callback();
        return;
      }
      
      const emails = value.split(',').map(email => email.trim());
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      
      for (const email of emails) {
        if (email && !emailRegex.test(email)) {
          callback(new Error('请输入有效的邮箱地址，多个邮箱以逗号分隔'));
          return;
        }
      }
      
      callback();
    },
    
    // 验证多个手机号（支持逗号分隔）
    validateMultiplePhones(rule, value, callback) {
      if (!value) {
        callback();
        return;
      }
      
      const phones = value.split(',').map(phone => phone.trim());
      const phoneRegex = /^1[3-9]\d{9}$/;
      
      for (const phone of phones) {
        if (phone && !phoneRegex.test(phone)) {
          callback(new Error('请输入有效的手机号码，多个手机号以逗号分隔'));
          return;
        }
      }
      
      callback();
    },
    
    // 验证邮箱或手机号（根据模板类型动态验证）
    validateEmailOrPhone(rule, value, callback) {
      if (!this.selectedTemplate) {
        callback();
        return;
      }

      if (this.selectedTemplate.type === 'EMAIL' && rule.field === 'receiverPhone') {
        callback();
        return;
      }

      if (this.selectedTemplate.type === 'SMS' && rule.field === 'receiverEmail') {
        callback();
        return;
      }

      if (!value) {
        callback(new Error('此项为必填项'));
        return;
      }

      if (rule.field === 'receiverEmail') {
        const emails = value.split(',').map(email => email.trim());
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        
        for (const email of emails) {
          if (email && !emailRegex.test(email)) {
            callback(new Error('请输入有效的邮箱地址，多个邮箱以逗号分隔'));
            return;
          }
        }
      }

      if (rule.field === 'receiverPhone') {
        const phones = value.split(',').map(phone => phone.trim());
        const phoneRegex = /^1[3-9]\d{9}$/;
        
        for (const phone of phones) {
          if (phone && !phoneRegex.test(phone)) {
            callback(new Error('请输入有效的手机号码，多个手机号以逗号分隔'));
            return;
          }
        }
      }

      callback();
    },

    // 发送测试邮件
    async handleTestEmail() {
      const formRef = this.$refs.emailFormRef;
      if (!formRef) {
        this.$message.error('表单引用未找到');
        return;
      }

      await formRef.validate(async (valid) => {
        if (valid) {
          this.loading = true;
          try {
            await NotificationTemplateService.testSendEmail(
              this.emailForm.email,
              this.emailForm.subject,
              this.emailForm.content
            );
            this.$message.success('测试邮件发送成功');
            // 跳转到待发送页面
            this.$router.push('/pending-notifications');
          } catch (error) {
            this.$message.error('测试邮件发送失败');
          } finally {
            this.loading = false;
          }
        }
      });
    },

    // 发送测试短信
    async handleTestSms() {
      const formRef = this.$refs.smsFormRef;
      if (!formRef) {
        this.$message.error('表单引用未找到');
        return;
      }

      await formRef.validate(async (valid) => {
        if (valid) {
          this.loading = true;
          try {
            await NotificationTemplateService.testSendSms(
              this.smsForm.phone,
              this.smsForm.content
            );
            this.$message.success('测试短信发送成功');
            // 跳转到待发送页面
            this.$router.push('/pending-notifications');
          } catch (error) {
            this.$message.error('测试短信发送失败');
          } finally {
            this.loading = false;
          }
        }
      });
    },

    // 使用模板发送测试
    async handleTestTemplate() {
      const formRef = this.$refs.templateTestFormRef;
      if (!formRef || !this.selectedTemplate) {
        return;
      }

      await formRef.validate(async (valid) => {
        if (valid) {
          this.loading = true;
          try {
            const params = {
              templateId: this.templateTestForm.templateId,
              receiver: this.selectedTemplate.type === 'EMAIL' ? this.templateTestForm.receiverEmail : this.templateTestForm.receiverPhone,
              params: this.templateVariables
            };

            await NotificationTemplateService.sendNotification(params);
            this.$message.success('使用模板发送测试成功');
            // 跳转到待发送页面
            this.$router.push('/pending-notifications');
          } catch (error) {
            this.$message.error('使用模板发送测试失败');
          } finally {
            this.loading = false;
          }
        }
      });
    },

    // 重置邮件表单
    resetEmailForm() {
      const formRef = this.$refs.emailFormRef;
      if (formRef) {
        formRef.resetFields();
      }
    },

    // 重置短信表单
    resetSmsForm() {
      const formRef = this.$refs.smsFormRef;
      if (formRef) {
        formRef.resetFields();
      }
    },

    // 重置模板测试表单
    resetTemplateTestForm() {
      const formRef = this.$refs.templateTestFormRef;
      if (formRef) {
        formRef.resetFields();
      }
      this.selectedTemplate = null;
      this.variables = [];
      this.templateVariables = {};
    }
  }
};
</script>

<style scoped>
.notification-test-view {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.test-container {
  background: white;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.test-form {
  margin-top: 20px;
}

.variables-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.variable-item {
  width: 100%;
}

.no-variables {
  padding: 20px;
  text-align: center;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>