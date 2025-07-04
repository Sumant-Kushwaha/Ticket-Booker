package com.amigo.ticketbooker.services.automaticBooking.anotherWeb

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlin.random.Random
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import android.graphics.*
import android.util.Base64
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.net.URL
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.set


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun IrctcWebViewScreen(
    inputUserName: String = "anshuabhishek",
    inputPassword: String = "Amigo@2805",
) {
    var statusMessage by remember { mutableStateOf("Loading...") }
    var hasLoggedIn by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.userAgentString = CHROME_USER_AGENT
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun sendToAndroid(message: String) {
                            Log.d("WebViewJS", message)
                            statusMessage = message
                        }
                    }, "Android")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d("IRCTC_WEBVIEW", "Page loaded: $url")

                            if (!hasLoggedIn) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    // 1. Remove popup for 2 minutes in background
                                    val popupJob = launch {
                                        val end = System.currentTimeMillis() + 2 * 60 * 1000
                                        while (System.currentTimeMillis() < end) {
                                            removePopup(this@apply)
                                            delay(2000)
                                        }
                                    }

                                    // 2. Click menu, then login button
                                    menuClick(this@apply)
                                    delay(800)
                                    loginButtonInMenu(this@apply)
                                    delay(800)

                                    // 3. Fill username and password ONCE
                                    enterUsername(this@apply, inputUserName)
                                    delay(400)
                                    enterPassword(this@apply, inputPassword)
                                    delay(400)

                                    // 4. Captcha solve loop
                                    val captchaImageSelector = ".captcha-img"
                                    val inputFieldSelector = "input[formcontrolname='captcha']"
                                    val buttonSelector = "#login_header_disable > div > div > div.ng-tns-c19-13.ui-dialog-content.ui-widget-content > div.irmodal.ng-tns-c19-13 > div > div.login-bg.pull-left > div > div.modal-body > form > span > button"
                                    val refreshButtonXPath = "//*[@id=\"login_header_disable\"]/div/div/div[2]/div[2]/div/div[2]/div/div[2]/form/div[5]/div/app-captcha/div/div/div[2]/span[2]/a/span"

                                    var lastCaptchaUrl: String? = null

                                    suspend fun getCaptchaUrl(): String {
                                        val js = """
                                            (function() {
                                                const img = document.querySelector("$captchaImageSelector");
                                                if (img) return img.src || img.getAttribute('src');
                                                return "";
                                            })();
                                        """.trimIndent()
                                        return suspendCancellableCoroutine { cont ->
                                            this@apply.evaluateJavascript(js) { url ->
                                                cont.resume(url.trim('"'), null)
                                            }
                                        }
                                    }

                                    suspend fun fillCaptchaAndSignIn(captchaText: String) {
                                        fillCaptchaInput(this@apply, inputFieldSelector, captchaText)
                                        delay(400)
                                        clickLoginButton(this@apply, buttonSelector)
                                    }

                                    suspend fun isSignInButtonPresent(): Boolean {
                                        val js = """
                                            (function() {
                                                const btn = document.querySelector("$buttonSelector");
                                                return !!btn;
                                            })();
                                        """.trimIndent()
                                        return suspendCancellableCoroutine { cont ->
                                            this@apply.evaluateJavascript(js) { result ->
                                                cont.resume(result == "true", null)
                                            }
                                        }
                                    }

                                    suspend fun solveCaptchaAndSignIn() {
                                        val maxRetries = 5 // Change this value as needed
                                        var retries = 0
                                        while (retries < maxRetries) {
                                            retries++
                                            // Download captcha and store link
                                            val captchaUrl = getCaptchaUrl()
                                            lastCaptchaUrl = captchaUrl

                                            // OCR
                                            val captchaText = suspendCancellableCoroutine<String> { cont ->
                                                downloadAndRecognizeCaptcha(context, captchaUrl) { text ->
                                                    cont.resume(text, null)
                                                }
                                            }
                                            if (captchaText.isEmpty()) {
                                                refreshCaptcha(this@apply, refreshButtonXPath)
                                                delay(1000)
                                                continue
                                            }

                                            // Only fill captcha and sign in
                                            fillCaptchaAndSignIn(captchaText)
                                            delay(500)

                                            // Check if sign in button still present
                                            if (isSignInButtonPresent()) {
                                                // Get captcha url again
                                                val newCaptchaUrl = getCaptchaUrl()
                                                if (newCaptchaUrl == lastCaptchaUrl) {
                                                    // Refresh until captcha changes
                                                    var refreshed = false
                                                    repeat(5) {
                                                        refreshCaptcha(this@apply, refreshButtonXPath)
                                                        delay(1000)
                                                        val checkUrl = getCaptchaUrl()
                                                        if (checkUrl != lastCaptchaUrl) {
                                                            refreshed = true
                                                            lastCaptchaUrl = checkUrl
                                                            return@repeat
                                                        }
                                                    }
                                                    if (!refreshed) {
                                                        // Could not get new captcha, try again
                                                        continue
                                                    }
                                                }
                                                // New captcha, repeat solve
                                                continue
                                            } else {
                                                // Sign in button gone, done
                                                statusMessage = "Login process finished."
                                                break
                                            }
                                        }
                                        if (retries >= maxRetries) {
                                            statusMessage = "❌ Max captcha retries reached."
                                        }
                                    }

                                    solveCaptchaAndSignIn()
                                    popupJob.cancel()
                                    hasLoggedIn = true
                                }
                            }
                        }
                    }
                    loadUrl("https://www.irctc.co.in/nget/train-search")
                }
            }
        )
        Text(
            text = statusMessage,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

private const val CHROME_USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Pixel 6 Pro) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/114.0.5735.199 Mobile Safari/537.36"

private fun downloadAndRecognizeCaptcha(context: Context, imageUrl: String, onResult: (String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val bitmap = if (imageUrl.startsWith("data:image")) {
                val base64 = imageUrl.substringAfter(",")
                val decoded = Base64.decode(base64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            } else {
                BitmapFactory.decodeStream(URL(imageUrl).openConnection().getInputStream())
            }

            val preprocessed = preprocessBitmap(bitmap)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(preprocessed, 0)

            recognizer.process(image)
                .addOnSuccessListener { onResult(it.text.trim().replace("\\s".toRegex(), "")) }
                .addOnFailureListener { onResult("") }

        } catch (e: Exception) {
            Log.e("CaptchaSolver", "Error loading captcha: ${e.message}")
            onResult("")
        }
    }
}

private fun preprocessBitmap(bitmap: Bitmap): Bitmap {
    val grayBitmap = createBitmap(bitmap.width, bitmap.height)
    val canvas = Canvas(grayBitmap)
    val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
    }
    canvas.drawBitmap(bitmap, 0f, 0f, paint)

    val binarized = createBitmap(grayBitmap.width, grayBitmap.height)
    for (x in 0 until grayBitmap.width) {
        for (y in 0 until grayBitmap.height) {
            val pixel = grayBitmap[x, y]
            val brightness = android.graphics.Color.red(pixel) // <-- use fully qualified name
            val binarizedColor = if (brightness < 128) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            binarized[x, y] = binarizedColor
        }
    }

    return binarized
}

private fun fillCaptchaInput(webView: WebView, inputSelector: String, captcha: String) {
    val js = """
        (function() {
            const input = document.querySelector("$inputSelector");
            if (input) { input.value = "$captcha"; input.dispatchEvent(new Event('input', { bubbles: true })); }
        })();
    """.trimIndent()
    webView.post { webView.evaluateJavascript(js, null) }
}

private fun clickLoginButton(webView: WebView, buttonSelector: String) {
    val js = """
        (function() {
            const btn = document.querySelector("$buttonSelector");
            if (btn) { btn.click(); }
        })();
    """.trimIndent()
    webView.post { webView.evaluateJavascript(js, null) }
}

private fun refreshCaptcha(webView: WebView, refreshButtonXPath: String) {
    val js = """
        (function() {
            const el = document.evaluate('$refreshButtonXPath', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
            if (el) el.click();
        })();
    """.trimIndent()

    webView.post { webView.evaluateJavascript(js, null) }
}

private fun removePopup(webView: WebView) {
    val js = """
        javascript:(function() {
            const el = document.querySelector("body > app-root > app-home > div.header-fix > app-header > p-dialog.ng-tns-c19-2 > div > div > div.ng-tns-c19-2.ui-dialog-content.ui-widget-content > div > form > div.text-center.col-xs-12 > button");
            if (el) {
                el.click();
                window.Android.sendToAndroid("✅ Popup Removed");
            } else {
                window.Android.sendToAndroid("❌ Popup not found");
            }
        })();
    """.trimIndent()
    webView.evaluateJavascript(js, null)
}

private fun menuClick(webView: WebView) {
    CoroutineScope(Dispatchers.Main).launch {
        val js = """
            (function() {
                const el = document.querySelector("body > app-root > app-home > div.header-fix > app-header > div.h_container_sm > div.h_menu_drop_button.moblogo.hidden-sm > a > i");
                if (el) {
                    el.click();
                    window.Android.sendToAndroid("✅ Menu clicked");
                    return "CLICKED";
                } else {
                    window.Android.sendToAndroid("❌ Menu not found, retrying...");
                    return "NOT_FOUND";
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(js) { result ->
            if (result?.contains("NOT_FOUND") == true) {
                Log.d("MenuClick", "Menu button not found, retrying...")
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500L)
                    menuClick(webView) // Retry until success
                }
            }
        }
    }
}



private fun loginButtonInMenu(webView: WebView) {
    val js = """
        javascript:(function() {
                        const el = document.querySelector("#slide-menu > p-sidebar > div > nav > div > label > button");
                        if (el) {
                            el.click();
                            Android.sendToAndroid("✅ Login button clicked");
                        } else {
                            Android.sendToAndroid("❌ Login button not found");
                        }
                    })();
                """.trimIndent()
    webView.evaluateJavascript(js, null)
}

private fun enterUsername(webView: WebView, inputUserName: String) {
    val js = """
        javascript:(function() {
                        const input = document.querySelector("input[formcontrolname='userid']");
                        if (input) {
                            input.value = "$inputUserName";
                            input.dispatchEvent(new Event('input', { bubbles: true }));
                            input.dispatchEvent(new Event('change', { bubbles: true }));
                            input.blur();
                            Android.sendToAndroid("✅ Username filled");
                        } else {
                            Android.sendToAndroid("❌ Username field not found");
                        }
                    })();
                """.trimIndent()
    webView.evaluateJavascript(js, null)
}

private fun enterPassword(webView: WebView, inputPassword: String) {
    val js = """
        javascript:(function() {
                        const input = document.querySelector("input[formcontrolname='password']");
                        if (input) {
                            input.value = "$inputPassword";
                            input.dispatchEvent(new Event('input', { bubbles: true }));
                            input.dispatchEvent(new Event('change', { bubbles: true }));
                            input.blur();
                            Android.sendToAndroid("✅ Password filled");
                        } else {
                            Android.sendToAndroid("❌ Password field not found");
                        }
                    })();
                """.trimIndent()
    webView.evaluateJavascript(js, null)
}
