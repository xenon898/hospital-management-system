# Hospital Management System

## Project Documentation

**Project Type:** Full-stack application using React and Spring Boot Microservices  
**Database:** PostgreSQL  
**Testing Tool:** Web Interface and Postman  
**Prepared By:** ____________________  
**Submitted To:** ____________________  
**Date:** ____________________

---

## 1. Introduction

The Hospital Management System is a full-stack application for handling basic hospital activities:

- Admin-controlled doctor and patient account creation
- Doctor and patient login using JWT authentication
- Doctor and patient profile management
- Appointment booking and appointment status updates
- Prescription creation and patient prescription history

The application follows a microservices architecture. An API Gateway provides one common entry point for the React web interface and clients such as Postman.

Public registration has been removed. This makes the workflow more production-like because doctors and patients cannot create their own accounts. A default admin account is created automatically when the backend starts, and the admin creates doctor and patient accounts.

---

## 2. Objectives

1. To manage three roles: Admin, Doctor, and Patient.
2. To create a default admin account automatically for the local demo.
3. To allow only Admin to create Doctor and Patient accounts.
4. To securely access protected operations using JSON Web Tokens (JWT).
5. To allow a patient to book an appointment with a doctor.
6. To allow a doctor to view appointments, update appointment status, and create prescriptions.
7. To store data using PostgreSQL databases.

---

## 3. Technologies Used

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 3.3.2 | Backend application framework |
| Spring Web | REST API development |
| Spring Security | Authentication and role authorization |
| JWT / JJWT | Token-based authentication |
| Spring Data JPA / Hibernate | Database mapping and operations |
| PostgreSQL | Relational database |
| Maven | Build and dependency management |
| Lombok | Reduces boilerplate Java code |
| React 18 | User interface for hospital workflows |
| Vite 6 | Frontend build and development server |
| Postman | API testing and demonstration |

---

## 4. System Architecture

```text
                    React Frontend / Postman Client
                       Port 3000 / API Client
                                |
                                v
                    API Gateway - Port 8080
                  JWT validation and routing
          _____________|______________|_______________
         |             |              |               |
         v             v              v               v
 User Service     Doctor Service  Patient Service  Appointment Service
 Port 8081        Port 8082       Port 8083       Port 8084
     |                |              |               |
     v                v              v               v
  user_db          doctor_db      patient_db     appointment_db
```

| Service | Port | Database | Responsibility |
|---|---:|---|---|
| API Gateway | `8080` | None | Receives client requests, validates tokens, checks roles, and forwards calls |
| User Service | `8081` | `user_db` | Logs users in, creates default admin, and provides admin-only account creation |
| Doctor Service | `8082` | `doctor_db` | Manages doctor profiles and prescriptions |
| Patient Service | `8083` | `patient_db` | Manages patient profiles |
| Appointment Service | `8084` | `appointment_db` | Manages appointment booking, viewing, and status changes |

All Postman and frontend calls should go through:

```text
http://localhost:8080/api
```

---

## 5. User Roles and Responsibilities

### Admin

Admin can:

- Login using the default admin credentials
- Create doctor accounts and doctor profiles
- Create patient accounts and patient profiles
- View all doctors
- View all patients
- View all appointments

Default admin credentials:

```text
username: admin
password: admin123
```

The default admin is created when:

```properties
app.admin.bootstrap.enabled=true
```

### Doctor

Doctor can:

- Login after Admin creates the doctor account
- View assigned appointments
- Change appointment status
- Create prescriptions
- View a patient's prescription history
- View the doctor list

### Patient

Patient can:

- Login after Admin creates the patient account
- View available doctors
- Book a future appointment
- View own appointments

---

## 6. Authentication and Authorization

Only this endpoint is public:

```http
POST /api/users/login
```

The removed public endpoint is:

```http
POST /api/users/register
```

For protected APIs, pass the JWT token:

```http
Authorization: Bearer JWT_TOKEN_VALUE
```

Role access matrix:

| API Operation | Admin | Doctor | Patient | Public |
|---|:---:|:---:|:---:|:---:|
| Login user | Yes | Yes | Yes | Yes |
| Create doctor account and profile | Yes | No | No | No |
| Create patient account and profile | Yes | No | No | No |
| View current token identity | Yes | Yes | Yes | No |
| List doctors | Yes | Yes | Yes | No |
| View doctor own profile | No | Yes | No | No |
| Create prescription | No | Yes | No | No |
| View patient prescription history | No | Yes | No | No |
| List patients | Yes | No | No | No |
| View patient own profile | No | No | Yes | No |
| View own prescriptions | No | No | Yes | No |
| Book appointment | No | No | Yes | No |
| View own patient appointments | No | No | Yes | No |
| View assigned doctor appointments | No | Yes | No | No |
| Update appointment status | No | Yes | No | No |
| View all appointments | Yes | No | No | No |

---

## 7. Database Design

The microservices use separate databases. References across services are stored as numeric IDs instead of database foreign keys.

### User Database: `user_db`

Table: `app_users`

| Field | Description |
|---|---|
| `id` | Unique registered user ID |
| `username` | Unique login username |
| `password_hash` | BCrypt encrypted password value |
| `role` | `ADMIN`, `DOCTOR`, or `PATIENT` |

### Doctor Database: `doctor_db`

Table: `doctor_profiles`

| Field | Description |
|---|---|
| `id` | Unique doctor profile ID |
| `user_id` | User ID of the doctor account |
| `name` | Doctor name |
| `specialization` | Medical specialization |

Table: `prescriptions`

| Field | Description |
|---|---|
| `id` | Unique prescription ID |
| `appointment_id` | Appointment to which the prescription belongs |
| `patient_id` | Patient profile ID |
| `doctor_id` | Doctor profile ID |
| `content` | Prescription instructions |
| `created_at` | Prescription creation time |

### Patient Database: `patient_db`

Table: `patient_profiles`

| Field | Description |
|---|---|
| `id` | Unique patient profile ID |
| `user_id` | User ID of the patient account |
| `name` | Patient name |
| `age` | Patient age |
| `phone` | Contact number |

### Appointment Database: `appointment_db`

Table: `appointments`

| Field | Description |
|---|---|
| `id` | Unique appointment ID |
| `doctor_id` | Doctor user ID |
| `patient_id` | Patient user ID |
| `appointment_time` | Requested appointment time |
| `status` | Current appointment status |

Allowed appointment status values:

```text
PENDING
CONFIRMED
COMPLETED
CANCELLED
```

---

## 8. Functional Workflow

```text
System starts
       |
Default Admin is created
       |
Admin logs in
       |
Admin creates Doctor account + profile
       |
Admin creates Patient account + profile
       |
Doctor and Patient log in
       |
Patient views doctors and books appointment
       |
Doctor confirms appointment
       |
Doctor creates prescription
       |
Admin views all appointments
```

---

## 9. API Documentation

Base URL:

```text
http://localhost:8080/api
```

### Login User

| Item | Details |
|---|---|
| Method | `POST` |
| Endpoint | `/users/login` |
| Access | Public |
| Purpose | Verifies username/password and returns a JWT token |

Request:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Response example:

```json
{
  "token": "JWT_TOKEN",
  "role": "ADMIN",
  "userId": "1"
}
```

### Admin Creates Doctor

| Item | Details |
|---|---|
| Method | `POST` |
| Endpoint | `/users/admin/create-doctor` |
| Access | Admin |
| Purpose | Creates doctor login account and doctor profile |

Request:

```json
{
  "username": "demo_doctor_01",
  "password": "doctor123",
  "name": "Dr Sharma",
  "specialization": "Cardiology"
}
```

Response example:

```json
{
  "userId": 2,
  "profileId": 1,
  "message": "Doctor account and profile created successfully"
}
```

### Admin Creates Patient

| Item | Details |
|---|---|
| Method | `POST` |
| Endpoint | `/users/admin/create-patient` |
| Access | Admin |
| Purpose | Creates patient login account and patient profile |

Request:

```json
{
  "username": "demo_patient_01",
  "password": "patient123",
  "name": "Rahul Kumar",
  "age": 25,
  "phone": "9876543210"
}
```

Response example:

```json
{
  "userId": 3,
  "profileId": 1,
  "message": "Patient account and profile created successfully"
}
```

These admin creation APIs never return JWT tokens. Doctor and Patient must login separately with `/users/login`.

### Other Main APIs

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/users/me` | Authenticated | Shows token user ID and role |
| `GET` | `/doctors` | Authenticated | Lists doctors |
| `GET` | `/doctors/me` | Doctor | Shows own doctor profile |
| `POST` | `/doctors/prescriptions` | Doctor | Creates prescription |
| `GET` | `/doctors/patient-history/{patientProfileId}` | Doctor | Shows patient prescription history |
| `GET` | `/patients` | Admin | Lists patients |
| `GET` | `/patients/me` | Patient | Shows own patient profile |
| `GET` | `/patients/my-prescriptions` | Patient | Shows prescriptions written for the logged-in patient |
| `POST` | `/appointments` | Patient | Books appointment |
| `GET` | `/appointments/my` | Patient | Shows patient appointments |
| `GET` | `/appointments/doctor` | Doctor | Shows doctor appointments |
| `PATCH` | `/appointments/{appointmentId}/status` | Doctor | Updates appointment status |
| `GET` | `/appointments` | Admin | Lists all appointments |

---

## 10. Installation and Execution Procedure

Create these PostgreSQL databases:

```text
user_db
doctor_db
patient_db
appointment_db
```

Build the backend:

```powershell
cd C:\Users\Mrigank\Desktop\hosptal\backend
& "..\.tools\apache-maven-3.9.9\bin\mvn.cmd" -DskipTests package
```

Start each service in a separate terminal:

```powershell
java -jar backend\api-gateway\target\api-gateway-1.0.0-SNAPSHOT.jar
java -jar backend\user-service\target\user-service-1.0.0-SNAPSHOT.jar
java -jar backend\doctor-service\target\doctor-service-1.0.0-SNAPSHOT.jar
java -jar backend\patient-service\target\patient-service-1.0.0-SNAPSHOT.jar
java -jar backend\appointment-service\target\appointment-service-1.0.0-SNAPSHOT.jar
```

Start the frontend:

```powershell
cd C:\Users\Mrigank\Desktop\hosptal\frontend
$env:Path = "C:\Users\Mrigank\Desktop\hosptal\.tools\node-v20.18.0-win-x64;$env:Path"
npm install
npm run dev
```

Open:

```text
http://localhost:3000
```

---

## 11. Testing Workflow

1. Start all backend services.
2. Login as Admin using `admin` / `admin123`.
3. Admin creates a Doctor account and profile.
4. Admin creates a Patient account and profile.
5. Login as Patient and book a future appointment.
6. Login as Doctor and update the appointment status to `CONFIRMED`.
7. Doctor creates a prescription using the patient profile ID.
8. Patient opens My Prescriptions and sees the prescription.
9. Doctor views patient prescription history.
10. Admin views all appointments.

Values to note during demo:

```text
ADMIN_TOKEN =
DOCTOR_USER_ID =
DOCTOR_PROFILE_ID =
PATIENT_USER_ID =
PATIENT_PROFILE_ID =
APPOINTMENT_ID =
```

---

## 12. Security Features

- Public registration is disabled.
- Only `/api/users/login` is public.
- Default admin is created by backend bootstrap, not by public registration.
- Admin-only APIs create Doctor and Patient accounts.
- Passwords are stored as BCrypt hashes.
- JWT tokens are generated only during login.
- API Gateway verifies JWT tokens and role permissions.
- Doctor, patient, and appointment services also validate forwarded bearer tokens.
- Duplicate usernames are prevented across all roles.

---

## 13. Current Limitations

- This project is suitable for a local academic demonstration.
- Admin bootstrap credentials are simple demo credentials and should be changed for production.
- Microservices reference one another using IDs and do not use cross-database foreign keys.
- There is no doctor schedule conflict checking before appointments are booked.
- Prescriptions are stored as simple text content.
- Automated test classes are not currently included.

---

## 14. Future Enhancements

1. Add richer dashboards with appointment calendars and printable prescription views.
2. Change default admin credentials through environment variables.
3. Add appointment date availability and conflict prevention.
4. Add patient medical records, billing, and reports.
5. Add API documentation using Swagger/OpenAPI.
6. Add automated integration and security tests.
7. Store configuration secrets using environment variables.

---

## 15. Conclusion

The Hospital Management System demonstrates a React and Spring Boot microservices workflow for Admin, Doctor, and Patient users. The upgraded version is more production-like because users cannot self-register. The backend creates a default admin account, and Admin is responsible for creating doctor and patient accounts with their profiles. Doctor and Patient users then login separately and perform their role-specific hospital tasks.

---

## 16. Suggested Screenshots for Submission

| Screenshot No. | Screenshot Content |
|---:|---|
| 1 | pgAdmin showing `user_db`, `doctor_db`, `patient_db`, `appointment_db` |
| 2 | PowerShell showing ports `3000` and `8080` to `8084` listening |
| 3 | React login page |
| 4 | Admin dashboard showing doctor and patient account creation |
| 5 | Admin dashboard showing created doctor and patient profiles |
| 6 | Patient dashboard showing booked appointment with `PENDING` status |
| 7 | Doctor dashboard showing appointment updated to `CONFIRMED` |
| 8 | Doctor dashboard showing prescription history |
| 9 | Postman response from `/api/users/admin/create-doctor` or `/api/users/admin/create-patient` |
