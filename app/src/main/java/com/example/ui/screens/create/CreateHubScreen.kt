package com.example.ui.screens.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.ObraRepository
import com.example.ui.components.Obra360TopBar
import com.example.ui.theme.*

@Composable
fun CreateHubScreen(
    userId: String,
    obraRepository: ObraRepository,
    onNavigateToCreateObra: () -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToCalculators: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToObraDetail: (String) -> Unit
) {
    val obras by obraRepository.getObrasByUser(userId).collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            Obra360TopBar(
                title = "Novo Registro",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "O que você deseja registrar hoje?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
                Text(
                    text = "Selecione uma ação técnica para atualizar o controle de engenharia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            item {
                CreateOptionCard(
                    icon = Icons.Outlined.AddHomeWork,
                    title = "Cadastrar Nova Obra",
                    description = "Crie uma nova obra com cronograma de etapas, tarefas, orçamento técnico e controle de medições.",
                    badge = "Principal",
                    onClick = onNavigateToCreateObra
                )
            }

            item {
                CreateOptionCard(
                    icon = Icons.Outlined.PostAdd,
                    title = "Publicar na Comunidade",
                    description = "Compartilhe evolução de obras, fotos, vídeos, antes e depois ou artigos técnicos para o feed profissional.",
                    badge = "Comunidade",
                    onClick = onNavigateToCreatePost
                )
            }

            item {
                CreateOptionCard(
                    icon = Icons.Outlined.Calculate,
                    title = "Calcular Materiais & Estrutura",
                    description = "Acesse 15 calculadoras reais de concreto, aço, cimento, tijolos, argamassa e escavação.",
                    badge = "Engenharia",
                    onClick = onNavigateToCalculators
                )
            }

            if (obras.isNotEmpty()) {
                item {
                    CreateOptionCard(
                        icon = Icons.Outlined.MenuBook,
                        title = "Lançar no Diário de Obra",
                        description = "Adicione atividades executadas hoje na sua obra ativa (${obras.first().nome}).",
                        badge = "Canteiro",
                        onClick = { onNavigateToObraDetail(obras.first().id) }
                    )
                }

                item {
                    CreateOptionCard(
                        icon = Icons.Outlined.Straighten,
                        title = "Registrar Medição Técnica",
                        description = "Calcule e armazene medições de área ou volume na obra (${obras.first().nome}).",
                        badge = "Medição",
                        onClick = { onNavigateToObraDetail(obras.first().id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CreateOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    badge: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Slate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Slate900,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
