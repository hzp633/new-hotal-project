import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const refreshToken = ref(localStorage.getItem('admin_refresh') || '')
  const adminInfo = ref(JSON.parse(localStorage.getItem('admin_info') || 'null'))

  function setTokens(data) {
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    localStorage.setItem('admin_token', data.accessToken)
    localStorage.setItem('admin_refresh', data.refreshToken)
    adminInfo.value = { id: data.userId, nickname: data.nickname, role: data.role }
    localStorage.setItem('admin_info', JSON.stringify(adminInfo.value))
  }

  function clear() {
    token.value = ''
    refreshToken.value = ''
    adminInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_refresh')
    localStorage.removeItem('admin_info')
  }

  function isLogged() {
    return !!token.value
  }

  return { token, refreshToken, adminInfo, setTokens, clear, isLogged }
})
