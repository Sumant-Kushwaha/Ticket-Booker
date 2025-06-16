package com.amigo.ticketbooker.services.automaticBooking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onCloneClick: (BookingForm) -> Unit,
    onAutomationClick: () -> Unit
) {
    var isAutomating by remember { mutableStateOf(false) }
    var automationStatus by remember { mutableStateOf("Idle") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
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

            items(
                count = forms.size,
                key = { index -> forms[index].id ?: index.toString() }
            ) { index ->
                val form = forms[index]
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
}