<template>
  <div class="profile-page">
    <h2 class="page-title">个人中心</h2>

    <el-tabs v-model="activeTab" class="profile-tabs">
      <el-tab-pane label="个人信息" name="info">
        <el-card v-loading="loadingProfile" shadow="never">
          <el-form
            ref="profileRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            style="max-width: 500px"
          >
            <el-form-item label="头像">
              <el-avatar :size="64" :src="profileForm.avatar" />
              <el-input
                v-model="profileForm.avatar"
                placeholder="请输入头像URL"
                style="width: 280px; margin-left: 12px"
              />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-select v-model="profileForm.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
                <el-option label="未知" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingProfile" @click="handleSaveProfile">保存</el-button>
              <el-button @click="loadProfile">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="修改密码" name="password">
        <el-card shadow="never">
          <el-form
            ref="pwdRef"
            :model="pwdForm"
            :rules="pwdRules"
            label-width="120px"
            style="max-width: 500px"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="pwdForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="pwdForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码"
              />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="pwdForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingPwd" @click="handleChangePassword">
                修改密码
              </el-button>
              <el-button @click="resetPwdForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, changePassword } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const activeTab = ref('info')
const loadingProfile = ref(false)
const savingProfile = ref(false)
const savingPwd = ref(false)
const profileRef = ref()
const pwdRef = ref()

const profileForm = reactive({
  nickname: '',
  phone: '',
  avatar: '',
  gender: 0
})

const profileRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function loadProfile() {
  loadingProfile.value = true
  try {
    const res = await getProfile()
    const data = res.data || {}
    profileForm.nickname = data.nickname || ''
    profileForm.phone = data.phone || ''
    profileForm.avatar = data.avatar || ''
    profileForm.gender = data.gender ?? 0
  } finally {
    loadingProfile.value = false
  }
}

async function handleSaveProfile() {
  await profileRef.value.validate(async (valid) => {
    if (!valid) return
    savingProfile.value = true
    try {
      await updateProfile({
        nickname: profileForm.nickname,
        phone: profileForm.phone,
        avatar: profileForm.avatar,
        gender: profileForm.gender
      })
      ElMessage.success('保存成功')
      if (userStore.userInfo) {
        userStore.userInfo = { ...userStore.userInfo, nickname: profileForm.nickname }
        localStorage.setItem('user_info', JSON.stringify(userStore.userInfo))
      }
      loadProfile()
    } finally {
      savingProfile.value = false
    }
  })
}

function resetPwdForm() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdRef.value?.clearValidate()
}

async function handleChangePassword() {
  await pwdRef.value.validate(async (valid) => {
    if (!valid) return
    savingPwd.value = true
    try {
      await changePassword({
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('密码修改成功')
      resetPwdForm()
    } finally {
      savingPwd.value = false
    }
  })
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page { max-width: 800px; margin: 0 auto; }
.page-title { margin-bottom: 20px; }
.profile-tabs { background: #fff; padding: 16px; border-radius: 8px; }
</style>
