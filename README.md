# ExamPlanner — App Android

App móvil para estudiantes de Tecsup que centraliza tareas, exámenes, horario,
asistencias y notas académicas en un solo lugar, sincronizando datos desde
Canvas LMS y agregando un asistente con IA para resolver dudas y armar planes
de repaso personalizados.

## Stack tecnológico

- **Kotlin + Jetpack Compose** (Material 3) — UI declarativa
- **MVVM**: `ViewModel` + `StateFlow` por pantalla, `Repository` como capa de datos
- **Retrofit + Gson** — consumo de la API REST
- **DataStore Preferences** — persistencia local del token JWT de sesión
- **Coroutines** (`viewModelScope`) — operaciones asíncronas
- **Navigation Compose** — navegación entre pantallas
- **Backend:** Django REST Framework, desplegado en `https://api.stackpe.online`

## Componente de IA

El módulo **Asistente** (`ui/asistente/`) consume el endpoint `POST /api/asistente/`
del backend, el cual usa la API de **Groq (Llama 3.3)** para responder preguntas
del estudiante con contexto real de sus tareas, exámenes y horario. La pantalla
maneja estado de carga ("pensando...") y errores de red mostrando un mensaje al
usuario en vez de crashear. El mismo motor de IA potencia el módulo **Repaso**,
que arma un plan de estudio sugerido a partir de los exámenes próximos del
estudiante.

## Capturas de pantalla

> Reemplazar con capturas reales antes de la entrega final.

| Login | Home | Asistente IA |
|---|---|---|
| _(captura pendiente)_ | _(captura pendiente)_ | _(captura pendiente)_ |

## Video de demostración

> Pendiente: agregar aquí el link al video de YouTube (3-5 min, narrado en
> español, mostrando el flujo principal y la IA con datos reales).

`Link: (pendiente)`

## Integrantes

- Pablo Rojas — Scrum Master
- Fabricio Tello — Frontend
- Diego Aragón — Backend / Base de datos
- Anthony Arana — Product Owner / Arquitectura backend

## Cómo abrir el proyecto

1. Clona o descomprime el repositorio.
2. Abre **Android Studio** → `File > Open` → selecciona la carpeta del proyecto.
3. Espera a que Gradle sincronice (puede tardar 2-3 minutos la primera vez).
4. Conecta tu celular Android por USB con **Depuración USB activada**, o usa un emulador.
5. Presiona ▶ **Run** o `Shift + F10`.

**Requisitos:** Android 7.0 (API 24) o superior · Android Studio Hedgehog o superior.

### Configuración de API keys

El proyecto Android **no requiere ninguna API key propia**: consume directamente
la API ya desplegada en `https://api.stackpe.online`. La key de Groq usada por
el asistente de IA vive del lado del backend, en su archivo `.env` /
`local.properties` correspondiente (no en este repositorio).

## Módulos y pantallas

| Módulo | Pantallas | Descripción |
|---|---|---|
| Autenticación | Login, Registro | Login JWT, registro con correo institucional `@tecsup.edu.pe` |
| Home | Lista de pendientes | Tareas + exámenes combinados, agregar/editar/completar |
| Académico | Notas, anuncios, materiales | Datos sincronizados desde Canvas |
| Horario | Horario de clases | Próxima clase y horario semanal |
| Asistencias | Registro y resumen | Control de asistencias por curso, con resumen visual |
| Asistente IA | Chat + Repaso | Preguntas al asistente y generación de plan de repaso |

## Endpoints principales que consume la app

| Endpoint | Método | Uso |
|---|---|---|
| `api/auth/login/` · `registro/` · `logout/` | POST | Sesión del usuario (JWT) |
| `api/pendientes/` | GET | Tareas + exámenes combinados (Home) |
| `api/tareas/` · `api/examenes/` | GET/POST | Crear y listar tareas y exámenes |
| `api/canvas/conectar/` · `sincronizar/` | POST | Vincular e importar datos de Canvas |
| `api/canvas/notas/` · `anuncios/` · `materiales/` | GET | Módulo Académico |
| `api/horarios/` · `api/horarios/proxima/` | GET/POST | Horario de clases |
| `api/asistencias/` · `/resumen/` · `/bloques/` | GET/POST | Control de asistencias |
| `api/asistente/` | POST | Pregunta al asistente con IA |

## Arquitectura

```
app/src/main/java/pe/tecsup/examplanner/
├── data/
│   ├── api/            ← ExamPlannerApi (Retrofit), RetrofitClient (JWT interceptor)
│   ├── models/          ← Data classes que mapean las respuestas del backend
│   └── repository/      ← Capa de datos (Result<Success/Error>), guarda JWT en DataStore
├── ui/
│   ├── auth/             ← Login, Registro
│   ├── home/             ← Pendientes, diálogos de tarea/examen/Canvas
│   ├── academico/        ← Notas, anuncios, materiales
│   ├── horario/          ← Horario de clases
│   ├── asistencias/       ← Registro y resumen de asistencias
│   ├── asistente/         ← Chat con IA y plan de repaso
│   └── theme/             ← Tema Material 3 personalizado (colores Tecsup)
├── notificaciones/        ← Recordatorios locales de tareas y exámenes
├── widget/                 ← Widget de pantalla de inicio con pendientes urgentes
├── Navigation.kt           ← NavHost de toda la app
└── MainActivity.kt         ← Entry point
```