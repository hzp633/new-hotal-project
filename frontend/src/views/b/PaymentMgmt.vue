<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRefundPage, processRefund } from '@/api/admin/payment'

const statusMap = {
  1: { text: '待退款', type: 'warning' },
  3: { text: '已退款', type: 'success' }
}

const pagination = reactive({ current: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)

const refundVisible = ref(false)
const refundForm = reactive({ id: null, reason: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRefundPage({ current: pagination.current, size: pagination.size })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } finally {
    loading.value = false
  }
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

const openRefund = (row) => {
  refundForm.id = row.id
  refundForm.reason = ''
  refundVisible.value = true
}

const submitRefund = async () => {
  if (!refundForm.reason.trim()) {
    ElMessage.warning('请输入退款原因')
    return
  }
  await processRefund(refundForm.id, { reason: refundForm.reason })
  ElMessage.success('退款已处理')
  refundVisible.value = false
  loadData()
}

const formatDate = (v) => (v ? String(v).replace('T', ' ').slice(0, 19) : '-')

onMounted(loadData)
</script>

<template>
  <el-card v-loading="loading">
    <el-table :data="tableData" stripe border size="default" style="width: 100%">
      <el-table-column prop="payNo" label="支付单号" min-width="180" />
      <el-table-column prop="reservationId" label="订单ID" min-width="100" />
      <el-table-column prop="userId" label="用户ID" min-width="100" />
      <el-table-column label="金额" width="120" align="right">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column prop="payMethod" label="支付方式" width="110" align="center" />
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="(statusMap[row.status] || {}).type || 'info'">
            {{ (statusMap[row.status] || {}).text || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" min-width="170">
        <template #default="{ row }">{{ formatDate(row.payTime) }}</template>
      </el-table-column>
      <el-table-column label="退款金额" width="120" align="right">
        <template #default="{ row }">¥{{ row.refundAmount || 0 }}</template>
      </el-table-column>
      <el-table-column prop="refundReason" label="退款原因" min-width="180" show-overflow-tooltip />
      <el-table-column label="退款时间" min-width="170">
        <template #default="{ row }">{{ formatDate(row.refundTime) }}</template>
      </el-table-column>
      <el-table-column prop="refundOperator" label="退款操作员" min-width="110" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 1"
            link
            type="warning"
            size="small"
            @click="openRefund(row)"
          >确认退款</el-button>
          <span v-else>-</span>
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

    <el-dialog v-model="refundVisible" title="确认退款" width="500px" center>
      <el-form label-width="100px">
        <el-form-item label="支付单号">
          <el-input :model-value="refundForm.id" disabled />
        </el-form-item>
        <el-form-item label="退款原因" required>
          <el-input v-model="refundForm.reason" type="textarea" :rows="3" placeholder="请输入退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
