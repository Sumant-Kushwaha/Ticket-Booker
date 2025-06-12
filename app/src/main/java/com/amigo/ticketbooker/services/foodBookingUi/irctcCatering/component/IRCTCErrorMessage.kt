package com.amigo.ticketbooker.services.foodBookingUi.irctcCatering.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IRCTCErrorMessage(
    errorMessage: String,
    onRetry: () -> Unit,
    onTryMobileVersion: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onTryMobileVersion) {
            Text("Try Mobile Version")
        }
    }
}
