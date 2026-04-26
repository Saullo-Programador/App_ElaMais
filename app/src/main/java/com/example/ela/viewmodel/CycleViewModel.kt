package com.example.ela.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.model.Cycle
import com.example.ela.domain.usecase.cycle.GetCycleUseCase
import com.example.ela.domain.usecase.cycle.SaveCycleUseCase
import com.example.ela.domain.usecase.notification.ScheduleCycleNotificationsUseCase
import com.example.ela.ui.screens.cycle.CycleUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CycleViewModel @Inject constructor(
    private val getCycleUseCase: GetCycleUseCase,
    private val saveCycleUseCase: SaveCycleUseCase,
    private val scheduleCycleNotificationsUseCase: ScheduleCycleNotificationsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CycleUiState())
    val state: StateFlow<CycleUiState> = _state

    init {
        loadCycle()
    }

    private fun loadCycle() {
        viewModelScope.launch {
            getCycleUseCase().collect {
                _state.value = _state.value.copy(cycle = it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveCycle(cycle: Cycle) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSaving = true)

                saveCycleUseCase(cycle)

                // Agenda notificações para período fértil e menstruação
                scheduleCycleNotificationsUseCase(
                    cycleId = cycle.id.takeIf { it > 0 } ?: System.currentTimeMillis(),
                    lastPeriodStart = cycle.lastPeriodStart,
                    cycleLength = cycle.cycleLength
                )

                _state.value = _state.value.copy(
                    isSaving = false,
                    success = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = e.message
                )
            }
        }
    }
}
