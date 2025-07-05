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
import java.lang.Math.random
import kotlin.String
import kotlin.coroutines.resume


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun IrctcWebViewScreen(
    inputUserName: String = "Shrisha2808",
    inputPassword: String = "Shrisha@2808",
    inputDate: String="18/08/2025",
    quotaName: String = "1",
    className: String = "12",
    inputOrigin: String = "NEW DELHI - NDLS (NEW DELHI)",
    inputDestination: String = "GORAKHPUR JN - GKP (GORAKHPUR)",
    targetTrainNumber: String = "12566",
    targetClassCode: String = "SL"
) {

    val quotaIndex = when (quotaName.trim().uppercase()) {
        in listOf("1", "GENERAL") -> 1
        in listOf("2", "LADIES") -> 2
        in listOf("3", "LOWER BERTH/SR.CITIZEN") -> 3
        in listOf("4", "PERSON WITH DISABILITY") -> 4
        in listOf("5", "DUTY PASS", "DUTYPASS") -> 5
        in listOf("6", "TATKAL", "TAT KAL") -> 6
        in listOf("7", "PREMIUM TATKAL", "PREMIUM-TATKAL") -> 7
        else -> 1
    }

    val classIndex = when (className.uppercase()) {
        in listOf("2", "EA", "ANUBHUTI CLASS") -> 2
        in listOf("3", "1A", "AC FIRST CLASS") -> 3
        in listOf("4", "EV", "VISTADOME AC") -> 4
        in listOf("5", "EC", "EXEC. CHAIR CAR") -> 5
        in listOf("6", "2A", "AC 2 TIER") -> 6
        in listOf("7", "FC", "FIRST CLASS") -> 7
        in listOf("8", "3A", "AC 3 TIER") -> 8
        in listOf("9", "3E", "AC 3 ECONOMY") -> 9
        in listOf("10", "VC", "VISTADOME CHAIR CAR") -> 10
        in listOf("11", "CC", "AC CHAIR CAR") -> 11
        in listOf("12", "SL", "SLEEPER") -> 12
        in listOf("13", "VS", "VISTADOME NON AC") -> 13
        in listOf("14", "2S", "SECOND SITTING") -> 14
        else -> 1
    }


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
                                    // 1. Remove popup for up to 1 minute or until found and clicked once
                                    val popupJob = launch {
                                        val end = System.currentTimeMillis() + 60 * 1000 // 1 minute
                                        var popupClicked = false
                                        while (System.currentTimeMillis() < end && !popupClicked) {
                                            popupClicked = removePopupOnce(this@apply)
                                            if (popupClicked) break
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
                                    // Use the new XPath for the sign-in button after captcha fill
                                    val buttonSelector = "//*[@id=\"login_header_disable\"]/div/div/div[2]/div[2]/div/div[2]/div/div[2]/form/span/button"
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
                                                // Check if captcha input is still present (means captcha failed)
                                                var captchaInput = document.querySelector("$inputFieldSelector");
                                                // Or check if sign-in button is still present (means not logged in)
                                                var signInBtn = document.evaluate('$buttonSelector', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                                                // Or check for error message (optional: customize selector as per IRCTC error)
                                                var error = document.querySelector('.errormsg, .error, .ng-star-inserted[style*="color:red"]');
                                                return !!captchaInput || !!signInBtn || !!error;
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
                                            delay(1200) // Give time for error to appear if captcha is wrong

                                            // Check if sign in button or captcha input still present (captcha failed)
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

                                    val journeyDate = inputDate  // user input in dd/MM/yyyy format

                                    val (targetDay, targetMonthNumber, targetYear) = journeyDate.split("/")

                                    val monthNames = listOf(
                                        "January", "February", "March", "April", "May", "June",
                                        "July", "August", "September", "October", "November", "December"
                                    )

                                    val targetMonthIndex = targetMonthNumber.toInt() - 1
                                    val targetMonth = monthNames[targetMonthIndex]  // e.g., "August"

                                    selectJourneyDate(
                                        webViewRef = this@apply,
                                        journeyDate = journeyDate,
                                        targetDay = targetDay,
                                        targetMonthName = targetMonth
                                    )

                                    var a: Long = (500..1000).random().toLong()
                                    delay(a)
                                    // Immediately expand and select class after date selection, no delay
                                    expandClass(this@apply)
                                    selectClass(this@apply, classIndex)

                                    expandQuota(this@apply)
                                    selectQuota(this@apply,quotaIndex)

                                    fillOrigin(this@apply,inputOrigin)
                                    fillDestination(this@apply,inputDestination)

                                    a= (500..1000).random().toLong()
                                    delay(a)
                                    searchButton(this@apply)
                                    selectTrain(this@apply,targetTrainNumber,targetClassCode)
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

private fun clickLoginButton(webView: WebView, buttonXPath: String) {
    val js = """
        (function() {
            const el = document.evaluate('$buttonXPath', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
            if (el) { el.click(); }
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

// Replace removePopup with removePopupOnce:
private fun removePopupOnce(webView: WebView): Boolean {
    var foundAndClicked = false
    val js = """
        (function() {
            if (!window.__popupClicked) {
                const el = document.querySelector("body > app-root > app-home > div.header-fix > app-header > p-dialog.ng-tns-c19-2 > div > div > div.ng-tns-c19-2.ui-dialog-content.ui-widget-content > div > form > div.text-center.col-xs-12 > button");
                if (el) {
                    el.click();
                    window.__popupClicked = true;
                    if (window.Android) Android.sendToAndroid("✅ Popup Removed");
                    return "CLICKED";
                } else {
                    if (window.Android) Android.sendToAndroid("❌ Popup not found");
                    return "NOT_FOUND";
                }
            } else {
                return "ALREADY_CLICKED";
            }
        })();
    """.trimIndent()
    // Synchronously check result (simulate with callback)
    webView.evaluateJavascript(js) { result ->
        if (result?.contains("CLICKED") == true) {
            foundAndClicked = true
        }
    }
    return foundAndClicked
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


suspend fun selectJourneyDate(
    webViewRef: WebView,
    journeyDate: String,
    targetDay: String,
    targetMonthName: String
) {
    // Step 1: Click the input to open calendar (your working code)
    val openCalendarScript = """
        javascript:(function () {
            const input = document.querySelector(
                '#jDate input, input[placeholder="DD/MM/YYYY"], input[formcontrolname="journeyDate"], input[formcontrolname="journeyDateInput"], input.ui-inputtext[role="textbox"]'
            );
            if (input) {
                input.focus();
                input.click();
                Android.sendToAndroid("✅ Calendar input clicked");
            } else {
                Android.sendToAndroid("❌ Calendar input not found");
            }
        })();
    """.trimIndent()
    webViewRef.evaluateJavascript(openCalendarScript, null)

    delay(500) // ✅ Give the calendar time to load

    // Step 2: Run month adjust + date picker
    val dateSelectionScript = """
        javascript:(function() {
            const targetMonth = "$targetMonthName";
            const targetDate = $targetDay;

            function log(msg) {
                if (window.Android) Android.sendToAndroid(msg);
                console.log(msg);
            }

            const monthXPath = "//*[@id='jDate']/span/div/div/div[1]/div/span[1]";
            const result = document.evaluate(monthXPath, document, null, XPathResult.STRING_TYPE, null);
            const currentMonth = result.stringValue.trim();

            log(`📅 Current month: ${'$'}{currentMonth}`);

            if (currentMonth !== targetMonth) {
                log(`🔁 Month mismatch (${'$'}{currentMonth} ≠ ${'$'}{targetMonth}). Switching month...`);
                const nextMonthXPath = "//*[@id='jDate']/span/div/div/div[1]/a[2]/span";
                const nextMonthNode = document.evaluate(nextMonthXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                if (nextMonthNode) {
                    nextMonthNode.click();
                    setTimeout(arguments.callee, 500);
                    return;
                } else {
                    log("❌ Next month arrow not found");
                    return;
                }
            }

            log(`✅ Month matches. Finding date...`);

            const baseXPath = "//*[@id='jDate']/span/div/div/div[2]/table/tbody/tr[1]/td";
            let firstDate = null;
            let firstTd = -1;

            for (let td = 1; td <= 7; td++) {
                const enabledXPath = baseXPath + "[" + td + "]/a";
                const node = document.evaluate(enabledXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                if (node) {
                    firstDate = parseInt(node.textContent.trim());
                    firstTd = td;
                    break;
                }
            }

            if (firstDate === null) {
                log("❌ No enabled date found in first row");
                return;
            }

            const dayOffset = targetDate - firstDate;
            const cellIndex = firstTd - 1 + dayOffset;
            const row = Math.floor(cellIndex / 7) + 1;
            const col = (cellIndex % 7) + 1;

            const targetXPath = "//*[@id='jDate']/span/div/div/div[2]/table/tbody/tr[" + row + "]/td[" + col + "]/a";
            const targetNode = document.evaluate(targetXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;

            if (targetNode) {
                targetNode.click();
                log(`✅ Clicked on date ${'$'}{targetNode.textContent.trim()} at tr[${'$'}{row}], td[${'$'}{col}]`);
            } else {
                log(`❌ Date $targetDay not found at tr[${'$'}{row}], td[${'$'}{col}]`);
            }
        })();
    """.trimIndent()
    webViewRef.evaluateJavascript(dateSelectionScript, null)

    // Step 3: Confirm the input is filled
    val isDateSelected = suspendCancellableCoroutine<Boolean> { cont ->
        CoroutineScope(Dispatchers.Main).launch {
            repeat(20) {
                val checkScript = """
                    javascript:(function(){
                        const inp = document.querySelector('#jDate input');
                        return inp ? inp.value : "";
                    })();
                """.trimIndent()

                val currentVal = suspendCancellableCoroutine<String> { innerCont ->
                    webViewRef.evaluateJavascript(checkScript) { result ->
                        innerCont.resume(result?.trim('"') ?: "")
                    }
                }

                if (currentVal.contains(journeyDate)) {
                    cont.resume(true)
                    return@launch
                }

                delay(300)
            }
            cont.resume(false)
        }
    }

    if (isDateSelected) {
        Log.d("DatePicker", "✅ Date confirmed in input: $journeyDate")
    } else {
        Log.d("DatePicker", "❌ Date not selected within timeout")
    }
}

private fun expandClass(webView: WebView) {
    val js = """
                    javascript:(function() {
                        const el = document.querySelector("#journeyClass > div > div.ui-dropdown-trigger.ui-state-default.ui-corner-right.ng-tns-c65-11 > span");
                        if (el) {
                            el.click();
                            Android.sendToAndroid("✅ DropDown Clicked");
                        } else {
                            Android.sendToAndroid("❌ DropDown Not Found");
                        }
                    })();
                """.trimIndent()
    webView.evaluateJavascript(js, null)
}

private fun selectClass(webView: WebView, classIndex: Int) {
    val js = """
    javascript:(function() {
        const classIndex = $classIndex;
        const xpath = "//*[@id='journeyClass']/div/div[4]/div/ul/p-dropdownitem[" + classIndex + "]/li/span";
        const result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
        const el = result.singleNodeValue;

        if (el) {
            el.click();
            Android.sendToAndroid("✅ Class selected using XPath at index " + classIndex);
        } else {
            Android.sendToAndroid("❌ Class not found at XPath index " + classIndex);
        }
    })();
""".trimIndent()

    webView.evaluateJavascript(js, null)
}

// Expand For Quota
private fun expandQuota(webView: WebView) {
    val js = """
                    javascript:(function() {
                        const el = document.querySelector("#journeyQuota > div > div.ui-dropdown-trigger.ui-state-default.ui-corner-right.ng-tns-c65-12");
                        if (el) {
                            el.click();
                            Android.sendToAndroid("✅ DropDown Clicked");
                        } else {
                            Android.sendToAndroid("❌ DropDown Not Found");
                        }
                    })();
                """.trimIndent()
    webView.evaluateJavascript(js, null)
}

// Fill Quota

private fun selectQuota(webView: WebView, quotaIndex: Int) {
    val js = """
        javascript:(function() {
            const quotaXPath = "//*[@id='journeyQuota']/div/div[4]/div/ul/p-dropdownitem[$quotaIndex]/li/span";
            const quotaResult = document.evaluate(quotaXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
            const quotaItem = quotaResult.singleNodeValue;

            if (quotaItem) {
                quotaItem.click();
                Android.sendToAndroid("✅ Quota index $quotaIndex selected.");

                setTimeout(function() {
                    let confirmXPath;
                    if ($quotaIndex === 3 || $quotaIndex === 4) {
                        confirmXPath = "//*[@id='divMain']/div/app-main-page/div/div/div[1]/div[2]/div[1]/app-jp-input/p-confirmdialog/div/div/div[3]/button/span[2]";
                    } else if ($quotaIndex === 5) {
                        confirmXPath = "//*[@id='divMain']/div/app-main-page/div/div/div[1]/div[2]/div[1]/app-jp-input/p-confirmdialog/div/div/div[3]/button[1]/span[2]";
                    } else {
                        Android.sendToAndroid("⚠️ No confirm button logic defined for quota index " + $quotaIndex);
                        return;
                    }

                    let attempts = 0;
                    const maxAttempts = 10;

                    function tryClickConfirm() {
                        const confirmResult = document.evaluate(confirmXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
                        const confirmButton = confirmResult.singleNodeValue;

                        if (confirmButton) {
                            confirmButton.click();
                            Android.sendToAndroid("✅ Confirm button clicked for quota index $quotaIndex");
                        } else if (attempts < maxAttempts) {
                            attempts++;
                            setTimeout(tryClickConfirm, 400);
                            Android.sendToAndroid("⌛ Waiting for confirm button (attempt " + attempts + ")");
                        } else {
                            Android.sendToAndroid("❌ Confirm button not found after " + maxAttempts + " attempts for quota index $quotaIndex");
                        }
                    }

                    tryClickConfirm();
                }, 600);
            } else {
                Android.sendToAndroid("❌ Quota index $quotaIndex not found in dropdown.");
            }
        })();
    """.trimIndent()

    webView.evaluateJavascript(js, null)
}


private fun fillOrigin(webView: WebView, inputOrigin: String) {
    val js = """
    javascript:(function() {
        const xpath = "//*[@id='origin']/span/input";
        const result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
        const input = result.singleNodeValue;

        if (input) {
            input.value = "$inputOrigin";
            input.dispatchEvent(new Event('input', { bubbles: true }));
            input.dispatchEvent(new Event('change', { bubbles: true }));
            input.blur();
            Android.sendToAndroid('✅ Origin input filled using XPath');
        } else {
            Android.sendToAndroid('❌ Origin input not found via XPath');
        }
    })();
""".trimIndent()
    webView.evaluateJavascript(js, null)
}

private fun fillDestination(webView: WebView, inputDestination: String) {
    val js = """
    javascript:(function() {
        const xpath = "//*[@id='destination']/span/input";
        const result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
        const input = result.singleNodeValue;

        if (input) {
            input.value = "$inputDestination";
            input.dispatchEvent(new Event('input', { bubbles: true }));
            input.dispatchEvent(new Event('change', { bubbles: true }));
            input.blur();
            Android.sendToAndroid('✅ Destination input filled using XPath');
        } else {
            Android.sendToAndroid('❌ Destination input not found via XPath');
        }
    })();
""".trimIndent()
    webView.evaluateJavascript(js, null)
}


private fun searchButton(webView: WebView) {
    val js = """
        javascript:(function() {
            const btn = document.querySelector(".search_btn.train_Search");
            if (btn) {
                btn.click();
                Android.sendToAndroid("✅ Search Button Clicked using selector");
            } else {
                Android.sendToAndroid("❌ Search Button Not Found using selector");
            }
        })();
    """.trimIndent()

    webView.evaluateJavascript(js, null)
}

class JSBridge {

    @JavascriptInterface
    fun sendToAndroid(message: String) {
        Log.d("JS_LOG", message)  // This prints the log from JavaScript to Logcat
    }
}

private fun selectTrain(webView: WebView, targetTrainNumber: String, targetClassCode: String) {
    webView.addJavascriptInterface(JSBridge(), "Android")  // Ensure JSBridge is set

    val js = """
        javascript:(function() {
            const trainNumber = "${targetTrainNumber}";
            const expectedClass = "${targetClassCode}";
            const maxAttempts = 30;
            let attempts = 0;

            function waitForTrainList(readyCallback) {
                const xpath = "//*[@id='divMain']/div/app-train-list/div[4]/div[3]/div[3]/span[1]/button[1]";
                const result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                if (result) {
                    Android.sendToAndroid("✅ Train list loaded, starting search...");
                    readyCallback();
                } else {
                    setTimeout(() => waitForTrainList(readyCallback), 300);
                }
            }

            function waitForLoaderToDisappear(callback) {
                const loader = document.getElementById("loaderP") || document.getElementById("preloaderP");
                if (!loader || loader.style.display === "none" || loader.hidden ||
                    getComputedStyle(loader).visibility === "hidden" || getComputedStyle(loader).opacity === "0") {
                    Android.sendToAndroid("✅ Loader gone, proceeding...");
                    callback();
                } else {
                    Android.sendToAndroid("⏳ Loader still visible, waiting...");
                    setTimeout(() => waitForLoaderToDisappear(callback), 200);
                }
            }

            function tryFindAndClick() {
                let foundTrain = false;

                for (let a = 1; a <= 40; a++) {
                    const trainXPath = "//*[@id='divMain']/div/app-train-list/div[4]/div[3]/div[5]/div[" + a + "]/div[1]/app-train-avl-enq/div[1]/div[1]/div[1]/strong";
                    const trainNode = document.evaluate(trainXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;

                    if (trainNode && trainNode.textContent.includes(trainNumber)) {
                        Android.sendToAndroid("✅ Found train '" + trainNumber + "' at index a=" + a);
                        foundTrain = true;

                        for (let b = 1; b <= 13; b++) {
                            const classXPath = "//*[@id='divMain']/div/app-train-list/div[4]/div[3]/div[5]/div[" + a + "]/div[1]/app-train-avl-enq/div[1]/div[5]/div[1]/table/tr/td[" + b + "]/div/div[1]/strong";
                            const classNode = document.evaluate(classXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;

                            if (classNode && classNode.textContent.trim().toUpperCase().includes(expectedClass)) {
                                Android.sendToAndroid("🔍 Checking a=" + a + ", b=" + b + " for class '" + expectedClass + "'");

                                const clickTarget = classNode.closest("td")?.querySelector("div");
                                if (clickTarget) {
                                    clickTarget.click();
                                    Android.sendToAndroid("✅ Clicked class '" + expectedClass + "'");

                                    const dateDivXPath = "//*[@id='divMain']/div/app-train-list/div[4]/div[3]/div[5]/div[" + a + "]/div[1]/app-train-avl-enq/div[1]/div[7]/div[1]/div[3]/table/tr/td[2]/div";

                                    function waitForDateCellAndClick() {
                                        const dateDivNode = document.evaluate(dateDivXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                                        if (dateDivNode) {
                                            dateDivNode.click();
                                            Android.sendToAndroid("✅ Clicked date availability div for train " + trainNumber);
                                            setTimeout(afterDateClick, 500);
                                        } else {
                                            Android.sendToAndroid("❌ Date cell not found, retrying...");
                                            setTimeout(waitForDateCellAndClick, 300);
                                        }
                                    }

                                    function afterDateClick() {
                                        const directBookNowNode = document.querySelector(".btnDefault.train_Search.ng-star-inserted:not(.disable-book)");
                                        const retryBookNowNode = document.querySelector(".btnDefault.train_Search.ng-star-inserted.disable-book");

                                        if (directBookNowNode) {
                                            directBookNowNode.click();
                                            Android.sendToAndroid("✅ Found direct Book Now button by class, clicked");
                                            return;
                                        }

                                        if (retryBookNowNode) {
                                            Android.sendToAndroid("🔁 Book Now not ready (disable-book class), retrying using fallback XPath...");
                                            Android.sendToAndroid("➡️ Retrying with a=" + a + ", b=" + b);

                                            const fallbackXPath = "//*[@id='divMain']/div/app-train-list/div[4]/div[3]/div[5]/div[" + a + "]/div[1]/app-train-avl-enq/div[1]/div[7]/div[1]/p-tabmenu/div/ul/li[" + b + "]/a/div/div/strong/span[2]";
                                            const fallbackNode = document.evaluate(fallbackXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;

                                            if (fallbackNode) {
                                            fallbackNode.closest("li").querySelector("a").click();
                                            Android.sendToAndroid("✅ Clicked fallback class tab at a=" + a + ", b=" + b);
                                        
                                            // ⏳ Wait for loader to disappear, THEN click date cell
                                            function waitForLoaderToDisappear(callback) {
                                                setTimeout(() => {
                                                    const loader = document.getElementById("loaderP") || document.getElementById("preloaderP");
                                        
                                                    if (!loader || loader.style.display === "none" || loader.hidden ||
                                                        getComputedStyle(loader).visibility === "hidden" ||
                                                        getComputedStyle(loader).opacity === "0") {
                                        
                                                        Android.sendToAndroid("✅ Loader gone, proceeding...");
                                                        callback();
                                        
                                                    } else {
                                                        Android.sendToAndroid("⏳ Loader still visible, waiting...");
                                                        setTimeout(() => waitForLoaderToDisappear(callback), 200);
                                                    }
                                                }, 500); // ✅ initial wait before first check
                                            }
                                        
                                            // ✅ Now call the wait function and then your logic to click on the date cell
                                            waitForLoaderToDisappear(() => {
                                                const dateDivRetryNode = document.evaluate(dateDivXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                                                if (dateDivRetryNode) {
                                                    dateDivRetryNode.click();
                                                    Android.sendToAndroid("✅ Re-clicked date availability div after fallback");
                                                    setTimeout(afterDateClick, 500);
                                                } else {
                                                    Android.sendToAndroid("❌ Date cell not found during fallback retry");
                                                }
                                            });
                                        
                                        } else {
                                            Android.sendToAndroid("❌ Fallback class tab XPath not found");
                                        }
                                        
                                        } else {
                                            Android.sendToAndroid("❌ No Book Now button found");
                                        }
                                    }

                                    waitForDateCellAndClick();
                                    break;
                                }
                            }
                        }

                        break;
                    }
                }

                if (!foundTrain && attempts < maxAttempts) {
                    attempts++;
                    Android.sendToAndroid("🔄 Retrying train search... attempt " + attempts);
                    setTimeout(tryFindAndClick, 400);
                } else if (!foundTrain) {
                    Android.sendToAndroid("❌ Train number '" + trainNumber + "' not found after " + maxAttempts + " attempts");
                }
            }

            waitForTrainList(tryFindAndClick);
        })();
    """.trimIndent()

    webView.evaluateJavascript(js, null)
}
