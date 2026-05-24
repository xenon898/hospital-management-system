# TODO - Hospital Management System (Spring Boot Microservices + React + PostgreSQL)

## Step 1: Scaffold repo structure
- [ ] Create `backend/` multi-module Maven parent project
- [ ] Create 5 Spring Boot apps: `api-gateway`, `user-service`, `doctor-service`, `patient-service`, `appointment-service`
- [ ] Create `frontend/` React app structure
- [ ] Add `db/` folder and SQL scripts placeholders
- [ ] Add `postman/` folder and collection placeholder

## Step 2: Implement Backend shared conventions
- [ ] Add common dependencies (Web, JPA, Security) per service
- [ ] Add Lombok + validation
- [ ] Add global exception handler per service
- [ ] Add entity/DTO/controller/service/repository folder structures

## Step 3: Implement User Service (8081)
- [ ] `AppUser` entity + JPA repository
- [ ] Register endpoint
- [ ] Login endpoint (JWT generation)
- [ ] `GET /api/users/me`
- [ ] Seed dummy admin/doctor/patient users

## Step 4: Implement JWT & Gateway (8080)
- [ ] JWT validation filter
- [ ] Role extraction (ADMIN/DOCTOR/PATIENT)
- [ ] Gateway route proxy to services (REST forwarding)
- [ ] Role-based endpoint protection

## Step 5: Implement Doctor Service (8082)
- [ ] `DoctorProfile` entity
- [ ] Prescription entity + CRUD for DOCTOR
- [ ] ADMIN manage doctors endpoints
- [ ] DOCTOR view appointments + patient history endpoints

## Step 6: Implement Patient Service (8083)
- [ ] `PatientProfile` entity
- [ ] ADMIN manage patients endpoints
- [ ] PATIENT view own profile + appointment history endpoints

## Step 7: Implement Appointment Service (8084)
- [ ] Appointment entity + status enum
- [ ] Book appointment (PATIENT)
- [ ] Update status (DOCTOR)
- [ ] Doctor/PATIENT/Admin list endpoints

## Step 8: Cross-service REST integration (simple)
- [ ] Where needed, call other services via REST using IDs
- [ ] Keep responses simple and beginner-friendly

## Step 9: Implement React frontend
- [ ] Create axios client (JWT from localStorage)
- [ ] Router + protected routes by role
- [ ] Pages: Login/Register/Admin/Doctor/Patient + booking + prescriptions
- [ ] Navbar + Error page
- [ ] CSS for clean simple styling

## Step 10: PostgreSQL SQL scripts
- [ ] Provide create/insert SQL for each DB: `user_db`, `doctor_db`, `patient_db`, `appointment_db`
- [ ] Insert sample dummy data

## Step 11: Postman collection
- [ ] Create requests for auth + admin + doctor + patient flows
- [ ] Export collection JSON

## Step 12: README + run instructions
- [ ] Step-by-step setup (DB, backend services, frontend)
- [ ] Ports and URLs
- [ ] Postman import instructions

## Step 13: Basic verification
- [ ] Run all services locally
- [ ] Test login/register + role-based access
- [ ] Test booking + status update + prescriptions flow

