package com.example.ela

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import dagger.hilt.android.AndroidEntryPoint
import com.example.ela.ui.navigation.MainScreen
import com.example.ela.ui.theme.ElaTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            ElaTheme (
                dynamicColor = false,
                darkTheme = false
            ){
                MainScreen()
            }
        }
    }
}
