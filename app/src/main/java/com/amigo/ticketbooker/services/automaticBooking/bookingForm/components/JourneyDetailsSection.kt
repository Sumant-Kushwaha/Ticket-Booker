package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.services.automaticBooking.BookingForm

@Composable
fun JourneyDetailsSection(
    formState: BookingForm,
    onFormStateChange: (BookingForm) -> Unit,
    onDatePickerClick: () -> Unit
) {
    var classExpanded by remember { mutableStateOf(false) }
    var quotaExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Date of Journey
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = formState.date,
                onValueChange = {},
                label = { Text("Date of Journey") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select Date") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Date") }
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0f)
                    .clickable { onDatePickerClick() }
            )
        }

        // Stations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = formState.fromStation,
                onValueChange = { onFormStateChange(formState.copy(fromStation = it.uppercase())) },
                label = { Text("From") },
                leadingIcon = { Icon(Icons.Default.Train, null) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = formState.fromStation.isBlank()
            )
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swap stations",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(8.dp)
                    .clickable {
                        val temp = formState.fromStation
                        onFormStateChange(
                            formState.copy(
                                fromStation = formState.toStation,
                                toStation = temp
                            )
                        )
                    }
            )
            OutlinedTextField(
                value = formState.toStation,
                onValueChange = { onFormStateChange(formState.copy(toStation = it.uppercase())) },
                label = { Text("To") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = formState.toStation.isBlank()
            )
        }

        // Train Number
        OutlinedTextField(
            value = formState.trainNumber,
            onValueChange = { onFormStateChange(formState.copy(trainNumber = it)) },
            label = { Text("Train Number (Optional)") },
            leadingIcon = { Icon(Icons.Default.DirectionsRailway, null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true
        )

        // Boarding Station
        OutlinedTextField(
            value = formState.boardingStation ?: "",
            onValueChange = { onFormStateChange(formState.copy(boardingStation = it)) },
            label = { Text("Boarding Station (Optional)") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true
        )

        // Class Dropdown
        DropdownField(
            label = "Class",
            value = formState.classType,
            options = listOf("SL", "3A", "2A", "1A", "CC", "EC"),
            expanded = classExpanded,
            onExpandedChange = { classExpanded = it },
            onOptionSelected = { onFormStateChange(formState.copy(classType = it)) }
        )

        // Quota Dropdown
        DropdownField(
            label = "Quota",
            value = formState.quota,
            options = listOf("GENERAL", "TATKAL", "PREMIUM_TATKAL", "LADIES"),
            expanded = quotaExpanded,
            onExpandedChange = { quotaExpanded = it },
            onOptionSelected = { onFormStateChange(formState.copy(quota = it)) }
        )
    }
}
