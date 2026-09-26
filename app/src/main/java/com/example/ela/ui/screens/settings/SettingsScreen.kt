package com.example.ela.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.ela.ui.theme.ElaTheme
import com.example.ela.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import androidx.core.net.toUri


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onClickPreferences: () -> Unit,
    onClickManegeCare: () -> Unit,
    onClickCoupleSharing: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = uiState,
        onClearAllData = {
            viewModel.clearAllData()
        },
        onDismissSuccess = {
            viewModel.dismissSuccessMessage()
        },
        onDismissError = {
            viewModel.dismissErrorMessage()
        },
        onToggleNotifications = {
            viewModel.toggleNotifications(it)
        },
        onUpdateTime = {
            viewModel.updateNotificationTime(it)
        },
        onUpdateFrequency = {
            viewModel.updateFrequency(it)
        },
        onToggleDarkMode = {
            viewModel.toggleDarkMode(it)
        },
        onConfirmDeleteClick = {
            viewModel.onConfirmDeleteDataClick()
        },
        onDismissDeleteConfirmation = {
            viewModel.onDismissDeleteConfirmation()
        },
        onClickPreferences = onClickPreferences,
        onClickManegeCare = onClickManegeCare,
        onClickCoupleSharing = onClickCoupleSharing,
        openEmail = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:saullo.programador@gmail.com".toUri()
                putExtra(Intent.EXTRA_SUBJECT, "Sugestão ou Feedback - Ela+")
            }
            context.startActivity(intent)
        }
    )
}


@Composable
fun SettingsContent(
    uiState: SettingsUiState = SettingsUiState(),
    onToggleNotifications: (Boolean) -> Unit,
    onUpdateTime: (String) -> Unit,
    onUpdateFrequency: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onClearAllData: () -> Unit = {},
    onDismissSuccess: () -> Unit = {},
    onDismissError: () -> Unit = {},
    onConfirmDeleteClick: () -> Unit = {},
    onDismissDeleteConfirmation: () -> Unit = {},
    onClickPreferences: () -> Unit = {},
    onClickManegeCare: () -> Unit = {},
    onClickCoupleSharing: () -> Unit = {},
    openEmail: () -> Unit = {}
) {

    val scrollState = rememberScrollState()

    // Controle do Snackbar
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val scope = rememberCoroutineScope()


    /*
     * Mostra mensagem de sucesso usando Snackbar.
     *
     * Dessa forma o usuário não precisa clicar em OK.
     */
    LaunchedEffect(uiState.dataClearedSuccess) {

        if (uiState.dataClearedSuccess) {

            snackbarHostState.showSnackbar(
                message = "Todos os dados foram removidos com sucesso."
            )

            onDismissSuccess()
        }
    }


    /*
     * Mostra mensagem de erro usando Snackbar.
     */
    LaunchedEffect(uiState.errorMessage) {

        uiState.errorMessage?.let { error ->

            snackbarHostState.showSnackbar(
                message = "Não foi possível limpar os dados: $error"
            )

            onDismissError()
        }
    }


    /*
     * Dialog de confirmação para limpar os dados.
     *
     * Esse Dialog permanece porque apagar dados é uma ação
     * importante e potencialmente irreversível.
     */
    if (uiState.showDeleteConfirmation) {

        AlertDialog(

            onDismissRequest = {
                onDismissDeleteConfirmation()
            },

            title = {
                Text("Limpar todos os dados")
            },

            text = {
                Text(
                    "Tem certeza que deseja apagar todo o histórico " +
                            "e informações salvas? Esta ação não pode ser desfeita."
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        onDismissDeleteConfirmation()

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

                TextButton(
                    onClick = {
                        onDismissDeleteConfirmation()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }


    /*
     * Box permite posicionar o Snackbar sobre a tela.
     */
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {

            // ==========================================
            // TÍTULO
            // ==========================================

            Text(
                text = "Configurações",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Personalize sua experiência no Ela+",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )


            // ==========================================
            // NOTIFICAÇÕES
            // ==========================================

            SettingsSection(
                title = "Notificações"
            ) {

                SettingSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Ativar Notificações",
                    description = "Lembretes de datas importantes e ciclo",
                    checked = uiState.preferences.notificationsEnabled,
                    onCheckedChange = onToggleNotifications
                )


                if (uiState.preferences.notificationsEnabled) {

                    // Seleção de horário

                    SettingTimeItem(
                        icon = Icons.Default.Schedule,
                        title = "Horário da primeira notificação",
                        time = uiState.preferences.notificationsTime,
                        onTimeSelected = onUpdateTime
                    )


                    // Frequência

                    ListItem(

                        leadingContent = {

                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = null
                            )
                        },

                        headlineContent = {
                            Text("Frequência diária")
                        },

                        supportingContent = {
                            Text("Quantas vezes ser notificada no dia")
                        },

                        trailingContent = {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                IconButton(
                                    onClick = {
                                        onUpdateFrequency(false)
                                    }
                                ) {

                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Diminuir"
                                    )
                                }


                                Text(
                                    text = uiState.preferences.timesPerDay.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp
                                    )
                                )


                                IconButton(
                                    onClick = {
                                        onUpdateFrequency(true)
                                    }
                                ) {

                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Aumentar"
                                    )
                                }
                            }
                        }
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            // ==========================================
            // PREFERÊNCIAS
            // ==========================================

            SettingsSection(
                title = "Preferências"
            ) {

                var soundEffects by remember {
                    mutableStateOf(true)
                }


                SettingSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Tema escuro",
                    description = "Usar tema escuro no app",
                    checked = uiState.preferences.isDarkMode,
                    onCheckedChange = onToggleDarkMode
                )


                SettingSwitchItem(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Efeitos sonoros",
                    description = "Sons ao completar ações",
                    checked = soundEffects,
                    onCheckedChange = {
                        soundEffects = it
                    }
                )


                SettingClickableItem(
                    icon = Icons.Default.Tune,
                    title = "Preferências",
                    description = "Navega para tela de Preferência",
                    onClick = onClickPreferences
                )
            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            // ==========================================
            // PRIVACIDADE E DADOS
            // ==========================================

            SettingsSection(
                title = "Privacidade e Dados"
            ) {

//                var syncEnabled by remember {
//                    mutableStateOf(true)
//                }
//
//                var backupEnabled by remember {
//                    mutableStateOf(true)
//                }

//                SettingSwitchItem(
//                    icon = Icons.Default.Cloud,
//                    title = "Sincronização na nuvem",
//                    description = "Backup automático dos dados",
//                    checked = syncEnabled,
//                    onCheckedChange = {
//                        syncEnabled = it
//                    }
//                )

//                SettingSwitchItem(
//                    icon = Icons.Default.Backup,
//                    title = "Backup automático",
//                    description = "Criar backup diariamente",
//                    checked = backupEnabled,
//                    onCheckedChange = {
//                        backupEnabled = it
//                    }
//                )

                SettingClickableItem(
                    icon = Icons.Default.Favorite,
                    title = "Cuidados",
                    description = "Gerenciar os cuidados",
                    onClick = onClickManegeCare
                )

                SettingClickableItem(
                    icon = Icons.Default.People,
                    title = "Compartilhamento do Casal",
                    description = "Configurações de compartilhamento entre o casal",
                    onClick = onClickCoupleSharing
                )

                // Limpar dados


                SettingClickableItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Limpar dados",
                    description = "Apagar histórico e informações salvas",
                    onClick = {
                        onConfirmDeleteClick()
                    }
                )


                // Indicador de progresso

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


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            // ==========================================
            // SOBRE
            // ==========================================

            SettingsSection(
                title = "Sobre"
            ) {

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
                    description = "Envie sugestões e feedback",
                    onClick = openEmail
                )
            }


            Spacer(
                modifier = Modifier.height(32.dp)
            )


            // ==========================================
            // SAIR DA CONTA
            // ==========================================

            OutlinedButton(

                onClick = {

                    scope.launch {

                        snackbarHostState.showSnackbar(
                            message = "A função de sair da conta ainda não foi implementada."
                        )
                    }
                },

                modifier = Modifier.fillMaxWidth(),

                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )

            ) {

                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("Sair da conta")
            }


            // Espaço para o Snackbar não ficar colado no conteúdo

            Spacer(
                modifier = Modifier.height(80.dp)
            )
        }


        // ==========================================
        // SNACKBAR
        // ==========================================

        SnackbarHost(

            hostState = snackbarHostState,

            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}


// ======================================================
// SEÇÃO DE CONFIGURAÇÕES
// ======================================================

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {

    Column {

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Card(
            modifier = Modifier.fillMaxWidth(),

            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
        ) {

            Column {
                content()
            }
        }
    }
}


// ======================================================
// SWITCH ITEM
// ======================================================

@Composable
fun SettingSwitchItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    ListItem(

        leadingContent = {

            Icon(
                icon,
                contentDescription = null
            )
        },

        headlineContent = {
            Text(title)
        },

        supportingContent = {
            Text(description)
        },

        trailingContent = {

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}


// ======================================================
// TIME ITEM
// ======================================================

@Composable
fun SettingTimeItem(
    icon: ImageVector,
    title: String,
    time: String,
    onTimeSelected: (String) -> Unit
) {

    var showTimePicker by remember {
        mutableStateOf(false)
    }


    ListItem(

        leadingContent = {

            Icon(
                icon,
                contentDescription = null
            )
        },

        headlineContent = {
            Text(title)
        },

        trailingContent = {

            TextButton(
                onClick = {
                    showTimePicker = true
                }
            ) {

                Text(time)
            }
        }
    )


    if (showTimePicker) {

        TimePickerDialog(

            initialTime = time,

            onDismiss = {
                showTimePicker = false
            },

            onTimeSelected = { selectedTime ->

                onTimeSelected(selectedTime)

                showTimePicker = false
            }
        )
    }
}


// ======================================================
// TIME PICKER DIALOG
// ======================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {

    val timeParts = initialTime.split(":")

    val initialHour =
        timeParts.getOrNull(0)?.toIntOrNull() ?: 8

    val initialMinute =
        timeParts.getOrNull(1)?.toIntOrNull() ?: 0


    val timePickerState = rememberTimePickerState(

        initialHour = initialHour,

        initialMinute = initialMinute,

        is24Hour = true
    )


    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("Selecionar hora")
        },

        text = {
            TimePicker(
                state = timePickerState
            )
        },

        confirmButton = {

            TextButton(

                onClick = {

                    val hour =
                        timePickerState.hour
                            .toString()
                            .padStart(2, '0')

                    val minute =
                        timePickerState.minute
                            .toString()
                            .padStart(2, '0')


                    onTimeSelected(
                        "$hour:$minute"
                    )
                }
            ) {

                Text("OK")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("Cancelar")
            }
        }
    )
}


// ======================================================
// CLICKABLE ITEM
// ======================================================

@Composable
fun SettingClickableItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {

    ListItem(

        modifier = Modifier.clickable(
            onClick = onClick
        ),

        leadingContent = {

            Icon(
                icon,
                contentDescription = null
            )
        },

        headlineContent = {
            Text(title)
        },

        supportingContent = {
            Text(description)
        },

        trailingContent = {

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    )
}


// ======================================================
// PREVIEW
// ======================================================

@Preview(
    showBackground = true
)
@Composable
fun SettingsScreenPreview() {

    ElaTheme {

        SettingsContent(

            uiState = SettingsUiState(),

            onToggleNotifications = {},

            onUpdateTime = {},

            onUpdateFrequency = {},

            onToggleDarkMode = {}
        )
    }
}