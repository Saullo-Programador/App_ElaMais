package com.example.ela.ui.screens.care

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.ui.components.ErrorView
import com.example.ela.ui.components.InputComponent
import com.example.ela.ui.components.LoadingView
import com.example.ela.ui.theme.*
import com.example.ela.viewmodel.CareActionViewModel

@Composable
fun CareScreen(
    phase: CyclePhase,
    viewModel: CareActionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(phase) {
        viewModel.load(phase)
    }

    CareScreenContent(
        state = state,
        onToggle = viewModel::toggleDone,
        onSave = viewModel::save
    )
}

@Composable
fun CareScreenContent(
    state: CareActionUiState,
    onToggle: (CareAction) -> Unit,
    onSave: (CareAction) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> LoadingView()
            state.error != null -> ErrorView(state.error)
            else -> CareContent(
                phase = state.phase,
                actions = state.actions,
                onToggle = onToggle,
                onSave = onSave
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CareContent(
    phase: CyclePhase?,
    actions: List<CareAction>,
    onToggle: (CareAction) -> Unit,
    onSave: (CareAction) -> Unit
) {
    val phaseColor = getPhaseColor(phase)
    val gradientColors = listOf(
        phaseColor.copy(alpha = 0.8f),
        phaseColor.copy(alpha = 0.4f)
    )

    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header com gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(gradientColors)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    val phaseEmoji = when (phase) {
                        CyclePhase.MENSTRUAL -> "🩸"
                        CyclePhase.FOLLICULAR -> "🌱"
                        CyclePhase.OVULATION -> "💕"
                        CyclePhase.LUTEAL -> "🌙"
                        CyclePhase.TPM -> "😅"
                        null -> "✨"
                    }

                    Text(
                        text = "$phaseEmoji ${getPhaseTitle(phase)}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = getPhaseDescription(phase),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progresso
                    val completedCount = actions.count { it.isCompleted }
                    val totalCount = actions.size
                    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.weight(1f),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            gapSize = 0.dp,
                            drawStopIndicator = {}
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$completedCount/$totalCount",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }

            // Lista de ações
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(actions) { action ->
                    CareItem(
                        action = action,
                        phaseColor = phaseColor,
                        onToggle = { onToggle(action) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = phaseColor,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar novos Cuidados")
        }
    }
    if (showAddDialog) {
        AddCareDialog(
            onDismiss = { showAddDialog = false },
            onSave = { care ->
                onSave(care)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCareDialog(
    onDismiss: () -> Unit,
    onSave: (CareAction) -> Unit
) {

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPhase by remember { mutableStateOf(CyclePhase.MENSTRUAL) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text("Novo Cuidados") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                InputComponent(
                    value = title,
                    onValueChange = { title = it },
                    label = "Título",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                InputComponent(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descrição (opcional)",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    InputComponent(
                        value = getPhaseTitle(selectedPhase),
                        onValueChange = {},
                        readOnly = true,
                        label = "Fase do ciclo",
                        trailingIcon = Icons.Default.ArrowDropDown,
                        trailingIconDescription = "Selecionar fase",
                        onTrailingIconClick = { expanded = expanded },
                        modifier = Modifier
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        CyclePhase.entries.forEach { phase ->
                            DropdownMenuItem(
                                text = { Text(getPhaseTitle(phase)) },
                                onClick = {
                                    selectedPhase = phase
                                    expanded = false
                                }
                            )
                        }
                    }
                }

            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            CareAction(
                                id = 0,
                                title = title,
                                description = description,
                                phase = selectedPhase,
                                isCompleted = false
                            )
                        )
                    }
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

@Composable
fun CareItem(
    action: CareAction,
    phaseColor: Color,
    onToggle: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (action.isCompleted)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surface,
        label = "background"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (action.isCompleted) 0.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox customizado
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (action.isCompleted) phaseColor
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (action.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (action.isCompleted) TextDecoration.LineThrough else null,
                    color = if (action.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                action.description.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (action.isCompleted) 0.4f else 0.8f
                        ),
                        textDecoration = if (action.isCompleted) TextDecoration.LineThrough else null
                    )
                }
            }
        }
    }
}

@Composable
fun getPhaseTitle(phase: CyclePhase?): String {
    return when (phase) {
        CyclePhase.MENSTRUAL -> "Período Menstrual"
        CyclePhase.FOLLICULAR -> "Fase Folicular"
        CyclePhase.OVULATION -> "Ovulação"
        CyclePhase.LUTEAL -> "Fase Lútea"
        CyclePhase.TPM -> "TPM"
        null -> "Cuidados"
    }
}

@Composable
fun getPhaseDescription(phase: CyclePhase?): String {
    return when (phase) {
        CyclePhase.MENSTRUAL -> "Momento de cuidar de si. Priorize descanso e conforto."
        CyclePhase.FOLLICULAR -> "Energia renovada! Ótimo momento para novos projetos."
        CyclePhase.OVULATION -> "Seu pico de energia e fertilidade. Aproveite!"
        CyclePhase.LUTEAL -> "Fase de integração. Organize e planeje."
        CyclePhase.TPM -> "Seja gentil com você mesma. Cuide do seu bem-estar."
        null -> "Complete as ações para cuidar de você"
    }
}

@Composable
fun getPhaseColor(phase: CyclePhase?): Color {
    return when (phase) {
        CyclePhase.MENSTRUAL -> MenstrualColor
        CyclePhase.FOLLICULAR -> FollicularColor
        CyclePhase.OVULATION -> OvulationColor
        CyclePhase.LUTEAL -> LutealColor
        CyclePhase.TPM -> TpmColor
        null -> Lavender500
    }
}

@Preview(showBackground = true)
@Composable
fun CareScreenPreview() {
    ElaTheme {
        CareScreenContent(
            state = CareActionUiState(
                phase = CyclePhase.MENSTRUAL,
                actions = listOf(
                    CareAction(1, "Beber Água", "Mantenha-se hidratada", CyclePhase.MENSTRUAL, false),
                    CareAction(2, "Descansar", "Dê um tempo para o seu corpo", CyclePhase.MENSTRUAL, true),
                    CareAction(3, "Comer chocolate", "Você merece 🍫", CyclePhase.MENSTRUAL, false)
                )
            ),
            onToggle = {},
            onSave = {}
        )
    }
}
