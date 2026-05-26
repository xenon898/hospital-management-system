import { useEffect, useState } from "react";
import { request } from "../api";
import { Empty, Loader, Notice, Panel, Stat, Status } from "./Ui";

export default function DoctorDashboard({ session, activePage }) {
  const [profile, setProfile] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [prescription, setPrescription] = useState({ appointmentId: "", patientId: "", content: "" });
  const [historyId, setHistoryId] = useState("");
  const [history, setHistory] = useState([]);
  const [historyLoaded, setHistoryLoaded] = useState(false);
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState(null);

  async function refresh() {
    setLoading(true);
    const [profileResult, appointmentResult] = await Promise.allSettled([
      request("/doctors/me", { token: session.token }),
      request("/appointments/doctor", { token: session.token })
    ]);
    if (profileResult.status === "fulfilled") {
      setProfile(profileResult.value);
    } else {
      setFeedback({ type: "info", text: "Your doctor profile must be created by an admin before prescribing." });
    }
    if (appointmentResult.status === "fulfilled") setAppointments(appointmentResult.value);
    setLoading(false);
  }

  useEffect(() => {
    refresh();
  }, []);

  async function setStatus(appointmentId, status) {
    try {
      await request(`/appointments/${appointmentId}/status`, {
        token: session.token,
        method: "PATCH",
        body: { status }
      });
      setFeedback({ type: "success", text: `Appointment #${appointmentId} updated to ${status}.` });
      setAppointments(await request("/appointments/doctor", { token: session.token }));
    } catch (error) {
      setFeedback({ type: "error", text: error.message });
    }
  }

  async function savePrescription(event) {
    event.preventDefault();
    try {
      const saved = await request("/doctors/prescriptions", {
        token: session.token,
        method: "POST",
        body: {
          appointmentId: Number(prescription.appointmentId),
          patientId: Number(prescription.patientId),
          content: prescription.content
        }
      });
      setFeedback({ type: "success", text: `Prescription #${saved.id} recorded successfully.` });
      setPrescription({ appointmentId: "", patientId: "", content: "" });
    } catch (error) {
      setFeedback({ type: "error", text: error.message });
    }
  }

  async function searchHistory(event) {
    event.preventDefault();
    try {
      const records = await request(`/doctors/patient-history/${historyId}`, { token: session.token });
      setHistory(records);
      setHistoryLoaded(true);
      setFeedback(null);
    } catch (error) {
      setFeedback({ type: "error", text: error.message });
    }
  }

  return (
    <main className="dashboard">
      <div className="dashboard-head">
        <div>
          <p className="kicker">Doctor workspace</p>
          <h1>{profile ? profile.name : "Clinical dashboard"}</h1>
          <p className="muted">{profile ? profile.specialization : "Review appointments and prescriptions."}</p>
        </div>
        <button className="secondary" onClick={refresh}>Refresh appointments</button>
      </div>
      <Notice type={feedback?.type}>{feedback?.text}</Notice>

      <div className="stats">
        <Stat label="Doctor user ID" value={session.userId} />
        <Stat label="Assigned visits" value={appointments.length} />
        <Stat label="Pending" value={appointments.filter((a) => a.status === "PENDING").length} />
        <Stat label="Completed" value={appointments.filter((a) => a.status === "COMPLETED").length} />
      </div>

      {activePage === "Appointments" && (
      <Panel className="page-panel" eyebrow="Schedule" title="Assigned appointments">
        {loading ? <Loader /> : appointments.length === 0 ? <Empty>No appointments found</Empty> : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>ID</th><th>Patient User ID</th><th>Date and time</th><th>Status</th><th>Action</th></tr></thead>
              <tbody>
                {appointments.map((item) => (
                  <tr key={item.id}>
                    <td>#{item.id}</td>
                    <td>{item.patientId}</td>
                    <td>{new Date(item.appointmentTime).toLocaleString()}</td>
                    <td><Status value={item.status} /></td>
                    <td className="actions">
                      <button className="mini" onClick={() => setStatus(item.id, "CONFIRMED")}>Confirm</button>
                      <button className="mini complete" onClick={() => setStatus(item.id, "COMPLETED")}>Complete</button>
                      <button className="mini cancel" onClick={() => setStatus(item.id, "CANCELLED")}>Cancel</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Panel>
      )}

      {activePage === "Prescription" && (
        <Panel className="page-panel" eyebrow="Treatment" title="Write prescription">
          <p className="helper">Ask admin for the Patient Profile ID; it is different from the patient user ID shown above.</p>
          <form className="form-stack" onSubmit={savePrescription}>
            <label>
              Appointment ID
              <input required type="number" value={prescription.appointmentId} onChange={(e) => setPrescription({ ...prescription, appointmentId: e.target.value })} />
            </label>
            <label>
              Patient Profile ID
              <input required type="number" value={prescription.patientId} onChange={(e) => setPrescription({ ...prescription, patientId: e.target.value })} />
            </label>
            <label>
              Prescription content
              <textarea required value={prescription.content} onChange={(e) => setPrescription({ ...prescription, content: e.target.value })} placeholder="Medication and care instructions" rows="4" />
            </label>
            <button className="primary" type="submit">Save prescription</button>
          </form>
        </Panel>
      )}

      {activePage === "History" && (
        <Panel className="page-panel" eyebrow="History" title="Patient prescriptions">
          <form className="inline-form" onSubmit={searchHistory}>
            <input required type="number" value={historyId} onChange={(e) => setHistoryId(e.target.value)} placeholder="Patient Profile ID" />
            <button className="secondary" type="submit">Search</button>
          </form>
          {!historyLoaded ? (
            <Empty>Enter a Patient Profile ID to view medical advice history.</Empty>
          ) : history.length === 0 ? (
            <Empty>No prescriptions found for this patient.</Empty>
          ) : (
            <div className="records prescriptions">
              {history.map((entry) => (
                <div className="record-block" key={entry.id}>
                  <div className="record-top"><strong>Prescription #{entry.id}</strong><small>{new Date(entry.createdAt).toLocaleString()}</small></div>
                  <p>{entry.content}</p>
                  <small>Appointment #{entry.appointmentId}</small>
                </div>
              ))}
            </div>
          )}
        </Panel>
      )}
    </main>
  );
}
