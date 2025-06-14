package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PaymentProvider(
    val name: String,
    val type: PaymentMode
)

object PaymentProviders {
    val upiProviders = listOf(
        "IRCTC ipay", "Paytm", "PayU", "RazorPay", "PhonePay", "Amazon Pay"
    ).map { PaymentProvider(it, PaymentMode.UPI) }

    val cardProviders = listOf(
        "IRCTC", "HDFC BANK", "AMERICAN EXPRESS", "NTT Data", "ICICI BANK",
        "KOTAK BANK", "Paytm", "PayU", "RazorPay", "PhonePay", "Plural"
    ).map { PaymentProvider(it, PaymentMode.CARDS) }

    val netBankingProviders = listOf(
        "IRCTC", "State Bank Of India", "Federal Bank", "Indian Bank",
        "Punjab National Bank", "Karur Vysya Bank", "Karnataka Bank",
        "ICICI Bank", "Kotak Mahindra Bank", "Nepal SBI Bank Ltd.",
        "South Indian Bank", "City Union Bank", "Canara Bank",
        "Airtel Payments Bank", "IDFC FIRST Bank", "Paytm", "PayU", "RazorPay"
    ).map { PaymentProvider(it, PaymentMode.NET_BANKING) }

    val walletProviders = listOf(
        "Mobikwik Wallet", "Airtel Money", "Amazon Pay Balance",
        "Paytm", "PayU", "PhonePay"
    ).map { PaymentProvider(it, PaymentMode.WALLETS) }

    fun getProvidersForMode(mode: PaymentMode): List<PaymentProvider> = when (mode) {
        PaymentMode.UPI -> upiProviders
        PaymentMode.CARDS -> cardProviders
        PaymentMode.NET_BANKING -> netBankingProviders
        PaymentMode.WALLETS -> walletProviders
    }
}

@Composable
fun PaymentProviderCard(
    selectedMode: PaymentMode?,
    selectedProvider: PaymentProvider?,
    onProviderSelected: (PaymentProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = selectedMode != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(6.dp)
            ) {
                Text(
                    "Payment Provider",
                    Modifier.padding(10.dp),
                    color = Color.Red,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                selectedMode?.let { mode ->
                    val providers = PaymentProviders.getProvidersForMode(mode)
                    
                    // Group providers into pairs and create rows
                    providers.chunked(2).forEach { rowProviders ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowProviders.forEach { provider ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .selectable(
                                            selected = provider == selectedProvider,
                                            onClick = { onProviderSelected(provider) }
                                        )
                                        .padding(2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (provider == selectedProvider) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        RadioButton(
                                            selected = provider == selectedProvider,
                                            onClick = { onProviderSelected(provider) }
                                        )
                                        
                                        Text(
                                            text = provider.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }
                            
                            // If odd number of providers in row, add empty weight
                            if (rowProviders.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
