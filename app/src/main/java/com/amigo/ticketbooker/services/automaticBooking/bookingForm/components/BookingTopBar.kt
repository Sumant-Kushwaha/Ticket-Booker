package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTopBar(isEditing: Boolean) {
    TopAppBar(
        title = { Text(if (isEditing) "Edit Booking" else "New Booking") },
        navigationIcon = {
            IconButton(onClick = { /* TODO: Implement back press */ }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}
