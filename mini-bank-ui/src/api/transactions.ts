import api from './client'
import type { Transaction, TransferResponse } from '../types'

type TransferPayload = {
  fromAccountNumber: string
  toAccountNumber: string
  amount: number
}

export const transactionsApi = {
  transfer: async (payload: TransferPayload) => {
    const { data } = await api.post<TransferResponse>('/api/transactions/transfer', payload)
    return data
  },
  history: async (accountId: string) => {
    const { data } = await api.get<Transaction[]>(`/api/transactions/account/${accountId}`)
    return data
  },
}
