import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { transactionsApi } from '../api/transactions'
import { getErrorMessage } from '../api/errors'
import type { TransferResponse } from '../types'
import { formatCurrency, formatDateTime } from '../utils/format'
import { pushToast } from '../store/toastStore'

function TransferPage() {
  const [searchParams] = useSearchParams()
  const [fromAccountNumber, setFromAccountNumber] = useState('')
  const [toAccountNumber, setToAccountNumber] = useState('')
  const [amount, setAmount] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState<TransferResponse | null>(null)

  useEffect(() => {
    const prefill = searchParams.get('from')
    if (prefill) {
      setFromAccountNumber(prefill)
    }
  }, [searchParams])

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError('')
    setResult(null)
    setLoading(true)
    try {
      const response = await transactionsApi.transfer({
        fromAccountNumber,
        toAccountNumber,
        amount: Number(amount),
      })
      setResult(response)
    } catch (err) {
      const message = getErrorMessage(err, 'Transfer failed.')
      setError(message)
      pushToast(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Move money</p>
          <h1>Transfer funds</h1>
          <p className="muted">Send money between accounts in real time.</p>
        </div>
      </header>

      <div className="grid two">
        <div className="panel">
          <h3>New transfer</h3>
          <form className="form" onSubmit={handleSubmit}>
            <label className="field">
              <span>From account number</span>
              <input
                value={fromAccountNumber}
                onChange={(event) => setFromAccountNumber(event.target.value)}
                placeholder="ACC-100"
                required
              />
            </label>
            <label className="field">
              <span>To account number</span>
              <input
                value={toAccountNumber}
                onChange={(event) => setToAccountNumber(event.target.value)}
                placeholder="ACC-200"
                required
              />
            </label>
            <label className="field">
              <span>Amount</span>
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
                placeholder="25.00"
                required
              />
            </label>
            {error ? <p className="error">{error}</p> : null}
            <button className="btn btn-primary" type="submit" disabled={loading}>
              {loading ? 'Processing...' : 'Send transfer'}
            </button>
          </form>
        </div>

        <div className="panel">
          <h3>Latest transfer</h3>
          {result ? (
            <div className="transfer-result">
              <div className={`status-pill ${result.status === 'SUCCESS' ? 'ok' : 'fail'}`}>
                {result.status}
              </div>
              <p className="result-message">{result.message}</p>
              <div className="result-grid">
                <div>
                  <p className="muted small">From</p>
                  <p>{result.fromAccountNumber}</p>
                </div>
                <div>
                  <p className="muted small">To</p>
                  <p>{result.toAccountNumber}</p>
                </div>
                <div>
                  <p className="muted small">Amount</p>
                  <p>{formatCurrency(result.amount)}</p>
                </div>
                <div>
                  <p className="muted small">Timestamp</p>
                  <p>{formatDateTime(result.transactionDate)}</p>
                </div>
              </div>
            </div>
          ) : (
            <p className="muted">Transfers will appear here after execution.</p>
          )}
        </div>
      </div>
    </section>
  )
}

export default TransferPage
