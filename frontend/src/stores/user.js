import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('user_token') || '')
  const refreshToken = ref(localStorage.getItem('user_refresh') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('user_info') || 'null'))

  function setTokens(data) {
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    localStorage.setItem('user_token', data.accessToken)
    localStorage.setItem('user_refresh', data.refreshToken)
    userInfo.value = { id: data.userId, nickname: data.nickname, role: data.role }
    localStorage.setItem('user_info', JSON.stringify(userInfo.value))
  }

  function clear() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('user_token')
    localStorage.removeItem('user_refresh')
    localStorage.removeItem('user_info')
  }

  function isLogged() {
    return !!token.value
  }

  return { token, refreshToken, userInfo, setTokens, clear, isLogged }
})
