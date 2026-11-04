# Comandos de desarrollo, prueba y sincronización

Guía de los comandos usados para ejecutar, probar y sincronizar **Caminar y Correr**.

Los seriales son ejemplos de este equipo:

- Teléfono Samsung: `RZGL70SRSZE`
- Reloj emulado: `emulator-5554`
- Paquete: `com.ejemplo.caminarcorrer`

En PowerShell puedes definir variables:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$phone = "RZGL70SRSZE"
$watch = "emulator-5554"
$root = "C:\Users\Edwin Quiroz\CaminarCorrerWear"
```

En CMD o en la terminal de Android Studio, reemplaza `$phone` y `$watch` por los seriales reales.

---

## 1. Comandos básicos de ADB

```bat
cd "C:\Users\Edwin Quiroz\AppData\Local\Android\Sdk\platform-tools"
.\adb.exe devices
```

Estados posibles:

- `device`: listo.
- `unauthorized`: falta aceptar el aviso de depuración USB.
- `offline`: reconectar o reiniciar ADB.

Reiniciar ADB:

```bat
.\adb.exe kill-server
.\adb.exe start-server
.\adb.exe devices
```

Reconectar un dispositivo offline:

```bat
.\adb.exe reconnect offline
```

Información del dispositivo:

```bat
.\adb.exe -s <serial> shell getprop ro.product.manufacturer
.\adb.exe -s <serial> shell getprop ro.product.model
.\adb.exe -s <serial> shell getprop ro.build.version.release
.\adb.exe -s <serial> shell getprop ro.build.version.sdk
.\adb.exe -s <serial> shell getprop ro.build.characteristics
```

Para identificar el reloj, `ro.build.characteristics` debe contener `watch`.

Tamaño de pantalla:

```bat
.\adb.exe -s <serial> shell wm size
```

Paquetes instalados:

```bat
.\adb.exe -s <serial> shell pm list packages | findstr caminarcorrer
```

---

## 2. Instalar, abrir y desinstalar

Instalar el APK del teléfono:

```bat
.\adb.exe -s RZGL70SRSZE install -r "C:\Users\Edwin Quiroz\CaminarCorrerWear\dist\CaminarCorrer-phone-debug.apk"
```

Instalar el APK del reloj:

```bat
.\adb.exe -s emulator-5554 install -r "C:\Users\Edwin Quiroz\CaminarCorrerWear\dist\CaminarCorrer-wear-debug.apk"
```

Abrir la app:

```bat
.\adb.exe -s <serial> shell monkey -p com.ejemplo.caminarcorrer -c android.intent.category.LAUNCHER 1
```

Forzar cierre:

```bat
.\adb.exe -s <serial> shell am force-stop com.ejemplo.caminarcorrer
```

Desinstalar:

```bat
.\adb.exe -s <serial> uninstall com.ejemplo.caminarcorrer
```

Borrar datos locales:

```bat
.\adb.exe -s <serial> shell pm clear com.ejemplo.caminarcorrer
```

> `pm clear` borra perfil e historial local de ese dispositivo.

---

## 3. Permisos del reloj

Conceder permisos por ADB:

```bat
.\adb.exe -s emulator-5554 shell pm grant com.ejemplo.caminarcorrer android.permission.ACTIVITY_RECOGNITION
.\adb.exe -s emulator-5554 shell pm grant com.ejemplo.caminarcorrer android.permission.ACCESS_FINE_LOCATION
.\adb.exe -s emulator-5554 shell pm grant com.ejemplo.caminarcorrer android.permission.ACCESS_COARSE_LOCATION
.\adb.exe -s emulator-5554 shell pm grant com.ejemplo.caminarcorrer android.permission.POST_NOTIFICATIONS
```

`POST_NOTIFICATIONS` solo existe en Android 13 o superior.

Revocar un permiso:

```bat
.\adb.exe -s emulator-5554 shell pm revoke com.ejemplo.caminarcorrer android.permission.ACCESS_FINE_LOCATION
```

Los permisos concedidos con `pm grant` son para pruebas de desarrollo. En una app publicada se deben pedir en contexto.

---

## 4. Emulador Wear OS

Listar AVDs:

```bat
cd "C:\Users\Edwin Quiroz\AppData\Local\Android\Sdk\emulator"
.\emulator.exe -list-avds
```

Arrancar el reloj con aceleración del host:

```bat
.\emulator.exe -avd WearOS4 -gpu host
```

Si la ventana falla:

```bat
.\emulator.exe -avd WearOS4 -gpu swiftshader_indirect
```

Sin ventana:

```bat
.\emulator.exe -avd WearOS4 -no-window -no-audio -no-snapshot -gpu swiftshader_indirect
```

Comprobar arranque:

```bat
.\adb.exe -s emulator-5554 shell getprop sys.boot_completed
```

Debe devolver `1`.

Apagar el emulador:

```bat
.\adb.exe -s emulator-5554 emu kill
```

---

## 5. Sincronización sin cable: ADB inalámbrico

Requisitos:

- Teléfono y PC en la misma red Wi-Fi.
- Depuración USB activa.
- El teléfono conectado por USB solo para activar el modo TCP.

### Activar ADB por Wi-Fi

```bat
cd "C:\Users\Edwin Quiroz\AppData\Local\Android\Sdk\platform-tools"
.\adb.exe -s RZGL70SRSZE tcpip 5555
```

Obtener la IP del teléfono:

```bat
.\adb.exe -s RZGL70SRSZE shell ip route
```

Busca la IP después de `src`, por ejemplo:

```text
192.168.1.0/24 dev wlan0 ... src 192.168.1.50
```

Conecta:

```bat
.\adb.exe connect 192.168.1.50:5555
.\adb.exe devices
```

Debe aparecer:

```text
192.168.1.50:5555   device
```

Ahora puedes desconectar el cable USB.

### Comprobar el teléfono por Wi-Fi

```bat
.\adb.exe -s 192.168.1.50:5555 shell getprop ro.product.model
```

### Volver a USB

```bat
.\adb.exe -s 192.168.1.50:5555 usb
```

### Desconectar ADB por Wi-Fi

```bat
.\adb.exe disconnect 192.168.1.50:5555
```

Notas:

- Si el teléfono se reinicia, normalmente vuelve a modo USB; hay que repetir `tcpip 5555`.
- Algunas redes Wi-Fi bloquean la comunicación entre dispositivos (aislamiento de clientes). Usa un hotspot o una red doméstica.
- El ADB inalámbrico es para desarrollo. No lo dejes activo en redes públicas.
- El emparejamiento del teléfono con el **emulador Wear OS** puede seguir dependiendo del puente ADB. Si al desconectar el cable se pierde la sincronización, mantén el cable o usa ADB inalámbrico.

### Método antiguo para emparejar con el emulador

Con el teléfono conectado por USB:

```bat
.\adb.exe -s RZGL70SRSZE forward tcp:5601 tcp:5601
```

Luego, en el teléfono:

1. Abre la app **Wear OS**.
2. Menú de tres puntos.
3. **Pair with emulator**.

> El método recomendado sigue siendo el **Wear OS Emulator Pairing Assistant** de Android Studio.

---

## 6. Emparejar teléfono físico con el reloj emulado

No hay un comando único de ADB para esto; se usa el asistente de Android Studio.

1. Abre el proyecto en Android Studio.
2. En el selector de dispositivos de la barra superior, selecciona el teléfono.
3. Busca **Pair Wearable** o **Wear OS Emulator Pairing Assistant**.
4. Selecciona el AVD `WearOS4`.
5. Sigue el asistente e instala la app **Wear OS** en el teléfono si lo pide.

Para comprobar que ADB ve los dos dispositivos:

```bat
.\adb.exe devices
```

Después de emparejar:

1. Guarda el peso en el teléfono.
2. Cierra y vuelve a abrir la app del reloj.
3. Si no se actualiza, pulsa **Sincronizar ahora** en el teléfono.

---

## 7. Health Services y datos sintéticos

En un **emulador Wear OS**, la distancia no viene del movimiento de la PC. Health Services genera datos sintéticos.

### Wear OS 4 o superior

1. Abre los controles extendidos del emulador.
2. Busca **Wear Health Services**.
3. Ajusta capacidades y valores de sensores.
4. Para dejar la distancia en cero, pon velocidad/distancia en `0` y pulsa **Apply**.

### Wear OS 3

Activar datos sintéticos:

```bat
.\adb.exe -s emulator-5554 shell am broadcast -a "whs.USE_SYNTHETIC_PROVIDERS" com.google.android.wearable.healthservices
```

Iniciar una caminata sintética:

```bat
.\adb.exe -s emulator-5554 shell am broadcast -a "whs.synthetic.user.START_WALKING" com.google.android.wearable.healthservices
```

Iniciar una carrera sintética:

```bat
.\adb.exe -s emulator-5554 shell am broadcast -a "whs.synthetic.user.START_RUNNING" com.google.android.wearable.healthservices
```

Detener:

```bat
.\adb.exe -s emulator-5554 shell am broadcast -a "whs.synthetic.user.STOP_EXERCISE" com.google.android.wearable.healthservices
```

Volver a sensores reales:

```bat
.\adb.exe -s emulator-5554 shell am broadcast -a "whs.USE_SENSOR_PROVIDERS" com.google.android.wearable.healthservices
```

La app muestra lo que reporta `DISTANCE_TOTAL`; no inventa distancia. En el emulador, ese dato es simulado.

---

## 8. Logs, capturas y automatización de UI

Limpiar logs:

```bat
.\adb.exe -s <serial> logcat -c
```

Guardar logs filtrados:

```bat
.\adb.exe -s <serial> logcat -d -v time | findstr caminarcorrer > logcat.txt
```

Ver errores:

```bat
.\adb.exe -s <serial> logcat -d -v time | findstr "FATAL EXCEPTION AndroidRuntime"
```

Captura de pantalla del reloj:

```bat
.\adb.exe -s emulator-5554 shell screencap -p /data/local/tmp/wear.png
.\adb.exe -s emulator-5554 pull /data/local/tmp/wear.png "C:\Users\Edwin Quiroz\CaminarCorrerWear\docs\screenshots\wear.png"
```

Captura del teléfono:

```bat
.\adb.exe -s RZGL70SRSZE shell screencap -p /sdcard/phone.png
.\adb.exe -s RZGL70SRSZE pull /sdcard/phone.png "C:\Users\Edwin Quiroz\CaminarCorrerWear\docs\screenshots\phone.png"
```

Volcar la jerarquía de UI:

```bat
.\adb.exe -s <serial> shell uiautomator dump /sdcard/ui.xml
.\adb.exe -s <serial> pull /sdcard/ui.xml ui.xml
```

Simular toques y texto:

```bat
.\adb.exe -s <serial> shell input tap 540 1124
.\adb.exe -s <serial> shell input text "70"
.\adb.exe -s <serial> shell input keyevent 4
```

`input keyevent 4` equivale a Atrás y sirve para cerrar el teclado.

---

## 9. Compilar, probar y analizar

Pruebas unitarias:

```bat
.\gradlew.bat :shared:test --no-daemon --max-workers=1
```

Compilar APK:

```bat
.\gradlew.bat :phone:assembleDebug :wear:assembleDebug
```

Lint:

```bat
.\gradlew.bat :phone:lintDebug :wear:lintDebug --no-daemon --max-workers=1
```

Salidas:

```text
phone/build/outputs/apk/debug/phone-debug.apk
wear/build/outputs/apk/debug/wear-debug.apk
```

---

## 10. Comprobar la sincronización

La sincronización no se controla directamente por ADB; la app usa Wear OS Data Layer.

En el teléfono:

1. Abre **Caminar y Correr**.
2. Pulsa **Sincronizar ahora**.

En el reloj:

- El estado debe pasar de `Pendiente` a `Sincronizado`.
- Si no hay teléfono, se queda en `Pendiente`, que es lo esperado.

Comprobar que ambas apps están instaladas:

```bat
.\adb.exe -s RZGL70SRSZE shell pm list packages | findstr caminarcorrer
.\adb.exe -s emulator-5554 shell pm list packages | findstr caminarcorrer
```

Ver la actividad en primer plano:

```bat
.\adb.exe -s <serial> shell dumpsys activity activities | findstr caminarcorrer
```

---

## 11. Problemas comunes

### `unauthorized`

- Desbloquea el teléfono.
- Acepta **Permitir depuración USB**.
- Si no aparece, revoca autorizaciones y reconecta.

### `offline`

```bat
.\adb.exe reconnect offline
.\adb.exe kill-server
.\adb.exe start-server
```

### `INSTALL_FAILED_UPDATE_INCOMPATIBLE`

```bat
.\adb.exe -s <serial> uninstall com.ejemplo.caminarcorrer
```

Y vuelve a instalar.

### El reloj dice “Ya hay una actividad en curso”

Health Services conserva una actividad anterior:

```bat
.\adb.exe -s emulator-5554 uninstall com.ejemplo.caminarcorrer
```

Vuelve a instalar. Si persiste, reinicia el emulador.

### El emulador se cierra

```bat
.\emulator.exe -avd WearOS4 -gpu host
```

o:

```bat
.\emulator.exe -avd WearOS4 -gpu swiftshader_indirect
```

### No aparece distancia

- En el emulador, revisa **Wear Health Services**.
- Si el reloj no reporta distancia, la app cambia a **modo solo tiempo** y muestra `—`.
- No es un error de la app; es una limitación de los datos disponibles.

### No sincroniza

1. Verifica el emparejamiento.
2. Abre la app del teléfono.
3. Pulsa **Sincronizar ahora**.
4. Cierra y abre la app del reloj.
5. Revisa los logs:

```bat
.\adb.exe -s emulator-5554 logcat -d -v time | findstr caminarcorrer
```

---

## 12. Git y GitHub

```bat
git status
git add .
git commit -m "docs: agrega comandos de prueba y sincronización"
git push
```

No subir:

- `local.properties`
- APK/ZIP de `dist/`
- logs
- capturas temporales
- credenciales o tokens
