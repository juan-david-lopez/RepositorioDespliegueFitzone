-- ============================================
-- SCRIPT PARA INSERTAR 2 CLASES GRUPALES
-- Sistema de Pagos con Stripe
-- ============================================
-- ELITE: Gratis
-- PREMIUM/BASIC: $15,000 COP por clase
-- ============================================

-- ⚠️ NOTA: Este script busca automáticamente los IDs
-- disponibles en tu base de datos (users_base, locations_base)

-- ============================================
-- PASO 1: OBTENER IDs DISPONIBLES
-- ============================================

-- Obtener el primer usuario ADMIN o INSTRUCTOR disponible
DO $$
DECLARE
    v_admin_id BIGINT;
    v_instructor_id BIGINT;
    v_location_id BIGINT;
BEGIN
    -- Buscar un usuario ADMIN o INSTRUCTOR
    SELECT id_user INTO v_admin_id
    FROM users_base
    WHERE role IN ('ADMIN', 'INSTRUCTOR')
    ORDER BY id_user
    LIMIT 1;

    -- Buscar un instructor
    SELECT id_user INTO v_instructor_id
    FROM users_base
    WHERE role = 'INSTRUCTOR'
    ORDER BY id_user
    LIMIT 1;

    -- Si no hay instructor, usar el mismo admin
    IF v_instructor_id IS NULL THEN
        v_instructor_id := v_admin_id;
    END IF;

    -- Buscar una ubicación (tabla locations_base)
    SELECT id INTO v_location_id
    FROM locations_base
    ORDER BY id
    LIMIT 1;

    -- Validar que existen los datos necesarios
    IF v_admin_id IS NULL THEN
        RAISE EXCEPTION 'No se encontró ningún usuario ADMIN o INSTRUCTOR. Debes crear uno primero.';
    END IF;

    IF v_location_id IS NULL THEN
        RAISE NOTICE 'ADVERTENCIA: No se encontró ninguna ubicación. Las clases se crearán sin location_id.';
    END IF;

    -- Mostrar los IDs que se usarán
    RAISE NOTICE '✅ IDs encontrados:';
    RAISE NOTICE '   - Usuario creador: %', v_admin_id;
    RAISE NOTICE '   - Instructor: %', v_instructor_id;
    RAISE NOTICE '   - Ubicación: %', COALESCE(v_location_id::TEXT, 'NULL');
    RAISE NOTICE '';
    RAISE NOTICE '📝 Insertando clases grupales...';

    -- ============================================
    -- CLASE 1: Yoga Matutino
    -- ============================================
    INSERT INTO reservation (
        user_id,
        reservation_type,
        target_id,
        start_datetime,
        end_datetime,
        status,
        payment_intent_id,
        requires_payment,
        payment_amount,
        is_group,
        max_capacity,
        instructor_id,
        class_name,
        location_id,
        created_at,
        updated_at
    ) VALUES (
        v_admin_id,
        'GROUP_CLASS',
        NULL,
        '2025-10-22 08:00:00',
        '2025-10-22 09:00:00',
        'CONFIRMED',
        NULL,
        TRUE,
        15000.00,
        TRUE,
        20,
        v_instructor_id,
        'Yoga Matutino',
        v_location_id,
        NOW(),
        NOW()
    );
    RAISE NOTICE '✅ Clase 1 insertada: Yoga Matutino';

    -- ============================================
    -- CLASE 2: Spinning Nocturno
    -- ============================================
    INSERT INTO reservation (
        user_id,
        reservation_type,
        target_id,
        start_datetime,
        end_datetime,
        status,
        payment_intent_id,
        requires_payment,
        payment_amount,
        is_group,
        max_capacity,
        instructor_id,
        class_name,
        location_id,
        created_at,
        updated_at
    ) VALUES (
        v_admin_id,
        'GROUP_CLASS',
        NULL,
        '2025-10-23 19:00:00',
        '2025-10-23 20:00:00',
        'CONFIRMED',
        NULL,
        TRUE,
        15000.00,
        TRUE,
        25,
        v_instructor_id,
        'Spinning Nocturno',
        v_location_id,
        NOW(),
        NOW()
    );
    RAISE NOTICE '✅ Clase 2 insertada: Spinning Nocturno';

    -- ============================================
    -- CLASE 3: CrossFit Intermedio (BONUS)
    -- ============================================
    INSERT INTO reservation (
        user_id,
        reservation_type,
        target_id,
        start_datetime,
        end_datetime,
        status,
        payment_intent_id,
        requires_payment,
        payment_amount,
        is_group,
        max_capacity,
        instructor_id,
        class_name,
        location_id,
        created_at,
        updated_at
    ) VALUES (
        v_admin_id,
        'GROUP_CLASS',
        NULL,
        '2025-10-24 17:00:00',
        '2025-10-24 18:00:00',
        'CONFIRMED',
        NULL,
        TRUE,
        15000.00,
        TRUE,
        15,
        v_instructor_id,
        'CrossFit Intermedio',
        v_location_id,
        NOW(),
        NOW()
    );
    RAISE NOTICE '✅ Clase 3 insertada: CrossFit Intermedio';
    RAISE NOTICE '';
    RAISE NOTICE '🎉 ¡3 clases grupales insertadas exitosamente!';

END $$;

-- ============================================
-- VERIFICAR LAS CLASES INSERTADAS
-- ============================================
SELECT
    id,
    class_name AS "Nombre de Clase",
    start_datetime AS "Inicio",
    end_datetime AS "Fin",
    max_capacity AS "Cupo Máximo",
    status AS "Estado",
    requires_payment AS "Requiere Pago",
    payment_amount AS "Precio (COP)",
    is_group AS "Es Grupal"
FROM reservation
WHERE reservation_type = 'GROUP_CLASS'
  AND status = 'CONFIRMED'
  AND start_datetime > NOW()
ORDER BY start_datetime;

-- ============================================
-- CONSULTAS ÚTILES
-- ============================================

-- Ver cuántos participantes tiene cada clase
SELECT
    r.id,
    r.class_name,
    r.max_capacity,
    COUNT(rp.user_id) AS participantes_actuales,
    (r.max_capacity - COUNT(rp.user_id)) AS cupos_disponibles
FROM reservation r
LEFT JOIN reservation_participants rp ON r.id = rp.reservation_id
WHERE r.reservation_type = 'GROUP_CLASS'
  AND r.status = 'CONFIRMED'
GROUP BY r.id, r.class_name, r.max_capacity
ORDER BY r.start_datetime;

-- Ver clases disponibles (con cupo)
SELECT
    r.id,
    r.class_name,
    r.start_datetime,
    r.end_datetime,
    r.max_capacity,
    COALESCE(COUNT(rp.user_id), 0) AS participantes,
    (r.max_capacity - COALESCE(COUNT(rp.user_id), 0)) AS cupos_libres,
    r.payment_amount
FROM reservation r
LEFT JOIN reservation_participants rp ON r.id = rp.reservation_id
WHERE r.reservation_type = 'GROUP_CLASS'
  AND r.status = 'CONFIRMED'
  AND r.start_datetime > NOW()
GROUP BY r.id, r.class_name, r.start_datetime, r.end_datetime, r.max_capacity, r.payment_amount
HAVING cupos_libres > 0
ORDER BY r.start_datetime;
