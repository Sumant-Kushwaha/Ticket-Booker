package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Sealed class defining the available payment modes
 */
sealed class PaymentMode {
    object CARD_OR_UPI : PaymentMode()
    object BHIM_UPI : PaymentMode()
    
    companion object {
        val values = listOf(CARD_OR_UPI, BHIM_UPI)
        
        fun getDisplayText(mode: PaymentMode): String {
            return when (mode) {
                is CARD_OR_UPI -> "Pay through Credit & Debit Cards / Net Banking / Wallets / EMI / Rewards and Others\nConvenience Fee: ₹15/- + GST"
                is BHIM_UPI -> "Pay through BHIM/UPI\nConvenience Fee: ₹10/- + GST"
            }
        }
        
        fun getIcon(mode: PaymentMode) = when (mode) {
            is CARD_OR_UPI -> Icons.Default.CreditCard
            is BHIM_UPI -> Icons.Default.Payment
        }
    }
}

@Composable
fun PaymentModeCard(
    selectedMode: PaymentMode,
    onPaymentModeSelected: (PaymentMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // Payment mode options are hardcoded for simplicity
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Select Payment Mode",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Card/UPI Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPaymentModeSelected(PaymentMode.CARD_OR_UPI) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMode == PaymentMode.CARD_OR_UPI,
                    onClick = { onPaymentModeSelected(PaymentMode.CARD_OR_UPI) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = PaymentMode.getDisplayText(PaymentMode.CARD_OR_UPI),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // BHIM UPI Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPaymentModeSelected(PaymentMode.BHIM_UPI) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMode == PaymentMode.BHIM_UPI,
                    onClick = { onPaymentModeSelected(PaymentMode.BHIM_UPI) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = PaymentMode.getDisplayText(PaymentMode.BHIM_UPI),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PaymentModeCardPreview() {
    var selectedMode by remember { mutableStateOf<PaymentMode>(PaymentMode.CARD_OR_UPI) }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                PaymentModeCard(
                    selectedMode = selectedMode,
                    onPaymentModeSelected = { selectedMode = it }
                )
            }
        }
    }
}
