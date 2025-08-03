package com.amigo.ticketbooker.services.manualBookingUi

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession.NavigationDelegate
import org.mozilla.geckoview.GeckoSession.ProgressDelegate
import org.mozilla.geckoview.GeckoSession.ContentDelegate
import org.mozilla.geckoview.AllowOrDeny

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OptimizedWebViewForManualBooking(url: String) {
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableStateOf(0) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showRefreshButton by remember { mutableStateOf(false) }
    var geckoSession by remember { mutableStateOf<GeckoSession?>(null) }
    var geckoView by remember { mutableStateOf<GeckoView?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                GeckoView(context).apply {
                    val runtime = GeckoRuntime.create(
                        context,
                        GeckoRuntimeSettings.Builder()
                            .javaScriptEnabled(true)
                            .build()
                    )
                    
                    val session = GeckoSession(
                        GeckoSessionSettings.Builder()
                            .usePrivateMode(true)
                            .useTrackingProtection(true)
                            .build()
                    ).apply {
                        open(runtime)
                        
                        setContentDelegate(object : ContentDelegate {})
                        setNavigationDelegate(object : NavigationDelegate {
                            override fun onLoadRequest(
                                session: GeckoSession,
                                request: GeckoSession.NavigationDelegate.LoadRequest
                            ): GeckoResult<AllowOrDeny>? {
                                isLoading = true
                                hasError = false
                                showRefreshButton = false
                                return GeckoResult.fromValue(AllowOrDeny.ALLOW)
                            }
                        })
                        setProgressDelegate(object : ProgressDelegate {
                            override fun onPageStart(session: GeckoSession, url: String) {
                                isLoading = true
                                hasError = false
                                showRefreshButton = false
                            }
                            override fun onPageStop(session: GeckoSession, success: Boolean) {
                                Handler(Looper.getMainLooper()).postDelayed({ isLoading = false }, 300)
                                if (!success) {
                                    hasError = true
                                    errorMessage = "Failed to load IRCTC Booking. Please check your internet connection and try again."
                                }
                            }
                            override fun onProgressChange(session: GeckoSession, progress: Int) {
                                loadingProgress = progress
                                if (progress >= 100) {
                                    Handler(Looper.getMainLooper()).postDelayed({ isLoading = false }, 500)
                                }
                            }
                        })
                    }
                    geckoSession = session
                    setSession(session)
                    session.loadUri(url)
                }.also { geckoView = it }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            LoadingIndicator(loadingProgress)
        }

        if (showRefreshButton && !isLoading && !hasError) {
            RefreshButton(geckoSession)
        }

        if (hasError) {
            ErrorMessage(errorMessage, geckoSession)
        }
    }
}
