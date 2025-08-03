package com.amigo.ticketbooker.services.manualBookingUi

import org.mozilla.geckoview.GeckoSession
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RefreshButton(geckoSession: GeckoSession?) {
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

            Button(
                onClick = {
                    geckoSession?.reload()
                }
            ) {
                Text("Refresh Page")
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
}
