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
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentMode.PaymentModeCard
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentProvider.PaymentProviderCard
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.passengerCard.PassengerCard

@Composable
fun PassengerDetailsSection(
    passengers: List<Passenger>,
    paymentMode: String?,
    paymentProvider: String?,
    paymentDetails: Map<String, String>,
    onPassengerChange: (List<Passenger>) -> Unit,
    onAddPassengerClick: () -> Unit,
    onEditPassengerClick: (Int) -> Unit,
    onDeletePassengerClick: (Int) -> Unit,
    onPaymentModeChange: (String) -> Unit,
    onPaymentProviderChange: (String) -> Unit,
    onPaymentDetailsChange: (Map<String, String>) -> Unit
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

        // Payment Mode Card
        PaymentModeCard(
            selectedMode = paymentMode?.let { com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentMode.PaymentMode.valueOf(it) },
            onPaymentModeSelected = { mode ->
                onPaymentModeChange(mode.name)
                onPaymentProviderChange("") // Reset provider
            },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Payment Provider Card
        PaymentProviderCard(
            selectedMode = paymentMode?.let { com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentMode.PaymentMode.valueOf(it) },
            selectedProvider = paymentProvider?.let { com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentProvider.PaymentProvider(it, paymentMode?.let { pm -> com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentMode.PaymentMode.valueOf(pm) } ?: com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentMode.PaymentMode.UPI) },
            onProviderSelected = { provider -> onPaymentProviderChange(provider.name) },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Payment Details Form
        PaymentDetailsForm(
            paymentDetails = paymentDetails,
            onPaymentDetailsChange = onPaymentDetailsChange,
            selectedMode = paymentMode?.let { com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentMode.PaymentMode.valueOf(it) },
            selectedProvider = paymentProvider?.let { com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentProvider.PaymentProvider(it, paymentMode?.let { pm -> com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentMode.PaymentMode.valueOf(pm) } ?: com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentMode.PaymentMode.UPI) }
        )
    }
}
