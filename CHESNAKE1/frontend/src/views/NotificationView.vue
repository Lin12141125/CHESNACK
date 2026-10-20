<template>
  <el-card shadow="never">
    <template #header><b>我的通知</b></template>
    <el-empty v-if="notifications.length === 0" description="暂无通知" />
    <div v-for="n in notifications" :key="n.id" class="notify-item" @click="read(n)">
      <el-badge :is-dot="n.isRead === 0" class="dot">
        <b>{{ n.title }}</b>
      </el-badge>
      <p class="content">{{ n.content }}</p>
      <p class="time">{{ fmt(n.createTime) }}</p>
    </div>
    <el-pagination v-if="total > 0" v-model:current-page="page" :page-size="size" :total="total"
                   layout="prev, pager, next" class="pager" @current-change="load" />
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getNotifications, markNotificationRead } from '../api'
import { onWsMessage } from '../ws'

const notifications = ref([])
const total = ref(0)
const page = ref(1)
const size = 10

let offWs = null

const fmt = (s) => (s || '').replace('T', ' ').slice(0, 16)

const load = async () => {
  const data = await getNotifications({ page: page.value, size })
  notifications.value = data.records
  total.value = Number(data.total)
}

const read = async (n) => {
  if (n.isRead === 0) {
    await markNotificationRead(n.id)
    n.isRead = 1
  }
}

onMounted(() => {
  load()
  // WebSocket 实时刷新：收到新通知时重载第一页
  offWs = onWsMessage((msg) => {
    if (msg.type === 'notification') {
      page.value = 1
      load()
    }
  })
})

onUnmounted(() => {
  if (offWs) offWs()
})
</script>

<style scoped>
.notify-item { padding: 14px; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.notify-item:hover { background: #faf7f2; }
.dot { margin-bottom: 6px; }
.content { color: #666; font-size: 14px; margin: 6px 0; }
.time { color: #aaa; font-size: 12px; }
.pager { margin-top: 16px; justify-content: center; }
</style>
