package com.example.ui.screens.perfil

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
import com.example.data.local.entities.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.CommunityRepository
import com.example.data.repository.ObraRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Obra360TopBar
import com.example.ui.components.RealProgressBar
import com.example.ui.screens.home.StatMiniItem
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(
    userId: String,
    authRepository: AuthRepository,
    obraRepository: ObraRepository,
    communityRepository: CommunityRepository,
    onNavigateToCalculators: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToObraDetail: (String) -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by authRepository.getCurrentUserFlow(userId).collectAsStateWithLifecycle(initialValue = null)
    val userObras by obraRepository.getObrasByUser(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val userPosts by communityRepository.getPostsByAuthor(userId).collectAsStateWithLifecycle(initialValue = emptyList())

    var showEditProfileDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val totalObras = userObras.size
    val concluidas = userObras.count { it.status == "CONCLUIDA" }

    Scaffold(
        topBar = {
            Obra360TopBar(
                title = "Perfil Profissional",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Slate800),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Engineering, contentDescription = null, tint = PureWhite, modifier = Modifier.size(34.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.nome ?: "Profissional",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite
                                )
                                Text(
                                    text = currentUser?.profissao ?: "Engenheiro Civil",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                                Text(
                                    text = currentUser?.localizacao ?: "Brasil",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMiniItem(label = "Total Obras", value = "$totalObras")
                            StatMiniItem(label = "Concluídas", value = "$concluidas")
                            StatMiniItem(label = "Publicações", value = "${userPosts.size}")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showEditProfileDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = PureWhite),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Editar Perfil Profissional")
                        }
                    }
                }
            }

            // Public Portfolio Section
            item {
                Text(
                    text = "Portfólio de Obras Públicas",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
            }

            val publicObras = userObras.filter { it.isPublica }
            if (publicObras.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Nenhuma obra configurada como pública no momento.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(publicObras, key = { it.id }) { obra ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToObraDetail(obra.id) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(obra.nome, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Slate900)
                                Text("${obra.areaM2} m²", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            RealProgressBar(progress = obra.progresso)
                        }
                    }
                }
            }

            // System actions (Logout)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        authRepository.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = AlertOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sair da Conta (Logout)", color = AlertOrange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showEditProfileDialog && currentUser != null) {
        val user = currentUser!!
        var nome by remember { mutableStateOf(user.nome) }
        var profissao by remember { mutableStateOf(user.profissao) }
        var localizacao by remember { mutableStateOf(user.localizacao ?: "") }
        var especialidades by remember { mutableStateOf("Estruturas, Gerenciamento") }
        var anos by remember { mutableStateOf("5") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Editar Perfil") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = profissao, onValueChange = { profissao = it }, label = { Text("Profissão") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = localizacao, onValueChange = { localizacao = it }, label = { Text("Localização") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = especialidades, onValueChange = { especialidades = it }, label = { Text("Especialidades") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = anos, onValueChange = { anos = it }, label = { Text("Anos de experiência") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            authRepository.updateProfile(
                                user = user.copy(nome = nome, profissao = profissao, localizacao = localizacao),
                                especialidades = especialidades,
                                experienciaAnos = anos.toIntOrNull() ?: 3,
                                descricao = "Profissional cadastrado no OBRA360.",
                                contactos = user.email
                            )
                            showEditProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Text("Salvar Alterações")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
