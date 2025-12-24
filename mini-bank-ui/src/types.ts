export type AuthResponse = {
  token: string
  tokenType: string
  expiresAt: string
}

export type Account = {
  id: string
  number: string
  name: string
  balance: string
  createdAt: string
  updatedAt: string
}

export type Transaction = {
  id: number
  fromAccountId: string
  fromAccountNumber: string
  toAccountId: string
  toAccountNumber: string
  amount: string
  transactionDate: string
  status: 'SUCCESS' | 'FAILED'
}

export type TransferResponse = {
  transactionId: number
  status: 'SUCCESS' | 'FAILED'
  message: string
  fromAccountId: string
  fromAccountNumber: string
  toAccountId: string
  toAccountNumber: string
  amount: string
  transactionDate: string
}

export type ErrorResponse = {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors?: Array<{ field: string; message: string }>
}
