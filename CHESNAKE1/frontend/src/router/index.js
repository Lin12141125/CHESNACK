import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
  { path: '/register', component: () => import('../views/RegisterView.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../layout/UserLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('../views/HomeView.vue') },
      { path: 'search', component: () => import('../views/SearchResultView.vue') },
      { path: 'goods/:id', component: () => import('../views/GoodsDetailView.vue') },
      { path: 'cart', component: () => import('../views/CartView.vue') },
      { path: 'orders', component: () => import('../views/OrderView.vue') },
      { path: 'notifications', component: () => import('../views/NotificationView.vue') },
      { path: 'community', component: () => import('../views/CommunityView.vue') },
      { path: 'community/post/:id', component: () => import('../views/CommunityDetailView.vue') },
      { path: 'community/upload', component: () => import('../views/CommunityUploadView.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layout/AdminLayout.vue'),
    meta: { role: 1 },
    children: [
      { path: '', name: 'admin-dashboard', component: () => import('../views/admin/AdminDashboardView.vue') },
      { path: 'warehouse', component: () => import('../views/admin/AdminWarehouseView.vue') },
      { path: 'reports', component: () => import('../views/admin/AdminReportView.vue') },
      { path: 'logs', component: () => import('../views/admin/AdminLogsView.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.role === 1 && auth.user?.role !== 1) {
    return '/'
  }
  return true
})

export default router
