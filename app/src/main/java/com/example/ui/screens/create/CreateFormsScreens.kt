package com.example.ui.screens.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.repository.AuthRepository
import com.example.data.repository.CommunityRepository
import com.example.data.repository.ObraRepository
import com.example.ui.components.Obra360TopBar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateObraScreen(
    userId: String,
    obraRepository: ObraRepository,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var cliente by remember { mutableStateOf("") }
    var localizacao by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Residencial") }
    var areaM2Str by remember { mutableStateOf("") }
    var duracaoMesesStr by remember { mutableStateOf("6") }
    var descricao by remember { mutableStateOf("") }
    var isPublica by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val tiposObra = listOf("Residencial", "Comercial", "Reforma", "Industrial", "Infraestrutura")

    Scaffold(
        topBar = {
            Obra360TopBar(
                title = "Cadastrar Nova Obra",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Identificação do Projeto",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )

                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it; errorMessage = null },
                        label = { Text("Nome da Obra (ex: Residência Alphaville)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = cliente,
                        onValueChange = { cliente = it },
                        label = { Text("Cliente / Contratante") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = localizacao,
                        onValueChange = { localizacao = it },
                        label = { Text("Localização / Endereço da Obra") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text(
                        text = "Tipo de Empreendimento:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Slate800
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tiposObra.take(3).forEach { t ->
                            FilterChip(
                                selected = (tipo == t),
                                onClick = { tipo = t },
                                label = { Text(t, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Slate900,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tiposObra.drop(3).forEach { t ->
                            FilterChip(
                                selected = (tipo == t),
                                onClick = { tipo = t },
                                label = { Text(t, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Slate900,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = areaM2Str,
                            onValueChange = { areaM2Str = it },
                            label = { Text("Área total (m²)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = duracaoMesesStr,
                            onValueChange = { duracaoMesesStr = it },
                            label = { Text("Prazo (meses)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        label = { Text("Descrição / Escopo dos trabalhos") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isPublica,
                            onCheckedChange = { isPublica = it },
                            colors = CheckboxDefaults.colors(checkedColor = Slate900)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Exibir no meu Portfólio Público",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Slate900
                            )
                            Text(
                                text = "Permite que outros profissionais e clientes vejam a evolução desta obra.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = AlertOrange,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (nome.isBlank()) {
                                errorMessage = "O nome da obra é obrigatório."
                                return@Button
                            }
                            val area = areaM2Str.toDoubleOrNull() ?: 100.0
                            val meses = duracaoMesesStr.toIntOrNull() ?: 6
                            val inicio = System.currentTimeMillis()
                            val previsao = inicio + (meses * 30L * 24L * 60L * 60L * 1000L)

                            isSaving = true
                            scope.launch {
                                val newId = obraRepository.createObra(
                                    ownerId = userId,
                                    nome = nome,
                                    descricao = descricao,
                                    cliente = cliente,
                                    localizacao = localizacao,
                                    tipo = tipo,
                                    areaM2 = area,
                                    dataInicial = inicio,
                                    previsao = previsao,
                                    isPublica = isPublica
                                )
                                isSaving = false
                                onSuccess(newId)
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(22.dp))
                        } else {
                            Text("Salvar e Criar Obra", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    userId: String,
    authRepository: AuthRepository,
    obraRepository: ObraRepository,
    communityRepository: CommunityRepository,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val currentUser by authRepository.getCurrentUserFlow(userId).collectAsStateWithLifecycle(initialValue = null)
    val userObras by obraRepository.getObrasByUser(userId).collectAsStateWithLifecycle(initialValue = emptyList())

    var texto by remember { mutableStateOf("") }
    var tipoPost by remember { mutableStateOf("EVOLUCAO") }
    var selectedObraId by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Obra360TopBar(
                title = "Nova Publicação",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tipo de Publicação Técnica",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = (tipoPost == "EVOLUCAO"),
                            onClick = { tipoPost = "EVOLUCAO" },
                            label = { Text("Evolução de Obra") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Slate900,
                                selectedLabelColor = PureWhite
                            )
                        )
                        FilterChip(
                            selected = (tipoPost == "FOTO"),
                            onClick = { tipoPost = "FOTO" },
                            label = { Text("Foto Técnica") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Slate900,
                                selectedLabelColor = PureWhite
                            )
                        )
                        FilterChip(
                            selected = (tipoPost == "TEXTO"),
                            onClick = { tipoPost = "TEXTO" },
                            label = { Text("Artigo") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Slate900,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }

                    OutlinedTextField(
                        value = texto,
                        onValueChange = { texto = it; errorMessage = null },
                        placeholder = { Text("Descreva a execução, detalhes dos materiais ou desafios de engenharia superados...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Photo selector button (zero-permission Android Photo Picker)
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = Slate800)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedImageUri != null) "Trocar Foto Selecionada" else "Adicionar Foto da Obra",
                            color = Slate900,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (selectedImageUri != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Foto selecionada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Optional: Link to an existing work
                    if (userObras.isNotEmpty()) {
                        Text(
                            text = "Vincular a uma Obra Minha (Opcional):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Slate800
                        )
                        userObras.forEach { obra ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedObraId = if (selectedObraId == obra.id) null else obra.id
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (selectedObraId == obra.id),
                                    onClick = {
                                        selectedObraId = if (selectedObraId == obra.id) null else obra.id
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Slate900)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(obra.nome, style = MaterialTheme.typography.bodyMedium, color = Slate800)
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = AlertOrange,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (texto.isBlank() && selectedImageUri == null) {
                                errorMessage = "Escreva uma mensagem técnica ou selecione uma imagem."
                                return@Button
                            }
                            val author = currentUser
                            val linkedObra = userObras.firstOrNull { it.id == selectedObraId }
                            isSubmitting = true
                            scope.launch {
                                communityRepository.createPost(
                                    autorId = userId,
                                    autorNome = author?.nome ?: "Profissional",
                                    autorProfissao = author?.profissao ?: "Engenheiro Civil",
                                    autorFoto = author?.foto,
                                    texto = texto,
                                    tipo = tipoPost,
                                    mediaUri = selectedImageUri?.toString(),
                                    obraId = linkedObra?.id,
                                    obraNome = linkedObra?.nome
                                )
                                isSubmitting = false
                                onSuccess()
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(22.dp))
                        } else {
                            Text("Publicar no Feed Real", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
