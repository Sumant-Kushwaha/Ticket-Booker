package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentMode.PaymentMode
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.paymentComposable.paymentProvider.PaymentProvider

@Composable
fun PaymentDetailsForm(
    paymentDetails: Map<String, String>,
    onPaymentDetailsChange: (Map<String, String>) -> Unit,
    selectedMode: PaymentMode?,
    selectedProvider: PaymentProvider?
) {
    if (selectedMode == null || selectedProvider == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedMode) {
                PaymentMode.UPI -> UPIForm(paymentDetails, onPaymentDetailsChange)
                PaymentMode.CARDS -> CardForm(paymentDetails, onPaymentDetailsChange)
                PaymentMode.NET_BANKING -> NetBankingForm(paymentDetails, onPaymentDetailsChange)
                PaymentMode.WALLETS -> WalletForm(paymentDetails, onPaymentDetailsChange)
            }
        }
    }
}

@Composable
private fun UPIForm(
    paymentDetails: Map<String, String>,
    onPaymentDetailsChange: (Map<String, String>) -> Unit
) {
    val upiId = paymentDetails["upiId"] ?: ""
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "We Don't Store Any Payment Details",
            Modifier.padding(bottom = 10.dp),
            color = Color.Red,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = upiId,
            onValueChange = { newId ->
                onPaymentDetailsChange(paymentDetails.toMutableMap().apply { put("upiId", newId) })
            },
            label = { Text("Enter UPI ID") },
            placeholder = { Text("username@upi") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun CardForm(
    paymentDetails: Map<String, String>,
    onPaymentDetailsChange: (Map<String, String>) -> Unit
) {
    val cardNumber = paymentDetails["cardNumber"] ?: ""
    val expiryDate = paymentDetails["expiryDate"] ?: ""
    val cvv = paymentDetails["cvv"] ?: ""
    val autoFillOtp = paymentDetails["autoFillOtp"] == "true"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "We Don't Store Any Payment Details",
                Modifier.padding(bottom = 10.dp),
                color = Color.Red,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { newValue ->
                    if (newValue.length <= 16) onPaymentDetailsChange(paymentDetails.toMutableMap().apply { put("cardNumber", newValue) })
                },
                label = { Text("Card Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { newValue ->
                        if (newValue.length <= 5) onPaymentDetailsChange(paymentDetails.toMutableMap().apply { put("expiryDate", newValue) })
                    },
                    label = { Text("MM/YY") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = cvv,
                    onValueChange = { newValue ->
                        if (newValue.length <= 3) onPaymentDetailsChange(paymentDetails.toMutableMap().apply { put("cvv", newValue) })
                    },
                    label = { Text("CVV") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = autoFillOtp,
                    onCheckedChange = { checked ->
                        onPaymentDetailsChange(paymentDetails.toMutableMap().apply { put("autoFillOtp", checked.toString()) })
                    }
                )
                Text("Enable automatic OTP filling")
            }
        }
    }
}

@Composable
private fun NetBankingForm(
    paymentDetails: Map<String, String>,
    onPaymentDetailsChange: (Map<String, String>) -> Unit
) {
    val username = paymentDetails["netbankingUsername"] ?: ""
    val password = paymentDetails["netbankingPassword"] ?: ""

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "We Don't Store Any Payment Details",
            Modifier.padding(bottom = 10.dp),
            color = Color.Red,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = username,
                onValueChange = { newValue ->
                    onPaymentDetailsChange(paymentDetails.toMutableMap().apply { put("netbankingUsername", newValue) })
                },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { newValue ->
                    onPaymentDetailsChange(paymentDetails.toMutableMap().apply { put("netbankingPassword", newValue) })
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }
}

@Composable
private fun WalletForm(
    paymentDetails: Map<String, String>,
    onPaymentDetailsChange: (Map<String, String>) -> Unit
) {
    val mobileNumber = paymentDetails["walletMobileNumber"] ?: ""
    val password = paymentDetails["walletPassword"] ?: ""

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "We Don't Store Any Payment Details",
            Modifier.padding(bottom = 10.dp),
            color = Color.Red,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { newValue ->
                    if (newValue.length <= 10) onPaymentDetailsChange(paymentDetails.toMutableMap().apply { put("walletMobileNumber", newValue) })
                },
                label = { Text("Mobile Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = password,
                onValueChange = { newValue ->
                    onPaymentDetailsChange(paymentDetails.toMutableMap().apply { put("walletPassword", newValue) })
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }
}