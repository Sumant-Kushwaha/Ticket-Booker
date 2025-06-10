package com.amigo.ticketbooker.services.coachPositionUi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.fontFamily
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.ui.ServiceTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachPositionScreen() {
    val navController = LocalNavController.current
    
    Scaffold(
        topBar = {
            ServiceTopBar(
                title = "Coach Position",
                onBackPressed = { navController.navigateUp() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Content will be added later
            Text(
                text = "Comming Soon",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily=fontFamily,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
