package com.example.ela.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ela.core.utils.getPhaseTitle
import com.example.ela.domain.model.CycleInfo
import com.example.ela.domain.model.CyclePhase
import com.example.ela.ui.components.ButtonComponent
import com.example.ela.ui.components.CycleCalendar
import com.example.ela.ui.components.ErrorView
import com.example.ela.ui.components.LoadingView
import com.example.ela.ui.theme.*
import com.example.ela.viewmodel.HomeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onGoToCare: (CyclePhase) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showCalendar by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    HomeContent(
        state = state,
        onGoToCare = onGoToCare,
        onOpenCalendar = { showCalendar = true },
        onPeriodStarted = { viewModel.onPeriodStarted() }
    )

    if (showCalendar) {
        ModalBottomSheet(
            onDismissRequest = { showCalendar = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            val today = LocalDate.now()
            // Simulação de dados para visualização
            CycleCalendar(
                menstruationDays = listOf(today.minusDays(20), today.minusDays(19), today.minusDays(18)),
                fertileDays = listOf(today.minusDays(8), today.minusDays(7), today.minusDays(6), today.minusDays(5)),
                pmsDays = listOf(today.minusDays(2), today.minusDays(1), today),
                predictedDays = listOf(today.plusDays(8), today.plusDays(9), today.plusDays(10))
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onGoToCare: (CyclePhase) -> Unit,
    onOpenCalendar: () -> Unit,
    onPeriodStarted: () -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            state.isLoading -> LoadingView()
            state.error != null -> ErrorView(state.error!!)
            state.cycleInfo == null -> EmptyView()
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    HeaderSection()

                    Spacer(modifier = Modifier.height(16.dp))

                    PhaseCardAnimated(
                        info = state.cycleInfo,
                        onPeriodStarted = onPeriodStarted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão para abrir calendário
                    ButtonComponent(
                        onClick = onOpenCalendar,
                        modifier = Modifier.fillMaxWidth(),
                        text = "Ver calendário completo",
                        textColor = Color.White,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = getPhaseColor(state.cycleInfo.currentPhase)
                        ),
                        icon = Icons.Default.CalendarMonth,
                        iconColor = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DaysCounterCard(days = state.cycleInfo.daysUntilNextPeriod)

                    Spacer(modifier = Modifier.height(16.dp))

                    AlertsCard(info = state.cycleInfo)

                    Spacer(modifier = Modifier.height(16.dp))

                    SuggestionsCard(info = state.cycleInfo)

                    Spacer(modifier = Modifier.height(24.dp))

                    ButtonComponent(
                        onClick = { onGoToCare(state.cycleInfo.currentPhase) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = getPhaseColor(state.cycleInfo.currentPhase)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        ),
                        text = "Ver cuidados recomendados ❤️",
                        textColor = Color.White,
                        icon = Icons.Default.Spa,
                        iconColor = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Column {
        Text(
            text = "Ela+",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Seu companheiro de cuidado",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PhaseCardAnimated(
    info: CycleInfo,
    onPeriodStarted: () -> Unit
) {
    val phaseColor = getPhaseColor(info.currentPhase)
    val gradientColors = listOf(
        phaseColor.copy(alpha = 0.8f),
        phaseColor.copy(alpha = 0.4f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(gradientColors)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fase Atual",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = getPhaseTitle(info.currentPhase),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                val infoText = when {
                    info.currentPhase == CyclePhase.MENSTRUAL -> {
                        "Faltam ${info.daysRemainingInPhase} ${if (info.daysRemainingInPhase == 1) "dia" else "dias"} para acabar"
                    }
                    info.daysUntilFertileWindow > 0 -> {
                        "Faltam ${info.daysUntilFertileWindow} ${if (info.daysUntilFertileWindow == 1) "dia" else "dias"} para o período fértil"
                    }
                    info.isFertileWindow -> {
                        "Você está no período fértil! 💕"
                    }
                    info.daysUntilPms > 0 -> {
                        "Faltam ${info.daysUntilPms} ${if (info.daysUntilPms == 1) "dia" else "dias"} para a TPM"
                    }
                    else -> {
                        "Faltam ${info.daysUntilNextPeriod} ${if (info.daysUntilNextPeriod == 1) "dia" else "dias"} para a menstruação"
                    }
                }

                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.weight(1f))

                val pulseAnimation = rememberInfiniteTransition(label = "pulse")
                val scale by pulseAnimation.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                            .animateContentSize()
                    )

                    if (info.daysUntilNextPeriod <= 5 && info.currentPhase != CyclePhase.MENSTRUAL) {
                        Button(
                            onClick = onPeriodStarted,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = phaseColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "Já desceu? 🩸",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DaysCounterCard(days: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Próxima menstruação",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$days ${if (days == 1) "dia" else "dias"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun AlertsCard(info: CycleInfo) {
    val hasAlerts = info.isPms || info.isFertileWindow

    AnimatedVisibility(
        visible = hasAlerts,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    info.isPms -> TpmColor.copy(alpha = 0.2f)
                    info.isFertileWindow -> Coral100
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Alertas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (info.isPms) {
                    AlertItem(
                        icon = "⚠️",
                        text = "TPM chegando — seja paciente 😅",
                        color = TpmColor
                    )
                }

                if (info.isFertileWindow) {
                    AlertItem(
                        icon = "💕",
                        text = "Período fértil ativo",
                        color = Coral500
                    )
                }
            }
        }
    }

    if (!hasAlerts) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Lavender50
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✨")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tudo tranquilo por enquanto",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun AlertItem(icon: String, text: String, color: Color) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun SuggestionsCard(info: CycleInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sugestões para hoje",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            info.suggestions.forEach { suggestion ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Rose300
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cadastre seu ciclo para começar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Vá em Configurar Ciclo ❤️",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun getPhaseColor(phase: CyclePhase): Color {
    return when (phase) {
        CyclePhase.MENSTRUAL -> MenstrualColor
        CyclePhase.FOLLICULAR -> FollicularColor
        CyclePhase.OVULATION -> OvulationColor
        CyclePhase.LUTEAL -> LutealColor
        CyclePhase.TPM -> TpmColor
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    ElaTheme {
        HomeContent(
            state = HomeUiState(),
            onGoToCare = {},
            onOpenCalendar = {},
            onPeriodStarted = {}
        )
    }
}
