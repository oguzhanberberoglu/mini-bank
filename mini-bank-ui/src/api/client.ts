import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('mini-bank-auth')
  if (token) {
    try {
      const parsed = JSON.parse(token) as { state?: { token?: string } }
      const accessToken = parsed.state?.token
      if (accessToken) {
        config.headers.Authorization = `Bearer ${accessToken}`
      }
    } catch {
      localStorage.removeItem('mini-bank-auth')
    }
  }
  return config
})

export default api
