package pe.tecsup.examplanner.ui.academico

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.examplanner.data.models.*
import pe.tecsup.examplanner.data.repository.ExtraRepository
import pe.tecsup.examplanner.data.repository.Result

data class AcademicoUiState(
    val cargandoNotas: Boolean = false,
    val cargandoAnuncios: Boolean = false,
    val cargandoMateriales: Boolean = false,
    val notas: NotasResponse? = null,
    val anuncios: List<Anuncio> = emptyList(),
    val materiales: List<CursoMateriales> = emptyList(),
    val errorNotas: String? = null,
    val errorAnuncios: String? = null,
    val errorMateriales: String? = null
)

class AcademicoViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExtraRepository(application)
    private val _ui = MutableStateFlow(AcademicoUiState())
    val ui: StateFlow<AcademicoUiState> = _ui

    init {
        cargarNotas(); cargarAnuncios(); cargarMateriales()
    }

    fun cargarNotas() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(cargandoNotas = true, errorNotas = null)
            when (val r = repo.getNotas()) {
                is Result.Success -> _ui.value = _ui.value.copy(cargandoNotas = false, notas = r.data)
                is Result.Error -> _ui.value = _ui.value.copy(cargandoNotas = false, errorNotas = r.message)
            }
        }
    }

    fun cargarAnuncios() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(cargandoAnuncios = true, errorAnuncios = null)
            when (val r = repo.getAnuncios()) {
                is Result.Success -> _ui.value = _ui.value.copy(cargandoAnuncios = false, anuncios = r.data.anuncios)
                is Result.Error -> _ui.value = _ui.value.copy(cargandoAnuncios = false, errorAnuncios = r.message)
            }
        }
    }

    fun cargarMateriales() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(cargandoMateriales = true, errorMateriales = null)
            when (val r = repo.getMateriales()) {
                is Result.Success -> _ui.value = _ui.value.copy(cargandoMateriales = false, materiales = r.data.cursos)
                is Result.Error -> _ui.value = _ui.value.copy(cargandoMateriales = false, errorMateriales = r.message)
            }
        }
    }
}
