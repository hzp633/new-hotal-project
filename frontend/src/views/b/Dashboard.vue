<template>
  <div class="dashboard">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">今日订单</div>
          <div class="stat-value">{{ summary.todayOrders }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">今日入住</div>
          <div class="stat-value">{{ summary.todayCheckIns }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">当前在住</div>
          <div class="stat-value">{{ summary.activeCheckIns }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">本月营收</div>
          <div class="stat-value revenue">¥{{ formatNumber(summary.monthRevenue) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 入住率进度条 -->
    <el-card shadow="never" class="occupancy-card">
      <div class="occupancy-header">
        <span class="occupancy-title">入住率</span>
        <span class="occupancy-value">{{ summary.occupancyRate }}%</span>
      </div>
      <el-progress :percentage="Number(summary.occupancyRate) || 0" :stroke-width="18" :color="occupancyColor" />
    </el-card>

    <!-- 订单 & 营收趋势 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never" header="近7天订单趋势">
          <div ref="orderChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="近7天营收趋势">
          <div ref="revenueChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 房型预订占比 -->
    <el-card shadow="never" header="房型预订占比" class="pie-card">
      <div ref="pieChartRef" class="chart-container pie-container"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getDashboardSummary } from '@/api/admin/dashboard'

const summary = reactive({
  todayOrders: 0,
  todayCheckIns: 0,
  activeCheckIns: 0,
  monthRevenue: 0,
  occupancyRate: 0,
  orderTrend: [],
  revenueTrend: [],
  typeProportion: []
})

const orderChartRef = ref(null)
const revenueChartRef = ref(null)
const pieChartRef = ref(null)

let orderChart = null
let revenueChart = null
let pieChart = null

const occupancyColor = [
  { color: '#f56c6c', percentage: 30 },
  { color: '#e6a23c', percentage: 60 },
  { color: '#67c23a', percentage: 100 }
]

function formatNumber(num) {
  const n = Number(num) || 0
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function loadData() {
  try {
    const res = await getDashboardSummary()
    const d = res.data
    summary.todayOrders = d.todayOrders || 0
    summary.todayCheckIns = d.todayCheckIns || 0
    summary.activeCheckIns = d.activeCheckIns || 0
    summary.monthRevenue = d.monthRevenue || 0
    summary.occupancyRate = d.occupancyRate || 0
    summary.orderTrend = d.orderTrend || []
    summary.revenueTrend = d.revenueTrend || []
    summary.typeProportion = d.typeProportion || []
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error('加载仪表盘数据失败')
  }
}

function renderCharts() {
  // 订单趋势折线图
  if (orderChartRef.value) {
    orderChart = echarts.init(orderChartRef.value)
    orderChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 30, bottom: 30 },
      xAxis: { type: 'category', data: summary.orderTrend.map(i => i.date), axisLabel: { fontSize: 11 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        name: '订单数',
        type: 'line',
        smooth: true,
        data: summary.orderTrend.map(i => i.count),
        itemStyle: { color: '#409eff' },
        areaStyle: { opacity: 0.15 }
      }]
    })
  }
  // 营收趋势柱状图
  if (revenueChartRef.value) {
    revenueChart = echarts.init(revenueChartRef.value)
    revenueChart.setOption({
      tooltip: { trigger: 'axis', formatter: (p) => `${p[0].name}<br/>营收: ¥${p[0].value}` },
      grid: { left: 50, right: 20, top: 30, bottom: 30 },
      xAxis: { type: 'category', data: summary.revenueTrend.map(i => i.date), axisLabel: { fontSize: 11 } },
      yAxis: { type: 'value' },
      series: [{
        name: '营收',
        type: 'bar',
        data: summary.revenueTrend.map(i => i.amount),
        itemStyle: { color: '#67c23a' },
        barMaxWidth: 32
      }]
    })
  }
  // 房型占比饼图
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, type: 'scroll' },
      series: [{
        name: '预订占比',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        data: summary.typeProportion.map(i => ({ name: i.name, value: i.count })),
        label: { formatter: '{b}: {d}%' }
      }]
    })
  }
}

function handleResize() {
  orderChart?.resize()
  revenueChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  orderChart?.dispose()
  revenueChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 16px; }
.stat-row .stat-card { text-align: center; }
.stat-card { border-radius: 6px; }
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 32px; font-weight: bold; color: #303133; }
.stat-value.revenue { color: #67c23a; }
.occupancy-card { border-radius: 6px; }
.occupancy-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.occupancy-title { font-size: 14px; color: #606266; }
.occupancy-value { font-size: 18px; font-weight: bold; color: #409eff; }
.chart-row { margin-top: 0; }
.chart-container { height: 300px; }
.pie-container { height: 340px; }
</style>
