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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.ObraEntity
import com.example.data.repository.ObraRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Obra360TopBar
import com.example.ui.components.RealProgressBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ObrasListScreen(
    userId: String,
    obraRepository: ObraRepository,
    onNavigateToObraDetail: (String) -> Unit,
    onNavigateToCreateObra: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToCalculators: () -> Unit
) {
    val obras by obraRepository.getObrasByUser(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedTab by remember { mutableStateOf("TODAS") }

    val filteredObras = remember(obras, selectedTab) {
        when (selectedTab) {
            "EM_ANDAMENTO" -> obras.filter { it.status == "EM_ANDAMENTO" }
            "CONCLUIDA" -> obras.filter { it.status == "CONCLUIDA" }
            else -> obras
        }
    }

    Scaffold(
        topBar = {
            Obra360TopBar(
                title = "Gestão de Obras",
                onChatClick = onNavigateToChat,
                onCalculatorsClick = onNavigateToCalculators
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateObra,
                containerColor = Slate900,
                contentColor = PureWhite,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Obra")
            }
        },
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Status Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = (selectedTab == "TODAS"),
                    onClick = { selectedTab = "TODAS" },
                    label = { Text("Todas (${obras.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Slate900,
                        selectedLabelColor = PureWhite
                    )
                )
                FilterChip(
                    selected = (selectedTab == "EM_ANDAMENTO"),
                    onClick = { selectedTab = "EM_ANDAMENTO" },
                    label = { Text("Em Andamento (${obras.count { it.status == "EM_ANDAMENTO" }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Slate900,
                        selectedLabelColor = PureWhite
                    )
                )
                FilterChip(
                    selected = (selectedTab == "CONCLUIDA"),
                    onClick = { selectedTab = "CONCLUIDA" },
                    label = { Text("Concluídas (${obras.count { it.status == "CONCLUIDA" }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Slate900,
                        selectedLabelColor = PureWhite
                    )
                )
            }

            if (filteredObras.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Outlined.Apartment,
                        title = "Nenhuma obra encontrada.",
                        description = "Crie sua obra para controlar cronograma, custos, diário de obra e medições com precisão técnica.",
                        actionLabel = "Cadastrar Nova Obra",
                        onActionClick = onNavigateToCreateObra
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredObras, key = { it.id }) { obra ->
                        ObraFullItemCard(
                            obra = obra,
                            onClick = { onNavigateToObraDetail(obra.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ObraFullItemCard(
    obra: ObraEntity,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val inicio = dateFormat.format(Date(obra.dataInicial))
    val previsao = dateFormat.format(Date(obra.previsao))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
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
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Cliente: ${obra.cliente.ifBlank { "Não informado" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }
                StatusBadge(status = obra.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoTag(icon = Icons.Outlined.Category, text = obra.tipo)
                InfoTag(icon = Icons.Outlined.SquareFoot, text = "${obra.areaM2} m²")
                InfoTag(icon = Icons.Outlined.LocationOn, text = obra.localizacao)
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
                    text = "Prazo: $inicio até $previsao",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Acessar Módulos",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Slate900,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoTag(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Slate500, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Slate600,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
