<template>
  <div class="auth-page">
    <div class="auth-card">
      <img src="/素材/白底logo.png" class="logo" />
      <h2>登录 CHESNAKE</h2>
      <el-form :model="form" @submit.prevent="submit">
        <el-form-item>
          <el-input v-model="form.email" placeholder="邮箱账号" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-radio-group v-model="form.role">
            <el-radio :value="2">用户</el-radio>
            <el-radio :value="1">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-button type="primary" size="large" class="submit" native-type="submit" :loading="loading">登录</el-button>
      </el-form>
      <p class="tip">还没有账号？<router-link to="/register">立即注册</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ email: '', password: '', role: 2 })

const submit = async () => {
  if (!form.email || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const data = await login({ ...form })
    auth.setLogin(data)
    ElMessage.success('登录成功')
    if (data.role === 1) router.push('/admin')
    else router.push(route.query.redirect || '/')
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
.tip { margin-top: 14px; color: #888; }
.tip a { color: #f2866a; }
</style>
