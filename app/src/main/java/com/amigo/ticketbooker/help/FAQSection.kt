package com.amigo.ticketbooker.help

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FAQSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Frequently Asked Questions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // FAQ items
            FAQItem(
                question = "How do I book a Tatkal ticket?",
                answer = "To book a Tatkal ticket, select 'Automatic Booking' or 'Manual Booking' from the home screen. Choose your train, date, and passenger details. Tatkal booking opens at 10:00 AM for AC classes and 11:00 AM for non-AC classes."
            )

            FAQItem(
                question = "What is the refund policy?",
                answer = "Refund policies vary based on ticket type and cancellation time. For Tatkal tickets, cancellation charges are higher. Check the IRCTC refund rules for detailed information based on your ticket type."
            )

            FAQItem(
                question = "How do I check my PNR status?",
                answer = "To check your PNR status, go to the home screen and select 'PNR Status'. Enter your 10-digit PNR number and tap 'Check Status' to view your current booking status."
            )

            FAQItem(
                question = "What are tokens and how do I use them?",
                answer = "Tokens are in-app currency used for premium features like automatic booking. You can earn tokens by watching ads in the 'Free Token' section or by purchasing them. Tokens are consumed when you use the automatic booking feature."
            )
        }
    }
}