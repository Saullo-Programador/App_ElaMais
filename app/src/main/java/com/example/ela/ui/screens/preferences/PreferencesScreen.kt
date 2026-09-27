package com.example.ela.ui.screens.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ela.domain.model.Preferences
import com.example.ela.ui.components.ButtonComponent
import com.example.ela.ui.components.InputComponent
import com.example.ela.ui.theme.ElaTheme
import com.example.ela.viewmodel.PreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    preferencesViewModel: PreferencesViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val prefState by preferencesViewModel.state.collectAsState()

    ElaTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Preferências", fontWeight = FontWeight.Bold) },
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
                    }
                )
            }
        ) { padding ->
            PreferencesContent(
                preferences = prefState.preferences,
                onSavePreferences = { preferencesViewModel.save(it) },
                onBack = onBack,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun PreferencesContent(
    preferences: Preferences?,
    onSavePreferences: (Preferences) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Local state for user preferences
    var notificationsEnabled by remember { mutableStateOf(preferences?.notificationsEnabled ?: true) }
    var favoriteFoods by remember { mutableStateOf(preferences?.favoriteFoods?.joinToString(", ") ?: "") }
    var favoriteSweets by remember { mutableStateOf(preferences?.favoriteSweets?.joinToString(", ") ?: "") }
    var symptoms by remember { mutableStateOf(preferences?.symptoms?.joinToString(", ") ?: "") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Text(
                text = "Personalize sua experiência e configure seu ciclo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // --- Seção de Personalização ---
                item {
                    SectionHeader(title = "Personalização", icon = Icons.Default.Settings)
                    Spacer(modifier = Modifier.height(6.dp))

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
                    Spacer(modifier = Modifier.height(6.dp))

                    InputComponent(
                        value = favoriteFoods,
                        onValueChange = { favoriteFoods = it },
                        label = "Alimentos Favoritos (separados por vírgula)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    InputComponent(
                        value = favoriteSweets,
                        onValueChange = { favoriteSweets = it },
                        label = "Doces Favoritos",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    InputComponent(
                        value = symptoms,
                        onValueChange = { symptoms = it },
                        label = "Sintomas Comuns",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ButtonComponent(
                        text = "Salvar Preferências",
                        textColor = Color.White,
                        onClick = {
                            onSavePreferences(
                                Preferences(
                                    id = preferences?.id ?: 0,
                                    favoriteFoods = favoriteFoods.split(",").map { it.trim() }
                                        .filter { it.isNotBlank() },
                                    favoriteSweets = favoriteSweets.split(",").map { it.trim() }
                                        .filter { it.isNotBlank() },
                                    symptoms = symptoms.split(",").map { it.trim() }
                                        .filter { it.isNotBlank() },
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
            onSavePreferences = {}
        )
    }
}