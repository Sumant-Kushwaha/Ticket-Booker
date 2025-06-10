package com.amigo.ticketbooker.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.navigation.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpAndSupportScreen() {
    val navController = LocalNavController.current
    
    Scaffold(
        topBar = {
            HelpSupportTopBar(onBackPressed = { navController.navigateUp() })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header section
            HelpSupportHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Contact section
            ContactSection()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // FAQ section
            FAQSection()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Help guides section
            HelpGuidesSection()
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

