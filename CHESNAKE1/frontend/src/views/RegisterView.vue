<template>
  <div class="auth-page">
    <div class="auth-card">
      <img src="/素材/白底logo.png" class="logo" />
      <h2>注册账号</h2>
      <el-form :model="form" @submit.prevent="submit">
        <el-form-item>
          <el-input v-model="form.email" placeholder="邮箱账号" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.username" placeholder="昵称" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.confirm" type="password" placeholder="确认密码" size="large" show-password />
        </el-form-item>
        <p class="role-tip">注册即为普通用户，管理员账号由管理员后台创建</p>
        <el-button type="primary" size="large" class="submit" native-type="submit" :loading="loading">注册</el-button>
      </el-form>
      <p class="tip">已有账号？<router-link to="/login">去登录</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api'

const router = useRouter()
const loading = ref(false)
const form = reactive({ email: '', username: '', password: '', confirm: '' })

const submit = async () => {
  if (!form.email || !form.username || !form.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (form.password !== form.confirm) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await register({ email: form.email, username: form.username, password: form.password })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fdf6ec 0%, #ffe9d6 100%);
}
.auth-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 36px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  text-align: center;
}
.logo { height: 72px; margin-bottom: 8px; }
h2 { margin-bottom: 20px; color: #444; }
.submit { width: 100%; margin-top: 8px; }
.role-tip { color: #999; font-size: 12px; margin: 4px 0 10px; }
.tip { margin-top: 14px; color: #888; }
.tip a { color: #f2866a; }
</style>
