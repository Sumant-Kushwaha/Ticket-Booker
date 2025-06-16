package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.passengerdetailsection

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.model.Passenger
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.PaymentDetailsForm
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentMode.PaymentMode
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentMode.PaymentModeCard
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentProvider.PaymentProvider
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentProvider.PaymentProviderCard
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.passengercard.PassengerCard

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
        var selectedProvider by remember { mutableStateOf<PaymentProvider?>(null) }

        PaymentModeCard(
            selectedMode = selectedPaymentMode,
            onPaymentModeSelected = {
                selectedPaymentMode = it
                selectedProvider = null // Reset provider when mode changes
            },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Add the payment provider card
        PaymentProviderCard(
            selectedMode = selectedPaymentMode,
            selectedProvider = selectedProvider,
            onProviderSelected = { selectedProvider = it },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Add payment details form
        PaymentDetailsForm(
            selectedMode = selectedPaymentMode,
            selectedProvider = selectedProvider
        )
    }
}
