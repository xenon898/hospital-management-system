import { useEffect, useState } from "react";
import AuthPage from "./components/AuthPage";
import AdminDashboard from "./components/AdminDashboard";
import DoctorDashboard from "./components/DoctorDashboard";
import PatientDashboard from "./components/PatientDashboard";

const SESSION_KEY = "hospital_session";

function readSession() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY)) || null;
  } catch {
    return null;
  }
}

export default function App() {
  const [session, setSession] = useState(readSession);
  const [activePage, setActivePage] = useState("");

  useEffect(() => {
    if (session) {
      localStorage.setItem(SESSION_KEY, JSON.stringify(session));
    } else {
      localStorage.removeItem(SESSION_KEY);
    }
  }, [session]);

  useEffect(() => {
    setActivePage("");
  }, [session?.role]);

  if (!session) {
    return <AuthPage onAuthenticated={setSession} />;
  }

  const navItems = {
    ADMIN: ["Create Accounts", "Doctors", "Patients", "Appointments"],
    DOCTOR: ["Appointments", "Prescription", "History"],
    PATIENT: ["Doctors", "My Appointments", "My Prescriptions"]
  };

  const currentNav = navItems[session.role];
  const selectedPage = activePage || currentNav[0];

  // Beginner note: sidebar clicks update this state; dashboards render only the selected section.
  function choosePage(page) {
    setActivePage(page);
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">+</span>
          <span>CareBridge</span>
        </div>
        <nav>
          {currentNav.map((item) => (
            <button
              className={`nav-item ${selectedPage === item ? "active" : ""}`}
              key={item}
              onClick={() => choosePage(item)}
              type="button"
            >
              {item}
            </button>
          ))}
        </nav>
      </aside>
      <section className="workspace">
        <header className="topbar">
          <div>
            <strong>{session.role.charAt(0) + session.role.slice(1).toLowerCase()} Dashboard</strong>
            <span className="user-code">User ID {session.userId}</span>
          </div>
          <div className="identity">
            <span className={`role-pill role-${session.role.toLowerCase()}`}>{session.role}</span>
            <button className="ghost" onClick={() => setSession(null)}>Sign out</button>
          </div>
        </header>
        {session.role === "ADMIN" && <AdminDashboard activePage={selectedPage} session={session} />}
        {session.role === "DOCTOR" && <DoctorDashboard activePage={selectedPage} session={session} />}
        {session.role === "PATIENT" && <PatientDashboard activePage={selectedPage} session={session} />}
      </section>
    </div>
  );
}
