package com.example.ela.ui.screens.care

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.ui.components.ButtonComponent
import com.example.ela.ui.components.ErrorView
import com.example.ela.ui.components.InputComponent
import com.example.ela.ui.components.LoadingView
import com.example.ela.ui.theme.*
import com.example.ela.viewmodel.CareActionViewModel

@Composable
fun ManageCareScreen(
    viewModel: CareActionViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showEditor by remember { mutableStateOf(false) }
    var editingAction by remember { mutableStateOf<CareAction?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ManageCareTopBar(
                onBack = onBack,
                onDeleteAllClick = { showDeleteAllConfirm = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingAction = null
                    showEditor = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Cuidado")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ManageCareScreenContent(
                state = state,
                onDelete = { action -> viewModel.delete(action.id) },
                onEdit = { action ->
                    editingAction = action
                    showEditor = true
                },
                actions = state.actions,
                viewModel = viewModel,
                onBack = onBack
            )
            if (showDeleteAllConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteAllConfirm = false },
                    title = { Text("Deletar Todos") },
                    text = { Text("Tem certeza que deseja remover todos os cuidados cadastrados? Esta ação não pode ser desfeita.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteAll()
                                showDeleteAllConfirm = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Deletar Tudo")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteAllConfirm = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (showEditor) {
                CareActionEditorBottomSheet(
                    action = editingAction,
                    onDismiss = { showEditor = false },
                    onSave = { action ->
                        if (editingAction == null) {
                            viewModel.save(action)
                        } else {
                            viewModel.update(action)
                        }
                        showEditor = false
                    }
                )
            }
        }
    }
}

@Composable
fun ManageCareTopBar(
    onBack: () -> Unit,
    onDeleteAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    MaterialTheme.colorScheme.background
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Gerenciar Cuidados",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        IconButton(
            onClick = onDeleteAllClick,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Deletar Todos",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ManageCareScreenContent(
    state: CareActionUiState,
    onDelete: (CareAction) -> Unit,
    onEdit: (CareAction) -> Unit,
    actions: List<CareAction>,
    onBack: () -> Unit,
    viewModel: CareActionViewModel
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> LoadingView()
            state.error != null -> ErrorView(state.error)
            else -> ManageCareContent(
                actions = actions,
                onDelete = onDelete,
                onEdit = onEdit,
                onBack = onBack
            )
        }
    }
}

@Composable
fun ManageCareContent(
    actions: List<CareAction>,
    onDelete: (CareAction) -> Unit = {},
    onEdit: (CareAction) -> Unit = {},
    onBack: () -> Unit = {}
){
    val phases = CyclePhase.entries

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(phases.size) { index ->
                    val phase = phases[index]
                    val phaseActions = actions.filter {
                        it.phase == phase
                    }

                    CareDropdown(
                        phase = phase,
                        actions = phaseActions,
                        onClickDelete = onDelete,
                        onClickEdite = onEdit
                    )
                }
            }
        }
    }
}

@Composable
fun CareDropdown(
    phase: CyclePhase,
    actions: List<CareAction>,
    onClickDelete: (CareAction) -> Unit = {},
    onClickEdite: (CareAction) -> Unit = {},
){
    var expanded by remember { mutableStateOf(false) }
    val phaseColor = getPhaseColor(phase)

    var iconDropdown = if(expanded)
            Icons.Default.ArrowDropUp
        else
            Icons.Default.ArrowDropDown

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {expanded = !expanded}),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ){
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(phaseColor.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 20.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(phaseColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = getPhaseTitle(phase),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    iconDropdown,
                    "Dropdown",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                            top = 8.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (actions.isEmpty()) {
                        Text(
                            text = "Nenhum cuidado cadastrado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }else{
                        actions.forEach { action ->
                            CareItemManege(
                                action = action,
                                onClickDelete = {
                                    onClickDelete(action)
                                },
                                onClickEdite = {
                                    onClickEdite(action)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CareItemManege(
    action: CareAction,
    onClickDelete: () -> Unit = {},
    onClickEdite: () -> Unit = {},
    phaseColor: Color? = MenstrualColor,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = action.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    action.description.let {
                        Spacer(modifier = Modifier.height(2.dp))
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

                Box {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        onClick = { expanded = true },
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = { expanded = false; onClickEdite() }
                        )
                        DropdownMenuItem(
                            text = { Text("Deletar") },
                            onClick = { expanded = false; onClickDelete() }
                        )
                    }
                }
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareActionEditorBottomSheet(
    action: CareAction?,
    onDismiss: () -> Unit,
    onSave: (CareAction) -> Unit
) {
    var title by remember { mutableStateOf(action?.title ?: "") }
    var description by remember { mutableStateOf(action?.description ?: "") }
    var selectedPhase by remember { mutableStateOf(action?.phase ?: CyclePhase.MENSTRUAL) }
    var expanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (action == null) "Novo Cuidado" else "Editar Cuidado",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            InputComponent(
                value = title,
                onValueChange = { title = it },
                label = "Título",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            InputComponent(
                value = description,
                onValueChange = { description = it },
                label = "Descrição (opcional)",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    onTrailingIconClick = { expanded = !expanded },
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

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ButtonComponent(
                    text = "Cancelar",
                    onClick = onDismiss,
                    textColor = MaterialTheme.colorScheme.primary,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.weight(1f)
                )

                ButtonComponent(
                    text = "Salvar",
                    textColor = Color.White,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                CareAction(
                                    id = action?.id ?: 0,
                                    title = title,
                                    description = description,
                                    phase = selectedPhase,
                                    isCompleted = action?.isCompleted ?: false
                                )
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewManegeCareScreen() {
    ElaTheme {
        ManageCareContent(
            actions = listOf(
                CareAction(1, "Teste Cuidado", "Descrição do cuidado", CyclePhase.MENSTRUAL, false)
            ),
            onDelete = {},
            onEdit = {},
        )
    }
}
