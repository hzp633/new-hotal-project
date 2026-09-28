<template>
  <div class="user-mgmt">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" @submit.prevent>
        <el-form-item label="手机号">
          <el-input v-model="search.phone" placeholder="请输入手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="search.nickname" placeholder="请输入昵称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="search.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar" v-if="row.avatar" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="性别" width="80" align="center">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="160" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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

    <!-- 用户详情抽屉 -->
    <el-drawer v-model="detailVisible" title="用户详情" size="400px">
      <div v-if="detail" class="detail-content">
        <div class="detail-avatar">
          <el-avatar :size="80" :src="detail.avatar" v-if="detail.avatar" />
          <el-avatar :size="80" v-else>{{ detail.nickname?.charAt(0) }}</el-avatar>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detail.nickname }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ genderText(detail.gender) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detail.status === 1 ? 'success' : 'danger'">
              {{ detail.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ detail.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPage, getUserDetail, updateUserStatus } from '@/api/admin/user'

const loading = ref(false)
const tableData = ref([])
const search = reactive({ phone: '', nickname: '', status: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const detailVisible = ref(false)
const detail = ref(null)

function genderText(g) {
  if (g === 1) return '男'
  if (g === 2) return '女'
  return '未知'
}

async function loadData() {
  loading.value = true
  try {
    const params = { current: pagination.current, size: pagination.size }
    if (search.phone) params.phone = search.phone
    if (search.nickname) params.nickname = search.nickname
    if (search.status !== '' && search.status !== null) params.status = search.status
    const res = await getUserPage(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  search.phone = ''
  search.nickname = ''
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

async function openDetail(row) {
  try {
    const res = await getUserDetail(row.id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) { /* ignore */ }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${action}该用户吗？`, '提示', { type: 'warning' })
    await updateUserStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(`${action}成功`)
  } catch (e) { /* 取消或失败 */ }
}

onMounted(loadData)
</script>

<style scoped>
.user-mgmt { display: flex; flex-direction: column; gap: 16px; }
.search-card, .table-card { border-radius: 6px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
.detail-avatar { display: flex; justify-content: center; margin-bottom: 16px; }
</style>
