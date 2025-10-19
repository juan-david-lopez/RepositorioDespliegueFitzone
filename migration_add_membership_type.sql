-- ============================================
-- Script de Migración: Agregar campo membership_type
-- Fecha: 2025-10-10
-- Descripción: Agrega la columna membership_type a users_base
--              para almacenar directamente el tipo de membresía
-- ============================================

-- 1. Agregar columna membership_type si no existe
ALTER TABLE users_base
ADD COLUMN IF NOT EXISTS membership_type VARCHAR(50);

-- 2. Crear índice para mejorar rendimiento de consultas
CREATE INDEX IF NOT EXISTS idx_users_membership_type
ON users_base(membership_type);

-- 3. Actualizar usuarios existentes que tienen membresía activa
-- Esto sincroniza los datos existentes
UPDATE users_base u
SET membership_type = mt.name
FROM memberships_base m
JOIN membership_types_base mt ON m.membership_type_id = mt.id_membership_type
WHERE u.id_user = m.user_id
AND m.status = 'ACTIVE'
AND u.membership_type IS NULL;

-- 4. Verificar que la columna se creó correctamente
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'users_base'
AND column_name = 'membership_type';

-- 5. Verificar datos actualizados (opcional)
SELECT
    u.id_user,
    u.email,
    u.membership_type AS campo_directo,
    mt.name AS tipo_desde_relacion,
    m.status AS estado_membresia
FROM users_base u
LEFT JOIN memberships_base m ON u.membership_id = m.id_membership
LEFT JOIN membership_types_base mt ON m.membership_type_id = mt.id_membership_type
WHERE u.membership_id IS NOT NULL
LIMIT 10;

-- ============================================
-- Resultado Esperado:
-- ============================================
-- La columna membership_type debe existir en users_base
-- Los usuarios con membresía activa deben tener el campo actualizado
-- Ejemplo:
-- id_user | email              | campo_directo | tipo_desde_relacion | estado_membresia
-- 1       | user@example.com   | PREMIUM       | PREMIUM             | ACTIVE
-- ============================================

COMMIT;
