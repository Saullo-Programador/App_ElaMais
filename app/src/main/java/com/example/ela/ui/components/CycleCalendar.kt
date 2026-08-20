package com.example.ela.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ela.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CycleCalendar(
    selectedDate: LocalDate = LocalDate.now(),
    menstruationDays: List<LocalDate> = emptyList(),
    fertileDays: List<LocalDate> = emptyList(),
    pmsDays: List<LocalDate> = emptyList(),
    predictedDays: List<LocalDate> = emptyList()
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7
    
    val days = (1..daysInMonth).map { currentMonth.atDay(it) }
    val emptyDaysBefore = (0 until firstDayOfMonth).map { null }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header do Calendário
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR")).replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 15.dp)
                )
                Row {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Mês Anterior")
                    }
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Próximo Mês")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dias da Semana
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb").forEach {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid de Dias
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(290.dp),
                userScrollEnabled = false
            ) {
                items(emptyDaysBefore) { Spacer(modifier = Modifier.size(40.dp)) }
                
                items(days) { date ->
                    val isMenstruation = menstruationDays.contains(date)
                    val isFertile = fertileDays.contains(date)
                    val isPms = pmsDays.contains(date)
                    val isPredicted = predictedDays.contains(date)
                    val isToday = date == LocalDate.now()

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isMenstruation -> MenstrualColor
                                    isFertile -> OvulationColor.copy(alpha = 0.2f)
                                    isPms -> TpmColor.copy(alpha = 0.3f)
                                    isPredicted -> MenstrualColor.copy(alpha = 0.15f)
                                    else -> Color.Transparent
                                }
                            )
                            .then(
                                if (isToday) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isToday || isMenstruation) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isMenstruation -> Color.White
                                    isFertile -> OvulationColor
                                    isPms -> TpmColor
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            // Pequeno ponto para indicador extra se necessário
                            if (isFertile) {
                                Box(modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(OvulationColor))
                            }
                        }
                    }
                }
            }

            // Legenda Detalhada
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            Column(
                modifier = Modifier.padding(start = 15.dp, top = 10.dp)
            ) {
                Text(
                    text = "Legenda do Ciclo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        LegendItem(
                            "Menstruação",
                            MenstrualColor,
                            "Dias registrados",
                            Modifier.weight(1f)
                        )
                        LegendItem("P. Fértil", OvulationColor, "Alta chance", Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        LegendItem("TPM", TpmColor, "Fase lútea", Modifier.weight(1f))
                        LegendItem(
                            "Previsão",
                            MenstrualColor.copy(alpha = 0.3f),
                            "Próximo ciclo",
                            Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(title: String, color: Color, description: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
