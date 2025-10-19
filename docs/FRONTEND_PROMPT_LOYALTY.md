# 🎨 Prompt para Frontend - Sistema de Fidelización FitZone

## 📋 Resumen del Sistema

Implementar la interfaz de usuario completa para el **Sistema de Fidelización y Canje de Beneficios** de FitZone. Este sistema permite a los usuarios acumular puntos por sus actividades, ascender de nivel según su antigüedad, y canjear recompensas.

---

## 🎯 Funcionalidades a Implementar

### 1. **Dashboard de Fidelización** (Página Principal)
**Ruta sugerida**: `/loyalty` o `/fidelizacion`

#### Sección Superior - Resumen del Perfil
- **Badge de Nivel Actual**: BRONCE 🥉, PLATA 🥈, ORO 🥇, PLATINO 💎
- **Barra de Progreso** hacia el siguiente nivel con meses faltantes
- **Contador de Puntos**: 
  - Puntos disponibles (grande y destacado)
  - Puntos totales acumulados (secundario)
- **Alerta de Puntos por Expirar**: Si hay puntos que expiran en 30 días, mostrar mensaje:
  ```
  ⚠️ Tienes 50 puntos que expirarán el 15 de noviembre. ¡Úsalos pronto!
  ```

#### Sección de Beneficios del Nivel Actual
Mostrar card con:
- Porcentaje de descuento en renovación
- Clases adicionales por mes
- Pases de invitado gratis por mes
- Prioridad en reservas (solo Oro y Platino)

#### Mensaje Motivacional
```
🎉 "¡Excelente progreso! Nivel Oro a solo 6 meses de distancia 🥇"
```

#### Actividades Recientes (Últimas 10)
Tabla o lista de cards con:
- Icono según tipo de actividad
- Tipo de actividad (ej: "Compra de Membresía", "Asistencia a Clase")
- Puntos ganados (+100, +10, etc.)
- Descripción breve
- Fecha y hora

#### Canjes Activos
Cards con:
- Nombre de la recompensa
- Código de canje (grande y copiable)
- Estado (badge: ACTIVO, USADO, EXPIRADO)
- Fecha de expiración
- Botón "Copiar código"

---

### 2. **Catálogo de Recompensas**
**Ruta sugerida**: `/loyalty/rewards` o `/recompensas`

#### Filtros
- **Por disponibilidad**: "Solo alcanzables", "Todas"
- **Por categoría**: "Clases", "Descuentos", "Entrenamientos", "Upgrades", etc.
- **Por nivel requerido**: BRONCE, PLATA, ORO, PLATINO

#### Cards de Recompensas
Cada recompensa mostrar:
- **Imagen/Icono** representativo
- **Nombre** de la recompensa
- **Descripción** breve
- **Costo en puntos** (destacado)
- **Badge de nivel mínimo** (si aplica)
- **Días de validez** (ej: "Válido por 30 días")
- **Indicador visual**: 
  - ✅ Verde si puedes costearlo
  - 🔒 Gris si no tienes suficientes puntos o nivel
- **Botón**: "Canjear" (habilitado solo si cumple requisitos)

#### Modal de Confirmación de Canje
Al hacer clic en "Canjear":
```
┌─────────────────────────────────────────┐
│  Confirmar Canje                        │
├─────────────────────────────────────────┤
│  Recompensa: Sesión de Entrenamiento   │
│  Costo: 250 puntos                      │
│  Tus puntos actuales: 350               │
│  Puntos después del canje: 100          │
│                                         │
│  ¿Deseas continuar?                     │
│                                         │
│  [Cancelar]  [Confirmar Canje]         │
└─────────────────────────────────────────┘
```

#### Modal de Éxito
Después del canje exitoso:
```
┌─────────────────────────────────────────┐
│  ¡Canje Exitoso! 🎉                     │
├─────────────────────────────────────────┤
│  Tu código de canje:                    │
│                                         │
│  ┌───────────────────┐                  │
│  │  RWD-B7K9M2P5    │  [Copiar]       │
│  └───────────────────┘                  │
│                                         │
│  Válido hasta: 20 Nov 2025              │
│                                         │
│  Presenta este código al personal       │
│  del gimnasio para usar tu recompensa.  │
│                                         │
│  [Ver Mis Canjes]  [Cerrar]            │
└─────────────────────────────────────────┘
```

---

### 3. **Mis Canjes**
**Ruta sugerida**: `/loyalty/redemptions` o `/mis-canjes`

#### Pestañas o Filtros
- **Activos**: Canjes que aún se pueden usar
- **Usados**: Canjes ya utilizados
- **Expirados**: Canjes que no se usaron a tiempo
- **Todos**: Vista completa

#### Cards de Canjes
Para cada canje mostrar:
- **Nombre de la recompensa**
- **Código** (con botón copiar)
- **Estado**: Badge de color
  - 🟢 ACTIVO (verde)
  - ✅ USADO (azul)
  - ⏰ EXPIRADO (gris/rojo)
- **Fechas**:
  - Fecha de canje
  - Fecha de expiración (si activo)
  - Fecha de uso (si usado)
- **Puntos gastados**
- **Notas** (si las hay)

---

### 4. **Historial de Actividades**
**Ruta sugerida**: `/loyalty/activities` o `/historial`

#### Filtros
- Por tipo de actividad (dropdown)
- Por rango de fechas (date picker)

#### Lista/Tabla de Actividades
Columnas:
- **Fecha** (con formato amigable: "Hace 2 días", "15 Sep 2025")
- **Tipo de Actividad** (con icono)
- **Descripción**
- **Puntos** (+100, +10, etc.) en color verde
- **Estado**: Activo, Cancelado, Expirado
- **Fecha de expiración** de los puntos

#### Indicadores Visuales
- ✅ Actividad activa (puntos válidos)
- ❌ Actividad cancelada (puntos revertidos)
- ⏰ Puntos próximos a expirar (< 30 días)

---

### 5. **Información de Niveles**
**Ruta sugerida**: `/loyalty/tiers` o `/niveles`

#### Cards Comparativos de Niveles
Para cada nivel (BRONCE, PLATA, ORO, PLATINO):

```
┌────────────────────────────────────┐
│  🥈 PLATA                          │
│  6-12 meses de antigüedad          │
├────────────────────────────────────┤
│  ✅ 5% descuento en renovación     │
│  ✅ 1 clase adicional/mes          │
│  ✅ Recompensas premium            │
│                                    │
│  [Tu nivel actual] o               │
│  [Alcanzas en 6 meses]            │
└────────────────────────────────────┘
```

#### Timeline Visual
Mostrar línea de tiempo con:
- 0 meses → BRONCE
- 6 meses → PLATA
- 12 meses → ORO
- 24 meses → PLATINO
- Indicador de "Estás aquí" en la posición actual del usuario

---

## 🌐 Endpoints de la API

### Base URL
```
http://localhost:8080/api/loyalty
```

### Autenticación
Todos los endpoints requieren header:
```javascript
headers: {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
}
```

### Endpoints Principales

#### 1. Obtener Dashboard
```javascript
GET /api/loyalty/dashboard

Response:
{
  "success": true,
  "data": {
    "profile": {
      "idLoyaltyProfile": 1,
      "userId": 42,
      "userEmail": "usuario@example.com",
      "userName": "Juan Pérez",
      "currentTier": "PLATA",
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
    },
    "recentActivities": [...],
    "activeRedemptions": [...],
    "recommendedRewards": [...],
    "pointsExpiringInNext30Days": 50,
    "motivationalMessage": "¡Excelente progreso! Nivel Oro a solo 6 meses de distancia 🥇"
  }
}
```

#### 2. Obtener Perfil
```javascript
GET /api/loyalty/profile
```

#### 3. Obtener Recompensas
```javascript
// Todas las recompensas
GET /api/loyalty/rewards

// Solo recompensas alcanzables
GET /api/loyalty/rewards/affordable

// Recompensa específica
GET /api/loyalty/rewards/{rewardId}

Response (array):
[
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
  }
]
```

#### 4. Canjear Recompensa
```javascript
POST /api/loyalty/redeem

Body:
{
  "rewardId": 4,
  "notes": "Quiero la sesión para entrenar piernas"
}

Response:
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
    "redemptionDate": "2025-10-06T11:30:00",
    "expirationDate": "2025-11-20T23:59:59",
    "canBeUsed": true
  }
}
```

#### 5. Obtener Mis Canjes
```javascript
GET /api/loyalty/redemptions

Response:
{
  "success": true,
  "data": [
    {
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
  ]
}
```

#### 6. Obtener Historial de Actividades
```javascript
GET /api/loyalty/activities

Response:
{
  "success": true,
  "data": [
    {
      "idLoyaltyActivity": 150,
      "activityType": "MEMBERSHIP_PURCHASE",
      "activityTypeDisplayName": "Compra de Membresía",
      "pointsEarned": 100,
      "description": "Compra de membresía PREMIUM",
      "activityDate": "2025-04-15T10:30:00",
      "expirationDate": "2026-04-15T10:30:00",
      "isExpired": false,
      "isCancelled": false
    }
  ]
}
```

#### 7. Obtener Beneficios de un Nivel
```javascript
GET /api/loyalty/tiers/{tier}/benefits
// tier = BRONCE, PLATA, ORO, PLATINO

Response:
{
  "success": true,
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

## 🎨 Guía de Diseño Visual

### Paleta de Colores por Nivel

#### BRONCE
```css
--bronce-primary: #CD7F32;
--bronce-light: #E8A87C;
--bronce-gradient: linear-gradient(135deg, #CD7F32 0%, #E8A87C 100%);
```

#### PLATA
```css
--plata-primary: #C0C0C0;
--plata-light: #E8E8E8;
--plata-gradient: linear-gradient(135deg, #C0C0C0 0%, #E8E8E8 100%);
```

#### ORO
```css
--oro-primary: #FFD700;
--oro-light: #FFED4E;
--oro-gradient: linear-gradient(135deg, #FFD700 0%, #FFED4E 100%);
```

#### PLATINO
```css
--platino-primary: #E5E4E2;
--platino-light: #FFFFFF;
--platino-gradient: linear-gradient(135deg, #B9F2FF 0%, #E5E4E2 100%);
--platino-accent: #00CED1;
```

### Iconos por Tipo de Actividad

```javascript
const activityIcons = {
  MEMBERSHIP_PURCHASE: '🎟️',
  MEMBERSHIP_RENEWAL: '🔄',
  MEMBERSHIP_UPGRADE: '⬆️',
  CLASS_ATTENDANCE: '💪',
  REFERRAL: '👥',
  LOGIN_STREAK: '🔥',
  EARLY_RENEWAL: '⚡',
  PAYMENT_ON_TIME: '✅',
  SOCIAL_SHARE: '📱',
  PROFILE_COMPLETION: '📝'
};

const rewardTypeIcons = {
  FREE_CLASS: '🎓',
  RENEWAL_DISCOUNT: '💰',
  TEMPORARY_UPGRADE: '⭐',
  PERSONAL_TRAINING: '🏋️',
  GUEST_PASS: '🎫',
  MERCHANDISE_DISCOUNT: '🛍️',
  NUTRITIONAL_CONSULTATION: '🥗',
  EXTENSION_DAYS: '📅'
};
```

### Animaciones Recomendadas

1. **Al ganar puntos**: Animación de "+100" flotando hacia arriba
2. **Al canjear**: Confetti animation
3. **Al ascender de nivel**: Badge con efecto de brillo/shine
4. **Barra de progreso**: Animación suave al cargar

---

## 📱 Responsive Design

### Mobile (< 768px)
- Dashboard en columna única
- Cards de recompensas en grid de 1 columna
- Tabla de actividades convertida en cards
- Bottom navigation para secciones principales

### Tablet (768px - 1024px)
- Grid de 2 columnas para recompensas
- Dashboard con 2 secciones side-by-side

### Desktop (> 1024px)
- Grid de 3-4 columnas para recompensas
- Dashboard completo visible sin scroll
- Sidebar con navegación de fidelización

---

## 🔔 Notificaciones y Feedback

### Toast Notifications

#### Canje Exitoso
```javascript
toast.success('¡Recompensa canjeada! 🎉', {
  description: 'Código: RWD-B7K9M2P5',
  action: {
    label: 'Ver',
    onClick: () => navigate('/loyalty/redemptions')
  }
});
```

#### Puntos por Expirar
```javascript
toast.warning('⚠️ Tienes 50 puntos que expiran pronto', {
  description: 'Expiran el 15 de noviembre',
  action: {
    label: 'Ver recompensas',
    onClick: () => navigate('/loyalty/rewards')
  }
});
```

#### Error al Canjear
```javascript
toast.error('No se pudo completar el canje', {
  description: 'No tienes suficientes puntos'
});
```

### Loading States
- Skeleton loaders para cards de recompensas
- Spinner para acciones de canje
- Progress bar para carga de dashboard

---

## 🧩 Componentes Sugeridos

### 1. `LoyaltyBadge`
```tsx
<LoyaltyBadge 
  tier="PLATA" 
  size="large" 
  animated={true}
/>
```

### 2. `PointsDisplay`
```tsx
<PointsDisplay 
  availablePoints={350}
  totalPoints={450}
  expiringPoints={50}
/>
```

### 3. `RewardCard`
```tsx
<RewardCard
  reward={rewardData}
  onRedeem={handleRedeem}
  userCanAfford={true}
/>
```

### 4. `TierProgressBar`
```tsx
<TierProgressBar
  currentTier="PLATA"
  nextTier="ORO"
  monthsRemaining={6}
  totalMonths={12}
/>
```

### 5. `ActivityTimeline`
```tsx
<ActivityTimeline
  activities={activitiesData}
  limit={10}
/>
```

### 6. `RedemptionCodeCard`
```tsx
<RedemptionCodeCard
  code="RWD-B7K9M2P5"
  expirationDate="2025-11-20"
  status="ACTIVE"
/>
```

---

## 🔧 Estado Global (Sugerencia con Zustand/Redux)

```javascript
const useLoyaltyStore = create((set) => ({
  profile: null,
  dashboard: null,
  rewards: [],
  redemptions: [],
  activities: [],
  
  // Actions
  fetchDashboard: async () => {
    const data = await loyaltyApi.getDashboard();
    set({ dashboard: data });
  },
  
  redeemReward: async (rewardId, notes) => {
    const redemption = await loyaltyApi.redeemReward(rewardId, notes);
    // Update profile points
    set((state) => ({
      profile: {
        ...state.profile,
        availablePoints: state.profile.availablePoints - redemption.pointsSpent
      }
    }));
    return redemption;
  },
  
  // ... más acciones
}));
```

---

## 📊 Métricas y Analytics (Opcional)

Eventos a trackear:
- `loyalty_dashboard_viewed`
- `reward_viewed` (con rewardId)
- `reward_redeemed` (con rewardId, pointsSpent)
- `redemption_code_copied`
- `tier_info_viewed`
- `activities_filtered` (con filtro aplicado)

---

## ✅ Checklist de Implementación

### Fase 1: Estructura Básica
- [ ] Crear rutas de navegación
- [ ] Implementar layout principal de fidelización
- [ ] Configurar llamadas a API
- [ ] Manejo de autenticación

### Fase 2: Dashboard
- [ ] Componente de perfil/badge de nivel
- [ ] Display de puntos disponibles
- [ ] Barra de progreso a siguiente nivel
- [ ] Lista de actividades recientes
- [ ] Cards de canjes activos
- [ ] Mensaje motivacional

### Fase 3: Catálogo de Recompensas
- [ ] Grid/lista de recompensas
- [ ] Filtros (disponibles, categoría, nivel)
- [ ] Card de recompensa con indicadores visuales
- [ ] Modal de confirmación de canje
- [ ] Modal de éxito con código
- [ ] Manejo de errores (puntos insuficientes, nivel bajo)

### Fase 4: Mis Canjes
- [ ] Lista de canjes con filtros por estado
- [ ] Card de canje con código copiable
- [ ] Indicadores de estado visual
- [ ] Fechas formateadas

### Fase 5: Historial
- [ ] Tabla/lista de actividades
- [ ] Filtros por tipo y fecha
- [ ] Paginación o infinite scroll
- [ ] Indicadores de estado (activo, cancelado, expirado)

### Fase 6: Info de Niveles
- [ ] Cards comparativos de niveles
- [ ] Timeline visual de progresión
- [ ] Highlight del nivel actual

### Fase 7: Polish
- [ ] Animaciones y transiciones
- [ ] Notificaciones toast
- [ ] Loading states
- [ ] Empty states
- [ ] Error boundaries
- [ ] Responsive design
- [ ] Tests (opcional)

---

## 🚀 Tecnologías Sugeridas

### Core
- **React** 18+ con TypeScript
- **React Router** para navegación
- **Axios** o **Fetch API** para llamadas HTTP

### UI Components
- **shadcn/ui**, **Material-UI**, **Chakra UI**, o **Ant Design**
- **Tailwind CSS** para estilos
- **Framer Motion** para animaciones

### Estado Global
- **Zustand**, **Redux Toolkit**, o **React Query**

### Utilidades
- **date-fns** o **dayjs** para manejo de fechas
- **react-hot-toast** o **sonner** para notificaciones
- **lucide-react** o **react-icons** para iconos

---

## 📝 Ejemplos de Código

### Servicio API
```typescript
// services/loyaltyService.ts
import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/loyalty';

const loyaltyApi = {
  getDashboard: async () => {
    const response = await axios.get(`${BASE_URL}/dashboard`, {
      headers: { Authorization: `Bearer ${getToken()}` }
    });
    return response.data.data;
  },

  getRewards: async (affordable = false) => {
    const endpoint = affordable ? '/rewards/affordable' : '/rewards';
    const response = await axios.get(`${BASE_URL}${endpoint}`, {
      headers: { Authorization: `Bearer ${getToken()}` }
    });
    return response.data.data;
  },

  redeemReward: async (rewardId: number, notes?: string) => {
    const response = await axios.post(
      `${BASE_URL}/redeem`,
      { rewardId, notes },
      { headers: { Authorization: `Bearer ${getToken()}` } }
    );
    return response.data.data;
  },

  getRedemptions: async () => {
    const response = await axios.get(`${BASE_URL}/redemptions`, {
      headers: { Authorization: `Bearer ${getToken()}` }
    });
    return response.data.data;
  },

  getActivities: async () => {
    const response = await axios.get(`${BASE_URL}/activities`, {
      headers: { Authorization: `Bearer ${getToken()}` }
    });
    return response.data.data;
  }
};

export default loyaltyApi;
```

### Componente RewardCard
```tsx
// components/RewardCard.tsx
import { Button, Badge, Card } from '@/components/ui';

interface RewardCardProps {
  reward: {
    idLoyaltyReward: number;
    name: string;
    description: string;
    pointsCost: number;
    rewardType: string;
    minimumTierRequired: string;
    canUserAfford: boolean;
    meetsMinimumTier: boolean;
  };
  onRedeem: (rewardId: number) => void;
}

export const RewardCard = ({ reward, onRedeem }: RewardCardProps) => {
  const canRedeem = reward.canUserAfford && reward.meetsMinimumTier;

  return (
    <Card className={`p-4 ${!canRedeem ? 'opacity-60' : ''}`}>
      <div className="flex justify-between items-start mb-2">
        <h3 className="text-lg font-bold">{reward.name}</h3>
        {!reward.meetsMinimumTier && (
          <Badge variant="secondary">
            Requiere {reward.minimumTierRequired}
          </Badge>
        )}
      </div>
      
      <p className="text-sm text-gray-600 mb-4">{reward.description}</p>
      
      <div className="flex justify-between items-center">
        <div className="text-2xl font-bold text-primary">
          {reward.pointsCost} pts
        </div>
        
        <Button
          onClick={() => onRedeem(reward.idLoyaltyReward)}
          disabled={!canRedeem}
          variant={canRedeem ? 'default' : 'outline'}
        >
          {canRedeem ? '🎁 Canjear' : '🔒 Bloqueado'}
        </Button>
      </div>
    </Card>
  );
};
```

### Página Dashboard
```tsx
// pages/LoyaltyDashboard.tsx
import { useEffect, useState } from 'react';
import loyaltyApi from '@/services/loyaltyService';
import { LoyaltyBadge, PointsDisplay, ActivityTimeline } from '@/components';

export const LoyaltyDashboard = () => {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const data = await loyaltyApi.getDashboard();
      setDashboard(data);
    } catch (error) {
      console.error('Error loading dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Cargando...</div>;
  if (!dashboard) return <div>Error al cargar datos</div>;

  return (
    <div className="container mx-auto p-6">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-500 to-purple-600 rounded-lg p-6 text-white mb-6">
        <h1 className="text-3xl font-bold mb-2">Mi Fidelización</h1>
        <p>{dashboard.motivationalMessage}</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Perfil */}
        <div className="col-span-1">
          <LoyaltyBadge 
            tier={dashboard.profile.currentTier}
            size="large"
          />
          <PointsDisplay
            availablePoints={dashboard.profile.availablePoints}
            totalPoints={dashboard.profile.totalPoints}
            expiringPoints={dashboard.pointsExpiringInNext30Days}
          />
        </div>

        {/* Actividades Recientes */}
        <div className="col-span-2">
          <h2 className="text-xl font-bold mb-4">Actividades Recientes</h2>
          <ActivityTimeline 
            activities={dashboard.recentActivities}
            limit={10}
          />
        </div>
      </div>

      {/* Canjes Activos */}
      {dashboard.activeRedemptions.length > 0 && (
        <div className="mt-6">
          <h2 className="text-xl font-bold mb-4">Tus Canjes Activos</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {dashboard.activeRedemptions.map(redemption => (
              <RedemptionCodeCard key={redemption.idLoyaltyRedemption} {...redemption} />
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
```

---

## 🎁 Bonus: Gamificación Adicional

### Ideas para mejorar la experiencia:

1. **Animación de confetti** al canjear recompensa
2. **Sound effects** (opcional, con toggle on/off)
3. **Badges de logros** especiales:
   - "Primera Recompensa"
   - "100 Clases Asistidas"
   - "Referidor Experto" (5+ referidos)
4. **Leaderboard** (opcional): Top 10 usuarios con más puntos del mes
5. **Retos mensuales**: "Asiste a 20 clases este mes y gana 50 puntos bonus"

---

## 📞 Soporte y Consultas

Si tienes dudas sobre:
- **Respuestas de la API**: Revisa `LOYALTY_API_EXAMPLES.md`
- **Lógica de negocio**: Consulta `LOYALTY_SYSTEM_DOCUMENTATION.md`
- **Problemas técnicos**: Contacta al equipo de backend

---

## ✅ Criterios de Aceptación

- [ ] Usuario puede ver su perfil de fidelización completo
- [ ] Usuario puede explorar el catálogo de recompensas
- [ ] Usuario puede filtrar recompensas por disponibilidad y categoría
- [ ] Usuario puede canjear una recompensa (si tiene puntos suficientes)
- [ ] Usuario recibe un código único tras el canje
- [ ] Usuario puede copiar el código fácilmente
- [ ] Usuario puede ver todos sus canjes (activos, usados, expirados)
- [ ] Usuario puede ver su historial completo de actividades
- [ ] Usuario puede ver información sobre todos los niveles
- [ ] Interface es responsive (mobile, tablet, desktop)
- [ ] Muestra loading states apropiados
- [ ] Maneja errores de forma amigable
- [ ] Muestra notificaciones de puntos próximos a expirar

---

## 🚀 ¡Éxito con la implementación!

Este sistema mejorará significativamente la retención y engagement de los usuarios de FitZone. 💪🎉

**Fecha de entrega**: _A definir_
**Prioridad**: Alta
**Complejidad estimada**: Media-Alta

---

**Documentación del Backend**:
- `docs/LOYALTY_SYSTEM_DOCUMENTATION.md`
- `docs/LOYALTY_API_EXAMPLES.md`

