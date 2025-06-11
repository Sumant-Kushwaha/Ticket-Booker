package com.amigo.ticketbooker.authPage.mainLoginScreen.loginWithOtpPage

import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.navigation.Routes

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amigo.ticketbooker.R
import com.amigo.ticketbooker.authPage.authViewModel.AuthViewModel
import com.amigo.ticketbooker.authPage.authViewModel.OtpState
import com.amigo.ticketbooker.font.fontFamily
import kotlinx.coroutines.delay

@Composable
fun OtpLoginScreen() {
    val navController = LocalNavController.current
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(OtpStep.ENTER_MOBILE) }
    var mobileNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isResendEnabled by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableStateOf(30) }

    // Observe OTP state
    val otpState by authViewModel.otpState.collectAsState()
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Handle OTP state changes
    LaunchedEffect(key1 = otpState) {
        when (otpState) {
            is OtpState.CodeSent -> {
                currentStep = OtpStep.ENTER_OTP
                remainingSeconds = 30
                isResendEnabled = false
                Toast.makeText(context, "OTP sent successfully!", Toast.LENGTH_SHORT).show()
            }

            is OtpState.Success -> {
                Toast.makeText(context, "Authentication successful!", Toast.LENGTH_SHORT).show()
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.AUTH) { inclusive = true }
                }
            }

            is OtpState.Error -> {
                errorMessage = (otpState as OtpState.Error).message
                showError = true
            }

            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3949AB), // Indian Railways Blue
                        Color(0xFF1A237E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // App Logo and Title
            Image(
                painter = painterResource(id = R.drawable.ic_train),
                contentDescription = "Train Logo",
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = "Indian Railways",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Ticket Booking",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Back Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            authViewModel.resetState()
                            navController.popBackStack()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = Color(0xFF3949AB)
                            )
                        }

                        Text(
                            text = "Login with OTP",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        color = Color.LightGray
                    )

                    // Mobile Number Entry
                    AnimatedVisibility(
                        visible = currentStep == OtpStep.ENTER_MOBILE,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        MobileNumberEntryContent(
                            mobileNumber = mobileNumber,
                            onMobileNumberChange = { mobileNumber = it },
                            isLoading = otpState is OtpState.Loading,
                            onContinue = {
                                if (mobileNumber.length == 10) {
                                    // Send OTP via Firebase
                                    authViewModel.sendOtp(mobileNumber, context as Activity)
                                }
                            }
                        )
                    }

                    // OTP Entry
                    AnimatedVisibility(
                        visible = currentStep == OtpStep.ENTER_OTP,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        OtpEntryContent(
                            mobileNumber = mobileNumber,
                            otpCode = otpCode,
                            onOtpChange = { otpCode = it },
                            isResendEnabled = isResendEnabled,
                            remainingSeconds = remainingSeconds,
                            isVerifying = otpState is OtpState.Loading,
                            onResendClick = {
                                // Reset timer and resend OTP
                                authViewModel.resendOtp(mobileNumber, context as Activity)
                            },
                            onVerifyClick = {
                                if (otpCode.length == 6) {
                                    // Verify OTP via Firebase
                                    authViewModel.verifyOtp(otpCode)
                                }
                            },
                            onEditMobile = {
                                currentStep = OtpStep.ENTER_MOBILE
                                authViewModel.resetState()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Text
        Text(
            text = "© 2025 Indian Railways",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        // Error Dialog
        if (showError) {
            AlertDialog(
                onDismissRequest = { showError = false },
                title = { Text("Authentication Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { showError = false }) {
                        Text("OK")
                    }
                }
            )
        }

        // Loading Indicator
        if (otpState is OtpState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }

    // Timer for OTP resend
    LaunchedEffect(key1 = currentStep, key2 = remainingSeconds) {
        if (currentStep == OtpStep.ENTER_OTP && !isResendEnabled) {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            }
            isResendEnabled = true
        }
    }
}

@Composable
fun MobileNumberEntryContent(
    mobileNumber: String,
    onMobileNumberChange: (String) -> Unit,
    isLoading: Boolean = false,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Enter your mobile number",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mobile Number Field
        OutlinedTextField(
            value = mobileNumber,
            onValueChange = {
                // Only allow digits and limit to 10 characters
                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                    onMobileNumberChange(it)
                }
            },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontFamily = fontFamily, fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3949AB),
                focusedLabelColor = Color(0xFF3949AB),
                cursorColor = Color(0xFF3949AB)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_phone),
                    contentDescription = "Mobile",
                    tint = Color.Gray
                )
            },
            prefix = {
                Text(
                    "+91 ",
                    color = Color.Black,
                    fontFamily = fontFamily,
                    fontSize = 16.sp
                )
            },
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We'll send a 6-digit OTP to verify this number",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Continue Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3949AB)
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !isLoading && mobileNumber.length == 10
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "GET OTP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OtpEntryContent(
    mobileNumber: String,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    isResendEnabled: Boolean,
    remainingSeconds: Int,
    isVerifying: Boolean,
    onResendClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onEditMobile: () -> Unit
) {
    val otpFocusRequester = remember { FocusRequester() }

    LaunchedEffect(key1 = Unit) {
        otpFocusRequester.requestFocus()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "OTP sent to +91 $mobileNumber",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Edit",
                color = Color(0xFF3949AB),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onEditMobile)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // OTP Entry Field
        OutlinedTextField(
            value = otpCode,
            onValueChange = {
                // Only allow digits and limit to 6 characters
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    onOtpChange(it)
                }
            },
            label = { Text("Enter 6-digit OTP") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(otpFocusRequester),
            textStyle = TextStyle(color = Color.Black, fontFamily = fontFamily),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF3949AB),
                focusedLabelColor = Color(0xFF3949AB),
                cursorColor = Color(0xFF3949AB)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sms),
                    contentDescription = "OTP",
                    tint = Color.Gray
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Resend OTP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Didn't receive OTP? ",
                fontSize = 12.sp,
                color = Color.Gray
            )

            if (isResendEnabled) {
                Text(
                    text = "Resend OTP",
                    color = Color(0xFF3949AB),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable(onClick = onResendClick)
                )
            } else {
                Text(
                    text = "Resend in ${remainingSeconds}s",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Verify Button
        Button(
            onClick = onVerifyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3949AB)
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = otpCode.length == 6 && !isVerifying
        ) {
            if (isVerifying) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "VERIFY & PROCEED",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

enum class OtpStep {
    ENTER_MOBILE,
    ENTER_OTP
}
