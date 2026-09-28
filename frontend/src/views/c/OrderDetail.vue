<template>
  <div class="order-detail-page">
    <div class="page-header">
      <el-button @click="router.push('/orders')">
        <el-icon><ArrowLeft /></el-icon> 返回订单列表
      </el-button>
      <h2 class="page-title">订单详情</h2>
    </div>

    <div v-loading="loading">
      <template v-if="order">
        <el-card class="section-card">
          <div class="header-row">
            <div>
              <span class="label">订单号：</span>{{ order.orderNo }}
            </div>
            <el-tag :type="statusTagType(order.status)" size="large">
              {{ statusText(order.status) }}
            </el-tag>
          </div>
        </el-card>

        <el-card class="section-card">
          <template #header>房型信息</template>
          <div class="room-summary">
            <el-image
              class="room-image"
              :src="firstImage(order.roomTypeImages)"
              fit="cover"
            >
              <template #error>
                <div class="image-placeholder">暂无图片</div>
              </template>
            </el-image>
            <div class="room-info">
              <h3>{{ order.roomTypeName }}</h3>
              <p class="price">¥{{ order.roomTypePrice }} / 晚</p>
              <el-button
                v-if="order.roomTypeId"
                type="primary"
                link
                @click="router.push(`/rooms/${order.roomTypeId}`)"
              >
                查看房型
              </el-button>
            </div>
          </div>
        </el-card>

        <el-card class="section-card">
          <template #header>预订信息</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="入住日期">{{ order.checkInDate }}</el-descriptions-item>
            <el-descriptions-item label="离店日期">{{ order.checkOutDate }}</el-descriptions-item>
            <el-descriptions-item label="入住天数">{{ order.days }} 晚</el-descriptions-item>
            <el-descriptions-item label="房间数量">{{ order.roomCount }} 间</el-descriptions-item>
            <el-descriptions-item label="订单总额">
              <span class="price-text">¥{{ order.totalPrice }}</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="order.actualAmount != null" label="实付金额">
              <span class="price-text">¥{{ order.actualAmount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="联系人">{{ order.contactName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ order.contactPhone }}</el-descriptions-item>
            <el-descriptions-item label="入住人" :span="2">
              <span v-if="parsedGuests.length === 0">无</span>
              <el-tag
                v-for="(g, i) in parsedGuests"
                :key="i"
                class="guest-tag"
                type="info"
              >
                {{ g.name }}（{{ genderText(g.gender) }}）{{ g.phone ? ' / ' + g.phone : '' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="order.remark" label="备注" :span="2">
              {{ order.remark }}
            </el-descriptions-item>
            <el-descriptions-item v-if="order.payTime" label="支付时间">{{ order.payTime }}</el-descriptions-item>
            <el-descriptions-item v-if="order.cancelTime" label="取消时间">{{ order.cancelTime }}</el-descriptions-item>
            <el-descriptions-item label="下单时间" :span="2">{{ order.createTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="section-card actions-card">
          <div class="action-buttons">
            <el-button @click="router.push('/orders')">返回订单列表</el-button>
            <template v-if="order.status === 0">
              <el-button type="primary" @click="handlePay">继续支付</el-button>
              <el-button type="danger" plain @click="handleCancel">取消订单</el-button>
            </template>
            <template v-else-if="order.status === 1">
              <el-button type="danger" plain @click="handleCancel">取消订单</el-button>
            </template>
            <template v-else-if="order.status === 3">
              <el-button type="primary" @click="openReview">立即评价</el-button>
            </template>
          </div>
        </el-card>
      </template>
    </div>

    <el-dialog v-model="reviewDialog.visible" title="发表评价" width="520px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" :max="5" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请分享您的入住体验"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="reviewSubmitting" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getReservationDetail, payReservation, cancelReservation } from '@/api/reservation'
import { createReview } from '@/api/review'

const router = useRouter()
const route = useRoute()

const order = ref(null)
const loading = ref(false)

const reviewDialog = reactive({ visible: false })
const reviewForm = reactive({ rating: 5, content: '' })
const reviewSubmitting = ref(false)

const STATUS_MAP = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'primary' },
  2: { text: '已入住', type: 'success' },
  3: { text: '已完成', type: 'info' },
  4: { text: '已取消', type: 'danger' },
  5: { text: '已退款', type: 'danger' }
}

function statusText(s) {
  return STATUS_MAP[s]?.text || '未知'
}
function statusTagType(s) {
  return STATUS_MAP[s]?.type || 'info'
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

const parsedGuests = computed(() => {
  if (!order.value?.guestInfo) return []
  try {
    const arr = typeof order.value.guestInfo === 'string'
      ? JSON.parse(order.value.guestInfo)
      : order.value.guestInfo
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
})

async function loadDetail() {
  loading.value = true
  try {
    const res = await getReservationDetail(route.params.id)
    order.value = res.data
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  try {
    await ElMessageBox.confirm(`确认支付订单 ¥${order.value.totalPrice}？`, '支付确认', {
      type: 'warning',
      confirmButtonText: '确认支付',
      cancelButtonText: '取消'
    })
    await payReservation(order.value.id)
    ElMessage.success('支付成功')
    loadDetail()
  } catch (e) {
    /* cancelled by user */
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确认取消该订单？取消后无法恢复', '取消订单', {
      type: 'warning',
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想'
    })
    await cancelReservation(order.value.id)
    ElMessage.success('订单已取消')
    loadDetail()
  } catch (e) {
    /* cancelled by user */
  }
}

function openReview() {
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewDialog.visible = true
}

async function submitReview() {
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  reviewSubmitting.value = true
  try {
    await createReview({
      reservationId: order.value.id,
      rating: reviewForm.rating,
      content: reviewForm.content,
      images: '[]'
    })
    ElMessage.success('评价已提交')
    reviewDialog.visible = false
  } finally {
    reviewSubmitting.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.order-detail-page { max-width: 900px; margin: 0 auto; }
.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.page-title { margin: 0; }
.section-card { margin-bottom: 16px; }
.header-row { display: flex; justify-content: space-between; align-items: center; }
.header-row .label { color: #909399; }
.room-summary { display: flex; gap: 20px; }
.room-image { width: 200px; height: 140px; border-radius: 8px; background: #f5f7fa; }
.image-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #909399; background: #f5f7fa; }
.room-info h3 { margin: 0 0 8px; }
.room-info .price { color: #f56c6c; font-size: 18px; font-weight: bold; margin: 4px 0 12px; }
.price-text { color: #f56c6c; font-weight: bold; }
.guest-tag { margin-right: 8px; }
.actions-card { text-align: right; }
.action-buttons { display: flex; justify-content: flex-end; gap: 8px; }
</style>
