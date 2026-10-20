<template>
  <div v-if="goods">
    <el-card class="detail-card" shadow="never">
      <div class="detail-top">
        <el-image :src="img1" fit="cover" class="big-img">
          <template #error><img src="/素材/limon chips.jpg" class="big-img" /></template>
        </el-image>
        <div class="info">
          <h1>{{ goods.name }}</h1>
          <p class="sold">{{ goods.saleQuantity }} copies sold</p>
          <p class="price">${{ goods.price }}</p>
          <p>发货地：{{ goods.origin }}</p>
          <p>品牌：{{ goods.brand }}</p>
          <p>库存：{{ goods.stock }}</p>
          <div class="amount">
            <el-input-number v-model="count" :min="1" :max="goods.stock" />
          </div>
          <div class="btns">
            <el-button type="warning" size="large" @click="onAddCart">加入购物车</el-button>
            <el-button type="danger" size="large" @click="onBuy">立即购买</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 评价 -->
    <el-card shadow="never" class="comment-card">
      <template #header><b>商品评价</b></template>
      <div class="stat-panel">
        <el-radio-group v-model="level" @change="loadComments">
          <el-radio-button v-for="s in stats" :key="s.level" :value="s.level">
            {{ s.label }} {{ s.count }}
          </el-radio-button>
        </el-radio-group>
      </div>
      <el-empty v-if="comments.length === 0" description="暂无评价" />
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-head">
          <b>{{ c.username }}</b>
          <span class="time">{{ fmt(c.sendTime) }}</span>
          <el-rate :model-value="c.level || 0" disabled />
        </div>
        <p class="content">{{ c.content }}</p>
        <div class="ops">
          <el-button link type="primary" size="small" @click="openReply(c)">回复</el-button>
          <el-button link type="warning" size="small" @click="onReport(c)">举报</el-button>
        </div>
        <div v-for="r in c.replies || []" :key="r.id" class="reply-item">
          <b>{{ r.username }}</b>：{{ r.content }}
          <span class="time">{{ fmt(r.sendTime) }}</span>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="replyVisible" title="回复评论" width="480px">
      <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="输入回复内容..." />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGoodsDetail, getComments, getCommentStats, addCart, pay, replyComment, reportComment } from '../api'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const goods = ref(null)
const count = ref(1)
const comments = ref([])
const stats = ref([])
const level = ref(1)
const replyVisible = ref(false)
const replyContent = ref('')
let replyTarget = null

const img1 = computed(() => `/images/${goods.value?.img1 || '1.jpg'}`)

const fmt = (s) => (s || '').replace('T', ' ').slice(0, 16)

const loadComments = async () => {
  comments.value = await getComments(id, { level: level.value })
}

onMounted(async () => {
  goods.value = await getGoodsDetail(id)
  stats.value = await getCommentStats(id)
  await loadComments()
})

const onAddCart = async () => {
  await addCart({ goodId: Number(id), amount: count.value })
  ElMessage.success('已加入购物车')
}

const onBuy = async () => {
  await addCart({ goodId: Number(id), amount: count.value })
  router.push({ path: '/cart', query: { pay: id } })
}

const openReply = (c) => { replyTarget = c; replyContent.value = ''; replyVisible.value = true }

const submitReply = async () => {
  if (!replyContent.value.trim()) return
  await replyComment({ goodsId: Number(id), commentId: replyTarget.id, content: replyContent.value.trim() })
  ElMessage.success('回复成功')
  replyVisible.value = false
  await loadComments()
}

const onReport = async (c) => {
  await reportComment(c.id)
  ElMessage.success('举报成功，管理员将审核处理')
}
</script>

<style scoped>
.detail-card { margin-bottom: 16px; }
.detail-top { display: flex; gap: 32px; }
.big-img { width: 400px; height: 400px; border-radius: 10px; }
.info h1 { font-size: 22px; margin: 8px 0 12px; }
.sold { color: #999; }
.price { color: #f2866a; font-size: 30px; font-weight: 700; margin: 10px 0; }
.info p { margin: 6px 0; }
.amount { margin: 16px 0; }
.btns { display: flex; gap: 12px; }
.comment-card { margin-bottom: 16px; }
.stat-panel { margin-bottom: 16px; }
.comment-item { padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.comment-head { display: flex; align-items: center; gap: 12px; }
.time { color: #999; font-size: 12px; }
.content { margin: 8px 0; }
.reply-item { margin-left: 32px; padding: 8px 12px; background: #faf7f2; border-radius: 8px; margin-top: 8px; font-size: 14px; }
</style>
