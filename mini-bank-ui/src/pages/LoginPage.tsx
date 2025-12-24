import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { authApi } from '../api/auth'
import { getErrorMessage } from '../api/errors'
import { useAuthStore } from '../store/authStore'
import { pushToast } from '../store/toastStore'

function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const setToken = useAuthStore((state) => state.setToken)
  const navigate = useNavigate()
  const location = useLocation()
  const redirectTo = (location.state as { from?: string } | null)?.from || '/accounts'

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      const response = await authApi.login({ username, password })
      setToken(response.token)
      navigate(redirectTo, { replace: true })
    } catch (err) {
      const message = getErrorMessage(err, 'Login failed.')
      setError(message)
      pushToast(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h2>Welcome back</h2>
      <p className="muted">Sign in to manage accounts and transfers.</p>
      <form className="form" onSubmit={handleSubmit}>
        <label className="field">
          <span>Username</span>
          <input
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            placeholder="your.username"
            required
          />
        </label>
        <label className="field">
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="••••••••"
            required
          />
        </label>
        {error ? <p className="error">{error}</p> : null}
        <button className="btn btn-primary" type="submit" disabled={loading}>
          {loading ? 'Signing in...' : 'Sign in'}
        </button>
      </form>
      <p className="muted small">
        New here? <Link to="/register">Create an account</Link>
      </p>
    </div>
  )
}

export default LoginPage
