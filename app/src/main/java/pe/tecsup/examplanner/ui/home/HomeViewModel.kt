package pe.tecsup.examplanner.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.examplanner.data.models.*
import pe.tecsup.examplanner.data.repository.ExamPlannerRepository
import pe.tecsup.examplanner.data.repository.Result

data class HomeUiState(
    val isLoading: Boolean = false,
    val pendientes: PendientesResponse? = null,
    val error: String? = null,
    val canvasConectando: Boolean = false,
    val canvasMensaje: String? = null,
    val accionExitosa: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExamPlannerRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    val nombreUsuario = repo.nombreUsuario
    val canvasConectado = repo.canvasConectado

    init {
        cargarPendientes()
    }

    fun cargarPendientes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repo.getPendientes()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pendientes = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun conectarCanvas(token: String) {
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                canvasMensaje = "Ingresa tu token de Canvas"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(canvasConectando = true, canvasMensaje = null)
            when (val result = repo.conectarCanvas(token)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        canvasConectando = false,
                        canvasMensaje = "✅ Canvas conectado — ${result.data.tareasImportadas} tareas importadas"
                    )
                    cargarPendientes() // recargar lista con las tareas nuevas
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    canvasConectando = false,
                    canvasMensaje = result.message
                )
            }
        }
    }

    fun sincronizarCanvas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repo.sincronizarCanvas()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        accionExitosa = "${result.data.tareasImportadas} tareas actualizadas"
                    )
                    cargarPendientes()
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    // CA1 marcar completada: actualiza inmediatamente en la lista local
    fun marcarCompletada(tareaId: Int, completada: Boolean) {
        viewModelScope.launch {
            when (val result = repo.completarTarea(tareaId, completada)) {
                is Result.Success -> {
                    val mensaje = if (completada) "✅ Tarea completada" else "↩ Tarea marcada como pendiente"
                    _uiState.value = _uiState.value.copy(accionExitosa = mensaje)
                    cargarPendientes()
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.message)
            }
        }
    }

    fun eliminarTarea(tareaId: Int) {
        viewModelScope.launch {
            when (val result = repo.eliminarTarea(tareaId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(accionExitosa = "Tarea eliminada")
                    cargarPendientes()
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.message)
            }
        }
    }

    fun crearTarea(nombre: String, curso: String, fechaLimite: String, descripcion: String?) {
        if (nombre.isBlank() || curso.isBlank() || fechaLimite.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Completa nombre, curso y fecha")
            return
        }
        viewModelScope.launch {
            when (val result = repo.crearTarea(nombre, curso, fechaLimite, descripcion)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(accionExitosa = "Tarea creada correctamente")
                    cargarPendientes()
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.message)
            }
        }
    }

    fun crearExamen(curso: String, fecha: String, descripcion: String?) {
        if (curso.isBlank() || fecha.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Completa el curso y la fecha del examen")
            return
        }
        viewModelScope.launch {
            when (val result = repo.crearExamen(curso, fecha, descripcion)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(accionExitosa = "Examen registrado")
                    cargarPendientes()
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.message)
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(
            error = null,
            accionExitosa = null,
            canvasMensaje = null
        )
    }

    suspend fun logout(refreshToken: String) {
        repo.cerrarSesion(refreshToken)
    }
}
