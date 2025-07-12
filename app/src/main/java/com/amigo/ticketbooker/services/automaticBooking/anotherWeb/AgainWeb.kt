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
import kotlin.String
import kotlin.coroutines.resume
import kotlin.random.Random


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun IrctcWebViewScreen(
    inputUserName: String = "Shrisha2808",
    inputPassword: String = "Shrisha@2808",
    inputDate: String = "02/09/2025",
    quotaName: String = "1",
    className: String = "SL",
    inputOrigin: String = "BAPUDM MOTIHARI - BMKI",
    inputDestination: String = "ANAND VIHAR TRM - ANVT (NEW DELHI)",
    targetTrainNumber: String = "12557",
    passengerCount: Int = 6,
    p1Name: String = "Omshree",
    p1Age: String = "23",
    p1Gender: String = "M",
    p1Seat: String = "L",
    p2Name: String = "Omshree",
    p2Age: String = "20",
    p2Gender: String = "F",
    p2Seat: String = "L",
    p3Name: String = "Abhishek",
    p3Age: String = "20",
    p3Gender: String = "M",
    p3Seat: String = "SU",
    p4Name: String = "Mina Devi",
    p4Age: String = "47",
    p4Gender: String = "F",
    p4Seat: String = "SL",
    p5Name: String = "Ram Pravesh",
    p5Age: String = "54",
    p5Gender: String = "M",
    p5Seat: String = "M",
    p6Name: String = "Nishu",
    p6Age: String = "30",
    p6Gender: String = "F",
    p6Seat: String = "U",
    passengerMobileNumber: String = "7302221097",
    autoUpgradeOption: String = "1", // "1" = check, "0" = leave unchecked
    confirmBerth: String = "1", // "1" = check, "0" = leave unchecked
    travelInsurance: String = "2",
    paymentOptionNo: Int = 2,
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

    val targetClassCode = when (classIndex) {
        2 -> "EA"
        3 -> "1A"
        4 -> "EV"
        5 -> "EC"
        6 -> "2A"
        7 -> "FC"
        8 -> "3A"
        9 -> "3E"
        10 -> "VC"
        11 -> "CC"
        12 -> "SL"
        13 -> "VS"
        14 -> "2S"
        else -> null  // or "UNKNOWN", or throw Exception("Invalid class index")
    }

    val paymentOption = when (paymentOptionNo) {
        1 -> "CARD_NETBANKING"
        2 -> "UPI"
        else -> null  // or "UNKNOWN", or throw Exception("Invalid class index")
    }

    fun getGenderCode(gender: String?): String {
        return when (gender?.uppercase()) {
            "M" -> "2"
            "F" -> "3"
            "T" -> "4"
            else -> "1"
        }
    }

    val p1GenderNo = getGenderCode(p1Gender)
    val p2GenderNo = getGenderCode(p2Gender)
    val p3GenderNo = getGenderCode(p3Gender)
    val p4GenderNo = getGenderCode(p4Gender)
    val p5GenderNo = getGenderCode(p5Gender)
    val p6GenderNo = getGenderCode(p6Gender)

    fun getSeatCode(seat: String?): String {
        return when (seat?.uppercase()) {
            "L" -> "2"
            "M" -> "3"
            "U" -> "4"
            "SL" -> "5"
            "SU" -> "6"
            else -> "1"
        }
    }

    val p1SeatNo = getSeatCode(p1Seat)
    val p2SeatNo = getSeatCode(p2Seat)
    val p3SeatNo = getSeatCode(p3Seat)
    val p4SeatNo = getSeatCode(p4Seat)
    val p5SeatNo = getSeatCode(p5Seat)
    val p6SeatNo = getSeatCode(p6Seat)

    var statusMessage by remember { mutableStateOf("Loading...") }
    var hasLoggedIn by remember { mutableStateOf(false) }
    var initialActionsDone by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.weight(1f), factory = { context ->
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
                            Log.d("LoginFlow", message)
                            statusMessage = message
                        }
                    }, "Android")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d("IRCTC_WEBVIEW", "Page loaded: $url")

                            if (!hasLoggedIn && !initialActionsDone) {
                                initialActionsDone = true
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
                                    delay(Random.nextLong(100L, 1000L))

                                    // 3. Fill username and password ONCE
                                    enterUsername(this@apply, inputUserName)
                                    enterPassword(this@apply, inputPassword)
                                    delay(400)

                                    // 4. Captcha solve loop
                                    val captchaImageSelector = ".captcha-img"
                                    val inputFieldSelector = "input[formcontrolname='captcha']"
                                    // Use the new XPath for the sign-in button after captcha fill
                                    val buttonSelector =
                                        "//*[@id=\"login_header_disable\"]/div/div/div[2]/div[2]/div/div[2]/div/div[2]/form/span/button"
                                    val refreshButtonXPath =
                                        "//*[@id=\"login_header_disable\"]/div/div/div[2]/div[2]/div/div[2]/div/div[2]/form/div[5]/div/app-captcha/div/div/div[2]/span[2]/a/span"

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
                                        fillCaptchaInput(
                                            this@apply, inputFieldSelector, captchaText
                                        )
                                        delay(500)
                                        clickLoginButton(this@apply, buttonSelector)
                                    }

                                    suspend fun isSignInElementsPresent(): Boolean {
                                        delay(500)
                                        val js = """
                                            (function() {
                                                var captchaInput = document.querySelector("$inputFieldSelector");
                                                var signInBtn = document.evaluate('$buttonSelector', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                                                var both = !!captchaInput && !!signInBtn;
                                                if (both && window.Android) Android.sendToAndroid('🔍 Captcha & Sign-in present');
                                                return both;
                                            })();
                                        """.trimIndent()
                                        return suspendCancellableCoroutine { cont ->
                                            this@apply.evaluateJavascript(js) { result ->
                                                cont.resume(result == "true", null)
                                            }
                                        }
                                    }

                                    suspend fun WebView.isLoggedIn(): Boolean {
                                        delay(500)
                                        val js = """
                                            (function(){
                                                const cssSel = "#slide-menu > p-sidebar > div > nav > div > div > span:nth-child(3) > a > span > label > b";
                                                const xpathSel = "//*[@id='slide-menu']/p-sidebar/div/nav/div/div/span[2]/a/span/label/b";
                                                var logout = document.querySelector(cssSel);
                                                if (!logout) {
                                                    logout = document.evaluate(xpathSel, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                                                }
                                                if (logout) {
                                                    var style = window.getComputedStyle(logout);
                                                    var visible = (style.display !== 'none' && style.visibility !== 'hidden' && style.opacity !== '0' && logout.offsetParent !== null);
                                                    if (visible) {
                                                        if (window.Android) Android.sendToAndroid('🔍 Logout button is visible');
                                                        return true;
                                                    }
                                                }
                                                return false;
                                            })();
                                        """.trimIndent()

                                        return suspendCancellableCoroutine { cont ->
                                            this.evaluateJavascript(js) { result ->
                                                cont.resume(result == "true", null)
                                            }
                                        }
                                    }


                                    suspend fun solveCaptchaAndSignIn(): Boolean {
                                        val maxRetries = 15 // Change this value as needed
                                        var loggedIn = false
                                        var retries = 0
                                        while (retries < maxRetries) {
                                            retries++
                                            // Download captcha and store link
                                            val captchaUrl = getCaptchaUrl()
                                            lastCaptchaUrl = captchaUrl

                                            // OCR
                                            val captchaText =
                                                suspendCancellableCoroutine<String> { cont ->
                                                    downloadAndRecognizeCaptcha(
                                                        context, captchaUrl
                                                    ) { text ->
                                                        cont.resume(text, null)
                                                    }
                                                }
                                            if (captchaText.isEmpty()) {
                                                refreshCaptcha(this@apply, refreshButtonXPath)
                                                delay(1000)
                                                continue
                                            }

                                            statusMessage = "Retry #$retries"
                                            // Fill captcha and attempt sign-in
                                            fillCaptchaAndSignIn(captchaText)
                                            // Wait for the IRCTC loader animation to finish
                                            this@apply.waitForLoaderToFinish()
                                            // Small buffer after loader disappears
                                            delay(100)

                                            // Check if sign in button or captcha input still present (captcha failed)
                                            if (isSignInElementsPresent()) {
                                                // Get captcha url again
                                                val newCaptchaUrl = getCaptchaUrl()
                                                if (newCaptchaUrl == lastCaptchaUrl) {
                                                    // Refresh until captcha changes
                                                    var refreshed = false
                                                    repeat(5) {
                                                        refreshCaptcha(
                                                            this@apply, refreshButtonXPath
                                                        )
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
                                                // Open sidebar menu so logout button becomes visible
                                                menuClick(this@apply)
                                                delay(600)
                                                if (isLoggedIn()) {
                                                    statusMessage = "✅ Logged in successfully."
                                                    loggedIn = true
                                                    menuClick(this@apply)
                                                    break
                                                } else {
                                                    // New captcha logged in yet, retry
                                                    continue
                                                }
                                            }
                                        }
                                        if (retries >= maxRetries) {
                                            statusMessage = "❌ Max captcha retries reached."
                                        }
                                        return loggedIn
                                    }

                                    val loginSuccess = solveCaptchaAndSignIn()
                                    if (!loginSuccess) {
                                        statusMessage = "Login failed. Stopping automation"
                                        return@launch
                                    }
                                    popupJob.cancel()
                                    hasLoggedIn = true

                                    val journeyDate = inputDate  // user input in dd/MM/yyyy format

                                    val (targetDay, targetMonthNumber, targetYear) = journeyDate.split(
                                        "/"
                                    )

                                    val monthNames = listOf(
                                        "January",
                                        "February",
                                        "March",
                                        "April",
                                        "May",
                                        "June",
                                        "July",
                                        "August",
                                        "September",
                                        "October",
                                        "November",
                                        "December"
                                    )

                                    val targetMonthIndex = targetMonthNumber.toInt() - 1
                                    val targetMonth =
                                        monthNames[targetMonthIndex]  // e.g., "August"

                                    delay(Random.nextLong(100L, 1000L))
                                    selectJourneyDate(
                                        webViewRef = this@apply,
                                        journeyDate = journeyDate,
                                        targetDay = targetDay,
                                        targetMonthName = targetMonth
                                    )

                                    expandClass(this@apply)
                                    selectClass(this@apply, classIndex)

                                    expandQuota(this@apply)
                                    selectQuota(this@apply, quotaIndex)

                                    delay(Random.nextLong(100L, 1000L))
                                    fillOrigin(this@apply, inputOrigin)
                                    fillDestination(this@apply, inputDestination)

                                    delay(Random.nextLong(800L, 1000L))
                                    searchButton(this@apply)
                                    this@apply.waitForLoaderToFinish()
                                    selectTrain(this@apply, targetTrainNumber, targetClassCode)
                                    this@apply.waitForLoaderToFinish()

                                    // Wait for verification text before filling mobile number
                                    val verificationSuccess = verificationTextPassengerDetails(this@apply)
                                    if (verificationSuccess) {
                                        addNewPassenger(this@apply,passengerCount)
                                        mobileNumber(this@apply, passengerMobileNumber)
                                        autoUpgrade(this@apply, autoUpgradeOption)
                                        confirmBerth(this@apply, confirmBerth)
                                        selectTravelInsurance(this@apply, travelInsurance)
                                        selectPaymentOption(this@apply, paymentOption)
                                        delay(Random.nextLong(100L, 700L))
                                    } else {
                                        statusMessage = "❌ Passenger details verification text not found."
                                        return@launch
                                    }



                                    CoroutineScope(Dispatchers.Main).launch {
                                        val passengerList = listOf(
                                            Triple(p1Name, p1Age, p1GenderNo) to p1SeatNo,
                                            Triple(p2Name, p2Age, p2GenderNo) to p2SeatNo,
                                            Triple(p3Name, p3Age, p3GenderNo) to p3SeatNo,
                                            Triple(p4Name, p4Age, p4GenderNo) to p4SeatNo,
                                            Triple(p5Name, p5Age, p5GenderNo) to p5SeatNo,
                                            Triple(p6Name, p6Age, p6GenderNo) to p6SeatNo
                                        )

                                        for (i in 0 until passengerCount) {
                                            val (nameAgeGender, seat) = passengerList[i]
                                            val (name, age, gender) = nameAgeGender

                                            // Await until all fields are filled for this passenger
                                            // (passengerDetails must be a suspend function)
                                            passengerDetails(
                                                this@apply,
                                                i + 1,
                                                name,
                                                age,
                                                gender,
                                                seat
                                            )
                                        }
                                        delay(Random.nextLong(900L, 1300L))
                                        // Only after all passengers are filled, continue
                                        continueAfterPassengerDetails(this@apply)
                                        this@apply.waitForLoaderToFinish()
                                    }

//                                    statusMessage = "🎉 Automation flow finished"
                                    return@launch
                                }
                            }
                        }
                    }
                    loadUrl("https://www.irctc.co.in/nget/train-search")
                }
            })
        Text(
            text = statusMessage,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

private const val CHROME_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 13; Pixel 6 Pro) " + "AppleWebKit/537.36 (KHTML, like Gecko) " + "Chrome/114.0.5735.199 Mobile Safari/537.36"

private fun downloadAndRecognizeCaptcha(
    context: Context, imageUrl: String, onResult: (String) -> Unit
) {
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
            val binarizedColor =
                if (brightness < 128) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            binarized[x, y] = binarizedColor
        }
    }

    return binarized
}

// Extension to wait for IRCTC loading animation to disappear before proceeding.
suspend fun WebView.waitForLoaderToFinish(
    loaderSelector: String = ".my-loading.ng-star-inserted",
    appearTimeoutMs: Long = 500L,
    checkIntervalMs: Long = 10L
) {
    withContext(Dispatchers.Main) {
        val appearStart = System.currentTimeMillis()

        // 1. Wait up to 500ms for loader to appear
        while (System.currentTimeMillis() - appearStart < appearTimeoutMs) {
            val js = """
                (function() {
                    const loader = document.querySelector("$loaderSelector");
                    if (!loader) return "not_found";
                    const style = window.getComputedStyle(loader);
                    return (style.display !== 'none' && style.visibility !== 'hidden' && style.opacity !== '0') ? "visible" : "hidden";
                })();
            """.trimIndent()

            val status = suspendCancellableCoroutine<String> { cont ->
                evaluateJavascript(js) { result ->
                    cont.resume(result.trim('"'), null)
                }
            }

            if (status == "visible") {
                Log.d("LoginFlow", "✅ Loader appeared — now waiting for it to disappear.")

                // 2. Wait indefinitely for loader to disappear
                while (true) {
                    val disappearJs = """
                        (function() {
                            const loader = document.querySelector("$loaderSelector");
                            if (!loader) return "not_found";
                            const style = window.getComputedStyle(loader);
                            return (style.display !== 'none' && style.visibility !== 'hidden' && style.opacity !== '0') ? "visible" : "hidden";
                        })();
                    """.trimIndent()

                    val disappearStatus = suspendCancellableCoroutine<String> { cont2 ->
                        evaluateJavascript(disappearJs) { result ->
                            cont2.resume(result.trim('"'), null)
                        }
                    }

                    if (disappearStatus == "hidden" || disappearStatus == "not_found") {
                        Log.d("LoginFlow", "✅ Loader disappeared.")
                        return@withContext
                    }

                    delay(checkIntervalMs)
                }
            }

            delay(checkIntervalMs)
        }

        Log.d("LoginFlow", "⏩ Loader never appeared within 100ms — skipping wait.")
    }
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

    webView.evaluateJavascript(js, null)
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
// Step 1: Open the calendar
    val openCalendarScript = """
    javascript: (function () {
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
    delay(500) // Give the calendar time to open

// Step 2: Select the correct date
    val dateSelectionScript = """
    javascript: (function () {
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

        log(`✅ Month matches. Searching dates...`);

        let firstDate = null;
        let firstTd = -1;
        let firstRow = -1;

        outer: for (let row = 1; row <= 5; row++) {
            for (let td = 1; td <= 7; td++) {
                const cellXPath = "//*[@id='jDate']/span/div/div/div[2]/table/tbody/tr[" + row + "]/td[" + td + "]/a";
                const node = document.evaluate(cellXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                if (node) {
                    firstDate = parseInt(node.textContent.trim());
                    firstTd = td;
                    firstRow = row;
                    break outer;
                }
            }
        }

        if (firstDate === null) {
            log("❌ No enabled date found in calendar");
            return;
        }

        const totalOffset = targetDate - firstDate;
        const startIndex = (firstRow - 1) * 7 + (firstTd - 1);
        const targetIndex = startIndex + totalOffset;

        const targetRow = Math.floor(targetIndex / 7) + 1;
        const targetCol = (targetIndex % 7) + 1;

        const targetXPath = "//*[@id='jDate']/span/div/div/div[2]/table/tbody/tr[" + targetRow + "]/td[" + targetCol + "]/a";
        const targetNode = document.evaluate(targetXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;

        if (targetNode) {
            targetNode.click();
            log(`✅ Clicked on date ${'$'}{targetNode.textContent.trim()} at tr[${'$'}{targetRow}], td[${'$'}{targetCol}]`);
        } else {
            log(`❌ Date $targetDay not found at tr[${'$'}{targetRow}], td[${'$'}{targetCol}]`);
        }
    })();
    """.trimIndent()
    webViewRef.evaluateJavascript(dateSelectionScript, null)

// Step 3: Confirm the input was updated with the selected date
    val isDateSelected = suspendCancellableCoroutine<Boolean> { cont ->
        CoroutineScope(Dispatchers.Main).launch {
            repeat(20) {
                val checkScript = """
            javascript: (function () {
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

private fun selectTrain(webView: WebView, targetTrainNumber: String, targetClassCode: String?) {
    webView.addJavascriptInterface(JSBridge(), "Android")  // Ensure JSBridge is set

    val js = """
        javascript:(function() {
            const trainNumber = "$targetTrainNumber";
            const expectedClass = "$targetClassCode";
            const maxAttempts = 50;
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
                    setTimeout(() => waitForLoaderToDisappear(callback), 100);
                }
            }

            function tryFindAndClick() {
                let foundTrain = false;

                for (let a = 1; a <= 50; a++) {
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
                                            setTimeout(afterDateClick, 100);
                                        } else {
                                            Android.sendToAndroid("❌ Date cell not found, retrying...");
                                            setTimeout(waitForDateCellAndClick, 100);
                                        }
                                    }

                                    function afterDateClick() {
                                        const directBookNowNode = document.querySelector(".btnDefault.train_Search.ng-star-inserted:not(.disable-book)");
                                        const retryBookNowNode = document.querySelector(".btnDefault.train_Search.ng-star-inserted.disable-book");

                                        if (directBookNowNode) {
                                            directBookNowNode.click();
                                            Android.sendToAndroid("✅ Found direct Book Now button by class, clicked");
                                            waitForLoaderToDisappear(() => {
                                                Android.sendToAndroid("✅ Proceeded after direct Book Now loader");
                                                // You can continue to next steps here, if needed
                                            });
                                            return;
                                        }

                                        if (retryBookNowNode) {
                                            Android.sendToAndroid("🔁 Book Now not ready (disable-book class), retrying using fallback XPath...");
                                            Android.sendToAndroid("➡️ Retrying with a=" + a + ", b=" + b);

                                            const fallbackXPath = "//*[@id='divMain']/div/app-train-list/div[4]/div[3]/div[5]/div[" + a + "]/div[1]/app-train-avl-enq/div[1]/div[7]/div[1]/p-tabmenu/div/ul/li[" + b + "]/a/div/div";
                                            const fallbackNode = document.evaluate(fallbackXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;

                                            if (fallbackNode) {
                                            fallbackNode.click();
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
                                                        setTimeout(() => waitForLoaderToDisappear(callback), 100);
                                                    }
                                                }, 100); // ✅ initial wait before first check
                                            }
                                        
                                            // ✅ Now call the wait function and then your logic to click on the date cell
                                            waitForLoaderToDisappear(() => {
                                                const dateDivRetryNode = document.evaluate(dateDivXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                                                if (dateDivRetryNode) {
                                                    dateDivRetryNode.click();
                                                    Android.sendToAndroid("✅ Re-clicked date availability div after fallback");
                                                    setTimeout(afterDateClick, 200);
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


private fun passengerDetails(
    webView: WebView,
    passengerCount: Int,
    passengerName: String,
    passengerAge: String,
    passengerGender: String?,
    passengerSeatPreference: String?
) {
    val js = """
javascript:(function () {
    function fillAllFields() {
        function fillNameCharByChar(name, index, input) {
            if (index < name.length) {
                input.value += name.charAt(index);
                input.dispatchEvent(new Event('input', { bubbles: true }));
                setTimeout(function () {
                    fillNameCharByChar(name, index + 1, input);
                }, 10); // delay between characters
            } else {
                Android.sendToAndroid("✅ Passenger name fully typed as: " + name);
                // Proceed to next fields
                fillAge();
                fillGender();
                fillBerth();
            }
        }

        function fillName() {
            const xpath = "//*[@id='ui-panel-13-content']/div/div[${passengerCount}]/div[2]/div/app-passenger/div/div[1]/span/div[1]/p-autocomplete/span/input";
            const result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;

            if (result) {
                result.value = ""; // clear field
                result.focus();
                Android.sendToAndroid("✍️ Typing name character-by-character...");
                fillNameCharByChar("${passengerName}", 0, result);
            } else {
                Android.sendToAndroid("❌ Name input not found, retrying...");
                setTimeout(fillName, 300);
            }
        }

        function fillAge() {
            const xpath = "//*[@id='ui-panel-13-content']/div/div[${passengerCount}]/div[2]/div/app-passenger/div/div[1]/span/div[2]/input";
            const ageInput = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;

            if (ageInput) {
                ageInput.value = "${passengerAge}";
                ageInput.dispatchEvent(new Event('input', { bubbles: true }));
                Android.sendToAndroid("✅ Passenger age set to ${passengerAge}");
            } else {
                Android.sendToAndroid("❌ Age input not found, retrying...");
                setTimeout(fillAge, 300);
            }
        }

        function fillGender() {
            const g = ${passengerGender ?: "null"};
            if (!g) {
                Android.sendToAndroid("⚠️ Gender not provided, skipping gender selection.");
                return;
            }

            const optionXPath = "//*[@id='ui-panel-13-content']/div/div[${passengerCount}]/div[2]/div/app-passenger/div/div[1]/span/div[3]/select/option[" + g + "]";
            const optionNode = document.evaluate(optionXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;

            if (optionNode && optionNode.parentElement) {
                const selectElement = optionNode.parentElement;
                selectElement.value = optionNode.value;
                selectElement.dispatchEvent(new Event('change', { bubbles: true }));
                Android.sendToAndroid("✅ Gender selected index " + g + ": " + optionNode.value);
            } else {
                Android.sendToAndroid("❌ Gender option not found, retrying...");
                setTimeout(fillGender, 300);
            }
        }

        function fillBerth() {
            const b = ${passengerSeatPreference ?: "null"};
            if (!b) {
                Android.sendToAndroid("⚠️ Seat preference not provided, skipping berth selection.");
                return;
            }

            const optionXPath = "//*[@id='ui-panel-13-content']/div/div[${passengerCount}]/div[2]/div/app-passenger/div/div[1]/div[1]/select/option[" + b + "]";
            const optionNode = document.evaluate(optionXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;

            if (optionNode && optionNode.parentElement) {
                const selectElement = optionNode.parentElement;
                selectElement.value = optionNode.value;
                selectElement.dispatchEvent(new Event('change', { bubbles: true }));
                Android.sendToAndroid("✅ Berth selected index " + b + ": " + optionNode.value);
            } else {
                Android.sendToAndroid("❌ Berth option not found, retrying...");
                setTimeout(fillBerth, 300);
            }
        }

        fillName();
    }

    fillAllFields();
})();
""".trimIndent()

    webView.evaluateJavascript(js, null)
}


private fun mobileNumber(webView: WebView, passengerMobileNumber: String) {
    val js = """
javascript: (function () {
    function waitForInputAndSetValue() {
        var input = document.getElementById("mobileNumber");
        if (input) {
            input.value = "$passengerMobileNumber";
            var event = new Event('input', { bubbles: true });
            input.dispatchEvent(event);
            if (window.Android) Android.sendToAndroid("📱 Passenger mobile number set.");
        } else {
            setTimeout(waitForInputAndSetValue, 300);
        }
    }
    waitForInputAndSetValue();
})();
""".trimIndent()
    webView.evaluateJavascript(js, null)
}

private fun autoUpgrade(webView: WebView, autoUpgradeOption: String) {
    val js = """
javascript: (function () {
    function waitAndToggleCheckbox() {
        var checkbox = document.getElementById("autoUpgradation");
        if (checkbox) {
            var shouldCheck = "$autoUpgradeOption" === "1";
            if (checkbox.checked !== shouldCheck) {
                checkbox.click();
                Android.sendToAndroid("✅ Auto Upgradation checkbox toggled to: " + shouldCheck);
            } else {
                Android.sendToAndroid("✅ Auto Upgradation checkbox already in correct state.");
            }
        } else {
            setTimeout(waitAndToggleCheckbox, 300);
        }
    }
    waitAndToggleCheckbox();
})();
""".trimIndent()

    webView.evaluateJavascript(js, null)
}

private fun confirmBerth(webView: WebView, confirmBerth: String) {
    val js = """
javascript:(function () {
    function waitAndToggleConfirmBerthBox() {
        var checkbox = document.getElementById("confirmberths");
        if (checkbox) {
            var shouldCheck = "$confirmBerth" === "1";
            if (shouldCheck && !checkbox.checked) {
                checkbox.click();
                Android.sendToAndroid("✅ confirmBerth checkbox clicked (set to checked).");
            } else if (!shouldCheck && checkbox.checked) {
                checkbox.click();
                Android.sendToAndroid("✅ confirmBerth checkbox clicked (set to unchecked).");
            } else {
                Android.sendToAndroid("ℹ️ confirmBerth checkbox already in correct state.");
            }
        } else {
            setTimeout(waitAndToggleConfirmBerthBox, 300);
        }
    }
    waitAndToggleConfirmBerthBox();  // ✅ Correct function call
})();
""".trimIndent()
    webView.evaluateJavascript(js, null)
}


private fun selectTravelInsurance(webView: WebView, travelInsurance: String) {
    val js = """
javascript:(function () {
    function selectByXPath(xpath) {
        try {
            var result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
            return result.singleNodeValue;
        } catch (e) {
            return null;
        }
    }

    function waitAndSelectInsurance() {
        var yesXPath = "//*[@id='travelInsuranceOptedYes-0']/div/div[2]/span";
        var noXPath = "//*[@id='travelInsuranceOptedNo-0']/div/div[2]/span";

        var yesSpan = selectByXPath(yesXPath);
        var noSpan = selectByXPath(noXPath);

        if (yesSpan && noSpan) {
            if ("$travelInsurance" === "1") {
                yesSpan.click();
                Android.sendToAndroid("✅ Travel insurance: YES selected");
            } else {
                noSpan.click();
                Android.sendToAndroid("❌ Travel insurance: NO selected");
            }
        } else {
            setTimeout(waitAndSelectInsurance, 300);
        }
    }

    waitAndSelectInsurance();
})();
""".trimIndent()

    webView.evaluateJavascript(js, null)
}


private fun selectPaymentOption(webView: WebView, paymentOption: String?) {
    val js = """
javascript: (function () {
    function waitAndSelectPayment() {
        var cardNetbankingRadio = document.querySelector('p-radiobutton[id="3"] input[type="radio"][name="paymentType"]');
        var upiRadio = document.querySelector('p-radiobutton[id="2"] input[type="radio"][name="paymentType"]');

        if (cardNetbankingRadio && upiRadio) {
            if ("$paymentOption" === "CARD_NETBANKING") {
                cardNetbankingRadio.click();
                Android.sendToAndroid("✅ Car NetBanking option selected.");
            } else if ("$paymentOption" === "UPI") {
                upiRadio.click();
                Android.sendToAndroid("✅ UPI option selected.");
            } else {
                console.log("Invalid payment option: " + "$paymentOption");
            }
        } else {
            setTimeout(waitAndSelectPayment, 300); // Retry every 300ms
        }
    }
    waitAndSelectPayment();
})();
""".trimIndent()
    webView.evaluateJavascript(js, null)
}

private suspend fun addNewPassenger(webView: WebView, passengerCount: Int) {
    if (passengerCount <= 1) return

    withContext(Dispatchers.Main) {
        suspend fun clickAndWait(index: Int) {
            val jsClick = """
                (function() {
                    const xpath = "//span[contains(text(), 'Add Passenger')]/parent::a";
                    const btn = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                    if (btn) {
                        btn.click();
                        return true;
                    }
                    return false;
                })();
            """.trimIndent()

            // Try clicking until button is found and clicked
            while (true) {
                val clicked = suspendCancellableCoroutine<Boolean> { cont ->
                    webView.evaluateJavascript(jsClick) { res -> cont.resume(res == "true") }
                }
                if (clicked) break
                delay(100)
            }

            // Wait for the new passenger input to appear before next click
            val jsInput = """
                (function() {
                    const nameXpath = "//*[@id='ui-panel-13-content']/div/div[${index + 1}]/div[2]/div/app-passenger/div/div[1]/span/div[1]/p-autocomplete/span/input";
                    const input = document.evaluate(nameXpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                    return !!input;
                })();
            """.trimIndent()
            while (true) {
                val found = suspendCancellableCoroutine<Boolean> { cont ->
                    webView.evaluateJavascript(jsInput) { res -> cont.resume(res == "true") }
                }
                if (found) break
                delay(50)
            }
        }

        for (i in 1 until passengerCount) {
            clickAndWait(i)
        }
    }
}


private fun continueAfterPassengerDetails(webView: WebView) {
    val js = """
    javascript:(function clickByXPath() {
        const xpath = "//*[@id='psgn-form']/form/div/div[1]/div[16]/div/button[2]";
        const node = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;

        if (node) {
            node.click();
            Android.sendToAndroid("✅ Button clicked via XPath");
        } else {
            Android.sendToAndroid("❌ Button not found via XPath, retrying...");
            setTimeout(clickByXPath, 300);
        }
    })();
""".trimIndent()

    webView.evaluateJavascript(js, null)
}


suspend fun verificationTextPassengerDetails(webView: WebView): Boolean {
    return withContext(Dispatchers.Main) {
        val js = """
            (function() {
                const xpath = '//*[@id="divMain"]/div/app-passenger-input/div[1]/div/div[2]/strong';
                var result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                return !!result;
            })();
        """.trimIndent()
        repeat(20) {
            val found = suspendCancellableCoroutine<Boolean> { cont ->
                webView.evaluateJavascript(js) { res ->
                    cont.resume(res == "true")
                }
            }
            if (found) {
                webView.evaluateJavascript(
                    """if(window.Android) Android.sendToAndroid("✅ Passenger details Text found");""",
                    null
                )
                return@withContext true
            }
            delay(500)
        }
        webView.evaluateJavascript(
            """if(window.Android) Android.sendToAndroid("❌ Max attempts reached while waiting for passenger text.");""",
            null
        )
        false
    }
}