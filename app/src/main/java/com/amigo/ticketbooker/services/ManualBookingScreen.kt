package com.amigo.ticketbooker.services

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.*
import android.net.http.SslError
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
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showRefreshButton by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    
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
                        // databaseEnabled is deprecated, using alternatives
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
                            hasError = false
                            showRefreshButton = false
                        }
                        
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            
                            // Check if the page might be blank
                            view?.evaluateJavascript("(function() { return {bodyLength: document.body.innerHTML.trim().length, hasContent: document.body.querySelector('div, p, img, form') !== null}; })();") { result ->
                                try {
                                    // Extract values from the JavaScript result
                                    val bodyLength = result.substringAfter("bodyLength").substringAfter(":").substringBefore(",").trim().toIntOrNull() ?: 0
                                    val hasContent = result.contains("hasContent:true")
                                    
                                    // If page appears blank, show refresh button instead of auto-refreshing
                                    showRefreshButton = bodyLength <= 10 && !hasContent
                                    
                                    // Always complete loading after checking
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        isLoading = false
                                    }, 300)
                                } catch (e: Exception) {
                                    // If there's an error parsing the result, just finish loading
                                    isLoading = false
                                }
                            }
                        }
                        
                        // Additional check for when the page is actually ready
                        override fun onPageCommitVisible(view: WebView?, url: String?) {
                            super.onPageCommitVisible(view, url)
                            loadingProgress = 100
                        }
                        
                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                hasError = true
                                isLoading = false
                                errorMessage = "Failed to load IRCTC Booking. Please check your internet connection and try again."
                            }
                        }
                        
                        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                            // Handle SSL certificate errors
                            handler?.proceed() // Proceed with SSL certificate issues in dev environment
                            // In production, you might want to handle this differently
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
                }.also { webView = it }
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
        
        // Show refresh button if we detected a blank page
        if (showRefreshButton && !isLoading && !hasError) {
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
                            showRefreshButton = false
                            isLoading = true
                            webView?.apply {
                                clearCache(true)
                                clearHistory()
                                reload()
                            }
                        }
                    ) {
                        Text("Refresh Page")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = {
                            showRefreshButton = false
                            isLoading = true
                            webView?.apply {
                                clearCache(true)
                                clearHistory()
                                // Try loading the mobile version explicitly
                                loadUrl("https://www.irctc.co.in/nget/train-search")
                            }
                        }
                    ) {
                        Text("Try Direct Train Search")
                    }
                }
            }
        }
        
        // Error message
        if (hasError) {
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
                        hasError = false
                        isLoading = true
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
                
                // Alternative loading option
                OutlinedButton(
                    onClick = {
                        hasError = false
                        isLoading = true
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
    }
}