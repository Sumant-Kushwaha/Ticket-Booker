package com.amigo.ticketbooker.authPage.mainLoginScreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amigo.ticketbooker.authPage.authViewModel.AuthState
import com.amigo.ticketbooker.authPage.authViewModel.AuthViewModel
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.navigation.Routes

@Composable
fun AuthScreen() {
    val navController = LocalNavController.current
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    // Observe auth state
    val authState by authViewModel.authState.collectAsState()
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Check if user is already logged in
    LaunchedEffect(key1 = Unit) {
        if (authViewModel.isUserLoggedIn()) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.AUTH) { inclusive = true }
            }
        }
    }

    // Handle auth state changes
    LaunchedEffect(key1 = authState) {
        when (authState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.AUTH) { inclusive = true }
                }
            }

            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
                showError = true
            }

            else -> {}
        }
    }

    // OTP screen is now handled by navigation

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3949AB), // Indian Railways Blue
                        Color(0xFF1A237E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // App Logo and Title
            AppLogoAndTitle()


            Spacer(modifier = Modifier.height(48.dp))

            // Auth Card
            AuthCard(
                authViewModel = authViewModel
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Text
        Text(
            text = "© 2025 Indian Railways",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        // Error Dialog
        if (showError) {
            AlertDialog(
                onDismissRequest = { showError = false },
                title = { Text("Authentication Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { showError = false }) {
                        Text("OK")
                    }
                }
            )
        }

        // Loading Indicator
        if (authState is AuthState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

