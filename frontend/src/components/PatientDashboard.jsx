import { useEffect, useState } from "react";
import { futureDateTime, minimumDateTime, request } from "../api";
import { Empty, Loader, Notice, Panel, Stat, Status } from "./Ui";

export default function PatientDashboard({ session, activePage }) {
  const [profile, setProfile] = useState(null);
  const [doctors, setDoctors] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [booking, setBooking] = useState({ doctorId: "", appointmentTime: futureDateTime() });
  const [editing, setEditing] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [prescriptionsLoading, setPrescriptionsLoading] = useState(false);
  const [feedback, setFeedback] = useState(null);

  async function refresh() {
    setLoading(true);
    setFeedback(null);
    const [profileResult, doctorsResult, appointmentResult, prescriptionResult] = await Promise.allSettled([
      request("/patients/me", { token: session.token }),
      request("/doctors", { token: session.token }),
      request("/appointments/my", { token: session.token }),
      request("/patients/my-prescriptions", { token: session.token })
    ]);
    if (profileResult.status === "fulfilled") setProfile(profileResult.value);
    if (doctorsResult.status === "fulfilled") {
      setDoctors(doctorsResult.value);
      if (!booking.doctorId && doctorsResult.value.length) {
        setBooking((current) => ({ ...current, doctorId: String(doctorsResult.value[0].userId) }));
      }
    }
    if (appointmentResult.status === "fulfilled") setAppointments(appointmentResult.value);
    if (prescriptionResult.status === "fulfilled") setPrescriptions(prescriptionResult.value);
    if (profileResult.status === "rejected") {
      setFeedback({ type: "info", text: "Your patient profile has not been created by an admin yet." });
    }
    setLoading(false);
  }

  useEffect(() => {
    refresh();
  }, []);

  useEffect(() => {
    if (activePage !== "My Prescriptions") return;
    loadPrescriptions();
    const timer = window.setInterval(loadPrescriptions, 8000);
    return () => window.clearInterval(timer);
  }, [activePage]);

  async function loadPrescriptions() {
    setPrescriptionsLoading(true);
    try {
      const result = await request("/patients/my-prescriptions", { token: session.token });
      setPrescriptions(result);
    } catch (error) {
      setFeedback({ type: "error", text: error.message });
    } finally {
      setPrescriptionsLoading(false);
    }
  }

  async function book(event) {
    event.preventDefault();
    setSaving(true);
    try {
      const saved = await request("/appointments", {
        token: session.token,
        method: "POST",
        body: {
          doctorId: Number(booking.doctorId),
          appointmentTime: booking.appointmentTime
        }
      });
      setFeedback({ type: "success", text: `Appointment #${saved.id} booked. Current status: PENDING.` });
      setBooking((current) => ({ ...current, appointmentTime: futureDateTime() }));
      const result = await request("/appointments/my", { token: session.token });
      setAppointments(result);
    } catch (error) {
      setFeedback({ type: "error", text: error.message });
    } finally {
      setSaving(false);
    }
  }

  function beginEdit(item) {
    setEditing({
      id: item.id,
      doctorId: String(item.doctorId),
      appointmentTime: String(item.appointmentTime).slice(0, 16)
    });
  }

  async function saveEdit(event) {
    event.preventDefault();
    setSaving(true);
    try {
      const saved = await request(`/appointments/${editing.id}`, {
        token: session.token,
        method: "PUT",
        body: {
          doctorId: Number(editing.doctorId),
          appointmentTime: editing.appointmentTime
        }
      });
      setFeedback({ type: "success", text: `Appointment #${saved.id} updated.` });
      setEditing(null);
      setAppointments(await request("/appointments/my", { token: session.token }));
    } catch (error) {
      setFeedback({ type: "error", text: error.message });
    } finally {
      setSaving(false);
    }
  }

  return (
    <main className="dashboard">
      <div className="dashboard-head">
        <div>
          <p className="kicker">Patient portal</p>
          <h1>{profile ? `Hello, ${profile.name}` : "Plan your care"}</h1>
          <p className="muted">Choose a doctor, request an appointment, and track its status.</p>
        </div>
        <button className="secondary" onClick={refresh}>Refresh</button>
      </div>
      <Notice type={feedback?.type}>{feedback?.text}</Notice>

      <div className="stats">
        <Stat label="Patient user ID" value={session.userId} />
        <Stat label="Available doctors" value={doctors.length} />
        <Stat label="My appointments" value={appointments.length} />
        <Stat label="Confirmed" value={appointments.filter((a) => a.status === "CONFIRMED").length} />
      </div>

      {activePage === "Doctors" && (
        <Panel className="page-panel" eyebrow="Directory" title="Available doctors">
          {loading ? <Loader /> : doctors.length === 0 ? <Empty>Admin must add a doctor profile first.</Empty> : (
            <div className="cards">
              {doctors.map((doctor) => (
                <article className="person-card doctor" key={doctor.id}>
                  <div className="avatar">Dr</div>
                  <strong>{doctor.name}</strong>
                  <span>{doctor.specialization}</span>
                  <small>User ID {doctor.userId}</small>
                </article>
              ))}
            </div>
          )}
        </Panel>
      )}

      {activePage === "My Appointments" && (
        <>
        <Panel className="page-panel" eyebrow="New visit" title="Book appointment">
          <form className="form-stack" onSubmit={book}>
            <label>
              Choose doctor
              <select required value={booking.doctorId} onChange={(e) => setBooking({ ...booking, doctorId: e.target.value })}>
                {doctors.length === 0 && <option value="">No doctors available</option>}
                {doctors.map((doctor) => (
                  <option value={doctor.userId} key={doctor.id}>{doctor.name} - {doctor.specialization}</option>
                ))}
              </select>
            </label>
            <label>
              Appointment date and time
              <input
                required
                type="datetime-local"
                min={minimumDateTime()}
                value={booking.appointmentTime}
                onChange={(e) => setBooking({ ...booking, appointmentTime: e.target.value })}
              />
            </label>
            <button className="primary" disabled={!doctors.length || saving} type="submit">
              {saving ? "Requesting..." : "Request appointment"}
            </button>
          </form>
        </Panel>

        {editing && (
          <Panel className="page-panel" eyebrow="Edit request" title={`Reschedule appointment #${editing.id}`}>
            <form className="form-stack" onSubmit={saveEdit}>
              <label>
                Choose doctor
                <select required value={editing.doctorId} onChange={(e) => setEditing({ ...editing, doctorId: e.target.value })}>
                  {doctors.map((doctor) => (
                    <option value={doctor.userId} key={doctor.id}>{doctor.name} - {doctor.specialization}</option>
                  ))}
                </select>
              </label>
              <label>
                Appointment date and time
                <input
                  required
                  type="datetime-local"
                  min={minimumDateTime()}
                  value={editing.appointmentTime}
                  onChange={(e) => setEditing({ ...editing, appointmentTime: e.target.value })}
                />
              </label>
              <div className="actions">
                <button className="primary" disabled={saving} type="submit">{saving ? "Saving..." : "Save changes"}</button>
                <button className="ghost" onClick={() => setEditing(null)} type="button">Cancel edit</button>
              </div>
            </form>
          </Panel>
        )}

      <Panel className="page-panel" eyebrow="Appointments" title="My visit history">
        {loading ? <Loader /> : appointments.length === 0 ? <Empty>No appointments found</Empty> : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Appointment</th><th>Doctor</th><th>Date and time</th><th>Status</th><th>Action</th></tr></thead>
              <tbody>
                {appointments.map((item) => (
                  <tr key={item.id}>
                    <td>#{item.id}</td>
                    <td>
                      <strong>{item.doctorName || `Doctor #${item.doctorId}`}</strong>
                      <small className="table-subtext">User ID {item.doctorId}</small>
                    </td>
                    <td>{new Date(item.appointmentTime).toLocaleString()}</td>
                    <td><Status value={item.status} /></td>
                    <td>
                      <button
                        className="mini"
                        disabled={item.status !== "PENDING"}
                        onClick={() => beginEdit(item)}
                        type="button"
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Panel>
        </>
      )}

      {activePage === "My Prescriptions" && (
        <Panel className="page-panel" eyebrow="Prescriptions" title="My prescriptions" actions={<button className="secondary" onClick={loadPrescriptions}>Refresh</button>}>
          {prescriptionsLoading ? <Loader /> : prescriptions.length === 0 ? <Empty>No prescriptions available</Empty> : (
            <div className="records prescriptions">
              {prescriptions.map((entry) => (
                <div className="record-block" key={entry.id}>
                  <div className="record-top">
                    <strong>Prescription #{entry.id}</strong>
                    <small>{new Date(entry.createdAt).toLocaleString()}</small>
                  </div>
                  <p>{entry.content}</p>
                  <small>Appointment #{entry.appointmentId} | Doctor User ID {entry.doctorId}</small>
                </div>
              ))}
            </div>
          )}
        </Panel>
      )}
    </main>
  );
}
