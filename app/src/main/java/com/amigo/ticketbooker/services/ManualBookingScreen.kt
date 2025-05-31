package com.amigo.ticketbooker.services

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.ui.ServiceTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualBookingScreen() {
    val navController = LocalNavController.current
    
    Scaffold(
        topBar = {
            ServiceTopBar(
                title = "Manual Booking",
                onBackPressed = { navController.navigateUp() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            OptimizedWebViewForManualBooking("https://www.irctc.co.in/")
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OptimizedWebViewForManualBooking(url: String) {
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableStateOf(0) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // WebView
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    // Configure WebView settings for better performance
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadsImagesAutomatically = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//                        cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                        cacheMode = WebSettings.LOAD_DEFAULT
                        setGeolocationEnabled(true)
                        // Enable hardware acceleration
                        setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                    }

                    // Configure WebViewClient
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }
                        
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // Add a small delay to ensure the page is fully rendered
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isLoading = false
                            }, 300)
                        }
                        
                        // Additional check for when the page is actually ready
                        override fun onPageCommitVisible(view: WebView?, url: String?) {
                            super.onPageCommitVisible(view, url)
                            loadingProgress = 100
                        }
                    }
                    
                    // Configure WebChromeClient for progress tracking
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            loadingProgress = newProgress
                            
                            // If progress is 100%, ensure loading state is updated
                            if (newProgress >= 100) {
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    isLoading = false
                                }, 500)
                            }
                        }
                    }
                    
                    // Load the URL
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Loading indicator
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Loading IRCTC Booking...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { loadingProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$loadingProgress%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}