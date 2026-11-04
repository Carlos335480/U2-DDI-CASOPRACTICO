param(
    [string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = "Stop"

$phoneApk = Join-Path $ProjectRoot "dist\CaminarCorrer-phone-debug.apk"
$wearApk = Join-Path $ProjectRoot "dist\CaminarCorrer-wear-debug.apk"

if (-not (Test-Path $phoneApk)) { throw "No existe $phoneApk" }
if (-not (Test-Path $wearApk)) { throw "No existe $wearApk" }
if (-not (Test-Path $Adb)) { throw "No existe adb en $Adb" }

Write-Host "Esperando dispositivos ADB..."
& $Adb start-server | Out-Null

$deadline = (Get-Date).AddMinutes(8)
$phone = $null
$watch = $null

while ((Get-Date) -lt $deadline) {
    $lines = & $Adb devices
    $serials = @()
    foreach ($line in $lines) {
        if ($line -match '^([^\s]+)\s+device$') {
            $serials += $Matches[1]
        }
    }

    $phone = $null
    $watch = $null
    foreach ($serial in $serials) {
        $characteristics = (& $Adb -s $serial shell getprop ro.build.characteristics 2>$null) -join " "
        if ($characteristics -match 'watch') {
            $watch = $serial
        } elseif (-not $phone) {
            $phone = $serial
        }
    }

    if ($phone -and $watch) { break }
    Start-Sleep -Seconds 5
}

if (-not $phone) { throw "No encontré el emulador de teléfono." }
if (-not $watch) { throw "No encontré el emulador de reloj." }

Write-Host "Teléfono: $phone"
Write-Host "Reloj:    $watch"

Write-Host "Instalando app del teléfono..."
& $Adb -s $phone install -r $phoneApk

Write-Host "Instalando app del reloj..."
& $Adb -s $watch install -r $wearApk

$permissions = @(
    "android.permission.ACTIVITY_RECOGNITION",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.POST_NOTIFICATIONS"
)

Write-Host "Concediendo permisos en el reloj..."
foreach ($permission in $permissions) {
    & $Adb -s $watch shell pm grant com.ejemplo.caminarcorrer $permission 2>$null
}

Write-Host "Abriendo aplicaciones..."
& $Adb -s $phone shell monkey -p com.ejemplo.caminarcorrer -c android.intent.category.LAUNCHER 1 | Out-Null
& $Adb -s $watch shell monkey -p com.ejemplo.caminarcorrer -c android.intent.category.LAUNCHER 1 | Out-Null

Write-Host ""
Write-Host "Listo. Revisa las dos ventanas del emulador."
Write-Host "Si no ves la app, búscala en el cajón de aplicaciones como 'Caminar y Correr'."
