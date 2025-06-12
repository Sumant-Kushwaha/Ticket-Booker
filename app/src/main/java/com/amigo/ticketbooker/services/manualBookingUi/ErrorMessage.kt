package com.amigo.ticketbooker.services.manualBookingUi

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorMessage(errorMessage: String, webView: WebView?) {
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
                webView?.apply {
                    clearCache(true)
                    clearHistory()
                    reload()
                }
            }
        ) {
            Text("Retry")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = {
                webView?.apply {
                    clearCache(true)
                    clearHistory()
                    loadUrl("https://www.irctc.co.in/nget/train-search")
                }
            }
        ) {
            Text("Try Direct Train Search")
        }
    }
}
