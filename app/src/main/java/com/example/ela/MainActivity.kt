package com.example.ela

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.ela.ui.navigation.MainScreen
import com.example.ela.ui.theme.ElaTheme
import com.example.ela.viewmodel.SettingsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val uiState by settingsViewModel.uiState.collectAsState()

            if (uiState.isLoading) {
                return@setContent
            }

            val isDarkMode = uiState.preferences.isDarkMode

            ElaTheme (
                dynamicColor = false,
                darkTheme = isDarkMode
            ){
                MainScreen()
            }
        }
    }
}
