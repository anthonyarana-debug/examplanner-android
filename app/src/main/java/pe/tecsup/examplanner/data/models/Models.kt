package pe.tecsup.examplanner.data.models

import com.google.gson.annotations.SerializedName

// ─── AUTH ────────────────────────────────────────────────────────────────────

data class RegistroRequest(
    val email: String,
    val nombre: String,
    val password: String,
    @SerializedName("password_confirmacion") val passwordConfirmacion: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class Tokens(
    val access: String,
    val refresh: String
)

data class Estudiante(
    val id: Int,
    val email: String,
    val nombre: String,
    @SerializedName("canvas_conectado") val canvasConectado: Boolean,
    @SerializedName("fecha_registro") val fechaRegistro: String
)

data class AuthResponse(
    val mensaje: String,
    val estudiante: Estudiante,
    val tokens: Tokens
)

// ─── CANVAS ──────────────────────────────────────────────────────────────────

data class CanvasTokenRequest(
    val token: String
)

data class CanvasConectarResponse(
    val mensaje: String,
    @SerializedName("tareas_importadas") val tareasImportadas: Int,
    @SerializedName("examenes_importados") val examenesImportados: Int
)

// ─── TAREAS ──────────────────────────────────────────────────────────────────

data class Tarea(
    val id: Int,
    val nombre: String,
    val curso: String,
    @SerializedName("fecha_limite") val fechaLimite: String,
    val completada: Boolean,
    @SerializedName("fecha_completada") val fechaCompletada: String?,
    val origen: String,          // "canvas" | "manual"
    @SerializedName("canvas_id") val canvasId: String?,
    val descripcion: String?,
    @SerializedName("dias_restantes") val diasRestantes: Int,
    @SerializedName("esta_vencida") val estaVencida: Boolean,
    @SerializedName("fecha_creacion") val fechaCreacion: String
)

data class TareaCreateRequest(
    val nombre: String,
    val curso: String,
    @SerializedName("fecha_limite") val fechaLimite: String,
    val descripcion: String? = null
)

data class CompletarRequest(
    val completada: Boolean
)

data class CompletarResponse(
    val mensaje: String,
    val tarea: Tarea
)

// ─── EXÁMENES ────────────────────────────────────────────────────────────────

data class Examen(
    val id: Int,
    val curso: String,
    val fecha: String,
    val descripcion: String?,
    val origen: String,
    @SerializedName("canvas_id") val canvasId: String?,
    @SerializedName("dias_restantes") val diasRestantes: Int,
    val proximo: Boolean,
    @SerializedName("fecha_creacion") val fechaCreacion: String
)

data class ExamenCreateRequest(
    val curso: String,
    val fecha: String,
    val descripcion: String? = null
)

// ─── PENDIENTES ──────────────────────────────────────────────────────────────

data class PendientesResponse(
    val tareas: List<Tarea>,
    val examenes: List<Examen>,
    @SerializedName("total_pendientes") val totalPendientes: Int,
    @SerializedName("progreso_porcentaje") val progresoPorcentaje: Double,
    @SerializedName("canvas_conectado") val canvasConectado: Boolean,
    val mensaje: String?
)

// ─── ERRORES ─────────────────────────────────────────────────────────────────

data class ErrorResponse(
    val error: String? = null,
    val errores: Map<String, List<String>>? = null,
    @SerializedName("registro_manual_habilitado") val registroManualHabilitado: Boolean = false
)
