package com.amigo.ticketbooker.authPage.mainLoginScreen.signUpPage


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.authPage.authViewModel.AuthViewModel

@Composable
fun SignupButton(
    authViewModel: AuthViewModel,
    email: String,
    password: String,
    confirmPassword: String,
    termsAccepted: Boolean,
    isLoading: Boolean = false
) {
    Button(
        onClick = {
            if (validateSignUpForm(email, password, confirmPassword, termsAccepted)) {
                authViewModel.signUpWithEmailPassword(email, password)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3949AB)
        ),
        shape = RoundedCornerShape(8.dp),
        enabled = !isLoading && validateSignUpForm(email, password, confirmPassword, termsAccepted)
    ) {
        Text(
            text = "SIGN UP",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun validateSignUpForm(
    email: String,
    password: String,
    confirmPassword: String,
    termsAccepted: Boolean
): Boolean {
    return email.isNotEmpty() &&
            password.isNotEmpty() &&
            password == confirmPassword &&
            password.length >= 6 &&
            termsAccepted
}