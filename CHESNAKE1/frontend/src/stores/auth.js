import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('chesnake_token') || '',
    user: JSON.parse(localStorage.getItem('chesnake_user') || 'null')
  }),
  getters: {
    isAdmin: (s) => s.user && s.user.role === 1
  },
  actions: {
    setLogin({ token, ...user }) {
      this.token = token
      this.user = user
      localStorage.setItem('chesnake_token', token)
      localStorage.setItem('chesnake_user', JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('chesnake_token')
      localStorage.removeItem('chesnake_user')
    }
  }
})
