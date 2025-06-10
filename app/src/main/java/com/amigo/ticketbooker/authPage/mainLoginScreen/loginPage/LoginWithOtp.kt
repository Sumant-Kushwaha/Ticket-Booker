package com.amigo.ticketbooker.authPage.mainLoginScreen.loginPage

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.R
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.navigation.Routes


@Composable
fun LoginWithOtpButton(
    isLoading: Boolean = false
) {

    val navController = LocalNavController.current

    OutlinedButton(
        onClick = { navController.navigate(Routes.OTP_LOGIN) },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        border = ButtonDefaults.outlinedButtonBorder(enabled = !isLoading),
        shape = RoundedCornerShape(8.dp),
        enabled = !isLoading
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_sms),
            contentDescription = "OTP",
            tint = Color(0xFF3949AB)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "LOGIN WITH OTP",
            color = Color(0xFF3949AB),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}