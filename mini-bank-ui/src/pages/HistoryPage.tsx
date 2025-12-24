import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { accountsApi } from '../api/accounts'
import { getErrorMessage } from '../api/errors'
import { transactionsApi } from '../api/transactions'
import type { Account } from '../types'
import { formatCurrency, formatDateTime } from '../utils/format'
import { pushToast } from '../store/toastStore'
import { useTransactionsStore } from '../store/transactionsStore'
import { useAccountsStore } from '../store/accountsStore'

function HistoryPage() {
  const { id } = useParams()
  const [account, setAccount] = useState<Account | null>(null)
  const transactions = useTransactionsStore((state) => state.transactions)
  const setTransactions = useTransactionsStore((state) => state.setTransactions)
  const upsertAccount = useAccountsStore((state) => state.upsertAccount)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!id) {
      return
    }
    const loadData = async () => {
      setLoading(true)
      setError('')
      try {
        const [accountData, historyData] = await Promise.all([
          accountsApi.getById(id),
          transactionsApi.history(id),
        ])
        setAccount(accountData)
        setTransactions(historyData)
        upsertAccount(accountData)
      } catch (err) {
        const message = getErrorMessage(err, 'Could not load transaction history.')
        setError(message)
        pushToast(message)
      } finally {
        setLoading(false)
      }
    }
    loadData()
  }, [id])

  return (
    <section className="page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Ledger</p>
          <h1>Transaction history</h1>
          <p className="muted">
            {account ? `${account.name} • ${account.number}` : 'Account activity'}
          </p>
        </div>
        <div className="header-actions">
          {account ? (
            <Link className="btn btn-ghost" to={`/accounts/${account.id}`}>
              Back to account
            </Link>
          ) : null}
        </div>
      </header>

      {error ? <p className="error">{error}</p> : null}
      {loading ? (
        <p className="muted">Loading history...</p>
      ) : (
        <div className="panel">
          {transactions.length === 0 ? (
            <p className="muted">No transfers yet for this account.</p>
          ) : (
            <div className="table">
              <div className="table-row head">
                <span>Status</span>
                <span>From</span>
                <span>To</span>
                <span>Amount</span>
                <span>Time</span>
              </div>
              {transactions.map((transaction) => (
                <div className="table-row" key={transaction.id}>
                  <span className={`status-pill ${transaction.status === 'SUCCESS' ? 'ok' : 'fail'}`}>
                    {transaction.status}
                  </span>
                  <span>{transaction.fromAccountNumber}</span>
                  <span>{transaction.toAccountNumber}</span>
                  <span>{formatCurrency(transaction.amount)}</span>
                  <span>{formatDateTime(transaction.transactionDate)}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </section>
  )
}

export default HistoryPage
