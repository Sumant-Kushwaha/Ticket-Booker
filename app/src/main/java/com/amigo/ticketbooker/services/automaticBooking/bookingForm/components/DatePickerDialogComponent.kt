package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogComponent(
    initialDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val calendar = Calendar.getInstance()
    val today = calendar.timeInMillis

    calendar.timeInMillis = today
    calendar.add(Calendar.DAY_OF_MONTH, 60)
    val maxDate = calendar.timeInMillis

    val initialDateMillis = try {
        if (initialDate.isNotBlank()) {
            dateFormatter.parse(initialDate)?.time?.coerceIn(today, maxDate) ?: today
        } else {
            today
        }
    } catch (e: Exception) {
        today
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        initialDisplayedMonthMillis = today
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = dateFormatter.format(Date(millis))
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
