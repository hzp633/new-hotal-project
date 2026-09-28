<template>
  <div class="home-page" v-loading="loading">
    <!-- 酒店信息 -->
    <el-card class="hotel-card" shadow="never" v-if="hotel">
      <div class="hotel-info">
        <div class="hotel-cover">
          <el-image
            v-if="hotel.coverImage"
            :src="hotel.coverImage"
            fit="cover"
            class="cover-img"
          />
          <el-image
            v-else
            :src="placeholderImage"
            fit="cover"
            class="cover-img"
          />
        </div>
        <div class="hotel-detail">
          <div class="hotel-title-row">
            <h2 class="hotel-name">{{ hotel.name }}</h2>
            <el-rate
              :model-value="hotel.star || 0"
              disabled
              show-score
              text-color="#ff9900"
              class="hotel-star"
            />
          </div>
          <p class="hotel-intro">{{ hotel.intro }}</p>
          <div class="hotel-meta">
            <p class="meta-line"><el-icon><Location /></el-icon> 地址：{{ hotel.address }}</p>
            <p class="meta-line"><el-icon><Phone /></el-icon> 电话：{{ hotel.phone }}</p>
          </div>
          <div class="hotel-facilities" v-if="facilityList.length">
            <span class="facilities-label">设施：</span>
            <el-tag
              v-for="(f, idx) in facilityList"
              :key="idx"
              class="facility-tag"
              effect="plain"
            >
              {{ f }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 公告 -->
    <el-card class="section-card" shadow="never" v-if="announcements.length">
      <template #header>
        <div class="section-header">
          <el-icon><Bell /></el-icon>
          <span>酒店公告</span>
        </div>
      </template>
      <el-carousel height="120px" direction="vertical" :autoplay="true" indicator-position="none">
        <el-carousel-item
          v-for="ann in sortedAnnouncements"
          :key="ann.id"
        >
          <div class="announcement-item">
            <div class="announcement-title">
              <el-tag v-if="ann.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
              <span>{{ ann.title }}</span>
            </div>
            <div class="announcement-content">{{ ann.content }}</div>
            <div class="announcement-time">{{ formatDate(ann.createTime) }}</div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <!-- 房型 -->
    <el-card class="section-card" shadow="never">
      <template #header>
        <div class="section-header">
          <el-icon><House /></el-icon>
          <span>房型推荐</span>
          <el-button text type="primary" class="more-btn" @click="$router.push('/rooms')">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>
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
              <div class="room-footer">
                <span class="room-price">¥{{ room.price }}</span>
                <el-button type="primary" size="small" @click="goDetail(room.id)">立即预订</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="暂无房型信息" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHotel, getAnnouncements, getRoomTypes } from '@/api/public'

const router = useRouter()

const loading = ref(false)
const hotel = ref(null)
const announcements = ref([])
const roomTypes = ref([])

const placeholderImage = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=' + encodeURIComponent('luxury hotel exterior building') + '&image_size=landscape_16_9'

const facilityList = computed(() => {
  if (!hotel.value || !hotel.value.facilities) return []
  const f = hotel.value.facilities
  if (Array.isArray(f)) return f
  try {
    return JSON.parse(f)
  } catch {
    return String(f).split(/[,，]/).map(s => s.trim()).filter(Boolean)
  }
})

const sortedAnnouncements = computed(() => {
  return [...announcements.value].sort((a, b) => {
    if (a.isTop !== b.isTop) return b.isTop - a.isTop
    return new Date(b.createTime) - new Date(a.createTime)
  })
})

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

function formatDate(d) {
  if (!d) return ''
  return String(d).replace('T', ' ').slice(0, 16)
}

function goDetail(id) {
  router.push(`/rooms/${id}`)
}

async function loadData() {
  loading.value = true
  try {
    const [hotelRes, annRes, roomRes] = await Promise.all([
      getHotel(),
      getAnnouncements(),
      getRoomTypes()
    ])
    hotel.value = hotelRes.data
    announcements.value = annRes.data || []
    roomTypes.value = roomRes.data || []
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.hotel-card, .section-card {
  border-radius: 8px;
}
.hotel-info {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}
.hotel-cover {
  flex: 0 0 360px;
  max-width: 360px;
}
.cover-img {
  width: 100%;
  height: 220px;
  border-radius: 8px;
  display: block;
}
.hotel-detail {
  flex: 1;
  min-width: 280px;
}
.hotel-title-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.hotel-name {
  margin: 0;
  font-size: 24px;
  color: #303133;
}
.hotel-star {
  margin-left: 8px;
}
.hotel-intro {
  color: #606266;
  line-height: 1.7;
  margin: 12px 0;
}
.hotel-meta {
  margin: 12px 0;
}
.meta-line {
  margin: 6px 0;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
}
.facilities-label {
  color: #909399;
  margin-right: 4px;
}
.facility-tag {
  margin: 4px 6px 4px 0;
}
.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.more-btn {
  margin-left: auto;
}
.announcement-item {
  padding: 8px 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.announcement-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.announcement-content {
  color: #606266;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.announcement-time {
  color: #c0c4cc;
  font-size: 12px;
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
  margin-bottom: 10px;
  min-height: 24px;
}
.room-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
</style>
