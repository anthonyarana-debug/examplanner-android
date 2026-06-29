package pe.tecsup.examplanner.data.models

import com.google.gson.annotations.SerializedName

// ═══════════════════════ REPASAR  (/api/repasar/) ════════════════════════════

data class RepasoRequest(
    val titulo: String,
    val curso: String
)

data class RepasoEnlace(
    val tipo: String,
    val descripcion: String,
    val url: String
)

data class RepasoResponse(
    val titulo: String? = null,
    val curso: String? = null,
    val resumen: String? = null,
    val conceptos: List<String> = emptyList(),
    val busqueda: String? = null,
    val enlaces: List<RepasoEnlace> = emptyList(),
    val error: String? = null
)
