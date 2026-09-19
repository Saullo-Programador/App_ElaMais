package com.example.ela.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ela.domain.model.ImportantDate
import com.example.ela.domain.model.Reminder
import com.example.ela.domain.usecase.important_date.GetImportantDatesUseCase
import com.example.ela.domain.usecase.important_date.SaveImportantDateUseCase
import com.example.ela.domain.usecase.reminder.DeleteReminderUseCase
import com.example.ela.domain.usecase.reminder.GetRemindersUseCase
import com.example.ela.domain.usecase.reminder.SaveReminderUseCase
import com.example.ela.ui.screens.reminder.ReminderFilterType
import com.example.ela.ui.screens.reminder.ReminderUiState
import com.example.ela.ui.screens.reminder.TimelineItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val saveReminderUseCase: SaveReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val getImportantDatesUseCase: GetImportantDatesUseCase,
    private val saveImportantDateUseCase: SaveImportantDateUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderUiState())
    val state: StateFlow<ReminderUiState> = _state

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            combine(
                getRemindersUseCase(),
                getImportantDatesUseCase()
            ) { reminders, dates ->
                updateFilteredEvents(reminders, dates)
                ReminderUiState(
                    reminders = reminders,
                    importantDates = dates,
                    isLoading = false,
                    searchQuery = _state.value.searchQuery,
                    filterType = _state.value.filterType,
                    filteredEvents = calculateFilteredEvents(reminders, dates, _state.value.searchQuery, _state.value.filterType)
                )
            }.collect {
                _state.value = it
            }
        }
    }

    private fun updateFilteredEvents(reminders: List<Reminder>, dates: List<ImportantDate>) {
        val filtered = calculateFilteredEvents(reminders, dates, _state.value.searchQuery, _state.value.filterType)
        _state.value = _state.value.copy(filteredEvents = filtered)
    }

    private fun calculateFilteredEvents(
        reminders: List<Reminder>,
        dates: List<ImportantDate>,
        query: String,
        filter: ReminderFilterType
    ): List<TimelineItem> {
        val filteredReminders = reminders.filter {
            (filter == ReminderFilterType.ALL || filter == ReminderFilterType.REMINDERS) &&
            (query.isBlank() || it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true))
        }.map { TimelineItem.HealthReminder(it) }

        val filteredDates = dates.filter {
            (filter == ReminderFilterType.ALL || filter == ReminderFilterType.SPECIAL_DATES) &&
            (query.isBlank() || it.title.contains(query, ignoreCase = true))
        }.map { TimelineItem.SpecialDate(it) }

        return (filteredReminders + filteredDates).sortedBy {
            when (it) {
                is TimelineItem.HealthReminder -> it.reminder.date
                is TimelineItem.SpecialDate -> it.date.date
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        val newState = _state.value.copy(searchQuery = query)
        _state.value = newState.copy(
            filteredEvents = calculateFilteredEvents(
                newState.reminders,
                newState.importantDates,
                query,
                newState.filterType
            )
        )
    }

    fun onFilterTypeChange(type: ReminderFilterType) {
        val newState = _state.value.copy(filterType = type)
        _state.value = newState.copy(
            filteredEvents = calculateFilteredEvents(
                newState.reminders,
                newState.importantDates,
                newState.searchQuery,
                type
            )
        )
    }

    fun save(reminder: Reminder) {
        viewModelScope.launch {
            try {
                saveReminderUseCase(reminder)
                _state.value = _state.value.copy(
                    success = "Lembrete salvo com sucesso! 🔔",
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Erro ao salvar lembrete",
                    success = null
                )
            }
        }
    }

    fun saveImportantDate(date: ImportantDate) {
        viewModelScope.launch {
            try {
                saveImportantDateUseCase(date)
                _state.value = _state.value.copy(
                    success = "Data especial salva com sucesso! ❤️",
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Erro ao salvar data especial",
                    success = null
                )
            }
        }
    }

    fun delete(reminder: Reminder){
        viewModelScope.launch {
            try {
                deleteReminderUseCase(reminder)
                _state.value = _state.value.copy(
                    success = "Lembrete removido com sucesso! 🗑️",
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Erro ao deletar lembrete",
                    success = null
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(
            success = null,
            error = null
        )
    }
}
