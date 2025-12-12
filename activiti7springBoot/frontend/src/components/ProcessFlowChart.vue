<template>
  <div class="process-flow-chart">
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script>
import { ref, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

export default {
  name: 'ProcessFlowChart',
  props: {
    // 流程定义ID
    processDefinitionId: {
      type: String,
      default: ''
    },
    // 流程名称
    processName: {
      type: String,
      default: ''
    },
    // 流程节点数据，格式：[{ id: 'start', name: '开始', type: 'start' }, { id: 'manager', name: '经理审批', type: 'userTask' }, { id: 'end', name: '结束', type: 'end' }]
    nodes: {
      type: Array,
      default: () => []
    },
    // 流程连线数据，格式：[{ source: 'start', target: 'manager' }, { source: 'manager', target: 'end' }]
    links: {
      type: Array,
      default: () => []
    }
  },
  setup(props) {
    const chartRef = ref(null)
    let chartInstance = null

    // 初始化图表
    const initChart = () => {
      if (!chartRef.value) return

      // 销毁已存在的图表实例
      if (chartInstance) {
        chartInstance.dispose()
      }

      // 创建新的图表实例
      chartInstance = echarts.init(chartRef.value)

      // 基于流程节点和连线数据生成ECharts配置
      const option = generateChartOption(props.nodes, props.links)

      // 设置图表配置
      chartInstance.setOption(option)

      // 添加窗口大小变化监听
      window.addEventListener('resize', handleResize)
    }

    // 生成图表配置
    const generateChartOption = (nodes, links) => {
      // 构建节点数据
      const echartsNodes = nodes.map(node => {
        // 根据节点类型设置不同的样式
        let itemStyle = {}
        let symbol = 'circle'
        let symbolSize = 60

        if (node.type === 'start') {
          // 开始节点
          symbol = 'circle'
          itemStyle = { color: '#52c41a' } // 绿色
        } else if (node.type === 'end') {
          // 结束节点
          symbol = 'circle'
          itemStyle = { color: '#f5222d' } // 红色
        } else if (node.type === 'userTask') {
          // 用户任务节点
          symbol = 'rect'
          symbolSize = [120, 60]
          itemStyle = { color: '#1890ff' } // 蓝色
        } else if (node.type === 'serviceTask') {
          // 服务任务节点
          symbol = 'diamond'
          itemStyle = { color: '#fa8c16' } // 橙色
        } else if (node.type === 'exclusiveGateway') {
          // 排他网关节点
          symbol = 'diamond'
          itemStyle = { color: '#722ed1' } // 紫色
        } else if (node.type === 'parallelGateway') {
          // 并行网关节点
          symbol = 'rect'
          symbolSize = [60, 60]
          itemStyle = { color: '#eb2f96' } // 粉色
        } else {
          // 其他节点
          symbol = 'circle'
          itemStyle = { color: '#a0d911' } // 浅绿色
        }

        return {
          id: node.id,
          name: node.name,
          symbol: symbol,
          symbolSize: symbolSize,
          itemStyle: itemStyle,
          label: {
            show: true,
            formatter: node.name,
            fontSize: 12,
            position: 'top'
          },
          emphasis: {
            label: {
              fontSize: 14,
              fontWeight: 'bold'
            },
            scale: true
          }
        }
      })

      // 构建连线数据
      const echartsLinks = links.map(link => ({
        source: link.source,
        target: link.target,
        lineStyle: {
          color: '#aaa',
          width: 2,
          type: 'solid'
        },
        label: {
          show: false // 可以根据需要显示连线标签
        },
        emphasis: {
          lineStyle: {
            color: '#1890ff',
            width: 3
          }
        }
      }))

      // 返回ECharts配置
      return {
        title: {
          text: props.processName || '流程流转图',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}'
        },
        animationDuration: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'force',
            force: {
              repulsion: 300,
              gravity: 0.1,
              edgeLength: 150
            },
            data: echartsNodes,
            links: echartsLinks,
            roam: true,
            label: {
              show: true
            },
            lineStyle: {
              show: true,
              color: '#aaa',
              width: 2,
              curveness: 0.1
            },
            emphasis: {
              focus: 'adjacency',
              lineStyle: {
                width: 5
              }
              // 禁用节点高亮
            }
          }
        ]
      }
    }

    // 处理窗口大小变化
    const handleResize = () => {
      chartInstance?.resize()
    }

    // 监听节点和连线数据变化
    watch(
      () => [props.nodes, props.links],
      () => {
        nextTick(() => {
          initChart()
        })
      },
      { deep: true }
    )

    // 监听流程定义ID变化
    watch(
      () => props.processDefinitionId,
      () => {
        nextTick(() => {
          initChart()
        })
      }
    )

    // 组件挂载时初始化图表
    onMounted(() => {
      nextTick(() => {
        initChart()
      })
    })

    // 组件卸载时销毁图表
    const beforeUnmount = () => {
      if (chartInstance) {
        chartInstance.dispose()
        chartInstance = null
      }
      window.removeEventListener('resize', handleResize)
    }

    return {
      chartRef,
      initChart,
      beforeUnmount
    }
  }
}
</script>

<style scoped>
.process-flow-chart {
  width: 100%;
  height: 100%;
}

.chart-container {
  width: 100%;
  height: 500px;
  background-color: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}
</style>