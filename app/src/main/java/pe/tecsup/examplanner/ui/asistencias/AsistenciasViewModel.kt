package pe.tecsup.examplanner.ui.asistencias

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.examplanner.data.models.*
import pe.tecsup.examplanner.data.repository.ExtraRepository
import pe.tecsup.examplanner.data.repository.Result
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AsistenciasUiState(
    val isLoading: Boolean = false,
    val resumen: List<CursoResumen> = emptyList(),
    val bloques: List<BloqueCurso> = emptyList(),
    val umbral: Double = 30.0,
    val error: String? = null,
    val mensaje: String? = null
)

class AsistenciasViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExtraRepository(application)
    private val _ui = MutableStateFlow(AsistenciasUiState())
    val ui: StateFlow<AsistenciasUiState> = _ui

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, error = null)
            val resumenR = repo.getResumenAsistencias()
            val bloquesR = repo.getBloques()
            val resumen = (resumenR as? Result.Success)?.data
            val bloques = (bloquesR as? Result.Success)?.data ?: emptyList()
            val err = (bloquesR as? Result.Error)?.message
            _ui.value = _ui.value.copy(
                isLoading = false,
                resumen = resumen?.cursos ?: emptyList(),
                umbral = resumen?.umbral ?: 30.0,
                bloques = bloques,
                error = err
            )
        }
    }

    fun agregarBloque(curso: String, tipo: String, sesiones: Int, duracion: Double) {
        viewModelScope.launch {
            when (val r = repo.crearBloque(BloqueCreateRequest(curso, tipo, sesiones, duracion))) {
                is Result.Success -> { _ui.value = _ui.value.copy(mensaje = "Bloque agregado"); cargar() }
                is Result.Error -> _ui.value = _ui.value.copy(error = r.message)
            }
        }
    }

    fun eliminarBloque(id: Int) {
        viewModelScope.launch {
            when (val r = repo.eliminarBloque(id)) {
                is Result.Success -> { _ui.value = _ui.value.copy(mensaje = "Bloque eliminado"); cargar() }
                is Result.Error -> _ui.value = _ui.value.copy(error = r.message)
            }
        }
    }

    /** Registra una sesión de hoy (presente/falta) en un bloque. */
    fun registrarHoy(bloqueId: Int, estado: String) {
        viewModelScope.launch {
            val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            when (val r = repo.registrarAsistencia(AsistenciaCreateRequest(bloqueId, hoy, estado))) {
                is Result.Success -> {
                    val txt = if (estado == "falta") "Falta registrada" else "Asistencia registrada"
                    _ui.value = _ui.value.copy(mensaje = txt); cargar()
                }
                is Result.Error -> _ui.value = _ui.value.copy(error = r.message)
            }
        }
    }

    fun limpiarMensajes() { _ui.value = _ui.value.copy(mensaje = null, error = null) }
}
