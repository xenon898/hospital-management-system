export function Notice({ type = "info", children }) {
  if (!children) return null;
  return <div className={`notice notice-${type}`}>{children}</div>;
}

export function Panel({ title, eyebrow, actions, children, className = "" }) {
  return (
    <section className={`panel ${className}`}>
      <div className="panel-heading">
        <div>
          {eyebrow && <p className="eyebrow">{eyebrow}</p>}
          <h2>{title}</h2>
        </div>
        {actions}
      </div>
      {children}
    </section>
  );
}

export function Stat({ label, value }) {
  return (
    <div className="stat">
      <strong>{value}</strong>
      <span>{label}</span>
    </div>
  );
}

export function Status({ value }) {
  return <span className={`status status-${String(value).toLowerCase()}`}>{value}</span>;
}

export function Empty({ children }) {
  return <div className="empty">{children}</div>;
}

export function Loader() {
  return <p className="muted">Loading...</p>;
}
