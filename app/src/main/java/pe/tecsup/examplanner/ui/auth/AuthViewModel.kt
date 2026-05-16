package pe.tecsup.examplanner.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.examplanner.data.repository.ExamPlannerRepository
import pe.tecsup.examplanner.data.repository.Result

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val nombre: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ExamPlannerRepository(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun registro(email: String, nombre: String, password: String) {
        if (email.isBlank() || nombre.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Completa todos los campos")
            return
        }
        if (!email.endsWith("@tecsup.edu.pe")) {
            _uiState.value = AuthUiState.Error("Solo se permiten correos @tecsup.edu.pe")
            return
        }
        if (password.length < 8) {
            _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 8 caracteres")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = repo.registro(email, nombre, password)) {
                is Result.Success -> _uiState.value = AuthUiState.Success(result.data.estudiante.nombre)
                is Result.Error   -> _uiState.value = AuthUiState.Error(result.message)
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Ingresa tu correo y contraseña")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = repo.login(email, password)) {
                is Result.Success -> _uiState.value = AuthUiState.Success(result.data.estudiante.nombre)
                is Result.Error   -> _uiState.value = AuthUiState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
