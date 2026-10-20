<template>
  <div class="user-layout">
    <header class="navbar">
      <div class="nav-left">
        <img src="/素材/白底logo.png" class="logo" @click="$router.push('/')" />
        <el-input v-model="keyword" placeholder="搜索零食..." class="search-input" @keyup.enter="doSearch" clearable>
          <template #append>
            <el-button @click="doSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
      <nav class="nav-menu">
        <router-link to="/">首页</router-link>
        <router-link to="/community">社区</router-link>
        <router-link to="/notifications">通知
          <el-badge :value="unread" :hidden="!unread" class="badge" />
        </router-link>
        <router-link to="/cart">购物车</router-link>
        <router-link to="/orders">订单</router-link>
      </nav>
      <div class="nav-user">
        <el-dropdown @command="onCommand">
          <span class="user-name">
            <img src="/素材/avatar.jpg" class="avatar" />
            {{ auth.user?.username || '用户' }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="orders">我的订单</el-dropdown-item>
              <el-dropdown-item command="admin" v-if="auth.isAdmin">管理后台</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { getUnreadCount, logout as apiLogout } from '../api'
import { connectWs, disconnectWs, onWsMessage } from '../ws'

const router = useRouter()
const auth = useAuthStore()
const keyword = ref('')
const unread = ref(0)

let pollTimer = null
let offWs = null

const loadUnread = async () => {
  try { unread.value = await getUnreadCount() } catch { /* ignore */ }
}

// WebSocket 实时通知（优化11）：未读角标实时更新 + 弹窗提醒；轮询作为兜底
const handleWsMessage = (msg) => {
  if (msg.type === 'notification') {
    unread.value = msg.unread != null ? Number(msg.unread) : unread.value + 1
    ElNotification({ title: msg.title || '新通知', message: msg.content || '', type: 'info', duration: 5000 })
  } else if (msg.type === 'order') {
    ElNotification({ title: msg.title || '订单完成', message: msg.content || '', type: 'success', duration: 5000 })
  }
}

const doSearch = () => {
  if (keyword.value.trim()) router.push({ path: '/search', query: { keyword: keyword.value.trim() } })
}

const onCommand = async (cmd) => {
  if (cmd === 'logout') {
    try { await apiLogout() } catch { /* 网络异常也继续本地登出 */ }
    disconnectWs()
    auth.logout()
    router.push('/login')
  } else if (cmd === 'admin') router.push('/admin')
  else router.push('/' + cmd)
}

onMounted(() => {
  loadUnread()
  connectWs()
  offWs = onWsMessage(handleWsMessage)
  // 兜底轮询：每 60s 同步一次未读数
  pollTimer = setInterval(loadUnread, 60000)
})

onUnmounted(() => {
  if (offWs) offWs()
  disconnectWs()
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}
.nav-left { display: flex; align-items: center; gap: 16px; }
.logo { height: 44px; cursor: pointer; }
.search-input { width: 320px; }
.nav-menu { display: flex; gap: 24px; }
.nav-menu a { color: #555; font-weight: 500; }
.nav-menu a.router-link-active { color: #f2866a; }
.badge { margin-left: 4px; }
.nav-user .user-name { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.avatar { width: 36px; height: 36px; border-radius: 50%; }
.content { max-width: 1200px; margin: 0 auto; padding: 20px; }
</style>
