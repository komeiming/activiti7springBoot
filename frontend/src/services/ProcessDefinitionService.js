import { ElMessage, ElMessageBox } from 'element-plus'
import userService from './UserService'
import axios from '../utils/axiosConfig'

// 流程定义服务类
class ProcessDefinitionService {
  // 获取流程定义列表
  async getProcessDefinitions(page = 1, size = 10, filters = {}) {
    try {
      const params = {
        page,
        size,
        ...filters
      }
      
      console.log('[ProcessDefinitionService] 调用流程定义API:', '/api/process/definition/list', '参数:', params)
      const response = await axios.get('/api/process/definition/list', { params })
      
      console.log('[ProcessDefinitionService] 流程定义API响应状态:', response.status)
      // 首先打印原始响应，帮助调试 - 打印整个response对象
      console.log('[ProcessDefinitionService] 完整API响应:', JSON.stringify(response, null, 2))
      
      // 情况1: 后端返回格式为 {total, rows, page}，这是后端实现的格式（直接在response对象中）
      if (response.rows && Array.isArray(response.rows)) {
        console.log('[ProcessDefinitionService] 检测到response.rows格式')
        const result = {
          data: response.rows,
          total: response.total || response.rows.length,
          page: response.page || page,
          pageSize: response.pageSize || size,
          originalResponse: response
        }
        console.log('[ProcessDefinitionService] 转换后的返回结果:', result)
        return result
      }
      // 情况2: 后端返回格式为 {total, rows, page}，这是后端实现的格式（在response.data对象中）
      else if (response.data && response.data.rows && Array.isArray(response.data.rows)) {
        console.log('[ProcessDefinitionService] 响应数据包含rows字段，进行转换')
        const result = {
          data: response.data.rows,
          total: response.data.total || response.data.rows.length,
          page: response.data.page || page,
          pageSize: response.data.pageSize || size,
          originalResponse: response.data
        }
        console.log('[ProcessDefinitionService] 转换后的返回结果:', result)
        return result
      }
      // 检查data字段是否存在
      else if (response.data && response.data.data) {
        return response.data
      }
      // 如果是直接的数组
      else if (Array.isArray(response.data)) {
        console.log('[ProcessDefinitionService] 响应数据直接是数组，进行转换')
        return { data: response.data, total: response.data.length }
      }
      // 其他情况，直接返回（包含success、total等字段的完整响应）
      else if (response.data) {
        return response.data
      }
      
      console.error('[ProcessDefinitionService] API返回格式异常:', response)
      return { data: [], total: 0 }
    } catch (error) {
      console.error('[ProcessDefinitionService] 获取流程定义列表失败:', error)
      console.error('[ProcessDefinitionService] 错误详情:', error.response ? JSON.stringify(error.response.data, null, 2) : (error.message || '未知错误'))
      return { data: [], total: 0 }
    }
  }

  // 上传并部署BPMN文件
  async deployProcess(file, deploymentName = '') {
    try {
      // 检查文件类型
      if (!file.name.endsWith('.bpmn') && !file.name.endsWith('.bpmn20.xml') && !file.name.endsWith('.zip')) {
        ElMessage.warning('请上传BPMN文件(.bpmn, .bpmn20.xml)或ZIP压缩包')
        return false
      }
      
      // 创建FormData
      const formData = new FormData()
      formData.append('file', file)
      
      if (deploymentName) {
        formData.append('name', deploymentName)
      } else {
        // 如果没有提供部署名称，使用文件名
        const fileName = file.name.split('.')[0]
        formData.append('name', fileName)
      }
      
      // 发送请求
      const response = await axios.post('/api/process/definition/deploy', formData, {
        onUploadProgress: (progressEvent) => {
          if (progressEvent.total) {
            const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
            console.log(`部署进度: ${percentCompleted}%`)
          }
        }
      })
      
      // 兼容不同的响应格式 - 与axios响应拦截器行为保持一致
      let isSuccess = false
      let errorMessage = ''
      
      if (typeof response === 'object' && response) {
        // 情况1: response直接是对象，检查是否有success字段
        if (response && response.success !== undefined) {
          isSuccess = response.success !== false
          errorMessage = response.message || ''
        }
        // 情况2: response是对象，包含data字段，检查data是否有success字段
        else if (response.data && typeof response.data === 'object') {
          isSuccess = response.data.success !== false
          errorMessage = response.data.message || ''
        }
        // 情况3: response是对象，没有success字段，检查HTTP状态码
        else if (response.status === 200 || response.status === 201) {
          isSuccess = true
        }
      }
      
      if (isSuccess) {
        ElMessage.success('流程部署成功')
        return true
      } else {
        ElMessage.error('流程部署失败: ' + (errorMessage || '未知错误'))
        return false
      }
    } catch (error) {
      console.error('部署流程失败:', error)
      ElMessage.error('部署失败: ' + (error.response?.data?.message || error.message))
      return false
    }
  }
  
  // 用于测试的自动部署leave流程定义
  async deployLeaveProcess() {
    try {
      console.log('[ProcessDefinitionService] 开始部署请假流程定义...')
      
      // 调用后端提供的部署请假流程接口
      console.log('[ProcessDefinitionService] 调用接口: POST /api/process/definition/deploy/leave')
      const response = await axios.post('/api/process/definition/deploy/leave')
      
      console.log('[ProcessDefinitionService] 部署接口响应状态:', response.status)
      console.log('[ProcessDefinitionService] 部署接口响应数据:', JSON.stringify(response.data, null, 2))
      
      if (response && response.data) {
        // 确保返回格式统一，包含success字段
        const result = {
          success: response.data.success !== false, // 默认成功
          message: response.data.message || '请假流程部署成功',
          deploymentId: response.data.deploymentId,
          deployed: true
        }
        console.log('[ProcessDefinitionService] 请假流程部署结果:', result)
        return result
      }
      
      console.log('[ProcessDefinitionService] 请假流程部署成功(默认返回)')
      return { 
        success: true, 
        message: '请假流程已部署成功',
        deployed: true
      }
    } catch (error) {
      console.error('[ProcessDefinitionService] 部署leave流程定义失败:', error)
      console.error('[ProcessDefinitionService] 错误详情:', error.response ? JSON.stringify(error.response.data, null, 2) : error.message)
      return { 
        success: false, 
        message: '流程部署失败: ' + (error.response?.data?.message || error.message),
        error: error.message,
        errorDetail: error.response ? error.response.data : null
      }
    }
  }

  // 启动流程实例
  async startProcessInstance(processDefinitionKey, businessKey = '', variables = {}) {
    try {
      console.log('[ProcessDefinitionService] 开始启动流程实例...');
      
      // 参数验证
      if (!processDefinitionKey || typeof processDefinitionKey !== 'string') {
        console.error('[ProcessDefinitionService] 错误: processDefinitionKey 必须是非空字符串');
        ElMessage.error('启动失败: 流程定义键无效');
        return { 
          success: false, 
          message: '流程定义键无效',
          error: 'processDefinitionKey must be a non-empty string'
        };
      }
      
      // 添加当前用户信息到流程变量
      const userVariables = userService.buildUserProcessVariables() || {};
      const mergedVariables = { 
        ...userVariables, 
        ...variables
      };
      
      // 详细日志记录
      console.log('[ProcessDefinitionService] 流程参数详情:');
      console.log('- processDefinitionKey:', processDefinitionKey);
      console.log('- businessKey:', businessKey || '未提供');
      console.log('- variables 数量:', Object.keys(variables).length);
      console.log('- userVariables 数量:', Object.keys(userVariables).length);
      console.log('- 关键变量检查:');
      console.log('  * manager 变量:', mergedVariables.hasOwnProperty('manager') ? mergedVariables.manager : '未设置');
      console.log('  * applicant 变量:', mergedVariables.hasOwnProperty('applicant') ? mergedVariables.applicant : '未设置');
      
      // 启动流程 - 与后端ProcessDefinitionController匹配
      console.log('[ProcessDefinitionService] 发送请求到后端...');
      const response = await axios.post(
        '/api/process/definition/start',
        mergedVariables,
        { 
          params: { 
            processDefinitionKey,
            businessKey // 可选的业务键
          },
          headers: {
            'Content-Type': 'application/json'
          },
          // 添加超时处理
          timeout: 30000
        }
      );
      
      // 响应处理增强
      console.log('[ProcessDefinitionService] 收到后端响应');
      console.log('[ProcessDefinitionService] 响应状态码:', response.status);
      console.log('[ProcessDefinitionService] 响应数据类型:', typeof response.data);
      
      // 打印完整响应对象以便调试（不仅是data字段）
      console.log('[ProcessDefinitionService] 完整响应对象:', JSON.stringify(response, null, 2));
      
      // 增强的响应处理逻辑 - 支持多种返回格式
      // 1. 首先检查response对象本身是否包含processInstanceId（直接在根级别）
      if (response.processInstanceId) {
        console.log('[ProcessDefinitionService] 成功: 在response根级别获取到processInstanceId:', response.processInstanceId);
        ElMessage.success('流程启动成功！');
        return { 
          success: true, 
          ...response 
        };
      }
      // 2. 然后检查response.data中是否包含processInstanceId
      else if (response.data && response.data.processInstanceId) {
        console.log('[ProcessDefinitionService] 成功: 在response.data中获取到processInstanceId:', response.data.processInstanceId);
        ElMessage.success('流程启动成功！');
        return { 
          success: true, 
          ...response.data 
        };
      }
      // 3. 检查response对象根级别是否有success标志
      else if (response && response.success === true) {
        console.log('[ProcessDefinitionService] 成功: response根级别success标志为true');
        ElMessage.success('流程启动成功！');
        return response;
      }
      // 4. 检查response.data中的success标志
      else if (response.data && response.data.success === true) {
        console.log('[ProcessDefinitionService] 成功: response.data中success标志为true');
        ElMessage.success('流程启动成功！');
        return response.data;
      }
      // 5. 处理后端数据直接在response对象中返回的情况
      else if (response.status === 200 && !response.data) {
        // 检查response对象本身是否包含有价值的数据
        const responseKeys = Object.keys(response);
        if (responseKeys.length > 3) { // 排除默认的status, statusText, headers
          console.log('[ProcessDefinitionService] 注意: 响应没有data字段，但response对象包含其他数据');
          // 检查是否有processInstanceId（即使在response根级别）
          if (response.processInstanceId) {
            console.log('[ProcessDefinitionService] 成功: 在response根级别获取到processInstanceId:', response.processInstanceId);
            ElMessage.success('流程启动成功！');
            return { 
              success: true, 
              ...response 
            };
          }
          // 尝试将整个response作为成功结果返回
          ElMessage.success('流程启动成功！');
          return { 
            success: true, 
            ...response,
            message: '流程实例创建成功'
          };
        } else {
          console.error('[ProcessDefinitionService] 错误: 后端返回空数据');
          ElMessage.error('流程启动失败: 后端返回数据为空');
          return { 
            success: false, 
            message: '后端返回数据为空',
            error: 'Empty response data'
          };
        }
      }
      // 6. 其他错误情况
      else {
        // 智能提取错误信息
        let errorMsg = '未知错误';
        if (response.data && response.data.message) {
          errorMsg = response.data.message;
        } else if (response.message) {
          errorMsg = response.message;
        } else if (response.data && typeof response.data === 'string') {
          errorMsg = response.data;
        }
        
        console.error('[ProcessDefinitionService] 失败: 后端返回错误信息:', errorMsg);
        ElMessage.error('流程启动失败: ' + errorMsg);
        return { 
          success: false, 
          message: errorMsg,
          ...(response.data || response)
        };
      }
    } catch (error) {
      // 增强的错误处理
      console.error('[ProcessDefinitionService] 异常捕获:');
      console.error('错误类型:', error.constructor.name);
      
      // 详细记录错误信息
      if (error.response) {
        // 服务器返回错误状态码
        console.error('HTTP状态码:', error.response.status);
        console.error('响应数据:', JSON.stringify(error.response.data, null, 2));
        console.error('请求配置:', JSON.stringify(error.config, null, 2));
        
        const errorMsg = error.response.data?.message || error.response.statusText || '服务器错误';
        ElMessage.error('启动失败: ' + errorMsg);
        return { 
          success: false, 
          message: errorMsg,
          status: error.response.status,
          error: error.response.data
        };
      } else if (error.request) {
        // 请求已发送但没有收到响应
        console.error('网络错误: 服务器无响应');
        console.error('请求配置:', JSON.stringify(error.config, null, 2));
        ElMessage.error('网络错误: 服务器无响应，请检查网络连接');
        return { 
          success: false, 
          message: '网络错误: 服务器无响应',
          error: 'Network error: No response from server'
        };
      } else {
        // 请求配置出错
        console.error('请求配置错误:', error.message);
        ElMessage.error('启动失败: ' + error.message);
        return { 
          success: false, 
          message: error.message,
          error: error.toString()
        };
      }
    }
  }

  // 挂起流程定义
  async suspendProcessDefinition(processDefinitionId) {
    try {
      const response = await axios.put(`/api/process/definition/${processDefinitionId}/suspend`)
      
      // 兼容不同的响应格式 - 与axios响应拦截器行为保持一致
      let isSuccess = false
      let errorMessage = ''
      
      if (typeof response === 'object' && response) {
        // 情况1: response直接是对象，检查是否有success字段
        if (response && response.success !== undefined) {
          isSuccess = response.success !== false
          errorMessage = response.message || ''
        }
        // 情况2: response是对象，包含data字段，检查data是否有success字段
        else if (response.data && typeof response.data === 'object') {
          isSuccess = response.data.success !== false
          errorMessage = response.data.message || ''
        }
        // 情况3: response是对象，没有success字段，检查HTTP状态码
        else if (response.status === 200 || response.status === 201) {
          isSuccess = true
        }
      }
      
      if (isSuccess) {
        ElMessage.success('流程定义已挂起')
        return true
      } else {
        ElMessage.error('挂起流程定义失败: ' + (errorMessage || '未知错误'))
        return false
      }
    } catch (error) {
      console.error('挂起流程定义失败:', error)
      ElMessage.error('挂起失败: ' + (error.response?.data?.message || error.message))
      return false
    }
  }

  // 激活流程定义
  async activateProcessDefinition(processDefinitionId) {
    try {
      const response = await axios.put(`/api/process/definition/${processDefinitionId}/activate`)
      
      // 兼容不同的响应格式 - 与axios响应拦截器行为保持一致
      let isSuccess = false
      let errorMessage = ''
      
      if (typeof response === 'object' && response) {
        // 情况1: response直接是对象，检查是否有success字段
        if (response && response.success !== undefined) {
          isSuccess = response.success !== false
          errorMessage = response.message || ''
        }
        // 情况2: response是对象，包含data字段，检查data是否有success字段
        else if (response.data && typeof response.data === 'object') {
          isSuccess = response.data.success !== false
          errorMessage = response.data.message || ''
        }
        // 情况3: response是对象，没有success字段，检查HTTP状态码
        else if (response.status === 200 || response.status === 201) {
          isSuccess = true
        }
      }
      
      if (isSuccess) {
        ElMessage.success('流程定义已激活')
        return true
      } else {
        ElMessage.error('激活流程定义失败: ' + (errorMessage || '未知错误'))
        return false
      }
    } catch (error) {
      console.error('激活流程定义失败:', error)
      ElMessage.error('激活失败: ' + (error.response?.data?.message || error.message))
      return false
    }
  }

  // 删除流程定义
  async deleteProcessDefinition(processDefinitionId, cascade = false) {
    try {
      // 二次确认
      const confirmResult = await ElMessageBox.confirm(
        `确定要删除该流程定义吗？${cascade ? '同时删除相关历史数据' : '相关历史数据将被保留'}`,
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      if (confirmResult !== 'confirm') {
        return false
      }
      
      const params = {
        cascade: cascade
      }
      const response = await axios.delete(`/api/process/definition/${processDefinitionId}`, { params })
      
      // 兼容不同的响应格式 - 与axios响应拦截器行为保持一致
      let isSuccess = false
      let errorMessage = ''
      
      if (typeof response === 'object' && response) {
        // 情况1: response直接是对象，检查是否有success字段
        if (response && response.success !== undefined) {
          isSuccess = response.success !== false
          errorMessage = response.message || ''
        }
        // 情况2: response是对象，包含data字段，检查data是否有success字段
        else if (response.data && typeof response.data === 'object') {
          isSuccess = response.data.success !== false
          errorMessage = response.data.message || ''
        }
        // 情况3: response是对象，没有success字段，检查HTTP状态码
        else if (response.status === 200 || response.status === 204) {
          isSuccess = true
        }
      }
      
      if (isSuccess) {
        ElMessage.success('流程定义已删除')
        return true
      } else {
        ElMessage.error('删除流程定义失败: ' + (errorMessage || '未知错误'))
        return false
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除流程定义失败:', error)
        ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message))
      }
      return false
    }
  }

  // 获取流程定义XML内容
  async getProcessDefinitionXml(processDefinitionId) {
    try {
      const response = await axios.get(`/api/process/definition/${processDefinitionId}/xml`)
      
      // 兼容不同的响应格式 - 与axios响应拦截器行为保持一致
      let isSuccess = false
      let errorMessage = ''
      let xmlData = null
      
      if (typeof response === 'object' && response) {
        // 情况1: response直接是对象，检查是否有success字段
        if (response && response.success !== undefined) {
          isSuccess = response.success !== false
          errorMessage = response.message || ''
          xmlData = response.data
        }
        // 情况2: response是对象，包含data字段，检查data是否有success字段
        else if (response.data && typeof response.data === 'object') {
          if (response.data.success !== undefined) {
            isSuccess = response.data.success !== false
            errorMessage = response.data.message || ''
            xmlData = response.data.data
          } else {
            // 情况2.1: response.data是对象，但没有success字段，直接使用data
            isSuccess = true
            xmlData = response.data
          }
        }
        // 情况3: response是对象，没有success字段，直接使用data
        else {
          isSuccess = true
          xmlData = response.data
        }
      }
      
      if (isSuccess && xmlData) {
        return xmlData
      } else {
        ElMessage.error('获取流程XML失败: ' + (errorMessage || '未知错误'))
        return null
      }
    } catch (error) {
      console.error('获取流程XML失败:', error)
      ElMessage.error('获取失败: ' + (error.response?.data?.message || error.message))
      return null
    }
  }

  // 获取流程定义图片
  async getProcessDefinitionDiagram(processDefinitionId) {
    try {
      console.log(`[ProcessDefinitionService] 获取流程定义图片: ${processDefinitionId}`)
      
      // 重新设置responseType为blob，确保能正确处理二进制图片数据
      // 同时处理可能的JSON错误响应
      const response = await axios.get(`/api/process/definition/${processDefinitionId}/diagram`, {
        responseType: 'blob' // 明确指定响应类型为blob
      })
      
      // 如果响应是成功的，将Blob转换为Data URL
      const reader = new FileReader()
      
      return new Promise((resolve, reject) => {
        reader.onload = () => {
          // 检查是否是有效的图片数据
          const result = reader.result
          if (result && typeof result === 'string' && result.startsWith('data:image/')) {
            console.log('[ProcessDefinitionService] 流程图转换成功')
            resolve(result)
          } else {
            // 可能是JSON格式的错误响应被转换为blob
            console.info('[ProcessDefinitionService] 非图片数据，可能是错误信息')
            resolve(null)
          }
        }
        reader.onerror = (e) => {
          console.error('[ProcessDefinitionService] 图片转换失败:', e)
          reject(new Error('图片转换失败'))
        }
        reader.readAsDataURL(response.data)
      })
    } catch (error) {
      // 只记录404和已知错误，不记录其他错误，减少控制台输出
      if (error.response && error.response.status === 404) {
        console.info('[ProcessDefinitionService] 流程图不存在(404)，返回空状态，这是正常情况')
      } else if (error.response && error.response.status >= 200 && error.response.status < 300) {
        // 成功状态码但解析失败，可能是格式问题
        console.info('[ProcessDefinitionService] 流程图获取成功但解析失败，返回空状态')
      }
      // 对于所有错误情况，都返回null，让前端显示空状态，而不是抛出错误
      return null
    }
  }

  // 根据流程实例ID获取流程图URL
  getProcessDiagramUrlByInstanceId(processDefinitionId) {
    // 直接使用传入的ID作为流程定义ID，因为后端没有提供根据流程实例ID获取流程定义ID的API
    // 这个方法现在接受流程定义ID作为参数，而不是流程实例ID
    return `/api/process/definition/${processDefinitionId}/diagram`;
  }
  
  // 兼容旧代码的获取流程图URL方法
  getProcessDiagramUrl(processDefinitionId) {
    return `/api/process/definition/${processDefinitionId}/diagram`
  }

  // 获取最新版本的流程定义
  async getLatestProcessDefinition(processKey) {
    try {
      console.log(`[ProcessDefinitionService] 获取最新流程定义[${processKey}]...`)
      
      // 由于后端没有直接的/latest/{processKey}接口，我们通过获取所有流程定义并筛选来实现
      const response = await this.getProcessDefinitions(1, 100) // 增加size以获取更多流程定义
      
      // 安全地获取流程定义列表，支持多种返回格式
      let processDefinitions = [];
      
      // 情况1: 检查response.data.rows是否为数组（我们修改后的getProcessDefinitions返回格式）
      if (response.data && response.data.rows && Array.isArray(response.data.rows)) {
        processDefinitions = response.data.rows;
      }
      // 情况2: 检查response.data是否为数组
      else if (response.data && Array.isArray(response.data)) {
        processDefinitions = response.data;
      }
      // 情况3: 检查response.rows是否为数组（可能是后端直接返回的格式）
      else if (response.rows && Array.isArray(response.rows)) {
        processDefinitions = response.rows;
      }
      // 情况4: 检查response.originalResponse.rows是否为数组（我们在getProcessDefinitions中保存的原始响应）
      else if (response.originalResponse && response.originalResponse.rows && Array.isArray(response.originalResponse.rows)) {
        processDefinitions = response.originalResponse.rows;
      }
      // 情况5: 如果response是直接的对象数组
      else if (Array.isArray(response)) {
        processDefinitions = response;
      }
      
      console.log(`[ProcessDefinitionService] 响应格式检查结果 - data.rows: ${!!(response.data && response.data.rows)}, data: ${Array.isArray(response.data)}, rows: ${!!(response.rows)}, originalResponse.rows: ${!!(response.originalResponse && response.originalResponse.rows)}`)
      
      console.log(`[ProcessDefinitionService] 获取到的流程定义总数:`, processDefinitions.length)
      
      // 更宽松的查找逻辑，匹配key或name中包含processKey或其相关词汇
      const relatedKeywords = [processKey, processKey.toLowerCase(), processKey.toUpperCase(),
                              processKey.includes('leave') ? '请假' : '',
                              processKey.includes('请假') ? 'leave' : '']
        .filter(Boolean); // 过滤空字符串
      
      console.log(`[ProcessDefinitionService] 使用关键词查找:`, relatedKeywords)
      
      // 查找逻辑：优先精确匹配key，然后是包含匹配
      let targetProcess = processDefinitions.find(
        process => process.key === processKey
      );
      
      // 如果没有精确匹配，尝试包含匹配
      if (!targetProcess) {
        targetProcess = processDefinitions.find(
          process => {
            // 检查key或name是否包含任何相关关键词
            return relatedKeywords.some(keyword => 
              (process.key && process.key.includes(keyword)) ||
              (process.name && process.name.includes(keyword))
            );
          }
        );
      }
      
      // 记录所有可用的流程定义，便于调试
      console.log(`[ProcessDefinitionService] 可用流程定义列表:`, 
        processDefinitions.map(pd => ({ key: pd.key, name: pd.name })));
      
      console.log(`[ProcessDefinitionService] 获取最新流程定义[${processKey}]结果:`, targetProcess)
      return targetProcess
    } catch (error) {
      console.error(`[ProcessDefinitionService] 获取最新流程定义[${processKey}]失败:`, error)
      return null
    }
  }
  
  // 兼容方法：获取最新版本的流程定义（别名方法，兼容LeaveApplicationView.vue中的调用）
  async getProcessDefinitionByKey(processKey) {
    // 直接调用现有的getLatestProcessDefinition方法
    return this.getLatestProcessDefinition(processKey)
  }

  // 获取流程表单属性
  async getProcessFormProperties(processDefinitionId) {
    try {
      const response = await axios.get(`/api/process/definition/${processDefinitionId}/form/properties`)
      
      // 兼容不同的响应格式 - 与axios响应拦截器行为保持一致
      let properties = []
      
      if (typeof response === 'object' && response) {
        // 情况1: response直接是对象，检查是否有success字段
        if (response.success !== undefined) {
          if (response.success) {
            properties = response.data || []
          }
        }
        // 情况2: response是对象，包含data字段，检查data是否有success字段
        else if (response.data && typeof response.data === 'object') {
          if (response.data.success !== undefined) {
            if (response.data.success) {
              properties = response.data.data || []
            }
          } else {
            // 情况2.1: response.data是对象，但没有success字段，直接使用data
            properties = response.data || []
          }
        }
        // 情况3: response是对象，直接使用data
        else {
          properties = response.data || []
        }
      }
      
      return properties
    } catch (error) {
      console.error('获取流程表单属性失败:', error)
      return []
    }
  }

  // 获取活跃流程实例列表
  async getActiveProcessInstances(page = 1, size = 10, userId, filters = {}) {
    try {
      // 构建完整的请求参数，包含所有过滤条件
      const params = {
        userId,
        page,
        size,
        ...filters // 合并所有过滤条件
      }
      
      console.log('[ProcessDefinitionService] 准备请求活跃流程实例:', {
        url: '/api/v1/process-instances/active',
        params
      });
      
      const response = await axios.get('/api/v1/process-instances/active', { params });
      console.log('[ProcessDefinitionService] 活跃流程实例API响应状态:', response.status);
      console.log('[ProcessDefinitionService] 活跃流程实例API响应数据:', JSON.stringify(response, null, 2));
      
      // 处理API响应，确保返回的数据格式一致
      let result = { data: [], total: 0 };
      
      // 情况1: 响应直接是包含data和total的对象（axios拦截器处理后的格式）
      if (response && typeof response === 'object') {
        // 检查是否有data字段，axios拦截器可能已处理
        if (response.data) {
          // 响应是CommonResponse格式，直接使用data
          result = response.data;
        } else {
          // 响应已被拦截器处理，直接使用response
          result = response;
        }
        
        // 确保result包含data和total字段
        if (!result.data || !Array.isArray(result.data)) {
          result.data = [];
        }
        if (result.total === undefined) {
          result.total = result.data.length;
        }
      }
      // 情况2: 响应是数组
      else if (Array.isArray(response)) {
        result = { data: response, total: response.length };
      }
      
      console.log('[ProcessDefinitionService] 处理后的活跃流程实例数据:', result);
      return result;
    } catch (error) {
      console.error('[ProcessDefinitionService] 获取活跃流程实例列表失败:', error);
      console.error('[ProcessDefinitionService] 错误详情:', error.response ? JSON.stringify(error.response.data, null, 2) : (error.message || '未知错误'));
      // 出错时返回空数据，让前端处理
      return { data: [], total: 0 };
    }
  }
  
  // 获取已完成流程实例列表
  async getCompletedProcessInstances(page = 1, size = 10, userId, filters = {}) {
    try {
      // 确保获取当前用户信息
      const currentUser = userService.getCurrentUser();
      console.log('[ProcessDefinitionService] 当前用户信息:', currentUser);
      // 如果没有传入userId，则尝试从当前用户获取
      const finalUserId = userId || currentUser?.id || currentUser?.username || 'admin';
      console.log('[ProcessDefinitionService] 使用的用户ID:', finalUserId);
      
      // 构建完整的请求参数，包含所有过滤条件
      const params = {
        userId: finalUserId,
        page,
        size,
        ...filters // 合并所有过滤条件
      }
      
      console.log('[ProcessDefinitionService] 准备请求已完成流程实例:', {
        url: '/api/v1/process-instances/completed',
        params
      });
      
      const response = await axios.get('/api/v1/process-instances/completed', { params });
      console.log('[ProcessDefinitionService] 已完成流程实例API响应状态:', response.status);
      console.log('[ProcessDefinitionService] 已完成流程实例API响应数据:', JSON.stringify(response, null, 2));
      
      // 处理API响应，确保返回的数据格式一致
      let result = { data: [], total: 0 };
      
      // 情况1: 响应直接是包含data和total的对象（axios拦截器处理后的格式）
      if (response && typeof response === 'object') {
        // 检查是否有data字段，axios拦截器可能已处理
        if (response.data) {
          // 响应是CommonResponse格式，直接使用data
          result = response.data;
        } else {
          // 响应已被拦截器处理，直接使用response
          result = response;
        }
        
        // 确保result包含data和total字段
        if (!result.data || !Array.isArray(result.data)) {
          result.data = [];
        }
        if (result.total === undefined) {
          result.total = result.data.length;
        }
      }
      // 情况2: 响应是数组
      else if (Array.isArray(response)) {
        result = { data: response, total: response.length };
      }
      
      console.log('[ProcessDefinitionService] 处理后的已完成流程实例数据:', result);
      return result;
    } catch (error) {
      console.error('[ProcessDefinitionService] 获取已完成流程实例列表失败:', error);
      console.error('[ProcessDefinitionService] 错误详情:', error.response ? JSON.stringify(error.response.data, null, 2) : (error.message || '未知错误'));
      // 出错时返回空数据，让前端处理
      return { data: [], total: 0 };
    }
  }
  
  // 获取流程实例变量
  async getProcessInstanceVariables(processInstanceId) {
    try {
      console.log('[ProcessDefinitionService] 获取流程实例变量:', processInstanceId)
      const response = await axios.get(`/api/v1/process-instances/${processInstanceId}/variables`)
      
      // 直接返回响应结果，axios拦截器已经处理了CommonResponse格式
      return response || {}
    } catch (error) {
      // 确保错误信息不包含null
      const errorMsg = error.message || error.toString() || '未知错误'
      console.error('[ProcessDefinitionService] 获取流程实例变量失败:', errorMsg)
      // 错误情况下返回空对象，让前端正常显示
      return {}
    }
  }

  // 获取流程实例历史
  async getProcessInstanceHistory(processInstanceId) {
    try {
      console.log('[ProcessDefinitionService] 获取流程实例历史:', processInstanceId)
      const response = await axios.get(`/api/v1/process-instances/${processInstanceId}/history`)
      
      // 兼容不同的响应格式 - 与axios响应拦截器行为保持一致
      let history = { data: [] }
      
      if (typeof response === 'object' && response) {
        // 直接使用response，axios拦截器已经处理了CommonResponse格式
        // 确保返回格式包含data数组
        if (Array.isArray(response)) {
          // 响应直接是数组
          history = { data: response }
        } else if (response.data && Array.isArray(response.data)) {
          // 响应包含data数组
          history = response
        } else {
          // 其他情况，使用默认格式
          history = { data: [] }
        }
      }
      
      return history
    } catch (error) {
      // 确保错误信息不包含null
      const errorMsg = error.message || error.toString() || '未知错误'
      console.error('[ProcessDefinitionService] 获取流程实例历史失败:', errorMsg)
      return { data: [] }
    }
  }
  
  // 根据processInstanceId获取流程实例详情
  async getProcessInstance(processInstanceId) {
    try {
      console.log('[ProcessDefinitionService] 获取流程实例详情:', processInstanceId)
      const response = await axios.get(`/api/v1/process-instances/${processInstanceId}`)
      
      // 兼容不同的响应格式 - 与axios响应拦截器行为保持一致
      let result = null
      
      if (typeof response === 'object' && response) {
        // 由于axios拦截器已经处理了CommonResponse格式，直接返回的是data内容
        result = response
      }
      
      console.log('[ProcessDefinitionService] 流程实例详情:', result)
      return result
    } catch (error) {
      console.error('[ProcessDefinitionService] 获取流程实例详情失败:', error)
      return null
    }
  }

  // 兼容旧代码的获取流程实例列表方法
  async getProcessInstances(params = {}) {
    console.log('[ProcessDefinitionService] 调用getProcessInstances方法，参数:', params);
    try {
      // 支持传入对象参数或分开的参数
      let page = 1
      let size = 10
      let filters = {}
      
      // 判断是对象参数还是分开的参数
      if (typeof params === 'object' && !Array.isArray(params)) {
        // 对象参数格式: {page, pageSize, status, processKey, ...}
        page = params.page || params.currentPage || 1
        size = params.pageSize || params.size || 10
        filters = params
      } else {
        // 分开的参数格式: (page, size, filters)
        page = params
        size = arguments[1] || 10
        filters = arguments[2] || {}
      }
      
      console.log('[ProcessDefinitionService] 处理后的参数:', {page, size, filters});
      
      // 获取当前用户ID
      const currentUser = userService.getCurrentUser();
      console.log('[ProcessDefinitionService] 当前用户信息:', currentUser);
      const userId = filters.userId || currentUser?.id || currentUser?.username || 'admin';
      console.log('[ProcessDefinitionService] 使用的用户ID:', userId);
      
      // 准备传递给getCompletedProcessInstances的过滤条件
      const processFilters = {
        // 提取与流程相关的过滤条件
        processKey: filters.processDefinitionKey || filters.processKey,
        startTimeFrom: filters.startTimeFrom || filters.startTime,
        startTimeTo: filters.startTimeTo || filters.endTime,
        // 添加其他可能的过滤条件
        status: filters.status
      }
      
      // 移除undefined值
      Object.keys(processFilters).forEach(key => {
        if (processFilters[key] === undefined) {
          delete processFilters[key]
        }
      })
      
      // 同时获取活跃和已完成的流程实例
      const [activeInstances, completedInstances] = await Promise.all([
        this.getActiveProcessInstances(page, size, userId, processFilters),
        this.getCompletedProcessInstances(page, size, userId, processFilters)
      ]);
      
      console.log('[ProcessDefinitionService] 获取到的活跃流程实例:', activeInstances);
      console.log('[ProcessDefinitionService] 获取到的已完成流程实例:', completedInstances);
      
      // 合并活跃和已完成的流程实例
      const activeData = activeInstances.data || [];
      const completedData = completedInstances.data || [];
      
      // 合并并按开始时间倒序排序
      const mergedData = [...activeData, ...completedData].sort((a, b) => {
        const timeA = new Date(a.startTime).getTime();
        const timeB = new Date(b.startTime).getTime();
        return timeB - timeA; // 倒序排序
      });
      
      // 计算总数量
      const total = (activeInstances.total || activeData.length) + (completedInstances.total || completedData.length);
      
      // 构建结果对象
      const result = {
        data: mergedData,
        total: total
      };
      
      console.log('[ProcessDefinitionService] 最终返回的数据:', result);
      return result;
    } catch (error) {
      console.error('[ProcessDefinitionService] 获取流程实例列表失败:', error);
      // 错误情况下返回空数据，让前端处理
      return {
        data: [],
        total: 0
      };
    }
  }

  // 删除流程实例
  async deleteProcessInstance(processInstanceId, deleteReason = '删除') {
    try {
      const response = await axios.delete(`/api/v1/process-instances/${processInstanceId}`, {
        params: { deleteReason }
      })
      
      // 兼容不同的响应格式 - 与axios响应拦截器行为保持一致
      let isSuccess = false
      
      if (typeof response === 'object' && response) {
        // 情况1: response直接是对象，检查是否有success字段
        if (response && response.success !== undefined) {
          isSuccess = response.success !== false
        }
        // 情况2: response是对象，包含data字段，检查data是否有success字段
        else if (response.data && typeof response.data === 'object') {
          isSuccess = response.data.success !== false
        }
        // 情况3: response是对象，没有success字段，检查HTTP状态码
        else if (response.status === 200 || response.status === 204) {
          isSuccess = true
        }
      }
      
      if (isSuccess) {
        ElMessage.success('流程实例已删除')
        return true
      }
      return false
    } catch (error) {
      console.error('删除流程实例失败:', error)
      return false
    }
  }

  // 终止流程实例 (使用删除API)
  async terminateProcessInstance(processInstanceId, terminateReason = '终止') {
    try {
      const response = await axios.delete(`/api/v1/process-instances/${processInstanceId}`, {
        params: { deleteReason: terminateReason }
      })
      
      // 兼容不同的响应格式 - 与axios响应拦截器行为保持一致
      let isSuccess = false
      
      if (typeof response === 'object' && response) {
        // 情况1: response直接是对象，检查是否有success字段
        if (response && response.success !== undefined) {
          isSuccess = response.success !== false
        }
        // 情况2: response是对象，包含data字段，检查data是否有success字段
        else if (response.data && typeof response.data === 'object') {
          isSuccess = response.data.success !== false
        }
        // 情况3: response是对象，没有success字段，检查HTTP状态码
        else if (response.status === 200 || response.status === 204) {
          isSuccess = true
        }
      }
      
      if (isSuccess) {
        ElMessage.success('流程实例已终止')
        return true
      }
      return false
    } catch (error) {
      console.error('终止流程实例失败:', error)
      return false
    }
  }
}

// 导出单例
export default new ProcessDefinitionService()