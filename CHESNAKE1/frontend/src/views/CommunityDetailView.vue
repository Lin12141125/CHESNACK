<template>
  <div v-if="post">
    <el-card shadow="never" class="post-card">
      <div class="author">
        <span class="avatar">{{ (post.username || '?').slice(0, 2) }}</span>
        <div>
          <b>{{ post.username }}</b>
          <p class="time">{{ fmt(post.sendTime) }}</p>
        </div>
      </div>
      <h2 class="title">{{ post.title }}</h2>
      <p class="content">{{ post.content }}</p>
      <img v-if="post.img1" :src="`/${post.img1}`" class="post-img" />
    </el-card>

    <el-card shadow="never" class="comment-card">
      <template #header><b>评论</b></template>
      <div class="input-row">
        <el-input v-model="commentText" placeholder="写下你的评论..." />
        <el-button type="primary" @click="submitComment">发表</el-button>
      </div>
      <el-empty v-if="comments.length === 0" description="暂无评论" />
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-head">
          <b>{{ c.username }}</b>
          <span class="time">{{ fmt(c.sendTime) }}</span>
          <el-button link type="primary" size="small" @click="openReply(c)">回复</el-button>
        </div>
        <p>{{ c.content }}</p>
        <div v-for="r in c.replies || []" :key="r.id" class="reply-item">
          <b>{{ r.username }}</b>：{{ r.content }}
        </div>
      </div>
    </el-card>

    <el-dialog v-model="replyVisible" title="回复评论" width="460px">
      <el-input v-model="replyText" type="textarea" :rows="3" />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCommunityDetail, getCommunityComments, addCommunityComment, deletePost } from '../api'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const post = ref(null)
const comments = ref([])
const commentText = ref('')
const replyText = ref('')
const replyVisible = ref(false)
let replyTarget = null

const fmt = (s) => (s || '').replace('T', ' ').slice(0, 16)

const load = async () => {
  post.value = await getCommunityDetail(route.params.id)
  comments.value = await getCommunityComments(route.params.id)
}

const submitComment = async () => {
  if (!commentText.value.trim()) return
  await addCommunityComment({ communityId: Number(route.params.id), content: commentText.value.trim() })
  ElMessage.success('评论成功')
  commentText.value = ''
  await load()
}

const openReply = (c) => { replyTarget = c; replyText.value = ''; replyVisible.value = true }

const submitReply = async () => {
  if (!replyText.value.trim()) return
  await addCommunityComment({ communityId: Number(route.params.id), pid: replyTarget.id, content: replyText.value.trim() })
  ElMessage.success('回复成功')
  replyVisible.value = false
  await load()
}

const removePost = async () => {
  await ElMessageBox.confirm('确定删除这篇帖子？', '提示', { type: 'warning' })
  await deletePost(route.params.id)
  ElMessage.success('已删除')
  router.push('/community')
}

onMounted(load)
</script>

<style scoped>
.post-card { margin-bottom: 16px; }
.author { display: flex; align-items: center; gap: 12px; }
.avatar { width: 44px; height: 44px; border-radius: 50%; background: #9f91c2; color: #fff; display: flex; align-items: center; justify-content: center; }
.time { color: #999; font-size: 12px; }
.title { margin: 14px 0 8px; }
.content { color: #555; line-height: 1.7; }
.post-img { max-width: 100%; max-height: 420px; border-radius: 10px; margin-top: 12px; }
.input-row { display: flex; gap: 12px; margin-bottom: 16px; }
.comment-item { padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.comment-head { display: flex; align-items: center; gap: 12px; }
.reply-item { margin-left: 32px; padding: 8px 12px; background: #faf7f2; border-radius: 8px; margin-top: 8px; font-size: 14px; }
</style>
