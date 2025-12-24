import api from './client'
import type { Account } from '../types'

type AccountCreatePayload = {
  number: string
  name: string
  initialBalance?: number
}

type AccountUpdatePayload = {
  number: string
  name: string
}

export const accountsApi = {
  list: async (params: { number?: string; name?: string } = {}) => {
    const { data } = await api.get<Account[]>('/api/accounts', { params })
    return data
  },
  getById: async (id: string) => {
    const { data } = await api.get<Account>(`/api/accounts/${id}`)
    return data
  },
  create: async (payload: AccountCreatePayload) => {
    const { data } = await api.post<Account>('/api/accounts', payload)
    return data
  },
  update: async (id: string, payload: AccountUpdatePayload) => {
    const { data } = await api.put<Account>(`/api/accounts/${id}`, payload)
    return data
  },
  remove: async (id: string) => {
    await api.delete(`/api/accounts/${id}`)
  },
}
