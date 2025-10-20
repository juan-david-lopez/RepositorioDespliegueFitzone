-- ============================================
-- CONSULTAR IDS EXISTENTES ANTES DE INSERTAR
-- ============================================
-- Ejecuta este script primero para obtener los IDs correctos

-- 1. Ver usuarios ADMIN o INSTRUCTOR disponibles
SELECT
    id_user,
    email,
    role
FROM users_base
WHERE role IN ('ADMIN', 'INSTRUCTOR')
ORDER BY id_user;

-- 2. Ver todas las sedes/ubicaciones disponibles
SELECT
    id,
    city
FROM locations_base
ORDER BY id;

-- 3. Ver todos los instructores disponibles
SELECT
    id_user,
    email,
    role
FROM users_base
WHERE role = 'INSTRUCTOR'
ORDER BY id_user;

-- 4. Ver estructura de la tabla reservation para confirmar columnas
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'reservation'
ORDER BY ordinal_position;

-- 5. Ver si existen clases grupales actualmente
SELECT
    id,
    class_name,
    start_datetime,
    end_datetime,
    max_capacity,
    status
FROM reservation
WHERE reservation_type = 'GROUP_CLASS'
ORDER BY start_datetime DESC
LIMIT 10;

-- ============================================
-- NOTA: Con este script verificas los IDs disponibles
-- El script INSERT_2_CLASES_GRUPALES.sql los buscará
-- automáticamente, pero puedes usar esta consulta
-- para verificar manualmente
-- ============================================
