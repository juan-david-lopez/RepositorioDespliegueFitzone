# 🔧 Manejo de Usuarios sin Membresía - Frontend

## ⚠️ IMPORTANTE: No Tener Membresía NO es un Error

**Los usuarios recién creados no tienen membresía activa por defecto.** Esto es completamente normal y esperado. El sistema debe manejar este estado como parte del flujo natural de la aplicación, no como un error.

### Estados Normales de un Usuario:
1. ✅ **Usuario nuevo sin membresía** - Estado inicial normal
2. ✅ **Usuario sin ubicación principal** - Debe asignar una sede antes de comprar
3. ✅ **Usuario con membresía activa** - Ha completado el proceso de compra
4. ✅ **Usuario con membresía expirada** - Puede renovar su membresía

## Cambios Implementados en el Backend

El endpoint `/memberships/details/{userId}` ahora devuelve **siempre un 200 OK** con una respuesta estructurada, incluso cuando el usuario no tiene membresía activa.

### Estructura de Respuesta

```typescript
interface MembershipDetailsResponse {
  hasMembership: boolean;
  membershipId?: number;
  userId: number;
  membershipTypeName?: string;
  locationId?: number;
  startDate?: string;
  endDate?: string;
  status?: string;
  message: string;
  needsLocation: boolean;
}
```

## Casos de Uso

### 1. Usuario CON Membresía Activa

**Request:**
```http
GET /memberships/details/22
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "hasMembership": true,
  "membershipId": 15,
  "userId": 22,
  "membershipTypeName": "PREMIUM",
  "locationId": 1,
  "startDate": "2025-10-08",
  "endDate": "2025-11-08",
  "status": "ACTIVE",
  "message": "Membresía activa",
  "needsLocation": false
}
```

### 2. Usuario SIN Membresía (con ubicación asignada)

**Request:**
```http
GET /memberships/details/22
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "hasMembership": false,
  "userId": 22,
  "message": "El usuario no tiene una membresía activa. Puede adquirir una membresía.",
  "needsLocation": false
}
```

### 3. Usuario SIN Membresía y SIN Ubicación

**Request:**
```http
GET /memberships/details/22
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "hasMembership": false,
  "userId": 22,
  "message": "El usuario debe asignar una ubicación principal antes de adquirir una membresía",
  "needsLocation": true
}
```

## Actualización del Frontend

### 1. Actualizar el Servicio TypeScript

```typescript
// membershipManagementService.ts

interface MembershipDetailsResponse {
  hasMembership: boolean;
  membershipId?: number;
  userId: number;
  membershipTypeName?: string;
  locationId?: number;
  startDate?: string;
  endDate?: string;
  status?: string;
  message: string;
  needsLocation: boolean;
}

async getMembershipDetails(userId: number): Promise<MembershipDetailsResponse> {
  console.log(`📡 [MembershipManagement] Getting membership details for user ${userId}`);
  
  const response = await this.request<MembershipDetailsResponse>(
    `/memberships/details/${userId}`,
    { method: 'GET' }
  );
  
  console.log(`✅ [MembershipManagement] Membership details:`, response);
  return response;
}
```

### 2. Actualizar el Hook de Notificaciones

```typescript
// use-membership-notifications.ts

const loadNotifications = async () => {
  try {
    setLoading(true);
    setError(null);

    // 1. Obtener detalles de membresía
    const membershipDetails = await membershipManagementService.getMembershipDetails(userId);
    
    // 2. Verificar si el usuario tiene membresía
    if (!membershipDetails.hasMembership) {
      console.warn('ℹ️ Usuario sin membresía activa:', membershipDetails.message);
      
      // Si necesita ubicación, mostrar mensaje apropiado
      if (membershipDetails.needsLocation) {
        setNotifications([{
          id: 'no-location',
          type: 'warning',
          message: 'Debes asignar una sede principal antes de adquirir una membresía',
          actionUrl: '/profile/location'
        }]);
      } else {
        setNotifications([{
          id: 'no-membership',
          type: 'info',
          message: 'No tienes una membresía activa. ¡Adquiere una ahora!',
          actionUrl: '/memberships/purchase'
        }]);
      }
      
      setLoading(false);
      return;
    }

    // 3. Si tiene membresía, obtener notificaciones del servidor
    const notificationsData = await membershipNotificationService.getUserNotifications(userId);
    setNotifications(notificationsData);
    
  } catch (error) {
    console.error('Error loading notifications:', error);
    setError('No se pudieron cargar las notificaciones');
  } finally {
    setLoading(false);
  }
};
```

### 3. Componente de UI para Mostrar Estados

```typescript
// MembershipStatus.tsx

const MembershipStatus: React.FC<{ userId: number }> = ({ userId }) => {
  const [details, setDetails] = useState<MembershipDetailsResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDetails = async () => {
      try {
        const data = await membershipManagementService.getMembershipDetails(userId);
        setDetails(data);
      } catch (error) {
        console.error('Error loading membership:', error);
      } finally {
        setLoading(false);
      }
    };
    
    loadDetails();
  }, [userId]);

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!details) {
    return <ErrorMessage>No se pudo cargar la información</ErrorMessage>;
  }

  // Usuario sin membresía
  if (!details.hasMembership) {
    return (
      <Card variant="warning">
        <Icon name="alert-circle" />
        <h3>Sin Membresía Activa</h3>
        <p>{details.message}</p>
        
        {details.needsLocation ? (
          <Button onClick={() => navigate('/profile/location')}>
            Asignar Sede Principal
          </Button>
        ) : (
          <Button onClick={() => navigate('/memberships/purchase')}>
            Adquirir Membresía
          </Button>
        )}
      </Card>
    );
  }

  // Usuario con membresía
  return (
    <Card variant="success">
      <Icon name="check-circle" />
      <h3>Membresía {details.membershipTypeName}</h3>
      <p>Estado: {details.status}</p>
      <p>Válida hasta: {formatDate(details.endDate)}</p>
      
      <Button onClick={() => navigate('/memberships/renew')}>
        Renovar Membresía
      </Button>
    </Card>
  );
};
```

## Flujo de Compra de Membresía

### Paso 1: Verificar Ubicación

```typescript
const checkUserLocation = async (userId: number): Promise<boolean> => {
  const details = await membershipManagementService.getMembershipDetails(userId);
  
  if (details.needsLocation) {
    // Redirigir a selección de sede
    navigate('/profile/location');
    return false;
  }
  
  return true;
};
```

### Paso 2: Proceso de Pago

```typescript
const purchaseMembership = async (membershipType: string) => {
  // 1. Verificar ubicación
  const hasLocation = await checkUserLocation(userId);
  if (!hasLocation) return;
  
  // 2. Crear Payment Intent
  const paymentIntent = await fetch('/api/v1/payments/create-intent', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      amount: getMembershipPrice(membershipType),
      currency: 'cop',
      membershipType: membershipType,
      userId: userId,
      description: `Membresía ${membershipType} - 1 mes`
    })
  });
  
  // 3. Confirmar pago con Stripe
  const { error } = await stripe.confirmCardPayment(
    paymentIntent.clientSecret,
    {
      payment_method: {
        card: cardElement,
        billing_details: { name, email }
      }
    }
  );
  
  if (!error) {
    // 4. Recargar detalles de membresía
    const newDetails = await membershipManagementService.getMembershipDetails(userId);
    console.log('✅ Membresía activada:', newDetails);
  }
};
```

## Endpoints Adicionales Útiles

### Asignar Ubicación Principal

```http
PATCH /api/v1/users/{userId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "mainLocationId": 1
}
```

### Verificar Estado de Membresía

```http
GET /memberships/status/{userId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "active": false,
  "status": "EXPIRED",
  "message": "La membresía ha expirado"
}
```

## Mensajes de Error Eliminados

❌ **ANTES:** Error 404 con mensaje confuso
```json
{
  "error": "NO_MEMBERSHIP_FOUND",
  "message": "El usuario no tiene una membresía activa",
  "status": 404
}
```

✅ **AHORA:** Respuesta 200 OK estructurada
```json
{
  "hasMembership": false,
  "userId": 22,
  "message": "El usuario no tiene una membresía activa. Puede adquirir una membresía.",
  "needsLocation": false
}
```

## Testing

### Probar con Usuario sin Membresía

```bash
curl -X GET "http://localhost:8080/memberships/details/22" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Probar Asignación de Ubicación

```bash
curl -X PATCH "http://localhost:8080/api/v1/users/22" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"mainLocationId": 1}'
```

### Probar Compra de Membresía

Ver archivo: `SOLUCION_USUARIO_SIN_MEMBRESIA.md`

## Resumen de Cambios

1. ✅ **Backend devuelve 200 OK** siempre (no más 404)
2. ✅ **Respuesta estructurada** con campo `hasMembership`
3. ✅ **Detecta si falta ubicación** con campo `needsLocation`
4. ✅ **Mensajes claros** para guiar al usuario
5. ✅ **Frontend puede distinguir** entre diferentes estados sin errores

## Beneficios

- **Mejor UX**: El usuario sabe exactamente qué hacer
- **Menos errores**: No más excepciones en el frontend
- **Código más limpio**: Manejo de estados sin try-catch
- **Flujo guiado**: El sistema indica los pasos a seguir
