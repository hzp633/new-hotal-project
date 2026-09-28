<template>
  <div class="guests-page">
    <div class="page-header">
      <h2 class="page-title">常用入住人</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新增入住人
      </el-button>
    </div>

    <el-card v-loading="loading" shadow="never">
      <el-empty v-if="guests.length === 0" description="暂无入住人">
        <el-button type="primary" @click="openDialog()">立即添加</el-button>
      </el-empty>

      <el-table v-else :data="guests" stripe>
        <el-table-column label="姓名" prop="name" min-width="100" />
        <el-table-column label="性别" min-width="80">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column label="手机号" prop="phone" min-width="130" />
        <el-table-column label="身份证号" prop="idCard" min-width="180">
          <template #default="{ row }">{{ maskIdCard(row.idCard) }}</template>
        </el-table-column>
        <el-table-column label="默认" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" min-width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.isDefault"
              type="primary"
              link
              @click="handleSetDefault(row)"
            >
              设为默认
            </el-button>
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.isEdit ? '编辑入住人' : '新增入住人'"
      width="480px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
            <el-option label="未知" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getGuests, addGuest, updateGuest, deleteGuest, setDefaultGuest } from '@/api/guest'

const guests = ref([])
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()

const dialog = reactive({
  visible: false,
  isEdit: false
})

const form = reactive({
  id: null,
  name: '',
  gender: 1,
  phone: '',
  idCard: '',
  isDefault: false
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{17}[\dXx]$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ]
}

function genderText(g) {
  return { 0: '未知', 1: '男', 2: '女' }[g] || '未知'
}

function maskIdCard(idCard) {
  if (!idCard || idCard.length < 8) return idCard || ''
  return idCard.slice(0, 4) + '********' + idCard.slice(-4)
}

function resetForm() {
  form.id = null
  form.name = ''
  form.gender = 1
  form.phone = ''
  form.idCard = ''
  form.isDefault = false
}

function openDialog(row) {
  resetForm()
  if (row) {
    dialog.isEdit = true
    Object.assign(form, {
      id: row.id,
      name: row.name,
      gender: row.gender,
      phone: row.phone,
      idCard: row.idCard,
      isDefault: !!row.isDefault
    })
  } else {
    dialog.isEdit = false
  }
  dialog.visible = true
}

async function loadGuests() {
  loading.value = true
  try {
    const res = await getGuests()
    guests.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        name: form.name,
        gender: form.gender,
        phone: form.phone,
        idCard: form.idCard,
        isDefault: form.isDefault
      }
      if (dialog.isEdit) {
        await updateGuest({ id: form.id, ...payload })
        ElMessage.success('修改成功')
      } else {
        await addGuest(payload)
        ElMessage.success('添加成功')
      }
      dialog.visible = false
      loadGuests()
    } finally {
      submitting.value = false
    }
  })
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除入住人「${row.name}」？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await deleteGuest(row.id)
    ElMessage.success('删除成功')
    loadGuests()
  } catch (e) {
    /* cancelled by user */
  }
}

async function handleSetDefault(row) {
  try {
    await setDefaultGuest(row.id)
    ElMessage.success('已设为默认')
    loadGuests()
  } catch {
    /* handled by interceptor */
  }
}

onMounted(loadGuests)
</script>

<style scoped>
.guests-page { max-width: 1000px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; }
</style>
