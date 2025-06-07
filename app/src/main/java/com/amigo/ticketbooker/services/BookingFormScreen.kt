package com.amigo.ticketbooker.services

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import com.amigo.ticketbooker.model.BerthPreference
import com.amigo.ticketbooker.model.Gender
import com.amigo.ticketbooker.model.Passenger
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassengerCard(
    passenger: Passenger,
    index: Int,
    onEditClick: (Int) -> Unit
) {
    Card(
        onClick = { onEditClick(index) },
        modifier = Modifier
            .width(220.dp)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Passenger ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (passenger.name.isNotBlank()) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Passenger",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                passenger.name.ifEmpty { "Tap to add details" },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (passenger.name.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )

            if (passenger.name.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${passenger.age} • ${passenger.gender}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (passenger.berthPreference != BerthPreference.NO_PREFERENCE) {
                    Text(
                        "Berth: ${passenger.berthPreference}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    formId: String? = null,
    onSave: (BookingForm) -> Unit
) {
    // Local context is available if needed
    LocalContext.current

    // In a real app, this would be loaded from a ViewModel
    val existingForm = remember(formId) {
        // If formId is not null, load the existing form data
        // For now, we'll create a sample form for demonstration
        if (formId != null) {
            BookingForm(
                id = formId,
                name = "Form Name",
                fromStation = "HW",
                toStation = "BMKI",
                date = "Date",
                trainNumber = "Train Number",
                classType = "Class",
                passengers = 2
            )
        } else null
    }

    var formState by remember {
        mutableStateOf(
            existingForm ?: BookingForm(
                name = "",
                fromStation = "",
                toStation = "",
                date = ""
            )
        )
    }

    val classOptions = listOf("SL", "3A", "2A", "1A", "CC", "EC")
    val quotaOptions = listOf(
        "GENERAL" to "General",
        "TATKAL" to "Tatkal",
        "PREMIUM_TATKAL" to "Premium Tatkal",
        "LADIES" to "Ladies",
        "LOWER_BERTH" to "Lower Berth",
        "PERSON_WITH_DISABILITY" to "Person with Disability",
        "DUTY_PASS" to "Duty Pass",
        "TATKAL_AC" to "Tatkal AC",
        "PREMIUM_TATKAL_AC" to "Premium Tatkal AC"
    )

    // Form state
    var passwordVisible by remember { mutableStateOf(false) }
    var quotaExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showAddPassengerDialog by remember { mutableStateOf(false) }
    var editingPassengerIndex by remember { mutableStateOf<Int?>(null) }

    // Form validation
    val isFormValid = formState.name.isNotBlank() &&
            formState.username.isNotBlank() &&
            formState.password.isNotBlank() &&
            formState.fromStation.isNotBlank() &&
            formState.toStation.isNotBlank() &&
            formState.date.isNotBlank() &&
            formState.passengerDetails.size == formState.passengers

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingForm != null) "Edit Booking" else "New Booking") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Implement back press */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isFormValid) {
                        onSave(formState)
                        /*TODO: Implement navigation*/
                    }
                },
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
                text = { Text("Save Booking") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Form Title
            Text(
                text = "Booking Details",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Form Fields
            OutlinedTextField(
                value = formState.name,
                onValueChange = { formState = formState.copy(name = it) },
                label = { Text("Form Name") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                isError = formState.name.isBlank()
            )

            // Username and Password
            OutlinedTextField(
                value = formState.username,
                onValueChange = { formState = formState.copy(username = it) },
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
                onValueChange = { formState = formState.copy(password = it) },
                label = { Text("IRCTC Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Outlined.Visibility
                    else Icons.Outlined.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        androidx.compose.material3.Icon(
                            image,
                            if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                isError = formState.password.isBlank()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = formState.fromStation,
                    onValueChange = { formState = formState.copy(fromStation = it.uppercase()) },
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
                            formState = formState.copy(
                                fromStation = formState.toStation,
                                toStation = temp
                            )
                        }
                )

                OutlinedTextField(
                    value = formState.toStation,
                    onValueChange = { formState = formState.copy(toStation = it.uppercase()) },
                    label = { Text("To") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = formState.toStation.isBlank()
                )
            }

            // Date Picker Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = formState.date,
                    onValueChange = {},
                    label = { Text("Date of Journey") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true,
                    isError = formState.date.isBlank(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Date"
                        )
                    }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0f)
                        .clickable { showDatePicker = true }
                )
            }

            // Date Picker Dialog
            if (showDatePicker) {
                val calendar = Calendar.getInstance()
                val today = calendar.timeInMillis

                // Reset calendar to today and add 60 days for max date
                calendar.timeInMillis = today
                calendar.add(Calendar.DAY_OF_MONTH, 60)
                val maxDate = calendar.timeInMillis

                // Calculate initial selected date
                val initialDate = try {
                    if (formState.date.isNotBlank()) {
                        dateFormatter.parse(formState.date)?.time?.coerceIn(today, maxDate)
                            ?: today
                    } else {
                        today
                    }
                } catch (e: Exception) {
                    today
                }

                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = initialDate,
                    initialDisplayedMonthMillis = today,
                    yearRange = IntRange(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.YEAR) + 1
                    )
                )

                // Function to check if a date is within the allowed range
                fun isDateValid(dateMillis: Long): Boolean {
                    return dateMillis in today..maxDate
                }

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            enabled = datePickerState.selectedDateMillis?.let { isDateValid(it) }
                                ?: false,
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    if (isDateValid(millis)) {
                                        try {
                                            val selectedDate = Date(millis)
                                            formState = formState.copy(
                                                date = dateFormatter.format(selectedDate)
                                            )
                                        } catch (e: Exception) {
                                            // Handle date formatting error
                                            formState = formState.copy(date = "")
                                        }
                                    }
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    // Create a custom date picker that only allows selection of valid dates
                    Column {
                        Text(
                            "Select date (next 60 days)",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                        DatePicker(
                            state = datePickerState,
                            showModeToggle = false
                        )
                    }
                }
            }

            OutlinedTextField(
                value = formState.trainNumber,
                onValueChange = { formState = formState.copy(trainNumber = it) },
                label = { Text("Train Number (Optional)") },
                leadingIcon = { Icon(Icons.Default.DirectionsRailway, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            var classExpanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = formState.classType,
                    onValueChange = {},
                    label = { Text("Class") },
                    leadingIcon = { Icon(Icons.Default.Class, null) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            if (classExpanded) Icons.Filled.ArrowDropUp
                            else Icons.Filled.ArrowDropDown,
                            "Class dropdown"
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Invisible clickable surface to handle dropdown
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0f)
                        .clickable { classExpanded = true }
                )

                // Class dropdown menu
                DropdownMenu(
                    expanded = classExpanded,
                    onDismissRequest = { classExpanded = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    classOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                formState = formState.copy(classType = option)
                                classExpanded = false
                            }
                        )
                    }
                }
            }


            // Quota Dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = quotaOptions.find { it.first == formState.quota }?.second ?: "Select Quota",
                    onValueChange = {},
                    label = { Text("Quota") },
                    leadingIcon = { Icon(Icons.Default.ConfirmationNumber, null) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            if (quotaExpanded) Icons.Filled.ArrowDropUp
                            else Icons.Filled.ArrowDropDown,
                            "Quota dropdown"
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )


                // Invisible clickable surface to handle dropdown
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0f)
                        .clickable { quotaExpanded = true }
                )

                // Quota dropdown menu
                DropdownMenu(
                    expanded = quotaExpanded,
                    onDismissRequest = { quotaExpanded = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    quotaOptions.forEach { (value, displayName) ->
                        DropdownMenuItem(
                            text = { Text(displayName) },
                            onClick = {
                                formState = formState.copy(quota = value)
                                quotaExpanded = false
                            }
                        )
                    }
                }
            }

            // Passenger Cards
            if (formState.passengerDetails.isNotEmpty()) {
                Text(
                    text = "Passenger Details",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }

                    // Delete Confirmation Dialog
                    if (showDeleteDialog != null) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = null },
                            title = { Text("Delete Passenger") },
                            text = { Text("Are you sure you want to delete this passenger?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showDeleteDialog?.let { index ->
                                            val updated = formState.passengerDetails.toMutableList()
                                            updated.removeAt(index)
                                            formState = formState.copy(
                                                passengerDetails = updated,
                                                passengers = updated.size
                                            )
                                        }
                                        showDeleteDialog = null
                                    }
                                ) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showDeleteDialog = null }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    formState.passengerDetails.forEachIndexed { index, passenger ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                // First Row: Number and Name
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = passenger.name.let {
                                            if (it.length > 17) it.take(14) + "..." else it
                                        }.ifEmpty { "Passenger ${index + 1}" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Action Buttons
                                    Row(
                                        modifier = Modifier.width(IntrinsicSize.Min),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(
                                            onClick = {
                                                editingPassengerIndex = index
                                                showAddPassengerDialog = true
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        IconButton(
                                            onClick = { showDeleteDialog = index },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                // Divider
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 1.dp
                                )

                                // Second Row: Age and Gender
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = "Age: ${passenger.age} yrs",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )
                                    Text(
                                        text = "Gender: ${passenger.gender.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Add Passenger Button
            if (formState.passengerDetails.size < 6) {
                OutlinedButton(
                    onClick = {
                        // Show the dialog to add passenger details first
                        editingPassengerIndex = formState.passengerDetails.size
                        showAddPassengerDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Passenger")
                }
            }

            // Passenger Dialog
            if (showAddPassengerDialog) {
                val passengerIndex = editingPassengerIndex ?: 0
                val passenger =
                    formState.passengerDetails.getOrNull(passengerIndex) ?: Passenger()
                var tempPassenger by remember { mutableStateOf(passenger) }

                val isNewPassenger = passengerIndex >= formState.passengerDetails.size

                AlertDialog(
                    onDismissRequest = {
                        showAddPassengerDialog = false
                    },
                    title = { Text("${if (isNewPassenger) "Add" else "Edit"} Passenger ${passengerIndex + 1} Details") },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = tempPassenger.name,
                                onValueChange = {
                                    tempPassenger = tempPassenger.copy(name = it)
                                },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = tempPassenger.age.toString(),
                                    onValueChange = {
                                        tempPassenger =
                                            tempPassenger.copy(age = it.toIntOrNull() ?: 0)
                                    },
                                    label = { Text("Age") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Number
                                    )
                                )

                                var genderExpanded by remember { mutableStateOf(false) }
                                val genderOptions = listOf("MALE", "FEMALE", "OTHER")
                                val genderDisplayNames = listOf("Male", "Female", "Other")

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .wrapContentSize(Alignment.TopStart)
                                ) {
                                    OutlinedTextField(
                                        value = when (tempPassenger.gender) {
                                            Gender.MALE -> "Male"
                                            Gender.FEMALE -> "Female"
                                            else -> "Other"
                                        },
                                        onValueChange = {},
                                        label = { Text("Gender") },
                                        modifier = Modifier.fillMaxWidth(),
                                        readOnly = true,
                                        trailingIcon = {
                                            Icon(
                                                if (genderExpanded) Icons.Filled.ArrowDropUp
                                                else Icons.Filled.ArrowDropDown,
                                                "Gender dropdown"
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        )
                                    )

                                    // Invisible clickable surface to handle dropdown
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .alpha(0f)
                                            .clickable { genderExpanded = true }
                                    )

                                    // Gender dropdown menu
                                    DropdownMenu(
                                        expanded = genderExpanded,
                                        onDismissRequest = { genderExpanded = false },
                                        properties = PopupProperties(focusable = true)
                                    ) {
                                        genderOptions.forEachIndexed { index, option ->
                                            val displayName = genderDisplayNames[index]
                                            DropdownMenuItem(
                                                text = { Text(displayName) },
                                                onClick = {
                                                    val selectedGender = Gender.valueOf(option)
                                                    tempPassenger =
                                                        tempPassenger.copy(gender = selectedGender)
                                                    genderExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            var berthExpanded by remember { mutableStateOf(false) }
                            val berthOptions = listOf(
                                BerthPreference.NO_PREFERENCE,
                                BerthPreference.LOWER,
                                BerthPreference.MIDDLE,
                                BerthPreference.UPPER,
                                BerthPreference.SIDE_LOWER,
                                BerthPreference.SIDE_UPPER
                            )

                            val berthDisplayNames = mapOf(
                                BerthPreference.NO_PREFERENCE to "No Preference",
                                BerthPreference.LOWER to "Lower",
                                BerthPreference.MIDDLE to "Middle",
                                BerthPreference.UPPER to "Upper",
                                BerthPreference.SIDE_LOWER to "Side Lower",
                                BerthPreference.SIDE_UPPER to "Side Upper"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(Alignment.TopStart)
                            ) {
                                OutlinedTextField(
                                    value = berthDisplayNames[tempPassenger.berthPreference]
                                        ?: "No Preference",
                                    onValueChange = {},
                                    label = { Text("Berth Preference") },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            if (berthExpanded) Icons.Filled.ArrowDropUp
                                            else Icons.Filled.ArrowDropDown,
                                            "Berth preference dropdown"
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                // Invisible clickable surface to handle dropdown
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .alpha(0f)
                                        .clickable { berthExpanded = true }
                                )

                                // Berth preference dropdown menu
                                DropdownMenu(
                                    expanded = berthExpanded,
                                    onDismissRequest = { berthExpanded = false },
                                    properties = PopupProperties(focusable = true)
                                ) {
                                    berthOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(berthDisplayNames[option] ?: "") },
                                            onClick = {
                                                tempPassenger =
                                                    tempPassenger.copy(berthPreference = option)
                                                berthExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            var mealExpanded by remember { mutableStateOf(false) }
                            val mealOptions =
                                listOf("Vegetarian", "Non-Vegetarian", "Jain", "Vegan")

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(Alignment.TopStart)
                            ) {
                                OutlinedTextField(
                                    value = tempPassenger.mealPreference,
                                    onValueChange = {},
                                    label = { Text("Meal Preference") },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            if (mealExpanded) Icons.Filled.ArrowDropUp
                                            else Icons.Filled.ArrowDropDown,
                                            "Meal preference dropdown"
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                // Invisible clickable surface to handle dropdown
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .alpha(0f)
                                        .clickable { mealExpanded = true }
                                )

                                // Meal preference dropdown menu
                                DropdownMenu(
                                    expanded = mealExpanded,
                                    onDismissRequest = { mealExpanded = false },
                                    properties = PopupProperties(focusable = true)
                                ) {
                                    mealOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                tempPassenger =
                                                    tempPassenger.copy(mealPreference = option)
                                                mealExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = tempPassenger.country,
                                onValueChange = {
                                    tempPassenger = tempPassenger.copy(country = it)
                                },
                                label = { Text("Country") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val updatedPassengers =
                                    formState.passengerDetails.toMutableList()
                                if (isNewPassenger) {
                                    // Only add the passenger if it's new and all fields are valid
                                    if (tempPassenger.name.isNotBlank() && tempPassenger.age > 0) {
                                        updatedPassengers.add(tempPassenger)
                                        formState = formState.copy(
                                            passengerDetails = updatedPassengers,
                                            passengers = updatedPassengers.size
                                        )
                                    }
                                } else if (passengerIndex < updatedPassengers.size) {
                                    // Update existing passenger
                                    updatedPassengers[passengerIndex] = tempPassenger
                                    formState = formState.copy(
                                        passengerDetails = updatedPassengers,
                                        passengers = updatedPassengers.size
                                    )
                                }
                                showAddPassengerDialog = false
                            },
                            enabled = tempPassenger.name.isNotBlank() &&
                                    tempPassenger.age > 0
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showAddPassengerDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
