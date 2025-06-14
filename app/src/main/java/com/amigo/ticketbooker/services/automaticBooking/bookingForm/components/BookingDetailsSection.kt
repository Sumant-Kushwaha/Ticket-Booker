package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.services.automaticBooking.BookingForm

@Composable
fun BookingDetailsSection(
    formState: BookingForm,
    onFormStateChange: (BookingForm) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Form Name",
            Modifier.padding(bottom = 10.dp),
            color = Color.Red,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        // Form Name
        OutlinedTextField(
            value = formState.name,
            onValueChange = { onFormStateChange(formState.copy(name = it)) },
            label = { Text("Form Name") },
            leadingIcon = { Icon(Icons.Default.Description, null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true,
            isError = formState.name.isBlank()
        )
    }
}
