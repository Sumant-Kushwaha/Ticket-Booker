package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PaymentDetailsForm(
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
                PaymentMode.UPI -> UPIForm(selectedProvider.name)
                PaymentMode.CARDS -> CardForm(selectedProvider.name)
                PaymentMode.NET_BANKING -> NetBankingForm(selectedProvider.name)
                PaymentMode.WALLETS -> WalletForm(selectedProvider.name)
            }
        }
    }
}

@Composable
private fun UPIForm(providerName: String) {
    var upiId by remember { mutableStateOf("") }
    var autoFillOtp by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$providerName UPI Details",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = upiId,
            onValueChange = { upiId = it },
            label = { Text("Enter UPI ID") },
            placeholder = { Text("username@upi") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = autoFillOtp,
                onCheckedChange = { autoFillOtp = it }
            )
            Text("Enable automatic OTP filling")
        }
    }
}

@Composable
private fun CardForm(providerName: String) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    var autoFillOtp by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$providerName Card Details",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = cardHolderName,
            onValueChange = { cardHolderName = it },
            label = { Text("Card Holder Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cardNumber,
            onValueChange = { if (it.length <= 16) cardNumber = it },
            label = { Text("Card Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = expiryDate,
                onValueChange = { if (it.length <= 5) expiryDate = it },
                label = { Text("MM/YY") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = cvv,
                onValueChange = { if (it.length <= 3) cvv = it },
                label = { Text("CVV") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = PasswordVisualTransformation()
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = autoFillOtp,
                onCheckedChange = { autoFillOtp = it }
            )
            Text("Enable automatic OTP filling")
        }
    }
}

@Composable
private fun NetBankingForm(providerName: String) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var autoFillOtp by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$providerName Login",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username/Customer ID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password/IPIN") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = autoFillOtp,
                onCheckedChange = { autoFillOtp = it }
            )
            Text("Enable automatic OTP filling")
        }
    }
}

@Composable
private fun WalletForm(providerName: String) {
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var autoFillOtp by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$providerName Wallet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { if (it.length <= 10) mobileNumber = it },
            label = { Text("Mobile Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password/PIN") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = autoFillOtp,
                onCheckedChange = { autoFillOtp = it }
            )
            Text("Enable automatic OTP filling")
        }
    }
}
