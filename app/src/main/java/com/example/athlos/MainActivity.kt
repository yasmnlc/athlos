package com.example.athlos

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.example.athlos.ui.screens.*
import com.example.athlos.ui.screens.signinscreens.RegisterScreen
import com.example.athlos.ui.theme.AthlosTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            val darkModeEnabled by this.dataStore.data.map { preferences ->
                preferences[DARK_MODE_KEY] ?: false
            }.collectAsState(initial = false)

            AthlosTheme(darkTheme = darkModeEnabled) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AthlosApp()
                }
            }
        }
    }
}

@Composable
fun AthlosApp() {
    val mainNavController = rememberNavController()

    NavHost(navController = mainNavController, startDestination = "splash") {
        composable("splash") { SplashScreen(mainNavController) }
        composable("login") { LoginScreen(navController = mainNavController) }
        composable("register") { RegisterScreen(mainNavController) }
        composable("main") { MainScreenWithBottomNav(mainNavController = mainNavController) }
        composable("settings") { SettingsScreen() }

        // ADICIONADO: Rota para a tela Fale Conosco
        composable("contact") {
            ContactScreen(navController = mainNavController)
        }
    }
}

data class DrawerItem(val label: String, val icon: ImageVector, val route: String?)

val drawerItems = listOf(
    DrawerItem("Configurações", Icons.Default.Settings, "settings"),
    DrawerItem("Fale Conosco", Icons.Default.Email, "contact")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithBottomNav(mainNavController: NavHostController) {
    val bottomNavController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

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
                            scope.launch { drawerState.close() }
                            item.route?.let { route ->
                                mainNavController.navigate(route)
                            }
                        }
                    )
                }
                // Item de Sair
                NavigationDrawerItem(
                    label = { Text("Sair") },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        auth.signOut()
                        mainNavController.navigate("login") {
                            popUpTo(mainNavController.graph.id) {
                                inclusive = true
                            }
                        }
                    }
                )
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
                val items = listOf(Screen.Home, Screen.Water, Screen.Diary, Screen.Training, Screen.Profile)
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.route) },
                            label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                bottomNavController.navigate(screen.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
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
                composable(Screen.Profile.route) { ProfileScreen(mainNavController = mainNavController) }

                composable(Screen.Training.route) {
                    TrainingScreen(
                        onSavedWorkoutsClick = {
                            bottomNavController.navigate("savedWorkouts")
                        },
                        onNavigateToExerciseList = { bodyPart ->
                            bottomNavController.navigate("exerciseList/$bodyPart")
                        }
                    )
                }

                composable(
                    route = "exerciseList/{bodyPart}",
                    arguments = listOf(navArgument("bodyPart") { type = NavType.StringType })
                ) { backStackEntry ->
                    val bodyPart = backStackEntry.arguments?.getString("bodyPart")
                    if (bodyPart != null) {
                        ExerciseListScreen(bodyPart = bodyPart)
                    }
                }

                composable("savedWorkouts") {
                    SavedWorkoutsScreen(
                        onWorkoutClick = { workoutId ->
                            bottomNavController.navigate("savedWorkoutDetail/$workoutId")
                        }
                    )
                }

                composable(
                    route = "savedWorkoutDetail/{workoutId}",
                    arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val workoutId = backStackEntry.arguments?.getString("workoutId")
                    if (workoutId != null) {
                        SavedWorkoutDetailScreen(workoutId = workoutId)
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector) {
    object Home : Screen("home", Icons.Default.Home)
    object Water : Screen("water", Icons.Default.LocalDrink)
    object Diary : Screen("diary", Icons.AutoMirrored.Filled.MenuBook)
    object Training : Screen("training", Icons.Default.FitnessCenter)
    object Profile : Screen("profile", Icons.Default.Person)
}