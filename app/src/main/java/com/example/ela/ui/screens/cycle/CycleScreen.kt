package com.example.ela.ui.screens.cycle

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ela.domain.model.Cycle
import com.example.ela.ui.components.ButtonComponent
import com.example.ela.ui.components.ErrorView
import com.example.ela.ui.components.InputComponent
import com.example.ela.ui.components.LoadingView
import com.example.ela.ui.screens.home.getPhaseColor
import com.example.ela.ui.theme.ElaTheme
import com.example.ela.viewmodel.CycleViewModel
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CycleScreen(
    viewModel: CycleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    CycleContent(
        state = state,
        onSave = { cycle ->
            viewModel.saveCycle(cycle)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleContent(
    state: CycleUiState,
    onSave: (Cycle) -> Unit
) {
    val scrollState = rememberScrollState()
    var cycleLength by remember { mutableStateOf(state.cycle?.cycleLength?.toString() ?: "28") }
    var periodLength by remember { mutableStateOf(state.cycle?.periodLength?.toString() ?: "5") }
    var lastPeriodStart by remember { mutableStateOf(state.cycle?.lastPeriodStart ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Atualiza campos quando o ciclo é carregado
    LaunchedEffect(state.cycle) {
        state.cycle?.let {
            cycleLength = it.cycleLength.toString()
            periodLength = it.periodLength.toString()
            lastPeriodStart = it.lastPeriodStart
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Configurar Ciclo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Personalize seu ciclo para previsões mais precisas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            state.isSaving -> LoadingView()
            state.error != null -> ErrorView(
                message = state.error!!
            )
            state.success -> {
                SuccessView()
            }
        }

        // Ciclo Length

        InputComponent(
            value = cycleLength,
            onValueChange = { cycleLength = it.filter { char -> char.isDigit() } },
            label = "Duração do ciclo (dias)",
            placeholder = "28",
            supportingText = "Geralmente entre 21-35 dias",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Period Length
        InputComponent(
            value = periodLength,
            onValueChange = { periodLength = it.filter { char -> char.isDigit() } },
            label = "Duração da menstruação (dias)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            supportingText = "Geralmente entre 2-7 dias"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Last Period Start Date
        InputComponent(
            value = dateFormatter.format(Date(lastPeriodStart)),
            onValueChange = { },
            label = "Início da última menstruação",
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = Icons.Default.DateRange,
            trailingIconDescription = "Selecionar data",
            onTrailingIconClick = { showDatePicker = true }
        )

        Spacer(modifier = Modifier.height(32.dp))

        ButtonComponent(
            onClick = {
                val cycleLen = cycleLength.toIntOrNull() ?: 28
                val periodLen = periodLength.toIntOrNull() ?: 5

                val cycle = Cycle(
                    id = state.cycle?.id ?: 0,
                    cycleLength = cycleLen,
                    periodLength = periodLen,
                    lastPeriodStart = lastPeriodStart
                )
                onSave(cycle)
            },
            shape = MaterialTheme.shapes.medium,
            text = "Salvar Configurações",
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),

            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth()
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = lastPeriodStart
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            lastPeriodStart = it
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
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun SuccessView() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✓ Configurações salvas com sucesso!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CycleScreenPreview() {
    ElaTheme {
        CycleContent(
            state = CycleUiState(),
            onSave = {}
        )
    }
}
