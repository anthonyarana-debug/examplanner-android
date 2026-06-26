package pe.tecsup.examplanner.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.tecsup.examplanner.data.api.RetrofitClient
import pe.tecsup.examplanner.data.api.dataStore
import pe.tecsup.examplanner.data.models.*
import retrofit2.Response

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class ExamPlannerRepository(private val context: Context) {

    private val api = RetrofitClient.api
    private val dataStore = context.dataStore
    private val gson = Gson()

    /**
     * Lee el mensaje de error REAL que envía el backend, en vez de mostrar
     * un texto fijo. Soporta los dos formatos del backend:
     *   {"errores": {"email": ["Ya existe Estudiante con este email."]}}
     *   {"error": "mensaje suelto"}
     */
    private fun <T> parseError(response: Response<T>, fallback: String): String {
        return try {
            val raw = response.errorBody()?.string()
            if (raw.isNullOrBlank()) return fallback
            val err = gson.fromJson(raw, ErrorResponse::class.java)
            when {
                !err.errores.isNullOrEmpty() ->
                    err.errores.values.firstOrNull()?.firstOrNull() ?: fallback
                !err.error.isNullOrBlank() -> err.error!!
                else -> fallback
            }
        } catch (e: Exception) {
            fallback
        }
    }

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
                // Muestra el mensaje real del backend (correo ya existe,
                // dominio inválido, contraseña corta, etc.)
                Result.Error(parseError(response, "No se pudo crear la cuenta."))
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
            } else if (response.code() == 401) {
                Result.Error("Usuario o contraseña incorrectos")
            } else {
                Result.Error(parseError(response, "No se pudo iniciar sesión."))
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

    suspend fun editarTarea(
        id: Int, nombre: String, curso: String, fechaLimite: String, descripcion: String?
    ): Result<Tarea> {
        return try {
            val response = api.editarTarea(
                id, TareaCreateRequest(nombre, curso, fechaLimite, descripcion)
            )
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error al editar la tarea.")
            }
        } catch (e: Exception) {
            Result.Error("Sin conexión al servidor.")
        }
    }
}