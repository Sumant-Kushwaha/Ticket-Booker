package com.amigo.ticketbooker.services.foodBookingUi.irctcCatering.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IRCTCRefreshOptions(
    onRefresh: () -> Unit,
    onTryMobileVersion: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "The page appears to be blank",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onRefresh) {
                Text("Refresh Page")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = onTryMobileVersion) {
                Text("Try Mobile Version")
            }
        }
    }
}
