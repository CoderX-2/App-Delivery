package com.example.appdelivery.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdelivery.data.db.AppDatabase
import com.example.appdelivery.data.repository.AuthRepository
import com.example.appdelivery.data.session.SessionManager
import com.example.appdelivery.util.Validators
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(AppDatabase.getInstance(app).customerDao())
    private val session = SessionManager(app)

    var uiState by mutableStateOf(AuthUiState())
        private set

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        val errors = buildMap<String, String> {
            Validators.email(email)?.let { put("email", it) }
            if (password.isBlank()) put("password", "Ingresa tu contraseña")
        }
        if (errors.isNotEmpty()) {
            uiState = AuthUiState(fieldErrors = errors)
            return
        }
        viewModelScope.launch {
            uiState = AuthUiState(loading = true)
            repo.login(email, password).fold(
                onSuccess = { id ->
                    session.save(id)
                    uiState = AuthUiState()
                    onSuccess()
                },
                onFailure = { uiState = AuthUiState(error = it.message) }
            )
        }
    }

    fun register(
        name: String, email: String, phone: String,
        password: String, confirm: String, onSuccess: () -> Unit
    ) {
        val errors = buildMap<String, String> {
            Validators.name(name)?.let { put("name", it) }
            Validators.email(email)?.let { put("email", it) }
            Validators.phone(phone)?.let { put("phone", it) }
            Validators.password(password)?.let { put("password", it) }
            if (password != confirm) put("confirm", "Las contraseñas no coinciden")
        }
        if (errors.isNotEmpty()) {
            uiState = AuthUiState(fieldErrors = errors)
            return
        }
        viewModelScope.launch {
            uiState = AuthUiState(loading = true)
            repo.register(name, email, phone, password).fold(
                onSuccess = { id ->
                    session.save(id)
                    uiState = AuthUiState()
                    onSuccess()
                },
                onFailure = { uiState = AuthUiState(error = it.message) }
            )
        }
    }
}
