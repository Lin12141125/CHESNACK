<template>
  <el-card shadow="never">
    <template #header>
      <div class="cart-head">
        <b>购物车（{{ items.length }}）</b>
        <div>
          <span class="total">合计：${{ total.toFixed(2) }}</span>
          <el-button type="danger" size="large" @click="onPay" :disabled="items.length === 0">结算</el-button>
        </div>
      </div>
    </template>
    <el-empty v-if="items.length === 0" description="购物车空空如也" />
    <el-table v-else :data="items" @selection-change="onSelection">
      <el-table-column type="selection" width="50" />
      <el-table-column label="商品" min-width="260">
        <template #default="{ row }">
          <div class="goods-cell">
            <img :src="`/images/${row.img1 || '1.jpg'}`" class="thumb" @click="$router.push(`/goods/${row.goodId}`)" />
            <span @click="$router.push(`/goods/${row.goodId}`)" class="g-name">{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="单价" width="120">
        <template #default="{ row }">${{ row.unitPrice }}</template>
      </el-table-column>
      <el-table-column label="数量" width="180">
        <template #default="{ row }">
          <el-input-number v-model="row.amount" :min="1" size="small" @change="(v) => changeAmount(row, v)" />
        </template>
      </el-table-column>
      <el-table-column label="小计" width="120">
        <template #default="{ row }">${{ Number(row.price).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCart, updateCart, deleteCart, pay } from '../api'

const route = useRoute()
const router = useRouter()
const items = ref([])
const selected = ref([])

const total = ref(0)

const load = async () => { items.value = await getCart() }

const onSelection = (rows) => {
  selected.value = rows
  total.value = rows.reduce((s, r) => s + Number(r.price), 0)
}

const changeAmount = async (row, v) => {
  await updateCart(row.id, v)
  const cart = await getCart()
  const r = cart.find((x) => x.id === row.id)
  if (r) row.price = r.price
  onSelection([...selected.value])
}

const onDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该商品？', '提示')
  await deleteCart(row.id)
  await load()
  onSelection([])
}

const onPay = async () => {
  if (selected.value.length === 0) {
    ElMessage.warning('请先勾选要结算的商品')
    return
  }
  await ElMessageBox.confirm(`确认支付 $${total.value.toFixed(2)} ？`, '支付确认', { type: 'warning' })
  const ids = selected.value.map((r) => r.goodId)
  await pay(ids)
  ElMessage.success('支付成功，订单生成中...')
  await load()
  onSelection([])
  if (route.query.pay) router.push('/orders')
}

onMounted(load)
</script>

<style scoped>
.cart-head { display: flex; justify-content: space-between; align-items: center; }
.total { color: #f2866a; font-weight: 700; font-size: 18px; margin-right: 16px; }
.goods-cell { display: flex; align-items: center; gap: 12px; }
.thumb { width: 60px; height: 60px; object-fit: cover; border-radius: 6px; cursor: pointer; }
.g-name { cursor: pointer; }
.g-name:hover { color: #f2866a; }
</style>
