package com.example.ela.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.usecase.care.DeleteAllCareActionsUseCase
import com.example.ela.domain.usecase.care.DeleteCareActionsUseCase
import com.example.ela.domain.usecase.care.GetCareActionsByPhaseUseCase
import com.example.ela.domain.usecase.care.InitializeCareActionsUseCase
import com.example.ela.domain.usecase.care.SaveCareActionUseCase
import com.example.ela.domain.usecase.care.SyncCareActionsUseCase
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
    private val updateCareActionUseCase: UpdateCareActionUseCase,
    private val saveCareActionUseCase: SaveCareActionUseCase,
    private val deleteCareActionsUseCase: DeleteCareActionsUseCase,
    private val deleteAllCareActionsUseCase: DeleteAllCareActionsUseCase,
    private val initializeCareActionsUseCase : InitializeCareActionsUseCase,
    private val syncCareActionsUseCase: SyncCareActionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CareActionUiState())
    val state: StateFlow<CareActionUiState> = _state



    private var loadJob: Job? = null

    /**
     * Carrega as ações de cuidado baseado na fase do ciclo
     */
    fun load(phase: CyclePhase) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            syncCareActionsUseCase()
            initializeCareActionsUseCase(phase)

            _state.value = _state.value.copy(
                isLoading = true,
                phase = phase,
                error = null
            )

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

    fun save(action: CareAction) {
        viewModelScope.launch {
            try {
                saveCareActionUseCase(action)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Erro ao Salvar cuidado"
                )
            }
        }
    }

    fun update(action: CareAction){
        viewModelScope.launch {
            try {
                updateCareActionUseCase(action)
            } catch (e: Exception){
                _state.value = _state.value.copy(
                    error = e.message ?: "Erro ao Editar Cuidado"
                )
            }
        }
    }

    fun delete(id: Long){
        viewModelScope.launch {
            try {
                deleteCareActionsUseCase(id)
            }catch (e: Exception){
                _state.value = _state.value.copy(
                    error = e.message ?: "Erro ao Deletar Cuidado"
                )
            }
        }
    }

    fun deleteAll(){
        viewModelScope.launch {
            try {
                deleteAllCareActionsUseCase()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Erro ao Deletar Todos os Cuidados"
                )
            }
        }
    }
}