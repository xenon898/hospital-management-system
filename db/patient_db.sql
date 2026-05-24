-- patient_db.sql
-- Patient service uses JPA ddl-auto=update, so tables are created automatically.
-- This file is for optional manual inserts if you want to pre-seed patient profiles.
-- Recommended flow for a beginner demo:
--  1) Create patient users via POST /api/users/register
--  2) Start Patient Service and create patient profiles via POST /api/patients

-- Example payload for Patient profile:
-- {"userId": 3, "name": "Alice", "age": 22, "phone": "9999999999"}

