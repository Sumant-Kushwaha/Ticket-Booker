package com.amigo.ticketbooker.authPage.mainLoginScreen.loginPage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.authPage.authViewModel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginMainScreen(
    authViewModel: AuthViewModel,
    isLoading: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Welcome Back",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Text(
            text = "Login to book your train tickets",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Email Field
        EmailFieldLogin(
            email = email,
            onEmailChange = { email = it },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        PasswordFieldLogin(
            password = password,
            onPasswordChange = { password = it },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Forgot Password
        ForgetPassword()

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        LoginButton(
            authViewModel = authViewModel,
            email = email,
            password = password,
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Or divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.LightGray
            )
            Text(
                text = "  OR  ",
                color = Color.Gray,
                fontSize = 12.sp
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login with OTP Button
        LoginWithOtpButton(isLoading = isLoading)
    }
}