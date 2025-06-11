package com.amigo.ticketbooker.services.automaticBooking.bookingForm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.services.automaticBooking.BookingForm
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    formId: String? = null,
    onSave: (BookingForm) -> Unit
) {
    // Local context is available if needed
    LocalContext.current

    val existingForm = remember(formId) {
        if (formId != null) {
            BookingForm(
                id = formId,
                name = "",
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

    var showDatePicker by remember { mutableStateOf(false) }
    var showAddPassengerDialog by remember { mutableStateOf(false) }
    var editingPassengerIndex by remember { mutableStateOf<Int?>(null) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val isFormValid = formState.name.isNotBlank() &&
            formState.username.isNotBlank() &&
            formState.password.isNotBlank() &&
            formState.fromStation.isNotBlank() &&
            formState.toStation.isNotBlank() &&
            formState.date.isNotBlank() &&
            formState.passengerDetails.size == formState.passengers

    Scaffold(
        topBar = { BookingTopBar(existingForm != null) },
        bottomBar = { BookingBottomBar(isFormValid) { onSave(formState) } },
        floatingActionButton = { BookingFAB(isFormValid) { onSave(formState) } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Form Name Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    BookingDetailsSection(
                        formState = formState,
                        onFormStateChange = { formState = it }
                    )
                }
            }

            // User ID and Password Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    CredentialsSection(
                        formState = formState,
                        onFormStateChange = { formState = it }
                    )
                }
            }

            // Journey Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    JourneyDetailsSection(
                        formState = formState,
                        onFormStateChange = { formState = it },
                        onDatePickerClick = { showDatePicker = true }
                    )
                }
            }

            PassengerDetailsSection(
                passengers = formState.passengerDetails,
                onPassengerChange = { updatedPassengers ->
                    formState = formState.copy(
                        passengerDetails = updatedPassengers,
                        passengers = updatedPassengers.size
                    )
                },
                onAddPassengerClick = {
                    editingPassengerIndex = formState.passengerDetails.size
                    showAddPassengerDialog = true
                },
                onEditPassengerClick = { index ->
                    editingPassengerIndex = index
                    showAddPassengerDialog = true
                }
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialogComponent(
            initialDate = formState.date,
            onDateSelected = { selectedDate ->
                formState = formState.copy(date = selectedDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showAddPassengerDialog) {
        PassengerDialog(
            passenger = formState.passengerDetails.getOrNull(editingPassengerIndex ?: 0),
            onSave = { passenger ->
                val updatedPassengers = formState.passengerDetails.toMutableList()
                if (editingPassengerIndex != null && editingPassengerIndex!! < updatedPassengers.size) {
                    updatedPassengers[editingPassengerIndex!!] = passenger
                } else {
                    updatedPassengers.add(passenger)
                }
                formState = formState.copy(
                    passengerDetails = updatedPassengers,
                    passengers = updatedPassengers.size
                )
                showAddPassengerDialog = false
            },
            onDismiss = { showAddPassengerDialog = false }
        )
    }
}
