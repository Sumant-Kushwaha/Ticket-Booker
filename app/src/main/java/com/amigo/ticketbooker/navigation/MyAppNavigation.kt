package com.amigo.ticketbooker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amigo.ticketbooker.auth.AuthScreen
import com.amigo.ticketbooker.auth.AuthViewModel
import com.amigo.ticketbooker.auth.OtpLoginScreen
import com.amigo.ticketbooker.help.HelpAndSupportScreen
import com.amigo.ticketbooker.home.HomeScreen
import com.amigo.ticketbooker.profile.ProfileScreen

// Define navigation routes as constants for easier access
object Routes {
    const val AUTH = "auth"
    const val OTP_LOGIN = "otp_login"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val BOOKING_HISTORY = "booking_history"
    const val SETTINGS = "settings"
    const val HELP_SUPPORT = "help_support"
    const val Logout = "logout"
}

@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    startDestination: String = Routes.AUTH
) {
    val navController = rememberNavController()

    // Check if user is already logged in
    LaunchedEffect(key1 = Unit) {
        if (authViewModel.isUserLoggedIn()) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.AUTH) { inclusive = true }
            }
        }
    }

    // Provide the NavController to the composition tree
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            composable(Routes.AUTH) {
                AuthScreen()
            }

            composable(Routes.OTP_LOGIN) {
                OtpLoginScreen()
            }

            composable(Routes.HOME) {
                HomeScreen()
            }

            composable(Routes.PROFILE) {
                ProfileScreen()
            }
            
            composable(Routes.HELP_SUPPORT) {
                HelpAndSupportScreen()
            }
        }
    }
}
