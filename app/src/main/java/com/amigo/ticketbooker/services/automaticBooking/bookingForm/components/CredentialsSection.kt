package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.services.automaticBooking.BookingForm

@Composable
fun CredentialsSection(
    formState: BookingForm,
    onFormStateChange: (BookingForm) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Username and Password
        OutlinedTextField(
            value = formState.username,
            onValueChange = { onFormStateChange(formState.copy(username = it)) },
            label = { Text("IRCTC Username") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true,
            isError = formState.username.isBlank()
        )
        OutlinedTextField(
            value = formState.password,
            onValueChange = { onFormStateChange(formState.copy(password = it)) },
            label = { Text("IRCTC Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility
                else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, if (passwordVisible) "Hide password" else "Show password")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            isError = formState.password.isBlank()
        )
    }
}
