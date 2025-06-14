package com.amigo.ticketbooker.authPage.mainLoginScreen.signUpPage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.R
import com.amigo.ticketbooker.font.fontFamily

@Composable
fun MobileNumberFieldSignup(
    mobileNumber: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    isLoading: Boolean = false
) {
    OutlinedTextField(
        value = mobileNumber,
        onValueChange = { if (it.length <= 10) onValueChange(it) },
        label = { Text("Mobile Number") },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = fontFamily,
            fontSize = 18.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
            errorTextColor = MaterialTheme.colorScheme.error,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_phone),
                contentDescription = "Mobile Number",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        isError = isError,
        enabled = !isLoading,
        supportingText = if (isError) {
            { Text("Please enter a valid 10-digit mobile number") }
        } else null
    )
}