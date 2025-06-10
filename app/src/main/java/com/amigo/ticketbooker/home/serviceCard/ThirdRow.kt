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
fun ThirdRow() {
    val navController = LocalNavController.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ServiceCard(
            title = "Platform Locator",
            icon = R.drawable.ic_platform_locator,
            modifier = Modifier.weight(1f),
            index = 1, // Outlined design
            onClick = { navController.navigate(Routes.PLATFORM_LOCATOR) }
        )
        ServiceCard(
            title = "Train on Map",
            icon = R.drawable.ic_train_map,
            modifier = Modifier.weight(1f),
            index = 2, // Elevated color design
            onClick = { navController.navigate(Routes.TRAIN_MAP) }
        )
        ServiceCard(
            title = "Master List",
            icon = R.drawable.ic_master_list,
            modifier = Modifier.weight(1f),
            index = 0, // Modern gradient design
            onClick = { navController.navigate(Routes.MASTER_LIST) }
        )
    }
}