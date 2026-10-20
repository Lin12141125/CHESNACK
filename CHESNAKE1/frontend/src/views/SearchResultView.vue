<template>
  <div>
    <div class="filters">
      <el-form inline>
        <el-form-item label="品牌">
          <el-select v-model="query.brand" clearable placeholder="全部" style="width: 180px" @change="reload">
            <el-option v-for="b in filters.brands" :key="b" :label="b" :value="b" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" clearable placeholder="全部" style="width: 160px" @change="reload">
            <el-option v-for="t in filters.types" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="产地">
          <el-select v-model="query.origin" clearable placeholder="全部" style="width: 160px" @change="reload">
            <el-option v-for="o in filters.origins" :key="o" :label="o" :value="o" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-radio-group v-model="query.field" @change="reload">
            <el-radio-button value="score">综合</el-radio-button>
            <el-radio-button value="price">价格</el-radio-button>
            <el-radio-button value="sale_quantity">销量</el-radio-button>
          </el-radio-group>
          <el-radio-group v-model="query.direction" style="margin-left: 12px" @change="reload">
            <el-radio-button value="desc">降序</el-radio-button>
            <el-radio-button value="asc">升序</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="total === 0" class="empty">
      <el-empty description="没有找到相关商品" />
    </div>
    <div class="goods-grid">
      <el-card v-for="g in goods" :key="g.id" class="goods-card" shadow="hover"
               @click="$router.push(`/goods/${g.id}`)">
        <img :src="`/images/${g.img1 || '1.jpg'}`" class="goods-img" />
        <p class="name">{{ g.name }}</p>
        <p class="price">${{ g.price }}</p>
        <p class="sold">{{ g.saleQuantity }} copies sold</p>
      </el-card>
    </div>
    <el-pagination v-if="total > 0" v-model:current-page="query.page" :page-size="query.size"
                   :total="total" layout="prev, pager, next, total" class="pager" @current-change="reload" />
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getGoodsList, searchGoods, getGoodsFilters } from '../api'

const route = useRoute()
const goods = ref([])
const total = ref(0)
const filters = ref({ brands: [], types: [], origins: [] })
const query = reactive({
  keyword: route.query.keyword || '',
  brand: '', type: '', origin: '',
  field: 'score', direction: 'desc', page: 1, size: 20
})

watch(() => route.query.keyword, (v) => {
  query.keyword = v || ''
  query.page = 1
  reload()
})

// 纯关键词搜索走 Elasticsearch 全文搜索；带筛选条件走列表接口
const useEsSearch = () => query.keyword && !query.brand && !query.type && !query.origin

const reload = async () => {
  const page = useEsSearch()
    ? await searchGoods({ keyword: query.keyword, page: query.page, size: query.size, field: query.field, direction: query.direction })
    : await getGoodsList({ ...query })
  goods.value = page.records
  total.value = Number(page.total)
}

reload()
getGoodsFilters().then((r) => { filters.value = r })
</script>

<style scoped>
.filters { background: #fff; border-radius: 10px; padding: 16px; margin-bottom: 16px; }
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.goods-card { cursor: pointer; }
.goods-img { width: 100%; height: 180px; object-fit: cover; border-radius: 8px; }
.name { margin-top: 8px; font-weight: 600; font-size: 14px; height: 40px; overflow: hidden; }
.price { color: #f2866a; font-weight: 700; margin-top: 6px; }
.sold { color: #999; font-size: 12px; margin-top: 4px; }
.pager { margin-top: 20px; justify-content: center; }
.empty { padding: 60px 0; }
</style>
