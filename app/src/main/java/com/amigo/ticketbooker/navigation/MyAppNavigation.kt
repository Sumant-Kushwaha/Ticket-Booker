package com.amigo.ticketbooker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.amigo.ticketbooker.auth.AuthScreen
import com.amigo.ticketbooker.auth.AuthViewModel
import com.amigo.ticketbooker.auth.OtpLoginScreen
import com.amigo.ticketbooker.home.HomeScreen
import com.amigo.ticketbooker.profile.ProfileScreen

// Define navigation routes
sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object OtpLogin : Screen("otp_login")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object BookingHistory : Screen("booking_history")
    object Settings : Screen("settings")
}

@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    startDestination: String = Screen.Auth.route
) {
    val navController = rememberNavController()
    
    // Check if user is already logged in
    LaunchedEffect(key1 = Unit) {
        if (authViewModel.isUserLoggedIn()) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Auth.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onNavigateToOtp = {
                    navController.navigate(Screen.OtpLogin.route)
                }
            )
        }
        
        composable(Screen.OtpLogin.route) {
            OtpLoginScreen(
                onBackToLogin = {
                    navController.popBackStack()
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                name = authViewModel.getCurrentUserName() ?: "User",
                phone = authViewModel.getCurrentUserPhone() ?: 0,
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
