package com.example.ela.ui.screens.reminder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.util.query
import com.example.ela.domain.model.ImportantDate
import com.example.ela.domain.model.Reminder
import com.example.ela.ui.components.ButtonComponent
import com.example.ela.ui.components.InputComponent
import com.example.ela.ui.components.LoadingView
import com.example.ela.ui.theme.Coral600
import com.example.ela.ui.theme.ElaTheme
import com.example.ela.ui.theme.Lavender600
import com.example.ela.ui.theme.Rose600
import com.example.ela.ui.theme.Sage600
import com.example.ela.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed interface TimelineItem {
    data class HealthReminder(val reminder: Reminder) : TimelineItem
    data class SpecialDate(val date: ImportantDate) : TimelineItem
}

@Composable
fun getReminderTypeColor(type: String): Color {
    return when (type) {
        "Medicação" -> Rose600
        "Consulta" -> Lavender600
        "Exame" -> Coral600
        "Geral" -> Sage600
        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.success, state.error) {
        state.success?.let {
            scope.launch {
                snackbarHostState.showSnackbar(message = it)
                viewModel.clearMessage()
            }
        }
        state.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(message = it)
                viewModel.clearMessage()
            }
        }
    }

    ReminderContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onSaveReminder = { reminder ->
            viewModel.save(reminder)
        },
        onSaveImportantDate = { date ->
            viewModel.saveImportantDate(date)
        },
        onDelete = { reminder ->
            viewModel.delete(reminder)
        },
        onChangeSearch = { query ->
            viewModel.onSearchQueryChange(query)
        },
        onClickFilter = { type ->
            viewModel.onFilterTypeChange(type)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderContent(
    state: ReminderUiState,
    snackbarHostState: SnackbarHostState,
    onSaveReminder: (Reminder) -> Unit,
    onSaveImportantDate: (ImportantDate) -> Unit,
    onDelete: (Reminder) -> Unit,
    onClickFilter: (ReminderFilterType) -> Unit,
    onChangeSearch: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                containerColor = Rose600,
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar lembrete", tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Lembretes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Configure lembretes importantes para seu ciclo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputComponent(
                value = state.searchQuery,
                onValueChange = onChangeSearch,
                label = "Pesquisar lembretes...",
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReminderFilterType.values().forEach { type ->
                    FilterChip(
                        selected = state.filterType == type,
                        onClick = {onClickFilter(type)},
                        label = {
                            Text(
                                text = when (type) {
                                    ReminderFilterType.ALL -> "Todos"
                                    ReminderFilterType.REMINDERS -> "Lembretes"
                                    ReminderFilterType.SPECIAL_DATES -> "Datas Especiais"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.isLoading -> LoadingView()
                state.filteredEvents.isEmpty() -> EmptyRemindersView()
                else -> RemindersList(
                    events = state.filteredEvents,
                    onDelete = onDelete
                )
            }
        }
    }

    if (showAddDialog) {
        AddReminderModal(
            onDismiss = { showAddDialog = false },
            onSaveReminder = { reminder ->
                onSaveReminder(reminder)
                showAddDialog = false
            },
            onSaveImportantDate = { date ->
                onSaveImportantDate(date)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RemindersList(
    events: List<TimelineItem>,
    onDelete: (Reminder) -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(events) { event ->
            when (event) {
                is TimelineItem.HealthReminder -> {
                    ReminderCard(
                        reminder = event.reminder,
                        dateFormatter = dateFormatter,
                        onDelete = onDelete
                    )
                }
                is TimelineItem.SpecialDate -> {
                    ImportantDateCard(
                        date = event.date,
                        dateFormatter = dateFormatter
                    )
                }
            }
        }
    }
}

@Composable
fun ImportantDateCard(
    date: ImportantDate,
    dateFormatter: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(Coral600)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = date.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Coral600
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dateFormatter.format(Date(date.date)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (date.isRecurring) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Coral600
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Anual",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Coral600,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    onDelete: (Reminder) -> Unit,
    dateFormatter: SimpleDateFormat
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val typeColor = getReminderTypeColor(reminder.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(typeColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (reminder.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reminder.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = typeColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dateFormatter.format(Date(reminder.date)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (reminder.type.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Label,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = typeColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            text = reminder.type,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = typeColor
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = typeColor.copy(alpha = 0.1f)
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = typeColor
                                    )
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    if (showDeleteDialog) {
        DeleteReminderDialog(
            onDismiss = { showDeleteDialog = false },
            onDelete = { reminder ->
                onDelete(reminder)
                showDeleteDialog = false
            },
            reminder = reminder
        )
    }
}

@Composable
fun EmptyRemindersView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nenhum lembrete configurado",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Toque no + para adicionar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderModal(
    onDismiss: () -> Unit,
    onSaveReminder: (Reminder) -> Unit,
    onSaveImportantDate: (ImportantDate) -> Unit
) {
    var isSpecialDate by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Geral") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isRecurring by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val reminderTypes = listOf("Geral", "Medicação", "Consulta", "Exame")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .align(Alignment.CenterHorizontally),
                text = if (isSpecialDate) "Nova Data Especial" else "Novo Lembrete",
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                style = TextStyle(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChip(
                    selected = !isSpecialDate,
                    onClick = { isSpecialDate = false },
                    label = { Text("Lembrete de Saúde") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = isSpecialDate,
                    onClick = { isSpecialDate = true },
                    label = { Text("Data Especial") }
                )
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                InputComponent(
                    value = title,
                    onValueChange = { title = it },
                    label = "Título",
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isSpecialDate) {
                    InputComponent(
                        value = description,
                        onValueChange = { description = it },
                        label = "Descrição (opcional)",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tipo",
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    reminderTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Text(text = type)
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it }
                        )
                        Text(text = "Repetir anualmente", modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ButtonComponent(
                    textColor = Color.White,
                    text = "Selecionar Data e Hora",
                    onClick = { showDatePicker = true },
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ButtonComponent(
                    text = "Cancelar",
                    onClick = onDismiss,
                    textColor = MaterialTheme.colorScheme.primary,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.weight(1f)
                )

                ButtonComponent(
                    text = "Salvar",
                    textColor = MaterialTheme.colorScheme.onBackground,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    onClick = {
                        if (title.isNotBlank()) {
                            if (isSpecialDate) {
                                onSaveImportantDate(
                                    ImportantDate(
                                        id = 0,
                                        title = title,
                                        date = selectedDate,
                                        isRecurring = isRecurring
                                    )
                                )
                            } else {
                                onSaveReminder(
                                    Reminder(
                                        id = 0,
                                        title = title,
                                        description = description,
                                        date = selectedDate,
                                        type = selectedType
                                    )
                                )
                            }
                        }
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )

        DatePickerDialog(
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.background
            ),
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background
            ))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteReminderDialog(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onDelete: (Reminder) -> Unit
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        title = { Text("Deletar Lembrete") },
        text = { Text("Tem certeza que deseja deletar esse lembrete?")},
        confirmButton = {
            TextButton(
                onClick = {
                    onDelete(reminder)
                },
            ) {
                Text("Deletar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ReminderScreenPreview() {
    ElaTheme {
        ReminderContent(
            state = ReminderUiState(
                isLoading = false,
                reminders = listOf(
                    Reminder(
                        id = 1,
                        title = "Teste 1",
                        description = "Descrição do card de lembrete",
                        date = 1787507460183,
                        type = "Geral"
                    ),
                    Reminder(
                        id = 2,
                        title = "Teste 2",
                        description = "Descrição do card de lembrete",
                        date = 1787507460183,
                        type = "Geral"
                    ),
                    Reminder(
                        id = 3,
                        title = "Teste 3",
                        description = "Descrição do card de lembrete",
                        date = 1787507460183,
                        type = "Geral"
                    )
                )
            ),
            onSaveReminder = {},
            onSaveImportantDate = {},
            onDelete = {},
            snackbarHostState = SnackbarHostState(),
            onClickFilter = {},
            onChangeSearch = {},
        )
    }
}
