package com.example.ela.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.model.Preferences
import com.example.ela.domain.usecase.notification.CancelNotificationsUseCase
import com.example.ela.domain.usecase.notification.ScheduleImportantDateNotificationUseCase
import com.example.ela.domain.usecase.preferences.GetPreferencesUseCase
import com.example.ela.domain.usecase.preferences.SavePreferencesUseCase
import com.example.ela.domain.usecase.preferences.UpdateDarkModeUseCase
import com.example.ela.domain.usecase.settings.ClearAllDataUseCase
import com.example.ela.ui.screens.settings.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val clearAllDataUseCase: ClearAllDataUseCase,
    private val cancelNotificationsUseCase: CancelNotificationsUseCase,
    private val getPreferencesUseCase: GetPreferencesUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
    private val scheduleImportantDateNotificationUseCase: ScheduleImportantDateNotificationUseCase,
    private val updateDarkModeUseCase: UpdateDarkModeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            getPreferencesUseCase().collect { preferences ->
                    _uiState.update {
                        it.copy(
                            preferences = preferences ?: it.preferences,
                            isLoading = false
                        )
                    }
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {

            val currentPrefs = getPreferencesUseCase().first()
                ?: return@launch

            val newPrefs = currentPrefs.copy(
                notificationsEnabled = enabled
            )

            saveAndReschedule(newPrefs)
        }
    }

    fun updateNotificationTime(time: String) {
        viewModelScope.launch {

            val currentPrefs = getPreferencesUseCase().first()
                ?: return@launch

            val newPrefs = currentPrefs.copy(
                notificationsTime = time
            )

            saveAndReschedule(newPrefs)
        }
    }

    fun updateFrequency(increase: Boolean) {
        viewModelScope.launch {

            // 🔥 Busca o valor REAL do banco
            val currentPrefs = getPreferencesUseCase().first()
                ?: return@launch

            val currentFreq = currentPrefs.timesPerDay

            val newFreq = if (increase) {
                (currentFreq + 1).coerceAtMost(3)
            } else {
                (currentFreq - 1).coerceAtLeast(1)
            }

            if (newFreq == currentFreq) {
                return@launch
            }

            val newPrefs = currentPrefs.copy(
                timesPerDay = newFreq
            )

            saveAndReschedule(newPrefs)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            updateDarkModeUseCase(enabled)
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
                _uiState.update {
                    it.copy(isClearingData = true)
                }

                clearAllDataUseCase()

                cancelNotificationsUseCase
                    .cancelAllNotifications()

                _uiState.update {
                    it.copy(
                        isClearingData = false,
                        dataClearedSuccess = true
                    )
                }

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isClearingData = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun dismissSuccessMessage() {
        _uiState.update {
            it.copy(dataClearedSuccess = false)
        }
    }

    fun dismissErrorMessage() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }
}