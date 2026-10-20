<template>
  <div>
    <div class="head">
      <b>商品列表（{{ total }}）</b>
      <el-button type="primary" @click="openDialog()">+ 添加商品</el-button>
    </div>
    <el-table :data="goods" v-loading="loading">
      <el-table-column label="图片" width="90">
        <template #default="{ row }">
          <img :src="`/images/${row.img1 || '1.jpg'}`" class="thumb" />
        </template>
      </el-table-column>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="220" show-overflow-tooltip />
      <el-table-column prop="brand" label="品牌" width="140" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="90" />
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="saleQuantity" label="销量" width="90" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                   layout="prev, pager, next, total" class="pager" @current-change="load" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '添加商品'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option v-for="t in types" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item>
        <el-form-item label="产地"><el-input v-model="form.origin" /></el-form-item>
        <el-form-item label="净含量(g)"><el-input-number v-model="form.netContent" :min="1" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.img1" placeholder="images/1.jpg 或 uploads/xxx.jpg" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGoodsList, getGoodsFilters, adminSaveGoods, adminDeleteGoods } from '../../api'

const types = ['dried meat', 'chili', 'confectionery', 'bread', 'meat', 'nut', 'puffed food', 'candy', 'chocolate', 'biscuits']
const goods = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', type: 'bread', brand: '', origin: '', netContent: 100, price: 1, stock: 100, img1: '' })

const load = async () => {
  loading.value = true
  try {
    const data = await getGoodsList({ page: page.value, size })
    goods.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  Object.assign(form, row || { id: null, name: '', type: 'bread', brand: '', origin: '', netContent: 100, price: 1, stock: 100, img1: '' })
  dialogVisible.value = true
}

const save = async () => {
  if (!form.name || !form.brand || !form.origin) {
    ElMessage.warning('请填写完整信息')
    return
  }
  await adminSaveGoods({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除商品「${row.name}」？`, '提示', { type: 'warning' })
  await adminDeleteGoods(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(async () => {
  await load()
  try { const f = await getGoodsFilters(); if (f.types?.length) types.splice(0, types.length, ...f.types) } catch { /* ignore */ }
})
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.thumb { width: 50px; height: 50px; object-fit: cover; border-radius: 6px; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
