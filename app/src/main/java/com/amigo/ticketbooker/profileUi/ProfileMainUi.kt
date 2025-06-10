package com.amigo.ticketbooker.profileUi

import android.util.Log
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.navigation.Routes
import com.amigo.ticketbooker.authPage.authViewModel.AuthViewModel
import com.amigo.ticketbooker.ui.ConfirmationDialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val navController = LocalNavController.current
    val authViewModel: AuthViewModel = viewModel()
    
    // Get user information from AuthViewModel
    val name = authViewModel.getCurrentUserName()
    val phone = authViewModel.getCurrentUserPhone()
    
    // If we're using the default values, try to save the current user info
    // This helps ensure we have the correct data saved for future use
    if (name == "IRCTC User" || phone == 9876543210L) {
        // Get Firebase phone number directly for debugging
        val firebaseUser =
            FirebaseAuth.getInstance().currentUser
        val firebasePhone = firebaseUser?.phoneNumber
        
        // If we have a phone number from Firebase, try to save it properly
        if (!firebasePhone.isNullOrBlank()) {
            try {
                val parsedPhone = firebasePhone.replace("+91", "").toLongOrNull() ?: 0L
                if (parsedPhone > 0) {
                    // Save the phone number and a default name if needed
                    authViewModel.saveUserInfo(
                        name = firebaseUser.displayName ?: "IRCTC User",
                        phone = parsedPhone
                    )
                }
            } catch (e: Exception) {
                // Log error but continue
                Log.e("ProfileScreen", "Error saving user info: ${e.message}")
            }
        }
    }
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            ProfileTopBar(onBackPressed = { navController.navigateUp() })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background gradient for the top portion
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile header with animation
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                    exit = fadeOut()
                ) {
                    ProfileHeaderCard(name = name, phone = phone)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Profile options with animation
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                    exit = fadeOut()
                ) {
                    ProfileOptions()
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}











