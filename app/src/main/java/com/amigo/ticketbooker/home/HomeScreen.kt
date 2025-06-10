package com.amigo.ticketbooker.home

import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.navigation.Routes
import com.amigo.ticketbooker.authPage.authViewModel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.fontFamily
import com.amigo.ticketbooker.home.serviceCard.FirstRow
import com.amigo.ticketbooker.home.serviceCard.SecondRow
import com.amigo.ticketbooker.home.serviceCard.ThirdRow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val navController = LocalNavController.current
    val authViewModel: AuthViewModel = viewModel()
    // Use TicketBookerTheme to match authentication screens
    Scaffold(
        topBar = { 
            HomeAppBar(
                onProfileClick = { 
                    navController.navigate(Routes.PROFILE) 
                },
                onSignOut = { 
                    authViewModel.signOut()
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        },
        bottomBar = { 
            BottomSection(navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MainContent()
        }
    }

}

@Composable
fun MainContent() {
    val navController = LocalNavController.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Featured banner
        FeaturedBanner(navController)

        Spacer(modifier = Modifier.height(24.dp))

        // Main options grid
        Text(
            text = "Quick Services",
            fontFamily = fontFamily,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // First row
        FirstRow()

        Spacer(modifier = Modifier.height(16.dp))

        // Second row
        SecondRow(
)

        Spacer(modifier = Modifier.height(16.dp))

        // Third row
        ThirdRow()

        Spacer(modifier = Modifier.height(24.dp))

        // Recent bookings
        RecentBookingsSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Offers section
        OffersSection()

        Spacer(modifier = Modifier.height(70.dp)) // Space for bottom bar
    }
}













