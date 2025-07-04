package com.amigo.ticketbooker.services.automaticBooking.webRun

import android.annotation.SuppressLint
import android.content.Context
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
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Base64
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.URL
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
    quotaName: String = "1",
    className: String = "12",
    trainNumber: Int = 12558,
    passengerName:String="Omshree",
    passengerAge:String="20",
    passengerGender:String="F",
    passengerSeatPreference:String="SU",
    passengerMobileNumber: String = "7302221097",
    autoUpgradeOption: String = "1", // "1" = check, "0" = leave unchecked
    travelInsurance:String="1",
    paymentOption:Int=2,
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


    val paymentOption = when (paymentOption) {
        1  -> "CARD_NETBANKING"
        2  -> "UPI"
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
    val scope = rememberCoroutineScope()



    Box(modifier = modifier.fillMaxSize()) {

        AndroidView(
            factory = { context ->
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
                            webViewRef = view // store reference if you need to run JS later
                        }
                    }

                    loadUrl("https://www.irctc.co.in/nget/train-search")
                }
            },
            modifier = Modifier.fillMaxSize()
        )


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

                captchaSolver(
                    context,
                    webViewRef!!,
                    captchaImageSelector = ".captcha-img",
                    inputFieldSelector = "input[formcontrolname='captcha']",
                    buttonSelector = "#login_header_disable > div > div > div.ng-tns-c19-13.ui-dialog-content.ui-widget-content > div.irmodal.ng-tns-c19-13 > div > div.login-bg.pull-left > div > div.modal-body > form > span > button",
                    refreshButtonXPath = "//*[@id=\"login_header_disable\"]/div/div/div[2]/div[2]/div/div[2]/div/div[2]/form/div[5]/div/app-captcha/div/div/div[2]/span[2]/a/span",
                    waitAnimationXPath = "/div/div[2]/span[2]",
                    verifiedElementXPath = "/html/body/app-root/app-home/div[1]/app-header/div[2]/div[2]/div[1]/a[2]/span",
                    maxRetries = 5
                )


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

                        val fillPassengerDetails = """
javascript: (function () {
    function fillName() {
        var inputElements = document.querySelectorAll(
            'input[placeholder="Name"][maxlength="16"][type="text"][autocomplete="off"].ui-autocomplete-input'
        );
        if (inputElements.length > 0) {
            inputElements[0].value = "$passengerName";
            var event = new Event('input', { bubbles: true });
            inputElements[0].dispatchEvent(event);
            console.log("Passenger name set.");
        } else {
            setTimeout(fillName, 300);
        }
    }

    function fillAge() {
        var inputElements = document.querySelectorAll(
            'input[placeholder="Age"][maxlength="3"][type="number"][min="1"][max="125"]'
        );
        if (inputElements.length > 0) {
            inputElements[0].value = "$passengerAge";
            var event = new Event('input', { bubbles: true });
            inputElements[0].dispatchEvent(event);
            console.log("Passenger age set.");
        } else {
            setTimeout(fillAge, 300);
        }
    }

    function fillGender() {
        var selectEl = document.querySelector('select[formcontrolname="passengerGender"]');
        if (selectEl) {
            selectEl.value = "$passengerGender"; // M, F, T
            var event = new Event('change', { bubbles: true });
            selectEl.dispatchEvent(event);
            console.log("Passenger gender set.");
        } else {
            setTimeout(fillGender, 300);
        }
    }

    function fillBerth() {
        var selectEl = document.querySelector('select[formcontrolname="passengerBerthChoice"]');
        if (selectEl) {
            selectEl.value = "";
            var clearEvent = new Event('change', { bubbles: true });
            selectEl.dispatchEvent(clearEvent);

            setTimeout(function () {
                selectEl.value = "$passengerSeatPreference";
                var changeEvent = new Event('change', { bubbles: true });
                selectEl.dispatchEvent(changeEvent);
                console.log("Berth preference set to: $passengerSeatPreference");
            }, 100);
        } else {
            setTimeout(fillBerth, 300);
        }
    }

    // Call all functions
    fillName();
    fillAge();
    fillGender();
    fillBerth();
})();
""".trimIndent()

                        webViewRef?.evaluateJavascript(fillPassengerDetails, null)


                        val fillMobileNumber = """
javascript: (function () {
    function waitForInputAndSetValue() {
        var input = document.querySelector('input[formcontrolname="mobileNumber"]');
        if (input) {
            input.value = "$passengerMobileNumber";
            var event = new Event('input', { bubbles: true });
            input.dispatchEvent(event);
            console.log("Passenger mobile number set.");
        } else {
            setTimeout(waitForInputAndSetValue, 300);
        }
    }
    waitForInputAndSetValue();
})();
""".trimIndent()

                        webViewRef?.evaluateJavascript(fillMobileNumber, null)

                        val toggleAutoUpgradationCheckbox = """
javascript: (function () {
    function waitAndToggleCheckbox() {
        var checkbox = document.getElementById("autoUpgradation");
        if (checkbox) {
            var shouldCheck = "$autoUpgradeOption" === "1";
            if (checkbox.checked !== shouldCheck) {
                checkbox.click();
                console.log("Auto Upgradation checkbox toggled to: " + shouldCheck);
            } else {
                console.log("Auto Upgradation checkbox already in correct state.");
            }
        } else {
            setTimeout(waitAndToggleCheckbox, 300);
        }
    }
    waitAndToggleCheckbox();
})();
""".trimIndent()

                        webViewRef?.evaluateJavascript(toggleAutoUpgradationCheckbox, null)



                        val selectInsurance = """
javascript: (function () {
    function waitAndSelectInsurance() {
        var yesRadio = document.getElementById("travelInsuranceOptedYes-0");
        var noRadio = document.getElementById("travelInsuranceOptedNo-0");

        if (yesRadio && noRadio) {
            if ("$travelInsurance" === "1") {
                yesRadio.click();
                Android.sendToAndroid("✅ Insurance selected YES for travel insurance.");
            } else {
                noRadio.click();
                Android.sendToAndroid("❌ Insurance selected NO for travel insurance.");
            }
        } else {
            setTimeout(waitAndSelectInsurance, 300); // Retry every 300ms
        }
    }
    waitAndSelectInsurance();
})();
""".trimIndent()

                        webViewRef?.evaluateJavascript(selectInsurance, null)


                        val paymentSelectionScript = """
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

//                        webViewRef?.evaluateJavascript(paymentSelectionScript, null)

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




fun captchaSolver(
    context: Context,
    webView: WebView,
    captchaImageSelector: String,
    inputFieldSelector: String,
    buttonSelector: String,
    refreshButtonXPath: String,
    waitAnimationXPath: String = "/div/div[2]/span[2]",
    verifiedElementXPath: String = "/html/body/app-root/app-home/div[1]/app-header/div[2]/div[2]/div[1]/a[2]/span",
    maxRetries: Int = 3
) {
    var retries = 0

    fun solve() {
        if (retries >= maxRetries) {
            Log.d("CaptchaSolver", "❌ Max retries reached.")
            return
        }

        retries++

        val getImageJS = """
            (function() {
                const img = document.querySelector("$captchaImageSelector");
                if (img) {
                    return img.src || img.getAttribute('src');
                }
                return "";
            })();
        """.trimIndent()

        webView.evaluateJavascript(getImageJS) { imageUrl ->
            if (imageUrl.isNullOrBlank() || imageUrl == "\"\"") {
                Log.d("CaptchaSolver", "❌ Captcha image not found.")
                return@evaluateJavascript
            }

            downloadAndRecognizeCaptcha(context, imageUrl.trim('"')) { captchaText ->
                if (captchaText.isEmpty()) {
                    Log.d("CaptchaSolver", "⚠️ OCR failed, refreshing captcha.")
                    refreshCaptcha(webView, refreshButtonXPath)
                    solve()
                    return@downloadAndRecognizeCaptcha
                }

                fillAndSubmitCaptcha(webView, inputFieldSelector, buttonSelector, captchaText)

                waitForCaptchaVerification(webView, waitAnimationXPath, verifiedElementXPath, refreshButtonXPath) { verified ->
                    if (verified) {
                        Log.d("CaptchaSolver", "✅ Captcha solved successfully.")
                    } else {
                        Log.d("CaptchaSolver", "❌ Captcha solve failed, retrying ($retries/$maxRetries).")
                        solve()
                    }
                }
            }
        }
    }

    solve()
}

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
    val grayBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(grayBitmap)
    val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
    }
    canvas.drawBitmap(bitmap, 0f, 0f, paint)

    val binarized = Bitmap.createBitmap(grayBitmap.width, grayBitmap.height, Bitmap.Config.ARGB_8888)
    for (x in 0 until grayBitmap.width) {
        for (y in 0 until grayBitmap.height) {
            val pixel = grayBitmap.getPixel(x, y)
            val brightness = android.graphics.Color.red(pixel) // <-- use fully qualified name
            val binarizedColor = if (brightness < 128) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            binarized.setPixel(x, y, binarizedColor)
        }
    }

    return binarized
}

private fun fillAndSubmitCaptcha(webView: WebView, inputSelector: String, buttonSelector: String, captcha: String) {
    val js = """
        (function() {
            const input = document.querySelector("$inputSelector");
            if (input) { input.value = "$captcha"; input.dispatchEvent(new Event('input', { bubbles: true })); }
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

private fun waitForCaptchaVerification(
    webView: WebView,
    waitXPath: String,
    verifyXPath: String,
    refreshButtonXPath: String,
    onResult: (Boolean) -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        delay(2000L)

        val checkJS = """
            (function() {
                const waitEl = document.evaluate('$waitXPath', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                if (waitEl) return "WAITING";
                const verifyEl = document.evaluate('$verifyXPath', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
                if (verifyEl) return "SUCCESS";
                return "FAIL";
            })();
        """.trimIndent()

        webView.evaluateJavascript(checkJS) { result ->
            when {
                result?.contains("WAITING") == true -> {
                    Log.d("CaptchaSolver", "⏳ Waiting for animation...")
                    waitForCaptchaVerification(webView, waitXPath, verifyXPath, refreshButtonXPath, onResult)
                }
                result?.contains("SUCCESS") == true -> onResult(true)
                else -> onResult(false)
            }
        }
    }
}

