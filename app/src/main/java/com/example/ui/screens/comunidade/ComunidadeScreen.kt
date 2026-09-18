package com.example.ui.screens.comunidade

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.entities.ComentarioEntity
import com.example.data.local.entities.PublicacaoEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.CommunityRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Obra360TopBar
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunidadeScreen(
    currentUserId: String,
    authRepository: AuthRepository,
    communityRepository: CommunityRepository,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToCalculators: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit
) {
    val posts by communityRepository.getFeed().collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedFilter by remember { mutableStateOf("TODAS") }
    var activeCommentPostId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val filteredPosts = remember(posts, selectedFilter) {
        when (selectedFilter) {
            "TODAS" -> posts
            "EVOLUCAO" -> posts.filter { it.tipo == "EVOLUCAO" || it.tipo == "ANTES_DEPOIS" }
            "FOTOS" -> posts.filter { it.tipo == "FOTO" || it.tipo == "VIDEO" }
            "OBRAS" -> posts.filter { it.tipo == "OBRA" }
            else -> posts
        }
    }

    Scaffold(
        topBar = {
            Obra360TopBar(
                title = "Comunidade Técnica",
                onChatClick = onNavigateToChat,
                onCalculatorsClick = onNavigateToCalculators
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreatePost,
                containerColor = Slate900,
                contentColor = PureWhite,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Publicação")
            }
        },
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = (selectedFilter == "TODAS"),
                    onClick = { selectedFilter = "TODAS" },
                    label = { Text("Todas") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Slate900,
                        selectedLabelColor = PureWhite
                    )
                )
                FilterChip(
                    selected = (selectedFilter == "EVOLUCAO"),
                    onClick = { selectedFilter = "EVOLUCAO" },
                    label = { Text("Evolução") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Slate900,
                        selectedLabelColor = PureWhite
                    )
                )
                FilterChip(
                    selected = (selectedFilter == "FOTOS"),
                    onClick = { selectedFilter = "FOTOS" },
                    label = { Text("Fotos & Obras") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Slate900,
                        selectedLabelColor = PureWhite
                    )
                )
            }

            if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Outlined.Groups,
                        title = "Ainda não há publicações.",
                        description = "Compartilhe a evolução de uma obra, técnicas executivas ou soluções de engenharia com a comunidade.",
                        actionLabel = "Fazer Primeira Publicação",
                        onActionClick = onNavigateToCreatePost
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredPosts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            currentUserId = currentUserId,
                            communityRepository = communityRepository,
                            onCommentClick = { activeCommentPostId = post.id },
                            onProfileClick = { onNavigateToUserProfile(post.autorId) }
                        )
                    }
                }
            }
        }
    }

    // Comment Sheet Dialog
    if (activeCommentPostId != null) {
        CommentsBottomSheet(
            postId = activeCommentPostId!!,
            currentUserId = currentUserId,
            authRepository = authRepository,
            communityRepository = communityRepository,
            onDismiss = { activeCommentPostId = null }
        )
    }
}

@Composable
fun PostCard(
    post: PublicacaoEntity,
    currentUserId: String,
    communityRepository: CommunityRepository,
    onCommentClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val isLiked by communityRepository.isPostLikedByUser(post.id, currentUserId)
        .collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
    val dataStr = dateFormat.format(Date(post.data))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Author Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onProfileClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Slate900),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.autorNome,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    Text(
                        text = "${post.autorProfissao} • $dataStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = post.tipo.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Slate700
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Text
            Text(
                text = post.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate800,
                lineHeight = 22.sp
            )

            // Attached Work Badge if present
            if (!post.obraNome.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = null,
                            tint = Slate800,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Obra associada: ${post.obraNome}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )
                    }
                }
            }

            // Attached Media (real local URI via Coil AsyncImage)
            if (!post.mediaUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    AsyncImage(
                        model = post.mediaUri,
                        contentDescription = "Mídia da obra",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Slate100, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons (Like, Comment)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable {
                        scope.launch {
                            communityRepository.toggleLike(post.id, currentUserId)
                        }
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Curtir",
                        tint = if (isLiked) Slate900 else Slate600,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.curtidasCount}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isLiked) Slate900 else Slate600
                    )
                }

                Row(
                    modifier = Modifier.clickable(onClick = onCommentClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comentar",
                        tint = Slate600,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.comentariosCount} comentários",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate600
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    postId: String,
    currentUserId: String,
    authRepository: AuthRepository,
    communityRepository: CommunityRepository,
    onDismiss: () -> Unit
) {
    val comments by communityRepository.getComentarios(postId).collectAsStateWithLifecycle(initialValue = emptyList())
    val currentUser by authRepository.getCurrentUserFlow(currentUserId).collectAsStateWithLifecycle(initialValue = null)
    var commentText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Comentários Técnicos",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate900
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (comments.isEmpty()) {
                Text(
                    text = "Ainda não há comentários. Deixe sua consideração técnica.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(comments, key = { it.id }) { com ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate50, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = com.autorNome,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = com.conteudo,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Escreva um comentário técnico...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            val user = currentUser
                            scope.launch {
                                communityRepository.addComentario(
                                    postId = postId,
                                    autorId = currentUserId,
                                    autorNome = user?.nome ?: "Profissional",
                                    autorFoto = user?.foto,
                                    conteudo = commentText
                                )
                                commentText = ""
                            }
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Slate900,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}
