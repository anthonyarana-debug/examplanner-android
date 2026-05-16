package pe.tecsup.examplanner.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.tecsup.examplanner.data.api.RetrofitClient
import pe.tecsup.examplanner.data.api.dataStore
import pe.tecsup.examplanner.data.models.*

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class ExamPlannerRepository(private val context: Context) {

    private val api = RetrofitClient.api
    private val dataStore = context.dataStore

    // ── SESSION ──────────────────────────────────────────────────────────────

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        !prefs[RetrofitClient.TOKEN_KEY].isNullOrBlank()
    }

    val nombreUsuario: Flow<String> = dataStore.data.map { prefs ->
        prefs[RetrofitClient.NOMBRE_KEY] ?: ""
    }

    val canvasConectado: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[RetrofitClient.CANVAS_KEY] == "true"
    }

    private suspend fun guardarSesion(response: AuthResponse) {
        dataStore.edit { prefs ->
            prefs[RetrofitClient.TOKEN_KEY]  = response.tokens.access
            prefs[RetrofitClient.REFRESH_KEY] = response.tokens.refresh
            prefs[RetrofitClient.NOMBRE_KEY] = response.estudiante.nombre
            prefs[RetrofitClient.EMAIL_KEY]  = response.estudiante.email
            prefs[RetrofitClient.CANVAS_KEY] = response.estudiante.canvasConectado.toString()
        }
    }

    suspend fun cerrarSesion(refreshToken: String) {
        try {
            api.logout(mapOf("refresh" to refreshToken))
        } catch (_: Exception) {}
        dataStore.edit { it.clear() }
    }

    // ── AUTH ─────────────────────────────────────────────────────────────────

    suspend fun registro(
        email: String, nombre: String, password: String
    ): Result<AuthResponse> {
        return try {
            val response = api.registro(
                RegistroRequest(email, nombre, password, password)
            )
            if (response.isSuccessful) {
                val body = response.body()!!
                guardarSesion(body)
                Result.Success(body)
            } else {
                val msg = when (response.code()) {
                    400 -> "Solo se permiten correos institucionales (@tecsup.edu.pe)"
                    else -> "Error al registrarse. Intenta de nuevo."
                }
                Result.Error(msg)
            }
        } catch (e: Exception) {
            Result.Error("Sin conexión al servidor. Verifica tu internet.")
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                guardarSesion(body)
                Result.Success(body)
            } else {
                Result.Error("Usuario o contraseña incorrectos")
            }
        } catch (e: Exception) {
            Result.Error("Sin conexión al servidor. Verifica tu internet.")
        }
    }

    // ── CANVAS ───────────────────────────────────────────────────────────────

    suspend fun conectarCanvas(token: String): Result<CanvasConectarResponse> {
        return try {
            val response = api.conectarCanvas(CanvasTokenRequest(token))
            if (response.isSuccessful) {
                dataStore.edit { prefs ->
                    prefs[RetrofitClient.CANVAS_KEY] = "true"
                }
                Result.Success(response.body()!!)
            } else {
                Result.Error(
                    "No se pudo conectar con Canvas. Puedes agregar tus tareas manualmente."
                )
            }
        } catch (e: Exception) {
            Result.Error("Sin conexión al servidor.")
        }
    }

    suspend fun sincronizarCanvas(): Result<CanvasConectarResponse> {
        return try {
            val response = api.sincronizarCanvas()
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error al sincronizar Canvas.")
            }
        } catch (e: Exception) {
            Result.Error("Sin conexión al servidor.")
        }
    }

    // ── PENDIENTES ───────────────────────────────────────────────────────────

    suspend fun getPendientes(): Result<PendientesResponse> {
        return try {
            val response = api.getPendientes()
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error al cargar pendientes.")
            }
        } catch (e: Exception) {
            Result.Error("Sin conexión al servidor.")
        }
    }

    // ── TAREAS ───────────────────────────────────────────────────────────────

    suspend fun crearTarea(
        nombre: String, curso: String, fechaLimite: String, descripcion: String?
    ): Result<Tarea> {
        return try {
            val response = api.crearTarea(
                TareaCreateRequest(nombre, curso, fechaLimite, descripcion)
            )
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error al crear la tarea.")
            }
        } catch (e: Exception) {
            Result.Error("Sin conexión al servidor.")
        }
    }

    suspend fun completarTarea(id: Int, completada: Boolean): Result<CompletarResponse> {
        return try {
            val response = api.completarTarea(id, CompletarRequest(completada))
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error al actualizar la tarea.")
            }
        } catch (e: Exception) {
            Result.Error("Sin conexión al servidor.")
        }
    }

    suspend fun eliminarTarea(id: Int): Result<Unit> {
        return try {
            val response = api.eliminarTarea(id)
            if (response.isSuccessful || response.code() == 204) {
                Result.Success(Unit)
            } else {
                Result.Error("Error al eliminar la tarea.")
            }
        } catch (e: Exception) {
            Result.Error("Sin conexión al servidor.")
        }
    }

    // ── EXÁMENES ─────────────────────────────────────────────────────────────

    suspend fun crearExamen(
        curso: String, fecha: String, descripcion: String?
    ): Result<Examen> {
        return try {
            val response = api.crearExamen(ExamenCreateRequest(curso, fecha, descripcion))
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error al registrar el examen.")
            }
        } catch (e: Exception) {
            Result.Error("Sin conexión al servidor.")
        }
    }
}
