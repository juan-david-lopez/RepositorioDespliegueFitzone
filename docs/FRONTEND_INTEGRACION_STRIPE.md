# 💳 Integración de Pagos con Stripe - Guía para Frontend

## 📋 Resumen

Este documento contiene **toda la información necesaria** para implementar el sistema de pagos con Stripe desde el frontend. No se requiere configuración de webhooks.

---

## 🎯 Información Importante

### **Claves de Stripe**

```javascript
// Clave Pública de Stripe (Frontend)
const STRIPE_PUBLIC_KEY = 'pk_test_51RziwdBrYtkodFY5...'; // Solicitar al equipo backend

// URL del Backend
const API_BASE_URL = 'http://localhost:8080'; // Cambiar en producción
```

### **Tipos de Membresía Disponibles**

| Tipo | Nombre | Descripción |
|------|--------|-------------|
| `BASIC` | Básica | Acceso básico al gimnasio |
| `PREMIUM` | Premium | Acceso completo + clases grupales |
| `VIP` | VIP | Todo incluido + entrenamiento personal |

---

## 🚀 Implementación Paso a Paso

### **Paso 1: Instalar Dependencias**

```bash
npm install @stripe/stripe-js @stripe/react-stripe-js
```

### **Paso 2: Configurar Stripe Provider**

```jsx
// src/App.jsx o src/main.jsx
import { Elements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';

const stripePromise = loadStripe('pk_test_51RziwdBrYtkodFY5...'); // Tu clave pública

function App() {
  return (
    <Elements stripe={stripePromise}>
      {/* Tu aplicación */}
    </Elements>
  );
}
```

### **Paso 3: Crear el Componente de Pago**

```jsx
// src/components/PaymentForm.jsx
import React, { useState } from 'react';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';

const PaymentForm = ({ userId, membershipType, amount }) => {
  const stripe = useStripe();
  const elements = useElements();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!stripe || !elements) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // 1️⃣ CREAR PAYMENT INTENT
      console.log('🔄 Creando Payment Intent...');
      const intentResponse = await fetch('http://localhost:8080/api/v1/payments/create-intent', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}` // Tu token JWT
        },
        body: JSON.stringify({
          userId: userId,
          membershipType: membershipType, // "BASIC", "PREMIUM" o "VIP"
          amount: amount, // En pesos colombianos (ej: 50000)
          currency: 'cop'
        })
      });

      if (!intentResponse.ok) {
        throw new Error('Error al crear Payment Intent');
      }

      const { clientSecret, paymentIntentId } = await intentResponse.json();
      console.log('✅ Payment Intent creado:', paymentIntentId);

      // 2️⃣ CONFIRMAR PAGO CON STRIPE
      console.log('💳 Procesando pago con Stripe...');
      const { error: stripeError, paymentIntent } = await stripe.confirmCardPayment(
        clientSecret,
        {
          payment_method: {
            card: elements.getElement(CardElement),
            billing_details: {
              name: 'Nombre del Usuario', // Obtener del estado/contexto
              email: 'usuario@email.com'  // Obtener del estado/contexto
            }
          }
        }
      );

      if (stripeError) {
        console.error('❌ Error de Stripe:', stripeError.message);
        setError(stripeError.message);
        setLoading(false);
        return;
      }

      // 3️⃣ ACTIVAR MEMBRESÍA EN EL BACKEND
      if (paymentIntent.status === 'succeeded') {
        console.log('✅ Pago exitoso, activando membresía...');
        
        const activationResponse = await fetch(
          `http://localhost:8080/api/v1/payments/${paymentIntentId}/activate-membership?userId=${userId}&membershipType=${membershipType}`,
          {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          }
        );

        if (!activationResponse.ok) {
          throw new Error('Error al activar membresía');
        }

        const result = await activationResponse.json();

        if (result.success) {
          console.log('🎉 Membresía activada:', result);
          setSuccess(true);
          
          // Redirigir después de 2 segundos
          setTimeout(() => {
            window.location.href = '/dashboard';
          }, 2000);
        } else {
          setError(result.error);
        }
      }

    } catch (err) {
      console.error('❌ Error general:', err);
      setError(err.message || 'Ocurrió un error al procesar el pago');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="success-message">
        <h2>🎉 ¡Pago Exitoso!</h2>
        <p>Tu membresía ha sido activada. Redirigiendo al dashboard...</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="payment-form">
      <h3>Información de Pago</h3>
      
      <div className="card-element-container">
        <CardElement
          options={{
            style: {
              base: {
                fontSize: '16px',
                color: '#424770',
                '::placeholder': {
                  color: '#aab7c4',
                },
              },
              invalid: {
                color: '#9e2146',
              },
            },
          }}
        />
      </div>

      {error && (
        <div className="error-message" style={{ color: 'red', marginTop: '10px' }}>
          {error}
        </div>
      )}

      <button 
        type="submit" 
        disabled={!stripe || loading}
        className="pay-button"
      >
        {loading ? 'Procesando...' : `Pagar ${amount.toLocaleString('es-CO')} COP`}
      </button>
    </form>
  );
};

export default PaymentForm;
```

### **Paso 4: Usar el Componente**

```jsx
// src/pages/CheckoutPage.jsx
import React from 'react';
import PaymentForm from '../components/PaymentForm';

const CheckoutPage = () => {
  // Obtener estos datos del contexto, props o estado global
  const userId = 123; // ID del usuario actual
  const membershipType = 'BASIC'; // Tipo seleccionado por el usuario
  const amount = 50000; // Precio en COP

  return (
    <div className="checkout-page">
      <h1>Checkout</h1>
      
      <div className="membership-summary">
        <h2>Resumen de tu membresía</h2>
        <p>Tipo: {membershipType}</p>
        <p>Precio: ${amount.toLocaleString('es-CO')} COP</p>
      </div>

      <PaymentForm 
        userId={userId}
        membershipType={membershipType}
        amount={amount}
      />
    </div>
  );
};

export default CheckoutPage;
```

---

## 📡 Endpoints del Backend

### **1. Crear Payment Intent**

```javascript
// POST /api/v1/payments/create-intent
const response = await fetch('http://localhost:8080/api/v1/payments/create-intent', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    userId: 123,
    membershipType: 'BASIC', // "BASIC", "PREMIUM" o "VIP"
    amount: 50000, // En pesos colombianos
    currency: 'cop'
  })
});

// Respuesta:
// {
//   "paymentIntentId": "pi_3abc123def456",
//   "clientSecret": "pi_3abc123def456_secret_xyz789",
//   "amount": 50000,
//   "currency": "cop",
//   "status": "requires_payment_method"
// }
```

### **2. Activar Membresía (Después del Pago)**

```javascript
// POST /api/v1/payments/{paymentIntentId}/activate-membership
const response = await fetch(
  `http://localhost:8080/api/v1/payments/${paymentIntentId}/activate-membership?userId=${userId}&membershipType=${membershipType}`,
  {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  }
);

// Respuesta Exitosa:
// {
//   "success": true,
//   "message": "¡Membresía activada exitosamente! Ya puedes disfrutar del gimnasio.",
//   "data": {
//     "membershipId": 456,
//     "transactionId": "pi_3abc123def456",
//     "membershipType": "BASIC",
//     "startDate": "2025-10-09T00:00:00",
//     "endDate": "2025-11-09T23:59:59"
//   },
//   "error": null
// }

// Respuesta de Error:
// {
//   "success": false,
//   "message": null,
//   "data": null,
//   "error": "El pago no fue completado exitosamente. Estado: requires_payment_method"
// }
```

---

## 🎨 Estilos CSS Básicos

```css
/* src/components/PaymentForm.css */
.payment-form {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-element-container {
  padding: 15px;
  border: 1px solid #ccc;
  border-radius: 4px;
  margin: 20px 0;
  background: white;
}

.pay-button {
  width: 100%;
  padding: 12px;
  background: #5469d4;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 20px;
}

.pay-button:hover:not(:disabled) {
  background: #4355c7;
}

.pay-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.error-message {
  color: #e74c3c;
  padding: 10px;
  background: #ffe6e6;
  border-radius: 4px;
  margin-top: 10px;
}

.success-message {
  text-align: center;
  padding: 40px;
  background: #d4edda;
  border-radius: 8px;
  color: #155724;
}

.success-message h2 {
  margin: 0 0 10px 0;
}
```

---

## 🧪 Testing con Tarjetas de Prueba

Stripe proporciona tarjetas de prueba para desarrollo:

### **Pago Exitoso ✅**
```
Número: 4242 4242 4242 4242
Fecha: Cualquier fecha futura (ej: 12/25)
CVC: Cualquier 3 dígitos (ej: 123)
ZIP: Cualquier código
```

### **Pago Fallido ❌**
```
Número: 4000 0000 0000 0002
Fecha: 12/25
CVC: 123
```

### **Requiere Autenticación 3D Secure 🔐**
```
Número: 4000 0025 0000 3155
Fecha: 12/25
CVC: 123
```

---

## 🔐 Manejo de Autenticación

### **Obtener el Token JWT**

```javascript
// Después del login
const loginResponse = await fetch('http://localhost:8080/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});

const { token } = await loginResponse.json();

// Guardar en localStorage
localStorage.setItem('token', token);

// Usar en las peticiones
headers: {
  'Authorization': `Bearer ${token}`
}
```

---

## 🚨 Manejo de Errores

### **Errores Comunes y Soluciones**

| Error | Causa | Solución |
|-------|-------|----------|
| "No se pudo verificar el pago" | PaymentIntent ID inválido | Verificar que se está enviando el ID correcto |
| "El pago no fue completado" | Stripe no confirmó el pago | Verificar que `paymentIntent.status === 'succeeded'` |
| "Usuario no encontrado" | userId incorrecto | Verificar que el ID del usuario es correcto |
| "Usuario sin ubicación principal" | Falta main_location | El usuario debe seleccionar una sede al registrarse |
| "Unauthorized" | Token inválido o expirado | Renovar el token o hacer login nuevamente |

### **Código de Manejo de Errores**

```javascript
try {
  const response = await fetch(url, options);
  
  if (response.status === 401) {
    // Token expirado - redirigir al login
    localStorage.removeItem('token');
    window.location.href = '/login';
    return;
  }
  
  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.error || 'Error en la petición');
  }
  
  const data = await response.json();
  return data;
  
} catch (error) {
  console.error('Error:', error);
  // Mostrar mensaje al usuario
  setError(error.message);
}
```

---

## 📱 Ejemplo Completo TypeScript

```typescript
// src/components/PaymentForm.tsx
import React, { useState, FormEvent } from 'react';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';

interface PaymentFormProps {
  userId: number;
  membershipType: 'BASIC' | 'PREMIUM' | 'VIP';
  amount: number;
}

interface PaymentIntentResponse {
  clientSecret: string;
  paymentIntentId: string;
  amount: number;
  currency: string;
  status: string;
}

interface ActivationResponse {
  success: boolean;
  message: string | null;
  data?: {
    membershipId: number;
    transactionId: string;
    membershipType: string;
    startDate: string;
    endDate: string;
  };
  error: string | null;
}

const PaymentForm: React.FC<PaymentFormProps> = ({ userId, membershipType, amount }) => {
  const stripe = useStripe();
  const elements = useElements();
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    if (!stripe || !elements) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // 1. Crear Payment Intent
      const intentResponse = await fetch('http://localhost:8080/api/v1/payments/create-intent', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          userId,
          membershipType,
          amount,
          currency: 'cop'
        })
      });

      if (!intentResponse.ok) {
        throw new Error('Error al crear Payment Intent');
      }

      const { clientSecret, paymentIntentId }: PaymentIntentResponse = await intentResponse.json();

      // 2. Confirmar pago
      const cardElement = elements.getElement(CardElement);
      if (!cardElement) {
        throw new Error('CardElement no encontrado');
      }

      const { error: stripeError, paymentIntent } = await stripe.confirmCardPayment(
        clientSecret,
        {
          payment_method: {
            card: cardElement
          }
        }
      );

      if (stripeError) {
        setError(stripeError.message || 'Error al procesar el pago');
        setLoading(false);
        return;
      }

      // 3. Activar membresía
      if (paymentIntent && paymentIntent.status === 'succeeded') {
        const activationResponse = await fetch(
          `http://localhost:8080/api/v1/payments/${paymentIntentId}/activate-membership?userId=${userId}&membershipType=${membershipType}`,
          {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          }
        );

        if (!activationResponse.ok) {
          throw new Error('Error al activar membresía');
        }

        const result: ActivationResponse = await activationResponse.json();

        if (result.success) {
          setSuccess(true);
          setTimeout(() => {
            window.location.href = '/dashboard';
          }, 2000);
        } else {
          setError(result.error || 'Error desconocido');
        }
      }

    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al procesar el pago');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="success-message">
        <h2>🎉 ¡Pago Exitoso!</h2>
        <p>Tu membresía ha sido activada. Redirigiendo...</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="payment-form">
      <h3>Información de Pago</h3>
      
      <div className="card-element-container">
        <CardElement />
      </div>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      <button type="submit" disabled={!stripe || loading}>
        {loading ? 'Procesando...' : `Pagar ${amount.toLocaleString('es-CO')} COP`}
      </button>
    </form>
  );
};

export default PaymentForm;
```

---

## 📞 Contacto y Soporte

Si tienes problemas con la integración:

1. **Revisa los logs del navegador** (Console de DevTools)
2. **Verifica que el backend esté corriendo** en http://localhost:8080
3. **Confirma que tienes un token JWT válido**
4. **Usa las tarjetas de prueba de Stripe**

### **Logs Útiles para Debugging**

```javascript
console.log('🔍 User ID:', userId);
console.log('🔍 Membership Type:', membershipType);
console.log('🔍 Amount:', amount);
console.log('🔍 Token:', localStorage.getItem('token') ? 'Presente' : 'Faltante');
console.log('🔍 Payment Intent ID:', paymentIntentId);
console.log('🔍 Payment Status:', paymentIntent.status);
```

---

## ✅ Checklist de Implementación

- [ ] Instalar dependencias de Stripe
- [ ] Configurar Stripe Provider con la clave pública
- [ ] Crear componente PaymentForm
- [ ] Implementar formulario con CardElement
- [ ] Llamar a `/create-intent` al iniciar el pago
- [ ] Confirmar pago con `stripe.confirmCardPayment()`
- [ ] Llamar a `/activate-membership` después del pago exitoso
- [ ] Manejar errores y mostrar mensajes al usuario
- [ ] Probar con tarjetas de prueba
- [ ] Redirigir al dashboard después del éxito

---

## 🎉 ¡Listo!

Con esta guía tienes todo lo necesario para implementar el sistema de pagos. El backend ya está completamente configurado y listo para recibir tus peticiones.

**No se requiere configuración de webhook** - todo funciona directamente desde el frontend.

---

**Última actualización:** 2025-10-09  
**Versión Backend:** Compatible con todas las versiones  
**Contacto Backend:** [Tu equipo backend]

