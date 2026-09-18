package com.example.ui.screens.chat

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
import com.example.data.local.entities.ConversaEntity
import com.example.data.local.entities.MensagemEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Obra360TopBar
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConversasListScreen(
    currentUserId: String,
    chatRepository: ChatRepository,
    authRepository: AuthRepository,
    onNavigateToChat: (String) -> Unit,
    onBack: () -> Unit
) {
    val conversas by chatRepository.getConversas(currentUserId).collectAsStateWithLifecycle(initialValue = emptyList())
    val otherUsers by authRepository.getAllOtherUsers(currentUserId).collectAsStateWithLifecycle(initialValue = emptyList())
    var showNewChatDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Obra360TopBar(
                title = "Mensagens Técnicas",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true },
                containerColor = Slate900,
                contentColor = PureWhite,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.AddComment, contentDescription = "Nova Conversa")
            }
        },
        containerColor = Slate50
    ) { padding ->
        if (conversas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    title = "Nenhuma conversa ativa",
                    description = "Inicie conversas diretas com arquitetos, engenheiros e mestres de obras para tirar dúvidas e alinhar projetos.",
                    actionLabel = "Iniciar Conversa",
                    onActionClick = { showNewChatDialog = true }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversas, key = { it.id }) { conversa ->
                    ConversaItemCard(conversa = conversa, onClick = { onNavigateToChat(conversa.id) })
                }
            }
        }
    }

    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("Selecionar Profissional") },
            text = {
                if (otherUsers.isEmpty()) {
                    Text("Nenhum outro profissional cadastrado no momento.")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(otherUsers, key = { it.id }) { u ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            val cId = chatRepository.getOrCreateConversa(currentUserId, u)
                                            showNewChatDialog = false
                                            onNavigateToChat(cId)
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = Slate50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Slate900),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(u.nome, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Slate900)
                                        Text(u.profissao, style = MaterialTheme.typography.labelSmall, color = Slate500)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNewChatDialog = false }) { Text("Fechar") }
            }
        )
    }
}

@Composable
fun ConversaItemCard(conversa: ConversaEntity, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val hora = dateFormat.format(Date(conversa.ultimaData))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Slate900),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = PureWhite, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(conversa.participanteNome, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Slate900)
                    Text(hora, style = MaterialTheme.typography.labelSmall, color = Slate400)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = conversa.ultimaMensagem.ifBlank { "Toque para abrir a conversa" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ChatDetailScreen(
    conversaId: String,
    currentUserId: String,
    chatRepository: ChatRepository,
    onBack: () -> Unit
) {
    val mensagens by chatRepository.getMensagens(conversaId).collectAsStateWithLifecycle(initialValue = emptyList())
    val conversa by chatRepository.getConversaById(conversaId).collectAsStateWithLifecycle(initialValue = null)
    var textInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Obra360TopBar(
                title = conversa?.participanteNome ?: "Conversa",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(mensagens, key = { it.id }) { msg ->
                    val isMe = (msg.remetenteId == currentUserId)
                    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val hora = dateFormat.format(Date(msg.data))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (isMe) Slate900 else PureWhite,
                            shape = RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (isMe) 14.dp else 2.dp,
                                bottomEnd = if (isMe) 2.dp else 14.dp
                            ),
                            tonalElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.conteudo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isMe) PureWhite else Slate800
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = hora,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isMe) Slate400 else Slate400,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            // Chat Input Bar
            Surface(
                color = PureWhite,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Digite sua mensagem...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                val txt = textInput
                                textInput = ""
                                scope.launch {
                                    chatRepository.enviarMensagem(conversaId, currentUserId, txt)
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Slate900, contentColor = PureWhite)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }
}
