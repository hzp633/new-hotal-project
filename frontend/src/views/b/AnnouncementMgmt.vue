<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAnnouncementPage,
  createAnnouncement,
  updateAnnouncement,
  updateAnnouncementStatus,
  deleteAnnouncement
} from '@/api/admin/announcement'

const statusOptions = [
  { value: '', label: '全部' },
  { value: 1, label: '发布' },
  { value: 0, label: '下架' }
]

const statusMap = {
  1: { text: '发布', type: 'success' },
  0: { text: '下架', type: 'info' }
}

const query = reactive({ title: '', status: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({
  id: null,
  title: '',
  content: '',
  isTop: 0,
  status: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      title: query.title || undefined,
      status: query.status === '' ? undefined : query.status
    }
    const res = await getAnnouncementPage(params)
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
  query.title = ''
  query.status = ''
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

const openCreate = () => {
  dialogTitle.value = '新增公告'
  form.id = null
  form.title = ''
  form.content = ''
  form.isTop = 0
  form.status = 1
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogTitle.value = '编辑公告'
  form.id = row.id
  form.title = row.title
  form.content = row.content
  form.isTop = row.isTop
  form.status = row.status
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  const payload = {
    title: form.title,
    content: form.content,
    isTop: form.isTop,
    status: form.status
  }
  if (form.id) {
    payload.id = form.id
    await updateAnnouncement(payload)
    ElMessage.success('更新成功')
  } else {
    await createAnnouncement(payload)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  loadData()
}

const toggleStatus = async (row) => {
  const next = row.status === 1 ? 0 : 1
  await updateAnnouncementStatus(row.id, next)
  ElMessage.success(next === 1 ? '已发布' : '已下架')
  loadData()
}

const onDelete = (row) => {
  ElMessageBox.confirm(`确定要删除公告「${row.title}」吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await deleteAnnouncement(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const truncate = (str, len = 30) => {
  if (!str) return '-'
  return str.length > len ? str.slice(0, len) + '...' : str
}

const formatDate = (v) => (v ? String(v).replace('T', ' ').slice(0, 19) : '-')

onMounted(loadData)
</script>

<template>
  <el-card v-loading="loading">
    <el-form :inline="true" class="search-bar">
      <el-form-item label="标题">
        <el-input v-model="query.title" placeholder="请输入标题" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="onReset">重置</el-button>
        <el-button type="success" @click="openCreate">新增公告</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe border size="default" style="width: 100%">
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column label="内容" min-width="220">
        <template #default="{ row }">{{ truncate(row.content) }}</template>
      </el-table-column>
      <el-table-column label="置顶" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isTop === 1" type="danger">置顶</el-tag>
          <el-tag v-else type="info">普通</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="(statusMap[row.status] || {}).type || 'info'">
            {{ (statusMap[row.status] || {}).text || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '发布' }}
          </el-button>
          <el-button link type="danger" size="small" @click="onDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" center>
      <el-form label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 140px">
            <el-option label="发布" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
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
