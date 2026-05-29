import { useState } from "react";
import { request } from "../api";
import { Notice } from "./Ui";

export default function AuthPage({ onAuthenticated }) {
  const [login, setLogin] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const data = await request("/users/login", {
        method: "POST",
        body: login
      });
      onAuthenticated(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="auth-shell">
      <section className="hero">
        <div className="brand">
          <span className="brand-mark">+</span>
          <span>CareBridge Hospital</span>
        </div>
        <p className="kicker">Hospital Management System</p>
        <h1>Care coordinated in one calm workspace.</h1>
        <p className="hero-copy">
          Admins prepare profiles, patients book visits, and doctors confirm
          care and add prescriptions through one connected workflow.
        </p>
        <div className="hero-metrics">
          <span>Secure roles</span>
          <span>Owned records</span>
          <span>Validated workflow</span>
        </div>
      </section>

      <section className="auth-card">
        <h2>Welcome back</h2>
        <p className="muted">Use your hospital account to continue. New doctor and patient accounts are created by Admin.</p>
        <Notice type="error">{error}</Notice>
        <form className="form-stack" onSubmit={submit}>
          <label>
            Username
            <input
              required
              value={login.username}
              onChange={(event) => setLogin({ ...login, username: event.target.value })}
              placeholder="Enter username"
            />
          </label>
          <label>
            Password
            <input
              required
              type="password"
              value={login.password}
              onChange={(event) => setLogin({ ...login, password: event.target.value })}
              placeholder="Enter password"
            />
          </label>
          <button className="primary" disabled={busy} type="submit">
            {busy ? "Please wait..." : "Sign in"}
          </button>
        </form>
        <p className="helper">Default demo admin: admin / admin123</p>
      </section>
    </main>
  );
}
