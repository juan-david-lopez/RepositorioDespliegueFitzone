# ============================================
# 🧪 TEST MANUAL DE LOGIN - RENDER BACKEND
# URL: https://repositoriodesplieguefitzone.onrender.com
# ============================================

# ============================================
# 1️⃣ TEST CON CURL (Windows CMD/PowerShell)
# ============================================

# Test 1: Login con usuario válido
curl -X POST "https://repositoriodesplieguefitzone.onrender.com/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@fitzone.com\",\"password\":\"admin123\"}"

# Test 2: Login con credenciales incorrectas
curl -X POST "https://repositoriodesplieguefitzone.onrender.com/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@fitzone.com\",\"password\":\"wrong_password\"}"

# Test 3: Login con email no existente
curl -X POST "https://repositoriodesplieguefitzone.onrender.com/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"noexiste@fitzone.com\",\"password\":\"test123\"}"

# ============================================
# 2️⃣ TEST CON CURL (Linux/Mac/Git Bash)
# ============================================

# Test 1: Login con usuario válido
curl -X POST "https://repositoriodesplieguefitzone.onrender.com/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@fitzone.com","password":"admin123"}'

# Test 2: Health check del backend
curl "https://repositoriodesplieguefitzone.onrender.com/actuator/health"

# ============================================
# 3️⃣ RESPUESTAS ESPERADAS
# ============================================

# ✅ Login exitoso (200 OK):
# {
#   "success": true,
#   "message": "Login exitoso",
#   "data": {
#     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#     "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#     "user": {
#       "idUser": 1,
#       "email": "admin@fitzone.com",
#       "role": "ADMIN",
#       "membershipType": "ELITE"
#     }
#   }
# }

# ❌ Credenciales incorrectas (401 Unauthorized):
# {
#   "success": false,
#   "message": "Credenciales inválidas",
#   "errors": ["Email o contraseña incorrectos"]
# }

# ❌ Usuario no encontrado (404 Not Found):
# {
#   "success": false,
#   "message": "Usuario no encontrado",
#   "errors": ["El email no está registrado"]
# }

# ============================================
# 4️⃣ TEST CORS DESDE EL NAVEGADOR
# ============================================

# Abre la consola del navegador (F12) en https://front-proyecto-psi.vercel.app
# y ejecuta este código JavaScript:

fetch('https://repositoriodesplieguefitzone.onrender.com/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'admin@fitzone.com',
    password: 'admin123'
  })
})
.then(response => response.json())
.then(data => console.log('✅ Login exitoso:', data))
.catch(error => console.error('❌ Error:', error));

# ============================================
# 5️⃣ CREDENCIALES DE PRUEBA DISPONIBLES
# ============================================

# Usuario ADMIN:
# Email: admin@fitzone.com
# Password: admin123
# Role: ADMIN
# Membership: ELITE

# Usuario ELITE:
# Email: elite@fitzone.com
# Password: elite123
# Role: MEMBER
# Membership: ELITE

# Usuario PREMIUM:
# Email: premium@fitzone.com
# Password: premium123
# Role: MEMBER
# Membership: PREMIUM

# Usuario BASIC:
# Email: basic@fitzone.com
# Password: basic123
# Role: MEMBER
# Membership: BASIC

# ============================================
# 6️⃣ TEST CON POSTMAN
# ============================================

# Método: POST
# URL: https://repositoriodesplieguefitzone.onrender.com/auth/login
# Headers:
#   Content-Type: application/json
# Body (raw JSON):
# {
#   "email": "admin@fitzone.com",
#   "password": "admin123"
# }

# ============================================
# 7️⃣ VERIFICAR ENDPOINTS DISPONIBLES
# ============================================

# Health Check:
curl "https://repositoriodesplieguefitzone.onrender.com/actuator/health"

# Swagger UI (si está habilitado):
# https://repositoriodesplieguefitzone.onrender.com/swagger-ui.html

# API Docs (si está habilitado):
# https://repositoriodesplieguefitzone.onrender.com/v3/api-docs

