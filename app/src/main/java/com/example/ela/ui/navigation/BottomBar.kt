package com.example.ela.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ela.domain.model.Preferences
import com.example.ela.ui.screens.settings.SettingsUiState
import com.example.ela.ui.theme.Rose600
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Itens da barra (certifique-se que bottomNavItems esteja acessível)
    val items = bottomNavItems

    // O container principal para centralizar a barra flutuante
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .navigationBarsPadding() // Garante espaço para a barra de navegação do sistema
            .padding(bottom = 16.dp), // Margem inferior da barra em relação à tela
        horizontalArrangement = Arrangement.Center // Centraliza a barra horizontalmente
    ) {
        // O Surface que cria a "pílula" branca flutuante
        Surface(
            modifier = Modifier
                .widthIn(max = 400.dp) // Define uma largura máxima opcional
                .height(80.dp), // Altura fixa para os itens
            shape = RoundedCornerShape(percent = 50), // Cantos totalmente arredondados (forma de pílula)
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp, // Sombra sutil para flutuar
            shadowElevation = 8.dp // Sombra mais definida para o efeito flutuante
        ) {
            // A Row que organiza os itens dentro da pílula
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp) // Espaçamento interno nas laterais
                    .selectableGroup(), // Importante para acessibilidade
                horizontalArrangement = Arrangement.SpaceEvenly, // Espaçamento uniforme entre itens
                verticalAlignment = Alignment.CenterVertically // Centraliza itens verticalmente
            ) {
                items.forEach { item ->
                    // Verifica a seleção (melhorado para hierarquia)
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                    // Usamos NavigationBarItem para manter o comportamento M3, mas dentro da nossa Row
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                // Simplificando a navegação para evitar problemas de estado travado
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon, // Supondo que você use ImageVector
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall // Fonte menor como na imagem
                            )
                        },
                        // Customização de cores para usar o esquema do tema
                        colors = NavigationBarItemDefaults.colors(
                            // Pílula de fundo do ícone selecionado
                            indicatorColor = MaterialTheme.colorScheme.primary,

                            // Ícone e Texto selecionados -> Usando sua cor primária
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,

                            // Ícone e Texto não selecionados -> Usando cores de variante do tema
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
}