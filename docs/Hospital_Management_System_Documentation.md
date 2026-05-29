# Hospital Management System Documentation

## 1. Project Summary

CareBridge Hospital Management System is a React + Vite frontend with Spring Boot microservices, PostgreSQL databases, JWT authentication, and an API Gateway. The project now behaves as a role-protected hospital workflow instead of a simple CRUD demo.

Core workflow:

```text
Admin creates doctor and patient
        |
Patient books appointment
        |
Patient may edit only while appointment is PENDING
        |
Doctor sees only assigned appointments
        |
Doctor confirms and completes appointment
        |
Doctor creates prescription for that completed appointment
        |
Patient sees only their own prescription
        |
Admin monitors all doctors, patients, and appointments
```

## 2. Root Cause Analysis

Before the enterprise upgrade, the main issues were:

- Appointment updates accepted an appointment ID without checking whether the logged-in doctor owned that appointment.
- Appointment booking did not check doctor slot conflicts, duplicate patient bookings, or invalid business state changes.
- Prescription creation trusted frontend-provided `patientId`, so the wrong patient could receive a prescription if the ID was entered incorrectly.
- Appointment data used patient user IDs, while prescription flow expected patient profile IDs. That mismatch caused synchronization and mapping bugs.
- Patient phone numbers were optional and weakly validated, allowing repeated digits, unrealistic numbers, and duplicates.
- Some APIs returned inconsistent error shapes and relied on broad `IllegalArgumentException` handling.
- Frontend prescription entry required manual patient profile IDs, creating preventable user error.
- Patient appointment editing was missing.
- UI navigation worked by selected section state, but workflows had weak loading, disabled, confirmation, and validation behavior.

## 3. Architecture

```text
React Frontend (Vite, port 3000)
        |
        v
API Gateway (8080)
JWT validation, role checks, proxy routing
        |
        +--> User Service (8081)         -> user_db
        +--> Doctor Service (8082)       -> doctor_db
        +--> Patient Service (8083)      -> patient_db
        +--> Appointment Service (8084)  -> appointment_db
```

All browser and Postman calls should use:

```text
http://localhost:8080/api
```

Service ownership model:

- User IDs are the security identity from JWT `sub`.
- Appointment `doctorId` is the doctor user ID.
- Appointment `patientId` is the patient user ID.
- Prescription `doctorId` is the doctor user ID.
- Prescription `patientId` is the patient user ID.
- Profile IDs still exist for profile tables, but they are no longer required for prescribing.

## 4. Services

| Service | Port | Responsibility |
|---|---:|---|
| API Gateway | 8080 | Validates JWT roles and forwards API calls |
| User Service | 8081 | Login, admin bootstrap, doctor/patient account creation |
| Doctor Service | 8082 | Doctor profiles, prescription ownership, patient prescription history |
| Patient Service | 8083 | Patient profiles, phone validation, patient prescription aggregation |
| Appointment Service | 8084 | Booking, rescheduling, status workflow, slot conflict checks |

## 5. Security Rules

Only this endpoint is public:

```http
POST /api/users/login
```

JWT-protected requests must include:

```http
Authorization: Bearer <token>
```

Role protection:

| Capability | Admin | Doctor | Patient |
|---|:---:|:---:|:---:|
| Create doctor account/profile | Yes | No | No |
| Create patient account/profile | Yes | No | No |
| List doctors | Yes | Yes | Yes |
| List patients | Yes | No | No |
| Book appointment | No | No | Yes |
| Edit own pending appointment | No | No | Yes |
| View own appointments | No | No | Yes |
| View assigned appointments | No | Yes | No |
| Update assigned appointment status | No | Yes | No |
| Create prescription for owned completed appointment | No | Yes | No |
| View own prescriptions | No | No | Yes |
| Monitor all appointments | Yes | No | No |

Service-level ownership checks:

- Patients can only list and edit appointments where `appointment.patientId == JWT subject`.
- Doctors can only list and update appointments where `appointment.doctorId == JWT subject`.
- Admin can list all appointments.
- Doctors can create prescriptions only for appointments assigned to them.
- Patients can retrieve prescriptions only for their own user ID.

## 6. Validation Rules

General validation:

- Required fields use `@NotBlank` and `@NotNull`.
- String length checks use `@Size`.
- Age uses `@Min(0)`.
- Appointment dates use `@Future`.
- Invalid state changes return clean HTTP errors.
- Duplicate and conflict scenarios return `409 CONFLICT`.
- Ownership violations return `403 FORBIDDEN`.
- Missing records return `404 NOT FOUND`.

Phone validation:

- Exactly 10 digits.
- Must start with `6`, `7`, `8`, or `9`.
- Repeated digits like `9999999999` and `1111111111` are rejected.
- `1234567890` is rejected.
- Phone number must be unique across patient profiles.
- Frontend validates in real time before submission.

Appointment validation:

- Appointment time must be in the future.
- Doctor ID must be valid and positive.
- Doctor cannot have another non-cancelled appointment in the same 30-minute slot window.
- Duplicate patient + doctor + exact appointment time is rejected.
- Patients can edit only `PENDING` appointments.
- Doctors can change status only for their assigned appointments.

Allowed status transitions:

```text
PENDING   -> CONFIRMED or CANCELLED
CONFIRMED -> COMPLETED or CANCELLED
COMPLETED -> no further transition
CANCELLED -> no further transition
```

Prescription validation:

- Prescription content is required.
- Prescription content must be 5 to 2000 characters.
- Appointment must exist.
- Appointment must belong to the logged-in doctor.
- Appointment must be `COMPLETED`.
- Only one prescription is allowed per appointment.
- Patient ID is resolved from the appointment service, not trusted from the frontend.

## 7. Database Entities

### `user_db.app_users`

| Field | Purpose |
|---|---|
| `id` | User security identity |
| `username` | Unique login username |
| `password_hash` | BCrypt password hash |
| `role` | `ADMIN`, `DOCTOR`, or `PATIENT` |
| `created_at` | Audit timestamp |
| `updated_at` | Audit timestamp |

Indexes:

- `idx_app_users_username`
- `idx_app_users_role`

### `patient_db.patient_profiles`

| Field | Purpose |
|---|---|
| `id` | Patient profile ID |
| `user_id` | Linked user-service ID |
| `name` | Patient full name |
| `age` | Optional age |
| `phone` | Unique validated mobile number |
| `created_at` | Audit timestamp |
| `updated_at` | Audit timestamp |

Constraints and indexes:

- Unique `user_id`
- Unique `phone`
- `idx_patient_user_id`
- `idx_patient_phone`

### `doctor_db.doctor_profiles`

| Field | Purpose |
|---|---|
| `id` | Doctor profile ID |
| `user_id` | Linked user-service ID |
| `name` | Doctor full name |
| `specialization` | Doctor specialization |
| `created_at` | Audit timestamp |
| `updated_at` | Audit timestamp |

Constraints and indexes:

- Unique `user_id`
- `idx_doctor_user_id`

### `doctor_db.prescriptions`

| Field | Purpose |
|---|---|
| `id` | Prescription ID |
| `appointment_id` | Linked appointment ID |
| `patient_id` | Patient user ID |
| `doctor_id` | Doctor user ID |
| `content` | Prescription content |
| `created_at` | Audit timestamp |
| `updated_at` | Audit timestamp |

Constraints and indexes:

- Unique `appointment_id`
- `idx_prescriptions_patient`
- `idx_prescriptions_doctor`
- `idx_prescriptions_appointment`

### `appointment_db.appointments`

| Field | Purpose |
|---|---|
| `id` | Appointment ID |
| `doctor_id` | Doctor user ID |
| `patient_id` | Patient user ID |
| `appointment_time` | Requested appointment date and time |
| `status` | Appointment lifecycle status |
| `created_at` | Audit timestamp |
| `updated_at` | Audit timestamp |

Indexes:

- `idx_appointments_patient_time`
- `idx_appointments_doctor_time`
- `idx_appointments_status`

## 8. API Reference

Base URL:

```text
http://localhost:8080/api
```

### User APIs

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/users/login` | Public | Login and receive JWT |
| `GET` | `/users/me` | Authenticated | View token identity |
| `POST` | `/users/admin/create-doctor` | Admin | Create doctor user and profile |
| `POST` | `/users/admin/create-patient` | Admin | Create patient user and profile |

### Doctor APIs

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/doctors` | Authenticated | List doctors |
| `GET` | `/doctors/me` | Doctor | Get own doctor profile |
| `POST` | `/doctors/prescriptions` | Doctor | Create prescription for completed assigned appointment |
| `GET` | `/doctors/patient-history/{patientUserId}` | Doctor | View prescriptions written by this doctor for that patient |

### Patient APIs

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/patients` | Admin | List patients |
| `GET` | `/patients/me` | Patient | Get own patient profile |
| `GET` | `/patients/my-prescriptions` | Patient | Get own prescriptions |

### Appointment APIs

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/appointments` | Patient | Book appointment |
| `PUT` | `/appointments/{appointmentId}` | Patient | Edit own pending appointment |
| `GET` | `/appointments/my` | Patient | List own appointments |
| `GET` | `/appointments/doctor` | Doctor | List assigned appointments |
| `GET` | `/appointments/{appointmentId}` | Patient/Doctor/Admin | Owned appointment lookup |
| `PATCH` | `/appointments/{appointmentId}/status` | Doctor | Update assigned appointment status |
| `GET` | `/appointments` | Admin | List all appointments |

## 9. Frontend Workflow

Admin dashboard:

- Creates doctor accounts and profiles in one step.
- Creates patient accounts and profiles in one step.
- Validates patient phone before request.
- Disables buttons during API calls.
- Shows doctors, patients, and all appointments.

Patient dashboard:

- Lists doctors.
- Books future appointments.
- Edits pending appointments only.
- Shows own appointments from `/appointments/my`.
- Polls and refreshes own prescriptions from `/patients/my-prescriptions`.

Doctor dashboard:

- Lists only assigned appointments from `/appointments/doctor`.
- Uses confirmation dialogs before status changes.
- Enables legal status action buttons only.
- Writes prescriptions by selecting a completed appointment.
- Does not ask for patient profile ID.
- Searches patient history by patient user ID and returns only prescriptions created by that doctor.

UX cleanup:

- Removed numeric Step 1 / Step 2 / Step 3 hero cards.
- Replaced them with concise enterprise capability indicators.
- Added professional panels, tables, empty states, disabled states, and responsive layout behavior.
- Reduced rounded card styling for a more operational dashboard feel.

## 10. Developer File Map

Appointment ownership and workflow:

- `backend/appointment-service/src/main/java/com/hospital/appointments/service/impl/AppointmentServiceImpl.java`
- `backend/appointment-service/src/main/java/com/hospital/appointments/controller/AppointmentController.java`
- `backend/appointment-service/src/main/java/com/hospital/appointments/dto/AppointmentUpdateRequest.java`
- `backend/appointment-service/src/main/java/com/hospital/appointments/repository/AppointmentRepository.java`
- `backend/appointment-service/src/main/java/com/hospital/appointments/security/SecurityConfig.java`

Prescription synchronization:

- `backend/doctor-service/src/main/java/com/hospital/doctors/service/impl/DoctorServiceImpl.java`
- `backend/doctor-service/src/main/java/com/hospital/doctors/controller/DoctorController.java`
- `backend/doctor-service/src/main/java/com/hospital/doctors/dto/AppointmentDto.java`
- `backend/doctor-service/src/main/java/com/hospital/doctors/dto/PrescriptionCreateRequest.java`
- `backend/patient-service/src/main/java/com/hospital/patients/service/impl/PatientServiceImpl.java`

Phone validation:

- `backend/patient-service/src/main/java/com/hospital/patients/validation/PhoneNumberValidator.java`
- `backend/patient-service/src/main/java/com/hospital/patients/dto/PatientProfileDto.java`
- `backend/user-service/src/main/java/com/hospital/users/dto/AdminCreatePatientRequest.java`
- `frontend/src/api.js`
- `frontend/src/components/AdminDashboard.jsx`

Security and error handling:

- `backend/api-gateway/src/main/java/com/hospital/gateway/security/SecurityConfig.java`
- `backend/user-service/src/main/java/com/hospital/users/controller/GlobalExceptionHandler.java`
- `backend/patient-service/src/main/java/com/hospital/patients/controller/GlobalExceptionHandler.java`
- `backend/doctor-service/src/main/java/com/hospital/doctors/controller/GlobalExceptionHandler.java`
- `backend/appointment-service/src/main/java/com/hospital/appointments/controller/GlobalExceptionHandler.java`

Frontend dashboards:

- `frontend/src/components/AdminDashboard.jsx`
- `frontend/src/components/PatientDashboard.jsx`
- `frontend/src/components/DoctorDashboard.jsx`
- `frontend/src/components/AuthPage.jsx`
- `frontend/src/components/Ui.jsx`
- `frontend/src/styles.css`

## 11. Maintenance Notes

- Keep user ID as the JWT identity source across services.
- Do not accept patient or doctor ownership from frontend form fields when it can be derived from JWT or appointment lookup.
- Add new appointment states only after updating `validateStatusTransition`.
- Add new prescription rules in `DoctorServiceImpl.addPrescription`.
- Keep gateway security and downstream service security aligned.
- Use `ResponseStatusException` for precise HTTP status responses.
- Keep frontend validation as usability support; backend validation remains authoritative.

## 12. Build and Run

Backend build:

```powershell
cd C:\Users\Mrigank\Desktop\hosptal\backend
..\.tools\apache-maven-3.9.9\bin\mvn.cmd -q -pl appointment-service,doctor-service,patient-service,user-service,api-gateway -am test
```

Frontend build:

```powershell
cd C:\Users\Mrigank\Desktop\hosptal\frontend
$env:PATH='C:\Users\Mrigank\Desktop\hosptal\.tools\node-v20.18.0-win-x64;' + $env:PATH
..\.tools\node-v20.18.0-win-x64\npm.cmd run build
```

Start services:

```powershell
java -jar backend\user-service\target\user-service-1.0.0-SNAPSHOT.jar
java -jar backend\doctor-service\target\doctor-service-1.0.0-SNAPSHOT.jar
java -jar backend\patient-service\target\patient-service-1.0.0-SNAPSHOT.jar
java -jar backend\appointment-service\target\appointment-service-1.0.0-SNAPSHOT.jar
java -jar backend\api-gateway\target\api-gateway-1.0.0-SNAPSHOT.jar
```

Start frontend:

```powershell
cd C:\Users\Mrigank\Desktop\hosptal\frontend
$env:PATH='C:\Users\Mrigank\Desktop\hosptal\.tools\node-v20.18.0-win-x64;' + $env:PATH
..\.tools\node-v20.18.0-win-x64\npm.cmd run dev
```

Open:

```text
http://localhost:3000
```

## 13. Verification Completed

The upgraded code was verified with:

```powershell
..\.tools\apache-maven-3.9.9\bin\mvn.cmd -q -pl appointment-service,doctor-service,patient-service,user-service,api-gateway -am test
```

```powershell
..\.tools\node-v20.18.0-win-x64\npm.cmd run build
```

Both backend and frontend builds completed successfully after using the bundled toolchains.
