import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import router from '../router'

const http = axios.create({
  baseURL: '/',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    const data = res.data
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code === 200) return data.data
      if (data.code === 401) {
        const auth = useAuthStore()
        auth.logout()
        router.push('/login')
      }
      ElMessage.error(data.msg || '请求失败')
      return Promise.reject(new Error(data.msg || '请求失败'))
    }
    return data
  },
  (err) => {
    ElMessage.error(err.message || '网络异常')
    return Promise.reject(err)
  }
)

export default http
