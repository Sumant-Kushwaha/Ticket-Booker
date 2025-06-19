package com.amigo.ticketbooker.services.automaticBooking.webRun

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint


@OptIn(DelicateCoroutinesApi::class)
@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun MainAutomate(
    modifier: Modifier = Modifier,
    inputUserName: String = "anshuabhishek",
    inputPassword: String = "Amigo@2805"
) {
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var captchaText by remember { mutableStateOf("") } // To store extracted captcha
    var captchaImageUrl by remember { mutableStateOf("") } // For debugging or future use
    val contextForML = context.applicationContext

    // Enhanced preprocessing: grayscale, resize, adaptive binarization
    fun adaptiveBinarize(bitmap: Bitmap, blockSize: Int = 12): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val binarized = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var sum = 0
                var count = 0
                for (dy in -blockSize/2..blockSize/2) {
                    for (dx in -blockSize/2..blockSize/2) {
                        val nx = x + dx
                        val ny = y + dy
                        if (nx in 0 until width && ny in 0 until height) {
                            val color = pixels[ny * width + nx]
                            sum += Color.red(color)
                            count++
                        }
                    }
                }
                val mean = sum / count
                val pixel = Color.red(pixels[y * width + x])
                val binColor = if (pixel > mean) Color.WHITE else Color.BLACK
                binarized.setPixel(x, y, binColor)
            }
        }
        return binarized
    }

    fun enhancedPreprocessBitmap(bitmap: Bitmap): Bitmap {
        // 1. Grayscale
        val width = bitmap.width
        val height = bitmap.height
        val grayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscale)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        // 2. Resize if needed
        val targetHeight = 60
        val resized = if (height < targetHeight) {
            Bitmap.createScaledBitmap(grayscale, (width * (targetHeight.toFloat() / height)).toInt(), targetHeight, true)
        } else {
            grayscale
        }
        // 3. Adaptive binarization
        return adaptiveBinarize(resized, blockSize = 12)
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
                val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
                    com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
                )
                val preprocessed = enhancedPreprocessBitmap(bitmap)
                val image = com.google.mlkit.vision.common.InputImage.fromBitmap(preprocessed, 0)
                val result = recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val finalText = visionText.text.trim() // Preserve all symbols and original case
                        Log.d("CaptchaOCR", "Captcha text: $finalText")
                        onResult(finalText)
                    }
                    .addOnFailureListener { e ->
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
                                                input.blur();
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
                val popUpRemove = """
                    javascript:(function() {
                        const el = document.querySelector("body > app-root > app-home > div.header-fix > app-header > p-dialog.ng-tns-c19-2 > div > div > div.ng-tns-c19-2.ui-dialog-content.ui-widget-content > div > form > div.text-center.col-xs-12 > button");
                        if (el) {
                            el.click();
                            Android.sendToAndroid("✅ First element clicked");
                        } else {
                            Android.sendToAndroid("❌ First element not found");
                        }
                    })();
                """.trimIndent()
                webViewRef?.evaluateJavascript(popUpRemove, null)

                delay(1000) // short delay before next click

                // Step 1: Click first element
                val menuIcon = """
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
                webViewRef?.evaluateJavascript(menuIcon, null)

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
                            input.blur();
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
                            input.blur();
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

                // Step 5: Click the SIGN IN button and perform up to 5 attempts for captcha and login
                var loginSuccess = false
                val targetSelector = "body > app-root > app-home > div.header-fix > app-header > div.col-sm-12.h_container > div.text-center.h_main_div > div.row.col-sm-12.h_head1 > a.search_btn.loginText.ng-star-inserted > span"
                for (attempt in 1..5) {
                    // Click SIGN IN
                    val clickSignIn = """
                        javascript:(function() {
                            var btn = document.querySelector('button.search_btn.train_Search.train_Search_custom_hover');
                            if (btn) {
                                btn.click();
                                Android.sendToAndroid('✅ SIGN IN button clicked (attempt $attempt)');
                            } else {
                                Android.sendToAndroid('❌ SIGN IN button not found (attempt $attempt)');
                            }
                        })();
                    """.trimIndent()
                    webViewRef?.evaluateJavascript(clickSignIn, null)
                    delay(3000) // Wait for login to process

                    // Check for target element
                    val checkTarget = """
                        javascript:(function() {
                            var el = document.querySelector('$targetSelector');
                            if (el) {
                                Android.sendToAndroid('✅ Target element found (attempt $attempt)');
                                return true;
                            } else {
                                Android.sendToAndroid('❌ Target element NOT found (attempt $attempt)');
                                return false;
                            }
                        })();
                    """.trimIndent()
                    var found = false
                    val latch = kotlinx.coroutines.CompletableDeferred<Boolean>()
                    webViewRef?.evaluateJavascript(
                        "(function() { return document.querySelector('$targetSelector') !== null; })();"
                    ) { value ->
                        found = value == "true"
                        latch.complete(found)
                    }
                    latch.await()
                    if (found) {
                        statusMessage = "✅ Login successful. Target element found."
                        loginSuccess = true
                        break
                    } else {
                        statusMessage = "❌ Target element not found. Retrying captcha (attempt $attempt)..."
                        // Re-extract captcha and fill it
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
                        delay(1500) // Wait for captcha to be solved and filled
                    }
                }
                if (!loginSuccess) {
                    statusMessage = "❌ Login failed after 5 attempts. Target element not found."
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
    