package com.amigo.ticketbooker.authPage.mainLoginScreen.loginPage


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
fun LoginButton(
    authViewModel: AuthViewModel,
    email: String,
    password: String,
    isLoading: Boolean = false
) {

    Button(
        onClick = { authViewModel.loginWithEmailPassword(email, password) },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3949AB)
        ),
        shape = RoundedCornerShape(8.dp),
        enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
    ) {
        Text(
            text = "LOGIN",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}