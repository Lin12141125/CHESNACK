<template>
  <div>
    <el-card shadow="never">
      <template #header><b>被举报评价（{{ reports.length }}）</b></template>
      <el-empty v-if="reports.length === 0" description="暂无被举报的评价" />
      <div v-for="r in reports" :key="r.id" class="report-item">
        <div class="head">
          <b>{{ r.username }}</b>
          <el-rate :model-value="r.level || 0" disabled />
          <span class="time">{{ fmt(r.sendTime) }}</span>
        </div>
        <p class="content">{{ r.content }}</p>
        <el-button type="danger" size="small" @click="remove(r)">删除该评价</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminReports, adminDeleteReport } from '../../api'

const reports = ref([])
const fmt = (s) => (s || '').replace('T', ' ').slice(0, 16)

const load = async () => { reports.value = await adminReports() }

const remove = async (r) => {
  await ElMessageBox.confirm('确定删除该被举报评价？', '提示', { type: 'warning' })
  await adminDeleteReport(r.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.report-item { padding: 14px; border-bottom: 1px solid #f0f0f0; }
.head { display: flex; align-items: center; gap: 14px; }
.time { color: #999; font-size: 12px; }
.content { margin: 8px 0; color: #555; }
</style>
