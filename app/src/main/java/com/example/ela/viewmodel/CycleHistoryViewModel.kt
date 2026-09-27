package com.example.ela.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.model.CycleRecord
import com.example.ela.domain.usecase.cycle_record.DeleteCycleRecordUseCase
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
    private val saveCycleRecordUseCase: SaveCycleRecordUseCase,
    private val deleteCycleRecordUseCase: DeleteCycleRecordUseCase,
    private val cycleRecordRepository: com.example.ela.domain.repository.CycleRecordRepository
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

    fun saveRecord(start: Long, end: Long, id: Long = 0) {
        viewModelScope.launch {
            if (id == 0L) {
                saveCycleRecordUseCase(start, end)
            } else {
                cycleRecordRepository.save(CycleRecord(id = id, startDate = start, endDate = end))
            }
        }
    }

    fun deleteRecord(record: CycleRecord) {
        viewModelScope.launch {
            deleteCycleRecordUseCase(record)
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            cycleRecordRepository.deleteAll()
        }
    }
}
