package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.model.Passenger

@Composable
fun PassengerDialog(
    passenger: Passenger?,
    onSave: (Passenger) -> Unit,
    onDismiss: () -> Unit
) {
    var tempPassenger by remember { mutableStateOf(passenger ?: Passenger()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Passenger Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tempPassenger.name,
                    onValueChange = { tempPassenger = tempPassenger.copy(name = it) },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tempPassenger.age.toString(),
                    onValueChange = { tempPassenger = tempPassenger.copy(age = it.toIntOrNull() ?: 0) },
                    label = { Text("Age") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Add other fields like gender, berth preference, etc.
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(tempPassenger) },
                enabled = tempPassenger.name.isNotBlank() && tempPassenger.age > 0
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
