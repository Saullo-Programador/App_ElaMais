package com.example.ela.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.usecase.cycle_record.GetCycleHistoryUseCase
import com.example.ela.domain.usecase.cycle_record.SaveCycleRecordUseCase
import com.example.ela.ui.screens.cycleHistory.CycleHistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CycleHistoryViewModel @Inject constructor(
    private val getCycleHistoryUseCase: GetCycleHistoryUseCase,
    private val saveCycleRecordUseCase: SaveCycleRecordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CycleHistoryUiState())
    val state: StateFlow<CycleHistoryUiState> = _state

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getCycleHistoryUseCase().collect {
                _state.value = CycleHistoryUiState(
                    history = it,
                    isLoading = false
                )
            }
        }
    }

    fun saveRecord(start: Long, end: Long) {
        viewModelScope.launch {
            saveCycleRecordUseCase(start, end)
        }
    }
}
