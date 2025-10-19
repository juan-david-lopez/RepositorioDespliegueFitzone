    )
    Write-Host ""

    $jsonResponse = $response.Content | ConvertFrom-Json

    if ($response.StatusCode -eq 200) {
        Write-Host "✅ ÉXITO" -ForegroundColor Green
        Write-Host ""
        Write-Host "UserID disponible en:" -ForegroundColor Yellow
        Write-Host "  → data.user.idUser = $($jsonResponse.data.user.idUser)" -ForegroundColor White
        Write-Host "  → data.user.id = $($jsonResponse.data.user.id)" -ForegroundColor White
    } else {
        Write-Host "❌ ERROR" -ForegroundColor Red
        Write-Host "Mensaje: $($jsonResponse.message)" -ForegroundColor Yellow
    }

    Write-Host ""
    Write-Host "Respuesta completa:" -ForegroundColor Gray
    Write-Host ($jsonResponse | ConvertTo-Json -Depth 10) -ForegroundColor DarkGray

} catch {
    Write-Host "❌ Excepción: $_" -ForegroundColor Red
}
# test-otp-flow.ps1
# Script para probar el flujo completo de OTP

$email = "test@example.com"
$apiUrl = "http://localhost:8080"

Write-Host "🚀 Iniciando test de flujo OTP..." -ForegroundColor Cyan
Write-Host ""

# PASO 1: Solicitar OTP
Write-Host "📧 PASO 1: Solicitando OTP para $email" -ForegroundColor Yellow
$requestOtpBody = @{
    email = $email
} | ConvertTo-Json

try {
    $otpResponse = Invoke-RestMethod -Uri "$apiUrl/auth/request-otp" `
        -Method POST `
        -ContentType "application/json" `
        -Body $requestOtpBody

    Write-Host "✅ OTP solicitado exitosamente" -ForegroundColor Green
    Write-Host "Respuesta: $($otpResponse | ConvertTo-Json -Depth 5)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Error al solicitar OTP: $_" -ForegroundColor Red
    exit 1
}

# PASO 2: Ingresar OTP manualmente
Write-Host ""
Write-Host "📱 PASO 2: Verificar OTP" -ForegroundColor Yellow
$otp = Read-Host "Ingresa el código OTP que recibiste por email"

# PASO 3: Verificar OTP
Write-Host ""
Write-Host "🔐 PASO 3: Verificando OTP..." -ForegroundColor Yellow

$verifyOtpBody = @{
    email = $email
    code = $otp
} | ConvertTo-Json

try {
    $verifyResponse = Invoke-RestMethod -Uri "$apiUrl/auth/verify-otp" `
        -Method POST `
        -ContentType "application/json" `
        -Body $verifyOtpBody

    Write-Host "✅ OTP verificado exitosamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "📊 DATOS RECIBIDOS:" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

    # Extraer datos importantes
    $user = $verifyResponse.data.user
    $membership = $verifyResponse.data.membership

    Write-Host "👤 Usuario:" -ForegroundColor White
    Write-Host "   ID (idUser): $($user.idUser)" -ForegroundColor Gray
    Write-Host "   ID (alias):  $($user.id)" -ForegroundColor Gray
    Write-Host "   Nombre:      $($user.name)" -ForegroundColor Gray
    Write-Host "   Email:       $($user.email)" -ForegroundColor Gray
    Write-Host "   Role:        $($user.role)" -ForegroundColor Gray
    Write-Host ""

    Write-Host "🎫 Membresía:" -ForegroundColor White
    Write-Host "   Tipo:        $($membership.type)" -ForegroundColor Gray
    Write-Host "   Estado:      $($membership.status)" -ForegroundColor Gray
    Write-Host "   Días Rest.:  $($membership.daysRemaining)" -ForegroundColor Gray
    Write-Host ""

    Write-Host "🔑 Token JWT:" -ForegroundColor White
    Write-Host "   $($verifyResponse.data.accessToken.Substring(0, 50))..." -ForegroundColor Gray
    Write-Host ""
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

    # Guardar token para usar en otros tests
    $token = $verifyResponse.data.accessToken
    $userId = $user.idUser

    Write-Host ""
    Write-Host "✅ PRUEBA EXITOSA - userId disponible: $userId" -ForegroundColor Green

    # Guardar en archivo para usar en otros scripts
    @{
        token = $token
        userId = $userId
        email = $user.email
    } | ConvertTo-Json | Out-File "test-session.json"

    Write-Host "💾 Sesión guardada en test-session.json" -ForegroundColor Cyan

} catch {
    Write-Host "❌ Error al verificar OTP: $_" -ForegroundColor Red
    Write-Host "Detalles del error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

