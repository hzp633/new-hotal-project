<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReviewPage, updateReviewStatus, replyReview } from '@/api/admin/review'

const ratingOptions = [
  { value: '', label: '全部' },
  { value: 5, label: '5星' },
  { value: 4, label: '4星' },
  { value: 3, label: '3星' },
  { value: 2, label: '2星' },
  { value: 1, label: '1星' }
]

const statusOptions = [
  { value: '', label: '全部' },
  { value: 1, label: '正常' },
  { value: 0, label: '隐藏' }
]

const statusMap = {
  1: { text: '正常', type: 'success' },
  0: { text: '隐藏', type: 'info' }
}

const query = reactive({ rating: '', status: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)

const replyVisible = ref(false)
const replyForm = reactive({ id: null, reply: '' })

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      rating: query.rating === '' ? undefined : query.rating,
      status: query.status === '' ? undefined : query.status
    }
    const res = await getReviewPage(params)
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
  query.rating = ''
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

const toggleStatus = async (row) => {
  const next = row.status === 1 ? 0 : 1
  await updateReviewStatus(row.id, next)
  ElMessage.success(next === 1 ? '已显示' : '已隐藏')
  loadData()
}

const openReply = (row) => {
  replyForm.id = row.id
  replyForm.reply = row.reply || ''
  replyVisible.value = true
}

const submitReply = async () => {
  if (!replyForm.reply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  await replyReview(replyForm.id, { reply: replyForm.reply })
  ElMessage.success('回复成功')
  replyVisible.value = false
  loadData()
}

const formatDate = (v) => (v ? String(v).replace('T', ' ').slice(0, 19) : '-')

onMounted(loadData)
</script>

<template>
  <el-card v-loading="loading">
    <el-form :inline="true" class="search-bar">
      <el-form-item label="评分">
        <el-select v-model="query.rating" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="opt in ratingOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe border size="default" style="width: 100%">
      <el-table-column label="评分" width="160">
        <template #default="{ row }">
          <el-rate :model-value="row.rating" disabled />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="220" show-overflow-tooltip />
      <el-table-column label="图片" min-width="120">
        <template #default="{ row }">
          <template v-if="row.images">
            <el-image
              v-for="(img, i) in (row.images || '').split(',').filter(Boolean)"
              :key="i"
              :src="img"
              :preview-src-list="[img]"
              fit="cover"
              style="width: 40px; height: 40px; margin-right: 4px"
            />
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="reply" label="回复" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.reply || '-' }}</template>
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
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="toggleStatus(row)">
            {{ row.status === 1 ? '隐藏' : '显示' }}
          </el-button>
          <el-button link type="primary" size="small" @click="openReply(row)">回复</el-button>
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

    <el-dialog v-model="replyVisible" title="回复评价" width="500px" center>
      <el-form label-width="80px">
        <el-form-item label="回复内容">
          <el-input v-model="replyForm.reply" type="textarea" :rows="4" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">确定</el-button>
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
