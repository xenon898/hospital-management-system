-- doctor_db.sql
-- Doctor service uses JPA ddl-auto=update, so tables are created automatically.
-- This file is for optional manual inserts if you want to pre-seed doctor profiles.
-- Recommended flow for a beginner demo:
--  1) Start User Service
--  2) Create doctor users via POST /api/users/register
--  3) Start Doctor Service and create doctor profiles via POST /api/doctors

-- Example payload for Doctor profile:
-- {"userId": 2, "name": "Dr Smith", "specialization": "Cardiology"}

