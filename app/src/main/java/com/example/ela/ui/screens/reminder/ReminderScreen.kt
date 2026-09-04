package com.example.ela.ui.screens.reminder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.example.ela.domain.model.Reminder
import com.example.ela.ui.components.ButtonComponent
import com.example.ela.ui.components.InputComponent
import com.example.ela.ui.components.LoadingView
import com.example.ela.ui.theme.ElaTheme
import com.example.ela.ui.theme.Rose600
import com.example.ela.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    ReminderContent(
        state = state,
        onSave = { reminder ->
            viewModel.save(reminder)
        },
        onDelete = { reminder ->
            viewModel.delete(reminder)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderContent(
    state: ReminderUiState,
    onSave: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
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
        }
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

            when {
                state.isLoading -> LoadingView()
                state.reminders.isEmpty() -> EmptyRemindersView()
                else -> RemindersList(reminders = state.reminders, onDelete = onDelete)
            }
        }
    }

    if (showAddDialog) {
        AddReminderModal(
            onDismiss = { showAddDialog = false },
            onSave = { reminder ->
                onSave(reminder)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RemindersList(reminders: List<Reminder>, onDelete: (Reminder) -> Unit) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(reminders) { reminder ->
            ReminderCard(
                reminder = reminder,
                dateFormatter = dateFormatter,
                onDelete = onDelete
            )
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormatter.format(Date(reminder.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (reminder.type.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = reminder.type,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
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
    onSave: (Reminder) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Geral") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
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
            text = "Novo Lembrete",
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            style = TextStyle(fontWeight = FontWeight.Bold)
        )
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

                Spacer(modifier = Modifier.height(8.dp))

                ButtonComponent(
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
                    textColor = if (title.isNotBlank()){
                        MaterialTheme.colorScheme.onBackground
                    }else{
                        MaterialTheme.colorScheme.onBackground
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                Reminder(
                                    id = 0,
                                    title = title,
                                    description = description,
                                    date = selectedDate,
                                    type = selectedType
                                )
                            )
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
            state = ReminderUiState(isLoading = false),
            onSave = {},
            onDelete = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ReminderCardPreview() {
    ElaTheme {
        ReminderCard(
            reminder = Reminder(
                id = 1,
                title = "Testando",
                description = "Descrição do card de lembrete",
                date = 1787507460183,
                type = "Geral"
            ),
            onDelete = {},
            dateFormatter = SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
            )
        )
    }
}
