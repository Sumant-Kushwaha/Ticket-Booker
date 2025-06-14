package com.amigo.ticketbooker.services.automaticBooking.bookingForm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.data.model.DataLists
import com.amigo.ticketbooker.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerDialog(
    passenger: Passenger?,
    onSave: (Passenger) -> Unit,
    onDismiss: () -> Unit
) {
    var tempPassenger by remember { mutableStateOf(passenger ?: Passenger()) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val initialPassenger = remember(passenger) { passenger ?: Passenger() }
    
    // Check if there are unsaved changes
    val hasUnsavedChanges = remember(tempPassenger, initialPassenger) {
        tempPassenger != initialPassenger && 
        (tempPassenger.name.isNotBlank() || tempPassenger.childAge.isNotBlank())
    }
    var genderExpanded by remember { mutableStateOf(false) }
    var childAgeExpanded by remember { mutableStateOf(false) }
    var countryExpanded by remember { mutableStateOf(false) }
    var berthPrefExpanded by remember { mutableStateOf(false) }
    var mealPrefExpanded by remember { mutableStateOf(false) }
    
    val isFormValid = tempPassenger.name.isNotBlank() &&
            (tempPassenger.isChild || tempPassenger.age in 5..120) &&
            (!tempPassenger.isChild || tempPassenger.childAge.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Passenger Details") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Adult / Child Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val selectedColor = MaterialTheme.colorScheme.primary
                    val unselectedColor = Color.Transparent
                    val selectedTextColor = MaterialTheme.colorScheme.onPrimary
                    val unselectedTextColor = MaterialTheme.colorScheme.onSurface

                    Button(
                        onClick = { tempPassenger = tempPassenger.copy(isChild = false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!tempPassenger.isChild) selectedColor else unselectedColor,
                            contentColor = if (!tempPassenger.isChild) selectedTextColor else unselectedTextColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        Text("Adult")
                    }

                    Button(
                        onClick = { tempPassenger = tempPassenger.copy(isChild = true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tempPassenger.isChild) selectedColor else unselectedColor,
                            contentColor = if (tempPassenger.isChild) selectedTextColor else unselectedTextColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        Text("Child")
                    }
                }

                // Full Name (common)
                OutlinedTextField(
                    value = tempPassenger.name,
                    onValueChange = { tempPassenger = tempPassenger.copy(name = it) },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = tempPassenger.name.isBlank()
                )

                // Age / Child Age
                if (tempPassenger.isChild) {
                    // Child Age Dropdown
                    ExposedDropdownMenuBox(
                        expanded = childAgeExpanded,
                        onExpandedChange = { childAgeExpanded = it }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = tempPassenger.childAge.ifEmpty { "Select Age" },
                            onValueChange = { },
                            label = { Text("Child's Age") },
                            leadingIcon = { Icon(Icons.Default.ChildCare, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = childAgeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            isError = tempPassenger.childAge.isBlank()
                        )
                        ExposedDropdownMenu(
                            expanded = childAgeExpanded,
                            onDismissRequest = { childAgeExpanded = false }
                        ) {
                            DataLists.childAgeOptions.forEach { age ->
                                DropdownMenuItem(
                                    text = { Text(age) },
                                    onClick = {
                                        tempPassenger = tempPassenger.copy(childAge = age)
                                        childAgeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Adult Age
                    OutlinedTextField(
                        value = if (tempPassenger.age > 0) tempPassenger.age.toString() else "",
                        onValueChange = {
                            val age = it.filter { char -> char.isDigit() }.take(3).toIntOrNull() ?: 0
                            if (age in 0..120) {
                                tempPassenger = tempPassenger.copy(age = age)
                            }
                        },
                        label = { Text("Age") },
                        leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = tempPassenger.age !in 5..120,
                        supportingText = {
                            if (tempPassenger.age !in 5..120) {
                                Text("Age must be between 5 and 120")
                            }
                        }
                    )
                }

                // Gender Dropdown (common)
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = it }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = tempPassenger.gender.name.replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        label = { Text("Gender") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        DataLists.genders.forEach { gender ->
                            DropdownMenuItem(
                                text = { Text(gender.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    tempPassenger = tempPassenger.copy(gender = Gender.valueOf(gender.uppercase()))
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }

                // Country Dropdown (only for adults)
                if (!tempPassenger.isChild) {
                    ExposedDropdownMenuBox(
                        expanded = countryExpanded,
                        onExpandedChange = { countryExpanded = it }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = tempPassenger.country.ifEmpty { "Select Country" },
                            onValueChange = { },
                            label = { Text("Country") },
                            leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = countryExpanded,
                            onDismissRequest = { countryExpanded = false }
                        ) {
                            DataLists.countries.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text(country) },
                                    onClick = {
                                        tempPassenger = tempPassenger.copy(country = country)
                                        countryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Berth Preference (only for adults)
                if (!tempPassenger.isChild) {
                    ExposedDropdownMenuBox(
                        expanded = berthPrefExpanded,
                        onExpandedChange = { berthPrefExpanded = it }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = tempPassenger.berthPreference.name.replace("_", " ").replaceFirstChar { it.uppercase() },
                            onValueChange = { },
                            label = { Text("Berth Preference") },
                            leadingIcon = { Icon(Icons.Default.AirlineSeatReclineNormal, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = berthPrefExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = berthPrefExpanded,
                            onDismissRequest = { berthPrefExpanded = false }
                        ) {
                            DataLists.berthPreferences.forEach { berth ->
                                DropdownMenuItem(
                                    text = { Text(berth) },
                                    onClick = {
                                        tempPassenger = tempPassenger.copy(
                                            berthPreference = BerthPreference.valueOf(
                                                berth.uppercase().replace(" ", "_")
                                            )
                                        )
                                        berthPrefExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Meal Preference (only for adults)
                    ExposedDropdownMenuBox(
                        expanded = mealPrefExpanded,
                        onExpandedChange = { mealPrefExpanded = it }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = tempPassenger.mealPreference.name.replace("_", " ").replaceFirstChar { it.uppercase() },
                            onValueChange = { },
                            label = { Text("Meal Preference") },
                            leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealPrefExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = mealPrefExpanded,
                            onDismissRequest = { mealPrefExpanded = false }
                        ) {
                            DataLists.mealPreferences.forEach { meal ->
                                DropdownMenuItem(
                                    text = { Text(meal) },
                                    onClick = {
                                        tempPassenger = tempPassenger.copy(
                                            mealPreference = MealPreference.valueOf(
                                                meal.uppercase().replace(" ", "_")
                                            )
                                        )
                                        mealPrefExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Valid Concession (only for adults)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tempPassenger.hasValidConcession,
                            onCheckedChange = {
                                tempPassenger = tempPassenger.copy(hasValidConcession = it)
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Has Valid Concession")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(tempPassenger) },
                enabled = isFormValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { 
                    if (hasUnsavedChanges) {
                        showDiscardDialog = true
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("Cancel")
            }
        }
    )
}
