package com.amigo.ticketbooker.services.automaticBooking.webRun

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay


@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun MainAutomate(
    modifier: Modifier = Modifier,
    inputText: String = "anshuabhishek"
) {
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Box(modifier = modifier.fillMaxSize()) {

        AndroidView(factory = {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                WebView.setWebContentsDebuggingEnabled(true)

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun sendToAndroid(message: String) {
                        Log.d("WebViewJS", message)
                        statusMessage = message
                    }
                }, "Android")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        isLoading = false
                        statusMessage = "✅ Page loaded. Running automation..."

                        webViewRef = view // store ref for use in LaunchedEffect
                    }
                }

                loadUrl("https://www.irctc.co.in/nget/train-search")
            }
        }, modifier = Modifier.fillMaxSize())

        // Step chain (triggered once page is loaded and webViewRef is set)
        LaunchedEffect(webViewRef) {
            if (webViewRef != null) {
                delay(3000) // wait 3 seconds after page load

                // Step 1: Click first element
                val click1 = """
                    javascript:(function() {
                        const el = document.querySelector("body > app-root > app-home > div.header-fix > app-header > div.h_container_sm > div.h_menu_drop_button.moblogo.hidden-sm > a > i");
                        if (el) {
                            el.click();
                            Android.sendToAndroid("✅ First element clicked");
                        } else {
                            Android.sendToAndroid("❌ First element not found");
                        }
                    })();
                """.trimIndent()
                webViewRef?.evaluateJavascript(click1, null)

                delay(1000) // short delay before next click

                // Step 2: Click second element
                val click2 = """
                    javascript:(function() {
                        const el = document.querySelector("#slide-menu > p-sidebar > div > nav > div > label > button");
                        if (el) {
                            el.click();
                            Android.sendToAndroid("✅ Second element clicked");
                        } else {
                            Android.sendToAndroid("❌ Second element not found");
                        }
                    })();
                """.trimIndent()
                webViewRef?.evaluateJavascript(click2, null)

                delay(1000) // short delay before typing

                // Step 3: Fill input field
                val userNameInput = """
                    javascript:(function() {
                        const input = document.querySelector("input[formcontrolname='userid']");
                        if (input) {
                            input.value = "$inputText";
                            input.dispatchEvent(new Event('input', { bubbles: true }));
                            input.dispatchEvent(new Event('change', { bubbles: true }));
                            Android.sendToAndroid("✅ Input field filled");
                        } else {
                            Android.sendToAndroid("❌ Input field not found");
                        }
                    })();
                """.trimIndent()
                webViewRef?.evaluateJavascript(userNameInput, null)

                delay(1000) // short delay before typing

                // Step 3: Fill input field
                val passwordInput = """
                    javascript:(function() {
                        const input = document.querySelector("input[formcontrolname='password']");
                        if (input) {
                            input.value = "$inputText";
                            input.dispatchEvent(new Event('input', { bubbles: true }));
                            input.dispatchEvent(new Event('change', { bubbles: true }));
                            Android.sendToAndroid("✅ Input field filled");
                        } else {
                            Android.sendToAndroid("❌ Input field not found");
                        }
                    })();
                """.trimIndent()
                webViewRef?.evaluateJavascript(passwordInput, null)

                delay(1000) // short delay before typing

            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        Text(
            text = statusMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
