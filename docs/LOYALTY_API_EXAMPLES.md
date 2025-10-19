# Ejemplos de Uso - API de Fidelización FitZone

## 📌 Configuración Inicial

**Base URL**: `http://localhost:8080/api/loyalty`

**Autenticación**: Todos los endpoints requieren un token JWT en el header:
```
Authorization: Bearer {tu_token_jwt}
```

---

## 1️⃣ Obtener Perfil de Fidelización

### Request
```http
GET /api/loyalty/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Perfil de fidelización obtenido exitosamente",
  "data": {
    "idLoyaltyProfile": 1,
    "userId": 42,
    "userEmail": "juan@example.com",
    "userName": "Juan Pérez",
    "currentTier": "PLATA",
    "tierDisplayName": "PLATA",
    "totalPoints": 450,
    "availablePoints": 350,
    "memberSince": "2024-04-15T10:30:00",
    "monthsAsMember": 6,
    "lastActivityDate": "2025-10-05T14:22:00",
    "totalActivitiesLogged": 25,
    "consecutiveLoginDays": 7,
    "totalReferrals": 2,
    "classesAttended": 15,
    "renewalsCompleted": 1,
    "tierBenefits": {
      "tierName": "PLATA",
      "renewalDiscountPercentage": 5,
      "additionalClassesPerMonth": 1,
      "freeGuestPassesPerMonth": 0,
      "priorityReservations": false,
      "description": "5% descuento en renovación + 1 clase adicional/mes"
    },
    "monthsToNextTier": 6,
    "nextTier": "ORO"
  }
}
```

---

## 2️⃣ Ver Dashboard Completo

### Request
```http
GET /api/loyalty/dashboard
Authorization: Bearer {token}
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Dashboard de fidelización obtenido exitosamente",
  "data": {
    "profile": { /* mismo objeto del perfil */ },
    "recentActivities": [
      {
        "idLoyaltyActivity": 123,
        "activityType": "CLASS_ATTENDANCE",
        "activityTypeDisplayName": "Asistencia a Clase",
        "pointsEarned": 10,
        "description": "Clase de Yoga - Sede Norte",
        "referenceId": 456,
        "activityDate": "2025-10-05T18:00:00",
        "isBonusActivity": false,
        "expirationDate": "2026-10-05T18:00:00",
        "isExpired": false,
        "isCancelled": false
      }
    ],
    "activeRedemptions": [
      {
        "idLoyaltyRedemption": 5,
        "redemptionCode": "RWD-A3F2C8D1",
        "rewardName": "Sesión de Entrenamiento Personal",
        "rewardType": "PERSONAL_TRAINING",
        "pointsSpent": 250,
        "status": "ACTIVE",
        "statusDisplayName": "ACTIVE",
        "redemptionDate": "2025-10-01T10:00:00",
        "expirationDate": "2025-11-15T23:59:59",
        "usedDate": null,
        "notes": "Para sesión de cardio",
        "appliedReferenceId": null,
        "isExpired": false,
        "canBeUsed": true
      }
    ],
    "recommendedRewards": [
      {
        "idLoyaltyReward": 2,
        "name": "10% Descuento en Renovación",
        "description": "Obtén 10% de descuento en tu próxima renovación",
        "rewardType": "RENEWAL_DISCOUNT",
        "rewardTypeDisplayName": "Descuento en Renovación",
        "pointsCost": 150,
        "minimumTierRequired": "BRONCE",
        "validityDays": 60,
        "rewardValue": "10",
        "termsAndConditions": "Aplicable solo en renovaciones...",
        "canUserAfford": true,
        "meetsMinimumTier": true
      }
    ],
    "pointsExpiringInNext30Days": 50,
    "motivationalMessage": "¡Excelente progreso! Nivel Oro a solo 6 meses de distancia 🥇"
  }
}
```

---

## 3️⃣ Ver Catálogo de Recompensas

### Request
```http
GET /api/loyalty/rewards
Authorization: Bearer {token}
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Recompensas obtenidas exitosamente",
  "data": [
    {
      "idLoyaltyReward": 1,
      "name": "Clase Gratis",
      "description": "Una clase grupal completamente gratis",
      "rewardType": "FREE_CLASS",
      "rewardTypeDisplayName": "Clase Gratis",
      "pointsCost": 100,
      "minimumTierRequired": "BRONCE",
      "validityDays": 30,
      "rewardValue": "1",
      "termsAndConditions": "Válido para clases grupales regulares...",
      "canUserAfford": true,
      "meetsMinimumTier": true
    },
    {
      "idLoyaltyReward": 4,
      "name": "Sesión de Entrenamiento Personal",
      "description": "Una sesión de 1 hora con entrenador certificado",
      "rewardType": "PERSONAL_TRAINING",
      "rewardTypeDisplayName": "Entrenamiento Personal",
      "pointsCost": 250,
      "minimumTierRequired": "PLATA",
      "validityDays": 45,
      "rewardValue": "1",
      "termsAndConditions": "Sujeto a disponibilidad...",
      "canUserAfford": true,
      "meetsMinimumTier": true
    }
  ],
  "total": 10
}
```

---

## 4️⃣ Ver Solo Recompensas Alcanzables

### Request
```http
GET /api/loyalty/rewards/affordable
Authorization: Bearer {token}
```

### Response
Similar a la anterior, pero solo incluye recompensas que el usuario puede costear con sus puntos actuales.

---

## 5️⃣ Canjear una Recompensa

### Request
```http
POST /api/loyalty/redeem
Authorization: Bearer {token}
Content-Type: application/json

{
  "rewardId": 4,
  "notes": "Quiero la sesión para entrenar piernas"
}
```

### Response (201 Created)
```json
{
  "success": true,
  "message": "Recompensa canjeada exitosamente",
  "data": {
    "idLoyaltyRedemption": 12,
    "redemptionCode": "RWD-B7K9M2P5",
    "rewardName": "Sesión de Entrenamiento Personal",
    "rewardType": "PERSONAL_TRAINING",
    "pointsSpent": 250,
    "status": "ACTIVE",
    "statusDisplayName": "ACTIVE",
    "redemptionDate": "2025-10-06T11:30:00",
    "expirationDate": "2025-11-20T23:59:59",
    "usedDate": null,
    "notes": "Quiero la sesión para entrenar piernas",
    "appliedReferenceId": null,
    "isExpired": false,
    "canBeUsed": true
  }
}
```

### Errores Comunes

**Puntos Insuficientes (400 Bad Request)**
```json
{
  "success": false,
  "message": "No tienes suficientes puntos para esta recompensa"
}
```

**Nivel Insuficiente (400 Bad Request)**
```json
{
  "success": false,
  "message": "No cumples con el nivel mínimo requerido para esta recompensa"
}
```

---

## 6️⃣ Ver Mis Canjes

### Request
```http
GET /api/loyalty/redemptions
Authorization: Bearer {token}
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Canjes obtenidos exitosamente",
  "data": [
    {
      "idLoyaltyRedemption": 12,
      "redemptionCode": "RWD-B7K9M2P5",
      "rewardName": "Sesión de Entrenamiento Personal",
      "rewardType": "PERSONAL_TRAINING",
      "pointsSpent": 250,
      "status": "ACTIVE",
      "statusDisplayName": "ACTIVE",
      "redemptionDate": "2025-10-06T11:30:00",
      "expirationDate": "2025-11-20T23:59:59",
      "usedDate": null,
      "notes": "Quiero la sesión para entrenar piernas",
      "appliedReferenceId": null,
      "isExpired": false,
      "canBeUsed": true
    },
    {
      "idLoyaltyRedemption": 8,
      "redemptionCode": "RWD-C3D1E4F6",
      "rewardName": "Clase Gratis",
      "rewardType": "FREE_CLASS",
      "pointsSpent": 100,
      "status": "USED",
      "statusDisplayName": "USED",
      "redemptionDate": "2025-09-20T09:15:00",
      "expirationDate": "2025-10-20T23:59:59",
      "usedDate": "2025-09-25T18:00:00",
      "notes": null,
      "appliedReferenceId": 789,
      "isExpired": false,
      "canBeUsed": false
    }
  ],
  "total": 2
}
```

---

## 7️⃣ Validar Código de Canje (Para Personal del Gimnasio)

### Request
```http
GET /api/loyalty/redemptions/validate/RWD-B7K9M2P5
Authorization: Bearer {token_admin}
```

### Response - Código Válido (200 OK)
```json
{
  "success": true,
  "message": "Código válido y listo para usar",
  "data": {
    "idLoyaltyRedemption": 12,
    "redemptionCode": "RWD-B7K9M2P5",
    "rewardName": "Sesión de Entrenamiento Personal",
    "rewardType": "PERSONAL_TRAINING",
    "pointsSpent": 250,
    "status": "ACTIVE",
    "redemptionDate": "2025-10-06T11:30:00",
    "expirationDate": "2025-11-20T23:59:59",
    "canBeUsed": true
  }
}
```

### Response - Código Ya Usado (200 OK)
```json
{
  "success": true,
  "message": "Código ya utilizado",
  "data": {
    "redemptionCode": "RWD-C3D1E4F6",
    "status": "USED",
    "usedDate": "2025-09-25T18:00:00",
    "canBeUsed": false
  }
}
```

### Response - Código Expirado (200 OK)
```json
{
  "success": true,
  "message": "Código expirado",
  "data": {
    "redemptionCode": "RWD-A1B2C3D4",
    "status": "EXPIRED",
    "expirationDate": "2025-09-30T23:59:59",
    "canBeUsed": false
  }
}
```

---

## 8️⃣ Marcar Código como Utilizado (Para Personal)

### Request
```http
PUT /api/loyalty/redemptions/RWD-B7K9M2P5/use?referenceId=999
Authorization: Bearer {token_admin}
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Canje marcado como utilizado exitosamente"
}
```

---

## 9️⃣ Ver Historial de Actividades

### Request
```http
GET /api/loyalty/activities
Authorization: Bearer {token}
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Actividades obtenidas exitosamente",
  "data": [
    {
      "idLoyaltyActivity": 150,
      "activityType": "MEMBERSHIP_PURCHASE",
      "activityTypeDisplayName": "Compra de Membresía",
      "pointsEarned": 100,
      "description": "Compra de membresía PREMIUM",
      "referenceId": 45,
      "activityDate": "2025-04-15T10:30:00",
      "isBonusActivity": false,
      "expirationDate": "2026-04-15T10:30:00",
      "isExpired": false,
      "isCancelled": false
    },
    {
      "idLoyaltyActivity": 151,
      "activityType": "CLASS_ATTENDANCE",
      "activityTypeDisplayName": "Asistencia a Clase",
      "pointsEarned": 10,
      "description": "Clase de Spinning - Sede Centro",
      "referenceId": 234,
      "activityDate": "2025-04-20T19:00:00",
      "isBonusActivity": false,
      "expirationDate": "2026-04-20T19:00:00",
      "isExpired": false,
      "isCancelled": false
    },
    {
      "idLoyaltyActivity": 152,
      "activityType": "REFERRAL",
      "activityTypeDisplayName": "Referido Exitoso",
      "pointsEarned": 200,
      "description": "Referido: María González",
      "referenceId": null,
      "activityDate": "2025-05-10T14:20:00",
      "isBonusActivity": false,
      "expirationDate": "2026-05-10T14:20:00",
      "isExpired": false,
      "isCancelled": false
    }
  ],
  "total": 25
}
```

---

## 🔟 Ver Beneficios de un Tier

### Request
```http
GET /api/loyalty/tiers/ORO/benefits
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Beneficios del nivel obtenidos exitosamente",
  "data": {
    "tierName": "ORO",
    "renewalDiscountPercentage": 10,
    "additionalClassesPerMonth": 2,
    "freeGuestPassesPerMonth": 1,
    "priorityReservations": true,
    "description": "10% descuento + 2 clases adicionales/mes + invitado gratis 1 vez/mes"
  }
}
```

---

## 🎯 Flujo Completo de Usuario

### Paso 1: Registro y Primera Compra
1. Usuario se registra en FitZone
2. Compra membresía BASIC → **+100 puntos** 🎉
3. Se crea perfil de fidelización automáticamente (Tier: BRONCE)

### Paso 2: Usar el Gimnasio
4. Asiste a clase de Yoga → **+10 puntos**
5. Asiste a clase de Spinning → **+10 puntos**
6. Asiste a clase de Funcional → **+10 puntos**
7. Login diario durante 5 días → **+25 puntos** (5 días × 5 puntos)
8. **Total acumulado: 155 puntos**

### Paso 3: Canjear Primera Recompensa
9. Consulta catálogo: `GET /api/loyalty/rewards/affordable`
10. Selecciona "10% Descuento en Renovación" (150 puntos)
11. Canjea: `POST /api/loyalty/redeem` con `rewardId: 2`
12. Recibe código: **RWD-X7Y9Z2A4**
13. **Puntos restantes: 5**

### Paso 4: Usar la Recompensa
14. Al renovar membresía, ingresa código RWD-X7Y9Z2A4
15. Sistema valida código y aplica 10% de descuento
16. Renovación → **+80 puntos** 🎉
17. **Total: 85 puntos**

### Paso 5: Acumular Más
18. Refiere a un amigo que se inscribe → **+200 puntos** 🎉
19. Pago puntual → **+20 puntos**
20. **Total acumulado: 305 puntos**

### Paso 6: Canjear Recompensa Premium
21. Consulta recompensas disponibles
22. Canjea "Sesión de Entrenamiento Personal" (250 puntos)
23. **Puntos restantes: 55**
24. Usa código con entrenador

---

## 📧 Notificaciones Automáticas

### Email: Ascenso de Nivel
```
Asunto: 🎉 ¡Felicidades! Has ascendido a PLATA

¡Increíble logro! Has ascendido de BRONCE a PLATA.

Tus nuevos beneficios incluyen:
5% descuento en renovación + 1 clase adicional/mes

Gracias por tu fidelidad.
```

### Email: Canje Confirmado
```
Asunto: ✅ Canje Confirmado - Sesión de Entrenamiento Personal

Tu canje ha sido procesado exitosamente.

Código: RWD-B7K9M2P5
Recompensa: Sesión de Entrenamiento Personal
Puntos gastados: 250
Válido hasta: 2025-11-20

Presenta este código para utilizar tu recompensa.
```

### Email: Recompensas Disponibles
```
Asunto: 🎁 ¡Tienes recompensas disponibles!

Tienes 350 puntos acumulados y hay 5 recompensas que puedes canjear.

Visita tu perfil de fidelización para ver las opciones disponibles.
```

---

## 🛠️ Testing con cURL

### Obtener Perfil
```bash
curl -X GET "http://localhost:8080/api/loyalty/profile" \
  -H "Authorization: Bearer {token}"
```

### Canjear Recompensa
```bash
curl -X POST "http://localhost:8080/api/loyalty/redeem" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "rewardId": 1,
    "notes": "Primera recompensa"
  }'
```

### Validar Código
```bash
curl -X GET "http://localhost:8080/api/loyalty/redemptions/validate/RWD-B7K9M2P5" \
  -H "Authorization: Bearer {token_admin}"
```

---

**¡Sistema listo para usar!** 🚀

