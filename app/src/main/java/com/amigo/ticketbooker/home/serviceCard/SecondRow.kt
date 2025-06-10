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
fun SecondRow() {
    val navController = LocalNavController.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ServiceCard(
            title = "Running Status",
            icon = R.drawable.ic_running_status,
            modifier = Modifier.weight(1f),
            index = 2, // Elevated color design
            onClick = { navController.navigate(Routes.RUNNING_STATUS) }
        )
        ServiceCard(
            title = "PNR Status",
            icon = R.drawable.ic_pnr_status,
            modifier = Modifier.weight(1f),
            index = 0, // Modern gradient design
            onClick = { navController.navigate(Routes.PNR_STATUS) }
        )
        ServiceCard(
            title = "Coach Position",
            icon = R.drawable.ic_coach_position,
            modifier = Modifier.weight(1f),
            index = 1, // Outlined design
            onClick = { navController.navigate(Routes.COACH_POSITION) }
        )
    }
}