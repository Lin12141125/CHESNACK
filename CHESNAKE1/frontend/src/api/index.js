import http from './http'

// 认证
export const register = (data) => http.post('/api/auth/register', data)
export const login = (data) => http.post('/api/auth/login', data)
export const logout = () => http.post('/api/auth/logout')

// 商品
export const getGoodsList = (params) => http.get('/api/goods', { params })
export const getGoodsDetail = (id) => http.get(`/api/goods/${id}`)
export const getGoodsFilters = () => http.get('/api/goods/filters')
// 商品全文搜索（Elasticsearch，不可用自动降级）
export const searchGoods = (params) => http.get('/api/goods/search', { params })

// 购物车
export const getCart = () => http.get('/api/cart')
export const addCart = (data) => http.post('/api/cart', data)
export const updateCart = (id, amount) => http.put(`/api/cart/${id}`, null, { params: { amount } })
export const deleteCart = (id) => http.delete(`/api/cart/${id}`)

// 订单
export const pay = (goodIds) => http.post('/api/order/pay', { goodIds })
export const getOrders = (params) => http.get('/api/order', { params })

// 评价
export const getComments = (goodsId, params) => http.get(`/api/comment/goods/${goodsId}`, { params })
export const getCommentStats = (goodsId) => http.get(`/api/comment/stats/${goodsId}`)
export const addComment = (data) => http.post('/api/comment', data)
export const replyComment = (data) => http.post('/api/comment/reply', data)
export const reportComment = (id) => http.post(`/api/comment/report/${id}`)

// 社区
export const getCommunityList = (mode) => http.get('/api/community', { params: { mode } })
export const getCommunityDetail = (id) => http.get(`/api/community/${id}`)
export const createPost = (form) => http.post('/api/community', form, { headers: { 'Content-Type': 'multipart/form-data' } })
export const deletePost = (id) => http.delete(`/api/community/${id}`)
export const getCommunityComments = (id) => http.get(`/api/community/${id}/comments`)
export const addCommunityComment = (data) => http.post('/api/community/comment', data)

// 关注
export const follow = (username) => http.post('/api/follow', { username })
export const unfollow = (username) => http.delete(`/api/follow/${username}`)
export const getFollowList = () => http.get('/api/follow')

// 通知
export const getNotifications = (params) => http.get('/api/notification', { params })
export const getUnreadCount = () => http.get('/api/notification/unread')
export const markNotificationRead = (id) => http.put(`/api/notification/${id}/read`)

// 管理员
export const adminSaveGoods = (data) => (data.id ? http.put('/api/admin/goods', data) : http.post('/api/admin/goods', data))
export const adminDeleteGoods = (id) => http.delete(`/api/admin/goods/${id}`)
export const adminReports = () => http.get('/api/admin/reports')
export const adminDeleteReport = (id) => http.delete(`/api/admin/reports/${id}`)
export const adminLogs = (params) => http.get('/api/admin/logs', { params })

// 上传
export const uploadFile = (file) => {
  const form = new FormData()
  form.append('file', file)
  return http.post('/api/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}
