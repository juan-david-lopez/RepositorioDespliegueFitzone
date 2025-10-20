# 🚀 GUÍA DE DESPLIEGUE EN RENDER

## ✅ CHECKLIST PRE-DESPLIEGUE

### 1. Archivos Listos
- [x] `render.yaml` - Configuración de servicios
- [x] `Dockerfile` - Imagen Docker optimizada
- [x] `.dockerignore` - Optimización de build
- [x] `application-prod.properties` - Configuración de producción

### 2. Variables de Entorno Requeridas

Debes configurar estas variables en el **Dashboard de Render** (Settings > Environment):

#### 🔐 Seguridad (JWT)
```
JWT_SECRET=tu_secret_key_super_seguro_min_64_caracteres
JWT_EXPIRATION=3600
JWT_REFRESH_EXPIRATION=2592000
```

#### 📧 SendGrid (Email)
```
SENDGRID_API_KEY=SG.xxxxxxxxxxxxxxxxxxxxx
SENDGRID_FROM_EMAIL=noreply@fitzone.com
```

#### 💳 Stripe (Pagos)
```
STRIPE_SECRET_KEY=sk_test_51xxxxxxxxxxxxx  (o sk_live_ para producción)
STRIPE_PUBLIC_KEY=pk_test_51xxxxxxxxxxxxx  (o pk_live_ para producción)
```

#### 🗄️ Base de Datos
Render las configura automáticamente desde `fitzone-db`:
- `DB_URL` ✅ Auto
- `DB_USERNAME` ✅ Auto
- `DB_PASSWORD` ✅ Auto

---

## 📋 PASOS PARA DESPLEGAR

### Paso 1: Subir código a GitHub
```bash
git add .
git commit -m "Preparado para despliegue en Render"
git push origin main
```

### Paso 2: Conectar repositorio en Render
1. Ve a https://dashboard.render.com
2. Click en **"New +"** > **"Blueprint"**
3. Conecta tu repositorio de GitHub
4. Render detectará automáticamente `render.yaml`

### Paso 3: Configurar Variables de Entorno
En el dashboard de Render, ve a tu servicio `fitzone-backend` > **Environment**:

1. **JWT_SECRET**: Genera uno seguro (mínimo 64 caracteres)
   ```bash
   # Genera uno aleatorio:
   openssl rand -base64 64
   ```

2. **SENDGRID_API_KEY**: Copia tu API key de SendGrid
   - Ve a https://app.sendgrid.com/settings/api_keys
   - Crea una nueva si no tienes

3. **SENDGRID_FROM_EMAIL**: El email verificado en SendGrid

4. **STRIPE_SECRET_KEY**: Copia de tu dashboard de Stripe
   - Ve a https://dashboard.stripe.com/test/apikeys
   - Usa `sk_test_` para pruebas

5. **STRIPE_PUBLIC_KEY**: Copia de tu dashboard de Stripe
   - Usa `pk_test_` para pruebas

### Paso 4: Desplegar
1. Click en **"Create Blueprint Instance"**
2. Espera a que se creen:
   - ✅ Base de datos PostgreSQL (`fitzone-db`)
   - ✅ Servicio web (`fitzone-backend`)
3. El build tarda 5-10 minutos la primera vez

---

## 🧪 VERIFICAR DESPLIEGUE

### 1. Health Check
```bash
curl https://fitzone-backend.onrender.com/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

### 2. Endpoints Principales
- `POST /api/auth/register` - Registro de usuarios
- `POST /api/auth/login` - Login
- `GET /api/reservations/group-classes/available` - Clases disponibles
- `POST /api/reservations/group-classes/{id}/join` - Unirse a clase

### 3. Probar Sistema de Pagos
```bash
# 1. Obtener token
TOKEN=$(curl -X POST https://fitzone-backend.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password"}' \
  | jq -r '.token')

# 2. Ver clases disponibles
curl -H "Authorization: Bearer $TOKEN" \
  https://fitzone-backend.onrender.com/api/reservations/group-classes/available
```

---

## 🔧 SOLUCIÓN DE PROBLEMAS

### Error: "Application failed to start"
- Verifica que todas las variables de entorno estén configuradas
- Revisa logs en Render Dashboard > Logs

### Error: "Could not connect to database"
- Espera a que la BD termine de iniciar (puede tardar 2-3 minutos)
- Verifica que `fitzone-db` esté en estado "Available"

### Error: "Port 8080 is already in use"
- ✅ Ya corregido - ahora usa puerto 10000

### Logs no aparecen
- ✅ Ya corregido - logging optimizado para producción

---

## 🌐 URLs DE PRODUCCIÓN

Una vez desplegado:
- **API Backend**: `https://fitzone-backend.onrender.com`
- **Health Check**: `https://fitzone-backend.onrender.com/actuator/health`
- **Swagger UI** (si habilitado): `https://fitzone-backend.onrender.com/swagger-ui.html`

---

## 📊 MONITOREO

### Dashboard de Render
- CPU/Memory usage
- Request logs
- Deploy history
- Database metrics

### Logs en tiempo real
```bash
# Desde el dashboard: Logs > Live logs
```

---

## 🔄 ACTUALIZACIONES

Para desplegar cambios:
```bash
git add .
git commit -m "Descripción del cambio"
git push origin main
```

Render re-desplegará automáticamente en 2-5 minutos.

---

## 💡 RECOMENDACIONES

1. **Free Tier de Render se duerme después de 15 min de inactividad**
   - Primera request después de dormir tarda 30-60 segundos
   - Solución: Upgrade a plan pagado ($7/mes)

2. **Usar Stripe en modo TEST durante desarrollo**
   - Cambiar a modo LIVE solo cuando estés listo para producción

3. **Monitorear logs regularmente**
   - Especialmente después del primer despliegue

4. **Backup de Base de Datos**
   - Render Free Tier NO incluye backups automáticos
   - Considera hacer backups manuales periódicos

---

## 🎉 ¡LISTO PARA DESPLEGAR!

Todos los archivos están configurados correctamente. Solo falta:
1. Subir a GitHub
2. Conectar en Render
3. Configurar variables de entorno
4. Deploy 🚀
# Archivos a ignorar en el build de Docker
.git
.gitignore
.env
.env.*
*.log
logs/
target/
!target/*.jar
*.md
README.md
docs/
.idea/
.vscode/
*.iml
.DS_Store
Thumbs.db
mvnw
mvnw.cmd
.mvn/
src/test/
*.sql
render.yaml

