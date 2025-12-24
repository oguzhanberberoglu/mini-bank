import { create } from 'zustand'

export type ToastVariant = 'error' | 'success' | 'info'

export type Toast = {
  id: string
  message: string
  variant: ToastVariant
}

type ToastState = {
  toasts: Toast[]
  addToast: (message: string, variant?: ToastVariant) => string
  removeToast: (id: string) => void
}

const createId = () => {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

export const useToastStore = create<ToastState>((set, get) => ({
  toasts: [],
  addToast: (message, variant = 'error') => {
    const id = createId()
    set((state) => ({
      toasts: [...state.toasts, { id, message, variant }],
    }))
    window.setTimeout(() => {
      get().removeToast(id)
    }, 4500)
    return id
  },
  removeToast: (id) =>
    set((state) => ({
      toasts: state.toasts.filter((toast) => toast.id !== id),
    })),
}))

export const pushToast = (message: string, variant: ToastVariant = 'error') => {
  useToastStore.getState().addToast(message, variant)
}
