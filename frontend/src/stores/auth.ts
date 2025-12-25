import { defineStore } from 'pinia'
import { api } from '@/api/client'
import { authEnabled } from '@/config'

interface LoginResponse { token: string }
interface Profile { id: string; email: string; role?: string }

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    profile: null as Profile | null,
  }),
  getters: {
    isAuthenticated: (s) => !!s.token,
    isAdmin: (s) => s.profile?.role === 'ADMIN',
  },
  actions: {
    async login(email: string, password: string) {
      const { data } = await api.post<LoginResponse>('/auth/login', { email, password })
      this.token = data.token
      localStorage.setItem('token', this.token)
      if (authEnabled) {
        await this.fetchProfile()
      }
    },
    async register(email: string, password: string) {
      // Try to register; handle validation and conflict via ProblemDetails
      await api.post('/auth/register', { email, password })
      // Try auto-login when JWT mode is on; if 404 or 401, swallow and let caller redirect
      if (authEnabled) {
        try {
          const { data } = await api.post<LoginResponse>('/auth/login', { email, password })
          this.token = data.token
          localStorage.setItem('token', this.token)
          await this.fetchProfile()
        } catch (_) {
          // login may still fail; ignore
        }
      }
    },
    async fetchProfile() {
      if (!authEnabled || !this.token) { this.profile = null; return }
      try {
        const { data } = await api.get<Profile>('/profile')
        this.profile = data
      } catch {
        this.logout()
      }
    },
    logout() {
      this.token = ''
      this.profile = null
      localStorage.removeItem('token')
    },
  },
})
