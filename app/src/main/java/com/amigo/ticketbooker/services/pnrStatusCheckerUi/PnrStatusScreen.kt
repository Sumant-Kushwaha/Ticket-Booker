package com.amigo.ticketbooker.services.pnrStatusCheckerUi

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.R
import com.amigo.ticketbooker.navigation.LocalNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Extension function for Google Play Services Tasks
suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.exception == null) {
            if (task.isCanceled) {
                cont.cancel()
            } else {
                cont.resume(task.result)
            }
        } else {
            cont.resumeWithException(task.exception!!)
        }
    }
}

// Helper functions for SharedPreferences
private fun savePnrToHistory(context: Context, pnr: String) {
    val sharedPref = context.getSharedPreferences("pnr_history", Context.MODE_PRIVATE)
    
    // Get existing PNRs as a comma-separated string to maintain order
    val existingPnrsString = sharedPref.getString("pnr_list_ordered", "") ?: ""
    val existingPnrs = if (existingPnrsString.isBlank()) {
        mutableListOf<String>()
    } else {
        existingPnrsString.split(",").toMutableList()
    }
    
    // Remove the PNR if it already exists (to avoid duplicates)
    existingPnrs.remove(pnr)
    // Add the PNR to the beginning (most recent)
    existingPnrs.add(0, pnr)
    
    // Keep only last 5 PNRs
    val recentPnrs = existingPnrs.take(5)
    
    // Save as comma-separated string
    sharedPref.edit().putString("pnr_list_ordered", recentPnrs.joinToString(",")).apply()
}

private fun getPnrHistory(context: Context): List<String> {
    val sharedPref = context.getSharedPreferences("pnr_history", Context.MODE_PRIVATE)
    val pnrString = sharedPref.getString("pnr_list_ordered", "") ?: ""
    return if (pnrString.isBlank()) {
        emptyList()
    } else {
        pnrString.split(",").filter { it.isNotBlank() }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun PnrStatusScreen() {
    val navController = LocalNavController.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // State for PNR number input
    var pnrNumber by remember { mutableStateOf("") }
    var showResults by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loadWebView by remember { mutableStateOf(false) }
    var reloadWebViewKey by remember { mutableStateOf(0) }
    
    // Get PNR history for hints
    val pnrHistory = remember { getPnrHistory(context) }
    val hintText = remember(pnrHistory) {
        if (pnrHistory.isNotEmpty()) {
            "Recent: ${pnrHistory.first()}"
        } else {
            "Enter 10-digit PNR"
        }
    }
    
    Scaffold(
        topBar = {
            PnrStatusTopBar(
                title = "PNR Status",
                onBackPressed = { navController.navigateUp() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background gradient for the top portion
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header section
                PnrStatusHeader()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // PNR input card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // PNR input field
                        OutlinedTextField(
                            value = pnrNumber,
                            onValueChange = { 
                                // Only allow digits and limit to 10 characters
                                if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                    pnrNumber = it
                                }
                            },
                            label = { Text("PNR Number") },
                            placeholder = { Text("Enter 10-digit PNR") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_ticket),
                                    contentDescription = "PNR",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (pnrNumber.isNotEmpty()) {
                                    IconButton(onClick = { pnrNumber = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    if (pnrNumber.length == 10) {
                                        isLoading = true
                                        showResults = true
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        // Recent PNR hints - only show if history exists and current field is empty
                        if (pnrHistory.isNotEmpty() && pnrNumber.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Recent PNRs:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Scrollable row of hint chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pnrHistory.take(2).forEach { historyPnr ->
                                    AssistChip(
                                        onClick = {
                                            pnrNumber = historyPnr
                                            keyboardController?.hide()
                                        },
                                        label = { 
                                            Text(
                                                text = historyPnr,
                                                style = MaterialTheme.typography.bodyMedium
                                            ) 
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = "Recent PNR",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Submit button
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                if (pnrNumber.length == 10) {
                                    // Save PNR to history
                                    savePnrToHistory(context, pnrNumber)
                                    
                                    isLoading = true
                                    showResults = true
                                    // First hide the WebView, then show it again to force recreation
                                    loadWebView = false
                                    reloadWebViewKey++
                                    
                                    // Auto-scroll to show WebView after a short delay
                                    coroutineScope.launch {
                                        delay(500) // Wait for WebView to load
                                        scrollState.animateScrollTo(scrollState.maxValue)
                                    }
                                }
                            },
                            enabled = pnrNumber.length == 10,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Show PNR Status",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // WebView for PNR Status (visible to user)
                        // Use LaunchedEffect to delay showing WebView to ensure proper recreation
                        LaunchedEffect(reloadWebViewKey) {
                            if (reloadWebViewKey > 0) {
                                delay(100) // Small delay to ensure state is reset
                                loadWebView = true
                            }
                        }

                        if (loadWebView) {
                            var webViewRef by remember(reloadWebViewKey) { mutableStateOf<android.webkit.WebView?>(null) }

                            // Clear cookies and local storage before each load
                            DisposableEffect(reloadWebViewKey) {
                                CookieManager.getInstance().removeAllCookies(null)
                                CookieManager.getInstance().flush()
                                onDispose { }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(600.dp)
                                    .padding(vertical = 8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                androidx.compose.ui.viewinterop.AndroidView(
                                    factory = { ctx ->
                                        android.webkit.WebView(ctx).apply {
                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true
                                            settings.loadWithOverviewMode = true
                                            settings.useWideViewPort = true
                                            settings.setSupportZoom(true)
                                            settings.builtInZoomControls = true
                                            settings.displayZoomControls = false
                                            // Disable all touch events
                                            isClickable = false
                                            isFocusable = false
                                            isFocusableInTouchMode = false
                                            setOnTouchListener { _, _ -> true } // Consume all touch events
                                            // Randomize User-Agent
                                            settings.userAgentString = settings.userAgentString + " " + UUID.randomUUID().toString()
                                            webViewRef = this
                                            addJavascriptInterface(object {
                                                @android.webkit.JavascriptInterface
                                                fun log(msg: String) {
                                                    android.util.Log.d("PNRWebView", msg)
                                                }
                                            }, "AndroidLog")
                                            // Load the PNR status page directly
                                            loadUrl("https://www.confirmtkt.com/pnr-status/$pnrNumber")
                                        }
                                    },
                                    update = { webView ->
                                        webViewRef = webView
                                        webView.clearCache(true)
                                        webView.clearHistory()
                                        webView.loadUrl("https://www.confirmtkt.com/pnr-status/$pnrNumber")
                                        
                                        // Disable touch events on update as well
                                        webView.isClickable = false
                                        webView.isFocusable = false
                                        webView.isFocusableInTouchMode = false
                                        webView.setOnTouchListener { _, _ -> true }
                                        
                                        // Always set the WebViewClient to ensure onPageFinished is called
                                        webView.webViewClient = object : android.webkit.WebViewClient() {
                                            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                                // Stop loading animation after page load
                                                isLoading = false
                                                
                                                // Auto-scroll to show the WebView content
                                                coroutineScope.launch {
                                                    delay(300) // Small delay for WebView to render
                                                    scrollState.animateScrollTo(scrollState.maxValue)
                                                }
                                                
                                                // Inject JavaScript to continuously search for and click the popup close button
                                                val js = """
                                                    javascript:(function() {
                                                        function clickPopupClose() {
                                                            var closeButton = document.querySelector('#popup > div.BottomSheetPopup_circleOverlay__sYea6 > button > svg > path');
                                                            if (closeButton) {
                                                                // Click the parent button element instead of the path
                                                                var buttonElement = closeButton.closest('button');
                                                                if (buttonElement) {
                                                                    buttonElement.click();
                                                                    window.AndroidLog.log('Popup close button clicked successfully');
                                                                    return true;
                                                                }
                                                            }
                                                            return false;
                                                        }
                                                        
                                                        // Try to click immediately
                                                        if (!clickPopupClose()) {
                                                            // If not found, set up continuous checking
                                                            var checkInterval = setInterval(function() {
                                                                if (clickPopupClose()) {
                                                                    clearInterval(checkInterval);
                                                                }
                                                            }, 1000); // Check every second
                                                
                                                            // Stop checking after 30 seconds to avoid infinite loop
                                                            setTimeout(function() {
                                                                clearInterval(checkInterval);
                                                                window.AndroidLog.log('Stopped checking for popup after 30 seconds');
                                                            }, 30000);
                                                        }
                                                    })();
                                                """.trimIndent()
                                                view?.evaluateJavascript(js, null)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PnrStatusTopBar(title: String, onBackPressed: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun PnrStatusHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Train icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_train),
                contentDescription = "Train",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header text
        Text(
            text = "Check PNR Status",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = "Enter your 10-digit PNR number to check the current status of your ticket",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}