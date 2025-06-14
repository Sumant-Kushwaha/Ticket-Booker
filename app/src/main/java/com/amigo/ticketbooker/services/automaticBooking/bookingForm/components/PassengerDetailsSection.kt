package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.model.Passenger

// Import PaymentMode and PaymentModeCard from the components package
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.PaymentMode
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.PaymentModeCard

// Import required Compose runtime functions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun PassengerDetailsSection(
    passengers: List<Passenger>,
    onPassengerChange: (List<Passenger>) -> Unit,
    onAddPassengerClick: () -> Unit,
    onEditPassengerClick: (Int) -> Unit,
    onDeletePassengerClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Initialize the selected payment mode state with explicit type
    var selectedPaymentMode: PaymentMode by remember { 
        mutableStateOf(PaymentMode.CARD_OR_UPI)
    }
    
    // Material3 theme is available through the MaterialTheme composable
    // No need to store these values as they're available through MaterialTheme

    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Passengers (${passengers.size}/6)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            itemsIndexed(passengers) { index, passenger ->
                PassengerCard(
                    passenger = passenger,
                    index = index,
                    onEditClick = { onEditPassengerClick(index) },
                    onDeleteClick = { onDeletePassengerClick(index) }
                )
            }
            
            item {
                if (passengers.size < 6) {
                    Button(
                        onClick = onAddPassengerClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Passenger")
                    }
                }
            }
        }
        
        // Payment mode selection card
        PaymentModeCard(
            selectedMode = selectedPaymentMode,
            onPaymentModeSelected = { newMode: PaymentMode ->
                selectedPaymentMode = newMode
            },
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
