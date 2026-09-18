package com.example.ui.screens.home

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.DiarioEntity
import com.example.data.local.entities.ObraEntity
import com.example.data.local.entities.ProblemaEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.ObraRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Obra360TopBar
import com.example.ui.components.RealProgressBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    userId: String,
    authRepository: AuthRepository,
    obraRepository: ObraRepository,
    onNavigateToObraDetail: (String) -> Unit,
    onNavigateToCreateObra: () -> Unit,
    onNavigateToCalculators: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToDiario: (String) -> Unit
) {
    val currentUser by authRepository.getCurrentUserFlow(userId).collectAsStateWithLifecycle(initialValue = null)
    val obras by obraRepository.getObrasByUser(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val recentDiarios by obraRepository.getRecentDiarios().collectAsStateWithLifecycle(initialValue = emptyList())
    val problemasPendentes by obraRepository.getProblemasPendentes().collectAsStateWithLifecycle(initialValue = emptyList())

    val totalObras = obras.size
    val obrasEmAndamento = obras.count { it.status == "EM_ANDAMENTO" }
    val mediaProgresso = if (obras.isNotEmpty()) obras.map { it.progresso }.average().toInt() else 0

    Scaffold(
        topBar = {
            Obra360TopBar(
                title = "Painel da Obra",
                onChatClick = onNavigateToChat,
                onCalculatorsClick = onNavigateToCalculators
            )
        },
        containerColor = Slate50
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Header Welcome
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Slate800),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentUser?.nome ?: "Profissional",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite
                                )
                                Text(
                                    text = "${currentUser?.profissao ?: "Engenharia"} • ${currentUser?.localizacao ?: "Brasil"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Stats Grid inside header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMiniItem(label = "Obras Ativas", value = "$obrasEmAndamento")
                            StatMiniItem(label = "Total Cadastrado", value = "$totalObras")
                            StatMiniItem(label = "Progresso Médio", value = "$mediaProgresso%")
                        }
                    }
                }
            }

            // Quick Actions Section
            item {
                Text(
                    text = "Ações Rápidas de Campo",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate800
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Outlined.AddHomeWork,
                        title = "Nova Obra",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCreateObra
                    )
                    QuickActionCard(
                        icon = Icons.Outlined.Calculate,
                        title = "Calculadoras",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCalculators
                    )
                    QuickActionCard(
                        icon = Icons.Outlined.MenuBook,
                        title = "Diário Campo",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (obras.isNotEmpty()) {
                                onNavigateToDiario(obras.first().id)
                            } else {
                                onNavigateToCreateObra()
                            }
                        }
                    )
                }
            }

            // Active Works Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Minhas Obras",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    if (obras.isNotEmpty()) {
                        Text(
                            text = "${obras.size} cadastradas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
            }

            if (obras.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        EmptyStateView(
                            icon = Icons.Outlined.Construction,
                            title = "Nenhuma obra cadastrada",
                            description = "Cadastre sua primeira obra para planejar etapas, tarefas, medições e diário técnico.",
                            actionLabel = "Cadastrar Primeira Obra",
                            onActionClick = onNavigateToCreateObra
                        )
                    }
                }
            } else {
                items(obras, key = { it.id }) { obra ->
                    ObraHomeCard(
                        obra = obra,
                        onClick = { onNavigateToObraDetail(obra.id) }
                    )
                }
            }

            // Recent Daily Logs Section
            if (recentDiarios.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Últimos Diários de Campo",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate800
                    )
                }
                items(recentDiarios, key = { it.id }) { diario ->
                    DiarioMiniCard(diario = diario)
                }
            }

            // Unresolved Issues Section
            if (problemasPendentes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pendências & Não-Conformidades",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate800
                    )
                }
                items(problemasPendentes, key = { it.id }) { problema ->
                    ProblemaMiniCard(problema = problema)
                }
            }
        }
    }
}

@Composable
fun StatMiniItem(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = PureWhite
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Slate400
        )
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Slate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Slate900,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ObraHomeCard(
    obra: ObraEntity,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dataInicioStr = dateFormat.format(Date(obra.dataInicial))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = obra.nome,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${obra.tipo} • ${obra.areaM2} m² • ${obra.localizacao}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusBadge(status = obra.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            RealProgressBar(progress = obra.progresso)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Início: $dataInicioStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gerenciar",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Slate900,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DiarioMiniCard(diario: DiarioEntity) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dataStr = dateFormat.format(Date(diario.data))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Registro de Campo: $dataStr",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
                Text(
                    text = diario.condicoesTempo,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = diario.atividades,
                style = MaterialTheme.typography.bodySmall,
                color = Slate700,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProblemaMiniCard(problema: ProblemaEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AlertOrangeLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = null,
                    tint = AlertOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = problema.titulo,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
                Text(
                    text = problema.descricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusBadge(status = problema.prioridade)
        }
    }
}
