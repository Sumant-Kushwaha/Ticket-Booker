package com.amigo.ticketbooker.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController

/**
 * A CompositionLocal to provide NavController access throughout the app
 * without having to pass it as a parameter to every composable
 */
val LocalNavController = compositionLocalOf<NavController> { 
    error("NavController not provided") 
}
