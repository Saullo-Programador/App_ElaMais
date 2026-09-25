package com.example.ela.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.usecase.auth.GetCurrentUserUseCase
import com.example.ela.domain.usecase.auth.LoginUseCase
import com.example.ela.domain.usecase.auth.LogoutUseCase
import com.example.ela.domain.usecase.auth.SignupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = loginUseCase(email, password)
            if (result.isSuccess) {
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao fazer login")
            }
        }
    }

    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = signupUseCase(email, password)
            if (result.isSuccess) {
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao criar conta")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun isUserAuthenticated(): Boolean {
        return getCurrentUserUseCase() != null
    }
}