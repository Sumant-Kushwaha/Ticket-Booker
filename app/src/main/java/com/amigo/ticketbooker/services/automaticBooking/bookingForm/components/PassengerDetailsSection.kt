package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.model.Passenger

@Composable
fun PassengerDetailsSection(
    passengers: List<Passenger>,
    onPassengerChange: (List<Passenger>) -> Unit,
    onAddPassengerClick: () -> Unit,
    onEditPassengerClick: (Int) -> Unit,
    onDeletePassengerClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        passengers.forEachIndexed { index, passenger ->
            PassengerCard(
                passenger = passenger,
                index = index,
                onEditClick = { onEditPassengerClick(index) },
                onDeleteClick = { onDeletePassengerClick(index) }
            )
        }

        if (passengers.size < 6) {
            OutlinedButton(
                onClick = onAddPassengerClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Passenger")
            }
        }
    }
}
