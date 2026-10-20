<template>
  <el-card shadow="never">
    <template #header><b>发布新帖</b></template>
    <el-form label-width="80px">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="输入标题..." />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="form.content" type="textarea" :rows="8" placeholder="分享你的零食心得..." />
      </el-form-item>
      <el-form-item label="图片">
        <el-upload :auto-upload="false" :limit="1" accept="image/*" list-type="picture-card"
                   :on-change="onFile" :on-remove="() => (file = null)">
          <span style="font-size: 26px;">+</span>
        </el-upload>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">发布</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPost } from '../api'

const router = useRouter()
const loading = ref(false)
const form = reactive({ title: '', content: '' })
let file = null

const onFile = (f) => { file = f.raw }

const submit = async () => {
  if (!form.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  loading.value = true
  try {
    const fd = new FormData()
    fd.append('title', form.title.trim())
    fd.append('content', form.content.trim())
    if (file) fd.append('file', file)
    const post = await createPost(fd)
    ElMessage.success('发布成功')
    router.push(`/community/post/${post.id}`)
  } finally {
    loading.value = false
  }
}
</script>
