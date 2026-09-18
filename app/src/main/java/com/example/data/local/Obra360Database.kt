package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        PerfilProfissionalEntity::class,
        EquipeEntity::class,
        ObraEntity::class,
        EtapaEntity::class,
        TarefaEntity::class,
        MedicaoEntity::class,
        DiarioEntity::class,
        ProjetoDocEntity::class,
        ChecklistEntity::class,
        ChecklistItemEntity::class,
        ProblemaEntity::class,
        PublicacaoEntity::class,
        ComentarioEntity::class,
        CurtidaEntity::class,
        SeguidorEntity::class,
        ConversaEntity::class,
        MensagemEntity::class,
        NotificacaoEntity::class,
        CalculoSalvoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class Obra360Database : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun obraDao(): ObraDao
    abstract fun etapaDao(): EtapaDao
    abstract fun tarefaDao(): TarefaDao
    abstract fun medicaoDao(): MedicaoDao
    abstract fun diarioDao(): DiarioDao
    abstract fun projetoDocDao(): ProjetoDocDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun problemaDao(): ProblemaDao
    abstract fun publicacaoDao(): PublicacaoDao
    abstract fun chatDao(): ChatDao
    abstract fun notificacaoDao(): NotificacaoDao
    abstract fun calculoDao(): CalculoDao

    companion object {
        @Volatile
        private var INSTANCE: Obra360Database? = null

        fun getInstance(context: Context): Obra360Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Obra360Database::class.java,
                    "obra360_database.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
