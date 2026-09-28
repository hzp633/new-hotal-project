import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAdminStore } from '@/stores/admin'

const routes = [
  // 公开页面
  { path: '/login', name: 'Login', component: () => import('@/views/c/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/c/Register.vue') },

  // C 端（需登录）
  {
    path: '/',
    component: () => import('@/layouts/CLayout.vue'),
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'Home', component: () => import('@/views/c/Home.vue') },
      { path: 'rooms', name: 'RoomList', component: () => import('@/views/c/RoomList.vue') },
      { path: 'rooms/:id', name: 'RoomDetail', component: () => import('@/views/c/RoomDetail.vue') },
      { path: 'reservation', name: 'Reservation', component: () => import('@/views/c/Reservation.vue') },
      { path: 'orders', name: 'Orders', component: () => import('@/views/c/Orders.vue') },
      { path: 'orders/:id', name: 'OrderDetail', component: () => import('@/views/c/OrderDetail.vue') },
      { path: 'favorites', name: 'Favorites', component: () => import('@/views/c/Favorites.vue') },
      { path: 'guests', name: 'Guests', component: () => import('@/views/c/Guests.vue') },
      { path: 'reviews', name: 'Reviews', component: () => import('@/views/c/Reviews.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/c/Profile.vue') },
    ]
  },

  // B 端登录
  { path: '/admin/login', name: 'AdminLogin', component: () => import('@/views/b/Login.vue') },

  // B 端（需登录）
  {
    path: '/admin',
    component: () => import('@/layouts/BLayout.vue'),
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/b/Dashboard.vue') },
      { path: 'users', name: 'UserMgmt', component: () => import('@/views/b/UserMgmt.vue') },
      { path: 'hotel', name: 'HotelMgmt', component: () => import('@/views/b/HotelMgmt.vue') },
      { path: 'room-types', name: 'RoomTypeMgmt', component: () => import('@/views/b/RoomTypeMgmt.vue') },
      { path: 'rooms', name: 'RoomMgmt', component: () => import('@/views/b/RoomMgmt.vue') },
      { path: 'orders', name: 'OrderMgmt', component: () => import('@/views/b/OrderMgmt.vue') },
      { path: 'checkin', name: 'CheckInMgmt', component: () => import('@/views/b/CheckInMgmt.vue') },
      { path: 'reviews', name: 'ReviewMgmt', component: () => import('@/views/b/ReviewMgmt.vue') },
      { path: 'payments', name: 'PaymentMgmt', component: () => import('@/views/b/PaymentMgmt.vue') },
      { path: 'announcements', name: 'AnnouncementMgmt', component: () => import('@/views/b/AnnouncementMgmt.vue') },
    ]
  },

  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const adminStore = useAdminStore()

  // B 端路由
  if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
    if (!adminStore.isLogged()) {
      next('/admin/login')
      return
    }
    next()
    return
  }

  // B 端登录页：公开放行
  if (to.path === '/admin/login') {
    next()
    return
  }

  // 登录/注册页：已登录则跳首页
  if (to.path === '/login' || to.path === '/register') {
    if (userStore.isLogged()) {
      next('/home')
      return
    }
    next()
    return
  }

  // 其余 C 端路由均需登录
  if (!userStore.isLogged()) {
    next('/login')
    return
  }

  next()
})

export default router
