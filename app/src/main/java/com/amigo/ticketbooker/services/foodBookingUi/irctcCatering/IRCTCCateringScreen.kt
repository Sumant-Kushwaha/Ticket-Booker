package com.amigo.ticketbooker.services.foodBookingUi.irctcCatering

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.services.foodBookingUi.irctcCatering.component.IRCTCErrorMessage
import com.amigo.ticketbooker.services.foodBookingUi.irctcCatering.component.IRCTCLoadingIndicator
import com.amigo.ticketbooker.services.foodBookingUi.irctcCatering.component.IRCTCRefreshOptions

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
    var showRefreshButton by remember { mutableStateOf(false) }

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
        Box(
            modifier = Modifier
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
                                        Handler(Looper.getMainLooper()).postDelayed({
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

                                // No automatic reload for suspiciously fast loads
                            }

                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    hasError = true
                                    isLoading = false
                                    errorMessage = "Failed to load IRCTC e-Catering. Please check your internet connection and try again."
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
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        isLoading = false
                                    }, 300)
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
                IRCTCLoadingIndicator(loadingProgress)
            }

            // Show refresh options if the page appears blank
            if (showRefreshButton && !isLoading && !hasError) {
                IRCTCRefreshOptions(
                    onRefresh = {
                        showRefreshButton = false
                        isLoading = true
                        webView?.apply {
                            clearCache(true)
                            clearHistory()
                            reload()
                        }
                    },
                    onTryMobileVersion = {
                        showRefreshButton = false
                        isLoading = true
                        webView?.apply {
                            clearCache(true)
                            clearHistory()
                            loadUrl("https://www.ecatering.irctc.co.in/mobile")
                        }
                    }
                )
            }

            // Error message
            if (hasError) {
                IRCTCErrorMessage(
                    errorMessage = errorMessage,
                    onRetry = {
                        hasError = false
                        isLoading = true
                        webView?.apply {
                            clearCache(true)
                            clearHistory()
                            reload()
                        }
                    },
                    onTryMobileVersion = {
                        hasError = false
                        isLoading = true
                        webView?.apply {
                            clearCache(true)
                            clearHistory()
                            loadUrl("https://www.ecatering.irctc.co.in/mobile")
                        }
                    }
                )
            }
        }
    }
}
