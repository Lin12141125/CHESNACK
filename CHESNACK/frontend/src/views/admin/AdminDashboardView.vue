<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="hover"><div class="stat"><b>{{ stats.goods }}</b><p>商品总数</p></div></el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover"><div class="stat"><b>{{ stats.orders }}</b><p>订单总数</p></div></el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover"><div class="stat"><b>{{ stats.reports }}</b><p>待处理举报</p></div></el-card>
      </el-col>
    </el-row>
    <el-card shadow="never" class="tips">
      <template #header><b>技术栈说明</b></template>
      <p>本系统后端基于 Spring Boot 4 + Spring Security(JWT) + MyBatis-Plus + Redis + RabbitMQ + MySQL：</p>
      <ul>
        <li>商品查询缓存实现 穿透(空值缓存)/击穿(SETNX互斥锁)/雪崩(随机TTL) 三重防护</li>
        <li>下单支付、注册通知、社区事件、管理员审计均通过 RabbitMQ 异步解耦</li>
        <li>管理操作自动写入审计日志</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getGoodsList, getOrders, adminReports } from '../../api'

const stats = ref({ goods: 0, orders: 0, reports: 0 })
onMounted(async () => {
  const [g, o, r] = await Promise.all([
    getGoodsList({ page: 1, size: 1 }),
    getOrders({ page: 1, size: 1 }),
    adminReports()
  ])
  stats.value = { goods: Number(g.total), orders: Number(o.total), reports: r.length }
})
</script>

<style scoped>
.stat { text-align: center; padding: 12px; }
.stat b { font-size: 32px; color: #f2866a; }
.stat p { color: #888; margin-top: 6px; }
.tips { margin-top: 16px; }
.tips ul { margin: 8px 0 0 20px; line-height: 1.9; color: #666; }
</style>
