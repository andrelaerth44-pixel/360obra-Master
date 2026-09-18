package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.*
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.calculators.CalculadorasScreen
import com.example.ui.screens.chat.ChatDetailScreen
import com.example.ui.screens.chat.ConversasListScreen
import com.example.ui.screens.comunidade.ComunidadeScreen
import com.example.ui.screens.create.CreateHubScreen
import com.example.ui.screens.create.CreateObraScreen
import com.example.ui.screens.create.CreatePostScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.obras.ObraDetailScreen
import com.example.ui.screens.obras.ObrasListScreen
import com.example.ui.screens.perfil.PerfilScreen
import com.example.ui.theme.*

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Home : Screen("tab_home", "Início", Icons.Filled.Home, Icons.Outlined.Home)
    object Comunidade : Screen("tab_comunidade", "Comunidade", Icons.Filled.Groups, Icons.Outlined.Groups)
    object Criar : Screen("tab_criar", "Criar", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline)
    object Obras : Screen("tab_obras", "Obras", Icons.Filled.Apartment, Icons.Outlined.Apartment)
    object Perfil : Screen("tab_perfil", "Perfil", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun MainAppNavigation(
    authRepository: AuthRepository,
    obraRepository: ObraRepository,
    communityRepository: CommunityRepository,
    chatRepository: ChatRepository,
    calculadorasRepository: CalculadorasRepository
) {
    val currentUserId by authRepository.currentUserId.collectAsStateWithLifecycle(initialValue = null)
    val rootNavController = rememberNavController()

    val startDestination = if (currentUserId != null) "main_app" else "login"

    NavHost(
        navController = rootNavController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                authRepository = authRepository,
                onLoginSuccess = {
                    rootNavController.navigate("main_app") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    rootNavController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                authRepository = authRepository,
                onRegisterSuccess = {
                    rootNavController.navigate("main_app") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    rootNavController.popBackStack()
                }
            )
        }

        composable("main_app") {
            val userId = currentUserId
            if (userId == null) {
                LaunchedEffect(Unit) {
                    rootNavController.navigate("login") {
                        popUpTo("main_app") { inclusive = true }
                    }
                }
            } else {
                MainAppScaffold(
                    userId = userId,
                    authRepository = authRepository,
                    obraRepository = obraRepository,
                    communityRepository = communityRepository,
                    chatRepository = chatRepository,
                    calculadorasRepository = calculadorasRepository,
                    onNavigateToObraDetail = { obraId ->
                        rootNavController.navigate("obra_detail/$obraId")
                    },
                    onNavigateToCreateObra = {
                        rootNavController.navigate("create_obra")
                    },
                    onNavigateToCreatePost = {
                        rootNavController.navigate("create_post")
                    },
                    onNavigateToCalculators = {
                        rootNavController.navigate("calculadoras")
                    },
                    onNavigateToChat = {
                        rootNavController.navigate("conversas")
                    },
                    onLogout = {
                        rootNavController.navigate("login") {
                            popUpTo("main_app") { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(
            route = "obra_detail/{obraId}",
            arguments = listOf(navArgument("obraId") { type = NavType.StringType })
        ) { backStackEntry ->
            val obraId = backStackEntry.arguments?.getString("obraId") ?: ""
            ObraDetailScreen(
                obraId = obraId,
                obraRepository = obraRepository,
                onBack = { rootNavController.popBackStack() }
            )
        }

        composable("create_obra") {
            val userId = currentUserId ?: ""
            CreateObraScreen(
                userId = userId,
                obraRepository = obraRepository,
                onBack = { rootNavController.popBackStack() },
                onSuccess = { newObraId ->
                    rootNavController.navigate("obra_detail/$newObraId") {
                        popUpTo("create_obra") { inclusive = true }
                    }
                }
            )
        }

        composable("create_post") {
            val userId = currentUserId ?: ""
            CreatePostScreen(
                userId = userId,
                authRepository = authRepository,
                obraRepository = obraRepository,
                communityRepository = communityRepository,
                onBack = { rootNavController.popBackStack() },
                onSuccess = { rootNavController.popBackStack() }
            )
        }

        composable("calculadoras") {
            val userId = currentUserId ?: ""
            CalculadorasScreen(
                userId = userId,
                calculadorasRepository = calculadorasRepository,
                onBack = { rootNavController.popBackStack() }
            )
        }

        composable("conversas") {
            val userId = currentUserId ?: ""
            ConversasListScreen(
                currentUserId = userId,
                chatRepository = chatRepository,
                authRepository = authRepository,
                onNavigateToChat = { conversaId ->
                    rootNavController.navigate("chat/$conversaId")
                },
                onBack = { rootNavController.popBackStack() }
            )
        }

        composable(
            route = "chat/{conversaId}",
            arguments = listOf(navArgument("conversaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversaId = backStackEntry.arguments?.getString("conversaId") ?: ""
            val userId = currentUserId ?: ""
            ChatDetailScreen(
                conversaId = conversaId,
                currentUserId = userId,
                chatRepository = chatRepository,
                onBack = { rootNavController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainAppScaffold(
    userId: String,
    authRepository: AuthRepository,
    obraRepository: ObraRepository,
    communityRepository: CommunityRepository,
    chatRepository: ChatRepository,
    calculadorasRepository: CalculadorasRepository,
    onNavigateToObraDetail: (String) -> Unit,
    onNavigateToCreateObra: () -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToCalculators: () -> Unit,
    onNavigateToChat: () -> Unit,
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabs = listOf(
        Screen.Home,
        Screen.Comunidade,
        Screen.Criar,
        Screen.Obras,
        Screen.Perfil
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 8.dp
            ) {
                tabs.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                bottomNavController.navigate(screen.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PureWhite,
                            selectedTextColor = Slate900,
                            indicatorColor = Slate900,
                            unselectedIconColor = Slate500,
                            unselectedTextColor = Slate500
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = bottomNavController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        userId = userId,
                        authRepository = authRepository,
                        obraRepository = obraRepository,
                        onNavigateToObraDetail = onNavigateToObraDetail,
                        onNavigateToCreateObra = onNavigateToCreateObra,
                        onNavigateToCalculators = onNavigateToCalculators,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToDiario = onNavigateToObraDetail
                    )
                }

                composable(Screen.Comunidade.route) {
                    ComunidadeScreen(
                        currentUserId = userId,
                        authRepository = authRepository,
                        communityRepository = communityRepository,
                        onNavigateToCreatePost = onNavigateToCreatePost,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToCalculators = onNavigateToCalculators,
                        onNavigateToUserProfile = { /* Navigate to profile */ }
                    )
                }

                composable(Screen.Criar.route) {
                    CreateHubScreen(
                        userId = userId,
                        obraRepository = obraRepository,
                        onNavigateToCreateObra = onNavigateToCreateObra,
                        onNavigateToCreatePost = onNavigateToCreatePost,
                        onNavigateToCalculators = onNavigateToCalculators,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToObraDetail = onNavigateToObraDetail
                    )
                }

                composable(Screen.Obras.route) {
                    ObrasListScreen(
                        userId = userId,
                        obraRepository = obraRepository,
                        onNavigateToObraDetail = onNavigateToObraDetail,
                        onNavigateToCreateObra = onNavigateToCreateObra,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToCalculators = onNavigateToCalculators
                    )
                }

                composable(Screen.Perfil.route) {
                    PerfilScreen(
                        userId = userId,
                        authRepository = authRepository,
                        obraRepository = obraRepository,
                        communityRepository = communityRepository,
                        onNavigateToCalculators = onNavigateToCalculators,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToObraDetail = onNavigateToObraDetail,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
