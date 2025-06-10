package com.amigo.ticketbooker.authPage.mainLoginScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.R

@Composable
fun AppLogoAndTitle() {
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
}