<template>
  <div class="room-list-page">
    <!-- 筛选区 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filter" class="filter-form">
        <el-form-item label="价格范围">
          <el-input-number
            v-model="filter.minPrice"
            :min="0"
            :step="100"
            placeholder="最低价"
            :controls="false"
            class="price-input"
          />
          <span class="price-sep">-</span>
          <el-input-number
            v-model="filter.maxPrice"
            :min="0"
            :step="100"
            placeholder="最高价"
            :controls="false"
            class="price-input"
          />
        </el-form-item>
        <el-form-item label="床型">
          <el-select v-model="filter.bedType" placeholder="全部床型" clearable class="bed-select">
            <el-option label="大床房" value="大床房" />
            <el-option label="双床房" value="双床房" />
            <el-option label="单床房" value="单床房" />
            <el-option label="套房" value="套房" />
            <el-option label="家庭房" value="家庭房" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="filter.orderBy" placeholder="默认排序" clearable class="order-select">
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 房型列表 -->
    <el-card class="list-card" shadow="never" v-loading="loading">
      <el-row :gutter="20" v-if="roomTypes.length">
        <el-col
          v-for="room in roomTypes"
          :key="room.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="room-card" shadow="hover" :body-style="{ padding: '0' }">
            <el-image
              :src="getFirstImage(room.images)"
              fit="cover"
              class="room-img"
            >
              <template #error>
                <div class="img-fallback">无图片</div>
              </template>
            </el-image>
            <div class="room-body">
              <h3 class="room-name">{{ room.name }}</h3>
              <div class="room-tags">
                <el-tag size="small" effect="plain">{{ room.bedType }}</el-tag>
                <el-tag size="small" effect="plain">{{ room.area }}㎡</el-tag>
                <el-tag size="small" effect="plain">最多{{ room.maxPeople }}人</el-tag>
              </div>
              <p class="room-desc">{{ room.description }}</p>
              <div class="room-footer">
                <span class="room-price">¥{{ room.price }}</span>
                <div class="room-actions">
                  <el-button size="small" @click="goDetail(room.id)">查看详情</el-button>
                  <el-button type="primary" size="small" @click="goReservation(room.id)">
                    立即预订
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else-if="!loading" description="没有找到符合条件的房型" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { searchRoomTypes } from '@/api/public'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const roomTypes = ref([])

const defaultFilter = () => ({
  minPrice: undefined,
  maxPrice: undefined,
  bedType: '',
  orderBy: ''
})
const filter = reactive(defaultFilter())

const placeholderImage = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=' + encodeURIComponent('hotel room interior cozy bed') + '&image_size=landscape_16_9'

function getFirstImage(images) {
  if (!images) return placeholderImage
  if (Array.isArray(images)) return images[0] || placeholderImage
  try {
    const arr = JSON.parse(images)
    return (Array.isArray(arr) && arr[0]) || placeholderImage
  } catch {
    return placeholderImage
  }
}

function buildParams() {
  const params = {}
  if (filter.minPrice != null && filter.minPrice !== '') params.minPrice = filter.minPrice
  if (filter.maxPrice != null && filter.maxPrice !== '') params.maxPrice = filter.maxPrice
  if (filter.bedType) params.bedType = filter.bedType
  if (filter.orderBy) params.orderBy = filter.orderBy
  return params
}

async function loadRooms() {
  loading.value = true
  try {
    const res = await searchRoomTypes(buildParams())
    roomTypes.value = res.data || []
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

function onSearch() {
  if (filter.minPrice != null && filter.maxPrice != null &&
      filter.minPrice !== '' && filter.maxPrice !== '' &&
      Number(filter.minPrice) > Number(filter.maxPrice)) {
    ElMessage.warning('最低价不能高于最高价')
    return
  }
  loadRooms()
}

function onReset() {
  Object.assign(filter, defaultFilter())
  loadRooms()
}

function goDetail(id) {
  router.push(`/rooms/${id}`)
}

function goReservation(id) {
  if (!userStore.isLogged()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(`/reservation?roomTypeId=${id}`)
}

onMounted(loadRooms)
</script>

<style scoped>
.room-list-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-card, .list-card {
  border-radius: 8px;
}
.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}
.price-input {
  width: 110px;
}
.price-sep {
  margin: 0 8px;
  color: #909399;
}
.bed-select, .order-select {
  width: 180px;
}
.room-card {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}
.room-img {
  width: 100%;
  height: 180px;
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
}
.room-body {
  padding: 14px;
}
.room-name {
  margin: 0 0 10px;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.room-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 8px;
  min-height: 24px;
}
.room-desc {
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
  margin: 0 0 12px;
  height: 39px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.room-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}
.room-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 600;
}
.room-price::after {
  content: ' /晚';
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}
.room-actions {
  display: flex;
  gap: 6px;
}
</style>
