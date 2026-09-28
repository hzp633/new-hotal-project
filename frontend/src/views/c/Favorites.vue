<template>
  <div class="favorites-page">
    <h2 class="page-title">我的收藏</h2>

    <el-card v-loading="loading" shadow="never">
      <el-empty v-if="favorites.length === 0" description="暂无收藏，去逛逛吧" >
        <el-button type="primary" @click="router.push('/rooms')">浏览房型</el-button>
      </el-empty>

      <div v-else class="fav-grid">
        <el-card
          v-for="item in favorites"
          :key="item.id"
          class="fav-card"
          shadow="hover"
          :body-style="{ padding: '0' }"
        >
          <el-image
            class="fav-image"
            :src="firstImage(item.images)"
            fit="cover"
          >
            <template #error>
              <div class="image-placeholder">暂无图片</div>
            </template>
          </el-image>
          <div class="fav-info">
            <h4 class="fav-name">{{ item.roomTypeName }}</h4>
            <p class="fav-meta">
              <span class="fav-price">¥{{ item.price }} / 晚</span>
            </p>
            <p class="fav-meta">
              <span v-if="item.bedType">床型：{{ item.bedType }}</span>
              <span v-if="item.area">面积：{{ item.area }}㎡</span>
            </p>
            <p class="fav-time">收藏于 {{ item.createTime }}</p>
            <div class="fav-actions">
              <el-button type="primary" size="small" @click="router.push(`/rooms/${item.roomTypeId}`)">
                查看详情
              </el-button>
              <el-button type="danger" size="small" plain @click="handleRemove(item)">
                取消收藏
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFavorites, removeFavorite } from '@/api/favorite'

const router = useRouter()

const favorites = ref([])
const loading = ref(false)

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

async function loadFavorites() {
  loading.value = true
  try {
    const res = await getFavorites()
    favorites.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleRemove(item) {
  try {
    await ElMessageBox.confirm(`确认取消收藏「${item.roomTypeName}」？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await removeFavorite(item.roomTypeId)
    ElMessage.success('已取消收藏')
    loadFavorites()
  } catch (e) {
    /* cancelled by user */
  }
}

onMounted(loadFavorites)
</script>

<style scoped>
.favorites-page { max-width: 1100px; margin: 0 auto; }
.page-title { margin-bottom: 20px; }
.fav-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.fav-card { overflow: hidden; }
.fav-image { width: 100%; height: 180px; background: #f5f7fa; }
.image-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #909399; background: #f5f7fa; }
.fav-info { padding: 14px; }
.fav-name { margin: 0 0 8px; font-size: 16px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.fav-meta { margin: 4px 0; color: #606266; font-size: 13px; display: flex; gap: 16px; }
.fav-price { color: #f56c6c; font-size: 16px; font-weight: bold; }
.fav-time { color: #909399; font-size: 12px; margin: 4px 0 12px; }
.fav-actions { display: flex; gap: 8px; }
</style>
