package com.amigo.ticketbooker.services.automaticBooking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.font.fontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedFormsList(
    forms: List<BookingForm>,
    onFormClick: (BookingForm) -> Unit,
    onEditClick: (BookingForm) -> Unit,
    onDeleteClick: (BookingForm) -> Unit,
    onCloneClick: (BookingForm) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Saved Booking Forms",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = fontFamily,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(forms, key = { it.id }) { form ->
            BookingFormCard(
                form = form,
                onClick = { onFormClick(form) },
                onEdit = { onEditClick(form) },
                onDelete = { onDeleteClick(form) },
                onClone = { onCloneClick(form) }
            )
        }
    }
}