<template>
  <div class="login-wrapper">
    <el-card class="login-card" shadow="always">
      <div class="login-title">🏨 管理员登录</div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" :prefix-icon="Lock" show-password clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="handleLogin">登录</el-button>
        </el-form-item>
        <div class="back-link">
          <router-link to="/login" class="link">← 返回用户登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { adminLogin } from '@/api/admin'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const res = await adminLogin({ username: form.username, password: form.password })
    adminStore.setTokens(res.data)
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } catch (e) {
    // 错误已在拦截器中提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2a48 0%, #001529 100%);
}
.login-card {
  width: 400px;
  padding: 24px 16px 8px;
  border-radius: 8px;
}
.login-title {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 28px;
}
.back-link {
  text-align: center;
  margin-top: 8px;
}
.back-link .link {
  color: #909399;
  font-size: 13px;
  text-decoration: none;
}
</style>
