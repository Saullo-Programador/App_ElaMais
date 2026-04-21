package com.example.ela.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.usecase.care.GetCareActionsByPhaseUseCase
import com.example.ela.domain.usecase.care.UpdateCareActionUseCase
import com.example.ela.ui.screens.care.CareActionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CareActionViewModel @Inject constructor(
    private val getCareActionsByPhaseUseCase: GetCareActionsByPhaseUseCase,
    private val updateCareActionUseCase: UpdateCareActionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CareActionUiState())
    val state: StateFlow<CareActionUiState> = _state



    private var loadJob: Job? = null

    /**
     * Carrega as ações de cuidado baseado na fase do ciclo
     */
    fun load(phase: CyclePhase) {

        // Cancela qualquer coleta anterior
        loadJob?.cancel()

        _state.value = _state.value.copy(
            isLoading = true,
            phase = phase,
            error = null
        )

        loadJob = viewModelScope.launch {
            getCareActionsByPhaseUseCase(phase)
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao carregar cuidados"
                    )
                }
                .collect { actions ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        actions = actions,
                        phase = phase
                    )
                }
        }
    }

    /**
     * Marca/desmarca ação como concluída
     */
    fun toggleDone(action: CareAction) {
        viewModelScope.launch {
            val updated = action.copy(
                isCompleted = !action.isCompleted
            )
            updateCareActionUseCase(updated)
        }
    }

    fun save (action: CareAction){}
}