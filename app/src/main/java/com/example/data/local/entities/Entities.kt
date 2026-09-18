package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "usuarios")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val email: String,
    val senhaHash: String,
    val foto: String? = null,
    val bio: String? = null,
    val localizacao: String? = null,
    val profissao: String = "Engenheiro Civil",
    val dataCriacao: Long = System.currentTimeMillis()
)

@Entity(tableName = "perfis_profissionais")
data class PerfilProfissionalEntity(
    @PrimaryKey val usuarioId: String,
    val profissao: String,
    val especialidades: String, // Comma-separated
    val experienciaAnos: Int,
    val localizacao: String,
    val descricao: String,
    val contactos: String,
    val portfolio: String? = null
)

@Entity(tableName = "equipes")
data class EquipeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val descricao: String,
    val liderId: String,
    val membrosNomes: String,
    val especialidades: String,
    val localizacao: String,
    val obrasAssociadasCount: Int = 0
)

@Entity(tableName = "obras")
data class ObraEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val proprietarioId: String,
    val nome: String,
    val descricao: String,
    val cliente: String,
    val localizacao: String,
    val tipo: String, // Residencial, Comercial, Reforma, Industrial, Infraestrutura
    val areaM2: Double,
    val dataInicial: Long,
    val previsao: Long,
    val progresso: Int = 0, // 0 to 100 calculated from stages/tasks
    val status: String = "EM_ANDAMENTO", // PLANEJAMENTO, EM_ANDAMENTO, PARALISADA, CONCLUIDA, ARQUIVADA
    val isPublica: Boolean = false,
    val capaFotoUri: String? = null
)

@Entity(tableName = "etapas")
data class EtapaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val obraId: String,
    val nome: String,
    val descricao: String = "",
    val progresso: Int = 0, // 0 to 100
    val status: String = "PENDENTE", // PENDENTE, EM_ANDAMENTO, CONCLUIDA
    val ordem: Int = 0
)

@Entity(tableName = "tarefas")
data class TarefaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val etapaId: String,
    val obraId: String,
    val titulo: String,
    val descricao: String = "",
    val prioridade: String = "MEDIA", // BAIXA, MEDIA, ALTA, URGENTE
    val prazo: Long = 0L,
    val status: String = "PENDENTE", // PENDENTE, EM_ANDAMENTO, CONCLUIDA
    val responsavel: String = ""
)

@Entity(tableName = "medicoes")
data class MedicaoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val obraId: String,
    val tipo: String, // Comprimento, Área, Volume, Quantidade
    val comprimento: Double = 0.0,
    val largura: Double = 0.0,
    val altura: Double = 0.0,
    val unidade: String = "m²",
    val resultado: Double = 0.0,
    val data: Long = System.currentTimeMillis()
)

@Entity(tableName = "diarios")
data class DiarioEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val obraId: String,
    val data: Long = System.currentTimeMillis(),
    val atividades: String,
    val equipe: String,
    val observacoes: String = "",
    val problemas: String = "",
    val condicoesTempo: String = "Ensolarado",
    val fotosUris: String = "", // Comma-separated URIs
    val progressoObservado: Int = 0
)

@Entity(tableName = "projetos_docs")
data class ProjetoDocEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val obraId: String,
    val nome: String,
    val arquivoUri: String,
    val tipo: String = "Planta Baixa", // Planta Baixa, Estrutural, Elétrica, Hidráulica, Alvará, Memorial
    val data: Long = System.currentTimeMillis()
)

@Entity(tableName = "checklists")
data class ChecklistEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val obraId: String,
    val titulo: String,
    val dataCriacao: Long = System.currentTimeMillis()
)

@Entity(tableName = "checklist_itens")
data class ChecklistItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val checklistId: String,
    val texto: String,
    val concluido: Boolean = false
)

@Entity(tableName = "problemas")
data class ProblemaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val obraId: String,
    val titulo: String,
    val descricao: String,
    val prioridade: String = "MEDIA", // BAIXA, MEDIA, ALTA, CRITICA
    val status: String = "ABERTO", // ABERTO, EM_ANALISE, RESOLVIDO
    val etapaRelacionada: String = "",
    val fotosUris: String = "",
    val data: Long = System.currentTimeMillis()
)

@Entity(tableName = "publicacoes")
data class PublicacaoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val autorId: String,
    val autorNome: String,
    val autorProfissao: String,
    val autorFoto: String? = null,
    val texto: String,
    val tipo: String, // TEXTO, FOTO, VIDEO, EVOLUCAO, ANTES_DEPOIS, OBRA
    val mediaUri: String? = null,
    val mediaSecundariaUri: String? = null, // Used for antes/depois
    val obraId: String? = null,
    val obraNome: String? = null,
    val curtidasCount: Int = 0,
    val comentariosCount: Int = 0,
    val data: Long = System.currentTimeMillis()
)

@Entity(tableName = "comentarios")
data class ComentarioEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val publicacaoId: String,
    val autorId: String,
    val autorNome: String,
    val autorFoto: String? = null,
    val conteudo: String,
    val data: Long = System.currentTimeMillis()
)

@Entity(tableName = "curtidas", primaryKeys = ["publicacaoId", "usuarioId"])
data class CurtidaEntity(
    val publicacaoId: String,
    val usuarioId: String
)

@Entity(tableName = "seguidores", primaryKeys = ["seguidorId", "seguidoId"])
data class SeguidorEntity(
    val seguidorId: String,
    val seguidoId: String
)

@Entity(tableName = "conversas")
data class ConversaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val participante1Id: String,
    val participante2Id: String,
    val participanteNome: String,
    val participanteProfissao: String,
    val participanteFoto: String? = null,
    val ultimaMensagem: String = "",
    val ultimaData: Long = System.currentTimeMillis(),
    val naoLidasCount: Int = 0
)

@Entity(tableName = "mensagens")
data class MensagemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversaId: String,
    val remetenteId: String,
    val conteudo: String,
    val data: Long = System.currentTimeMillis(),
    val lida: Boolean = false
)

@Entity(tableName = "notificacoes")
data class NotificacaoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val usuarioId: String,
    val tipo: String, // SEGUIDOR, CURTIDA, COMENTARIO, MENSAGEM, EQUIPE, OBRA
    val referenciaId: String? = null,
    val titulo: String,
    val mensagem: String,
    val lida: Boolean = false,
    val data: Long = System.currentTimeMillis()
)

@Entity(tableName = "calculos_salvos")
data class CalculoSalvoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val usuarioId: String,
    val obraId: String? = null,
    val titulo: String,
    val tipoCalculadora: String,
    val detalhes: String,
    val resultado: String,
    val data: Long = System.currentTimeMillis()
)
