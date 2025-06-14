package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

enum class PaymentMode {
    UPI,
    CARDS,
    NET_BANKING,
    WALLETS;

    companion object {
        fun getDisplayText(mode: PaymentMode): String = when (mode) {
            UPI -> "UPI"
            CARDS -> "Pay Using Cards"
            NET_BANKING -> "NetBanking"
            WALLETS -> "Wallets"
        }

        fun getDescription(mode: PaymentMode): String = when (mode) {
            UPI -> "Pay instantly using UPI"
            CARDS -> "Credit/Debit cards"
            NET_BANKING -> "Internet Banking"
            WALLETS -> "Digital Wallets"
        }

        fun getIcon(mode: PaymentMode) = when (mode) {
            UPI -> Icons.Default.Payment
            CARDS -> Icons.Default.CreditCard 
            NET_BANKING -> Icons.Default.AccountBalance
            WALLETS -> Icons.Default.AccountBalanceWallet
        }
    }
}

@Composable
fun PaymentModeCard(
    selectedMode: PaymentMode?,
    onPaymentModeSelected: (PaymentMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp)
        ) {
            PaymentMode.values().forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = mode == selectedMode,
                            onClick = { onPaymentModeSelected(mode) }
                        )
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = mode == selectedMode,
                        onClick = { onPaymentModeSelected(mode) }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = PaymentMode.getDisplayText(mode),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = PaymentMode.getDescription(mode),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
