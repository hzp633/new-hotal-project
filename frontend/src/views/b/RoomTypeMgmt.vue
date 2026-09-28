<template>
  <div class="room-type-mgmt">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" @submit.prevent>
        <el-form-item label="房型名称">
          <el-input v-model="search.name" placeholder="请输入房型名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="search.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openDialog(null)">新增房型</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 房型列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="name" label="房型名称" min-width="120" />
        <el-table-column label="价格" width="100" align="right">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="面积" width="90" align="right">
          <template #default="{ row }">{{ row.area }}㎡</template>
        </el-table-column>
        <el-table-column prop="bedType" label="床型" width="110" align="center" />
        <el-table-column prop="maxPeople" label="最大人数" width="100" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
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
      :title="isEdit ? '编辑房型' : '新增房型'"
      width="600px"
      center
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="房型名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入房型名称" maxlength="30" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" controls-position="right" style="width: 200px" />
          <span style="margin-left: 8px; color: #909399">元/晚</span>
        </el-form-item>
        <el-form-item label="面积" prop="area">
          <el-input-number v-model="form.area" :min="0" :precision="1" controls-position="right" style="width: 200px" />
          <span style="margin-left: 8px; color: #909399">㎡</span>
        </el-form-item>
        <el-form-item label="床型" prop="bedType">
          <el-input v-model="form.bedType" placeholder="如：大床/双床/单床" maxlength="20" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxPeople">
          <el-input-number v-model="form.maxPeople" :min="1" :max="20" controls-position="right" style="width: 200px" />
        </el-form-item>
        <el-form-item label="图片URL" prop="imagesText">
          <el-input
            v-model="form.imagesText"
            type="textarea"
            :rows="3"
            placeholder='多张图片URL以逗号分隔，或输入JSON数组如 ["url1","url2"]'
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入房型描述" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="上架"
            inactive-text="下架"
          />
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
  getRoomTypePage,
  createRoomType,
  updateRoomType,
  updateRoomTypeStatus,
  deleteRoomType
} from '@/api/admin/roomType'

const loading = ref(false)
const tableData = ref([])
const search = reactive({ name: '', status: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const defaultForm = () => ({
  id: null,
  name: '',
  price: 0,
  area: 0,
  bedType: '',
  maxPeople: 1,
  imagesText: '',
  description: '',
  status: 1
})
const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入房型名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  area: [{ required: true, message: '请输入面积', trigger: 'blur' }],
  bedType: [{ required: true, message: '请输入床型', trigger: 'blur' }],
  maxPeople: [{ required: true, message: '请输入最大人数', trigger: 'blur' }]
}

function parseImages(text) {
  if (!text) return []
  const trimmed = String(text).trim()
  if (trimmed.startsWith('[')) {
    try {
      const arr = JSON.parse(trimmed)
      return Array.isArray(arr) ? arr.filter(Boolean) : []
    } catch { return [] }
  }
  return trimmed.split(/[,，]/).map(s => s.trim()).filter(Boolean)
}

async function loadData() {
  loading.value = true
  try {
    const params = { current: pagination.current, size: pagination.size }
    if (search.name) params.name = search.name
    if (search.status !== '' && search.status !== null) params.status = search.status
    const res = await getRoomTypePage(params)
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
  search.name = ''
  search.status = ''
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
      name: row.name,
      price: row.price,
      area: row.area,
      bedType: row.bedType,
      maxPeople: row.maxPeople,
      imagesText: Array.isArray(row.images) && row.images.length ? JSON.stringify(row.images) : '',
      description: row.description || '',
      status: row.status
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
        name: form.name,
        price: form.price,
        area: form.area,
        bedType: form.bedType,
        maxPeople: form.maxPeople,
        images: parseImages(form.imagesText),
        description: form.description,
        status: form.status
      }
      if (isEdit.value) {
        payload.id = form.id
        await updateRoomType(payload)
        ElMessage.success('更新成功')
      } else {
        await createRoomType(payload)
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
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定${action}该房型吗？`, '提示', { type: 'warning' })
    await updateRoomTypeStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(`${action}成功`)
  } catch (e) { /* 取消或失败 */ }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除房型"${row.name}"吗？此操作不可恢复。`, '删除确认', { type: 'warning' })
    await deleteRoomType(row.id)
    ElMessage.success('删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    loadData()
  } catch (e) { /* 取消或失败 */ }
}

onMounted(loadData)
</script>

<style scoped>
.room-type-mgmt { display: flex; flex-direction: column; gap: 16px; }
.search-card, .table-card { border-radius: 6px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
