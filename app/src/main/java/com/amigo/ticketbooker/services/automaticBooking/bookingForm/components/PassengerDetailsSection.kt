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

        // Add passengers cards
        passengers.forEachIndexed { index, passenger ->
            PassengerCard(
                passenger = passenger,
                index = index,
                onEditClick = { onEditPassengerClick(index) },
                onDeleteClick = { onDeletePassengerClick(index) }
            )
        }

        // Add payment mode card after passenger cards
        var selectedPaymentMode by remember { mutableStateOf<PaymentMode?>(null) }

        Text(
            "Payment Mode",
            Modifier.padding(top = 10.dp, start = 10.dp),
            style = MaterialTheme.typography.titleMedium
        )
        PaymentModeCard(
            selectedMode = selectedPaymentMode,
            onPaymentModeSelected = { selectedPaymentMode = it },
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
