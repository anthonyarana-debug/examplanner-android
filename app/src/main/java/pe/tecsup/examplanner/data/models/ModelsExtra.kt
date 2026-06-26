package pe.tecsup.examplanner.data.models

import com.google.gson.annotations.SerializedName

// ═══════════════════════ NOTAS  (GET /api/canvas/notas/) ═════════════════════

data class NotaCurso(
    val curso: String,
    @SerializedName("nota_actual") val notaActual: Double?,
    @SerializedName("nota_letra") val notaLetra: String?
)

data class NotasResponse(
    val notas: List<NotaCurso> = emptyList(),
    @SerializedName("promedio_general") val promedioGeneral: Double?,
    @SerializedName("total_cursos") val totalCursos: Int = 0,
    val error: String? = null
)

// ═════════════════════ ANUNCIOS  (GET /api/canvas/anuncios/) ═════════════════

data class Anuncio(
    val titulo: String,
    val curso: String,
    val fecha: String?,
    val mensaje: String?,
    val url: String?
)

data class AnunciosResponse(
    val anuncios: List<Anuncio> = emptyList(),
    val total: Int = 0,
    val error: String? = null
)

// ═══════════════════ MATERIALES  (GET /api/canvas/materiales/) ═══════════════

data class MaterialItem(
    val titulo: String,
    val tipo: String?,
    val url: String?
)

data class Modulo(
    val nombre: String,
    val items: List<MaterialItem> = emptyList()
)

data class CursoMateriales(
    val curso: String,
    val modulos: List<Modulo> = emptyList()
)

data class MaterialesResponse(
    val cursos: List<CursoMateriales> = emptyList(),
    @SerializedName("total_cursos") val totalCursos: Int = 0,
    val error: String? = null
)

// ═══════════════════════ HORARIOS  (/api/horarios/) ══════════════════════════

data class Horario(
    val id: Int,
    val curso: String,
    val codigo: String,
    val aula: String,
    val dia: Int,
    @SerializedName("dia_nombre") val diaNombre: String,
    @SerializedName("hora_inicio") val horaInicio: String,
    @SerializedName("hora_fin") val horaFin: String
)

data class HorarioCreateRequest(
    val curso: String,
    val codigo: String,
    val aula: String,
    val dia: Int,
    @SerializedName("hora_inicio") val horaInicio: String,
    @SerializedName("hora_fin") val horaFin: String
)

data class ProximaClaseResponse(
    @SerializedName("en_curso") val enCurso: Boolean,
    @SerializedName("minutos_para_inicio") val minutosParaInicio: Int?,
    val clase: Horario?
)

// ═════════════════════ ASISTENCIAS  (/api/asistencias/) ══════════════════════

data class BloqueCurso(
    val id: Int,
    val curso: String,
    val tipo: String,
    @SerializedName("total_sesiones") val totalSesiones: Int,
    @SerializedName("duracion_sesion") val duracionSesion: String,
    @SerializedName("horas_totales") val horasTotales: Double
)

data class BloqueCreateRequest(
    val curso: String,
    val tipo: String,
    @SerializedName("total_sesiones") val totalSesiones: Int,
    @SerializedName("duracion_sesion") val duracionSesion: Double
)

data class AsistenciaRegistro(
    val id: Int,
    val bloque: Int,
    val curso: String,
    val tipo: String,
    val fecha: String,
    val estado: String
)

data class AsistenciaCreateRequest(
    val bloque: Int,
    val fecha: String,
    val estado: String   // "presente" | "falta"
)

data class BloqueResumen(
    val tipo: String,
    val faltas: Int,
    val sesiones: Int,
    @SerializedName("duracion_sesion") val duracionSesion: Double,
    @SerializedName("horas_falta") val horasFalta: Double
)

data class CursoResumen(
    val curso: String,
    @SerializedName("horas_totales") val horasTotales: Double,
    @SerializedName("horas_falta") val horasFalta: Double,
    @SerializedName("porcentaje_inasistencia") val porcentajeInasistencia: Double,
    val riesgo: Boolean,
    @SerializedName("horas_margen") val horasMargen: Double,
    val bloques: List<BloqueResumen> = emptyList()
)

data class ResumenAsistenciaResponse(
    val cursos: List<CursoResumen> = emptyList(),
    val umbral: Double = 30.0
)

// ═══════════════════════ ASISTENTE IA  (/api/asistente/) ═════════════════════

data class AsistenteRequest(
    val mensaje: String
)

data class AsistenteResponse(
    val respuesta: String? = null,
    val error: String? = null
)
