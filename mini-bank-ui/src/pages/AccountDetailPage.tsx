import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { accountsApi } from '../api/accounts'
import { getErrorMessage } from '../api/errors'
import type { Account } from '../types'
import { formatCurrency, formatDateTime } from '../utils/format'
import { pushToast } from '../store/toastStore'
import { useAccountsStore } from '../store/accountsStore'

function AccountDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const upsertAccount = useAccountsStore((state) => state.upsertAccount)
  const removeAccount = useAccountsStore((state) => state.removeAccount)
  const [account, setAccount] = useState<Account | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [number, setNumber] = useState('')
  const [name, setName] = useState('')
  const [updateLoading, setUpdateLoading] = useState(false)
  const [updateError, setUpdateError] = useState('')

  useEffect(() => {
    if (!id) {
      return
    }
    const loadAccount = async () => {
      setLoading(true)
      setError('')
      try {
        const data = await accountsApi.getById(id)
        setAccount(data)
        setNumber(data.number)
        setName(data.name)
        upsertAccount(data)
      } catch (err) {
        const message = getErrorMessage(err, 'Account not found.')
        setError(message)
        pushToast(message)
      } finally {
        setLoading(false)
      }
    }
    loadAccount()
  }, [id])

  const handleUpdate = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!account) {
      return
    }
    setUpdateError('')
    setUpdateLoading(true)
    try {
      const updated = await accountsApi.update(account.id, { number, name })
      setAccount(updated)
      upsertAccount(updated)
    } catch (err) {
      const message = getErrorMessage(err, 'Update failed.')
      setUpdateError(message)
      pushToast(message)
    } finally {
      setUpdateLoading(false)
    }
  }

  const handleDelete = async () => {
    if (!account) {
      return
    }
    const confirmed = window.confirm(
      'Delete this account? Balance must be zero and this action cannot be undone.',
    )
    if (!confirmed) {
      return
    }
    try {
      await accountsApi.remove(account.id)
      removeAccount(account.id)
      navigate('/accounts')
    } catch (err) {
      const message = getErrorMessage(err, 'Delete failed.')
      setError(message)
      pushToast(message)
    }
  }

  if (loading) {
    return (
      <section className="page">
        <p className="muted">Loading account...</p>
      </section>
    )
  }

  if (!account) {
    return (
      <section className="page">
        <p className="error">{error || 'Account not found.'}</p>
        <Link className="link" to="/accounts">
          Back to accounts
        </Link>
      </section>
    )
  }

  return (
    <section className="page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Account detail</p>
          <h1>{account.name}</h1>
          <p className="muted">{account.number}</p>
        </div>
        <div className="header-actions">
          <Link className="btn btn-ghost" to={`/accounts/${account.id}/history`}>
            View history
          </Link>
          <Link className="btn btn-secondary" to={`/transfer?from=${account.number}`}>
            New transfer
          </Link>
        </div>
      </header>

      {error ? <p className="error">{error}</p> : null}

      <div className="grid two">
        <div className="panel">
          <h3>Balance snapshot</h3>
          <div className="stat">
            <span className="stat-label">Current balance</span>
            <span className="stat-value">{formatCurrency(account.balance)}</span>
          </div>
          <div className="stat-grid">
            <div>
              <p className="muted small">Created</p>
              <p>{formatDateTime(account.createdAt)}</p>
            </div>
            <div>
              <p className="muted small">Updated</p>
              <p>{formatDateTime(account.updatedAt)}</p>
            </div>
          </div>
        </div>

        <div className="panel">
          <h3>Update account</h3>
          <form className="form" onSubmit={handleUpdate}>
            <label className="field">
              <span>Account number</span>
              <input value={number} onChange={(event) => setNumber(event.target.value)} />
            </label>
            <label className="field">
              <span>Name</span>
              <input value={name} onChange={(event) => setName(event.target.value)} />
            </label>
            {updateError ? <p className="error">{updateError}</p> : null}
            <button className="btn btn-primary" type="submit" disabled={updateLoading}>
              {updateLoading ? 'Saving...' : 'Save changes'}
            </button>
          </form>
          <div className="danger-zone">
            <p className="muted small">Danger zone</p>
            <button
              className="btn btn-danger"
              onClick={handleDelete}
              disabled={Number(account.balance) !== 0}
            >
              Delete account
            </button>
            {Number(account.balance) !== 0 ? (
              <p className="muted small">Balance must be zero to delete.</p>
            ) : null}
          </div>
        </div>
      </div>
    </section>
  )
}

export default AccountDetailPage
