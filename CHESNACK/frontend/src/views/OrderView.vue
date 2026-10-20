<template>
  <el-card shadow="never">
    <template #header><b>我的订单</b></template>

    <!-- 订单搜索与筛选（订单号/商品名关键词 + 日期区间 + 评价状态） -->
    <div class="filter-bar">
      <el-input v-model="filters.keyword" placeholder="搜索订单号 / 商品名" clearable
                class="kw" @keyup.enter="search" @clear="search" />
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
                      start-placeholder="开始日期" end-placeholder="结束日期"
                      value-format="YYYY-MM-DD" class="dr" @change="search" />
      <el-select v-model="filters.evaluated" placeholder="评价状态" clearable class="st" @change="search">
        <el-option label="已评价" :value="1" />
        <el-option label="未评价" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-empty v-if="orders.length === 0" description="暂无订单" />
    <el-table v-else :data="orders">
      <el-table-column prop="orderNo" label="订单号" width="220" />
      <el-table-column label="商品" min-width="240">
        <template #default="{ row }">
          <div class="goods-cell">
            <img :src="`/images/${row.goodsImg || '1.jpg'}`" class="thumb" @click="$router.push(`/goods/${row.goodId}`)" />
            <span class="g-name" @click="$router.push(`/goods/${row.goodId}`)">{{ row.goodsName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="数量" width="90" />
      <el-table-column label="金额" width="120">
        <template #default="{ row }">${{ row.price }}</template>
      </el-table-column>
      <el-table-column label="下单时间" width="170">
        <template #default="{ row }">{{ fmt(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default>
          <el-tag type="success">已支付</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.evaluated" type="info" size="small">已评价</el-tag>
          <el-button v-else link type="primary" @click="openEval(row)">评价</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="total > 0" v-model:current-page="page" :page-size="size" :total="total"
                   layout="prev, pager, next" class="pager" @current-change="load" />
  </el-card>

  <el-dialog v-model="evalVisible" title="评价订单" width="480px">
    <el-form>
      <el-form-item label="评分">
        <el-rate v-model="evalForm.level" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="evalForm.content" type="textarea" :rows="4" placeholder="说说你的使用感受..." />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="evalVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEval">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrders, addComment } from '../api'

const orders = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const evalVisible = ref(false)
const evalForm = ref({ orderId: null, level: 5, content: '' })
const filters = ref({ keyword: '', evaluated: null })
const dateRange = ref(null)

const fmt = (s) => (s || '').replace('T', ' ').slice(0, 16)

const load = async () => {
  // 后端已 join 商品名/图，一次查询返回，无需逐单 N+1；关键词/日期/评价状态筛选由后端处理
  const data = await getOrders({
    page: page.value,
    size,
    keyword: filters.value.keyword.trim() || undefined,
    startDate: dateRange.value?.[0] || undefined,
    endDate: dateRange.value?.[1] || undefined,
    evaluated: filters.value.evaluated ?? undefined
  })
  orders.value = data.records
  total.value = Number(data.total)
}

const search = () => {
  page.value = 1
  load()
}

const reset = () => {
  filters.value = { keyword: '', evaluated: null }
  dateRange.value = null
  search()
}

const openEval = (row) => {
  evalForm.value = { orderId: row.id, level: 5, content: '' }
  evalVisible.value = true
}

const submitEval = async () => {
  if (!evalForm.value.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  await addComment({ orderId: evalForm.value.orderId, content: evalForm.value.content.trim(), level: evalForm.value.level })
  ElMessage.success('评价成功')
  evalVisible.value = false
  // 重新加载：该订单状态变为「已评价」，评价入口关闭
  load()
}

onMounted(load)
</script>

<style scoped>
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-bar .kw { width: 240px; }
.filter-bar .dr { width: 280px; }
.filter-bar .st { width: 140px; }
.goods-cell { display: flex; align-items: center; gap: 12px; }
.thumb { width: 50px; height: 50px; object-fit: cover; border-radius: 6px; cursor: pointer; }
.g-name { cursor: pointer; }
.g-name:hover { color: #f2866a; }
.pager { margin-top: 16px; justify-content: center; }
</style>
