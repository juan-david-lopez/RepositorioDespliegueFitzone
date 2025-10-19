# 💳 Integración Frontend-Backend con Stripe

## 📋 Resumen

Este documento explica cómo el **frontend de FitZone** se conecta con el **backend que tiene Stripe implementado** para procesar pagos de membresías.

---

## 🏗️ Arquitectura

```
Frontend (Next.js)  →  Backend (Spring Boot/Node.js)  →  Stripe API
     ↓                          ↓                            ↓
[paymentService.ts]    [Stripe Integration]        [Payment Processing]
     ↓                          ↓                            ↓
  [Checkout UI]         [Webhooks Handler]          [Transactions]
```

---

## 📁 Archivo Creado

### `services/paymentService.ts`

Servicio completo para comunicarse con los endpoints de Stripe en el backend.

**Métodos principales:**

1. **`createPaymentIntent()`** - Crea un Payment Intent de Stripe
2. **`processPayment()`** - Procesa un pago completo
3. **`createCheckoutSession()`** - Crea una sesión de Stripe Checkout
4. **`getPaymentStatus()`** - Verifica el estado de un pago
5. **`confirmPayment()`** - Confirma un pago
6. **`getSavedPaymentMethods()`** - Obtiene métodos de pago guardados
7. **`savePaymentMethod()`** - Guarda un método de pago
8. **`deletePaymentMethod()`** - Elimina un método de pago

---

## 🔌 Endpoints Necesarios en el Backend

El backend debe implementar estos endpoints para que el frontend funcione:

### 1. **Crear Payment Intent**
```http
POST /api/v1/payments/create-intent
Content-Type: application/json
Authorization: Bearer {token}

Body:
{
  "amount": 250000,
  "currency": "cop",
  "membershipType": "PREMIUM",
  "userId": 123,
  "description": "Membresía Premium - 1 mes",
  "metadata": {
    "membershipType": "PREMIUM",
    "duration": "1_month"
  }
}

Response:
{
  "success": true,
  "clientSecret": "pi_xxx_secret_xxx",
  "paymentIntentId": "pi_xxx"
}
```

### 2. **Procesar Pago**
```http
POST /api/v1/payments/process
Content-Type: application/json
Authorization: Bearer {token}

Body:
{
  "userId": 123,
  "amount": 250000,
  "paymentMethod": "CREDIT_CARD",
  "paymentMethodId": "pm_xxx", // Stripe Payment Method ID
  "membershipType": "PREMIUM",
  "membershipStartDate": "2025-10-07",
  "membershipEndDate": "2025-11-07",
  "billingInfo": {
    "name": "Juan Pérez",
    "email": "juan@example.com",
    "phone": "+57 300 123 4567",
    "address": "Calle 123 #45-67",
    "city": "Bogotá",
    "country": "Colombia"
  }
}

Response:
{
  "success": true,
  "transactionId": "txn_xxx",
  "receiptId": "receipt_xxx",
  "message": "Pago procesado exitosamente"
}
```

### 3. **Crear Checkout Session**
```http
POST /api/v1/payments/create-checkout-session
Content-Type: application/json
Authorization: Bearer {token}

Body:
{
  "membershipType": "PREMIUM",
  "userId": 123,
  "successUrl": "https://fitzone.com/checkout/success?session_id={CHECKOUT_SESSION_ID}",
  "cancelUrl": "https://fitzone.com/checkout/cancel",
  "billingInfo": {
    "name": "Juan Pérez",
    "email": "juan@example.com"
  }
}

Response:
{
  "success": true,
  "sessionId": "cs_xxx",
  "sessionUrl": "https://checkout.stripe.com/pay/cs_xxx"
}
```

### 4. **Verificar Estado del Pago**
```http
GET /api/v1/payments/{paymentId}/status
Authorization: Bearer {token}

Response:
{
  "success": true,
  "status": "succeeded" | "pending" | "failed" | "canceled"
}
```

### 5. **Confirmar Pago**
```http
POST /api/v1/payments/{paymentIntentId}/confirm
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "Pago confirmado"
}
```

### 6. **Obtener Métodos de Pago Guardados**
```http
GET /api/v1/users/{userId}/payment-methods
Authorization: Bearer {token}

Response:
{
  "success": true,
  "paymentMethods": [
    {
      "id": "pm_xxx",
      "type": "card",
      "last4": "4242",
      "brand": "visa",
      "expiryMonth": 12,
      "expiryYear": 2026
    }
  ]
}
```

### 7. **Guardar Método de Pago**
```http
POST /api/v1/users/{userId}/payment-methods
Content-Type: application/json
Authorization: Bearer {token}

Body:
{
  "paymentMethodId": "pm_xxx"
}

Response:
{
  "success": true,
  "message": "Método de pago guardado"
}
```

### 8. **Eliminar Método de Pago**
```http
DELETE /api/v1/users/{userId}/payment-methods/{paymentMethodId}
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "Método de pago eliminado"
}
```

---

## 🎯 Flujos de Integración

### Opción 1: Payment Intent (Recomendado para control total)

**Flujo:**
1. Usuario llena formulario de pago en frontend
2. Frontend llama `paymentService.createPaymentIntent()`
3. Backend crea Payment Intent en Stripe y retorna `clientSecret`
4. Frontend usa Stripe.js con el `clientSecret` para recolectar datos de tarjeta
5. Stripe procesa el pago
6. Frontend confirma con `paymentService.confirmPayment()`
7. Backend crea recibo y activa membresía

**Código de ejemplo:**

```typescript
import paymentService from '@/services/paymentService'
import { loadStripe } from '@stripe/stripe-js'

const stripePromise = loadStripe(process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY!)

async function handlePayment() {
  // 1. Crear Payment Intent
  const intentResponse = await paymentService.createPaymentIntent({
    amount: 250000,
    membershipType: 'PREMIUM',
    userId: user.id,
    description: 'Membresía Premium - 1 mes'
  })

  if (!intentResponse.success) {
    alert('Error creando intención de pago')
    return
  }

  // 2. Usar Stripe.js para procesar
  const stripe = await stripePromise
  if (!stripe) return

  const { error } = await stripe.confirmCardPayment(intentResponse.clientSecret!, {
    payment_method: {
      card: cardElement, // Stripe Card Element
      billing_details: {
        name: billingName,
        email: billingEmail
      }
    }
  })

  if (error) {
    alert('Error procesando pago: ' + error.message)
    return
  }

  // 3. Confirmar en backend
  await paymentService.confirmPayment(intentResponse.paymentIntentId!)
  
  alert('¡Pago exitoso!')
  router.push('/dashboard')
}
```

### Opción 2: Stripe Checkout (Más simple, menos control)

**Flujo:**
1. Usuario selecciona membresía
2. Frontend llama `paymentService.createCheckoutSession()`
3. Backend crea Checkout Session en Stripe
4. Frontend redirige a `sessionUrl` de Stripe
5. Usuario completa pago en página de Stripe
6. Stripe redirige a `successUrl` o `cancelUrl`
7. Backend recibe webhook de Stripe
8. Backend crea recibo y activa membresía

**Código de ejemplo:**

```typescript
import paymentService from '@/services/paymentService'

async function handleCheckout() {
  const sessionResponse = await paymentService.createCheckoutSession({
    membershipType: 'PREMIUM',
    userId: user.id,
    successUrl: `${window.location.origin}/checkout/success?session_id={CHECKOUT_SESSION_ID}`,
    cancelUrl: `${window.location.origin}/checkout/cancel`,
    billingInfo: {
      name: user.name,
      email: user.email
    }
  })

  if (!sessionResponse.success) {
    alert('Error creando sesión de pago')
    return
  }

  // Redirigir a Stripe Checkout
  window.location.href = sessionResponse.sessionUrl!
}
```

### Opción 3: Procesar Pago Directo (Backend maneja todo)

**Flujo:**
1. Frontend envía toda la información al backend
2. Backend procesa pago con Stripe
3. Backend crea recibo y activa membresía
4. Backend retorna resultado

**Código de ejemplo:**

```typescript
import paymentService from '@/services/paymentService'

async function handleDirectPayment() {
  const paymentResponse = await paymentService.processPayment({
    userId: user.id,
    amount: 250000,
    paymentMethod: 'CREDIT_CARD',
    paymentMethodId: 'pm_xxx', // ID de Stripe Payment Method
    membershipType: 'PREMIUM',
    membershipStartDate: new Date().toISOString(),
    membershipEndDate: addMonths(new Date(), 1).toISOString(),
    billingInfo: {
      name: formData.billingName,
      email: formData.billingEmail,
      phone: formData.phone,
      address: formData.address,
      city: formData.city,
      country: 'Colombia'
    }
  })

  if (!paymentResponse.success) {
    alert('Error procesando pago: ' + paymentResponse.error)
    return
  }

  alert('¡Pago exitoso!')
  router.push(`/dashboard/pagos/${paymentResponse.receiptId}`)
}
```

---

## 🔒 Seguridad

### En el Frontend:
- ✅ **NUNCA envíes datos de tarjeta al backend**
- ✅ Usa Stripe.js o Stripe Elements para manejar datos de tarjeta
- ✅ Solo envía el `payment_method_id` que Stripe genera
- ✅ Valida inputs del usuario
- ✅ Usa HTTPS siempre

### En el Backend:
- ✅ Implementa autenticación JWT
- ✅ Valida que el usuario esté autorizado
- ✅ Usa webhooks de Stripe para confirmar pagos
- ✅ Nunca confíes solo en el frontend
- ✅ Guarda las claves de Stripe en variables de entorno
- ✅ Usa `STRIPE_WEBHOOK_SECRET` para validar webhooks

---

## 🧪 Testing

### Variables de Prueba de Stripe:

```env
# Frontend (.env.local)
NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY=pk_test_xxxxxxxxxxxxx

# Backend
STRIPE_SECRET_KEY=sk_test_xxxxxxxxxxxxx
STRIPE_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxx
```

### Tarjetas de Prueba:

| Tarjeta | Número | Resultado |
|---------|--------|-----------|
| Visa | 4242 4242 4242 4242 | Pago exitoso |
| Visa (decline) | 4000 0000 0000 0002 | Pago rechazado |
| Mastercard | 5555 5555 5555 4444 | Pago exitoso |
| Amex | 3782 822463 10005 | Pago exitoso |

- **CVV:** Cualquier 3 dígitos (ej: 123)
- **Fecha:** Cualquier fecha futura (ej: 12/28)

---

## 📝 Próximos Pasos

### Para completar la integración:

1. **Backend debe implementar los 8 endpoints** listados arriba
2. **Configurar webhooks de Stripe** en el backend:
    - `payment_intent.succeeded`
    - `payment_intent.payment_failed`
    - `charge.refunded`
3. **Actualizar página de checkout** (`app/checkout/page.tsx`) para usar `paymentService`
4. **Agregar Stripe.js** al proyecto:
   ```bash
   pnpm add @stripe/stripe-js
   ```
5. **Configurar variable de entorno:**
   ```env
   NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY=pk_test_xxx
   ```

---

## 🎉 Resumen

✅ **Servicio creado:** `services/paymentService.ts`  
✅ **8 métodos disponibles** para comunicarse con backend  
✅ **3 flujos de integración** documentados  
✅ **Endpoints del backend** especificados  
✅ **Ejemplos de código** incluidos  
✅ **Seguridad** documentada  
✅ **Testing** con tarjetas de prueba

El frontend está **listo para conectarse** con el backend de Stripe. Solo falta que el backend implemente los endpoints y el frontend los use en el checkout.

---

## 📞 Soporte

Si necesitas ayuda con la integración:
- Documentación de Stripe: https://stripe.com/docs
- Stripe Dashboard: https://dashboard.stripe.com
- Webhook Testing: https://stripe.com/docs/webhooks/test
