# Arquitectura del Sistema Caminar y Correr — Wear OS + Android

## 1. Visión General y Estilo Arquitectónico
El sistema **Caminar y Correr** está diseñado bajo una **Arquitectura Modular Multi-Dispositivo (Companion App + Wearable)** desarrollada íntegramente en **Kotlin**, con interfaces declarativas en **Jetpack Compose (Material 3)**, persistencia local reactiva mediante **Jetpack DataStore Preferences** y comunicación asíncrona punto a punto a través de **Google Play Services Wear OS Data Layer API**.

El sistema se divide en tres módulos desacoplados con una única fuente de verdad en las reglas de dominio:
- `:wear`: Aplicación nativa para relojes inteligentes (**Wear OS 3.0+**, API 30+), encargada de la captura biométrica y de desplazamiento mediante **Health Services API** y ejecución continua en segundo plano.
- `:phone`: Aplicación nativa para smartphones (**Android 11+**, API 30+), encargada de la gestión del perfil fisiológico del usuario (peso), visualización del historial consolidado y análisis detallado de sesiones.
- `:shared`: Biblioteca transversal de dominio puro en Kotlin que centraliza los modelos de datos, ecuaciones metabólicas ACSM, cálculo de métricas, formateo localizado y el contrato de rutas IPC del Data Layer.

Ambos ejecutables comparten el mismo identificador de paquete (`applicationId = "com.ejemplo.caminarcorrer"`) y firma criptográfica de compilación, habilitando la comunicación directa e interprocesos (IPC) cifrada de extremo a extremo sin requerir infraestructura de servidores externos, cuentas de usuario ni bases de datos en la nube.

---

## 2. Capas del Sistema

### 2.1 Capa de Presentación (UI/UX Adaptativa Material 3)
- **Módulo Reloj (`:wear` — Compose for Wear OS Material 3):**
  - **Pantalla de Inicio:** Selector rápido de modalidad (`Caminar` / `Correr`), visualización del peso sincronizado vigente y estado de disponibilidad de sensores.
  - **Monitor de Actividad en Vivo:** Interfaz optimizada para pantallas circulares con lectura en tiempo real de tiempo transcurrido, distancia acumulada, ritmo promedio y calorías netas, junto con controles táctiles de `Pausar / Reanudar` y `Terminar`.
  - **Pantalla de Resumen y Estado de Sincronización:** Desglose post-entrenamiento con indicador explícito del estado de transmisión hacia el teléfono (`Sincronizado` vs. `Pendiente`).
  - **Estados de Carga, Error y Fallback:** Renderizado condicional de alertas visuales (`SOLO TIEMPO`) cuando el hardware o los permisos impiden el rastreo de distancia.
- **Módulo Teléfono (`:phone` — Jetpack Compose Material 3):**
  - **Inicio y Panel de Sincronización:** Vista principal con el estado de conexión del nodo wearable y disparador manual de sincronización (`Sincronizar ahora`).
  - **Perfil Fisiológico:** Formulario con validación estricta de rango biométrico (peso entre `20.0 kg` y `300.0 kg`) que propaga cambios automáticamente al reloj.
  - **Historial y Detalle de Actividad:** Listado cronológico de sesiones recibidas con vista de inspección detallada (fecha en español, duración, distancia, ritmo promedio, calorías netas y origen de medición).

### 2.2 Motor de Captura Física y Servicio en Primer Plano (`ExerciseService`)
- **Ejecución Persistente (`FOREGROUND_SERVICE_HEALTH` / `FOREGROUND_SERVICE_LOCATION`):**
  - La sesión deportiva se ejecuta dentro de un `Foreground Service` vinculado con notificación activa (`POST_NOTIFICATIONS`), evitando que el sistema operativo suspenda o destruya el proceso de captura cuando la pantalla del reloj entra en modo ambiente o se apaga.
- **Integración con Health Services API:**
  - Consume deltas de distancia y velocidad directamente desde los sensores de hardware del reloj (GPS asistido por `ACCESS_FINE_LOCATION` o podómetro interno mediante `ACTIVITY_RECOGNITION`).
- **Degradación Elegante (`Modo Solo Tiempo`):**
  - Si Health Services no está disponible en el dispositivo, los permisos son denegados o el hardware carece de soporte de distancia, el motor no bloquea la sesión: conmuta automáticamente a **Modo Solo Tiempo**, registrando la duración exacta y asignando `—` a distancia, ritmo y calorías sin inventar métricas sintéticas.

### 2.3 Motor de Sincronización Transaccional Offline-First (`Wear OS Data Layer`)
- **Arquitectura *Store & Forward* con Idempotencia:**
  - El reloj actúa como fuente autoritativa de las actividades (`ActivityRecord`) y el teléfono como fuente autoritativa del perfil (`UserProfile`).
  - Cada sesión registrada en el reloj recibe un identificador único universal (**UUID**) y se persiste inmediatamente en `wear_data` (`DataStore`) con bandera `synced = false`.
- **Protocolo de Rutas y Confirmación (`DataLayerPaths`):**
  - `/profile`: El teléfono publica el peso actualizado hacia el reloj.
  - `/activity/{id}`: El reloj transmite el payload de la actividad finalizada hacia `PhoneDataLayerService`.
  - `/ack/{id}`: Al persistir la actividad en `phone_data` mediante una operación `upsert` indexada por UUID (garantizando cero duplicados ante reintentos), el teléfono emite un acuse de recibo (`ack`) que actualiza el estado en el reloj a `synced = true`.
  - `/request-sync` y `/request-profile`: Canales de mensajería bajo demanda para forzar el reenvío de registros pendientes o solicitar el perfil actualizado al reconectar nodos.

### 2.4 Motor Matemático y Reglas de Dominio (`:shared`)
- **Ecuaciones Metabólicas Netas (`CalorieCalculator`):**
  - Calcula exclusivamente el gasto energético atribuible al ejercicio físico (excluyendo el metabolismo basal) mediante las ecuaciones estándar del **ACSM** (*American College of Sports Medicine*):
    - **Caminata:** $\text{VO}_2 = 0.1 \times \text{velocidad (m/min)} + 3.5$
    - **Carrera:** $\text{VO}_2 = 0.2 \times \text{velocidad (m/min)} + 3.5$
    - **Conversión Calórica:** $\text{MET} = \frac{\text{VO}_2}{3.5} \implies \text{kcal netas} = (\text{MET} - 1) \times 3.5 \times \frac{\text{peso (kg)}}{200} \times \text{minutos}$
  - El cálculo exige precondiciones válidas (peso entre `20–300 kg`, distancia $> 0$ y duración suficiente); de lo contrario retorna `null` y la capa de presentación renderiza `—`.
- **Cálculo de Ritmo y Formateo Localizado (`MetricsCalculator` & `Formatters`):**
  - Determina el ritmo promedio ($\text{min/km}$) protegiendo divisiones entre cero y estandariza la presentación de fechas, distancias y unidades en español.

### 2.5 Privacidad por Diseño y Seguridad Local
- **Cero Rastreo Geográfico:** No se solicitan permisos de ubicación en segundo plano (`ACCESS_BACKGROUND_LOCATION`), ni frecuencia cardiaca (`BODY_SENSORS`), ni se almacenan coordenadas GPS (latitud/longitud); únicamente se procesa el escalar de distancia acumulada.
- **Aislamiento de Datos Locales:** Tanto `phone_data` como `wear_data` residen en almacenamiento privado mediante `DataStore Preferences` con `android:allowBackup="false"` en el manifiesto para impedir la extracción automática de respaldos no cifrados.

---

## 3. Estructura del Proyecto

```text
CaminarCorrerWear/
├── shared/    # Modelos de dominio, ecuaciones ACSM, formateadores y contratos Data Layer
├── phone/     # App Android: PhoneApplication, PhoneDataLayerService y pantallas Compose M3
├── wear/      # App Wear OS: ExerciseService, WatchDataLayerService y pantallas Wear M3
├── docs/      # Documentación operativa (COMANDOS.md) y capturas de verificación
├── scripts/   # Automatización PowerShell para despliegue en emuladores emparejados
├── gradle/    # Version Catalog (libs.versions.toml) y Gradle Wrapper
└── dist/      # Artefactos APK compilados (ignorados en control de versiones)
