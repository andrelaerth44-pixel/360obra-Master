package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.local.Obra360Database
import com.example.data.local.SessionManager
import com.example.data.repository.*
import com.example.ui.navigation.MainAppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate50

class MainActivity : ComponentActivity() {

    private lateinit var database: Obra360Database
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var obraRepository: ObraRepository
    private lateinit var communityRepository: CommunityRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var calculadorasRepository: CalculadorasRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = Obra360Database.getInstance(applicationContext)
        sessionManager = SessionManager(applicationContext)
        authRepository = AuthRepository(database, sessionManager)
        obraRepository = ObraRepository(database)
        communityRepository = CommunityRepository(database)
        chatRepository = ChatRepository(database)
        calculadorasRepository = CalculadorasRepository(database)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Slate50
                ) {
                    MainAppNavigation(
                        authRepository = authRepository,
                        obraRepository = obraRepository,
                        communityRepository = communityRepository,
                        chatRepository = chatRepository,
                        calculadorasRepository = calculadorasRepository
                    )
                }
            }
        }
    }
}
