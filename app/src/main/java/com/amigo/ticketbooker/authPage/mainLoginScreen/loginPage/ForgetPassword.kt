package com.amigo.ticketbooker.authPage.mainLoginScreen.loginPage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ForgetPassword(
    isLoading: Boolean = false
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Forgot Password?",
            color = Color(0xFF3949AB),
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable(enabled = !isLoading) { /* Handle forgot password */ }
        )
    }
}