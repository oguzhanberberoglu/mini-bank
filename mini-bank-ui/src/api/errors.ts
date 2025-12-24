import axios from 'axios'
import type { ErrorResponse } from '../types'

const formatFieldErrors = (fieldErrors?: ErrorResponse['fieldErrors']) => {
  if (!fieldErrors || fieldErrors.length === 0) {
    return null
  }
  return fieldErrors.map((item) => `${item.field}: ${item.message}`).join(', ')
}

export const getErrorMessage = (error: unknown, fallback = 'Something went wrong.') => {
  if (!axios.isAxiosError(error)) {
    return fallback
  }

  const data = error.response?.data
  if (!data) {
    return fallback
  }

  if (typeof data === 'string') {
    return data
  }

  const typed = data as Partial<ErrorResponse>
  const fieldDetails = formatFieldErrors(typed.fieldErrors)

  if (typed.message) {
    return fieldDetails ? `${typed.message}: ${fieldDetails}` : typed.message
  }

  if (typed.error) {
    return fieldDetails ? `${typed.error}: ${fieldDetails}` : typed.error
  }

  return fieldDetails ? `Validation error: ${fieldDetails}` : fallback
}
