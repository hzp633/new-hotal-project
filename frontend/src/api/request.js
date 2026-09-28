import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAdminStore } from '@/stores/admin'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：注入 Token
request.interceptors.request.use(config => {
  const path = config.url || ''
  const userStore = useUserStore()
  const adminStore = useAdminStore()

  if (path.startsWith('/admin') && !path.startsWith('/admin/auth')) {
    if (adminStore.token) config.headers.Authorization = `Bearer ${adminStore.token}`
  } else if (!path.startsWith('/auth') && !path.startsWith('/public') && !path.startsWith('/admin')) {
    if (userStore.token) config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

let isRefreshing = false
let pendingQueue = []

function onRefreshed(newToken) {
  pendingQueue.forEach(cb => cb(newToken))
  pendingQueue = []
}

// 响应拦截：统一错误处理 + 401 自动刷新
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) return res
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(res)
  },
  async error => {
    const originalConfig = error.config
    if (error.response?.status === 401 && !originalConfig._retry) {
      originalConfig._retry = true

      const path = originalConfig.url || ''
      const isAdminApi = path.startsWith('/admin')
      const store = isAdminApi ? useAdminStore() : useUserStore()

      if (store.refreshToken) {
        if (isRefreshing) {
          return new Promise(resolve => {
            pendingQueue.push(token => {
              originalConfig.headers.Authorization = `Bearer ${token}`
              resolve(request(originalConfig))
            })
          })
        }
        isRefreshing = true
        try {
          const refreshUrl = isAdminApi ? '/admin/auth/refresh' : '/auth/refresh'
          const res = await axios.post('/api' + refreshUrl, { refreshToken: store.refreshToken })
          if (res.data.code === 200) {
            store.setTokens(res.data.data)
            onRefreshed(res.data.data.accessToken)
            originalConfig.headers.Authorization = `Bearer ${res.data.data.accessToken}`
            return request(originalConfig)
          }
        } catch {
          // refresh failed
        }
        isRefreshing = false
        store.clear()
        ElMessage.warning('登录已过期，请重新登录')
        router.push(isAdminApi ? '/admin/login' : '/login')
        return Promise.reject(error)
      }
    }

    const msg = error.response?.data?.message || '网络异常'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
