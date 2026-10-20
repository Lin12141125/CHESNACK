<template>
  <div>
    <!-- 轮播 -->
    <el-carousel height="340px" class="banner">
      <el-carousel-item v-for="n in 4" :key="n">
        <img :src="`/素材/snack${n}.jpg`" class="banner-img" />
      </el-carousel-item>
    </el-carousel>

    <!-- 猜你喜欢 -->
    <div class="section-title"><b>猜你喜欢</b></div>
    <div class="goods-grid">
      <el-card v-for="g in goods" :key="g.id" class="goods-card" shadow="hover"
               @click="$router.push(`/goods/${g.id}`)">
        <img :src="`/images/${g.img1 || '1.jpg'}`" class="goods-img" />
        <p class="name">{{ g.name }}</p>
        <p class="price">${{ g.price }}</p>
        <p class="sold">{{ g.saleQuantity }} copies sold</p>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getGoodsList } from '../api'

const goods = ref([])

onMounted(async () => {
  const page = await getGoodsList({ field: 'score', direction: 'desc', page: 1, size: 12 })
  goods.value = page.records
})
</script>

<style scoped>
.banner { border-radius: 12px; overflow: hidden; }
.banner-img { width: 100%; height: 100%; object-fit: cover; }
.section-title { margin: 24px 0 16px; font-size: 20px; color: #f2866a; }
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.goods-card { cursor: pointer; }
.goods-img { width: 100%; height: 180px; object-fit: cover; border-radius: 8px; }
.name {
  margin-top: 8px;
  font-weight: 600;
  font-size: 14px;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.price { color: #f2866a; font-weight: 700; margin-top: 6px; }
.sold { color: #999; font-size: 12px; margin-top: 4px; }
</style>
