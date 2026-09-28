<template>
  <div class="c-layout">
    <el-header class="header">
      <div class="header-left">
        <router-link to="/home" class="logo">🏨 酒店预约</router-link>
      </div>
      <el-menu mode="horizontal" :default-active="activeMenu" router class="nav-menu">
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/rooms">房型搜索</el-menu-item>
        <el-menu-item index="/orders">我的订单</el-menu-item>
        <el-menu-item index="/favorites">收藏</el-menu-item>
      </el-menu>
      <div class="header-right">
        <template v-if="userStore.isLogged()">
          <el-dropdown>
            <span class="user-name">{{ userStore.userInfo?.nickname }} <el-icon><ArrowDown /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="$router.push('/guests')">常用入住人</el-dropdown-item>
                <el-dropdown-item @click="$router.push('/reviews')">我的评价</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" text @click="$router.push('/login')">登录</el-button>
          <el-button type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
    <el-footer class="footer">
      <span>酒店预约管理系统 © 2026</span>
      <router-link to="/admin/login" class="admin-link">管理后台</router-link>
    </el-footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ArrowDown } from '@element-plus/icons-vue'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  if (route.path === '/' || route.path.startsWith('/home')) return '/home'
  if (route.path.startsWith('/rooms')) return '/rooms'
  if (route.path.startsWith('/orders')) return '/orders'
  if (route.path.startsWith('/favorites')) return '/favorites'
  return route.path
})

async function logout() {
  try {
    await request.post('/user/logout')
  } catch { /* ignore */ }
  userStore.clear()
  router.push('/login')
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  height: 60px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
  padding: 0 24px;
}
.logo { font-size: 20px; font-weight: bold; color: #409eff; white-space: nowrap; margin-right: 24px; }
.nav-menu { border-bottom: none !important; flex: 1; }
.header-right { display: flex; align-items: center; gap: 8px; }
.user-name { cursor: pointer; display: flex; align-items: center; gap: 4px; color: #606266; }
.main { min-height: calc(100vh - 120px); padding: 20px; max-width: 1200px; margin: 0 auto; width: 100%; }
.footer { display: flex; align-items: center; justify-content: center; gap: 16px; color: #909399; font-size: 14px; }
.admin-link { color: #c0c4cc; font-size: 13px; text-decoration: none; }
.admin-link:hover { color: #409eff; }
</style>
