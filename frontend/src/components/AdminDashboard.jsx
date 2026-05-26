import { useEffect, useState } from "react";
import { request } from "../api";
import { Empty, Loader, Notice, Panel, Stat, Status } from "./Ui";

const blankDoctor = { username: "", password: "", name: "", specialization: "" };
const blankPatient = { username: "", password: "", name: "", age: "", phone: "" };

export default function AdminDashboard({ session, activePage }) {
  const [doctors, setDoctors] = useState([]);
  const [patients, setPatients] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [doctor, setDoctor] = useState(blankDoctor);
  const [patient, setPatient] = useState(blankPatient);
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState(null);

  async function refresh() {
    setLoading(true);
    try {
      const [doctorList, patientList, appointmentList] = await Promise.all([
        request("/doctors", { token: session.token }),
        request("/patients", { token: session.token }),
        request("/appointments", { token: session.token })
      ]);
      setDoctors(doctorList);
      setPatients(patientList);
      setAppointments(appointmentList);
    } catch (error) {
      setFeedback({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  async function createDoctor(event) {
    event.preventDefault();
    try {
      const saved = await request("/users/admin/create-doctor", {
        token: session.token,
        method: "POST",
        body: doctor
      });
      setDoctor(blankDoctor);
      setFeedback({
        type: "success",
        text: `Doctor created. User ID ${saved.userId}, Profile ID ${saved.profileId}.`
      });
      refresh();
    } catch (error) {
      setFeedback({ type: "error", text: error.message });
    }
  }

  async function createPatient(event) {
    event.preventDefault();
    try {
      const saved = await request("/users/admin/create-patient", {
        token: session.token,
        method: "POST",
        body: {
          ...patient,
          age: patient.age ? Number(patient.age) : null
        }
      });
      setPatient(blankPatient);
      setFeedback({
        type: "success",
        text: `Patient created. User ID ${saved.userId}, Profile ID ${saved.profileId}. Save the profile ID for prescriptions.`
      });
      refresh();
    } catch (error) {
      setFeedback({ type: "error", text: error.message });
    }
  }

  return (
    <main className="dashboard">
      <div className="dashboard-head">
        <div>
          <p className="kicker">Administration</p>
          <h1>Hospital overview</h1>
          <p className="muted">Create staff and patient accounts, then monitor appointment activity.</p>
        </div>
        <button className="secondary" onClick={refresh}>Refresh data</button>
      </div>
      <Notice type={feedback?.type}>{feedback?.text}</Notice>

      <div className="stats">
        <Stat label="Doctors" value={doctors.length} />
        <Stat label="Patients" value={patients.length} />
        <Stat label="Appointments" value={appointments.length} />
        <Stat label="Pending visits" value={appointments.filter((a) => a.status === "PENDING").length} />
      </div>

      {activePage === "Create Accounts" && (
      <div className="dashboard-grid forms page-panel">
        <Panel eyebrow="Setup" title="Create doctor">
          <p className="helper">Creates the login account and doctor profile in one admin-only step.</p>
          <form className="form-grid" onSubmit={createDoctor}>
            <label>
              Username
              <input required value={doctor.username} onChange={(e) => setDoctor({ ...doctor, username: e.target.value })} placeholder="doctor_01" />
            </label>
            <label>
              Password
              <input required type="password" value={doctor.password} onChange={(e) => setDoctor({ ...doctor, password: e.target.value })} placeholder="doctor123" />
            </label>
            <label>
              Full name
              <input required value={doctor.name} onChange={(e) => setDoctor({ ...doctor, name: e.target.value })} placeholder="Dr Sharma" />
            </label>
            <label className="span-two">
              Specialization
              <input required value={doctor.specialization} onChange={(e) => setDoctor({ ...doctor, specialization: e.target.value })} placeholder="Cardiology" />
            </label>
            <button className="primary span-two" type="submit">Create doctor account</button>
          </form>
        </Panel>

        <Panel eyebrow="Setup" title="Create patient">
          <p className="helper">Creates the patient login and hospital profile together.</p>
          <form className="form-grid" onSubmit={createPatient}>
            <label>
              Username
              <input required value={patient.username} onChange={(e) => setPatient({ ...patient, username: e.target.value })} placeholder="patient_01" />
            </label>
            <label>
              Password
              <input required type="password" value={patient.password} onChange={(e) => setPatient({ ...patient, password: e.target.value })} placeholder="patient123" />
            </label>
            <label>
              Full name
              <input required value={patient.name} onChange={(e) => setPatient({ ...patient, name: e.target.value })} placeholder="Rahul Kumar" />
            </label>
            <label>
              Age
              <input type="number" value={patient.age} onChange={(e) => setPatient({ ...patient, age: e.target.value })} />
            </label>
            <label>
              Phone
              <input value={patient.phone} onChange={(e) => setPatient({ ...patient, phone: e.target.value })} placeholder="9876543210" />
            </label>
            <button className="primary span-two" type="submit">Create patient account</button>
          </form>
        </Panel>
      </div>
      )}

      {activePage === "Doctors" && (
        <Panel className="page-panel" eyebrow="Directory" title="Doctors">
          {loading ? <Loader /> : doctors.length === 0 ? <Empty>No doctor profiles created yet.</Empty> : (
            <div className="cards">
              {doctors.map((item) => (
                <article className="person-card" key={item.id}>
                  <strong>{item.name}</strong>
                  <span>{item.specialization}</span>
                  <small>Doctor User ID: {item.userId}</small>
                </article>
              ))}
            </div>
          )}
        </Panel>
      )}

      {activePage === "Patients" && (
        <Panel className="page-panel" eyebrow="Records" title="Patients">
          {loading ? <Loader /> : patients.length === 0 ? <Empty>No patient profiles created yet.</Empty> : (
            <div className="records">
              {patients.map((item) => (
                <div className="record" key={item.id}>
                  <div><strong>{item.name}</strong><small>{item.phone || "No phone"} | Age {item.age || "-"}</small></div>
                  <span className="id-pill">Profile ID {item.id}</span>
                </div>
              ))}
            </div>
          )}
        </Panel>
      )}

      {activePage === "Appointments" && (
      <Panel className="page-panel" eyebrow="Activity" title="All appointments">
        {loading ? <Loader /> : appointments.length === 0 ? <Empty>No appointments have been booked.</Empty> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>ID</th><th>Doctor User ID</th><th>Patient User ID</th><th>Time</th><th>Status</th></tr>
              </thead>
              <tbody>
                {appointments.map((item) => (
                  <tr key={item.id}>
                    <td>#{item.id}</td>
                    <td>{item.doctorId}</td>
                    <td>{item.patientId}</td>
                    <td>{new Date(item.appointmentTime).toLocaleString()}</td>
                    <td><Status value={item.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Panel>
      )}
    </main>
  );
}
