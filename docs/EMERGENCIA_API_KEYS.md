# 🚨 GUÍA DE EMERGENCIA: ELIMINAR API KEYS DEL REPOSITORIO

## ⚠️ SITUACIÓN ACTUAL

Se subieron API keys sensibles al repositorio en los archivos:
- `application.properties`
- `application-dev.properties`
- `application-prod.properties`

**Tipos de API keys expuestas:**
- ❌ Stripe Secret Key
- ❌ Stripe Public Key
- ❌ SendGrid API Key
- ❌ Database credentials

## ✅ ACCIONES YA REALIZADAS

1. ✅ API keys eliminadas de todos los archivos `.properties`
2. ✅ Archivos modificados para usar solo variables de entorno
3. ✅ Creado `.env.example` con plantilla
4. ✅ Actualizado `.gitignore` para prevenir futuros accidentes

## 🔧 PASOS PARA LIMPIAR EL REPOSITORIO

### OPCIÓN 1: Si NO has hecho PUSH todavía (RECOMENDADO)

```bash
# 1. Deshacer el último commit pero mantener los cambios
git reset --soft HEAD~1

# 2. O deshacer el commit y los cambios (CUIDADO: pierdes cambios)
git reset --hard HEAD~1

# 3. Agregar los archivos corregidos
git add .

# 4. Hacer un nuevo commit limpio
git commit -m "fix: Remove hardcoded API keys and use environment variables"

# 5. Ahora puedes hacer push
git push origin main
```

### OPCIÓN 2: Si YA hiciste PUSH al repositorio remoto (URGENTE)

```bash
# ⚠️ ESTO REESCRIBE LA HISTORIA - Avisa a tu equipo antes

# 1. Eliminar el último commit del historial local
git reset --hard HEAD~1

# 2. Forzar el push para sobrescribir el remoto
git push --force origin main

# 3. Agregar los archivos corregidos
git add .

# 4. Hacer un nuevo commit limpio
git commit -m "fix: Remove hardcoded API keys and use environment variables"

# 5. Push normal
git push origin main
```

### OPCIÓN 3: Limpiar TODO el historial de Git (DRÁSTICO)

Si las keys están en múltiples commits anteriores:

```bash
# 1. Instalar BFG Repo-Cleaner (más fácil que git-filter-branch)
# Descarga desde: https://rtyley.github.io/bfg-repo-cleaner/

# 2. Crear un archivo con las keys a eliminar
# NOTA: NO incluir las keys reales aquí, solo usar como referencia

# 3. Limpiar el repositorio
java -jar bfg.jar --replace-text passwords.txt

# 4. Limpiar referencias
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# 5. Forzar push
git push --force origin main
```

## 🔐 ACCIONES INMEDIATAS EN STRIPE

**⚠️ MUY IMPORTANTE:** Las keys expuestas siguen siendo válidas aunque las elimines del código.

### 1. Rotar (Regenerar) tus API Keys inmediatamente

1. Ve a: https://dashboard.stripe.com/test/apikeys
2. Click en los 3 puntos `...` al lado de cada key
3. Selecciona **"Roll key"** (Rotar clave)
4. Confirma la acción
5. Copia las nuevas keys y guárdalas en un lugar seguro

### 2. Configurar las nuevas keys

**En tu máquina local:**

Crea un archivo `.env` en la raíz del proyecto:

```env
# Stripe API Keys (NUEVAS después de rotarlas)
STRIPE_SECRET_KEY=sk_test_NUEVA_KEY_AQUI
STRIPE_PUBLIC_KEY=pk_test_NUEVA_KEY_AQUI
STRIPE_WEBHOOK_SECRET=whsec_NUEVA_WEBHOOK

# JWT
JWT_SECRET=UniversidadDelQuindioSoftware3GarciaLopezValencia
JWT_EXPIRATION=3600
JWT_REFRESH_EXPIRATION=2592000

# SendGrid (también deberías rotarla)
SENDGRID_API_KEY=SG.NUEVA_KEY_AQUI
SENDGRID_FROM_EMAIL=tu_email@gmail.com

# Database
DB_URL=jdbc:postgresql://tu-host.neon.tech/neondb?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=tu_password

# Server
PORT=8080
```

**En Render.com (producción):**

1. Ve a tu servicio en Render
2. Settings → Environment
3. Agrega las variables con las NUEVAS keys rotadas
4. Guarda y redeploy

## 📋 CHECKLIST FINAL

Antes de continuar trabajando:

- [ ] ✅ API keys eliminadas de todos los `.properties`
- [ ] ✅ `.gitignore` actualizado para ignorar `.env`
- [ ] ✅ Commit problemático eliminado del historial
- [ ] 🔐 Keys de Stripe rotadas en el dashboard
- [ ] 🔐 SendGrid API key rotada
- [ ] 🔐 Nuevas keys configuradas en `.env` local
- [ ] 🔐 Nuevas keys configuradas en Render.com
- [ ] ✅ Aplicación funcionando con las nuevas keys
- [ ] ✅ Push limpio exitoso al repositorio

## 🎯 COMANDOS RÁPIDOS (COPIA Y PEGA)

**Si NO hiciste push:**
```bash
git reset --soft HEAD~1
git add .
git commit -m "fix: Remove hardcoded API keys and use environment variables"
git push origin main
```

**Si YA hiciste push:**
```bash
git reset --hard HEAD~1
git push --force origin main
git add .
git commit -m "fix: Remove hardcoded API keys and use environment variables"
git push origin main
```

## 🚀 VERIFICACIÓN

Después de todo, verifica que las keys NO aparecen en el código:

```bash
# Buscar en el historial (no debería mostrar nada)
git log -p | grep "sk_test"
```

---

**Última actualización:** 2025-10-19  
**Urgencia:** 🔴 CRÍTICA  
**Acción requerida:** INMEDIATA
