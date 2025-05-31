package com.amigo.ticketbooker.services

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.ui.ServiceTopBar

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ZomatoTrainFoodScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    
    // Track loading state
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableStateOf(0) }
    var webView: WebView? by remember { mutableStateOf(null) }
    
    // Track error state
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // We'll keep everything within our app now
    // No need to track if external app was launched
    
    // Handle back navigation
    BackHandler {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            ServiceTopBar(
                title = "Zomato Food on Train", 
                onBackPressed = {
                    if (webView?.canGoBack() == true) {
                        webView?.goBack()
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // WebView container
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webView = this
                        settings.apply {
                            // Enable optimizations
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            // Removed deprecated databaseEnabled
                            setGeolocationEnabled(true)
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            displayZoomControls = false
                            builtInZoomControls = true
                            // Set caching mode
                            cacheMode = WebSettings.LOAD_DEFAULT
                            // Enable hardware acceleration
                            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                            // Enable mixed content
                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        }
                        
                        // Set up WebViewClient to handle page loading
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                hasError = false
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }
                            
                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    hasError = true
                                    errorMessage = "Failed to load Zomato: ${error?.description}"
                                    isLoading = false
                                }
                            }
                            
                            // Keep all navigation within our WebView
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString() ?: return false
                                
                                // Allow all navigation to happen inside our WebView
                                // except for non-web links like tel:, mailto:, etc.
                                if (url.startsWith("http://") || url.startsWith("https://") || 
                                    url.startsWith("file://") || url.startsWith("javascript:")) {
                                    return false // Let WebView handle web URLs
                                }
                                
                                // For non-web schemes, we can still try to handle them as intents
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                    return true
                                } catch (e: Exception) {
                                    // If we can't handle the URL, just ignore it
                                    return true
                                }
                            }
                        }
                        
                        // Handle progress updates
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                loadingProgress = newProgress
                            }
                        }
                        
                        // Load the Zomato train food page
                        loadUrl("https://link.zomato.com/xqzv/orderForTrain")
                    }
                },
                update = { _ ->
                    // Update logic if needed
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Show loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 5.dp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LinearProgressIndicator(
                            modifier = Modifier
                                .width(200.dp)
                                .height(6.dp),
                            progress = { loadingProgress / 100f },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "$loadingProgress%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Loading Zomato Food on Train...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Show error state
            if (hasError && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Connection Error",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                hasError = false
                                isLoading = true
                                webView?.reload()
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
