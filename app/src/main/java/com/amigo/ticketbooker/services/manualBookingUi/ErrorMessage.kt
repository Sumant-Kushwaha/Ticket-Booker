package com.amigo.ticketbooker.services.manualBookingUi

import org.mozilla.geckoview.GeckoSession
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorMessage(errorMessage: String, geckoSession: GeckoSession?) {
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
        Button(
            onClick = {
                geckoSession?.reload()
            }
        ) {
            Text("Retry")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                geckoSession?.loadUri("https://www.irctc.co.in/nget/train-search")
            }
        ) {
            Text("Try Direct Train Search")
        }
    }
}
