package com.example.ela.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.model.Reminder
import com.example.ela.domain.usecase.reminder.GetRemindersUseCase
import com.example.ela.domain.usecase.reminder.SaveReminderUseCase
import com.example.ela.ui.screens.reminder.ReminderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val saveReminderUseCase: SaveReminderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderUiState())
    val state: StateFlow<ReminderUiState> = _state

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            getRemindersUseCase().collect {
                _state.value = ReminderUiState(
                    reminders = it,
                    isLoading = false
                )
            }
        }
    }

    fun save(reminder: Reminder) {
        viewModelScope.launch {
            saveReminderUseCase(reminder)
        }
    }
}
