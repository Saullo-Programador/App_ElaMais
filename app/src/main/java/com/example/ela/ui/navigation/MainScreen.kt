package com.example.ela.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.ela.domain.model.CyclePhase
import com.example.ela.ui.screens.auth.LoginScreen
import com.example.ela.ui.screens.auth.LoginViewModel
import com.example.ela.ui.screens.auth.SignUpScreen
import com.example.ela.ui.screens.care.CareScreen
import com.example.ela.ui.screens.care.ManageCareScreen
import com.example.ela.ui.screens.cycle.CycleScreen
import com.example.ela.ui.screens.home.HomeScreen
import com.example.ela.ui.screens.preferences.PreferencesScreen
import com.example.ela.ui.screens.reminder.ReminderScreen
import com.example.ela.ui.screens.settings.SettingsScreen
import com.example.ela.ui.screens.splash.SplashScreen
import com.example.ela.viewmodel.HomeViewModel
import com.example.ela.ui.theme.ElaTheme

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val authViewModel: LoginViewModel = hiltViewModel()
    val homeState by homeViewModel.state.collectAsState()
    val currentPhase = homeState.cycleInfo?.currentPhase ?: CyclePhase.FOLLICULAR

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isUserAuthenticated = authViewModel.isUserAuthenticated()
    val startDestination = Screen.Splash.route


    val showBottomBar = currentRoute != Screen.Preferences.route &&
                        currentRoute != Screen.ManageCare.route &&
                        currentRoute != Screen.Splash.route &&
                        currentRoute != Screen.Login.route &&
                        currentRoute != Screen.Signup.route &&
                        currentRoute != Screen.ForgotPassword.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier,
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController, currentPhase)
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            // 🚀 SPLASH
            composable(Screen.Splash.route) {
                SplashScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            // 🔐 AUTH
            composable(Screen.Login.route) {
                LoginScreen(
                    navController = navController,
                    onSignup = {
                        navController.navigate(Screen.Signup.route)
                    },
                    onForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }
            composable(Screen.Signup.route) {
                SignUpScreen(
                    navController = navController,
                    onLogin = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }
            composable(Screen.ForgotPassword.route) {
                com.example.ela.ui.screens.auth.ForgotPasswordScreen(
                    navController = navController,
                    viewModel = authViewModel
                )
            }

            // 🏠 HOME
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onGoToCare = { phase ->
                        navController.navigate(Screen.Care.createRoute(phase.name)) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // ❤️ CARE (com argumento)
            composable(
                route = Screen.Care.route,
                arguments = listOf(
                    navArgument("phase") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

                val phaseString =
                    backStackEntry.arguments?.getString("phase")

                val phase = try {
                    CyclePhase.valueOf(phaseString ?: "")
                } catch (e: Exception) {
                    CyclePhase.FOLLICULAR
                }

                CareScreen(phase = phase)
            }

            // 📅 CICLO
            composable(Screen.Cycle.route) {
                CycleScreen()
            }

            // 🔔 REMINDER
            composable(Screen.Reminder.route) {
                ReminderScreen()
            }

            // ⚙️ SETTINGS
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onClickPreferences = {
                        navController.navigate(Screen.Preferences.route)
                    },
                    onClickManegeCare = {
                        navController.navigate(Screen.ManageCare.route)
                    }
                )
            }

            composable(Screen.Preferences.route) {
                PreferencesScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ManageCare.route) {
                ManageCareScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen(){
    ElaTheme(
        content = { MainScreen() }
    )
}