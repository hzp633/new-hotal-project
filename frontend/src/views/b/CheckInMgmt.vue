<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingCheckIns, checkIn, getActiveCheckIns, checkOut, getCheckInPage } from '@/api/admin/checkin'

const activeTab = ref('pending')
const loading = ref(false)

// 待入住
const pendingData = ref([])
// 在住中
const activeData = ref([])
// 入住记录
const recordPagination = reactive({ current: 1, size: 10, total: 0 })
const recordData = ref([])

// 办理入住
const checkInVisible = ref(false)
const checkInForm = reactive({ reservationId: null, roomId: '', remark: '' })

// 办理退住
const checkOutVisible = ref(false)
const checkOutForm = reactive({ reservationId: null, extraFee: 0, remark: '' })

const loadPending = async () => {
  loading.value = true
  try {
    const res = await getPendingCheckIns()
    pendingData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const loadActive = async () => {
  loading.value = true
  try {
    const res = await getActiveCheckIns()
    activeData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getCheckInPage({ current: recordPagination.current, size: recordPagination.size })
    recordData.value = res.data.records || []
    recordPagination.total = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const onTabChange = (tab) => {
  if (tab === 'pending') loadPending()
  else if (tab === 'active') loadActive()
  else if (tab === 'records') loadRecords()
}

const openCheckIn = (row) => {
  checkInForm.reservationId = row.id
  checkInForm.roomId = ''
  checkInForm.remark = ''
  checkInVisible.value = true
}

const submitCheckIn = async () => {
  if (!checkInForm.roomId) {
    ElMessage.warning('请输入房间ID')
    return
  }
  await checkIn({
    reservationId: checkInForm.reservationId,
    roomId: checkInForm.roomId,
    remark: checkInForm.remark
  })
  ElMessage.success('入住办理成功')
  checkInVisible.value = false
  loadPending()
}

const openCheckOut = (row) => {
  checkOutForm.reservationId = row.reservationId
  checkOutForm.extraFee = 0
  checkOutForm.remark = ''
  checkOutVisible.value = true
}

const submitCheckOut = async () => {
  await checkOut(checkOutForm.reservationId, {
    extraFee: checkOutForm.extraFee,
    remark: checkOutForm.remark
  })
  ElMessage.success('退住办理成功')
  checkOutVisible.value = false
  loadActive()
}

const onRecordPageChange = (page) => {
  recordPagination.current = page
  loadRecords()
}

const onRecordSizeChange = (size) => {
  recordPagination.size = size
  recordPagination.current = 1
  loadRecords()
}

const formatDate = (v) => (v ? String(v).replace('T', ' ').slice(0, 19) : '-')

onMounted(loadPending)
</script>

<template>
  <el-card v-loading="loading">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="待入住" name="pending">
        <el-table :data="pendingData" stripe border size="default" style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" min-width="180" />
          <el-table-column prop="contactName" label="联系人" min-width="100" />
          <el-table-column prop="contactPhone" label="联系电话" min-width="130" />
          <el-table-column prop="checkInDate" label="入住日期" min-width="120" />
          <el-table-column prop="checkOutDate" label="退房日期" min-width="120" />
          <el-table-column prop="roomCount" label="房间数" width="90" align="center" />
          <el-table-column label="总价" width="120" align="right">
            <template #default="{ row }">¥{{ row.totalPrice }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openCheckIn(row)">办理入住</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="在住中" name="active">
        <el-table :data="activeData" stripe border size="default" style="width: 100%">
          <el-table-column prop="reservationId" label="订单ID" min-width="120" />
          <el-table-column prop="roomId" label="房间ID" min-width="100" />
          <el-table-column label="入住时间" min-width="170">
            <template #default="{ row }">{{ formatDate(row.checkInTime) }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100" align="center" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="warning" size="small" @click="openCheckOut(row)">办理退住</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="入住记录" name="records">
        <el-table :data="recordData" stripe border size="default" style="width: 100%">
          <el-table-column prop="reservationId" label="订单ID" min-width="120" />
          <el-table-column prop="roomId" label="房间ID" min-width="100" />
          <el-table-column label="入住时间" min-width="170">
            <template #default="{ row }">{{ formatDate(row.checkInTime) }}</template>
          </el-table-column>
          <el-table-column label="退房时间" min-width="170">
            <template #default="{ row }">{{ formatDate(row.checkOutTime) }}</template>
          </el-table-column>
          <el-table-column label="额外费用" width="120" align="right">
            <template #default="{ row }">¥{{ row.extraFee || 0 }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center" />
          <el-table-column prop="operatorId" label="操作员ID" min-width="100" />
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="recordPagination.current"
            v-model:page-size="recordPagination.size"
            :total="recordPagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, prev, pager, next"
            @current-change="onRecordPageChange"
            @size-change="onRecordSizeChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="checkInVisible" title="办理入住" width="500px" center>
      <el-form label-width="100px">
        <el-form-item label="订单ID">
          <el-input :model-value="checkInForm.reservationId" disabled />
        </el-form-item>
        <el-form-item label="房间ID" required>
          <el-input v-model="checkInForm.roomId" placeholder="请输入房间ID" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkInForm.remark" type="textarea" :rows="3" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkInVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheckIn">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="checkOutVisible" title="办理退住" width="500px" center>
      <el-form label-width="100px">
        <el-form-item label="订单ID">
          <el-input :model-value="checkOutForm.reservationId" disabled />
        </el-form-item>
        <el-form-item label="额外费用">
          <el-input-number v-model="checkOutForm.extraFee" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkOutForm.remark" type="textarea" :rows="3" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkOutVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheckOut">确定</el-button>
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
