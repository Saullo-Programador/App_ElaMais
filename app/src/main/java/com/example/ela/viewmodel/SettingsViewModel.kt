package com.example.ela.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.model.Preferences
import com.example.ela.domain.usecase.notification.CancelNotificationsUseCase
import com.example.ela.domain.usecase.notification.ScheduleImportantDateNotificationUseCase
import com.example.ela.domain.usecase.preferences.GetPreferencesUseCase
import com.example.ela.domain.usecase.preferences.SavePreferencesUseCase
import com.example.ela.domain.usecase.settings.ClearAllDataUseCase
import com.example.ela.ui.screens.settings.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val clearAllDataUseCase: ClearAllDataUseCase,
    private val cancelNotificationsUseCase: CancelNotificationsUseCase,
    private val getPreferencesUseCase: GetPreferencesUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
    private val scheduleImportantDateNotificationUseCase: ScheduleImportantDateNotificationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState


    init {
        viewModelScope.launch {
            getPreferencesUseCase().collect { preferences ->
                if (preferences != null) {
                    _uiState.value = _uiState.value.copy(preferences = preferences)
                }
            }
        }
    }


    fun toggleNotifications(enabled: Boolean) {
        val currentPrefs = _uiState.value.preferences
        val newPrefs = currentPrefs.copy(notificationsEnabled = enabled)
        saveAndReschedule(newPrefs)
    }

    fun updateNotificationTime(time: String) {
        val currentPrefs = _uiState.value.preferences
        val newPrefs = currentPrefs.copy(notificationsTime = time) // Use o nome correto do campo
        saveAndReschedule(newPrefs)
    }

    fun updateFrequency(increase: Boolean) {
        val currentPrefs = _uiState.value.preferences
        val currentFreq = currentPrefs.timesPerDay
        val newFreq = if (increase) (currentFreq + 1).coerceAtMost(3) else (currentFreq - 1).coerceAtLeast(1)

        if (newFreq != currentFreq) {
            val newPrefs = currentPrefs.copy(timesPerDay = newFreq)
            saveAndReschedule(newPrefs)
        }
    }


    private fun saveAndReschedule(preferences: Preferences){
        viewModelScope.launch {
            // 1. Salva no Banco/Datastore
            savePreferencesUseCase(preferences)

            // 2. Se desativou, cancela tudo. Se ativou ou mudou horário, reagenda
            if (preferences.notificationsEnabled) {
                cancelNotificationsUseCase.cancelAllNotifications()
                scheduleImportantDateNotificationUseCase
            }else{
                cancelNotificationsUseCase.cancelAllNotifications()

            }
        }
    }
    fun clearAllData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isClearingData = true)
                clearAllDataUseCase()
                // Cancela todas as notificações ao limpar dados
                cancelNotificationsUseCase.cancelAllNotifications()
                _uiState.value = _uiState.value.copy(
                    isClearingData = false,
                    dataClearedSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isClearingData = false,
                    errorMessage = e.message
                )
            }
        }
    }
    fun dismissSuccessMessage() {
        _uiState.value = _uiState.value.copy(dataClearedSuccess = false)
    }

    fun dismissErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
