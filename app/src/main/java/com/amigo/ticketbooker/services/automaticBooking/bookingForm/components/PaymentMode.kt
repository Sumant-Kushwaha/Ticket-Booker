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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

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
            Text(
                "Payment Mode",
                Modifier.padding(10.dp),
                color = Color.Red,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            // Convert array to list before using chunked
            PaymentMode.values().toList().chunked(2).forEach { rowModes ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rowModes.forEach { mode ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .selectable(
                                    selected = mode == selectedMode,
                                    onClick = { onPaymentModeSelected(mode) }
                                )
                                .padding(2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (mode == selectedMode)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    RadioButton(
                                        selected = mode == selectedMode,
                                        onClick = { onPaymentModeSelected(mode) }
                                    )
                                    
                                    Text(
                                        text = PaymentMode.getDisplayText(mode),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }

                            }
                        }
                    }
                    
                    // If odd number of modes in row, add empty weight
                    if (rowModes.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
