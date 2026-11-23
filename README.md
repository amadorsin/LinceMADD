# LinceOPS (Proyecto Lince)

![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%204-Java%2021-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-4169E1?logo=postgresql&logoColor=white)
![Render](https://img.shields.io/badge/Deploy-Render%20%2B%20Docker-46E3B7?logo=render&logoColor=black)

## 1. Nombre y Propósito

**LinceOPS** es una solución móvil y backend diseñada para centralizar la coordinación operativa de una empresa de turismo receptivo. Su propósito es gestionar servicios, asignaciones, disponibilidad y trazabilidad en terreno de conductores, guías y coordinadores, reemplazando procesos manuales mediante un sistema seguro, auditable y preparado para operar sin conexión en zonas remotas.

---

## 2. Arquitectura y Estructura del Repositorio

El repositorio está organizado como un monorepo con separación clara entre cliente móvil, API REST propia y documentación del proyecto:

```text
LinceMADD/
├── LinceOPS/          # Aplicación Android nativa (Kotlin + Jetpack Compose + MVVM + Room + WorkManager)
├── apiLince/          # Backend REST (Java 21 + Spring Boot 4 + Spring Security JWT + JPA + Docker)
└── docs/              # Identidad visual, diagrama UML, evidencias de diseño y pruebas
```

### Flujo General de Datos
* **Cliente móvil (`LinceOPS`):** implementa arquitectura **MVVM** con separación en capas (`ui`, `viewmodel`, `repository`, `network`, `data/local`, `data/sync`).
* **Integración dual de APIs:**
  * **Southbound API (externa):** consulta catálogo operativo de servicios, guías, transportistas y resumen por oficina (`SCL`, `PUX`, `PUQ`).
  * **API LinceOPS (`apiLince`):** gestiona autenticación JWT, marcajes geolocalizados, geocercas, incidencias, confirmaciones y bloqueos de disponibilidad persistidos en PostgreSQL (Supabase).
* **Estrategia Offline-First:** cuando no hay conectividad o el servidor responde con fallo transitorio (`5xx`), los marcajes se persisten en **Room** (`HitoPendienteDao`) y se sincronizan automáticamente mediante **WorkManager** (`SincronizarHitosWorker`) al recuperar conexión.

---

## 3. Funcionalidades Principales y Reglas de Negocio

### Control de Acceso por Rol (RBAC + JWT)
* **Conductor (Driver) y Guía:** consulta de servicios asignados, confirmación de servicios, marcaje de Check-In / Check-Out con mapa y gestión de bloqueos de disponibilidad.
* **Administrador (Manager):** tablero de coordinación con métricas operativas, recursos activos, monitoreo en vivo de hitos e incidencias, gestión de geocercas y asignación local de contingencia.

### Validaciones de Negocio (Cliente + Servidor)
* **Geocerca de 150 metros (Haversine):** validación de distancia respecto al punto de recogida/término. Los intentos fuera de rango son rechazados (`HTTP 400`) y generan automáticamente una incidencia `FUERA_DE_RADIO` auditable por el Manager.
* **Control de precisión GPS:** validación de señal satelital antes de registrar el Check-In, mostrando distancia directa y radio permitido en el mapa interactivo.
* **Ventana de puntualidad (15 minutos):** control horario del Check-In respecto a la hora programada de pickup.
* **Disponibilidad con anticipación mínima (7 días):** los bloqueos de calendario con menos de 7 días de anticipación son rechazados por el servidor (`HTTP 400`).
* **Idempotencia en sincronización offline:** cada hito pendiente se registra con clave única `${servicioId}_${usuarioId}_${tipo}`. `SincronizarHitosWorker` aplica reintento exponencial (`15 s`, `NetworkType.CONNECTED`), descarta duplicados ya aceptados por el servidor y detiene reintentos infinitos ante rechazos definitivos de negocio (`ERROR_VALIDACION`).

### Experiencia de Usuario (UX/UI)
* **Tema Claro y Modo Oscuro OLED:** soporte completo con `LinceTheme` para reducir fatiga visual y consumo de batería en terreno.
* **Internacionalización (i18n ES / EN):** cambio dinámico de idioma Español / Inglés persistido localmente con **Jetpack DataStore**.
* **Estados de carga y trazabilidad:** skeletons de carga, línea de tiempo de ruta y banner reactivo de cola offline con opción de reintento manual.

---

## 4. Backend, Despliegue y Documentación de API

El backend `apiLince` se encuentra contenerizado con **Docker** y desplegado en **Render**:

* **URL Base:** `https://lincemadd.onrender.com`
* **Documentación interactiva (Swagger UI / OpenAPI 3):** [https://lincemadd.onrender.com/swagger-ui/index.html](https://lincemadd.onrender.com/swagger-ui/index.html)

### Endpoints Publicados (14 rutas)

| Módulo | Endpoints | Seguridad |
| :--- | :--- | :--- |
| **Autenticación** | `POST /api/auth/login` | Público (emite JWT) |
| **Hitos y Marcaje** | `POST /api/hitos/marcaje`, `GET /api/hitos` | JWT requerido |
| **Geocercas** | `GET /api/geocercas`, `PUT/PATCH /api/geocercas/{id}/radio` | JWT + Rol |
| **Incidencias** | `POST /api/incidencias/registrar`, `GET /api/incidencias`, `GET /api/incidencias/servicio/{servicioId}` | JWT requerido |
| **Disponibilidad** | `POST /api/disponibilidad/bloquear`, `GET /api/disponibilidad/mis-bloqueos`, `DELETE /api/disponibilidad/desbloquear/{fecha}` | JWT requerido |
| **Confirmaciones** | `POST /api/confirmaciones/registrar`, `GET /api/confirmaciones/mias`, `GET /api/confirmaciones/servicio/{servicioId}` | JWT requerido |

> **Seguridad y manejo de secretos:** Todas las rutas protegidas rechazan peticiones sin token JWT válido. Las credenciales de base de datos, secretos JWT, llaves de firma (`.jks`) y claves externas se inyectan mediante variables de entorno y están excluidas del control de versiones.

---

## 5. Identidad Visual, Flujo de Usuario y Diseño

El diseño emplea una paleta sobria inspirada en entornos naturales de turismo receptivo, complementada con un modo oscuro OLED de alto contraste para uso nocturno o en exteriores.

* **Logotipo:** ícono minimalista de un lince ubicado en `docs/diseno/logo.png`.
* **Paleta de Colores (Tema Claro):**
  * **Principal (`#4A7C59`):** acciones primarias, botones destacados e indicadores activos.
  * **Secundario (`#EAE6DE`):** superficies y tarjetas de información.
  * **Fondo (`#FAF6F0`):** fondo base de pantallas.
  * **Texto (`#222728`):** tipografía principal de alta legibilidad.
  * **Acento (`#F8E0A8`):** indicadores de tiempo y estados de preparación para marcaje.
* **Diagrama de Flujo UML:** disponible en `docs/diseno/flujo-usuario-uml.png`.

![Diagrama de Flujo UML - LinceOPS](docs/Diseno/flujo-usuario-uml.png)
* **Prototipos de Pantallas (HTML interactivos):**
  * **Acceso / Validación:** `docs/evidencias/Diseno/Interfaces/InicioSesion`
  * **Dashboard por Rol:** `docs/evidencias/Diseno/Interfaces/Manager/DashBoard`, `.../Driver/DashBoard` y `.../Guia/DashBoard`
  * **Feed de Asignaciones / Servicios:** `docs/evidencias/Diseno/Interfaces/Driver/Servicios`, `.../Guia/Servicios` y `.../Manager/Servicios`
  * **Detalle de Servicio:** `docs/evidencias/Diseno/Interfaces/PopUpDetalles`
  * **Marcaje (Check-In / Check-Out):** `docs/evidencias/Diseno/Interfaces/Driver/Marcaje`
  * **Perfil / Disponibilidad:** `docs/evidencias/Diseno/Interfaces/Driver/Perfil` y `.../Guia/Perfil`
  * **Gestión Operativa:** `docs/evidencias/Diseno/Interfaces/Manager/Gestión`

---

## 6. Stack Tecnológico

| Capa | Tecnologías y Librerías |
| :--- | :--- |
| **Android / Frontend** | Kotlin, Jetpack Compose, Material Design 3, Navigation Compose, MVVM, Coroutines & StateFlow |
| **Persistencia y Offline** | Room (`ServicioDao`, `HitoPendienteDao`), WorkManager (`SincronizarHitosWorker`), Jetpack DataStore |
| **Red y Geolocalización** | Retrofit 2, OkHttp 3, Gson, Google Play Services Location (FusedLocationProvider), OSMDroid |
| **Backend** | Java 21, Spring Boot 4, Spring Security (JWT), Spring Data JPA, Hibernate, Springdoc OpenAPI (Swagger) |
| **Base de Datos e Infra** | PostgreSQL (Supabase), Docker, Render |
| **Pruebas y Calidad** | JUnit, Mockito / Coroutines Test, Compose UI Testing, JaCoCo |
| **Diseño y Gestión** | Material Design 3, Stitch, Mermaid.js, GitHub, Trello |

---

## 7. Pruebas Automatizadas y Cobertura

La calidad del proyecto se verifica mediante pruebas unitarias de lógica de negocio (`ViewModel`, `Repository`, validaciones de geocerca/disponibilidad y cola offline) y pruebas de interfaz instrumentadas en Jetpack Compose.

### Comandos de Ejecución
```bash
cd LinceOPS

# Pruebas unitarias locales
./gradlew :app:testDebugUnitTest

# Pruebas instrumentadas de interfaz (requiere dispositivo o emulador)
./gradlew :app:connectedDebugAndroidTest
```

### Resumen de Resultados
<!-- ACTUALIZAR MAÑANA con los valores reales obtenidos tras ejecutar los tests -->
* **Pruebas Unitarias:** `[XX]` pruebas ejecutadas, `100%` aprobadas.
* **Pruebas UI (Compose):** `[X]` flujos críticos validados en dispositivo/emulador.
* **Cobertura de lógica de negocio:** `[XX%]` (evidencias en `docs/evidencias/Pruebas/`).

---

## 8. Integrantes

| Integrante | Rol |
| :--- | :--- |
| **Matías Wenger** | Product Owner / Backend & Despliegue |
| **Darithza Cárdenas** | Software Developer |
| **David Soto** | Software Developer |
| **Amador Suárez** | GUI Developer / Offline Sync |
