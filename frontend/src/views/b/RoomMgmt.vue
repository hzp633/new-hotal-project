<template>
  <div class="room-mgmt">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" @submit.prevent>
        <el-form-item label="房型">
          <el-select v-model="search.roomTypeId" placeholder="全部房型" clearable filterable style="width: 160px">
            <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.name" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="search.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="空闲" :value="0" />
            <el-option label="已入住" :value="1" />
            <el-option label="维护中" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层">
          <el-input v-model="search.floor" placeholder="楼层" clearable style="width: 110px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openDialog(null)">新增房间</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 房间列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="roomNo" label="房号" width="100" />
        <el-table-column prop="floor" label="楼层" width="80" align="center" />
        <el-table-column label="房型" min-width="120">
          <template #default="{ row }">{{ roomTypeName(row.roomTypeId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 2 ? 'success' : 'warning'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 2 ? '设为空闲' : '设为维护' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :current-page="pagination.current"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          @current-change="onCurrentChange"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑房间' : '新增房间'"
      width="560px"
      center
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="form.roomTypeId" placeholder="请选择房型" filterable style="width: 100%">
            <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.name" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房号" prop="roomNo">
          <el-input v-model="form.roomNo" placeholder="请输入房号，如 101" maxlength="10" />
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input-number v-model="form.floor" :min="1" :max="100" controls-position="right" style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 200px">
            <el-option label="空闲" :value="0" />
            <el-option label="已入住" :value="1" />
            <el-option label="维护中" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getRoomPage,
  createRoom,
  updateRoom,
  updateRoomStatus,
  deleteRoom
} from '@/api/admin/room'
import { getRoomTypes } from '@/api/public'

const loading = ref(false)
const tableData = ref([])
const roomTypes = ref([])
const search = reactive({ roomTypeId: '', status: '', floor: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const defaultForm = () => ({
  id: null,
  roomTypeId: '',
  roomNo: '',
  floor: 1,
  status: 0,
  remark: ''
})
const form = reactive(defaultForm())

const rules = {
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  roomNo: [{ required: true, message: '请输入房号', trigger: 'blur' }],
  floor: [{ required: true, message: '请输入楼层', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function roomTypeName(id) {
  const rt = roomTypes.value.find(i => i.id === id)
  return rt ? rt.name : (id || '-')
}

function statusText(s) {
  return ['空闲', '已入住', '维护中'][s] || '未知'
}

function statusTagType(s) {
  if (s === 0) return 'success'
  if (s === 1) return 'warning'
  return 'info'
}

async function loadRoomTypes() {
  try {
    const res = await getRoomTypes()
    roomTypes.value = res.data || []
  } catch (e) { /* ignore */ }
}

async function loadData() {
  loading.value = true
  try {
    const params = { current: pagination.current, size: pagination.size }
    if (search.roomTypeId !== '' && search.roomTypeId !== null) params.roomTypeId = search.roomTypeId
    if (search.status !== '' && search.status !== null) params.status = search.status
    if (search.floor) params.floor = search.floor
    const res = await getRoomPage(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) { /* ignore */ } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  search.roomTypeId = ''
  search.status = ''
  search.floor = ''
  pagination.current = 1
  loadData()
}

function onCurrentChange(page) {
  pagination.current = page
  loadData()
}

function onSizeChange(size) {
  pagination.size = size
  pagination.current = 1
  loadData()
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    Object.assign(form, {
      id: row.id,
      roomTypeId: row.roomTypeId,
      roomNo: row.roomNo,
      floor: row.floor,
      status: row.status,
      remark: row.remark || ''
    })
  } else {
    isEdit.value = false
    Object.assign(form, defaultForm())
  }
  dialogVisible.value = true
}

function resetForm() {
  if (formRef.value) formRef.value.clearValidate()
  Object.assign(form, defaultForm())
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        roomTypeId: form.roomTypeId,
        roomNo: form.roomNo,
        floor: form.floor,
        status: form.status,
        remark: form.remark
      }
      if (isEdit.value) {
        payload.id = form.id
        await updateRoom(payload)
        ElMessage.success('更新成功')
      } else {
        await createRoom(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (e) { /* ignore */ } finally {
      submitting.value = false
    }
  })
}

async function toggleStatus(row) {
  const newStatus = row.status === 2 ? 0 : 2
  const action = newStatus === 2 ? '设为维护中' : '设为空闲'
  try {
    await ElMessageBox.confirm(`确定${action}房间"${row.roomNo}"吗？`, '提示', { type: 'warning' })
    await updateRoomStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success('操作成功')
  } catch (e) { /* 取消或失败 */ }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除房间"${row.roomNo}"吗？此操作不可恢复。`, '删除确认', { type: 'warning' })
    await deleteRoom(row.id)
    ElMessage.success('删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    loadData()
  } catch (e) { /* 取消或失败 */ }
}

onMounted(async () => {
  await loadRoomTypes()
  loadData()
})
</script>

<style scoped>
.room-mgmt { display: flex; flex-direction: column; gap: 16px; }
.search-card, .table-card { border-radius: 6px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
