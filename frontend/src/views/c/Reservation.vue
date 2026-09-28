<template>
  <div class="reservation-page">
    <h2 class="page-title">在线预订</h2>

    <el-card v-loading="loading">
      <template v-if="roomType">
        <div class="room-summary">
          <el-image
            class="room-image"
            :src="firstImage(roomType.images)"
            fit="cover"
          >
            <template #error>
              <div class="image-placeholder">暂无图片</div>
            </template>
          </el-image>
          <div class="room-info">
            <h3>{{ roomType.name }}</h3>
            <p class="price">¥{{ roomType.price }} / 晚</p>
            <p class="meta">
              <span v-if="roomType.bedType">床型：{{ roomType.bedType }}</span>
              <span v-if="roomType.area">面积：{{ roomType.area }}㎡</span>
            </p>
          </div>
        </div>
      </template>

      <el-divider />

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="入住日期" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="入住日期"
            end-placeholder="离店日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
            style="width: 320px"
          />
        </el-form-item>

        <el-form-item label="房间数量" prop="roomCount">
          <el-input-number v-model="form.roomCount" :min="1" :max="10" />
        </el-form-item>

        <el-form-item label="联系人姓名" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名" style="width: 240px" />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" style="width: 240px" />
        </el-form-item>

        <el-form-item label="入住人">
          <div class="guest-select">
            <div v-if="guests.length === 0" class="empty-tip">
              暂无常用入住人，<el-button type="primary" link @click="router.push('/guests')">去添加</el-button>
            </div>
            <el-checkbox-group v-else v-model="selectedGuestIds">
              <el-checkbox
                v-for="g in guests"
                :key="g.id"
                :label="g.id"
              >
                {{ g.name }}（{{ genderText(g.gender) }}，{{ maskIdCard(g.idCard) }}，{{ g.phone }}）
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="如有特殊需求请填写"
            style="width: 480px"
          />
        </el-form-item>
      </el-form>

      <el-divider />

      <div class="price-summary">
        <div class="price-line">
          <span>房费单价</span>
          <span>¥{{ roomType ? roomType.price : 0 }} × {{ days }} 晚 × {{ form.roomCount }} 间</span>
        </div>
        <div class="price-total">
          <span>预计总价</span>
          <span class="total-amount">¥{{ totalPrice }}</span>
        </div>
      </div>

      <div class="actions">
        <el-button @click="router.back()">返回</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交订单</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRoomTypeDetail } from '@/api/public'
import { getGuests } from '@/api/guest'
import { createReservation } from '@/api/reservation'

const router = useRouter()
const route = useRoute()

const roomType = ref(null)
const guests = ref([])
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const selectedGuestIds = ref([])

const form = ref({
  dateRange: [],
  roomCount: 1,
  contactName: '',
  contactPhone: '',
  remark: ''
})

const rules = {
  dateRange: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  roomCount: [{ required: true, message: '请选择房间数量', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const days = computed(() => {
  if (!form.value.dateRange || form.value.dateRange.length !== 2) return 0
  const start = new Date(form.value.dateRange[0])
  const end = new Date(form.value.dateRange[1])
  const diff = Math.round((end - start) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 0
})

const totalPrice = computed(() => {
  if (!roomType.value || days.value <= 0) return 0
  return (roomType.value.price * days.value * form.value.roomCount).toFixed(2)
})

function disabledDate(date) {
  return date.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

function firstImage(images) {
  if (!images) return ''
  if (Array.isArray(images)) return images[0] || ''
  try {
    const arr = JSON.parse(images)
    return Array.isArray(arr) ? arr[0] || '' : arr
  } catch {
    return images
  }
}

function genderText(g) {
  return { 0: '未知', 1: '男', 2: '女' }[g] || '未知'
}

function maskIdCard(idCard) {
  if (!idCard || idCard.length < 8) return idCard || ''
  return idCard.slice(0, 4) + '********' + idCard.slice(-4)
}

async function loadRoomType() {
  const id = route.query.roomTypeId
  if (!id) {
    ElMessage.error('缺少房型参数')
    router.back()
    return
  }
  loading.value = true
  try {
    const res = await getRoomTypeDetail(id)
    roomType.value = res.data
  } finally {
    loading.value = false
  }
}

async function loadGuests() {
  try {
    const res = await getGuests()
    guests.value = res.data || []
    const def = guests.value.find(g => g.isDefault)
    if (def) {
      selectedGuestIds.value = [def.id]
      form.value.contactName = def.name
      form.value.contactPhone = def.phone
    }
  } catch {
    /* ignore */
  }
}

async function handleSubmit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (days.value <= 0) {
      ElMessage.warning('请选择有效的入住日期')
      return
    }
    const selectedGuests = guests.value
      .filter(g => selectedGuestIds.value.includes(g.id))
      .map(g => ({
        name: g.name,
        phone: g.phone,
        idCard: g.idCard,
        gender: g.gender
      }))

    submitting.value = true
    try {
      const res = await createReservation({
        roomTypeId: Number(route.query.roomTypeId),
        checkInDate: form.value.dateRange[0],
        checkOutDate: form.value.dateRange[1],
        roomCount: form.value.roomCount,
        contactName: form.value.contactName,
        contactPhone: form.value.contactPhone,
        guestInfo: JSON.stringify(selectedGuests),
        remark: form.value.remark
      })
      ElMessage.success(`订单提交成功，订单号：${res.data.orderNo}`)
      router.push(`/orders/${res.data.id}`)
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadRoomType()
  loadGuests()
})
</script>

<style scoped>
.reservation-page { max-width: 900px; margin: 0 auto; }
.page-title { margin-bottom: 20px; }
.room-summary { display: flex; gap: 20px; }
.room-image { width: 200px; height: 140px; border-radius: 8px; background: #f5f7fa; }
.image-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #909399; background: #f5f7fa; }
.room-info h3 { margin: 0 0 8px; }
.room-info .price { color: #f56c6c; font-size: 18px; font-weight: bold; margin: 4px 0; }
.room-info .meta { color: #909399; font-size: 14px; display: flex; gap: 16px; }
.guest-select { width: 100%; }
.empty-tip { color: #909399; }
.price-summary { background: #fafafa; padding: 16px 20px; border-radius: 8px; margin-bottom: 16px; }
.price-line { display: flex; justify-content: space-between; color: #606266; margin-bottom: 8px; }
.price-total { display: flex; justify-content: space-between; font-size: 16px; font-weight: bold; border-top: 1px dashed #dcdfe6; padding-top: 8px; }
.total-amount { color: #f56c6c; font-size: 20px; }
.actions { display: flex; justify-content: flex-end; gap: 12px; }
</style>
