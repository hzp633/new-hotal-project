<template>
  <div class="reviews-page">
    <h2 class="page-title">我的评价</h2>

    <el-card v-loading="loading" shadow="never">
      <el-empty v-if="reviews.length === 0" description="暂无评价" />

      <div v-else class="review-list">
        <el-card
          v-for="review in reviews"
          :key="review.id"
          class="review-card"
          shadow="hover"
        >
          <div class="review-header">
            <div class="review-title">
              <el-icon><House /></el-icon>
              <span>房型评价</span>
            </div>
            <el-rate v-model="review.rating" disabled />
          </div>

          <div class="review-content">{{ review.content }}</div>

          <div v-if="parsedImages(review.images).length" class="review-images">
            <el-image
              v-for="(img, i) in parsedImages(review.images)"
              :key="i"
              class="review-image"
              :src="img"
              :preview-src-list="parsedImages(review.images)"
              fit="cover"
              :initial-index="i"
              preview-teleported
            >
              <template #error>
                <div class="image-placeholder">图片</div>
              </template>
            </el-image>
          </div>

          <div v-if="review.reply" class="reply-block">
            <div class="reply-title">
              <el-icon><ChatDotRound /></el-icon>
              酒店回复
            </div>
            <div class="reply-content">{{ review.reply }}</div>
            <div v-if="review.replyTime" class="reply-time">回复时间：{{ review.replyTime }}</div>
          </div>

          <div class="review-footer">
            <span class="review-time">评价时间：{{ review.createTime }}</span>
            <el-tag v-if="review.status !== undefined" size="small" :type="reviewStatusType(review.status)">
              {{ reviewStatusText(review.status) }}
            </el-tag>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyReviews } from '@/api/review'
import { House, ChatDotRound } from '@element-plus/icons-vue'

const reviews = ref([])
const loading = ref(false)

const REVIEW_STATUS = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已发布', type: 'success' },
  2: { text: '已隐藏', type: 'info' }
}

function reviewStatusText(s) {
  return REVIEW_STATUS[s]?.text || ''
}
function reviewStatusType(s) {
  return REVIEW_STATUS[s]?.type || 'info'
}

function parsedImages(images) {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    const arr = JSON.parse(images)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

async function loadReviews() {
  loading.value = true
  try {
    const res = await getMyReviews()
    reviews.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(loadReviews)
</script>

<style scoped>
.reviews-page { max-width: 900px; margin: 0 auto; }
.page-title { margin-bottom: 20px; }
.review-list { display: flex; flex-direction: column; gap: 16px; }
.review-card { border: 1px solid #ebeef5; }
.review-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.review-title { display: flex; align-items: center; gap: 6px; font-size: 15px; font-weight: bold; color: #303133; }
.review-content { color: #303133; line-height: 1.7; margin-bottom: 12px; }
.review-images { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.review-image { width: 100px; height: 100px; border-radius: 6px; background: #f5f7fa; }
.image-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #909399; background: #f5f7fa; font-size: 12px; }
.reply-block { background: #f5f7fa; border-radius: 8px; padding: 12px; margin-bottom: 12px; }
.reply-title { display: flex; align-items: center; gap: 6px; color: #409eff; font-weight: bold; font-size: 14px; margin-bottom: 6px; }
.reply-content { color: #606266; line-height: 1.6; }
.reply-time { color: #909399; font-size: 12px; margin-top: 6px; }
.review-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px dashed #ebeef5; }
.review-time { color: #909399; font-size: 12px; }
</style>
