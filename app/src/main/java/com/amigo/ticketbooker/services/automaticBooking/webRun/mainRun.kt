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
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.graphics.scale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun MainAutomate(
    modifier: Modifier = Modifier,
    inputUserName: String = "anshuabhishek",
    inputPassword: String = "Amigo@2805",
    inputOrigin: String = "ANAND VIHAR TRM - ANVT",
    inputDestination: String = "BAPUDM MOTIHARI - BMKI ",
    inputDate: String = "15/08/2025",
    quotaName: String = "4",
    className: String = "12",
    trainNumber: Int = 12558
) {

    val quotaIndex = when (quotaName.trim().uppercase()) {
        in listOf("1", "GENERAL") -> 1
        in listOf("2", "LADIES") -> 2
        in listOf("3", "LOWER BERTH/SR.CITIZEN") -> 3
        in listOf("4", "PERSON WITH DISABILITY") -> 4
        in listOf("5", "DUTY PASS", "DUTYPASS") -> 5
        in listOf("6", "TATKAL", "TAT KAL") -> 6
        in listOf("7", "PREMIUM TATKAL", "PREMIUM-TATKAL") -> 7
        else -> 0
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
        else -> 1 // not 0 anymore
    }

    val className = when (classIndex) {
        2  -> "EA"
        3  -> "1A"
        4  -> "EV"
        5  -> "EC"
        6  -> "2A"
        7  -> "FC"
        8  -> "3A"
        9  -> "3E"
        10 -> "VC"
        11 -> "CC"
        12 -> "SL"
        13 -> "VS"
        14 -> "2S"
        else -> null  // or "UNKNOWN", or throw Exception("Invalid class index")
    }



    val journeyDate = inputDate  // user input in dd/MM/yyyy format

    val (targetDay, targetMonthNumber, targetYear) = journeyDate.split("/")

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val targetMonthIndex = targetMonthNumber.toInt() - 1
    val targetMonthName = monthNames[targetMonthIndex]  // e.g., "August"

    Log.d("ParsedDate", "Day: $targetDay, Month: $targetMonthName, Year: $targetYear")


    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var captchaText by remember { mutableStateOf("") } // To store extracted captcha
    var captchaImageUrl by remember { mutableStateOf("") } // For debugging or future use
    val contextForML = context.applicationContext
    val scope = rememberCoroutineScope()

    // Enhanced preprocessing: grayscale, resize, adaptive binarization
    fun adaptiveBinarize(bitmap: Bitmap, blockSize: Int = 12): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val binarized = createBitmap(width, height)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var sum = 0
                var count = 0
                for (dy in -blockSize / 2..blockSize / 2) {
                    for (dx in -blockSize / 2..blockSize / 2) {
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
                binarized[x, y] = binColor
            }
        }
        return binarized
    }

    fun enhancedPreprocessBitmap(bitmap: Bitmap): Bitmap {
        // 1. Grayscale
        val width = bitmap.width
        val height = bitmap.height
        val grayscale = createBitmap(width, height)
        val canvas = Canvas(grayscale)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        // 2. Resize if needed
        val targetHeight = 60
        val resized = if (height < targetHeight) {
            grayscale.scale((width * (targetHeight.toFloat() / height)).toInt(), targetHeight)
        } else {
            grayscale
        }
        // 3. Adaptive binarization
        return adaptiveBinarize(resized, blockSize = 12)
    }

    // Helper to download image and run ML Kit OCR
    fun processCaptchaImage(
        url: String,
        context: android.content.Context,
        onResult: (String) -> Unit
    ) {
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val bitmap = if (url.startsWith("data:image")) {
                    // base64 image
                    val base64Data = url.substringAfter(",")
                    val decoded =
                        android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
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
                        val raw = visionText.text.trim()
                        val cleaned = raw.replace(
                            "\\s".toRegex(),
                            ""
                        ) // Remove all whitespace, keep special chars
                        Log.d("CaptchaOCR", "Cleaned captcha text: $cleaned (raw: $raw)")
                        onResult(cleaned)
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

                // Remove popup
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

//                 Step 2: Click on Login Button In Menu
                val loginButtonInMenu = """
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
                webViewRef?.evaluateJavascript(loginButtonInMenu, null)

                delay(500) // short delay before typing

                // Step 3: Fill UserName
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

                // Step 3: Fill Password
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

                //Date Input
                val dateInput = """
                    javascript:(function () {
                        const monthNames = ["January", "February", "March", "April", "May", "June",
                                            "July", "August", "September", "October", "November", "December"];

                        const targetDay = "$targetDay"; // "27"
                        const targetMonth = "$targetMonthName"; // "June"
                        const targetYear = parseInt("$targetYear"); // 2025

                        function log(msg) {
                            if (window.Android) Android.sendToAndroid(msg);
                            console.log(msg);
                        }

                        // Find the calendar input
                        const input = document.querySelector(
                            '#jDate input, input[placeholder="DD/MM/YYYY"], input[formcontrolname="journeyDate"], input[formcontrolname="journeyDateInput"], input.ui-inputtext[role="textbox"]'
                        );
                        if (!input) {
                            log("❌ Date input not found");
                            return;
                        }

                        // Open the calendar
                        input.focus();
                        input.click();

                        // Wait for the calendar to open
                        function waitForCalendar(callback) {
                            if (document.querySelector(".ui-datepicker-calendar")) {
                                callback();
                            } else {
                                setTimeout(() => waitForCalendar(callback), 200);
                            }
                        }

                        function getMonthIndex(monthName) {
                            return monthNames.indexOf(monthName);
                        }

                        function getCurrentMonthYear() {
                            const monthText = document.querySelector(".ui-datepicker-month")?.innerText?.trim();
                            const yearText = document.querySelector(".ui-datepicker-year")?.innerText?.trim();

                            return {
                                monthIndex: getMonthIndex(monthText),
                                year: parseInt(yearText)
                            };
                        }

                        function alignMonthYear(callback) {
                            const { monthIndex: currentMonthIndex, year: currentYear } = getCurrentMonthYear();
                            const targetMonthIndex = getMonthIndex(targetMonth);

                            if (isNaN(currentMonthIndex) || isNaN(currentYear)) {
                                setTimeout(() => alignMonthYear(callback), 200);
                                return;
                            }

                            if (currentYear < targetYear || (currentYear === targetYear && currentMonthIndex < targetMonthIndex)) {
                                document.querySelector(".ui-datepicker-next")?.click();
                                setTimeout(() => alignMonthYear(callback), 300);
                            } else if (currentYear > targetYear || (currentYear === targetYear && currentMonthIndex > targetMonthIndex)) {
                                document.querySelector(".ui-datepicker-prev")?.click();
                                setTimeout(() => alignMonthYear(callback), 300);
                            } else {
                                callback();
                            }
                        }

                        function selectDate() {
                        const calendar = document.querySelector(".ui-datepicker-calendar");
                        if (!calendar) {
                            log("❌ Calendar table not found");
                            return;
                        }

                        const dayLinks = calendar.querySelectorAll("td > a");

                        let found = false;
                        dayLinks.forEach(a => {
                            const text = a.textContent.trim();
                            if (text === targetDay || text === String(parseInt(targetDay))) {
                                a.click();
                                found = true;
                            }
                        });

                        if (found) {
                            log("✅ Day " + targetDay + " clicked successfully");
                        } else {
                            log("❌ Day " + targetDay + " not found in calendar grid");
                        }
                    }


                        waitForCalendar(() => {
                            alignMonthYear(() => {
                                setTimeout(selectDate, 300);
                            });
                        });
                    })();
                    """.trimIndent()
                // Evaluate the script in WebView and wait until the date field is actually filled
                val dateSelected = suspendCancellableCoroutine<Boolean> { cont ->
                    // Kick off the click script
                    webViewRef?.evaluateJavascript(dateInput) { _ ->
                        // Start polling for the selected date value
                        scope.launch {
                            val checkScript = """
                                javascript:(function(){
                                    const inp = document.querySelector('#jDate input, input[placeholder=\"DD/MM/YYYY\"], input[formcontrolname=\"journeyDate\"], input[formcontrolname=\"journeyDateInput\"], input.ui-inputtext[role=\"textbox\"]');
                                    return inp ? inp.value : "";
                                })();
                            """.trimIndent()
                            while (isActive) {
                                val currentVal = suspendCancellableCoroutine<String> { innerCont ->
                                    webViewRef?.evaluateJavascript(checkScript) {
                                        innerCont.resume(it?.trim('"') ?: "")
                                    }
                                }
                                if (currentVal.isNotBlank() && currentVal.replace("\\s", "") == journeyDate.replace("\\s", "")) {
                                    cont.resume(true)
                                    break
                                }
                                delay(300)
                            }
                        }
                    }
                }

                // Wait until dateSelected completes
                if (dateSelected) {
                    Log.d("DatePicker", "✅ Date confirmed in input: $journeyDate")
                } else {
                    Log.d("DatePicker", "❌ Date not selected within timeout")
                }


                // Expand For Class
                        val expandClass = """
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
                        webViewRef?.evaluateJavascript(expandClass, null)

                        // Fill Class
                        val fillClass = """
                    javascript:(function() {
                        const el = document.querySelector("#journeyClass > div > div.ng-trigger.ng-trigger-overlayAnimation.ng-tns-c65-11.ui-dropdown-panel.ui-widget.ui-widget-content.ui-corner-all.ui-shadow.ng-star-inserted > div > ul > p-dropdownitem:nth-child($classIndex) > li");
                        if (el) {
                            el.click();
                            Android.sendToAndroid("✅ Class Selected");
                        } else {
                            Android.sendToAndroid("❌ Class not Selected");
                        }
                    })();
                """.trimIndent()
                        webViewRef?.evaluateJavascript(fillClass, null)

                        // Expand For Quota
                        val expandQuota = """
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
                        webViewRef?.evaluateJavascript(expandQuota, null)

                        // Fill Quota
                        val fillQuota = """
                    javascript:(function() {
                        const quotaIndex = $quotaIndex; // Use a constant for clarity

                        // Step 1: Click class dropdown item
                        const dropdownItem = document.querySelector(`#journeyQuota > div > div.ng-trigger.ng-trigger-overlayAnimation.ng-tns-c65-12.ui-dropdown-panel.ui-widget.ui-widget-content.ui-corner-all.ui-shadow.ng-star-inserted > div > ul > p-dropdownitem:nth-child(${quotaIndex}) > li`);

                        if (dropdownItem) {
                            dropdownItem.click();
                            Android.sendToAndroid(`✅ Quota index ${quotaIndex} selected.`);

                            // Step 2: Attempt to click the confirm button after a delay
                            setTimeout(function() {
                                // Find the common selector for the confirm/accept button in the dialog
                                // You'll need to inspect the page to get the MOST accurate selector.
                                // Here are some common patterns; try them one by one if unsure.
                                let confirmButton = document.querySelector("p-confirmdialog button.ui-confirmdialog-acceptbutton"); // Most common
                                if (!confirmButton) {
                                     confirmButton = document.querySelector("p-confirmdialog div.ui-dialog-footer button:last-child"); // Another common pattern
                                }
                                if (!confirmButton) {
                                     confirmButton = document.querySelector("p-confirmdialog button.ui-button-success"); // Sometimes there's a success class
                                }
                                // Add more specific selectors here if the above don't work for your exact page

                                if (confirmButton) {
                                    confirmButton.click();
                                    Android.sendToAndroid("✅ Confirm button clicked.");
                                } else {
                                    Android.sendToAndroid("⚠️ Confirm button not found after selecting quota.");
                                }

                            }, 700); // Increased delay slightly to ensure dialog appears

                        } else {
                            Android.sendToAndroid(`❌ Quota index $quotaIndex not found in dropdown.`);
                        }
                    })();
                """.trimIndent()
                        webViewRef?.evaluateJavascript(fillQuota, null)

                        // Fill the origin field
                        val fillOrigin = """
                    javascript:(function() {
                        const div = document.querySelector('#divMain > div > app-main-page > div > div > div.col-xs-12.level_2.slanted-div > div.col-xs-12.remove-padding.tbis-box > div:nth-child(1) > app-jp-input > div > form > div:nth-child(2) > div.col-md-7.col-xs-12.remove-padding > div:nth-child(1)');
                        if (div) {
                            const input = div.querySelector('input');
                            if (input) {
                                input.value = "$inputOrigin";
                                input.dispatchEvent(new Event('input', { bubbles: true }));
                                input.dispatchEvent(new Event('change', { bubbles: true }));
                                input.blur();
                                Android.sendToAndroid('✅ Origin input filled');
                            } else {
                                Android.sendToAndroid('❌ Origin input not found in div');
                            }
                        } else {
                            Android.sendToAndroid('❌ Origin div not found');
                        }
                    })();
                """.trimIndent()
                        webViewRef?.evaluateJavascript(fillOrigin, null)

                        // Fill the destination field
                        val fillDestination = """
                    javascript:(function() {
                        const div = document.querySelector('#divMain > div > app-main-page > div > div > div.col-xs-12.level_2.slanted-div > div.col-xs-12.remove-padding.tbis-box > div:nth-child(1) > app-jp-input > div > form > div:nth-child(2) > div.col-md-7.col-xs-12.remove-padding > div:nth-child(2)');
                        if (div) {
                            const input = div.querySelector('input');
                            if (input) {
                                input.value = "$inputDestination";
                                input.dispatchEvent(new Event('input', { bubbles: true }));
                                input.dispatchEvent(new Event('change', { bubbles: true }));
                                input.blur();
                                Android.sendToAndroid('✅ Destination input filled');
                            } else {
                                Android.sendToAndroid('❌ Destination input not found in div');
                            }
                        } else {
                            Android.sendToAndroid('❌ Destination div not found');
                        }
                    })();
                """.trimIndent()
                        webViewRef?.evaluateJavascript(fillDestination, null)

//                        delay(2000)

                        // Search Train Button
                        val searchButton = """
                    javascript:(function() {
                        const el = document.querySelector("#divMain > div > app-main-page > div > div > div.col-xs-12.level_2.slanted-div > div.col-xs-12.remove-padding.tbis-box > div:nth-child(1) > app-jp-input > div > form > div:nth-child(5) > div.col-md-3.col-sm-12.col-xs-12.remove-pad > button");
                        if (el) {
                            el.click();
                            Android.sendToAndroid("✅ Search Button Clicked");
                        } else {
                            Android.sendToAndroid("❌ Search Button Not Found");
                        }
                    })();
                """.trimIndent()
                        webViewRef?.evaluateJavascript(searchButton, null)

                        val selectTrain = """
                    javascript:(function() {
                        const targetTrainNumber = "$trainNumber";
                        const targetClassCode = "$className";
                        const targetDay = "$targetDay";

                        function waitForElementLoad(callback) {
                            const interval = setInterval(() => {
                                const el = document.querySelector("#divMain > div > app-train-list > div.col-sm-9.col-xs-12 > div.tbis-div");
                                if (el) {
                                    clearInterval(interval);
                                    callback();
                                }
                            }, 300);
                        }

                        function tryClickSearchButton(attempt = 1, maxAttempts = 1000) {
                        const searchBtn = document.querySelector(".btnDefault.train_Search.ng-star-inserted:not(.disable-book)");
                        if (searchBtn) {
                            searchBtn.click();
                            Android.sendToAndroid("✅ Enabled search button clicked.");
                            return;
                        }

                        const fallbackArea = document.querySelector(".ng-star-inserted");
                        if (fallbackArea) {
                            const containers = fallbackArea.querySelectorAll("div");
                            let clicked = false;

                            containers.forEach(container => {
                                if (container.textContent.trim().toUpperCase().includes(targetClassCode.toUpperCase())) {
                                    container.click(); // ✅ Click the whole container, not <td>
                                    Android.sendToAndroid("🔁 Retried by clicking class block containing '" + targetClassCode + "' (attempt " + attempt + ")");
                                    clicked = true;
                                }
                            });

                            if (clicked) {
                                setTimeout(() => {
                                    if (attempt < maxAttempts) {
                                        tryClickSearchButton(attempt + 1, maxAttempts);
                                    } else {
                                        Android.sendToAndroid("❌ Gave up after " + maxAttempts + " attempts.");
                                    }
                                }, 1200);
                            } else {
                                Android.sendToAndroid("❌ Class block containing '" + targetClassCode + "' not found in fallback area.");
                            }
                        } else {
                            Android.sendToAndroid("❌ Fallback area not found (attempt " + attempt + ").");
                        }
                    }


                        function matchTrainAndClickClass() {
                            for (let n = 3; n <= 20; n++) {
                                const headingSelector =
                                    "#divMain > div > app-train-list > div.col-sm-9.col-xs-12 > div.tbis-div > div.ng-star-inserted > div:nth-child(" + n + ") > div.form-group.no-pad.col-xs-12.bull-back.border-all > app-train-avl-enq > div.ng-star-inserted > div.dull-back.no-pad.col-xs-12 > div.col-sm-5.col-xs-11.train-heading";

                                const headingElement = document.querySelector(headingSelector);
                                if (!headingElement) continue;

                                const fullTrainText = headingElement.textContent.trim();
                                if (fullTrainText.includes(targetTrainNumber)) {
                                    Android.sendToAndroid("✅ Found train number '" + targetTrainNumber + "' at nth-child(" + n + ")");

                                    for (let b = 1; b <= 9; b++) {
                                        const classSelector =
                                            "#divMain > div > app-train-list > div.col-sm-9.col-xs-12 > div.tbis-div > div.ng-star-inserted > div:nth-child(" + n + ") > div.form-group.no-pad.col-xs-12.bull-back.border-all > app-train-avl-enq > div.ng-star-inserted > div:nth-child(5) > div.white-back.col-xs-12.ng-star-inserted > table > tr > td:nth-child(" + b + ")";
                                        const classCell = document.querySelector(classSelector);
                                        if (!classCell) continue;

                                        const classText = classCell.textContent.trim().toUpperCase();
                                        if (classText.includes(targetClassCode.toUpperCase())) {
                                            const clickable = classCell.querySelector("div");
                                            if (clickable) {
                                                clickable.click();
                                                Android.sendToAndroid("✅ Clicked on class '" + targetClassCode + "'");

                                                setTimeout(() => {
                                                    const preAvlSelector =
                                                        "#divMain > div > app-train-list > div.col-sm-9.col-xs-12 > div.tbis-div > div.ng-star-inserted > div:nth-child(" + n + ") .pre-avl";
                                                    const cells = document.querySelectorAll(preAvlSelector);
                                                    let matched = false;

                                                    cells.forEach(el => {
                                                        if (el.textContent.trim().includes(targetDay)) {
                                                            el.click();
                                                            Android.sendToAndroid("✅ Clicked on date: " + targetDay);
                                                            matched = true;

                                                            setTimeout(() => {
                                                                tryClickSearchButton();
                                                            }, 1000);
                                                        }
                                                    });

                                                    if (!matched) {
                                                        Android.sendToAndroid("❌ Date '" + targetDay + "' not found in .pre-avl.");
                                                    }
                                                }, 800);
                                                return;
                                            }
                                        }
                                    }

                                    Android.sendToAndroid("❌ Class '" + targetClassCode + "' not found in section nth-child(" + n + ")");
                                    return;
                                }
                            }

                            Android.sendToAndroid("❌ Train number '" + targetTrainNumber + "' not found between nth-child(3) to 20");
                        }

                        if (document.readyState === "complete" || document.readyState === "interactive") {
                            waitForElementLoad(matchTrainAndClickClass);
                        } else {
                            document.addEventListener("DOMContentLoaded", () => {
                                waitForElementLoad(matchTrainAndClickClass);
                            });
                        }
                    })();
                    """.trimIndent()
                        webViewRef?.evaluateJavascript(selectTrain, null)


                        break
                    } else {
                        statusMessage =
                            "❌ Target element not found. Retrying captcha (attempt $attempt)..."
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
