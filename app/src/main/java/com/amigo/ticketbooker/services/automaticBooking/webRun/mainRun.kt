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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter


@OptIn(DelicateCoroutinesApi::class)
@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun MainAutomate(
    modifier: Modifier = Modifier,
    inputUserName: String = "anshuabhishek",
    inputPassword: String = "Amigo@2805",
) {
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var captchaText by remember { mutableStateOf("") } // To store extracted captcha
    var captchaImageUrl by remember { mutableStateOf("") } // For debugging or future use
    val contextForML = context.applicationContext

    // Preprocess bitmap for better OCR accuracy (grayscale + contrast)
    fun preprocessBitmap(src: Bitmap): Bitmap {
        val bmp = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        // Increase contrast
        val contrast = 2.0f
        val translate = (-0.5f * contrast + 0.5f) * 255f
        colorMatrix.set(floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return bmp
    }

    // Helper to download image and run ML Kit OCR
    fun processCaptchaImage(url: String, context: android.content.Context, onResult: (String) -> Unit) {
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val bitmap = if (url.startsWith("data:image")) {
                    // base64 image
                    val base64Data = url.substringAfter(",")
                    val decoded = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                } else {
                    // normal url
                    val connection = java.net.URL(url).openConnection()
                    connection.connect()
                    val input = connection.getInputStream()
                    android.graphics.BitmapFactory.decodeStream(input)
                }
                val preprocessedBitmap = preprocessBitmap(bitmap)
                val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
                    com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
                )
                val image = com.google.mlkit.vision.common.InputImage.fromBitmap(preprocessedBitmap, 0)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        // Clean up: remove non-alphanum, preserve case
                        val cleanedText = visionText.text.replace("[^A-Za-z0-9]".toRegex(), "").trim()
                        onResult(cleanedText)
                    }
                    .addOnFailureListener { _ ->
                        onResult("")
                    }
            } catch (e: Exception) {
                onResult("")
            }
        }
    }

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

                    @JavascriptInterface
                    fun sendCaptchaImageUrl(url: String) {
                        Log.d("WebViewJS", "Captcha Image URL: $url")
                        captchaImageUrl = url
                        // Download and process image in background
                        processCaptchaImage(url, contextForML) { text ->
                            captchaText = text
                            statusMessage = "Captcha solved: $text"
                            Log.d("CaptchaOCR", "Extracted captcha text: $text")
                            // Inject captcha text into the captcha input field
                            if (text.isNotEmpty()) {
                                webViewRef?.post {
                                    webViewRef?.evaluateJavascript(
                                        """
                                        (function() {
                                            var input = document.querySelector('input[formcontrolname="captcha"]');
                                            if (input) {
                                                input.value = '""" + text + """';
                                                input.dispatchEvent(new Event('input', { bubbles: true }));
                                                input.dispatchEvent(new Event('change', { bubbles: true }));
                                                Android.sendToAndroid('✅ Captcha field filled');
                                            } else {
                                                Android.sendToAndroid('❌ Captcha input not found');
                                            }
                                        })();
                                        """.trimIndent(),
                                        null
                                    )
                                }
                            }
                        }
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
                            input.value = "$inputUserName";
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
                            input.value = "$inputPassword";
                            input.dispatchEvent(new Event('input', { bubbles: true }));
                            input.dispatchEvent(new Event('change', { bubbles: true }));
                            Android.sendToAndroid("✅ Input field filled");
                        } else {
                            Android.sendToAndroid("❌ Input field not found");
                        }
                    })();
                """.trimIndent()
                webViewRef?.evaluateJavascript(passwordInput, null)

//                delay(1200) // short delay before captcha extraction

                // Step 4: Extract captcha image after password
                val extractCaptcha = """
                    javascript:(function() {
                        var img = document.querySelector('.captcha-img');
                        if (img) {
                            var src = img.src || img.getAttribute('src');
                            Android.sendCaptchaImageUrl(src);
                        } else {
                            Android.sendToAndroid('❌ Captcha image not found');
                        }
                    })();
                """.trimIndent()
                webViewRef?.evaluateJavascript(extractCaptcha, null)
                delay(1200) // give time for captcha to process

                // Step 5: Click the SIGN IN button
                val clickSignIn = """
                    javascript:(function() {
                        var btn = document.querySelector('button.search_btn.train_Search.train_Search_custom_hover');
                        if (btn) {
                            btn.click();
                            Android.sendToAndroid('✅ SIGN IN button clicked');
                        } else {
                            Android.sendToAndroid('❌ SIGN IN button not found');
                        }
                    })();
                """.trimIndent()
                webViewRef?.evaluateJavascript(clickSignIn, null)
                delay(1000) // allow time for sign in action

                // Retry logic for captcha and login error
                var loginSuccess = false
                var attempt = 0
                while (attempt < 5 && !loginSuccess) {
                    // 1. Check for login error element
                    var errorFound = false
                    val checkError = """
                        javascript:(function() {
                            var err = document.querySelector('#login_header_disable > div > div > div.ng-tns-c19-13.ui-dialog-content.ui-widget-content > div.irmodal.ng-tns-c19-13 > div > div.login-bg.pull-left > div > div.modal-body > form > div.loginError');
                            if (err) {
                                Android.sendToAndroid('LOGIN_ERROR_PRESENT');
                            } else {
                                Android.sendToAndroid('LOGIN_ERROR_NOT_FOUND');
                            }
                        })();
                    """.trimIndent()
                    val checkSuccess = """
                        javascript:(function() {
                            var succ = document.querySelector('body > app-root > app-home > div.header-fix > app-header > div.col-sm-12.h_container > div.text-center.h_main_div > div.row.col-sm-12.h_head2 > nav > ul > li:nth-child(12) > a');
                            if (succ) {
                                Android.sendToAndroid('LOGIN_SUCCESS_PRESENT');
                            } else {
                                Android.sendToAndroid('LOGIN_SUCCESS_NOT_FOUND');
                            }
                        })();
                    """.trimIndent()

                    // Set up a one-time JS interface for this attempt
                    val resultHolder = java.util.concurrent.atomic.AtomicReference<String>()
                    val jsInterface = object {
                        @JavascriptInterface
                        fun sendToAndroid(message: String) {
                            Log.d("LoginRetry", "Attempt ${'$'}attempt: ${'$'}message")
                            resultHolder.set(message)
                        }
                    }
                    webViewRef?.addJavascriptInterface(jsInterface, "AndroidRetry")

                    // Check for error
                    webViewRef?.evaluateJavascript(checkError.replace("Android.sendToAndroid", "AndroidRetry.sendToAndroid"), null)
                    delay(1200)
                    val errorMsg = resultHolder.get() ?: ""
                    if (errorMsg.contains("LOGIN_ERROR_PRESENT")) {
                        errorFound = true
                    }

                    if (errorFound) {
                        // Solve captcha again and click sign in again, but wait for OCR and fill to complete
                        statusMessage = "Attempt ${'$'}{attempt+1}: Login error found, retrying captcha..."
                        // Extract captcha again (triggers JS to send image URL)
                        val captchaText = kotlinx.coroutines.suspendCancellableCoroutine<String> { cont ->
                            // Listen for captcha image URL and then solve
                            val jsInterface = object {
                                @android.webkit.JavascriptInterface
                                fun sendCaptchaImageUrl(url: String) {
                                    if (url.isEmpty() || url == "NOT_FOUND") {
                                        cont.resumeWith(Result.failure(Exception("Captcha image not loaded")))
                                    } else {
                                        processCaptchaImage(url, contextForML) { text ->
                                            // Fill captcha input with JS
                                            webViewRef?.post {
                                                webViewRef?.evaluateJavascript(
                                                    """
                                                    (function() {
                                                        var input = document.querySelector('input[formcontrolname=\"captcha\"]');
                                                        if (input) {
                                                            input.value = '""" + text + """';
                                                            input.dispatchEvent(new Event('input', { bubbles: true }));
                                                            input.dispatchEvent(new Event('change', { bubbles: true }));
                                                        }
                                                    })();
                                                    """.trimIndent(),
                                                    null
                                                )
                                            }
                                            cont.resume(text, null)
                                        }
                                    }
                                }
                            }
                            webViewRef?.addJavascriptInterface(jsInterface, "AndroidCaptchaRetry")
                            // Retry logic: poll for captcha image every 500ms up to 5s
                            val startTime = System.currentTimeMillis()
                            fun tryExtractCaptcha() {
                                val js = """
                                    (function() {
                                        var img = document.querySelector('.captcha-img');
                                        if (img && img.src) {
                                            AndroidCaptchaRetry.sendCaptchaImageUrl(img.src);
                                        } else {
                                            AndroidCaptchaRetry.sendCaptchaImageUrl('NOT_FOUND');
                                        }
                                    })();
                                """.trimIndent()
                                webViewRef?.evaluateJavascript(js, null)
                            }
                            fun poll() {
                                tryExtractCaptcha()
                                kotlinx.coroutines.GlobalScope.launch {
                                    kotlinx.coroutines.delay(500)
                                    if (!cont.isCompleted && (System.currentTimeMillis() - startTime < 5000)) {
                                        poll()
                                    } else if (!cont.isCompleted) {
                                        cont.resumeWith(Result.failure(Exception("Captcha image did not load in time")))
                                    }
                                }
                            }
                            poll()
                        }
                        // Wait a bit for JS to fill
                        delay(800)
                        // Click sign in again
                        webViewRef?.evaluateJavascript(clickSignIn, null)
                        delay(1200)
                    } else {
                        // Check for login success
                        webViewRef?.evaluateJavascript(checkSuccess.replace("Android.sendToAndroid", "AndroidRetry.sendToAndroid"), null)
                        delay(1200)
                        val succMsg = resultHolder.get() ?: ""
                        if (succMsg.contains("LOGIN_SUCCESS_PRESENT")) {
                            statusMessage = "✅ Login successful on attempt ${'$'}{attempt+1}"
                            loginSuccess = true
                        }
                    }
                    attempt++
                }
                if (!loginSuccess) {
                    statusMessage = "❌ Login failed after 5 attempts."
                }

            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        Text(
            text = statusMessage + if (captchaText.isNotEmpty()) "\nCaptcha: $captchaText" else "",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
