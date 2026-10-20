<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="admin-logo">
        <img src="/素材/白底logo.png" />
        <span>管理员后台</span>
      </div>
      <el-menu :default-active="active" router background-color="#2b3a4a" text-color="#cfd8e3" active-text-color="#f2866a">
        <el-menu-item index="/admin">仪表盘</el-menu-item>
        <el-menu-item index="/admin/warehouse">仓库管理</el-menu-item>
        <el-menu-item index="/admin/reports">举报管理</el-menu-item>
        <el-menu-item index="/admin/logs">审计日志</el-menu-item>
        <el-menu-item index="/" >返回商城</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ auth.user?.username }}</span>
        <el-button link type="danger" @click="logout">退出</el-button>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { logout as apiLogout } from '../api'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const active = computed(() => route.path)
const logout = async () => {
  try { await apiLogout() } catch { /* ignore */ }
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout { min-height: 100vh; }
.aside { background: #2b3a4a; }
.admin-logo { display: flex; align-items: center; gap: 8px; padding: 16px; color: #fff; }
.admin-logo img { height: 40px; background: #fff; border-radius: 6px; }
.header { display: flex; align-items: center; justify-content: flex-end; gap: 16px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
</style>
