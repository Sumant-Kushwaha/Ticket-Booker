package com.amigo.ticketbooker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.amigo.ticketbooker.auth.AuthViewModel
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.navigation.MyAppNavigation
import com.amigo.ticketbooker.navigation.Routes

/**
 * Root component that handles navigation and exit confirmation
 */
@Composable
fun AppRoot(
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val authViewModel: AuthViewModel = viewModel()
    
    // Apply the modifier to a Box that wraps everything
    Box(modifier = modifier) {
    
    // State for exit confirmation dialog
    var showExitDialog by remember { mutableStateOf(false) }
    
    // Handle back press on Home screen
    if (currentRoute == Routes.HOME) {
        BackHandler {
            showExitDialog = true
        }
    }
    
    // Exit confirmation dialog
    if (showExitDialog) {
        ConfirmationDialog(
            title = "Exit Application",
            message = "Are you sure you want to exit the application?",
            confirmText = "Exit",
            dismissText = "Cancel",
            confirmColor = androidx.compose.material3.MaterialTheme.colorScheme.error,
            onConfirm = {
                showExitDialog = false
                onExitApp()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }
    
    // Provide the NavController to the composition tree
    CompositionLocalProvider(LocalNavController provides navController) {
        MyAppNavigation(
            navController = navController,
            authViewModel = authViewModel
        )
    }
  }
}
