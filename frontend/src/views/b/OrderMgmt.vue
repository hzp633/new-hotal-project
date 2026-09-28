<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReservationPage, getReservationDetail, cancelReservation } from '@/api/admin/reservation'

const statusOptions = [
  { value: '', label: '全部' },
  { value: 0, label: '待支付' },
  { value: 1, label: '已支付' },
  { value: 2, label: '已入住' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已取消' },
  { value: 5, label: '已退款' }
]

const statusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'primary' },
  2: { text: '已入住', type: 'success' },
  3: { text: '已完成', type: 'info' },
  4: { text: '已取消', type: 'danger' },
  5: { text: '已退款', type: 'danger' }
}

const query = reactive({
  orderNo: '',
  phone: '',
  status: '',
  dateRange: []
})

const pagination = reactive({ current: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)

const detailVisible = ref(false)
const detail = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      orderNo: query.orderNo || undefined,
      phone: query.phone || undefined,
      status: query.status === '' ? undefined : query.status
    }
    if (query.dateRange && query.dateRange.length === 2) {
      params.startDate = query.dateRange[0]
      params.endDate = query.dateRange[1]
    }
    const res = await getReservationPage(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  pagination.current = 1
  loadData()
}

const onReset = () => {
  query.orderNo = ''
  query.phone = ''
  query.status = ''
  query.dateRange = []
  onSearch()
}

const onPageChange = (page) => {
  pagination.current = page
  loadData()
}

const onPageSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

const showDetail = async (row) => {
  const res = await getReservationDetail(row.id)
  detail.value = res.data || {}
  detailVisible.value = true
}

const onCancel = (row) => {
  ElMessageBox.confirm(`确定要取消订单「${row.orderNo}」吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await cancelReservation(row.id)
    ElMessage.success('订单已取消')
    loadData()
  }).catch(() => {})
}

const formatDate = (v) => (v ? String(v).replace('T', ' ').slice(0, 19) : '-')

onMounted(loadData)
</script>

<template>
  <el-card v-loading="loading">
    <el-form :inline="true" class="search-bar">
      <el-form-item label="订单号">
        <el-input v-model="query.orderNo" placeholder="请输入订单号" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="query.phone" placeholder="请输入联系电话" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="入住日期">
        <el-date-picker
          v-model="query.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 260px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe border size="default" style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="contactName" label="联系人" min-width="100" />
      <el-table-column prop="contactPhone" label="联系电话" min-width="130" />
      <el-table-column label="入住日期" min-width="200">
        <template #default="{ row }">
          {{ row.checkInDate }} ~ {{ row.checkOutDate }}
        </template>
      </el-table-column>
      <el-table-column prop="roomCount" label="房间数" width="90" align="center" />
      <el-table-column label="总价" width="120" align="right">
        <template #default="{ row }">¥{{ row.totalPrice }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="(statusMap[row.status] || {}).type || 'info'">
            {{ (statusMap[row.status] || {}).text || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
          <el-button
            v-if="row.status === 0 || row.status === 1"
            link
            type="danger"
            size="small"
            @click="onCancel(row)"
          >取消订单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next"
        @current-change="onPageChange"
        @size-change="onPageSizeChange"
      />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="600px" center>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="(statusMap[detail.status] || {}).type || 'info'">
            {{ (statusMap[detail.status] || {}).text || detail.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detail.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ detail.checkInDate }}</el-descriptions-item>
        <el-descriptions-item label="退房日期">{{ detail.checkOutDate }}</el-descriptions-item>
        <el-descriptions-item label="入住天数">{{ detail.days }}</el-descriptions-item>
        <el-descriptions-item label="房间数">{{ detail.roomCount }}</el-descriptions-item>
        <el-descriptions-item label="总价">¥{{ detail.totalPrice }}</el-descriptions-item>
        <el-descriptions-item label="实付">¥{{ detail.actualAmount }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="房型ID">{{ detail.roomTypeId }}</el-descriptions-item>
        <el-descriptions-item label="入住人信息" :span="2">{{ detail.guestInfo }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatDate(detail.payTime) }}</el-descriptions-item>
        <el-descriptions-item label="取消时间">{{ formatDate(detail.cancelTime) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatDate(detail.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.search-bar {
  margin-bottom: 16px;
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
