package com.amigo.ticketbooker.authPage.mainLoginScreen.signUpPage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import com.amigo.ticketbooker.authPage.authViewModel.AuthState
import com.amigo.ticketbooker.authPage.authViewModel.AuthViewModel

@Composable
fun SignupMainScreen(
    authViewModel: AuthViewModel
) {
    // Hoisted form state
    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf("") }

    val isLoading = authViewModel.authState.value == AuthState.Loading

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Create Account",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Text(
            text = "Sign up to book tatkal and normal tickets",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Full Name Field
        NameField(
            fullName = fullName,
            onValueChange = { fullName = it },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mobile Number Field
        MobileNumberFieldSignup(
            mobileNumber = mobileNumber,
            onValueChange = { mobileNumber = it },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field
        EmailFieldSignup(
            email = email,
            onValueChange = { email = it },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        PasswordFieldSignup(
            password = password,
            onValueChange = {
                password = it
                passwordError = if (confirmPassword.isNotEmpty() && it != confirmPassword)
                    "Passwords do not match" else ""
            },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        ConfirmPasswordFieldSignup(
            confirmPassword = confirmPassword,
            onValueChange = {
                confirmPassword = it
                passwordError = if (it.isNotEmpty() && it != password)
                    "Passwords do not match" else ""
            },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password error message
        if (passwordError.isNotEmpty()) {
            Text(
                text = passwordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Terms and Conditions
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "I accept the Terms & Conditions")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up Button
        SignupButton(
            authViewModel = authViewModel,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            termsAccepted = termsAccepted,
            isLoading = isLoading
        )
    }
}
