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
import com.amigo.ticketbooker.R

@Composable
fun HelpGuidesSection() {
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
                text = "Help Guides",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Help guide items
            HelpGuideItem(
                title = "Tatkal Booking Guide",
                description = "Learn how to book Tatkal tickets quickly and efficiently",
                iconRes = R.drawable.ic_train
            )

            HelpGuideItem(
                title = "Master List Tutorial",
                description = "How to create and manage your list of frequent passengers",
                iconRes = R.drawable.ic_person
            )

            HelpGuideItem(
                title = "Payment Options Guide",
                description = "Understanding different payment methods and their processing times",
                iconRes = R.drawable.ic_setting
            )

            HelpGuideItem(
                title = "Cancellation & Refunds",
                description = "Step-by-step guide to cancel tickets and get refunds",
                iconRes = R.drawable.ic_back
            )

            HelpGuideItem(
                title = "App Navigation Tutorial",
                description = "Learn how to use all features of the Quick Tatkal app",
                iconRes = R.drawable.ic_phone
            )
        }
    }
}