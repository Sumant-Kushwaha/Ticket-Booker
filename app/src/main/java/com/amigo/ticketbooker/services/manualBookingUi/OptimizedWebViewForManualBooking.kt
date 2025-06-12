package com.amigo.ticketbooker.services.manualBookingUi

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Handler
import android.os.Looper
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

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
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadsImagesAutomatically = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        cacheMode = WebSettings.LOAD_DEFAULT
                        setGeolocationEnabled(true)
                        setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            hasError = false
                            showRefreshButton = false
                        }
                        
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript("(function() { return {bodyLength: document.body.innerHTML.trim().length, hasContent: document.body.querySelector('div, p, img, form') !== null}; })();") { result ->
                                try {
                                    val bodyLength = result.substringAfter("bodyLength").substringAfter(":").substringBefore(",").trim().toIntOrNull() ?: 0
                                    val hasContent = result.contains("hasContent:true")
                                    showRefreshButton = bodyLength <= 10 && !hasContent
                                    Handler(Looper.getMainLooper()).postDelayed({ isLoading = false }, 300)
                                } catch (e: Exception) {
                                    isLoading = false
                                }
                            }
                        }
                        
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
                            handler?.proceed()
                        }
                    }
                    
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            loadingProgress = newProgress
                            if (newProgress >= 100) {
                                Handler(Looper.getMainLooper()).postDelayed({ isLoading = false }, 500)
                            }
                        }
                    }
                    
                    loadUrl(url)
                }.also { webView = it }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        if (isLoading) {
            LoadingIndicator(loadingProgress)
        }
        
        if (showRefreshButton && !isLoading && !hasError) {
            RefreshButton(webView)
        }
        
        if (hasError) {
            ErrorMessage(errorMessage, webView)
        }
    }
}
