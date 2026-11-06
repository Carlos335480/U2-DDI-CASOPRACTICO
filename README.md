# Caminar y Correr — Wear OS + Android

Prototipo escolar de una app para caminar y correr con dos módulos:

- `wear`: app para reloj Wear OS.
- `phone`: app para teléfono Android.
- `shared`: modelos, fórmula de calorías, formato y rutas del Data Layer.

La app del reloj registra la actividad con **Health Services**. La app del teléfono configura el peso y consulta el historial. La comunicación usa **Wear OS Data Layer**.

## Requisitos

- Android Studio 2026.2 o compatible con AGP 9.1.1.
- JDK 17 o superior. En este equipo se compiló con el JBR 25 de Android Studio.
- Android SDK 36 (compileSdk/targetSdk 36).
- Para el reloj: Wear OS 3 o superior. Para datos sintéticos de Health Services en emulador: Wear OS 4 o superior.
- Para el teléfono: Android 11 (API 30) o superior con Google Play services.

## Compilar

Desde la raíz del proyecto:

```bat
.\gradlew.bat :phone:assembleDebug :wear:assembleDebug
```

Los APK quedan en:

```text
phone/build/outputs/apk/debug/phone-debug.apk
wear/build/outputs/apk/debug/wear-debug.apk
```

También se copian a `dist/`:

```text
dist/CaminarCorrer-phone-debug.apk
dist/CaminarCorrer-wear-debug.apk
```

Pruebas unitarias del módulo compartido:

```bat
.\gradlew.bat :shared:test --no-daemon --max-workers=1
```

Análisis estático:

```bat
.\gradlew.bat :phone:lintDebug :wear:lintDebug --no-daemon --max-workers=1
```

### Verificación realizada

- `:shared:test`: 12 pruebas unitarias aprobadas (fórmula de calorías, ritmo y formato).
- `:phone:assembleDebug` y `:wear:assembleDebug`: APK generados correctamente.
- `:phone:lintDebug` y `:wear:lintDebug`: sin errores de lint.
- La app del reloj se probó en un emulador **Wear OS 4 (API 33)**: inicio, caminata, pausa, reanudación, fin y resumen.
- Health Services reportó distancia y ritmo. Sin peso configurado, las calorías se mostraron como `—`, sin inventar valores.
- El resumen mostró `Pendiente (1)` al no haber teléfono emparejado, que es el comportamiento esperado.
- La app del teléfono se probó en un **Samsung SM-A175F** con Android 16: inicio, perfil y guardado de peso.
- La sincronización de peso e historial entre el Samsung y el emulador Wear OS fue confirmada durante la prueba.
- Capturas en `docs/screenshots/`.

## Estructura

```text
CaminarCorrerWear/
├── shared/   Modelos, cálculos, formato y rutas del Data Layer
├── phone/    App Android: perfil, inicio, historial y detalle
├── wear/     App Wear OS: inicio, actividad, resumen y sincronización
├── docs/     Comandos y capturas de prueba
├── scripts/  Ayudas de instalación para emuladores
├── gradle/   Version catalog y wrapper
├── dist/     APK generados (no se versionan)
└── README.md
```

## Documentación adicional

- [Comandos de desarrollo, prueba y sincronización](docs/COMANDOS.md)
- [Capturas del reloj y del teléfono](docs/screenshots/)

## Módulos y responsabilidades

### `shared`

- `ActivityType`, `DistanceSource`, `ActivityRecord`, `UserProfile`.
- `CalorieCalculator`: estimación neta con ecuaciones ACSM.
- `MetricsCalculator`: ritmo promedio.
- `Formatters`: duración, distancia, ritmo, calorías, peso y fecha en español.
- `DataLayerPaths`: rutas `/profile`, `/activity/{id}`, `/ack/{id}`, `/request-sync`, `/request-profile`.

### `phone`

- `PhoneApplication` y `PhoneAppContainer`.
- Persistencia local con DataStore Preferences (`phone_data`).
- `PhoneDataLayerService`: recibe actividades del reloj, las guarda y envía `ack`.
- Pantallas Compose Material 3:
  - Inicio con estado de sincronización.
  - Perfil con validación de peso (20–300 kg).
  - Historial.
  - Detalle de actividad.

### `wear`

- `WearApplication` y `WearAppContainer`.
- Persistencia local con DataStore Preferences (`wear_data`).
- `WatchDataLayerService`: recibe perfil y `ack`, y atiende solicitudes de sincronización.
- `ExerciseService`: servicio en primer plano con Health Services.
- Pantallas Compose for Wear OS Material 3:
  - Inicio con Caminar / Correr.
  - Actividad con tiempo, distancia, ritmo, calorías, pausar/reanudar y terminar.
  - Resumen.
  - Estados de carga y error.

## Permisos

### Reloj

- `ACTIVITY_RECOGNITION`: necesario para que Health Services registre la actividad.
- `ACCESS_FINE_LOCATION`: opcional. Si se concede y el GPS está activo, mejora la distancia. Si se deniega, Health Services usa el podómetro.
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_HEALTH`, `FOREGROUND_SERVICE_LOCATION`: para que la actividad continúe con la pantalla apagada.
- `POST_NOTIFICATIONS`: para mostrar la notificación de actividad en curso (Android 13+).

No se solicita frecuencia cardiaca ni `BODY_SENSORS`. No se pide ubicación en segundo plano ni se guardan coordenadas.

### Teléfono

No requiere permisos de runtime.

## Ejecutar con emuladores emparejados

1. En Android Studio, abre **Device Manager**.
2. Crea o usa un teléfono con **Google Play Store** y API 30 o superior.
3. Instala una imagen **Wear OS 4 o superior** y crea un AVD de reloj.
4. Usa **Wear OS Emulator Pairing Assistant** para emparejar el teléfono y el reloj.
5. Instala cada APK en su dispositivo:

> En este equipo ya existe el AVD `WearOS4`, con Wear OS 4. Para arrancarlo:
>
> ```bat
> "%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe" -avd WearOS4 -gpu host
> ```
>
> El arranque puede tardar varios minutos. En este equipo el primer arranque tardó aproximadamente 11 minutos y los siguientes fueron más rápidos. Si el emulador se cierra por un error de OpenGL/Qt, prueba con `-gpu swiftshader_indirect` o `-no-window`. También existe `Phone_API_37`, pero esa imagen no arrancó de forma estable; para el teléfono es más fiable usar un teléfono físico o crear un AVD nuevo desde Android Studio.

```bat
adb -s <serial-telefono> install -r dist\CaminarCorrer-phone-debug.apk
adb -s <serial-reloj> install -r dist\CaminarCorrer-wear-debug.apk
```

6. Concede permisos en el reloj si no los pediste desde la app:

```bat
adb -s <serial-reloj> shell pm grant com.ejemplo.caminarcorrer android.permission.ACTIVITY_RECOGNITION
adb -s <serial-reloj> shell pm grant com.ejemplo.caminarcorrer android.permission.ACCESS_FINE_LOCATION
adb -s <serial-reloj> shell pm grant com.ejemplo.caminarcorrer android.permission.ACCESS_COARSE_LOCATION
adb -s <serial-reloj> shell pm grant com.ejemplo.caminarcorrer android.permission.POST_NOTIFICATIONS
```

`POST_NOTIFICATIONS` solo existe en API 33 o superior.

Si los dos emuladores están corriendo, también puedes instalar y abrir todo con:

```bat
powershell -ExecutionPolicy Bypass -File scripts\install-emulators.ps1
```

### Datos sintéticos de Health Services

En **Wear OS 4 o superior**, abre el panel **Wear Health Services** del emulador y ajusta las capacidades/métricas. Después inicia la actividad desde la app.

En **Wear OS 3**, puedes usar:

```bat
adb -s <serial-reloj> shell am broadcast -a "whs.USE_SYNTHETIC_PROVIDERS" com.google.android.wearable.healthservices
adb -s <serial-reloj> shell am broadcast -a "whs.synthetic.user.START_WALKING" com.google.android.wearable.healthservices
```

Para detener:

```bat
adb -s <serial-reloj> shell am broadcast -a "whs.synthetic.user.STOP_EXERCISE" com.google.android.wearable.healthservices
```

## Flujo de prueba

1. Abre la app del teléfono y guarda un peso, por ejemplo `70`.
2. Verifica que el reloj muestre `Peso: 70,0 kg`.
3. En el reloj, pulsa **Caminar** o **Correr** y concede permisos.
4. Inicia los datos sintéticos o camina con el reloj físico.
5. Pausa, reanuda y termina.
6. El resumen debe mostrar duración, distancia, ritmo y calorías (o `—` si faltan datos).
7. Abre el historial en el teléfono: la actividad debe aparecer y el resumen del reloj debe indicar que está sincronizado.

## Data Layer: mismo paquete y firma

Wear OS Data Layer solo comunica apps con el **mismo `applicationId`** y la **misma firma**:

- Ambos módulos usan `applicationId = "com.ejemplo.caminarcorrer"`.
- Los `namespace` internos son distintos (`...phone` y `...wear`).
- Los APK debug se firman con la misma keystore de debug si se compilan en el mismo equipo.
- No instales los dos APK en el mismo dispositivo: colisiona el paquete.

Si compilas un APK en otra máquina, la firma de debug cambia y el Data Layer no verá a la otra app. Usa la misma keystore o compila ambos en el mismo equipo.

## Sincronización

- El teléfono es la fuente del peso.
- El reloj es la fuente de las actividades.
- Cada actividad tiene un UUID. El teléfono hace `upsert` por ID, así que los reintentos no duplican.
- El reloj guarda la actividad con `synced = false`; el teléfono responde con un `ack`; el reloj la marca como sincronizada.
- Si no hay conexión, la actividad queda pendiente y se reintenta al abrir la app o al recibir una solicitud de sincronización.
- El botón **Sincronizar ahora** del teléfono envía un mensaje al reloj para que reenvíe pendientes.

## Calorías

Se muestran **calorías netas estimadas** (gasto atribuible a la actividad, sin el metabolismo basal):

```text
Caminar: VO2 = 0,1 × velocidad(m/min) + 3,5
Correr:  VO2 = 0,2 × velocidad(m/min) + 3,5
MET = VO2 / 3,5
kcal netas = (MET − 1) × 3,5 × peso(kg) / 200 × minutos
```

Se calculan solo si hay peso válido (20–300 kg), distancia y duración suficiente. Si falta algo, se muestra `—`.

Limitaciones: es una estimación poblacional; no considera edad, sexo, composición corporal, terreno, temperatura, frecuencia cardiaca ni eficiencia individual. No es una medición médica.

## Modo solo tiempo

Si Health Services no está disponible o el reloj no reporta distancia, la app no bloquea el registro: cambia a **modo solo tiempo**. En ese modo se guarda la duración y se muestran `—` para distancia, ritmo y calorías. La pantalla de actividad indica `SOLO TIEMPO` y el resumen conserva la actividad con `Sin datos de distancia`.

Esto permite un comportamiento alternativo seguro sin inventar métricas.

## Privacidad

- Perfil e historial se guardan localmente con DataStore.
- No hay cuentas, anuncios, analítica ni backend propio.
- No se guardan coordenadas GPS; solo la distancia que reporta Health Services.
- `android:allowBackup="false"` para evitar respaldo automático.
- El Data Layer puede usar servidores de Google cuando no hay Bluetooth, con cifrado extremo a extremo. No hay una opción pública para forzar solo Bluetooth.

## Limitaciones conocidas

- No se incluye frecuencia cardiaca.
- No hay mapas ni navegación.
- La distancia por pasos es una estimación.
- Las capacidades de Health Services varían por reloj; si no hay soporte de distancia, se activa el modo solo tiempo.
- Si el proceso del reloj se destruye durante la actividad, la sesión puede perderse; el servicio en primer plano reduce ese riesgo.
- No se verificó en reloj físico.
- La app del teléfono se verificó en compilación y lint, pero no en ejecución; el emulador de teléfono disponible no arrancó de forma estable.
- La sincronización real entre teléfono y reloj no se verificó en ejecución; el reloj mostró `Pendiente` sin teléfono, que es el comportamiento esperado.
