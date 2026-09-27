package com.example.ela.ui.screens.cycleHistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ela.ui.components.LoadingView
import com.example.ela.ui.theme.*
import com.example.ela.viewmodel.CycleHistoryViewModel
import com.example.ela.domain.model.CycleRecord
import java.text.SimpleDateFormat
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleHistoryScreen(
    viewModel: CycleHistoryViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    var showAddRecordDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<CycleRecord?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Histórico de Ciclos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteAllConfirm = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Deletar Tudo",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    LoadingView()
                }
                state.history.isEmpty() -> {
                    EmptyHistoryView { showAddRecordDialog = true }
                }
                else -> {
                    CycleHistoryContent(
                        history = state.history,
                        onDeleteRecord = { record ->
                            viewModel.deleteRecord(record)
                        },
                        onEditRecord = { record ->
                            recordToEdit = record
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (showDeleteAllConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteAllConfirm = false },
                    title = { Text("Deletar Todo Histórico") },
                    text = { Text("Tem certeza que deseja apagar todos os seus registros de ciclo? Esta ação não pode ser desfeita.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteAllRecords()
                                showDeleteAllConfirm = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Deletar Tudo")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteAllConfirm = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (showAddRecordDialog) {
                AddCycleRecordDialog(
                    onDismiss = { showAddRecordDialog = false },
                    onSave = { start, end ->
                        viewModel.saveRecord(start, end)
                        showAddRecordDialog = false
                    }
                )
            }

            if (recordToEdit != null) {
                EditCycleRecordBottomSheet(
                    record = recordToEdit!!,
                    onDismiss = { recordToEdit = null },
                    onSave = { start, end ->
                        viewModel.saveRecord(start, end, id = recordToEdit!!.id)
                        recordToEdit = null
                    }
                )
            }
        }
    }
}

@Composable
fun CycleHistoryContent(
    history: List<CycleRecord>,
    onDeleteRecord: (CycleRecord) -> Unit,
    onEditRecord: (CycleRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Seu histórico de ciclos registrados",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(history) { record ->
                CycleRecordItem(
                    record = record,
                    onDelete = { onDeleteRecord(record) },
                    onEdit = { onEditRecord(record) }
                )
            }
        }
    }
}

@Composable
fun CycleRecordItem(
    record: CycleRecord,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val duration = calculateDuration(record.startDate, record.endDate)
    val startDateFormatted = formatDate(record.startDate)
    val endDateFormatted = formatDate(record.endDate)
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MenstrualColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MenstrualColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "$startDateFormatted - $endDateFormatted",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ciclo registrado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        text = "$duration dias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MenstrualColor
                    )
                    Text(
                        text = "Duração",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Opções",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                expanded = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Deletar", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                expanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryView(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MenstrualColor.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nenhum ciclo registrado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Comece a registrar seus ciclos para acompanhar seu histórico.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = MenstrualColor)
            ) {
                Text("Adicionar Registro", color = Color.White)
            }
        }
    }
}

@Composable
fun AddCycleRecordDialog(
    onDismiss: () -> Unit,
    onSave: (Long, Long) -> Unit
) {
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Registro de Ciclo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Insira as datas do ciclo (Timestamp Long). Para um app real, usaríamos um DatePicker.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Data de Início") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("Data de Fim") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val start = startDate.toLongOrNull() ?: 0L
                    val end = endDate.toLongOrNull() ?: 0L
                    if ((start != 0L) && (end != 0L)) onSave(start, end)
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
     )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCycleRecordBottomSheet(
    record: CycleRecord,
    onDismiss: () -> Unit,
    onSave: (Long, Long) -> Unit
) {
    var startDate by remember { mutableStateOf(record.startDate.toString()) }
    var endDate by remember { mutableStateOf(record.endDate.toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Editar Registro de Ciclo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Data de Início") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("Data de Fim") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val start = startDate.toLongOrNull() ?: 0L
                    val end = endDate.toLongOrNull() ?: 0L
                    if (start != 0L && end != 0L) onSave(start, end)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MenstrualColor)
            ) {
                Text("Salvar Alterações", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun calculateDuration(start: Long, end: Long): Long {
    val diff = end - start
    return (diff / (1000 * 60 * 60 * 24)) + 1
}

@Preview(showBackground = true)
@Composable
fun CycleHistoryScreenPreview() {
    ElaTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            CycleHistoryContent(
                history = listOf(
                    CycleRecord(id = 1, startDate = 1724136000000, endDate = 1724740800000),
                    CycleRecord(id = 2, startDate = 1725340800000, endDate = 1725945600000),
                ),
                onDeleteRecord = {},
                onEditRecord = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
