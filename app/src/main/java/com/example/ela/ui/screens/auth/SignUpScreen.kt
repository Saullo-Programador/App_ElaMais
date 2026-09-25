package com.example.ela.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ela.ui.components.ButtonComponent
import com.example.ela.ui.components.InputComponent
import com.example.ela.ui.theme.ElaTheme

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    SignUpScreenContent(
        uiState = uiState,
        onLogin = { email, password -> viewModel.login(email, password) },
        onSignup = { email, password -> viewModel.signup(email, password) }
    )
}

@Composable
fun SignUpScreenContent(
    uiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onSignup: (String, String) -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Branding Icon
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Greeting
            Text(
                text = "Crie sua conta no ElaMais",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Junte-se a nós nesta jornada de autoconhecimento.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // User Name Input
            InputComponent(
                value = userName,
                onValueChange = { userName = it },
                label = "Usuário",
                placeholder = "Digite seu nome",
                leadingIcon = Icons.Default.Person,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Email Input
            InputComponent(
                value = email,
                onValueChange = { email = it },
                label = "E-mail",
                placeholder = "exemplo@email.com",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Password Input
            InputComponent(
                value = password,
                onValueChange = { password = it },
                label = "Senha",
                placeholder = "Digite sua senha",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                onTrailingIconClick = { passwordVisible = !passwordVisible },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Confirm Password Input
            InputComponent(
                value = passwordConfirm,
                onValueChange = { passwordConfirm = it },
                label = "Confirmar Senha",
                placeholder = "Confirme sua senha",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (passwordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = if (passwordConfirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                onTrailingIconClick = { passwordConfirmVisible = !passwordConfirmVisible },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Sign Up Button
            ButtonComponent(
                text = "Cadastrar",
                loading = uiState is AuthUiState.Loading,
                onClick = {
                    onSignup(email, password)
                }
            )

            // Footer Navigation
            Row(
                modifier = Modifier.padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "Já tem uma conta?",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = {
                        onLogin(email, password)
                    },
                ) {
                    Text(
                        text = "Entrar",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (uiState is AuthUiState.Error) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpPreview() {
    ElaTheme {
        SignUpScreenContent(
            uiState = AuthUiState.Idle,
            onLogin = { _, _ -> },
            onSignup = { _, _ -> }
        )
    }
}
