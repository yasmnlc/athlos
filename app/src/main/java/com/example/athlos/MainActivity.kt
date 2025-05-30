package com.example.athlos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.athlos.ui.screens.*
import com.example.athlos.ui.theme.AthlosTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AthlosTheme {
                AthlosApp()
            }
        }
    }
}

@Composable
fun AthlosApp() {
    val mainNavController = rememberNavController()

    NavHost(navController = mainNavController, startDestination = "splash") {
        composable("splash") { SplashScreen(mainNavController) }
        composable("login") { LoginScreen(mainNavController) }
        composable("register") { RegisterScreen(mainNavController) }
        composable("main") {
            MainScreenWithBottomNav(mainNavController)
        }
    }
}

data class DrawerItem(val label: String, val icon: ImageVector)

val drawerItems = listOf(
    DrawerItem("Dúvidas frequentes", Icons.Default.Help),
    DrawerItem("Configurações", Icons.Default.Settings),
    DrawerItem("Fale Conosco", Icons.Default.Email),
    DrawerItem("Sair", Icons.Default.ExitToApp)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithBottomNav(mainNavController: NavHostController? = null) {
    val bottomNavController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        selected = false,
                        onClick = {
                            // TODO
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Athlos") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                val items = listOf(
                    Screen.Home,
                    Screen.Water,
                    Screen.Diary,
                    Screen.Training,
                    Screen.Profile
                )

                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val colors = MaterialTheme.colorScheme

                NavigationBar(
                    containerColor = colors.surface,
                    contentColor = colors.onSurface
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.route) },
                            label = {
                                Text(
                                    screen.route.replaceFirstChar { it.uppercase() },
                                    color = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true)
                                        colors.primary else colors.onSurface
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                bottomNavController.navigate(screen.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primary,
                                selectedTextColor = colors.primary,
                                unselectedIconColor = colors.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = colors.onSurface.copy(alpha = 0.6f),
                                indicatorColor = colors.primary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = bottomNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) { HomeScreen() }
                composable(Screen.Water.route) { WaterScreen() }
                composable(Screen.Diary.route) { DiaryScreen() }
                composable(Screen.Training.route) { TrainingScreen() }
                composable(Screen.Profile.route) { ProfileScreen() }
            }
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector) {
    object Home : Screen("home", Icons.Default.Home)
    object Water : Screen("water", Icons.Default.LocalDrink)
    object Diary : Screen("diary", Icons.Default.MenuBook)
    object Training : Screen("training", Icons.Default.FitnessCenter)
    object Profile : Screen("profile", Icons.Default.Person)
}
