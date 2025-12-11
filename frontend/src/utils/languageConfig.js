// 系统多语言配置

// 系统名称和页面标题配置
const systemTitles = {
  zh: {
    systemName: '零售门店业务管理平台',
    pageTitles: {
      dashboard: '零售门店业务管理平台 - 仪表盘',
      UserManagement: '零售门店业务管理平台 - 用户管理',
      RoleManagement: '零售门店业务管理平台 - 角色管理',
      roleMenuPermission: '零售门店业务管理平台 - 角色菜单权限',
      menuManagement: '零售门店业务管理平台 - 菜单管理',
      languageManagement: '零售门店业务管理平台 - 语言管理',
      NotificationTemplateManagement: '零售门店业务管理平台 - 通知模板管理',
      NotificationTestView: '零售门店业务管理平台 - 通知测试',
      NotificationLogView: '零售门店业务管理平台 - 通知日志',
      PendingNotificationView: '零售门店业务管理平台 - 待发送通知',
      leaveApplication: '零售门店业务管理平台 - 请假申请',
      onboardingApplication: '零售门店业务管理平台 - 入职申请',
      ProcessDefinitionManagement: '零售门店业务管理平台 - 流程管理',
      userProcessDefinition: '零售门店业务管理平台 - 我的流程定义',
      genericProcessApply: '零售门店业务管理平台 - 流程申请',
      processHistory: '零售门店业务管理平台 - 流程历史',
      approvalCenter: '零售门店业务管理平台 - 审批中心',
      tasks: '零售门店业务管理平台 - 待办任务',
      enhancedTaskList: '零售门店业务管理平台 - 我的待办',
      completedTasks: '零售门店业务管理平台 - 已办任务'
    }
  },
  en: {
    systemName: 'Retail Store Business Management Platform',
    pageTitles: {
      dashboard: 'Retail Store Business Management Platform - Dashboard',
      UserManagement: 'Retail Store Business Management Platform - User Management',
      RoleManagement: 'Retail Store Business Management Platform - Role Management',
      roleMenuPermission: 'Retail Store Business Management Platform - Role Menu Permission',
      menuManagement: 'Retail Store Business Management Platform - Menu Management',
      languageManagement: 'Retail Store Business Management Platform - Language Management',
      NotificationTemplateManagement: 'Retail Store Business Management Platform - Notification Template Management',
      NotificationTestView: 'Retail Store Business Management Platform - Notification Test',
      NotificationLogView: 'Retail Store Business Management Platform - Notification Log',
      PendingNotificationView: 'Retail Store Business Management Platform - Pending Notifications',
      leaveApplication: 'Retail Store Business Management Platform - Leave Application',
      onboardingApplication: 'Retail Store Business Management Platform - Onboarding Application',
      ProcessDefinitionManagement: 'Retail Store Business Management Platform - Process Management',
      userProcessDefinition: 'Retail Store Business Management Platform - My Process Definitions',
      genericProcessApply: 'Retail Store Business Management Platform - Process Application',
      processHistory: 'Retail Store Business Management Platform - Process History',
      approvalCenter: 'Retail Store Business Management Platform - Approval Center',
      tasks: 'Retail Store Business Management Platform - My Tasks',
      enhancedTaskList: 'Retail Store Business Management Platform - My Tasks',
      completedTasks: 'Retail Store Business Management Platform - Completed Tasks'
    }
  }
};

// 获取当前语言对应的系统名称
export const getSystemName = (languageCode = 'zh-CN') => {
  // 将zh-CN转换为zh，en-US转换为en
  const lang = languageCode.split('-')[0];
  return systemTitles[lang]?.systemName || systemTitles.zh.systemName;
};

// 获取当前语言对应的页面标题
export const getPageTitle = (routeName, languageCode = 'zh-CN') => {
  // 将zh-CN转换为zh，en-US转换为en
  const lang = languageCode.split('-')[0];
  return systemTitles[lang]?.pageTitles[routeName] || 
         systemTitles.zh?.pageTitles[routeName] || 
         systemTitles.zh.systemName;
};

// 获取当前语言的完整配置
export const getLanguageConfig = (languageCode = 'zh-CN') => {
  // 将zh-CN转换为zh，en-US转换为en
  const lang = languageCode.split('-')[0];
  return systemTitles[lang] || systemTitles.zh;
};

export default systemTitles;
