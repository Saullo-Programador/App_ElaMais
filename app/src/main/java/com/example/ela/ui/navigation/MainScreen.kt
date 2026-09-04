package com.example.ela.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ela.domain.model.CyclePhase
import com.example.ela.ui.screens.care.CareScreen
import com.example.ela.ui.screens.cycle.CycleScreen
import com.example.ela.ui.screens.home.HomeScreen
import com.example.ela.ui.screens.reminder.ReminderScreen
import com.example.ela.ui.screens.settings.SettingsScreen
import com.example.ela.ui.theme.ElaTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {

    val navController = rememberNavController()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier,
        bottomBar = {
            BottomBar(navController)
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {

            // 🏠 HOME
            composable(Screen.Home.route) {
                HomeScreen(
                    onGoToCare = { phase ->
                        navController.navigate(
                            Screen.Care.createRoute(phase.name)
                        )
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
                SettingsScreen()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewMainScreen(){
    ElaTheme(
        content = { MainScreen() }
    )
}