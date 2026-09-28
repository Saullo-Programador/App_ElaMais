package com.example.ela.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.example.ela.ui.screens.settings.ManageCareScreen
import com.example.ela.ui.screens.cycle.CycleScreen
import com.example.ela.ui.screens.cycleHistory.CycleHistoryScreen
import com.example.ela.ui.screens.home.HomeScreen
import com.example.ela.ui.screens.preferences.PreferencesScreen
import com.example.ela.ui.screens.reminder.ReminderScreen
import com.example.ela.ui.screens.settings.CoupleSharingScreen
import com.example.ela.ui.screens.settings.SettingsScreen
import com.example.ela.ui.screens.splash.SplashScreen
import com.example.ela.viewmodel.HomeViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val authViewModel: LoginViewModel = hiltViewModel()
    val homeState by homeViewModel.state.collectAsState()
    val currentPhase = homeState.cycleInfo?.currentPhase ?: CyclePhase.FOLLICULAR

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = Screen.Splash.route


    val showBottomBar = (currentRoute != Screen.Preferences.route) &&
            (currentRoute != Screen.ManageCare.route) &&
            (currentRoute != Screen.CycleHistory.route) &&
            (currentRoute != Screen.CoupleSharing.route) &&
            (currentRoute != Screen.Splash.route) &&
            (currentRoute != Screen.Login.route) &&
            (currentRoute != Screen.Signup.route) &&
            (currentRoute != Screen.ForgotPassword.route)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        modifier = Modifier,
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController, currentPhase)
            }
        },
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
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
                ) {
                    navController.navigate(Screen.ForgotPassword.route)
                }
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
                } catch (_: Exception) {
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
                    onClickManageCare = {
                        navController.navigate(Screen.ManageCare.route)
                    },
                    onClickCycleHistory = {
                        navController.navigate(Screen.CycleHistory.route)
                    },
                    onClickCoupleSharing = {
                        navController.navigate(Screen.CoupleSharing.route)
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

            composable(Screen.CycleHistory.route) {
                CycleHistoryScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CoupleSharing.route) {
                CoupleSharingScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}