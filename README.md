# ExamPlanner — Frontend Android (Sprint 1)

App móvil para estudiantes de Tecsup que conecta con el backend Django en
`https://api.stackpe.online` y permite gestionar tareas y exámenes de la semana de parciales.

---

## Cómo abrir el proyecto

1. **Descomprime** `examplanner_android.zip`
2. Abre **Android Studio** → `File > Open` → selecciona la carpeta `examplanner_android`
3. Espera que Gradle sincronice (puede tardar 2-3 minutos la primera vez)
4. Conecta tu celular Android por USB con **Depuración USB activada**
5. Presiona ▶ **Run** o `Shift+F10`

> Requisito mínimo: Android 8.0 (API 26) · Android Studio Hedgehog o superior

---

## Estructura del proyecto

```
app/src/main/java/pe/tecsup/examplanner/
│
├── data/
│   ├── api/
│   │   ├── ExamPlannerApi.kt       ← Endpoints Retrofit (mapean exactamente el backend)
│   │   └── RetrofitClient.kt       ← OkHttp con interceptor JWT + DataStore
│   ├── models/
│   │   └── Models.kt               ← Data classes que mapean las respuestas Django
│   └── repository/
│       └── ExamPlannerRepository.kt ← Capa de datos, guarda JWT en DataStore
│
├── ui/
│   ├── auth/
│   │   ├── AuthViewModel.kt        ← Estado de login y registro
│   │   ├── LoginScreen.kt          ← Pantalla de inicio de sesión
│   │   └── RegistroScreen.kt       ← Pantalla de registro (@tecsup.edu.pe)
│   ├── home/
│   │   ├── HomeViewModel.kt        ← Estado de pendientes, Canvas, tareas
│   │   ├── HomeScreen.kt           ← Pantalla principal con lista de tareas/exámenes
│   │   └── Dialogs.kt              ← Dialog Canvas, agregar tarea, agregar examen
│   └── theme/
│       └── Theme.kt                ← Material 3 con colores azul Tecsup
│
├── Navigation.kt                   ← NavHost: Login → Registro → Home
└── MainActivity.kt                 ← Entry point
```

---

## Pantallas del Sprint 1

| Pantalla | Historia | CAs implementados |
|----------|----------|-------------------|
| Login | Inicio de sesión [2 SP] | CA1: error sin especificar cuál falló · CA2: redirige al Home en < 3s |
| Registro | Registro con correo institucional [3 SP] | CA1: valida @tecsup.edu.pe en tiempo real · CA2: redirige al Home tras crear cuenta |
| Home - Tareas | Ver pendientes [3 SP] | CA1: ordenado por fecha · CA2: mensaje si no hay tareas |
| Home - Checkbox | Marcar completada [2 SP] | CA1: actualiza progreso inmediatamente · botón deshacer en 5s |
| Dialog Canvas | Conexión con Canvas [5 SP] | CA1: importa 30 días · CA2: habilita registro manual si falla |

---

## Endpoints que consume

| Endpoint | Método | Pantalla |
|----------|--------|----------|
| `/api/auth/registro/` | POST | Registro |
| `/api/auth/login/` | POST | Login |
| `/api/auth/logout/` | POST | Botón cerrar sesión |
| `/api/canvas/conectar/` | POST | Dialog Canvas |
| `/api/canvas/sincronizar/` | POST | Botón sincronizar |
| `/api/pendientes/` | GET | Home (tareas + exámenes combinados) |
| `/api/tareas/` | POST | Dialog agregar tarea manual |
| `/api/tareas/{id}/completar/` | PATCH | Checkbox de tarea |
| `/api/tareas/{id}/` | DELETE | Botón eliminar tarea manual |
| `/api/examenes/` | POST | Dialog agregar examen |

---

## Cómo obtener el token de Canvas

1. Entra a `canvas.tecsup.edu.pe`
2. Ve a **Cuenta → Configuración**
3. Baja hasta **Tokens de Acceso → Generar nuevo token**
4. Copia el token
5. En la app: ícono 🔗 en la barra superior → pega el token → Conectar

---

## Velocidad calculada (Sprint 1)

- Developers: 2 (Fabricio - Frontend, Diego - Backend)
- Horas/día: 6 · Días: 10 · Factor enfoque: 0.5
- **Velocidad = 15 Story Points**
- Historias en este sprint: HU-01 (3) + HU-02 (2) + HU-22 (5) + HU-05 (3) + HU-07 (2) = 15 SP ✅
