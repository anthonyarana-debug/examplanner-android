# ExamPlanner

App móvil para estudiantes de Tecsup que junta en un solo lugar las tareas, los exámenes, las notas, el horario y las asistencias del ciclo. Se conecta con Canvas para traer las entregas pendientes de cada curso y tiene un asistente con IA que ayuda a decidir qué hacer primero. La desarrollamos como proyecto final del curso de Programación en Móviles.

Repositorios:
- App Android: https://github.com/anthonyarana-debug/examplanner-android
- Backend: https://github.com/anthonyarana-debug/examplanner-backend

Video demo: https://youtu.be/YH826jwBzH0

## El problema

En la semana de exámenes la información académica de un estudiante de Tecsup está repartida en Canvas. Cada curso tiene su propio calendario de tareas y exámenes, y hay que entrar módulo por módulo para no perder una entrega. Es fácil enterarse tarde de algo o no saber cuántas faltas llevas en un curso. ExamPlanner reúne todo eso en una sola app y además usa IA para decir qué priorizar.

## Capturas

![Inicio de sesión](docs/01-login.jpg)

![Inicio con las tareas de Canvas](docs/02-inicio.jpg)

![Asistente con IA](docs/03-asistente.jpg)

![Repasar con IA](docs/04-repasar.jpg)

![Notas](docs/05-notas.jpg)

![Horario](docs/06-horario.jpg)

## Stack

App Android
- Kotlin y Jetpack Compose
- Material Design 3 con tema propio
- Arquitectura MVVM (vista, ViewModel con StateFlow y Repository)
- Coroutines y Flow
- Retrofit, Gson y OkHttp para consumir la API
- DataStore para guardar la sesión y los tokens

Backend
- Django y Django REST Framework
- Autenticación con JWT
- Base de datos PostgreSQL
- Conexión con Canvas y con la IA
- Desplegado en api.stackpe.online con Gunicorn, Nginx y certificado SSL

## Componente de IA

Usamos el modelo Llama 3.3 (llama-3.3-70b-versatile) a través de Groq. La IA no es un botón aparte, está dentro del flujo de la app en dos partes:

- Asistente: el estudiante pregunta cosas como "¿qué priorizo esta semana?" o "¿en qué curso estoy en riesgo de faltas?". El backend junta el contexto real del alumno (tareas, exámenes y asistencia de la base de datos), arma el prompt y se lo manda al modelo. La respuesta aparece en el chat.
- Repasar: sobre una tarea traída de Canvas, la IA lee el título y la descripción de la entrega y genera los puntos que conviene estudiar, más una búsqueda de material en YouTube.

En los dos casos trabaja con datos reales y muestra estados de carga y de error en la pantalla.

## Servicio externo

La app se conecta con Canvas LMS por su API para importar automáticamente las tareas y entregas del estudiante, que después aparecen en el inicio con su fecha límite y su curso. Las credenciales del proveedor están en variables de entorno del backend, no en el código.

## Integrantes

- Pablo Rojas
- Fabricio Tello
- Diego Aragón
- Anthony Arana

## Configuración

### Backend

1. Instalar las dependencias:

```
pip install -r requirements.txt
```

2. Crear un archivo `.env` en la raíz (no se sube al repo):

```
SECRET_KEY=tu-secret-key
DEBUG=False

IA_API_KEY=tu-api-key-de-groq
IA_BASE_URL=https://api.groq.com/openai/v1
IA_MODEL=llama-3.3-70b-versatile

CANVAS_BASE_URL=https://tecsup.instructure.com

USE_POSTGRES=True
DB_NAME=examplanner
DB_USER=postgres
DB_PASSWORD=tu-password
DB_HOST=localhost
DB_PORT=5432
```

La API key de Groq se saca gratis en https://console.groq.com

3. Migrar y levantar el servidor:

```
python manage.py migrate
python manage.py runserver
```

### App Android

1. Abrir el proyecto en Android Studio y sincronizar Gradle.
2. Por defecto la app apunta a https://api.stackpe.online. Si usas tu propio backend, cambia la `BASE_URL` en `data/api/RetrofitClient.kt`.
3. Correr en un celular o emulador con conexión a internet.
4. Registrarse con un correo `@tecsup.edu.pe`, iniciar sesión y conectar la cuenta de Canvas desde el botón de sincronizar para traer las tareas.
