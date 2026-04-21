package com.example.ela.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.ela.ui.theme.ElaTheme
import com.example.ela.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = uiState,
        onClearAllData = { viewModel.clearAllData() },
        onDismissSuccess = { viewModel.dismissSuccessMessage() },
        onDismissError = { viewModel.dismissErrorMessage() },
        onToggleNotifications = { viewModel.toggleNotifications(it) },
        onUpdateTime = { viewModel.updateNotificationTime(it) },
        onUpdateFrequency = { viewModel.updateFrequency(it) }
    )
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState = SettingsUiState(),
    onToggleNotifications: (Boolean) -> Unit,
    onUpdateTime: (String) -> Unit,
    onUpdateFrequency: (Boolean) -> Unit,
    onClearAllData: () -> Unit = {},
    onDismissSuccess: () -> Unit = {},
    onDismissError: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    var showClearDataDialog by remember { mutableStateOf(false) }

    // Mostra dialog de sucesso
    if (uiState.dataClearedSuccess) {
        AlertDialog(
            onDismissRequest = onDismissSuccess,
            title = { Text("Dados limpos") },
            text = { Text("Todos os dados foram removidos com sucesso.") },
            confirmButton = {
                TextButton(onClick = onDismissSuccess) {
                    Text("OK")
                }
            }
        )
    }

    // Mostra dialog de erro
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Erro") },
            text = { Text("Não foi possível limpar os dados: $error") },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text("OK")
                }
            }
        )
    }

    // Dialog de confirmação para limpar dados
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Limpar todos os dados") },
            text = { Text("Tem certeza que deseja apagar todo o histórico e informações salvas? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Limpar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Personalize sua experiência no Ela+",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Notificações
        SettingsSection(title = "Notificações") {

            SettingSwitchItem(
                icon = Icons.Default.Notifications,
                title = "Ativar Notificações",
                description = "Lembretes de datas importantes e ciclo",
                checked = uiState.preferences.notificationsEnabled,
                onCheckedChange = onToggleNotifications
            )

            if (uiState.preferences.notificationsEnabled) {
                // Seleção de Horário
                SettingTimeItem(
                    icon = Icons.Default.Schedule,
                    title = "Horário da primeira notificação",
                    time = uiState.preferences.notificationsTime,
                    onTimeSelected = onUpdateTime
                )

                // Seleção de Frequência
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.Repeat, contentDescription = null)
                    },
                    headlineContent = { Text("Frequência diária") },
                    supportingContent = { Text("Quantas vezes ser notificada no dia") },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onUpdateFrequency(false) }) {
                                Icon(Icons.Default.Remove, "Diminuir")
                            }
                            Text(
                                text =uiState.preferences.timesPerDay.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 8.dp)

                            )
                            IconButton(onClick = { onUpdateFrequency(true) }) {
                                Icon(Icons.Default.Add, "Aumentar")
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preferências
        SettingsSection(title = "Preferências") {
            var darkTheme by remember { mutableStateOf(false) }
            var soundEffects by remember { mutableStateOf(true) }

            SettingSwitchItem(
                icon = Icons.Default.DarkMode,
                title = "Tema escuro",
                description = "Usar tema escuro no app",
                checked = darkTheme,
                onCheckedChange = { darkTheme = it }
            )

            SettingSwitchItem(
                icon = Icons.Default.VolumeUp,
                title = "Efeitos sonoros",
                description = "Sons ao completar ações",
                checked = soundEffects,
                onCheckedChange = { soundEffects = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacidade e Dados
        SettingsSection(title = "Privacidade e Dados") {
            var syncEnabled by remember { mutableStateOf(true) }
            var backupEnabled by remember { mutableStateOf(true) }

            SettingSwitchItem(
                icon = Icons.Default.Cloud,
                title = "Sincronização na nuvem",
                description = "Backup automático dos dados",
                checked = syncEnabled,
                onCheckedChange = { syncEnabled = it }
            )

            SettingSwitchItem(
                icon = Icons.Default.Backup,
                title = "Backup automático",
                description = "Criar backup diariamente",
                checked = backupEnabled,
                onCheckedChange = { backupEnabled = it }
            )

            // Opção para limpar dados
            SettingClickableItem(
                icon = Icons.Default.DeleteForever,
                title = "Limpar dados",
                description = "Apagar histórico e informações salvas",
                onClick = { showClearDataDialog = true }
            )

            // Mostrar indicador de progresso quando estiver limpando
            if (uiState.isClearingData) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sobre
        SettingsSection(title = "Sobre") {
            SettingClickableItem(
                icon = Icons.Default.Info,
                title = "Versão do app",
                description = "Ela+ v1.0.0"
            )

            SettingClickableItem(
                icon = Icons.Default.Star,
                title = "Avaliar o app",
                description = "Ajude-nos na Play Store"
            )

            SettingClickableItem(
                icon = Icons.Default.Email,
                title = "Entre em contato",
                description = "Envie sugestões e feedback"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botão Sair
        OutlinedButton(
            onClick = { /* TODO: Implementar logout */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sair da conta")
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
fun SettingTimeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    time: String,
    onTimeSelected: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    ListItem(
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        headlineContent = { Text(title) },
        trailingContent = {
            TextButton(onClick = { showTimePicker = true }) {
                Text(time)
            }
        }
    )

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = time,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { selectedTime ->
                onTimeSelected(selectedTime)
                showTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val timeParts = initialTime.split(":")
    val initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
    val initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar hora") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hour = timePickerState.hour.toString().padStart(2, '0')
                    val minute = timePickerState.minute.toString().padStart(2, '0')
                    onTimeSelected("$hour:$minute")
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SettingClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ElaTheme {
        SettingsContent(
            uiState = SettingsUiState(),
            onToggleNotifications = {},
            onUpdateTime = {},
            onUpdateFrequency = {}
        )
    }
}
