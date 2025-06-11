package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.model.Passenger
import com.amigo.ticketbooker.model.BerthPreference

@Composable
fun PassengerCard(
    passenger: Passenger,
    index: Int,
    onEditClick: (Int) -> Unit
) {
    Card(
        onClick = { onEditClick(index) },
        modifier = Modifier
            .width(220.dp)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Passenger ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (passenger.name.isNotBlank()) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Passenger",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                passenger.name.ifEmpty { "Tap to add details" },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (passenger.name.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )

            if (passenger.name.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${passenger.age} • ${passenger.gender}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (passenger.berthPreference != BerthPreference.NO_PREFERENCE) {
                    Text(
                        "Berth: ${passenger.berthPreference}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
