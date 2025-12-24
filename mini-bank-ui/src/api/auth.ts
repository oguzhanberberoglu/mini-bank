import api from './client'
import type { AuthResponse } from '../types'

type RegisterPayload = {
  username: string
  email: string
  password: string
}

type LoginPayload = {
  username: string
  password: string
}

export const authApi = {
  register: async (payload: RegisterPayload) => {
    const { data } = await api.post('/api/users/register', payload)
    return data
  },
  login: async (payload: LoginPayload) => {
    const { data } = await api.post<AuthResponse>('/api/users/login', payload)
    return data
  },
}
