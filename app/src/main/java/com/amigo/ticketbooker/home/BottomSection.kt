package com.amigo.ticketbooker.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.amigo.ticketbooker.R
import com.amigo.ticketbooker.navigation.Routes

@Composable
fun BottomSection(navController : NavController) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // My Account section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AccountOption(
                    title = "Free Token",
                    icon = R.drawable.ic_ticket,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(Routes.FREE_TOKEN)
                    }
                )

                AccountOption(
                    title = "Profile",
                    icon = R.drawable.ic_profile,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(Routes.PROFILE)
                    }
                )

                AccountOption(
                    title = "Help & Support",
                    icon = R.drawable.ic_help,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(Routes.HELP_SUPPORT)
                    }
                )
            }
        }
    }
}