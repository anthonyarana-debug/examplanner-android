package pe.tecsup.examplanner.ui.asistente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.examplanner.data.repository.ExtraRepository
import pe.tecsup.examplanner.data.repository.Result

data class Mensaje(val texto: String, val esUsuario: Boolean)

data class AsistenteUiState(
    val mensajes: List<Mensaje> = listOf(
        Mensaje(
            "¡Hola! Soy tu asistente de organización. Pregúntame cosas como " +
            "\"¿qué priorizo esta semana?\" o \"¿en qué curso estoy en riesgo de faltas?\".",
            esUsuario = false
        )
    ),
    val pensando: Boolean = false
)

class AsistenteViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExtraRepository(application)
    private val _ui = MutableStateFlow(AsistenteUiState())
    val ui: StateFlow<AsistenteUiState> = _ui

    fun enviar(texto: String) {
        val t = texto.trim()
        if (t.isEmpty() || _ui.value.pensando) return

        _ui.value = _ui.value.copy(
            mensajes = _ui.value.mensajes + Mensaje(t, esUsuario = true),
            pensando = true
        )

        viewModelScope.launch {
            val respuesta = when (val r = repo.preguntarAsistente(t)) {
                is Result.Success -> r.data
                is Result.Error -> r.message
            }
            _ui.value = _ui.value.copy(
                mensajes = _ui.value.mensajes + Mensaje(respuesta, esUsuario = false),
                pensando = false
            )
        }
    }
}
