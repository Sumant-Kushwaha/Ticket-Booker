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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import com.amigo.ticketbooker.services.automaticBooking.BookingForm
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun JourneyDetailsSection(
    formState: BookingForm,
    onFormStateChange: (BookingForm) -> Unit,
    onDatePickerClick: () -> Unit
) {
    var classExpanded by remember { mutableStateOf(false) }
    var quotaExpanded by remember { mutableStateOf(false) }
    val spaceBetween= 8.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Payment Provider",
            Modifier.padding(bottom = 10.dp),
            color = Color.Red,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Date of Journey
        Box(
            modifier = Modifier.fillMaxWidth()
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

        Spacer(modifier = Modifier.height(8.dp))

        // Stations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = formState.fromStation,
                onValueChange = { onFormStateChange(formState.copy(fromStation = it.uppercase())) },
                label = { Text("From") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = formState.fromStation.isBlank()
            )
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swap stations",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
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

        Spacer(modifier = Modifier.height(8.dp))

        // Train Number
        OutlinedTextField(
            value = formState.trainNumber,
            onValueChange = { onFormStateChange(formState.copy(trainNumber = it)) },
            label = { Text("Train Number") },
            leadingIcon = { Icon(Icons.Default.DirectionsRailway, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Boarding Station
        OutlinedTextField(
            value = formState.boardingStation ?: "",
            onValueChange = {
                onFormStateChange(formState.copy(boardingStation = it.ifBlank { null }))
            },
            label = { Text("Boarding Station (Optional)") },
            leadingIcon = { Icon(Icons.Default.Train, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(formState.fromStation, fontSize = 12.sp) },
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mobile Number
        OutlinedTextField(
            value = formState.mobileNumber,
            onValueChange = {
                // Only allow digits and limit to 10 characters
                if (it.length <= 10 && (it.isEmpty() || it.all { char -> char.isDigit() })) {
                    onFormStateChange(formState.copy(mobileNumber = it))
                }
            },
            label = { Text("Mobile Number") },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+91",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            ,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = formState.mobileNumber.isNotEmpty() && formState.mobileNumber.length != 10,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = androidx.compose.ui.text.input.ImeAction.Done
            ),
            supportingText = {
                if (formState.mobileNumber.isNotEmpty() && formState.mobileNumber.length != 10) {
                    Text("Please enter a valid 10-digit mobile number")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Class Dropdown
        DropdownField(
            label = "Class",
            value = formState.classType,
            options = listOf("SL", "3A", "2A", "1A", "CC", "EC"),
            expanded = classExpanded,
            onExpandedChange = { classExpanded = it },
            onOptionSelected = { onFormStateChange(formState.copy(classType = it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

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
