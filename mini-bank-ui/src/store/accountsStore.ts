import { create } from 'zustand'
import type { Account } from '../types'

type AccountsState = {
  accounts: Account[]
  setAccounts: (accounts: Account[]) => void
  upsertAccount: (account: Account) => void
  removeAccount: (id: string) => void
  clearAccounts: () => void
}

export const useAccountsStore = create<AccountsState>((set) => ({
  accounts: [],
  setAccounts: (accounts) => set({ accounts }),
  upsertAccount: (account) =>
    set((state) => {
      const existingIndex = state.accounts.findIndex((item) => item.id === account.id)
      if (existingIndex === -1) {
        return { accounts: [...state.accounts, account] }
      }
      const next = [...state.accounts]
      next[existingIndex] = account
      return { accounts: next }
    }),
  removeAccount: (id) =>
    set((state) => ({
      accounts: state.accounts.filter((account) => account.id !== id),
    })),
  clearAccounts: () => set({ accounts: [] }),
}))
