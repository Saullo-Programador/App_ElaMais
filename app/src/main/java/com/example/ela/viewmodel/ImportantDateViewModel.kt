package com.example.ela.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.model.ImportantDate
import com.example.ela.domain.usecase.important_date.GetImportantDatesUseCase
import com.example.ela.domain.usecase.important_date.SaveImportantDateUseCase
import com.example.ela.domain.usecase.notification.ScheduleImportantDateNotificationUseCase
import com.example.ela.ui.screens.importantDate.ImportantDateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportantDateViewModel @Inject constructor(
    private val getImportantDatesUseCase: GetImportantDatesUseCase,
    private val saveImportantDateUseCase: SaveImportantDateUseCase,
    private val scheduleImportantDateNotificationUseCase: ScheduleImportantDateNotificationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ImportantDateUiState())
    val state: StateFlow<ImportantDateUiState> = _state

    init {
        viewModelScope.launch {
            getImportantDatesUseCase().collect {
                _state.value = ImportantDateUiState(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun save(date: ImportantDate) {
        viewModelScope.launch {
            saveImportantDateUseCase(date)

            // Agenda notificações para a data importante
            scheduleImportantDateNotificationUseCase(
                dateId = date.id.takeIf { it > 0 } ?: System.currentTimeMillis(),
                title = date.title,
                dateMillis = date.date
            )
        }
    }
}
