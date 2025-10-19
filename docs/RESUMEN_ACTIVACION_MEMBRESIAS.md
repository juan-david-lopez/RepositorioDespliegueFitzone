# 🔧 Resumen de Implementación - Activación Automática de Membresías

## ✅ Cambios Implementados

### **1. Actualizado `GenericResponse.java`**
**Archivo:** `src/main/java/co/edu/uniquindio/FitZone/dto/response/GenericResponse.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponse {
    private boolean success;
    private String message;
    private String error;
    private Map<String, Object> data; // ✅ AGREGADO
}
```

**Cambio:** Agregado campo `data` para poder enviar información adicional en las respuestas (como detalles de la membresía creada).

---

### **2. Actualizado `UserResponse.java`**
**Archivo:** `src/main/java/co/edu/uniquindio/FitZone/dto/response/UserResponse.java`

```java
public record UserResponse(
        @JsonProperty("idUser")
        Long idUser,
        String name,
        String email,
        String role,
        String membershipType, // ✅ AGREGADO
        Boolean isActive,      // ✅ AGREGADO
        String phoneNumber,    // ✅ AGREGADO
        String mainLocation    // ✅ AGREGADO
) {
    // Método estático para crear respuesta con membresía
    public static UserResponse fromUser(User user, String membershipType) {
        // ... lógica implementada
    }
}
```

**Cambios:**
- ✅ Agregado campo `membershipType` - Tipo de membresía activa del usuario
- ✅ Agregado campo `isActive` - Estado del usuario
- ✅ Agregado campo `phoneNumber` - Teléfono del usuario
- ✅ Agregado campo `mainLocation` - Nombre de la sede principal
- ✅ Agregado método `fromUser(User user, String membershipType)` para incluir la membresía

---

### **3. Actualizado `UserServiceImpl.java`**
**Archivo:** `src/main/java/co/edu/uniquindio/FitZone/service/impl/UserServiceImpl.java`

#### **3.1 Inyección de IMembershipService**
```java
private final IMembershipService membershipService; // ✅ AGREGADO

public UserServiceImpl(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder, 
                      LocationRepository locationRepository, 
                      IMembershipService membershipService) { // ✅ AGREGADO
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.locationRepository = locationRepository;
    this.membershipService = membershipService; // ✅ AGREGADO
}
```

#### **3.2 Actualizado método `getUserById()`**
```java
@Override
public UserResponse getUserById(Long idUser) {
    User user = userRepository.findById(idUser)
            .orElseThrow(() -> new UserNotFoundException("El id ingresado no existe"));

    // ✅ OBTENER MEMBRESÍA ACTIVA DEL USUARIO
    String membershipType = null;
    try {
        MembershipResponse membership = membershipService.getMembershipByUserId(idUser);
        if (membership != null && "ACTIVE".equalsIgnoreCase(membership.status())) {
            membershipType = membership.membershipTypeName();
            logger.debug("Usuario tiene membresía activa: {}", membershipType);
        } else {
            logger.debug("Usuario no tiene membresía activa - ID: {}", idUser);
        }
    } catch (Exception e) {
        logger.debug("Usuario {} no tiene membresía: {}", idUser, e.getMessage());
        membershipType = null;
    }

    // ✅ USAR EL NUEVO MÉTODO fromUser QUE INCLUYE MEMBERSHIPTYPE
    return UserResponse.fromUser(user, membershipType);
}
```

**Cambio:** Ahora consulta la membresía activa del usuario y la incluye en la respuesta.

---

### **4. Agregado Endpoint para Activar Membresía SIN Webhook**
**Archivo:** `src/main/java/co/edu/uniquindio/FitZone/controller/PaymentController.java`

```java
/**
 * Activa la membresía después de verificar que el pago fue exitoso.
 * Este endpoint NO requiere webhook. El frontend llama aquí después de confirmar el pago.
 * POST /api/v1/payments/{paymentIntentId}/activate-membership
 */
@PostMapping("/{paymentIntentId}/activate-membership")
@PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
public ResponseEntity<?> activateMembershipAfterPayment(
        @PathVariable String paymentIntentId,
        @RequestParam Long userId,
        @RequestParam String membershipType) {
    // ... lógica implementada
}
```

---

### **5. Implementado `activateMembershipAfterPayment()` en StripePaymentServiceImpl**
**Archivo:** `src/main/java/co/edu/uniquindio/FitZone/service/impl/StripePaymentServiceImpl.java`

```java
@Override
@Transactional
public GenericResponse activateMembershipAfterPayment(
        String paymentIntentId, Long userId, String membershipType) throws Exception {
    
    // 1. Validar que el pago existe y fue exitoso en Stripe
    PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
    
    if (!"succeeded".equals(paymentIntent.getStatus())) {
        return GenericResponse.builder()
                .success(false)
                .error("El pago no fue completado exitosamente")
                .build();
    }

    // 2. Validar usuario y ubicación principal
    UserBase user = userRepository.findById(userId)
            .orElseThrow(() -> new Exception("Usuario no encontrado"));
    
    if (user.getMainLocation() == null) {
        return GenericResponse.builder()
                .success(false)
                .error("El usuario no tiene una ubicación principal asignada")
                .build();
    }

    // 3. Crear la membresía
    CreateMembershipRequest membershipRequest = new CreateMembershipRequest(
            userId,
            membershipTypeEntity.getIdMembershipType(),
            user.getMainLocation().getIdLocation(),
            paymentIntentId
    );

    MembershipResponse membership = membershipService.createMembership(membershipRequest);

    // 4. Retornar éxito con detalles
    return GenericResponse.builder()
            .success(true)
            .message("¡Membresía activada exitosamente!")
            .data(Map.of(
                    "membershipId", membership.id(),
                    "transactionId", paymentIntentId,
                    "membershipType", membershipType,
                    "startDate", membership.startDate(),
                    "endDate", membership.endDate()
            ))
            .build();
}
```

---

### **6. Agregado método a la interfaz**
**Archivo:** `src/main/java/co/edu/uniquindio/FitZone/service/interfaces/IStripePaymentService.java`

```java
/**
 * Activa la membresía después de verificar que el pago fue exitoso.
 * Este método NO requiere webhook - se llama directamente desde el frontend.
 */
GenericResponse activateMembershipAfterPayment(
        String paymentIntentId, Long userId, String membershipType) throws Exception;
```

---

### **7. Implementado Webhook Completo (Opcional)**
**Archivo:** `src/main/java/co/edu/uniquindio/FitZone/controller/StripeWebhookController.java`

```java
private void handlePaymentIntentSucceeded(Event event) {
    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
            .getObject()
            .orElse(null);

    String userIdStr = paymentIntent.getMetadata().get("userId");
    String membershipTypeStr = paymentIntent.getMetadata().get("membershipType");

    // Validar usuario y crear membresía automáticamente
    UserBase user = userBaseRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    if (user.getMainLocation() == null) {
        logger.error("El usuario no tiene ubicación principal");
        return;
    }

    // Crear la membresía
    CreateMembershipRequest membershipRequest = new CreateMembershipRequest(
            userId,
            membershipType.getIdMembershipType(),
            user.getMainLocation().getIdLocation(),
            paymentIntent.getId()
    );

    MembershipResponse membership = membershipService.createMembership(membershipRequest);
    
    logger.info("✅ Membresía activada exitosamente - Usuario: {}, Membership ID: {}", 
                userId, membership.id());
}
```

---

## 🚀 Cómo Funciona el Flujo Completo

### **Opción A: SIN Webhook (Recomendado para desarrollo)**

```
1. Usuario paga con Stripe desde el frontend
   ↓
2. Frontend recibe confirmación de Stripe
   ↓
3. Frontend llama: POST /api/v1/payments/{paymentIntentId}/activate-membership
   ↓
4. Backend verifica el pago en Stripe API
   ↓
5. Backend crea y activa la membresía ✅
   ↓
6. Backend devuelve UserResponse con membershipType incluido
   ↓
7. Frontend hace GET /api/v1/users/{userId}
   ↓
8. Respuesta incluye membershipType: "BASIC" ✅
```

### **Opción B: CON Webhook**

```
1. Usuario paga con Stripe
   ↓
2. Stripe confirma el pago
   ↓
3. Stripe envía webhook: payment_intent.succeeded
   ↓
4. Backend recibe webhook y crea membresía automáticamente ✅
   ↓
5. Frontend hace GET /api/v1/users/{userId}
   ↓
6. Respuesta incluye membershipType: "BASIC" ✅
```

---

## 📡 Endpoints Actualizados

### **1. GET /api/v1/users/{id}**

**Respuesta ANTES:**
```json
{
  "idUser": 22,
  "id": 22,
  "name": "Eliana Garcia",
  "email": "eliana@osxofulk.com",
  "role": "MEMBER"
}
```

**Respuesta AHORA (CON membresía activa):**
```json
{
  "idUser": 22,
  "id": 22,
  "name": "Eliana Garcia",
  "email": "eliana@osxofulk.com",
  "role": "MEMBER",
  "membershipType": "BASIC",     // ✅ NUEVO
  "isActive": true,              // ✅ NUEVO
  "phoneNumber": "+57 300 1234567", // ✅ NUEVO
  "mainLocation": "Sede Centro"  // ✅ NUEVO
}
```

**Respuesta AHORA (SIN membresía activa):**
```json
{
  "idUser": 22,
  "id": 22,
  "name": "Eliana Garcia",
  "email": "eliana@osxofulk.com",
  "role": "MEMBER",
  "membershipType": null,        // ✅ null si no tiene
  "isActive": true,
  "phoneNumber": "+57 300 1234567",
  "mainLocation": "Sede Centro"
}
```

### **2. POST /api/v1/payments/{paymentIntentId}/activate-membership**

**Request:**
```
POST /api/v1/payments/pi_3abc123/activate-membership?userId=22&membershipType=BASIC
Authorization: Bearer {token}
```

**Response Exitosa:**
```json
{
  "success": true,
  "message": "¡Membresía activada exitosamente! Ya puedes disfrutar del gimnasio.",
  "data": {
    "membershipId": 456,
    "transactionId": "pi_3abc123",
    "membershipType": "BASIC",
    "startDate": "2025-10-09T00:00:00",
    "endDate": "2025-11-09T23:59:59"
  },
  "error": null
}
```

**Response de Error:**
```json
{
  "success": false,
  "message": null,
  "data": null,
  "error": "El pago no fue completado exitosamente. Estado: requires_payment_method"
}
```

---

## 🧪 Cómo Probar

### **Paso 1: Reiniciar la Aplicación**

Desde IntelliJ IDEA:
1. Detén la aplicación si está corriendo
2. Click derecho en `FitZoneApplication.java`
3. Selecciona "Run 'FitZoneApplication'"

O desde terminal (si Maven está configurado):
```bash
mvn spring-boot:run
```

### **Paso 2: Verificar que el usuario tiene ubicación principal**

```bash
GET http://localhost:8080/api/v1/users/22
Authorization: Bearer {token}
```

Debe mostrar `"mainLocation": "Sede Centro"` o similar.

### **Paso 3: Simular un pago exitoso**

```bash
# Crear Payment Intent
POST http://localhost:8080/api/v1/payments/create-intent
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": 22,
  "membershipType": "BASIC",
  "amount": 50000,
  "currency": "cop"
}

# Respuesta: { "paymentIntentId": "pi_xxxxx", "clientSecret": "..." }
```

### **Paso 4: Activar membresía (simular que Stripe confirmó el pago)**

```bash
POST http://localhost:8080/api/v1/payments/pi_xxxxx/activate-membership?userId=22&membershipType=BASIC
Authorization: Bearer {token}
```

### **Paso 5: Verificar que la membresía está activa**

```bash
GET http://localhost:8080/api/v1/users/22
Authorization: Bearer {token}
```

**Respuesta esperada:**
```json
{
  "membershipType": "BASIC",  // ✅ Ahora debe aparecer
  ...
}
```

---

## ⚠️ Problemas Conocidos y Soluciones

### **Problema 1: Error de compilación en GenericResponse.data()**

**Síntoma:**
```
Cannot resolve method 'data' in 'GenericResponseBuilder'
```

**Solución:**
1. Desde IntelliJ IDEA: `Build > Rebuild Project`
2. O ejecutar: `mvn clean compile`
3. Esto regenerará las clases Lombok con el nuevo campo `data`

### **Problema 2: Usuario sin ubicación principal**

**Síntoma:**
```json
{
  "success": false,
  "error": "El usuario no tiene una ubicación principal asignada"
}
```

**Solución:**
Asignar ubicación principal al usuario:
```bash
# Consultar ubicaciones disponibles
GET http://localhost:8080/api/v1/locations

# Actualizar usuario con ubicación principal
PATCH http://localhost:8080/api/v1/users/22
{
  "mainLocationId": 1
}
```

### **Problema 3: "Usuario no tiene membresía" en logs**

**Síntoma:**
```
Usuario 22 no tiene membresía: El usuario no tiene una membresía activa
```

**Esto es NORMAL** si el usuario realmente no tiene membresía. La respuesta simplemente tendrá:
```json
{
  "membershipType": null
}
```

---

## 📝 Resumen de lo Implementado

✅ **GenericResponse** - Agregado campo `data` para información adicional
✅ **UserResponse** - Agregado `membershipType`, `isActive`, `phoneNumber`, `mainLocation`
✅ **UserServiceImpl** - Inyectado `IMembershipService` y actualizado `getUserById()`
✅ **PaymentController** - Agregado endpoint `/activate-membership` (SIN webhook)
✅ **StripePaymentServiceImpl** - Implementado `activateMembershipAfterPayment()`
✅ **IStripePaymentService** - Agregado método a la interfaz
✅ **StripeWebhookController** - Implementado activación automática con webhook (opcional)

---

## 🎯 Resultado Final

**ANTES del pago:**
```json
GET /api/v1/users/22
{
  "membershipType": null
}
```

**DESPUÉS del pago:**
```json
POST /api/v1/payments/pi_xxx/activate-membership
{
  "success": true,
  "message": "¡Membresía activada exitosamente!"
}

GET /api/v1/users/22
{
  "membershipType": "BASIC"  // ✅ ACTIVADA
}
```

---

**Fecha:** 2025-10-09  
**Estado:** ✅ IMPLEMENTACIÓN COMPLETA

