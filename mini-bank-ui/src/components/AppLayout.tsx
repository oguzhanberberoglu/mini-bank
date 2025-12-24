import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

function AppLayout() {
  const clearToken = useAuthStore((state) => state.clearToken)
  const navigate = useNavigate()

  const handleLogout = () => {
    clearToken()
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">MB</div>
          <div>
            <p className="brand-kicker">Mini Bank</p>
            <h2 className="brand-title">Command Center</h2>
          </div>
        </div>
        <nav className="nav">
          <NavLink
            to="/accounts"
            className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
          >
            Accounts
          </NavLink>
          <NavLink
            to="/transfer"
            className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
          >
            Transfer
          </NavLink>
        </nav>
        <div className="sidebar-footer">
          <button className="btn btn-ghost" onClick={handleLogout}>
            Sign out
          </button>
        </div>
      </aside>
      <main className="main">
        <Outlet />
      </main>
    </div>
  )
}

export default AppLayout
