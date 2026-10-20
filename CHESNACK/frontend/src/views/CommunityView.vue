<template>
  <div>
    <div class="head">
      <el-radio-group v-model="mode" @change="load">
        <el-radio-button value="recommend">推荐</el-radio-button>
        <el-radio-button value="follow">关注</el-radio-button>
        <el-radio-button value="personal">我的</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="$router.push('/community/upload')">+ 发帖</el-button>
    </div>
    <el-empty v-if="posts.length === 0" description="还没有帖子，来发第一篇吧" />
    <div class="post-grid">
      <el-card v-for="p in posts" :key="p.id" class="post-card" shadow="hover">
        <img :src="img(p.img1)" class="post-img" @click="$router.push(`/community/post/${p.id}`)" />
        <p class="title" @click="$router.push(`/community/post/${p.id}`)">{{ p.title }}</p>
        <div class="meta">
          <span>@{{ p.username }}</span>
          <span class="time">{{ fmt(p.sendTime) }}</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCommunityList } from '../api'

const mode = ref('recommend')
const posts = ref([])
const img = (i) => i ? `/${i}` : '/素材/limon chips.jpg'
const fmt = (s) => (s || '').replace('T', ' ').slice(0, 16)
const load = async () => { posts.value = await getCommunityList(mode.value) }
onMounted(load)
</script>

<style scoped>
.head { display: flex; justify-content: space-between; margin-bottom: 16px; }
.post-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}
.post-img { width: 100%; height: 180px; object-fit: cover; border-radius: 8px; cursor: pointer; }
.title { margin-top: 8px; font-weight: 600; cursor: pointer; height: 42px; overflow: hidden; }
.meta { display: flex; justify-content: space-between; color: #999; font-size: 12px; margin-top: 8px; }
</style>
