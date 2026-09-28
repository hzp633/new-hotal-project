<template>
  <div class="hotel-mgmt">
    <el-card shadow="never" class="hotel-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>酒店信息</span>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 700px">
        <el-form-item label="酒店名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入酒店名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="星级" prop="star">
          <el-rate v-model="form.star" :max="5" show-text :texts="['一星','二星','三星','四星','五星']" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入酒店地址" maxlength="100" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="酒店简介" prop="intro">
          <el-input v-model="form.intro" type="textarea" :rows="4" placeholder="请输入酒店简介" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <el-input v-model="form.coverImage" placeholder="请输入封面图URL" />
          <el-image v-if="form.coverImage" :src="form.coverImage" fit="cover" class="cover-preview" :preview-src-list="[form.coverImage]" />
        </el-form-item>
        <el-form-item label="图片列表" prop="imagesText">
          <el-input
            v-model="form.imagesText"
            type="textarea"
            :rows="3"
            placeholder='多张图片URL以逗号分隔，或输入JSON数组如 ["url1","url2"]'
          />
        </el-form-item>
        <el-form-item label="设施服务" prop="facilitiesText">
          <div class="facilities-wrap">
            <div>
              <el-input
                v-model="facilityInput"
                placeholder="输入设施后回车添加，如：WiFi、停车场"
                style="width: 320px"
                @keyup.enter="addFacility"
              />
              <el-button type="primary" plain @click="addFacility" style="margin-left: 8px">添加</el-button>
            </div>
            <div class="facility-tags">
              <el-tag
                v-for="(f, idx) in form.facilitiesList"
                :key="idx"
                closable
                @close="removeFacility(idx)"
                style="margin: 4px"
              >{{ f }}</el-tag>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHotel } from '@/api/public'
import { updateHotel } from '@/api/admin/hotel'

const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const facilityInput = ref('')

const form = reactive({
  name: '',
  star: 0,
  address: '',
  phone: '',
  intro: '',
  coverImage: '',
  imagesText: '',
  facilitiesList: []
})

const rules = {
  name: [{ required: true, message: '请输入酒店名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入酒店地址', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

function parseImages(text) {
  if (!text) return []
  const trimmed = text.trim()
  // 尝试解析JSON数组
  if (trimmed.startsWith('[')) {
    try {
      const arr = JSON.parse(trimmed)
      return Array.isArray(arr) ? arr.filter(Boolean) : []
    } catch {
      return []
    }
  }
  // 逗号分隔
  return trimmed.split(/[,，]/).map(s => s.trim()).filter(Boolean)
}

function parseFacilities(text) {
  if (!text) return []
  if (Array.isArray(text)) return text
  const trimmed = String(text).trim()
  if (trimmed.startsWith('[')) {
    try {
      const arr = JSON.parse(trimmed)
      return Array.isArray(arr) ? arr : []
    } catch {
      return trimmed.split(/[,，]/).map(s => s.trim()).filter(Boolean)
    }
  }
  return trimmed.split(/[,，]/).map(s => s.trim()).filter(Boolean)
}

function addFacility() {
  const v = facilityInput.value.trim()
  if (!v) return
  if (form.facilitiesList.includes(v)) {
    ElMessage.warning('该设施已存在')
    return
  }
  form.facilitiesList.push(v)
  facilityInput.value = ''
}

function removeFacility(idx) {
  form.facilitiesList.splice(idx, 1)
}

async function loadData() {
  loading.value = true
  try {
    const res = await getHotel()
    const d = res.data || {}
    form.name = d.name || ''
    form.star = d.star || 0
    form.address = d.address || ''
    form.phone = d.phone || ''
    form.intro = d.intro || ''
    form.coverImage = d.coverImage || ''
    const imgs = Array.isArray(d.images) ? d.images : parseImages(d.images)
    form.imagesText = imgs.length ? JSON.stringify(imgs) : ''
    form.facilitiesList = parseFacilities(d.facilities)
  } catch (e) {
    ElMessage.error('加载酒店信息失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        name: form.name,
        star: form.star,
        address: form.address,
        phone: form.phone,
        intro: form.intro,
        coverImage: form.coverImage,
        images: parseImages(form.imagesText),
        facilities: [...form.facilitiesList]
      }
      await updateHotel(payload)
      ElMessage.success('保存成功')
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      saving.value = false
    }
  })
}

onMounted(loadData)
</script>

<style scoped>
.hotel-mgmt { display: flex; flex-direction: column; gap: 16px; }
.hotel-card { border-radius: 6px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.cover-preview { width: 200px; height: 120px; margin-top: 8px; border-radius: 4px; border: 1px solid #e4e7ed; }
.facilities-wrap { display: flex; flex-direction: column; gap: 8px; width: 100%; }
.facility-tags { min-height: 32px; }
</style>
