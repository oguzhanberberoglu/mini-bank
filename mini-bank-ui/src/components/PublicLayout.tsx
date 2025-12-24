import { Outlet } from "react-router-dom";

function PublicLayout() {
  return (
    <div className="public-layout">
      <div className="public-card">
        <div className="public-brand">
          <div>
            <p className="brand-kicker">Mini Bank Application</p>
            <h1> Oguzhan Berberoglu</h1>
            <p className="muted">created by</p>
            <p className="muted">Oredata Job Application 2025 December</p>
          </div>
        </div>
        <div className="public-panel">
          <Outlet />
        </div>
      </div>
    </div>
  );
}

export default PublicLayout;
