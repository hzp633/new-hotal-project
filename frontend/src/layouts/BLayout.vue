<template>
  <el-container class="b-layout">
    <el-aside width="220px" class="sidebar">
      <div class="sidebar-logo">🏨 后台管理</div>
      <el-menu :default-active="route.path" router class="sidebar-menu" background-color="#001529" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/admin/dashboard"><el-icon><DataLine /></el-icon>数据仪表盘</el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User /></el-icon>用户管理</el-menu-item>
        <el-menu-item index="/admin/hotel"><el-icon><OfficeBuilding /></el-icon>酒店信息</el-menu-item>
        <el-menu-item index="/admin/room-types"><el-icon><House /></el-icon>房型管理</el-menu-item>
        <el-menu-item index="/admin/rooms"><el-icon><Key /></el-icon>房间管理</el-menu-item>
        <el-menu-item index="/admin/orders"><el-icon><List /></el-icon>订单管理</el-menu-item>
        <el-menu-item index="/admin/checkin"><el-icon>< Tickets /></el-icon>入住退住</el-menu-item>
        <el-menu-item index="/admin/reviews"><el-icon><ChatDotRound /></el-icon>评论管理</el-menu-item>
        <el-menu-item index="/admin/payments"><el-icon><Money /></el-icon>退款处理</el-menu-item>
        <el-menu-item index="/admin/announcements"><el-icon><Bell /></el-icon>公告管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="b-header">
        <span>{{ adminStore.adminInfo?.nickname || '管理员' }}</span>
        <el-button type="text" @click="logout">退出</el-button>
      </el-header>
      <el-main class="b-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import request from '@/api/request'
import { DataLine, User, OfficeBuilding, House, Key, List, Tickets, ChatDotRound, Money, Bell } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

async function logout() {
  try { await request.post('/admin/logout') } catch {}
  adminStore.clear()
  router.push('/admin/login')
}
</script>

<style scoped>
.b-layout { height: 100vh; }
.sidebar { background: #001529; }
.sidebar-logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 18px; font-weight: bold; }
.sidebar-menu { border-right: none; }
.b-header { display: flex; align-items: center; justify-content: flex-end; gap: 16px; background: #fff; border-bottom: 1px solid #e4e7ed; }
.b-main { background: #f0f2f5; }
</style>
