<template>
  <div class="room-detail-page" v-loading="loading">
    <template v-if="room">
      <!-- 顶部图片轮播 + 信息 -->
      <el-card class="main-card" shadow="never">
        <el-row :gutter="24">
          <el-col :xs="24" :md="14">
            <el-carousel
              v-if="imageList.length"
              :autoplay="false"
              height="380px"
              indicator-position="outside"
              class="img-carousel"
            >
              <el-carousel-item v-for="(img, idx) in imageList" :key="idx">
                <el-image :src="img" fit="cover" class="carousel-img" />
              </el-carousel-item>
            </el-carousel>
            <div v-else class="img-fallback large">暂无图片</div>
          </el-col>
          <el-col :xs="24" :md="10">
            <div class="info-panel">
              <div class="info-title-row">
                <h2 class="room-name">{{ room.name }}</h2>
                <el-button
                  :type="isFavorite ? 'danger' : 'default'"
                  circle
                  class="fav-btn"
                  @click="toggleFavorite"
                >
                  <el-icon><Star v-if="!isFavorite" /><StarFilled v-else /></el-icon>
                </el-button>
              </div>
              <div class="info-tags">
                <el-tag effect="plain">{{ room.bedType }}</el-tag>
                <el-tag effect="plain">{{ room.area }}㎡</el-tag>
                <el-tag effect="plain">最多{{ room.maxPeople }}人</el-tag>
              </div>
              <div class="price-box">
                <span class="price-label">价格</span>
                <span class="price-value">¥{{ room.price }}</span>
                <span class="price-unit">/晚</span>
              </div>
              <div class="info-desc">
                <h4 class="desc-title">房型介绍</h4>
                <p class="desc-text">{{ room.description || '暂无介绍' }}</p>
              </div>
              <el-button
                type="primary"
                size="large"
                class="book-btn"
                @click="goReservation"
              >
                立即预订
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 评价 -->
      <el-card class="reviews-card" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><ChatDotRound /></el-icon>
            <span>用户评价</span>
            <span class="review-count">（{{ reviews.length }}条）</span>
          </div>
        </template>
        <div v-if="reviews.length" class="review-list">
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <div class="review-top">
              <div class="review-user">
                <el-avatar :size="36">{{ getUserInitial(r.userId) }}</el-avatar>
                <span class="user-name">用户{{ r.userId }}</span>
              </div>
              <el-rate
                :model-value="r.rating"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
            <div class="review-content">{{ r.content }}</div>
            <div class="review-time">{{ formatDate(r.createTime) }}</div>
            <div v-if="r.reply" class="review-reply">
              <div class="reply-title">酒店回复：</div>
              <div class="reply-text">{{ r.reply }}</div>
              <div class="reply-time" v-if="r.replyTime">{{ formatDate(r.replyTime) }}</div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评价" />
      </el-card>
    </template>
    <el-empty v-else-if="!loading" description="未找到该房型" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { getRoomTypeDetail, getRoomTypeReviews } from '@/api/public'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const room = ref(null)
const reviews = ref([])
const isFavorite = ref(false)

const placeholderImage = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=' + encodeURIComponent('hotel room interior cozy bed') + '&image_size=landscape_16_9'

const imageList = computed(() => {
  if (!room.value || !room.value.images) return [placeholderImage]
  const imgs = room.value.images
  if (Array.isArray(imgs)) return imgs.length ? imgs : [placeholderImage]
  try {
    const arr = JSON.parse(imgs)
    return Array.isArray(arr) && arr.length ? arr : [placeholderImage]
  } catch {
    return [placeholderImage]
  }
})

function getUserInitial(id) {
  if (!id) return '?'
  return String(id).slice(-2)
}

function formatDate(d) {
  if (!d) return ''
  return String(d).replace('T', ' ').slice(0, 16)
}

async function loadDetail() {
  const id = route.params.id
  loading.value = true
  try {
    const [detailRes, reviewRes] = await Promise.all([
      getRoomTypeDetail(id),
      getRoomTypeReviews(id)
    ])
    room.value = detailRes.data
    reviews.value = reviewRes.data || []
    if (userStore.isLogged()) {
      try {
        const favRes = await checkFavorite(id)
        isFavorite.value = !!favRes.data
      } catch {
        // ignore favorite check failure
      }
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

async function toggleFavorite() {
  if (!userStore.isLogged()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const id = route.params.id
  try {
    if (isFavorite.value) {
      await removeFavorite(id)
      isFavorite.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(id)
      isFavorite.value = true
      ElMessage.success('已加入收藏')
    }
  } catch (e) {
    // error handled by interceptor
  }
}

function goReservation() {
  if (!userStore.isLogged()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(`/reservation?roomTypeId=${route.params.id}`)
}

onMounted(loadDetail)
</script>

<style scoped>
.room-detail-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.main-card, .reviews-card {
  border-radius: 8px;
}
.img-carousel, .carousel-img {
  width: 100%;
  height: 380px;
  border-radius: 8px;
}
.carousel-img {
  display: block;
}
.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  background: #f5f7fa;
  border-radius: 8px;
}
.img-fallback.large {
  height: 380px;
}
.info-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.info-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.room-name {
  margin: 0;
  font-size: 24px;
  color: #303133;
}
.fav-btn {
  flex-shrink: 0;
}
.info-tags {
  display: flex;
  gap: 8px;
  margin: 16px 0;
  flex-wrap: wrap;
}
.price-box {
  background: #fdf6ec;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}
.price-label {
  color: #909399;
  font-size: 14px;
  margin-right: 12px;
}
.price-value {
  color: #f56c6c;
  font-size: 28px;
  font-weight: 700;
}
.price-unit {
  color: #909399;
  font-size: 14px;
}
.info-desc {
  margin-bottom: 20px;
  flex: 1;
}
.desc-title {
  margin: 0 0 8px;
  color: #303133;
  font-size: 16px;
}
.desc-text {
  color: #606266;
  line-height: 1.7;
  margin: 0;
  white-space: pre-wrap;
}
.book-btn {
  width: 100%;
  margin-top: auto;
}
.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.review-count {
  color: #909399;
  font-weight: normal;
  font-size: 14px;
}
.review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.review-item {
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}
.review-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.review-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 8px;
}
.review-user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-name {
  font-weight: 600;
  color: #303133;
}
.review-content {
  color: #303133;
  line-height: 1.7;
  margin-bottom: 6px;
}
.review-time {
  color: #c0c4cc;
  font-size: 12px;
}
.review-reply {
  margin-top: 12px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 3px solid #409eff;
}
.reply-title {
  color: #409eff;
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 14px;
}
.reply-text {
  color: #606266;
  line-height: 1.6;
}
.reply-time {
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 4px;
}
</style>
