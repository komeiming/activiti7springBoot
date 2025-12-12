package com.itheima.activiti.controller;


import com.itheima.activiti.dto.CommonResponse;
import com.itheima.activiti.entity.ApiCallLog;
import com.itheima.activiti.entity.MenuOperationLog;
import com.itheima.activiti.entity.TemplateOperationLog;
import com.itheima.activiti.entity.Tenant;
import com.itheima.activiti.mapper.MenuOperationLogMapper;
import com.itheima.activiti.mapper.TemplateOperationLogMapper;
import com.itheima.activiti.service.ApiCallLogService;
import com.itheima.activiti.service.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统监控控制器（管理员使用）
 */
@RestController
@RequestMapping("/api/monitor")
public class SystemMonitorController {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemMonitorController.class);
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private ApiCallLogService apiCallLogService;
    
    @Autowired
    private MenuOperationLogMapper menuOperationLogMapper;
    
    @Autowired
    private TemplateOperationLogMapper templateOperationLogMapper;
    
    /**
     * 获取租户使用统计数据
     */
    @GetMapping("/tenant-stats")
    public CommonResponse<Map<String, Object>> getTenantStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String tenantName,
            @RequestParam(required = false) String serviceModule) {
        try {
            logger.info("获取租户统计数据，参数：startDate={}, endDate={}, tenantName={}, serviceModule={}", 
                    startDate, endDate, tenantName, serviceModule);
            
            // 获取所有租户
            List<Tenant> tenants = tenantService.getAll();
            
            // 构建统计结果
            Map<String, Object> result = new HashMap<>();
            
            // 总租户数
            result.put("totalTenants", tenants.size());
            
            // 构建租户统计列表
            List<Map<String, Object>> tenantStatsList = new ArrayList<>();
            
            // 计算总API调用量、总成功次数、总失败次数、总响应时间
            int totalApiCalls = 0;
            int totalSuccessCount = 0;
            int totalFailCount = 0;
            long totalResponseTime = 0;
            int totalTenantCount = 0;
            
            // 为每个租户获取真实的API调用统计数据
            for (Tenant tenant : tenants) {
                // 构建API调用日志查询参数
                Map<String, Object> apiCallParams = new HashMap<>();
                apiCallParams.put("tenantId", tenant.getId());
                if (startDate != null) {
                    apiCallParams.put("startTime", startDate);
                }
                if (endDate != null) {
                    apiCallParams.put("endTime", endDate);
                }
                
                // 从API调用日志中获取真实数据
                List<ApiCallLog> apiCallLogs = apiCallLogService.getApiCallLogs(apiCallParams);
                
                // 统计该租户的API调用情况
                int apiCalls = apiCallLogs.size();
                int successCount = 0;
                int failCount = 0;
                long sumResponseTime = 0;
                int maxResponseTime = 0;
                LocalDateTime lastCallTime = null;
                
                for (ApiCallLog log : apiCallLogs) {
                    if (log.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                    sumResponseTime += log.getResponseTime();
                    if (log.getResponseTime() > maxResponseTime) {
                        maxResponseTime = (int) log.getResponseTime();
                    }
                    if (lastCallTime == null || log.getCallTime().isAfter(lastCallTime)) {
                        lastCallTime = log.getCallTime();
                    }
                }
                
                // 计算成功率和平均响应时间
                double successRate = apiCalls > 0 ? Math.round((double) successCount / apiCalls * 1000) / 10.0 : 0;
                int avgResponseTime = apiCalls > 0 ? (int) (sumResponseTime / apiCalls) : 0;
                
                // 累加统计数据
                totalApiCalls += apiCalls;
                totalSuccessCount += successCount;
                totalFailCount += failCount;
                totalResponseTime += sumResponseTime;
                totalTenantCount++;
                
                // 构建租户统计数据
                Map<String, Object> tenantStats = new HashMap<>();
                tenantStats.put("tenantId", tenant.getId());
                tenantStats.put("tenantName", tenant.getSystemName() + " (" + tenant.getEnterpriseName() + ")");
                tenantStats.put("serviceModule", "all");
                tenantStats.put("apiCalls", apiCalls);
                tenantStats.put("successCount", successCount);
                tenantStats.put("failCount", failCount);
                tenantStats.put("successRate", successRate);
                tenantStats.put("avgResponseTime", avgResponseTime);
                tenantStats.put("maxResponseTime", maxResponseTime);
                tenantStats.put("lastCallTime", lastCallTime != null ? lastCallTime : LocalDateTime.now());
                
                tenantStatsList.add(tenantStats);
            }
            
            // 计算平均成功率和平均响应时间
            double avgSuccessRate = totalApiCalls > 0 ? Math.round((double) totalSuccessCount / totalApiCalls * 1000) / 10.0 : 0;
            int avgResponseTime = totalApiCalls > 0 ? (int) (totalResponseTime / totalApiCalls) : 0;
            
            // 更新统计结果
            result.put("totalApiCalls", totalApiCalls);
            result.put("avgSuccessRate", avgSuccessRate);
            result.put("avgResponseTime", avgResponseTime);
            result.put("tenantStatsList", tenantStatsList);
            
            // 生成API调用趋势数据（最近10天）
            List<Map<String, Object>> apiTrend = new ArrayList<>();
            for (int i = 9; i >= 0; i--) {
                // 计算日期
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.getMonthValue() + "-" + date.getDayOfMonth();
                
                // 查询该日期的API调用日志
                Map<String, Object> dailyParams = new HashMap<>();
                dailyParams.put("startTime", date.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                dailyParams.put("endTime", date.atTime(23, 59, 59).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                List<ApiCallLog> dailyApiCallLogs = apiCallLogService.getApiCallLogs(dailyParams);
                
                // 统计该日期的API调用情况
                int dailyCallCount = dailyApiCallLogs.size();
                int dailySuccessCount = (int) dailyApiCallLogs.stream().filter(ApiCallLog::isSuccess).count();
                
                // 统计该日期的活跃租户数
                Set<String> activeTenants = dailyApiCallLogs.stream().map(ApiCallLog::getTenantId).collect(Collectors.toSet());
                int dailyTenantCount = activeTenants.size();
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dateStr);
                dayData.put("callCount", dailyCallCount);
                dayData.put("successCount", dailySuccessCount);
                dayData.put("tenantCount", dailyTenantCount);
                apiTrend.add(dayData);
            }
            result.put("apiTrend", apiTrend);
            
            // 生成服务模块分布数据
            // 从API调用日志中统计不同服务模块的调用量
            Map<String, Integer> moduleCallCountMap = new HashMap<>();
            Map<String, Object> moduleParams = new HashMap<>();
            if (startDate != null) {
                moduleParams.put("startTime", startDate);
            }
            if (endDate != null) {
                moduleParams.put("endTime", endDate);
            }
            
            List<ApiCallLog> allApiCallLogs = apiCallLogService.getApiCallLogs(moduleParams);
            for (ApiCallLog log : allApiCallLogs) {
                String apiPath = log.getApiPath();
                String module = "other";
                if (apiPath.contains("/notification/") || apiPath.contains("/template/")) {
                    module = "notification";
                } else if (apiPath.contains("/workflow/") || apiPath.contains("/process/") || apiPath.contains("/task/")) {
                    module = "workflow";
                }
                
                moduleCallCountMap.put(module, moduleCallCountMap.getOrDefault(module, 0) + 1);
            }
            
            List<Map<String, Object>> moduleDistribution = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : moduleCallCountMap.entrySet()) {
                String module = entry.getKey();
                int count = entry.getValue();
                int percentage = totalApiCalls > 0 ? (int) (count * 100.0 / totalApiCalls) : 0;
                
                Map<String, Object> moduleData = new HashMap<>();
                moduleData.put("module", module);
                moduleData.put("count", count);
                moduleData.put("percentage", percentage);
                moduleDistribution.add(moduleData);
            }
            result.put("moduleDistribution", moduleDistribution);
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("获取租户统计数据失败:", e);
            return CommonResponse.fail("获取租户统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取API调用统计数据
     */
    @GetMapping("/api-stats")
    public CommonResponse<Map<String, Object>> getApiStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String apiName,
            @RequestParam(required = false) String serviceModule) {
        try {
            logger.info("获取API调用统计数据，参数：startDate={}, endDate={}, apiName={}, serviceModule={}", 
                    startDate, endDate, apiName, serviceModule);
            
            // 构建API调用日志查询参数
            Map<String, Object> apiCallParams = new HashMap<>();
            if (startDate != null) {
                apiCallParams.put("startTime", startDate);
            }
            if (endDate != null) {
                apiCallParams.put("endTime", endDate);
            }
            // TODO: 根据API名称查询（需要在ApiCallLogService中添加支持）
            // if (apiName != null) {
            //     apiCallParams.put("apiName", apiName);
            // }
            
            // 从API调用日志中获取真实数据
            List<ApiCallLog> apiCallLogs = apiCallLogService.getApiCallLogs(apiCallParams);
            
            // 构建统计结果
            Map<String, Object> result = new HashMap<>();
            
            // 总API调用量
            int totalApiCalls = apiCallLogs.size();
            result.put("totalApiCalls", totalApiCalls);
            
            // 总成功次数和总失败次数
            int totalSuccessCalls = (int) apiCallLogs.stream().filter(ApiCallLog::isSuccess).count();
            int totalFailCalls = totalApiCalls - totalSuccessCalls;
            result.put("totalSuccessCalls", totalSuccessCalls);
            result.put("totalFailCalls", totalFailCalls);
            
            // 平均成功率
            double avgSuccessRate = totalApiCalls > 0 ? Math.round((double) totalSuccessCalls / totalApiCalls * 1000) / 10.0 : 0;
            result.put("avgSuccessRate", avgSuccessRate);
            
            // 平均响应时间
            long totalResponseTime = apiCallLogs.stream().mapToLong(ApiCallLog::getResponseTime).sum();
            int avgResponseTime = totalApiCalls > 0 ? (int) (totalResponseTime / totalApiCalls) : 0;
            result.put("avgResponseTime", avgResponseTime);
            
            // 最大响应时间和最小响应时间
            OptionalLong maxResponseTime = apiCallLogs.stream().mapToLong(ApiCallLog::getResponseTime).max();
            OptionalLong minResponseTime = apiCallLogs.stream().mapToLong(ApiCallLog::getResponseTime).min();
            result.put("maxResponseTime", maxResponseTime.isPresent() ? (int) maxResponseTime.getAsLong() : 0);
            result.put("minResponseTime", minResponseTime.isPresent() ? (int) minResponseTime.getAsLong() : 0);
            
            // 活跃API数量
            long activeApis = apiCallLogs.stream().map(ApiCallLog::getApiPath).distinct().count();
            result.put("activeApis", activeApis);
            
            // API调用趋势（最近10天）
            List<Map<String, Object>> apiTrend = new ArrayList<>();
            for (int i = 9; i >= 0; i--) {
                // 计算日期
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.getMonthValue() + "-" + date.getDayOfMonth();
                
                // 查询该日期的API调用日志
                Map<String, Object> dailyParams = new HashMap<>();
                dailyParams.put("startTime", date.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                dailyParams.put("endTime", date.atTime(23, 59, 59).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                List<ApiCallLog> dailyApiCallLogs = apiCallLogService.getApiCallLogs(dailyParams);
                
                // 统计该日期的API调用情况
                int dailyCallCount = dailyApiCallLogs.size();
                int dailySuccessCount = (int) dailyApiCallLogs.stream().filter(ApiCallLog::isSuccess).count();
                
                // 统计该日期的活跃租户数
                Set<String> activeTenants = dailyApiCallLogs.stream().map(ApiCallLog::getTenantId).collect(Collectors.toSet());
                int dailyTenantCount = activeTenants.size();
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dateStr);
                dayData.put("totalCalls", dailyCallCount);
                dayData.put("successCalls", dailySuccessCount);
                dayData.put("failCalls", dailyCallCount - dailySuccessCount);
                dayData.put("tenantCount", dailyTenantCount);
                apiTrend.add(dayData);
            }
            result.put("trend", apiTrend);
            
            // 服务模块分布
            Map<String, Integer> moduleCallCountMap = new HashMap<>();
            for (ApiCallLog log : apiCallLogs) {
                String apiPath = log.getApiPath();
                String module = "other";
                if (apiPath.contains("/notification/") || apiPath.contains("/template/")) {
                    module = "notification";
                } else if (apiPath.contains("/workflow/") || apiPath.contains("/process/") || apiPath.contains("/task/")) {
                    module = "workflow";
                }
                
                moduleCallCountMap.put(module, moduleCallCountMap.getOrDefault(module, 0) + 1);
            }
            
            List<Map<String, Object>> moduleDistribution = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : moduleCallCountMap.entrySet()) {
                String module = entry.getKey();
                int count = entry.getValue();
                int percentage = totalApiCalls > 0 ? (int) (count * 100.0 / totalApiCalls) : 0;
                
                Map<String, Object> moduleData = new HashMap<>();
                moduleData.put("module", module);
                moduleData.put("count", count);
                moduleData.put("percentage", percentage);
                moduleDistribution.add(moduleData);
            }
            result.put("moduleDistribution", moduleDistribution);
            
            // API调用排行（按API路径分组统计）
            Map<String, List<ApiCallLog>> apiPathGroupedLogs = apiCallLogs.stream()
                    .collect(Collectors.groupingBy(ApiCallLog::getApiPath));
            
            List<Map<String, Object>> ranking = new ArrayList<>();
            for (Map.Entry<String, List<ApiCallLog>> entry : apiPathGroupedLogs.entrySet()) {
                String apiPath = entry.getKey();
                List<ApiCallLog> groupedLogs = entry.getValue();
                
                int callCount = groupedLogs.size();
                int successCount = (int) groupedLogs.stream().filter(ApiCallLog::isSuccess).count();
                double successRate = callCount > 0 ? Math.round((double) successCount / callCount * 1000) / 10.0 : 0;
                
                Map<String, Object> apiStats = new HashMap<>();
                apiStats.put("apiName", apiPath); // 暂时使用API路径作为API名称
                apiStats.put("callCount", callCount);
                apiStats.put("successCount", successCount);
                apiStats.put("failCount", callCount - successCount);
                apiStats.put("successRate", successRate);
                
                ranking.add(apiStats);
            }
            
            // 按调用次数降序排序
            ranking.sort((a, b) -> Integer.compare((int) b.get("callCount"), (int) a.get("callCount")));
            result.put("ranking", ranking);
            
            // 响应时间分布
            List<Map<String, Object>> distribution = new ArrayList<>();
            // 统计不同响应时间区间的数量
            int lessThan50ms = 0;
            int between50And100ms = 0;
            int between100And200ms = 0;
            int between200And500ms = 0;
            int moreThan500ms = 0;
            
            for (ApiCallLog log : apiCallLogs) {
                long responseTime = log.getResponseTime();
                if (responseTime < 50) {
                    lessThan50ms++;
                } else if (responseTime < 100) {
                    between50And100ms++;
                } else if (responseTime < 200) {
                    between100And200ms++;
                } else if (responseTime < 500) {
                    between200And500ms++;
                } else {
                    moreThan500ms++;
                }
            }
            
            distribution.add(Map.of("name", "50ms以下", "value", lessThan50ms));
            distribution.add(Map.of("name", "50-100ms", "value", between50And100ms));
            distribution.add(Map.of("name", "100-200ms", "value", between100And200ms));
            distribution.add(Map.of("name", "200-500ms", "value", between200And500ms));
            distribution.add(Map.of("name", "500ms以上", "value", moreThan500ms));
            result.put("distribution", distribution);
            
            // API调用详情列表（表格数据）
            List<Map<String, Object>> apiCallLogList = new ArrayList<>();
            for (ApiCallLog log : apiCallLogs) {
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("logId", log.getId());
                logMap.put("apiPath", log.getApiPath());
                logMap.put("requestMethod", log.getRequestMethod());
                logMap.put("requestParams", log.getRequestParams());
                logMap.put("responseData", log.getResponseData());
                logMap.put("statusCode", log.isSuccess() ? 200 : 500); // 简化处理，实际应该从responseData中解析
                logMap.put("success", log.isSuccess());
                logMap.put("responseTime", log.getResponseTime());
                logMap.put("clientIp", log.getClientIp());
                logMap.put("callTime", log.getCallTime());
                logMap.put("tenantId", log.getTenantId());
                logMap.put("errorMessage", log.getErrorMessage());
                
                apiCallLogList.add(logMap);
            }
            result.put("apiCallLogs", apiCallLogList);
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("获取API调用统计数据失败:", e);
            return CommonResponse.fail("获取API调用统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取系统操作日志
     */
    @GetMapping("/system-logs")
    public CommonResponse<Map<String, Object>> getSystemLogs(
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("获取系统操作日志，参数：operator={}, operationType={}, startDate={}, endDate={}, page={}, size={}", 
                    operator, operationType, startDate, endDate, page, size);
            
            // 构建统一的日志列表
            List<Map<String, Object>> allLogs = new ArrayList<>();
            
            // 查询菜单操作日志 - 注意：当前Mapper没有分页和时间范围查询方法，后续可以优化
            List<MenuOperationLog> menuOperationLogs = menuOperationLogMapper.findAll();
            for (MenuOperationLog log : menuOperationLogs) {
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("id", "MENU_LOG_" + log.getId());
                logMap.put("operator", log.getOperator() != null ? log.getOperator() : "admin");
                logMap.put("operationType", log.getOperationType() != null ? log.getOperationType() : "unknown");
                logMap.put("targetType", "menu");
                logMap.put("targetId", log.getMenuId() != null ? log.getMenuId().toString() : "");
                logMap.put("targetName", "菜单" + (log.getMenuId() != null ? log.getMenuId() : ""));
                logMap.put("description", log.getDescription() != null ? log.getDescription() : "菜单" + getOperationTypeName(log.getOperationType()));
                logMap.put("ipAddress", log.getIpAddress() != null ? log.getIpAddress() : "127.0.0.1");
                logMap.put("operationTime", log.getOperationTime());
                logMap.put("details", log.getOldData() != null || log.getNewData() != null ? 
                        Map.of("oldData", log.getOldData(), "newData", log.getNewData()) : null);
                allLogs.add(logMap);
            }
            
            // 查询模板操作日志
            Map<String, Object> templateParams = new HashMap<>();
            if (operator != null) {
                templateParams.put("operator", operator);
            }
            if (operationType != null) {
                templateParams.put("operationType", operationType);
            }
            // 添加时间范围查询
            if (startDate != null) {
                templateParams.put("startTime", startDate);
            }
            if (endDate != null) {
                templateParams.put("endTime", endDate);
            }
            List<TemplateOperationLog> templateOperationLogs = templateOperationLogMapper.selectByParams(templateParams);
            for (TemplateOperationLog log : templateOperationLogs) {
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("id", "TEMPLATE_LOG_" + log.getId());
                logMap.put("operator", log.getOperator() != null ? log.getOperator() : "admin");
                logMap.put("operationType", log.getOperationType() != null ? log.getOperationType() : "unknown");
                logMap.put("targetType", "template");
                logMap.put("targetId", log.getTemplateId() != null ? log.getTemplateId() : "");
                logMap.put("targetName", log.getTemplateName() != null ? log.getTemplateName() : "模板" + log.getId());
                logMap.put("description", "模板" + getOperationTypeName(log.getOperationType()) + ": " + (log.getTemplateName() != null ? log.getTemplateName() : "模板" + log.getId()));
                logMap.put("ipAddress", log.getIpAddress() != null ? log.getIpAddress() : "127.0.0.1");
                logMap.put("operationTime", log.getOperationTime());
                logMap.put("details", log.getOperationDetails() != null ? log.getOperationDetails() : "无详细信息");
                allLogs.add(logMap);
            }
            
            // TODO: 查询其他类型的操作日志（如用户操作日志、角色操作日志等）
            
            // 按操作时间降序排序
            allLogs.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("operationTime");
                LocalDateTime timeB = (LocalDateTime) b.get("operationTime");
                return timeB.compareTo(timeA); // 降序排序
            });
            
            // 过滤日志
            List<Map<String, Object>> filteredLogs = allLogs;
            
            // 按操作人过滤
            if (operator != null) {
                filteredLogs = filteredLogs.stream()
                        .filter(log -> operator.equals(log.get("operator")))
                        .collect(Collectors.toList());
            }
            
            // 按操作类型过滤
            if (operationType != null) {
                filteredLogs = filteredLogs.stream()
                        .filter(log -> operationType.equals(log.get("operationType")))
                        .collect(Collectors.toList());
            }
            
            // 按时间范围过滤
            if (startDate != null || endDate != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime startDateTime = startDate != null ? LocalDateTime.parse(startDate, formatter) : null;
                LocalDateTime endDateTime = endDate != null ? LocalDateTime.parse(endDate, formatter) : null;
                
                filteredLogs = filteredLogs.stream()
                        .filter(log -> {
                            LocalDateTime logTime = (LocalDateTime) log.get("operationTime");
                            boolean afterStart = startDateTime == null || logTime.isAfter(startDateTime) || logTime.isEqual(startDateTime);
                            boolean beforeEnd = endDateTime == null || logTime.isBefore(endDateTime) || logTime.isEqual(endDateTime);
                            return afterStart && beforeEnd;
                        })
                        .collect(Collectors.toList());
            }
            
            // 分页
            int total = filteredLogs.size();
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, total);
            List<Map<String, Object>> paginatedLogs = startIndex < endIndex ? filteredLogs.subList(startIndex, endIndex) : new ArrayList<>();
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("data", paginatedLogs);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("获取系统操作日志失败:", e);
            return CommonResponse.fail("获取系统操作日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取操作类型名称
     */
    private String getOperationTypeName(String type) {
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("create", "创建");
        typeMap.put("update", "更新");
        typeMap.put("delete", "删除");
        typeMap.put("enable", "启用");
        typeMap.put("disable", "禁用");
        typeMap.put("export", "导出");
        typeMap.put("import", "导入");
        typeMap.put("batch_delete", "批量删除");
        return typeMap.getOrDefault(type, type);
    }
    
    // ================ 告警管理API ================
    
    /**
     * 获取告警管理数据
     */
    @GetMapping("/alerts")
    public CommonResponse<Map<String, Object>> getAlerts(
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            logger.info("获取告警管理数据，参数：alertType={}, status={}, startTime={}, endTime={}", 
                    alertType, status, startTime, endTime);
            
            // TODO: 实际实现中，从告警表中获取真实数据
            // 这里返回模拟数据，确保前端页面不会404
            Map<String, Object> result = new HashMap<>();
            
            // 告警统计
            result.put("totalAlerts", 15);
            result.put("pendingAlerts", 5);
            result.put("processedAlerts", 10);
            result.put("todayAlerts", 3);
            
            // 告警历史记录
            List<Map<String, Object>> alertHistory = new ArrayList<>();
            alertHistory.add(Map.of(
                    "alertId", "ALERT_001",
                    "ruleName", "调用失败率超限",
                    "alertType", "failure-rate",
                    "triggerCondition", "调用失败率>5%持续10分钟",
                    "triggerTime", "2025-12-10 14:30:00",
                    "alertContent", "调用失败率达到6.5%，超过阈值5%",
                    "status", "pending",
                    "processedTime", null,
                    "tenantId", "TENANT_001",
                    "tenantName", "测试租户1"
            ));
            alertHistory.add(Map.of(
                    "alertId", "ALERT_002",
                    "ruleName", "响应时间超限",
                    "alertType", "response-time",
                    "triggerCondition", "平均响应时间>500ms持续5分钟",
                    "triggerTime", "2025-12-10 13:20:00",
                    "alertContent", "平均响应时间达到650ms，超过阈值500ms",
                    "status", "processed",
                    "processedTime", "2025-12-10 13:30:00",
                    "tenantId", "TENANT_002",
                    "tenantName", "测试租户2"
            ));
            alertHistory.add(Map.of(
                    "alertId", "ALERT_003",
                    "ruleName", "调用量突增",
                    "alertType", "call-spike",
                    "triggerCondition", "调用量环比增长>200%",
                    "triggerTime", "2025-12-10 12:15:00",
                    "alertContent", "调用量环比增长达到350%，超过阈值200%",
                    "status", "pending",
                    "processedTime", null,
                    "tenantId", "TENANT_001",
                    "tenantName", "测试租户1"
            ));
            
            result.put("alertHistory", alertHistory);
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("获取告警管理数据失败:", e);
            return CommonResponse.fail("获取告警管理数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取告警规则列表
     */
    @GetMapping("/alert-rules")
    public CommonResponse<Map<String, Object>> getAlertRules() {
        try {
            logger.info("获取告警规则列表");
            
            // TODO: 实际实现中，从告警规则表中获取真实数据
            // 这里返回模拟数据
            Map<String, Object> result = new HashMap<>();
            
            List<Map<String, Object>> rules = new ArrayList<>();
            rules.add(Map.of(
                    "ruleId", "RULE_001",
                    "ruleName", "调用失败率超限",
                    "alertType", "failure-rate",
                    "triggerCondition", "调用失败率>5%持续10分钟",
                    "alertMethod", List.of("sms", "email"),
                    "recipients", "13800138000,admin@example.com",
                    "webhookUrl", "",
                    "status", "启用",
                    "tenantId", "TENANT_001",
                    "tenantName", "测试租户1"
            ));
            rules.add(Map.of(
                    "ruleId", "RULE_002",
                    "ruleName", "响应时间超限",
                    "alertType", "response-time",
                    "triggerCondition", "平均响应时间>500ms持续5分钟",
                    "alertMethod", List.of("email"),
                    "recipients", "admin@example.com",
                    "webhookUrl", "",
                    "status", "禁用",
                    "tenantId", "TENANT_002",
                    "tenantName", "测试租户2"
            ));
            
            result.put("data", rules);
            result.put("total", rules.size());
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("获取告警规则列表失败:", e);
            return CommonResponse.fail("获取告警规则列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取告警历史记录
     */
    @GetMapping("/alert-history")
    public CommonResponse<Map<String, Object>> getAlertHistory(
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("获取告警历史记录，参数：alertType={}, status={}, startTime={}, endTime={}, page={}, size={}", 
                    alertType, status, startTime, endTime, page, size);
            
            // TODO: 实际实现中，从告警历史表中获取真实数据
            // 这里返回模拟数据
            Map<String, Object> result = new HashMap<>();
            
            List<Map<String, Object>> history = new ArrayList<>();
            history.add(Map.of(
                    "alertId", "ALERT_001",
                    "ruleName", "调用失败率超限",
                    "alertType", "failure-rate",
                    "triggerCondition", "调用失败率>5%持续10分钟",
                    "triggerTime", "2025-12-10 14:30:00",
                    "alertContent", "调用失败率达到6.5%，超过阈值5%",
                    "status", "pending",
                    "processedTime", null
            ));
            history.add(Map.of(
                    "alertId", "ALERT_002",
                    "ruleName", "响应时间超限",
                    "alertType", "response-time",
                    "triggerCondition", "平均响应时间>500ms持续5分钟",
                    "triggerTime", "2025-12-10 13:20:00",
                    "alertContent", "平均响应时间达到650ms，超过阈值500ms",
                    "status", "processed",
                    "processedTime", "2025-12-10 13:30:00"
            ));
            
            result.put("data", history);
            result.put("total", history.size());
            result.put("page", page);
            result.put("size", size);
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            logger.error("获取告警历史记录失败:", e);
            return CommonResponse.fail("获取告警历史记录失败: " + e.getMessage());
        }
    }
}