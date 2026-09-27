package com.example.ela.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ela.ui.theme.ElaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoupleSharingScreen(
    onBack: () -> Unit
) {
    ElaTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Compartilhamento do Casal", fontWeight = FontWeight.Bold) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Configure o que você deseja compartilhar com seu parceiro(a).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                SettingsSection(title = "Opções de Compartilhamento") {
                    CoupleSharingItem(
                        icon = Icons.Default.CalendarMonth,
                        title = "Dados do Ciclo",
                        description = "Compartilhar datas e fases do ciclo",
                        checked = true, // Mock state
                        onCheckedChange = { /* TODO: Implement ViewModel call */ }
                    )
                    CoupleSharingItem(
                        icon = Icons.Default.EditNote,
                        title = "Notas e Lembretes",
                        description = "Compartilhar anotações pessoais",
                        checked = false, // Mock state
                        onCheckedChange = { /* TODO: Implement ViewModel call */ }
                    )
                    CoupleSharingItem(
                        icon = Icons.Default.Favorite,
                        title = "Métricas de Saúde",
                        description = "Compartilhar indicadores de saúde",
                        checked = true, // Mock state
                        onCheckedChange = { /* TODO: Implement ViewModel call */ }
                    )
                }
            }
        }
    }
}

@Composable
fun CoupleSharingItem(
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

@Preview(showBackground = true)
@Composable
fun CoupleSharingScreenPreview() {
    ElaTheme {
        CoupleSharingScreen(
            onBack = {}
        )
    }
}
