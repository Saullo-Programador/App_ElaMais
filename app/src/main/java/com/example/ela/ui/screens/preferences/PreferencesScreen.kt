package com.example.ela.ui.screens.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ela.domain.model.Cycle
import com.example.ela.domain.model.Preferences
import com.example.ela.ui.components.ButtonComponent
import com.example.ela.ui.components.InputComponent
import com.example.ela.ui.theme.ElaTheme
import com.example.ela.viewmodel.CycleViewModel
import com.example.ela.viewmodel.PreferencesViewModel

@Composable
fun PreferencesScreen(
    preferencesViewModel: PreferencesViewModel = hiltViewModel(),
    cycleViewModel: CycleViewModel = hiltViewModel()
) {
    val prefState by preferencesViewModel.state.collectAsState()
    val cycleState by cycleViewModel.state.collectAsState()

    PreferencesContent(
        preferences = prefState.preferences,
        cycle = cycleState.cycle,
        onSavePreferences = { preferencesViewModel.save(it) },
        onSaveCycle = { cycleViewModel.saveCycle(it) }
    )
}

@Composable
fun PreferencesContent(
    preferences: Preferences?,
    cycle: Cycle?,
    onSavePreferences: (Preferences) -> Unit,
    onSaveCycle: (Cycle) -> Unit
) {
    // Local state for cycle settings
    var cycleLength by remember { mutableStateOf(cycle?.cycleLength?.toString() ?: "28") }
    var periodLength by remember { mutableStateOf(cycle?.periodLength?.toString() ?: "5") }
    var lastPeriodDate by remember { mutableStateOf(cycle?.lastPeriodStart?.toString() ?: "") }

    // Local state for user preferences
    var notificationsEnabled by remember { mutableStateOf(preferences?.notificationsEnabled ?: true) }
    var favoriteFoods by remember { mutableStateOf(preferences?.favoriteFoods?.joinToString(", ") ?: "") }
    var favoriteSweets by remember { mutableStateOf(preferences?.favoriteSweets?.joinToString(", ") ?: "") }
    var symptoms by remember { mutableStateOf(preferences?.symptoms?.joinToString(", ") ?: "") }

    ElaTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(vertical = 32.dp)
            ) {
                item {
                    Text(
                        text = "Preferências",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Personalize sua experiência e configure seu ciclo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // --- Seção do Ciclo ---
                item {
                    SectionHeader(title = "Configurações do Ciclo", icon = Icons.Default.DateRange)
                    Spacer(modifier = Modifier.height(16.dp))

                    InputComponent(
                        value = cycleLength,
                        onValueChange = { cycleLength = it },
                        label = "Duração do Ciclo (dias)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    InputComponent(
                        value = periodLength,
                        onValueChange = { periodLength = it },
                        label = "Duração da Menstruação (dias)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    InputComponent(
                        value = lastPeriodDate,
                        onValueChange = { lastPeriodDate = it },
                        label = "Data da Última Menstruação (Timestamp)",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ButtonComponent(
                        text = "Salvar Ciclo",
                        onClick = {
                            onSaveCycle(
                                Cycle(
                                    id = cycle?.id ?: 0,
                                    cycleLength = cycleLength.toIntOrNull() ?: 28,
                                    periodLength = periodLength.toIntOrNull() ?: 5,
                                    lastPeriodStart = lastPeriodDate.toLongOrNull() ?: System.currentTimeMillis()
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // --- Seção de Personalização ---
                item {
                    SectionHeader(title = "Personalização", icon = Icons.Default.Settings)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Notificações Ativadas",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    InputComponent(
                        value = favoriteFoods,
                        onValueChange = { favoriteFoods = it },
                        label = "Alimentos Favoritos (separados por vírgula)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    InputComponent(
                        value = favoriteSweets,
                        onValueChange = { favoriteSweets = it },
                        label = "Doces Favoritos",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    InputComponent(
                        value = symptoms,
                        onValueChange = { symptoms = it },
                        label = "Sintomas Comuns",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ButtonComponent(
                        text = "Salvar Preferências",
                        onClick = {
                            onSavePreferences(
                                Preferences(
                                    id = preferences?.id ?: 0,
                                    favoriteFoods = favoriteFoods.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                    favoriteSweets = favoriteSweets.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                    symptoms = symptoms.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                    notificationsEnabled = notificationsEnabled
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreferencesContentPreview(){
    ElaTheme {
        PreferencesContent(
            preferences = Preferences(),
            cycle = Cycle(
                id = 1,
                cycleLength = 28,
                periodLength = 5,
                lastPeriodStart = 1725148800000L
            ),
            onSavePreferences = {},
            onSaveCycle = {}
        )
    }
}