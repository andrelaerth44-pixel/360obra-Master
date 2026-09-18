package com.example.ui.screens.obras

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.*
import com.example.data.repository.ObraRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Obra360TopBar
import com.example.ui.components.RealProgressBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObraDetailScreen(
    obraId: String,
    obraRepository: ObraRepository,
    onBack: () -> Unit
) {
    val obra by obraRepository.getObraById(obraId).collectAsStateWithLifecycle(initialValue = null)
    val etapas by obraRepository.getEtapas(obraId).collectAsStateWithLifecycle(initialValue = emptyList())
    val tarefas by obraRepository.getTarefas(obraId).collectAsStateWithLifecycle(initialValue = emptyList())
    val medicoes by obraRepository.getMedicoes(obraId).collectAsStateWithLifecycle(initialValue = emptyList())
    val diarios by obraRepository.getDiarios(obraId).collectAsStateWithLifecycle(initialValue = emptyList())
    val checklists by obraRepository.getChecklists(obraId).collectAsStateWithLifecycle(initialValue = emptyList())
    val problemas by obraRepository.getProblemas(obraId).collectAsStateWithLifecycle(initialValue = emptyList())
    val docs by obraRepository.getDocs(obraId).collectAsStateWithLifecycle(initialValue = emptyList())

    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Visão Geral", "Etapas & Tarefas", "Medições", "Diário", "Checklists", "Problemas", "Plantas")

    // Dialog state controllers
    var showAddEtapaDialog by remember { mutableStateOf(false) }
    var showAddTarefaDialog by remember { mutableStateOf(false) }
    var showAddMedicaoDialog by remember { mutableStateOf(false) }
    var showAddDiarioDialog by remember { mutableStateOf(false) }
    var showAddChecklistDialog by remember { mutableStateOf(false) }
    var showAddProblemaDialog by remember { mutableStateOf(false) }
    var showAddDocDialog by remember { mutableStateOf(false) }

    val currentObra = obra

    Scaffold(
        topBar = {
            Obra360TopBar(
                title = currentObra?.nome ?: "Obra",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            when (currentTab) {
                1 -> FloatingActionButton(
                    onClick = { showAddTarefaDialog = true },
                    containerColor = Slate900,
                    contentColor = PureWhite
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nova Tarefa")
                }
                2 -> FloatingActionButton(
                    onClick = { showAddMedicaoDialog = true },
                    containerColor = Slate900,
                    contentColor = PureWhite
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nova Medição")
                }
                3 -> FloatingActionButton(
                    onClick = { showAddDiarioDialog = true },
                    containerColor = Slate900,
                    contentColor = PureWhite
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Novo Registro no Diário")
                }
                4 -> FloatingActionButton(
                    onClick = { showAddChecklistDialog = true },
                    containerColor = Slate900,
                    contentColor = PureWhite
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Novo Checklist")
                }
                5 -> FloatingActionButton(
                    onClick = { showAddProblemaDialog = true },
                    containerColor = Slate900,
                    contentColor = PureWhite
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Registrar Problema")
                }
                6 -> FloatingActionButton(
                    onClick = { showAddDocDialog = true },
                    containerColor = Slate900,
                    contentColor = PureWhite
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Anexar Planta/Projeto")
                }
            }
        },
        containerColor = Slate50
    ) { padding ->
        if (currentObra == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Slate900)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = currentTab,
                containerColor = PureWhite,
                contentColor = Slate900,
                edgePadding = 16.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = (currentTab == index),
                        onClick = { currentTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (currentTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (currentTab) {
                0 -> TabVisaoGeral(obra = currentObra, etapasCount = etapas.size, tarefasCount = tarefas.size)
                1 -> TabEtapasETarefas(
                    obraId = obraId,
                    etapas = etapas,
                    tarefas = tarefas,
                    onAddEtapa = { showAddEtapaDialog = true },
                    onToggleTarefa = { tarefa ->
                        val nextStatus = if (tarefa.status == "CONCLUIDA") "PENDENTE" else "CONCLUIDA"
                        scope.launch {
                            obraRepository.updateTarefaStatus(tarefa.id, nextStatus, obraId)
                        }
                    }
                )
                2 -> TabMedicoes(medicoes = medicoes, onAdd = { showAddMedicaoDialog = true })
                3 -> TabDiario(diarios = diarios, onAdd = { showAddDiarioDialog = true })
                4 -> TabChecklists(
                    checklists = checklists,
                    obraRepository = obraRepository,
                    onAdd = { showAddChecklistDialog = true }
                )
                5 -> TabProblemas(
                    problemas = problemas,
                    onAdd = { showAddProblemaDialog = true },
                    onToggleStatus = { problema ->
                        val next = if (problema.status == "RESOLVIDO") "ABERTO" else "RESOLVIDO"
                        scope.launch {
                            obraRepository.updateProblemaStatus(problema.id, next)
                        }
                    }
                )
                6 -> TabDocumentos(docs = docs, onAdd = { showAddDocDialog = true })
            }
        }
    }

    // Modal Dialogs for real additions:
    if (showAddEtapaDialog) {
        AddEtapaModal(
            onDismiss = { showAddEtapaDialog = false },
            onConfirm = { nome, desc ->
                scope.launch {
                    obraRepository.addEtapa(obraId, nome, desc, etapas.size)
                    showAddEtapaDialog = false
                }
            }
        )
    }

    if (showAddTarefaDialog) {
        AddTarefaModal(
            etapas = etapas,
            onDismiss = { showAddTarefaDialog = false },
            onConfirm = { etapaId, titulo, desc, prioridade, responsavel ->
                scope.launch {
                    obraRepository.addTarefa(
                        etapaId = etapaId,
                        obraId = obraId,
                        titulo = titulo,
                        descricao = desc,
                        prioridade = prioridade,
                        prazo = System.currentTimeMillis() + 86400000L * 7,
                        responsavel = responsavel
                    )
                    showAddTarefaDialog = false
                }
            }
        )
    }

    if (showAddMedicaoDialog) {
        AddMedicaoModal(
            onDismiss = { showAddMedicaoDialog = false },
            onConfirm = { tipo, comp, larg, alt, unidade, resultado ->
                scope.launch {
                    obraRepository.addMedicao(
                        MedicaoEntity(
                            obraId = obraId,
                            tipo = tipo,
                            comprimento = comp,
                            largura = larg,
                            altura = alt,
                            unidade = unidade,
                            resultado = resultado
                        )
                    )
                    showAddMedicaoDialog = false
                }
            }
        )
    }

    if (showAddDiarioDialog) {
        AddDiarioModal(
            onDismiss = { showAddDiarioDialog = false },
            onConfirm = { atividades, equipe, obs, tempo ->
                scope.launch {
                    obraRepository.addDiario(
                        DiarioEntity(
                            obraId = obraId,
                            atividades = atividades,
                            equipe = equipe,
                            observacoes = obs,
                            condicoesTempo = tempo,
                            progressoObservado = currentObra?.progresso ?: 0
                        )
                    )
                    showAddDiarioDialog = false
                }
            }
        )
    }

    if (showAddChecklistDialog) {
        AddChecklistModal(
            onDismiss = { showAddChecklistDialog = false },
            onConfirm = { titulo, itens ->
                scope.launch {
                    obraRepository.addChecklist(obraId, titulo, itens)
                    showAddChecklistDialog = false
                }
            }
        )
    }

    if (showAddProblemaDialog) {
        AddProblemaModal(
            onDismiss = { showAddProblemaDialog = false },
            onConfirm = { titulo, desc, prioridade ->
                scope.launch {
                    obraRepository.addProblema(
                        ProblemaEntity(
                            obraId = obraId,
                            titulo = titulo,
                            descricao = desc,
                            prioridade = prioridade,
                            status = "ABERTO"
                        )
                    )
                    showAddProblemaDialog = false
                }
            }
        )
    }

    if (showAddDocDialog) {
        AddDocModal(
            onDismiss = { showAddDocDialog = false },
            onConfirm = { nome, tipo, uri ->
                scope.launch {
                    obraRepository.addDoc(
                        ProjetoDocEntity(
                            obraId = obraId,
                            nome = nome,
                            tipo = tipo,
                            arquivoUri = uri
                        )
                    )
                    showAddDocDialog = false
                }
            }
        )
    }
}

// Sub-views for each tab:
@Composable
fun TabVisaoGeral(obra: ObraEntity, etapasCount: Int, tarefasCount: Int) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val inicio = dateFormat.format(Date(obra.dataInicial))
    val previsao = dateFormat.format(Date(obra.previsao))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = obra.nome,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                        StatusBadge(status = obra.status)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = obra.descricao.ifBlank { "Sem descrição adicional informada." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    RealProgressBar(progress = obra.progresso, height = 10)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Dados Técnicos do Projeto",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    DetailRow(label = "Tipo de Construção", value = obra.tipo)
                    DetailRow(label = "Área Total Construída", value = "${obra.areaM2} m²")
                    DetailRow(label = "Localização da Obra", value = obra.localizacao)
                    DetailRow(label = "Cliente / Contratante", value = obra.cliente.ifBlank { "Não informado" })
                    DetailRow(label = "Data de Início", value = inicio)
                    DetailRow(label = "Previsão de Término", value = previsao)
                    DetailRow(label = "Visibilidade", value = if (obra.isPublica) "Pública (Portfólio)" else "Privada")
                    DetailRow(label = "Etapas Planejadas", value = "$etapasCount etapas")
                    DetailRow(label = "Tarefas Cadastradas", value = "$tarefasCount tarefas")
                }
            }
        }
    }
}

@Composable
fun TabEtapasETarefas(
    obraId: String,
    etapas: List<EtapaEntity>,
    tarefas: List<TarefaEntity>,
    onAddEtapa: () -> Unit,
    onToggleTarefa: (TarefaEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cronograma de Etapas",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
                TextButton(onClick = onAddEtapa) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar Etapa")
                }
            }
        }

        if (etapas.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Outlined.Checklist,
                    title = "Nenhuma etapa criada",
                    description = "Crie etapas como Fundação, Alvenaria ou Instalações.",
                    actionLabel = "Criar Etapa",
                    onActionClick = onAddEtapa
                )
            }
        } else {
            items(etapas, key = { it.id }) { etapa ->
                val tarefasDaEtapa = tarefas.filter { it.etapaId == etapa.id }
                val concluídas = tarefasDaEtapa.count { it.status == "CONCLUIDA" }

                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = etapa.nome,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            Surface(
                                color = Slate100,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$concluídas/${tarefasDaEtapa.size} tarefas",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate700
                                )
                            }
                        }

                        if (tarefasDaEtapa.isEmpty()) {
                            Text(
                                text = "Nenhuma tarefa vinculada a esta etapa.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            tarefasDaEtapa.forEach { tarefa ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleTarefa(tarefa) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = (tarefa.status == "CONCLUIDA"),
                                        onCheckedChange = { onToggleTarefa(tarefa) },
                                        colors = CheckboxDefaults.colors(checkedColor = SlateTeal)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tarefa.titulo,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = if (tarefa.status == "CONCLUIDA") Slate400 else Slate900
                                        )
                                        if (tarefa.responsavel.isNotBlank()) {
                                            Text(
                                                text = "Resp: ${tarefa.responsavel}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate500
                                            )
                                        }
                                    }
                                    StatusBadge(status = tarefa.prioridade)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabMedicoes(medicoes: List<MedicaoEntity>, onAdd: () -> Unit) {
    if (medicoes.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.Straighten,
            title = "Nenhuma medição registrada",
            description = "Registre medições de área, volume de concreto, vãos ou extensões associadas a esta obra.",
            actionLabel = "Registrar Medição Real",
            onActionClick = onAdd
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(medicoes, key = { it.id }) { medicao ->
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = medicao.tipo,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            Text(
                                text = "Dimensões: ${medicao.comprimento} x ${medicao.largura} x ${medicao.altura}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                            Text(
                                text = "Data: ${dateFormat.format(Date(medicao.data))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                        Text(
                            text = "${medicao.resultado} ${medicao.unidade}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Slate900
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabDiario(diarios: List<DiarioEntity>, onAdd: () -> Unit) {
    if (diarios.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.MenuBook,
            title = "Diário de Obra vazio",
            description = "Registre as atividades diárias, efetivo no canteiro, condições climáticas e ocorrências técnicas.",
            actionLabel = "Novo Registro Diário",
            onActionClick = onAdd
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(diarios, key = { it.id }) { diario ->
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dateFormat.format(Date(diario.data)),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            Surface(
                                color = Slate100,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = diario.condicoesTempo,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate700
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Atividades Executadas:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )
                        Text(
                            text = diario.atividades,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate700
                        )
                        if (diario.equipe.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Equipe no canteiro: ${diario.equipe}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }
                        if (diario.observacoes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Observações: ${diario.observacoes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabChecklists(
    checklists: List<ChecklistEntity>,
    obraRepository: ObraRepository,
    onAdd: () -> Unit
) {
    val scope = rememberCoroutineScope()

    if (checklists.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.FactCheck,
            title = "Nenhum checklist criado",
            description = "Crie rotinas de inspeção, liberação de concretagem, segurança e conferência de armadura.",
            actionLabel = "Criar Checklist",
            onActionClick = onAdd
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(checklists, key = { it.id }) { checklist ->
                val itens by obraRepository.getChecklistItens(checklist.id).collectAsStateWithLifecycle(initialValue = emptyList())
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = checklist.titulo,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        itens.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch { obraRepository.toggleChecklistItem(item) }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.concluido,
                                    onCheckedChange = {
                                        scope.launch { obraRepository.toggleChecklistItem(item) }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = SlateTeal)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.texto,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (item.concluido) Slate400 else Slate800
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabProblemas(
    problemas: List<ProblemaEntity>,
    onAdd: () -> Unit,
    onToggleStatus: (ProblemaEntity) -> Unit
) {
    if (problemas.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.CheckCircle,
            title = "Nenhum problema pendente",
            description = "Nenhuma não-conformidade ou problema técnico registrado nesta obra.",
            actionLabel = "Registrar Problema",
            onActionClick = onAdd
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(problemas, key = { it.id }) { prob ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prob.titulo,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            StatusBadge(status = prob.status)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = prob.descricao,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(status = "Prioridade: ${prob.prioridade}")
                            TextButton(onClick = { onToggleStatus(prob) }) {
                                Text(if (prob.status == "RESOLVIDO") "Reabrir" else "Marcar como Resolvido")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabDocumentos(docs: List<ProjetoDocEntity>, onAdd: () -> Unit) {
    if (docs.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.FolderShared,
            title = "Nenhuma planta ou projeto anexado",
            description = "Anexe referências de projetos arquitetônicos, estruturais, elétricos e memoriais descritivos.",
            actionLabel = "Anexar Projeto",
            onActionClick = onAdd
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(docs, key = { it.id }) { doc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Description, contentDescription = null, tint = Slate800)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.nome,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            Text(
                                text = doc.tipo,
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Slate500)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = Slate900)
    }
}

// Modal implementations for real insertions:
@Composable
fun AddEtapaModal(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var nome by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Etapa Técnica") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da etapa (ex: Cobertura, Pintura)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descrição técnica") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nome.isNotBlank()) onConfirm(nome, desc) },
                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddTarefaModal(
    etapas: List<EtapaEntity>,
    onDismiss: () -> Unit,
    onConfirm: (etapaId: String, titulo: String, desc: String, prioridade: String, responsavel: String) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var responsavel by remember { mutableStateOf("") }
    var selectedEtapaId by remember { mutableStateOf(etapas.firstOrNull()?.id ?: "") }
    var prioridade by remember { mutableStateOf("MEDIA") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Tarefa Técnica") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título da tarefa") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = responsavel,
                    onValueChange = { responsavel = it },
                    label = { Text("Responsável no canteiro") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Instruções de execução") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isNotBlank()) {
                        val etapa = if (selectedEtapaId.isNotBlank()) selectedEtapaId else etapas.firstOrNull()?.id ?: ""
                        onConfirm(etapa, titulo, desc, prioridade, responsavel)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
            ) {
                Text("Criar Tarefa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddMedicaoModal(
    onDismiss: () -> Unit,
    onConfirm: (tipo: String, comp: Double, larg: Double, alt: Double, unidade: String, resultado: Double) -> Unit
) {
    var tipo by remember { mutableStateOf("Área de Piso") }
    var compStr by remember { mutableStateOf("") }
    var largStr by remember { mutableStateOf("") }
    var altStr by remember { mutableStateOf("1.0") }
    var unidade by remember { mutableStateOf("m²") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Medição") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text("Tipo de medição") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = compStr,
                    onValueChange = { compStr = it },
                    label = { Text("Comprimento (m)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = largStr,
                    onValueChange = { largStr = it },
                    label = { Text("Largura (m)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c = compStr.toDoubleOrNull() ?: 0.0
                    val l = largStr.toDoubleOrNull() ?: 0.0
                    val a = altStr.toDoubleOrNull() ?: 1.0
                    val res = c * l * (if (a == 0.0) 1.0 else a)
                    onConfirm(tipo, c, l, a, unidade, res)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
            ) {
                Text("Salvar Medição")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddDiarioModal(
    onDismiss: () -> Unit,
    onConfirm: (atividades: String, equipe: String, obs: String, tempo: String) -> Unit
) {
    var atividades by remember { mutableStateOf("") }
    var equipe by remember { mutableStateOf("") }
    var obs by remember { mutableStateOf("") }
    var tempo by remember { mutableStateOf("Ensolarado") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registro Diário de Obra") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = atividades,
                    onValueChange = { atividades = it },
                    label = { Text("Atividades executadas") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = equipe,
                    onValueChange = { equipe = it },
                    label = { Text("Equipe no local (ex: 2 pedreiros, 3 serventes)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tempo,
                    onValueChange = { tempo = it },
                    label = { Text("Condição Climática") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = obs,
                    onValueChange = { obs = it },
                    label = { Text("Observações técnicas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (atividades.isNotBlank()) onConfirm(atividades, equipe, obs, tempo) },
                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
            ) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddChecklistModal(
    onDismiss: () -> Unit,
    onConfirm: (titulo: String, itens: List<String>) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var item1 by remember { mutableStateOf("") }
    var item2 by remember { mutableStateOf("") }
    var item3 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Checklist Técnico") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título da Inspeção") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = item1,
                    onValueChange = { item1 = it },
                    label = { Text("Item 1") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = item2,
                    onValueChange = { item2 = it },
                    label = { Text("Item 2") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = item3,
                    onValueChange = { item3 = it },
                    label = { Text("Item 3") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isNotBlank()) {
                        onConfirm(titulo, listOf(item1, item2, item3))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
            ) {
                Text("Criar Checklist")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddProblemaModal(
    onDismiss: () -> Unit,
    onConfirm: (titulo: String, desc: String, prioridade: String) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var prioridade by remember { mutableStateOf("ALTA") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar Problema Técnico") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título da Não-Conformidade") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descrição detalhada do problema") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (titulo.isNotBlank()) onConfirm(titulo, desc, prioridade) },
                colors = ButtonDefaults.buttonColors(containerColor = AlertOrange)
            ) {
                Text("Registrar Não-Conformidade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddDocModal(
    onDismiss: () -> Unit,
    onConfirm: (nome: String, tipo: String, uri: String) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Planta Baixa Arquitetônica") }
    var uri by remember { mutableStateOf("doc://planta_baixa.pdf") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Anexar Projeto / Planta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do Documento") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text("Tipo (ex: Estrutural, Elétrica, Hidráulica)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nome.isNotBlank()) onConfirm(nome, tipo, uri) },
                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
