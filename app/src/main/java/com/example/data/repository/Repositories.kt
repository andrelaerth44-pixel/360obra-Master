package com.example.data.repository

import com.example.data.local.Obra360Database
import com.example.data.local.SessionManager
import com.example.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class AuthRepository(
    private val db: Obra360Database,
    private val sessionManager: SessionManager
) {
    private val usuarioDao = db.usuarioDao()

    val currentUserId: Flow<String?> = sessionManager.currentUserId

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun register(
        nome: String,
        email: String,
        senha: String,
        profissao: String,
        localizacao: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        if (nome.isBlank() || email.isBlank() || senha.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Preencha todos os campos. A senha deve ter no mínimo 6 caracteres."))
        }
        val existing = usuarioDao.getUsuarioByEmail(email.trim().lowercase())
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("Este e-mail já está cadastrado."))
        }

        val newUser = UserEntity(
            id = UUID.randomUUID().toString(),
            nome = nome.trim(),
            email = email.trim().lowercase(),
            senhaHash = hashPassword(senha),
            profissao = profissao.ifBlank { "Engenheiro Civil" },
            localizacao = localizacao.trim()
        )
        usuarioDao.insertUsuario(newUser)

        // Create initial professional profile
        val perfil = PerfilProfissionalEntity(
            usuarioId = newUser.id,
            profissao = newUser.profissao,
            especialidades = "Gerenciamento de Obras, Estruturas",
            experienciaAnos = 3,
            localizacao = newUser.localizacao ?: "Brasil",
            descricao = "Profissional cadastrado no OBRA360.",
            contactos = newUser.email
        )
        usuarioDao.insertPerfilProfissional(perfil)

        sessionManager.saveUserSession(newUser.id, newUser.email, newUser.nome, newUser.profissao)
        Result.success(newUser)
    }

    suspend fun login(email: String, senha: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = usuarioDao.getUsuarioByEmail(email.trim().lowercase())
            ?: return@withContext Result.failure(IllegalArgumentException("Usuário ou senha incorretos."))

        val hash = hashPassword(senha)
        if (user.senhaHash != hash) {
            return@withContext Result.failure(IllegalArgumentException("Usuário ou senha incorretos."))
        }

        sessionManager.saveUserSession(user.id, user.email, user.nome, user.profissao)
        Result.success(user)
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun getCurrentUserFlow(userId: String): Flow<UserEntity?> {
        return usuarioDao.observeUsuarioById(userId)
    }

    suspend fun getCurrentUserDirect(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        usuarioDao.getUsuarioById(userId)
    }

    suspend fun updateProfile(
        user: UserEntity,
        especialidades: String,
        experienciaAnos: Int,
        descricao: String,
        contactos: String
    ) = withContext(Dispatchers.IO) {
        usuarioDao.updateUsuario(user)
        val perfil = PerfilProfissionalEntity(
            usuarioId = user.id,
            profissao = user.profissao,
            especialidades = especialidades,
            experienciaAnos = experienciaAnos,
            localizacao = user.localizacao ?: "",
            descricao = descricao,
            contactos = contactos
        )
        usuarioDao.insertPerfilProfissional(perfil)
        sessionManager.saveUserSession(user.id, user.email, user.nome, user.profissao)
    }

    fun getAllOtherUsers(currentUserId: String): Flow<List<UserEntity>> {
        return usuarioDao.getAllOtherUsers(currentUserId)
    }
}

class ObraRepository(private val db: Obra360Database) {
    private val obraDao = db.obraDao()
    private val etapaDao = db.etapaDao()
    private val tarefaDao = db.tarefaDao()
    private val medicaoDao = db.medicaoDao()
    private val diarioDao = db.diarioDao()
    private val docDao = db.projetoDocDao()
    private val checklistDao = db.checklistDao()
    private val problemaDao = db.problemaDao()

    fun getObrasByUser(userId: String): Flow<List<ObraEntity>> = obraDao.getObrasByProprietario(userId)
    fun getPublicObras(): Flow<List<ObraEntity>> = obraDao.getObrasPublicas()
    fun getPublicObrasByUser(userId: String): Flow<List<ObraEntity>> = obraDao.getObrasPublicasByProprietario(userId)
    fun getObraById(obraId: String): Flow<ObraEntity?> = obraDao.getObraById(obraId)

    suspend fun createObra(
        ownerId: String,
        nome: String,
        descricao: String,
        cliente: String,
        localizacao: String,
        tipo: String,
        areaM2: Double,
        dataInicial: Long,
        previsao: Long,
        isPublica: Boolean,
        capaFotoUri: String? = null
    ): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val obra = ObraEntity(
            id = id,
            proprietarioId = ownerId,
            nome = nome.trim(),
            descricao = descricao.trim(),
            cliente = cliente.trim(),
            localizacao = localizacao.trim(),
            tipo = tipo,
            areaM2 = areaM2,
            dataInicial = dataInicial,
            previsao = previsao,
            progresso = 0,
            status = "EM_ANDAMENTO",
            isPublica = isPublica,
            capaFotoUri = capaFotoUri
        )
        obraDao.insertObra(obra)

        // Create standard civil engineering initial stages:
        val defaultStages = listOf(
            "Fundação e Sondagem",
            "Estrutura e Alvenaria",
            "Cobertura e Instalações",
            "Reboco e Acabamentos"
        )
        defaultStages.forEachIndexed { index, stageName ->
            etapaDao.insertEtapa(
                EtapaEntity(
                    obraId = id,
                    nome = stageName,
                    descricao = "Etapa técnica da obra",
                    progresso = 0,
                    status = if (index == 0) "EM_ANDAMENTO" else "PENDENTE",
                    ordem = index
                )
            )
        }
        id
    }

    suspend fun updateObra(obra: ObraEntity) = withContext(Dispatchers.IO) {
        obraDao.updateObra(obra)
    }

    suspend fun deleteObra(id: String) = withContext(Dispatchers.IO) {
        obraDao.deleteObraById(id)
    }

    fun getEtapas(obraId: String): Flow<List<EtapaEntity>> = etapaDao.getEtapasByObra(obraId)

    suspend fun addEtapa(obraId: String, nome: String, descricao: String, ordem: Int) = withContext(Dispatchers.IO) {
        val etapa = EtapaEntity(
            obraId = obraId,
            nome = nome.trim(),
            descricao = descricao.trim(),
            progresso = 0,
            status = "PENDENTE",
            ordem = ordem
        )
        etapaDao.insertEtapa(etapa)
    }

    fun getTarefas(obraId: String): Flow<List<TarefaEntity>> = tarefaDao.getTarefasByObra(obraId)

    suspend fun addTarefa(
        etapaId: String,
        obraId: String,
        titulo: String,
        descricao: String,
        prioridade: String,
        prazo: Long,
        responsavel: String
    ) = withContext(Dispatchers.IO) {
        val tarefa = TarefaEntity(
            etapaId = etapaId,
            obraId = obraId,
            titulo = titulo.trim(),
            descricao = descricao.trim(),
            prioridade = prioridade,
            prazo = prazo,
            status = "PENDENTE",
            responsavel = responsavel.trim()
        )
        tarefaDao.insertTarefa(tarefa)
        recalculateProgress(obraId)
    }

    suspend fun updateTarefaStatus(tarefaId: String, newStatus: String, obraId: String) = withContext(Dispatchers.IO) {
        tarefaDao.updateStatus(tarefaId, newStatus)
        recalculateProgress(obraId)
    }

    suspend fun recalculateProgress(obraId: String) = withContext(Dispatchers.IO) {
        val tarefas = tarefaDao.getTarefasByObraList(obraId)
        if (tarefas.isNotEmpty()) {
            val completed = tarefas.count { it.status == "CONCLUIDA" }
            val pct = ((completed.toDouble() / tarefas.size.toDouble()) * 100.0).toInt()
            obraDao.updateProgresso(obraId, pct)
            if (pct == 100) {
                obraDao.updateStatus(obraId, "CONCLUIDA")
            }
        }
    }

    fun getMedicoes(obraId: String): Flow<List<MedicaoEntity>> = medicaoDao.getMedicoesByObra(obraId)
    suspend fun addMedicao(medicao: MedicaoEntity) = withContext(Dispatchers.IO) {
        medicaoDao.insertMedicao(medicao)
    }
    suspend fun deleteMedicao(id: String) = withContext(Dispatchers.IO) {
        medicaoDao.deleteMedicaoById(id)
    }

    fun getDiarios(obraId: String): Flow<List<DiarioEntity>> = diarioDao.getDiariosByObra(obraId)
    fun getRecentDiarios(): Flow<List<DiarioEntity>> = diarioDao.getRecentDiarios()
    suspend fun addDiario(diario: DiarioEntity) = withContext(Dispatchers.IO) {
        diarioDao.insertDiario(diario)
    }

    fun getDocs(obraId: String): Flow<List<ProjetoDocEntity>> = docDao.getDocsByObra(obraId)
    suspend fun addDoc(doc: ProjetoDocEntity) = withContext(Dispatchers.IO) {
        docDao.insertDoc(doc)
    }

    fun getChecklists(obraId: String): Flow<List<ChecklistEntity>> = checklistDao.getChecklistsByObra(obraId)
    fun getChecklistItens(checklistId: String): Flow<List<ChecklistItemEntity>> = checklistDao.getItensByChecklist(checklistId)
    suspend fun addChecklist(obraId: String, titulo: String, initialItems: List<String>) = withContext(Dispatchers.IO) {
        val checklistId = UUID.randomUUID().toString()
        checklistDao.insertChecklist(ChecklistEntity(id = checklistId, obraId = obraId, titulo = titulo))
        initialItems.forEach { text ->
            if (text.isNotBlank()) {
                checklistDao.insertChecklistItem(ChecklistItemEntity(checklistId = checklistId, texto = text.trim()))
            }
        }
    }
    suspend fun toggleChecklistItem(item: ChecklistItemEntity) = withContext(Dispatchers.IO) {
        checklistDao.updateChecklistItem(item.copy(concluido = !item.concluido))
    }

    fun getProblemas(obraId: String): Flow<List<ProblemaEntity>> = problemaDao.getProblemasByObra(obraId)
    fun getProblemasPendentes(): Flow<List<ProblemaEntity>> = problemaDao.getProblemasPendentes()
    suspend fun addProblema(problema: ProblemaEntity) = withContext(Dispatchers.IO) {
        problemaDao.insertProblema(problema)
    }
    suspend fun updateProblemaStatus(id: String, status: String) = withContext(Dispatchers.IO) {
        problemaDao.updateStatus(id, status)
    }
}

class CommunityRepository(private val db: Obra360Database) {
    private val publicacaoDao = db.publicacaoDao()
    private val usuarioDao = db.usuarioDao()

    fun getFeed(): Flow<List<PublicacaoEntity>> = publicacaoDao.getFeed()
    fun getPostsByAuthor(authorId: String): Flow<List<PublicacaoEntity>> = publicacaoDao.getPublicacoesByAutor(authorId)

    suspend fun createPost(
        autorId: String,
        autorNome: String,
        autorProfissao: String,
        autorFoto: String?,
        texto: String,
        tipo: String,
        mediaUri: String? = null,
        mediaSecundariaUri: String? = null,
        obraId: String? = null,
        obraNome: String? = null
    ): String = withContext(Dispatchers.IO) {
        val post = PublicacaoEntity(
            autorId = autorId,
            autorNome = autorNome,
            autorProfissao = autorProfissao,
            autorFoto = autorFoto,
            texto = texto.trim(),
            tipo = tipo,
            mediaUri = mediaUri,
            mediaSecundariaUri = mediaSecundariaUri,
            obraId = obraId,
            obraNome = obraNome
        )
        publicacaoDao.insertPublicacao(post)
        post.id
    }

    suspend fun toggleLike(postId: String, userId: String) = withContext(Dispatchers.IO) {
        val isLiked = publicacaoDao.isLikedByUser(postId, userId).firstOrNull() ?: false
        if (isLiked) {
            publicacaoDao.deleteCurtida(postId, userId)
            publicacaoDao.decrementLikes(postId)
        } else {
            publicacaoDao.insertCurtida(CurtidaEntity(postId, userId))
            publicacaoDao.incrementLikes(postId)
        }
    }

    fun isPostLikedByUser(postId: String, userId: String): Flow<Boolean> = publicacaoDao.isLikedByUser(postId, userId)

    fun getComentarios(postId: String): Flow<List<ComentarioEntity>> = publicacaoDao.getComentarios(postId)

    suspend fun addComentario(
        postId: String,
        autorId: String,
        autorNome: String,
        autorFoto: String?,
        conteudo: String
    ) = withContext(Dispatchers.IO) {
        val comentario = ComentarioEntity(
            publicacaoId = postId,
            autorId = autorId,
            autorNome = autorNome,
            autorFoto = autorFoto,
            conteudo = conteudo.trim()
        )
        publicacaoDao.insertComentario(comentario)
        publicacaoDao.incrementComments(postId)
    }

    fun getAllEquipes(): Flow<List<EquipeEntity>> = usuarioDao.getAllEquipes()
    suspend fun createEquipe(equipe: EquipeEntity) = withContext(Dispatchers.IO) {
        usuarioDao.insertEquipe(equipe)
    }
}

class ChatRepository(private val db: Obra360Database) {
    private val chatDao = db.chatDao()

    fun getConversas(userId: String): Flow<List<ConversaEntity>> = chatDao.getConversasForUser(userId)
    fun getConversaById(conversaId: String): Flow<ConversaEntity?> = chatDao.getConversaById(conversaId)
    fun getMensagens(conversaId: String): Flow<List<MensagemEntity>> = chatDao.getMensagens(conversaId)

    suspend fun getOrCreateConversa(
        currentUserId: String,
        contactUser: UserEntity
    ): String = withContext(Dispatchers.IO) {
        val existing = chatDao.findExistingConversa(currentUserId, contactUser.id)
        if (existing != null) {
            return@withContext existing.id
        }
        val newId = UUID.randomUUID().toString()
        val newConversa = ConversaEntity(
            id = newId,
            participante1Id = currentUserId,
            participante2Id = contactUser.id,
            participanteNome = contactUser.nome,
            participanteProfissao = contactUser.profissao,
            participanteFoto = contactUser.foto,
            ultimaMensagem = "Início da conversa",
            ultimaData = System.currentTimeMillis()
        )
        chatDao.insertConversa(newConversa)
        newId
    }

    suspend fun enviarMensagem(
        conversaId: String,
        senderId: String,
        conteudo: String
    ) = withContext(Dispatchers.IO) {
        val msg = MensagemEntity(
            conversaId = conversaId,
            remetenteId = senderId,
            conteudo = conteudo.trim(),
            data = System.currentTimeMillis()
        )
        chatDao.insertMensagem(msg)
        chatDao.updateLastMessage(conversaId, conteudo.trim(), msg.data)
    }
}

class CalculadorasRepository(private val db: Obra360Database) {
    private val calculoDao = db.calculoDao()

    fun getCalculosByUser(userId: String): Flow<List<CalculoSalvoEntity>> = calculoDao.getCalculosByUser(userId)

    suspend fun salvarCalculo(
        userId: String,
        titulo: String,
        tipo: String,
        detalhes: String,
        resultado: String,
        obraId: String? = null
    ) = withContext(Dispatchers.IO) {
        val calculo = CalculoSalvoEntity(
            usuarioId = userId,
            obraId = obraId,
            titulo = titulo,
            tipoCalculadora = tipo,
            detalhes = detalhes,
            resultado = resultado
        )
        calculoDao.insertCalculo(calculo)
    }

    suspend fun deletarCalculo(id: String) = withContext(Dispatchers.IO) {
        calculoDao.deleteCalculoById(id)
    }
}
