import { create } from 'zustand'
import type { Transaction } from '../types'

type TransactionsState = {
  transactions: Transaction[]
  setTransactions: (transactions: Transaction[]) => void
  clearTransactions: () => void
}

export const useTransactionsStore = create<TransactionsState>((set) => ({
  transactions: [],
  setTransactions: (transactions) => set({ transactions }),
  clearTransactions: () => set({ transactions: [] }),
}))
