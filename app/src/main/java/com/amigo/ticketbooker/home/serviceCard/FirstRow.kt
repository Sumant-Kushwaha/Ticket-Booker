package com.amigo.ticketbooker.home.serviceCard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.R
import com.amigo.ticketbooker.home.ServiceCard
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.navigation.Routes

@Composable
fun FirstRow() {
    val navController = LocalNavController.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ServiceCard(
            title = "Automatic Booking",
            icon = R.drawable.ic_automatic_booking,
            modifier = Modifier.weight(1f),
            index = 0, // Modern gradient design
            onClick = { navController.navigate(Routes.AUTOMATIC_BOOKING) }
        )
        ServiceCard(
            title = "Manual Booking",
            icon = R.drawable.ic_manual_booking,
            modifier = Modifier.weight(1f),
            index = 1, // Outlined design
            onClick = { navController.navigate(Routes.MANUAL_BOOKING) }
        )
        ServiceCard(
            title = "Order Food",
            icon = R.drawable.ic_food,
            modifier = Modifier.weight(1f),
            index = 2, // Elevated color design
            onClick = { navController.navigate(Routes.ORDER_FOOD) }
        )
    }
}