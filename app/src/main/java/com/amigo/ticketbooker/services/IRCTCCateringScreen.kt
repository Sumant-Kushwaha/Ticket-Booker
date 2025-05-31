package com.amigo.ticketbooker.services

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.amigo.ticketbooker.navigation.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun IRCTCCateringScreen() {
    val navController = LocalNavController.current
    
    // State variables
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingProgress by remember { mutableStateOf(0) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // URL for IRCTC e-catering
    val irctcCateringUrl = "https://www.ecatering.irctc.co.in"
    
    // Handle back navigation within WebView
    BackHandler {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            navController.navigateUp()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "IRCTC Food Ordering",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (webView?.canGoBack() == true) {
                            webView?.goBack()
                        } else {
                            navController.navigateUp()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            // WebView to load IRCTC e-catering website
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        
                        // Configure WebView settings for optimal performance
                        with(settings) {
                            // Enable JavaScript
                            javaScriptEnabled = true
                            
                            // Enable DOM storage for web app functionality
                            domStorageEnabled = true
                            
                            // Use default mobile user agent for mobile-optimized website
                            // We're not setting a custom userAgentString here to use the default mobile one
                            
                            // Cache settings for faster loading
                            cacheMode = WebSettings.LOAD_DEFAULT
                            
                            // Enable hardware acceleration
                            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                            
                            // Other optimizations
                            loadsImagesAutomatically = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                        }
                        
                        // Configure WebView client
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                hasError = false
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
                            
                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    hasError = true
                                    errorMessage = "Error loading page: ${error?.description}"
                                    isLoading = false
                                }
                            }
                            
                            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                // Handle SSL certificate errors
                                handler?.proceed() // Proceed with SSL certificate issues in dev environment
                                // In production, you might want to handle this differently
                            }
                        }
                        
                        // Configure Chrome client for progress tracking
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
                        
                        // Load the IRCTC e-catering URL
                        loadUrl(irctcCateringUrl)
                    }.also { webView = it }
                },
                update = { _ ->
                    // Update logic if needed
                }
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
                        text = "Loading IRCTC e-Catering...",
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
