import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const base = import.meta.env.VITE_API_BASE_URL || '/api'

export const api = axios.create({ baseURL: base })

api.interceptors.request.use((config) => {
  try {
    // Pinia store can be used here at runtime
    const auth = useAuthStore()
    if (auth.token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${auth.token}`
    }
  } catch {}
  return config
})

api.interceptors.response.use(
  (r) => r,
  (error) => Promise.reject(error)
)
