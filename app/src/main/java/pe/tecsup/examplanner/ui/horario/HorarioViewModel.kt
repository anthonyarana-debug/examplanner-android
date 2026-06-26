package pe.tecsup.examplanner.ui.horario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.examplanner.data.models.*
import pe.tecsup.examplanner.data.repository.ExtraRepository
import pe.tecsup.examplanner.data.repository.Result

data class HorarioUiState(
    val isLoading: Boolean = false,
    val clases: List<Horario> = emptyList(),
    val proxima: ProximaClaseResponse? = null,
    val error: String? = null,
    val mensaje: String? = null
)

class HorarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExtraRepository(application)
    private val _ui = MutableStateFlow(HorarioUiState())
    val ui: StateFlow<HorarioUiState> = _ui

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, error = null)
            val clasesR = repo.getHorarios()
            val proxR = repo.getProximaClase()
            val clases = (clasesR as? Result.Success)?.data ?: emptyList()
            val proxima = (proxR as? Result.Success)?.data
            val err = (clasesR as? Result.Error)?.message
            _ui.value = _ui.value.copy(isLoading = false, clases = clases, proxima = proxima, error = err)
        }
    }

    fun agregarClase(curso: String, codigo: String, aula: String, dia: Int, ini: String, fin: String) {
        viewModelScope.launch {
            val req = HorarioCreateRequest(curso, codigo, aula, dia, ini, fin)
            when (val r = repo.crearHorario(req)) {
                is Result.Success -> { _ui.value = _ui.value.copy(mensaje = "Clase agregada"); cargar() }
                is Result.Error -> _ui.value = _ui.value.copy(error = r.message)
            }
        }
    }

    fun eliminarClase(id: Int) {
        viewModelScope.launch {
            when (val r = repo.eliminarHorario(id)) {
                is Result.Success -> { _ui.value = _ui.value.copy(mensaje = "Clase eliminada"); cargar() }
                is Result.Error -> _ui.value = _ui.value.copy(error = r.message)
            }
        }
    }

    fun limpiarMensajes() { _ui.value = _ui.value.copy(mensaje = null, error = null) }
}
