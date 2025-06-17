package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.saveButton

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun BookingFAB(isFormValid: Boolean, onSave: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onSave,
        containerColor = if (isFormValid) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        },
        contentColor = if (isFormValid) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        },
        icon = {
            Icon(
                Icons.Default.Save,
                contentDescription = "Save",
                tint = if (isFormValid) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        },
        text = { Text("") }
    )
}
