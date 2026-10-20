<template>
  <el-card shadow="never">
    <template #header><b>操作审计日志（RabbitMQ 异步落库）</b></template>
    <el-table :data="logs" v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="操作人" width="140" />
      <el-table-column prop="action" label="动作" width="160" />
      <el-table-column prop="detail" label="详情" min-width="260" show-overflow-tooltip />
      <el-table-column label="时间" width="170">
        <template #default="{ row }">{{ fmt(row.createTime) }}</template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                   layout="prev, pager, next, total" class="pager" @current-change="load" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminLogs } from '../../api'

const logs = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)
const fmt = (s) => (s || '').replace('T', ' ').slice(0, 16)

const load = async () => {
  loading.value = true
  try {
    const data = await adminLogs({ page: page.value, size })
    logs.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
