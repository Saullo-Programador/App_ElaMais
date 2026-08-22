package com.example.ela.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.model.Preferences
import com.example.ela.domain.usecase.preferences.GetPreferencesUseCase
import com.example.ela.domain.usecase.preferences.SavePreferencesUseCase
import com.example.ela.domain.usecase.preferences.UpdateDarkModeUseCase
import com.example.ela.ui.screens.preferences.PreferencesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val getPreferencesUseCase: GetPreferencesUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PreferencesUiState())
    val state: StateFlow<PreferencesUiState> = _state

    init {
        viewModelScope.launch {
            getPreferencesUseCase().collect {
                _state.value = PreferencesUiState(it)
            }

        }
    }

    fun save(preferences: Preferences) {
        viewModelScope.launch {
            savePreferencesUseCase(preferences)
        }
    }
}
