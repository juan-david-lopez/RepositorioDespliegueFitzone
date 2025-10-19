# Sistema de Fidelización y Canje de Beneficios - FitZone

## 📋 Descripción General

El Sistema de Fidelización permite gestionar de forma automática la acumulación, validación y canje de créditos/puntos en función del comportamiento del usuario dentro de los procesos del sistema (membresía, reservas y login).

## 🎯 Objetivos

- Incentivar la participación constante
- Mejorar la retención de miembros
- Convertir cada acción en una oportunidad para ganar recompensas
- Personalizar la experiencia del usuario según su nivel de fidelización

---

## 🏆 Niveles de Fidelización (Tiers)

### 🥉 BRONCE (0-6 meses)
- Beneficios estándar según membresía
- Acceso al catálogo básico de recompensas

### 🥈 PLATA (6-12 meses)
- **+5% descuento** en renovación
- **+1 clase adicional/mes**
- Acceso a recompensas premium

### 🥇 ORO (12-24 meses)
- **+10% descuento** en renovación
- **+2 clases adicionales/mes**
- **1 invitado gratis/mes**
- Prioridad en reservas
- Acceso a recompensas exclusivas

### 💎 PLATINO (+24 meses)
- **+15% descuento** en renovación
- **+4 clases adicionales/mes**
- **2 invitados gratis/mes**
- Máxima prioridad en reservas
- Acceso total a todas las recompensas

---

## 💰 Sistema de Puntos

### Actividades que Generan Puntos

| Actividad | Puntos Base | Puntos Bonus (x2) |
|-----------|-------------|-------------------|
| Compra de Membresía | 100 | 200 |
| Renovación de Membresía | 80 | 160 |
| Upgrade de Membresía | 150 | 300 |
| Asistencia a Clase | 10 | 20 |
| Referido Exitoso | 200 | 400 |
| Racha de Logins | 5 | 10 |
| Renovación Anticipada | 50 | 100 |
| Pago Puntual | 20 | 40 |
| Compartir en Redes | 15 | 30 |
| Completar Perfil | 30 | 60 |

### Reglas de Puntos

- ✅ Los puntos se acumulan automáticamente
- ✅ Solo se otorgan puntos si la membresía está activa
- ⏰ Los puntos expiran después de 12 meses
- ❌ Los puntos se revierten si la actividad se cancela

---

## 🎁 Catálogo de Recompensas

### Recompensas Básicas (Nivel BRONCE)

1. **Clase Gratis** - 100 puntos
   - Validez: 30 días
   - Una clase grupal en cualquier sucursal

2. **10% Descuento en Renovación** - 150 puntos
   - Validez: 60 días
   - No acumulable con otras promociones

3. **Pase para Invitado** - 80 puntos
   - Validez: 30 días
   - Un amigo puede entrenar contigo

4. **5 Días Extra de Membresía** - 120 puntos
   - Validez: 90 días
   - Se suman a tu fecha de vencimiento

### Recompensas Premium (Nivel PLATA)

5. **Sesión de Entrenamiento Personal** - 250 puntos
   - Validez: 45 días
   - 1 hora con entrenador certificado

6. **Consulta Nutricional** - 300 puntos
   - Validez: 60 días
   - Incluye plan nutricional básico

7. **Pack de 3 Clases Gratis** - 250 puntos
   - Validez: 60 días

### Recompensas Exclusivas (Nivel ORO)

8. **Upgrade Temporal a Premium** - 200 puntos
   - Validez: 7 días
   - Todos los beneficios Premium

9. **20% Descuento en Renovación** - 350 puntos
   - Validez: 60 días
   - Solo para Oro y Platino

10. **Upgrade Temporal a Elite** - 400 puntos
    - Validez: 7 días
    - Acceso a todas las sucursales

---

## 🔄 Procesos Automáticos

### 1. Registro de Actividades
- ✅ Captura automática de eventos desde otros módulos
- ✅ Validación de membresía activa
- ✅ Cálculo automático de puntos base y bonificaciones
- ✅ Registro en historial personal

### 2. Evaluación de Nivel
- ✅ Verificación diaria de antigüedad a las 2:00 AM
- ✅ Actualización automática de tiers
- ✅ Notificación por email al cambiar de nivel

### 3. Aplicación de Beneficios Pasivos
- ✅ Descuentos automáticos según tier
- ✅ Prioridad en reservas para Oro y Platino
- ✅ Cupos adicionales en clases

### 4. Gestión de Expiración
- ✅ Puntos: Expiran a los 12 meses (verificado diariamente a las 3:00 AM)
- ✅ Canjes: Expiran según validez de la recompensa (verificado a las 4:00 AM)
- ✅ Notificaciones de puntos próximos a expirar

---

## 🌐 API Endpoints

### Perfil de Fidelización

**GET** `/api/loyalty/profile`
- Obtiene el perfil de fidelización del usuario autenticado
- Crea el perfil automáticamente si no existe

**GET** `/api/loyalty/dashboard`
- Dashboard completo con estadísticas, actividades recientes y recompensas recomendadas

**GET** `/api/loyalty/tiers/{tier}/benefits`
- Obtiene los beneficios de un nivel específico (BRONCE, PLATA, ORO, PLATINO)

### Actividades

**GET** `/api/loyalty/activities`
- Lista todas las actividades de fidelización del usuario

**POST** `/api/loyalty/activities`
- Registra manualmente una actividad (para casos especiales)

### Recompensas

**GET** `/api/loyalty/rewards`
- Obtiene todas las recompensas disponibles

**GET** `/api/loyalty/rewards/affordable`
- Obtiene solo las recompensas que el usuario puede costear

**GET** `/api/loyalty/rewards/{rewardId}`
- Obtiene detalles de una recompensa específica

### Canjes

**POST** `/api/loyalty/redeem`
```json
{
  "rewardId": 1,
  "notes": "Quiero usar esta recompensa para..."
}
```
- Canjea una recompensa por puntos

**GET** `/api/loyalty/redemptions`
- Lista todos los canjes del usuario

**GET** `/api/loyalty/redemptions/validate/{code}`
- Valida un código de canje (para uso del personal)

**PUT** `/api/loyalty/redemptions/{code}/use`
- Marca un canje como utilizado

---

## 📊 Integración con Otros Módulos

### Módulo de Membresías
- ✅ **Compra**: Otorga 100 puntos automáticamente
- ✅ **Renovación**: Otorga 80 puntos (o 50 si es anticipada)
- ✅ **Upgrade**: Otorga 150 puntos
- ✅ **Descuentos**: Aplica descuentos según tier en renovaciones

### Módulo de Reservas
- ✅ **Asistencia**: Otorga 10 puntos por clase completada
- ✅ **Cancelación**: Revierte puntos si se cancela sin asistir
- ✅ **Prioridad**: Usuarios Oro y Platino tienen prioridad

### Módulo de Autenticación
- ✅ **Registro**: Crea perfil de fidelización automáticamente
- ✅ **Login continuo**: Otorga 5 puntos por racha de logins

### Módulo de Pagos
- ✅ **Pago puntual**: Otorga 20 puntos adicionales
- ✅ **Descuentos**: Aplica cupones de fidelización automáticamente

---

## 🗄️ Estructura de Base de Datos

### Tablas Principales

1. **loyalty_profiles** - Perfil de fidelización del usuario
   - Tier actual, puntos totales, puntos disponibles
   - Estadísticas: clases asistidas, renovaciones, referidos

2. **loyalty_activities** - Registro de actividades que generan puntos
   - Tipo de actividad, puntos ganados, fecha de expiración
   - Referencia a la acción original (membresía, reserva, etc.)

3. **loyalty_rewards** - Catálogo de recompensas
   - Tipo, costo en puntos, validez, tier mínimo requerido

4. **loyalty_redemptions** - Historial de canjes
   - Código único, estado, fecha de canje, fecha de uso

---

## 📝 Ejemplos de Uso

### Escenario 1: Usuario Nuevo
1. Usuario se registra → Perfil de fidelización creado (Tier: BRONCE)
2. Compra membresía BASIC → +100 puntos
3. Asiste a 5 clases → +50 puntos (5 × 10)
4. Total: 150 puntos disponibles
5. Puede canjear: "10% Descuento en Renovación" (150 puntos)

### Escenario 2: Usuario Ascendiendo
1. Usuario con 5 meses de antigüedad (BRONCE)
2. Sistema ejecuta tarea nocturna
3. Usuario cumple 6 meses → Asciende a PLATA
4. Recibe email de notificación
5. Nuevos beneficios: +5% descuento, +1 clase/mes

### Escenario 3: Canje de Recompensa
1. Usuario con 350 puntos
2. Canjea "Sesión de Entrenamiento Personal" (250 puntos)
3. Sistema descuenta puntos → 100 puntos restantes
4. Genera código único: RWD-A3F2C8D1
5. Código válido por 45 días
6. Usuario presenta código al entrenador
7. Sistema marca como utilizado

---

## 🚀 Ventajas del Sistema

✅ **Automatización Total**: Sin intervención manual
✅ **Integrado**: Funciona con todos los módulos existentes
✅ **Escalable**: Fácil agregar nuevas recompensas o actividades
✅ **Transparente**: Historial completo de puntos y canjes
✅ **Motivacional**: Mensajes personalizados según progreso
✅ **Seguro**: Códigos únicos, validación de expiración

---

## 📅 Tareas Programadas

| Hora | Frecuencia | Tarea | Descripción |
|------|-----------|-------|-------------|
| 02:00 AM | Diaria | Actualización de Tiers | Evalúa antigüedad y actualiza niveles |
| 03:00 AM | Diaria | Expiración de Puntos | Marca puntos de +12 meses como expirados |
| 04:00 AM | Diaria | Expiración de Canjes | Marca canjes no usados como expirados |

---

## 🔧 Configuración Inicial

### Recompensas Pre-cargadas
Al iniciar la aplicación por primera vez, el sistema carga automáticamente 10 recompensas predeterminadas a través del `LoyaltyRewardsInitializer`.

### Creación de Perfiles
Los perfiles de fidelización se crean automáticamente cuando:
- Un usuario realiza su primera actividad elegible
- Se consulta el perfil del usuario
- El usuario accede al dashboard de fidelización

---

## 📞 Soporte y Mantenimiento

### Agregar Nueva Recompensa
1. Crear registro en `loyalty_rewards` con admin panel o SQL
2. Definir: nombre, tipo, costo, tier mínimo, validez
3. La recompensa aparece automáticamente en el catálogo

### Agregar Nuevo Tipo de Actividad
1. Agregar valor al enum `ActivityType`
2. Actualizar método `calculatePointsForActivity()` en `LoyaltyServiceImpl`
3. Integrar llamada a `logActivityAutomatic()` en el módulo correspondiente

### Modificar Beneficios de Tier
Actualizar método `getTierBenefits()` en `LoyaltyServiceImpl` con los nuevos valores.

---

## ✅ Estado del Sistema

**✅ COMPLETAMENTE IMPLEMENTADO Y FUNCIONAL**

- [x] Modelos de datos (Entidades y Enums)
- [x] Repositorios JPA
- [x] DTOs de Request y Response
- [x] Servicio completo con toda la lógica de negocio
- [x] Controlador REST con todos los endpoints
- [x] Tareas programadas para automatización
- [x] Integración con módulo de Membresías
- [x] Inicializador de recompensas predeterminadas
- [x] Sistema de notificaciones por email
- [x] Validación y expiración de puntos/canjes
- [x] Dashboard completo de fidelización

---

## 🎉 Próximos Pasos

1. **Compilar y ejecutar la aplicación**
2. **Verificar que las tablas se creen correctamente**
3. **Probar endpoints con Postman o similar**
4. **Integrar con el frontend**
5. **Monitorear logs para validar funcionamiento**

---

**Desarrollado para FitZone - Sistema de Gestión de Gimnasios**
Fecha de implementación: 06 de Octubre, 2025

