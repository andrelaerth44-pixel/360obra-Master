package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(usuario: UserEntity)

    @Update
    suspend fun updateUsuario(usuario: UserEntity)

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getUsuarioById(id: String): UserEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observeUsuarioById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getUsuarioByEmail(email: String): UserEntity?

    @Query("SELECT * FROM usuarios WHERE id != :currentUserId ORDER BY nome ASC")
    fun getAllOtherUsers(currentUserId: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerfilProfissional(perfil: PerfilProfissionalEntity)

    @Query("SELECT * FROM perfis_profissionais WHERE usuarioId = :usuarioId")
    suspend fun getPerfilByUsuarioId(usuarioId: String): PerfilProfissionalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipe(equipe: EquipeEntity)

    @Query("SELECT * FROM equipes ORDER BY nome ASC")
    fun getAllEquipes(): Flow<List<EquipeEntity>>
}

@Dao
interface ObraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObra(obra: ObraEntity)

    @Update
    suspend fun updateObra(obra: ObraEntity)

    @Query("DELETE FROM obras WHERE id = :id")
    suspend fun deleteObraById(id: String)

    @Query("SELECT * FROM obras WHERE id = :id")
    fun getObraById(id: String): Flow<ObraEntity?>

    @Query("SELECT * FROM obras WHERE proprietarioId = :proprietarioId ORDER BY dataInicial DESC")
    fun getObrasByProprietario(proprietarioId: String): Flow<List<ObraEntity>>

    @Query("SELECT * FROM obras WHERE isPublica = 1 ORDER BY dataInicial DESC")
    fun getObrasPublicas(): Flow<List<ObraEntity>>

    @Query("SELECT * FROM obras WHERE proprietarioId = :proprietarioId AND isPublica = 1")
    fun getObrasPublicasByProprietario(proprietarioId: String): Flow<List<ObraEntity>>

    @Query("UPDATE obras SET progresso = :progresso WHERE id = :obraId")
    suspend fun updateProgresso(obraId: String, progresso: Int)

    @Query("UPDATE obras SET status = :status WHERE id = :obraId")
    suspend fun updateStatus(obraId: String, status: String)

    @Query("SELECT COUNT(*) FROM obras WHERE proprietarioId = :proprietarioId")
    fun countObrasByProprietario(proprietarioId: String): Flow<Int>
}

@Dao
interface EtapaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEtapa(etapa: EtapaEntity)

    @Update
    suspend fun updateEtapa(etapa: EtapaEntity)

    @Query("DELETE FROM etapas WHERE id = :id")
    suspend fun deleteEtapaById(id: String)

    @Query("SELECT * FROM etapas WHERE obraId = :obraId ORDER BY ordem ASC")
    fun getEtapasByObra(obraId: String): Flow<List<EtapaEntity>>

    @Query("SELECT * FROM etapas WHERE obraId = :obraId")
    suspend fun getEtapasByObraList(obraId: String): List<EtapaEntity>
}

@Dao
interface TarefaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarefa(tarefa: TarefaEntity)

    @Update
    suspend fun updateTarefa(tarefa: TarefaEntity)

    @Query("DELETE FROM tarefas WHERE id = :id")
    suspend fun deleteTarefaById(id: String)

    @Query("SELECT * FROM tarefas WHERE etapaId = :etapaId")
    fun getTarefasByEtapa(etapaId: String): Flow<List<TarefaEntity>>

    @Query("SELECT * FROM tarefas WHERE obraId = :obraId ORDER BY prazo ASC")
    fun getTarefasByObra(obraId: String): Flow<List<TarefaEntity>>

    @Query("SELECT * FROM tarefas WHERE obraId = :obraId")
    suspend fun getTarefasByObraList(obraId: String): List<TarefaEntity>

    @Query("UPDATE tarefas SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}

@Dao
interface MedicaoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicao(medicao: MedicaoEntity)

    @Query("DELETE FROM medicoes WHERE id = :id")
    suspend fun deleteMedicaoById(id: String)

    @Query("SELECT * FROM medicoes WHERE obraId = :obraId ORDER BY data DESC")
    fun getMedicoesByObra(obraId: String): Flow<List<MedicaoEntity>>
}

@Dao
interface DiarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiario(diario: DiarioEntity)

    @Query("DELETE FROM diarios WHERE id = :id")
    suspend fun deleteDiarioById(id: String)

    @Query("SELECT * FROM diarios WHERE obraId = :obraId ORDER BY data DESC")
    fun getDiariosByObra(obraId: String): Flow<List<DiarioEntity>>

    @Query("SELECT * FROM diarios ORDER BY data DESC LIMIT 5")
    fun getRecentDiarios(): Flow<List<DiarioEntity>>
}

@Dao
interface ProjetoDocDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoc(doc: ProjetoDocEntity)

    @Query("DELETE FROM projetos_docs WHERE id = :id")
    suspend fun deleteDocById(id: String)

    @Query("SELECT * FROM projetos_docs WHERE obraId = :obraId ORDER BY data DESC")
    fun getDocsByObra(obraId: String): Flow<List<ProjetoDocEntity>>
}

@Dao
interface ChecklistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(checklist: ChecklistEntity)

    @Query("DELETE FROM checklists WHERE id = :id")
    suspend fun deleteChecklistById(id: String)

    @Query("SELECT * FROM checklists WHERE obraId = :obraId ORDER BY dataCriacao DESC")
    fun getChecklistsByObra(obraId: String): Flow<List<ChecklistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(item: ChecklistItemEntity)

    @Update
    suspend fun updateChecklistItem(item: ChecklistItemEntity)

    @Query("DELETE FROM checklist_itens WHERE id = :id")
    suspend fun deleteChecklistItemById(id: String)

    @Query("SELECT * FROM checklist_itens WHERE checklistId = :checklistId")
    fun getItensByChecklist(checklistId: String): Flow<List<ChecklistItemEntity>>
}

@Dao
interface ProblemaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblema(problema: ProblemaEntity)

    @Update
    suspend fun updateProblema(problema: ProblemaEntity)

    @Query("DELETE FROM problemas WHERE id = :id")
    suspend fun deleteProblemaById(id: String)

    @Query("SELECT * FROM problemas WHERE obraId = :obraId ORDER BY data DESC")
    fun getProblemasByObra(obraId: String): Flow<List<ProblemaEntity>>

    @Query("UPDATE problemas SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("SELECT * FROM problemas WHERE status != 'RESOLVIDO' ORDER BY data DESC LIMIT 5")
    fun getProblemasPendentes(): Flow<List<ProblemaEntity>>
}

@Dao
interface PublicacaoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPublicacao(publicacao: PublicacaoEntity)

    @Query("DELETE FROM publicacoes WHERE id = :id")
    suspend fun deletePublicacaoById(id: String)

    @Query("SELECT * FROM publicacoes ORDER BY data DESC")
    fun getFeed(): Flow<List<PublicacaoEntity>>

    @Query("SELECT * FROM publicacoes WHERE autorId = :autorId ORDER BY data DESC")
    fun getPublicacoesByAutor(autorId: String): Flow<List<PublicacaoEntity>>

    @Query("SELECT * FROM publicacoes WHERE id = :id")
    fun getPublicacaoById(id: String): Flow<PublicacaoEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCurtida(curtida: CurtidaEntity)

    @Query("DELETE FROM curtidas WHERE publicacaoId = :postId AND usuarioId = :usuarioId")
    suspend fun deleteCurtida(postId: String, usuarioId: String)

    @Query("SELECT COUNT(*) > 0 FROM curtidas WHERE publicacaoId = :postId AND usuarioId = :usuarioId")
    fun isLikedByUser(postId: String, usuarioId: String): Flow<Boolean>

    @Query("UPDATE publicacoes SET curtidasCount = curtidasCount + 1 WHERE id = :postId")
    suspend fun incrementLikes(postId: String)

    @Query("UPDATE publicacoes SET curtidasCount = MAX(0, curtidasCount - 1) WHERE id = :postId")
    suspend fun decrementLikes(postId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComentario(comentario: ComentarioEntity)

    @Query("SELECT * FROM comentarios WHERE publicacaoId = :postId ORDER BY data ASC")
    fun getComentarios(postId: String): Flow<List<ComentarioEntity>>

    @Query("UPDATE publicacoes SET comentariosCount = comentariosCount + 1 WHERE id = :postId")
    suspend fun incrementComments(postId: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM conversas WHERE participante1Id = :userId OR participante2Id = :userId ORDER BY ultimaData DESC")
    fun getConversasForUser(userId: String): Flow<List<ConversaEntity>>

    @Query("SELECT * FROM conversas WHERE id = :conversaId")
    fun getConversaById(conversaId: String): Flow<ConversaEntity?>

    @Query("SELECT * FROM conversas WHERE (participante1Id = :u1 AND participante2Id = :u2) OR (participante1Id = :u2 AND participante2Id = :u1) LIMIT 1")
    suspend fun findExistingConversa(u1: String, u2: String): ConversaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversa(conversa: ConversaEntity)

    @Query("UPDATE conversas SET ultimaMensagem = :msg, ultimaData = :timestamp WHERE id = :conversaId")
    suspend fun updateLastMessage(conversaId: String, msg: String, timestamp: Long)

    @Query("SELECT * FROM mensagens WHERE conversaId = :conversaId ORDER BY data ASC")
    fun getMensagens(conversaId: String): Flow<List<MensagemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMensagem(mensagem: MensagemEntity)

    @Query("UPDATE mensagens SET lida = 1 WHERE conversaId = :conversaId AND remetenteId != :currentUserId")
    suspend fun markMessagesAsRead(conversaId: String, currentUserId: String)
}

@Dao
interface NotificacaoDao {
    @Query("SELECT * FROM notificacoes WHERE usuarioId = :userId ORDER BY data DESC")
    fun getNotificacoesForUser(userId: String): Flow<List<NotificacaoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificacao(notificacao: NotificacaoEntity)

    @Query("UPDATE notificacoes SET lida = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
}

@Dao
interface CalculoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculo(calculo: CalculoSalvoEntity)

    @Query("SELECT * FROM calculos_salvos WHERE usuarioId = :userId ORDER BY data DESC")
    fun getCalculosByUser(userId: String): Flow<List<CalculoSalvoEntity>>

    @Query("DELETE FROM calculos_salvos WHERE id = :id")
    suspend fun deleteCalculoById(id: String)
}
