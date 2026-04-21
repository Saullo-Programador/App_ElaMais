package com.example.ela.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.usecase.cycle.GetCycleInfoUseCase
import com.example.ela.domain.usecase.cycle.GetCycleUseCase
import com.example.ela.domain.usecase.cycle_record.GetCycleHistoryUseCase
import com.example.ela.ui.screens.home.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCycleUseCase: GetCycleUseCase,
    private val getCycleHistoryUseCase: GetCycleHistoryUseCase,
    private val getCycleInfoUseCase: GetCycleInfoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        observeCycle()
    }

    private fun observeCycle() {
        viewModelScope.launch {
            combine(
                getCycleUseCase(),
                getCycleHistoryUseCase()
            ) { cycle, history ->

                getCycleInfoUseCase(cycle, history)

            }.catch {
                _state.value = HomeUiState(error = it.message)
            }.collect { info ->
                _state.value = HomeUiState(
                    isLoading = false,
                    cycleInfo = info
                )
            }
        }
    }
}
