<template>
  <div class="orders-page">
    <h2 class="page-title">我的订单</h2>

    <el-tabs v-model="activeTab" @tab-change="loadOrders">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待支付" name="0" />
      <el-tab-pane label="已支付" name="1" />
      <el-tab-pane label="已入住" name="2" />
      <el-tab-pane label="已完成" name="3" />
      <el-tab-pane label="已取消" name="4" />
      <el-tab-pane label="已退款" name="5" />
    </el-tabs>

    <el-card v-loading="loading" shadow="never">
      <el-empty v-if="orders.length === 0" description="暂无订单" />

      <div v-else class="order-list">
        <el-card
          v-for="order in orders"
          :key="order.id"
          class="order-card"
          shadow="hover"
        >
          <div class="order-header">
            <div class="order-no">
              <span class="label">订单号：</span>{{ order.orderNo }}
            </div>
            <el-tag :type="statusTagType(order.status)">
              {{ statusText(order.status) }}
            </el-tag>
          </div>

          <div class="order-body">
            <el-image
              class="order-image"
              :src="firstImage(order.roomTypeImages)"
              fit="cover"
            >
              <template #error>
                <div class="image-placeholder">暂无图片</div>
              </template>
            </el-image>
            <div class="order-info">
              <h4 class="room-name">{{ order.roomTypeName }}</h4>
              <p class="info-line">
                <el-icon><Calendar /></el-icon>
                {{ order.checkInDate }} 至 {{ order.checkOutDate }}（{{ order.days }}晚）
              </p>
              <p class="info-line">
                <el-icon><HomeFilled /></el-icon>
                {{ order.roomCount }} 间
              </p>
              <p class="info-line create-time">下单时间：{{ order.createTime }}</p>
            </div>
            <div class="order-price">
              <div class="price-label">订单金额</div>
              <div class="price-value">¥{{ order.totalPrice }}</div>
              <div v-if="order.actualAmount != null && order.actualAmount !== order.totalPrice" class="actual-amount">
                实付 ¥{{ order.actualAmount }}
              </div>
            </div>
          </div>

          <div class="order-footer">
            <el-button link @click="router.push(`/orders/${order.id}`)">查看详情</el-button>
            <template v-if="order.status === 0">
              <el-button type="primary" @click="handlePay(order)">继续支付</el-button>
              <el-button type="danger" plain @click="handleCancel(order)">取消订单</el-button>
            </template>
            <template v-else-if="order.status === 1">
              <el-button type="danger" plain @click="handleCancel(order)">取消订单</el-button>
            </template>
            <template v-else-if="order.status === 3">
              <el-button type="primary" @click="openReview(order)">评价</el-button>
            </template>
          </div>
        </el-card>
      </div>
    </el-card>

    <el-dialog
      v-model="reviewDialog.visible"
      title="发表评价"
      width="520px"
    >
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="房型">
          <span>{{ reviewDialog.order?.roomTypeName }}</span>
        </el-form-item>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, HomeFilled } from '@element-plus/icons-vue'
import { getReservations, payReservation, cancelReservation } from '@/api/reservation'
import { createReview } from '@/api/review'

const router = useRouter()

const activeTab = ref('all')
const orders = ref([])
const loading = ref(false)

const reviewDialog = reactive({
  visible: false,
  order: null
})
const reviewForm = reactive({
  rating: 5,
  content: ''
})
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

async function loadOrders() {
  loading.value = true
  try {
    const params = activeTab.value === 'all' ? {} : { status: activeTab.value }
    const res = await getReservations(params)
    orders.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handlePay(order) {
  try {
    await ElMessageBox.confirm(`确认支付订单 ¥${order.totalPrice}？`, '支付确认', {
      type: 'warning',
      confirmButtonText: '确认支付',
      cancelButtonText: '取消'
    })
    await payReservation(order.id)
    ElMessage.success('支付成功')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel' && e?.code !== undefined) {
      /* handled by interceptor */
    }
  }
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm('确认取消该订单？取消后无法恢复', '取消订单', {
      type: 'warning',
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想'
    })
    await cancelReservation(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) {
    /* cancelled by user */
  }
}

function openReview(order) {
  reviewDialog.order = order
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
      reservationId: reviewDialog.order.id,
      rating: reviewForm.rating,
      content: reviewForm.content,
      images: '[]'
    })
    ElMessage.success('评价已提交')
    reviewDialog.visible = false
    loadOrders()
  } finally {
    reviewSubmitting.value = false
  }
}

onMounted(loadOrders)
</script>

<style scoped>
.orders-page { max-width: 1000px; margin: 0 auto; }
.page-title { margin-bottom: 20px; }
.order-list { display: flex; flex-direction: column; gap: 16px; }
.order-card { border: 1px solid #ebeef5; }
.order-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 12px; border-bottom: 1px dashed #ebeef5; }
.order-no .label { color: #909399; font-size: 14px; }
.order-body { display: flex; gap: 16px; padding: 16px 0; }
.order-image { width: 140px; height: 100px; border-radius: 6px; flex-shrink: 0; background: #f5f7fa; }
.image-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #909399; background: #f5f7fa; }
.order-info { flex: 1; }
.room-name { margin: 0 0 8px; font-size: 16px; }
.info-line { display: flex; align-items: center; gap: 6px; color: #606266; font-size: 14px; margin: 4px 0; }
.create-time { color: #909399; font-size: 12px; }
.order-price { text-align: right; min-width: 120px; display: flex; flex-direction: column; justify-content: center; }
.price-label { color: #909399; font-size: 12px; }
.price-value { color: #f56c6c; font-size: 22px; font-weight: bold; }
.actual-amount { color: #606266; font-size: 12px; margin-top: 4px; }
.order-footer { display: flex; justify-content: flex-end; gap: 8px; padding-top: 12px; border-top: 1px dashed #ebeef5; }
</style>
