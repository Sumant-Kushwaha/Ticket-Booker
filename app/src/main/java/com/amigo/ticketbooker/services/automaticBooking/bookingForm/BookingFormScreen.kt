package com.amigo.ticketbooker.services.automaticBooking.bookingForm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.services.automaticBooking.BookingForm
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.saveButton.BookingFAB
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.bottombar.BookingBottomBar
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.credentials.CredentialsSection
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.formname.BookingDetailsSection
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.journeydetails.JourneyDetailsSection
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.passengerdetailsection.PassengerDetailsSection
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.passengerdialog.PassengerDialog
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.topbar.BookingTopBar
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.components.passengerdialog.datepicker.DatePickerDialogComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    bookingForm: BookingForm? = null,
    onSave: (BookingForm) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    // Local context is available if needed
    LocalContext.current

    var formState by remember {
        mutableStateOf(
            bookingForm ?: BookingForm(
                fromStation = "",
                toStation = "",
                date = ""
            )
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showAddPassengerDialog by remember { mutableStateOf(false) }
    var editingPassengerIndex by remember { mutableStateOf<Int?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<Int?>(null) }
    

    val isFormValid =
            formState.username.isNotBlank() &&
            formState.password.isNotBlank() &&
            formState.fromStation.isNotBlank() &&
            formState.toStation.isNotBlank() &&
            formState.date.isNotBlank() &&
            formState.passengerDetails.size == formState.passengers &&
            formState.paymentMode.isNotBlank() &&
            formState.paymentProvider.isNotBlank() &&
            formState.paymentDetails.isNotEmpty()

    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    if (snackbarMessage != null) {
        LaunchedEffect(snackbarMessage) {
            snackbarHostState.showSnackbar(snackbarMessage!!)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = { BookingTopBar(bookingForm != null) },
        bottomBar = { BookingBottomBar(isFormValid) {
            if (isFormValid) {
                onSave(formState)
            } else {
                snackbarMessage = "Please fill all fields before saving."
            }
        } },
        floatingActionButton = { BookingFAB(isFormValid) {
            if (isFormValid) {
                onSave(formState)
            } else {
                snackbarMessage = "Please fill all fields before saving."
            }
        } }
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
                    BookingDetailsSection()
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
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.Transparent)
                ) {
                    JourneyDetailsSection(
                        formState = formState,
                        onFormStateChange = { formState = it },
                        onDatePickerClick = { showDatePicker = true }
                    )
                }
            }

            PassengerDetailsSection(
                passengers = formState.passengerDetails,
                paymentMode = formState.paymentMode.takeIf { it.isNotBlank() },
                paymentProvider = formState.paymentProvider.takeIf { it.isNotBlank() },
                paymentDetails = formState.paymentDetails,
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
                },
                onDeletePassengerClick = { index ->
                    showDeleteConfirmation = index
                },
                onPaymentModeChange = { mode ->
                    formState = formState.copy(paymentMode = mode, paymentProvider = "", paymentDetails = emptyMap())
                },
                onPaymentProviderChange = { provider ->
                    formState = formState.copy(paymentProvider = provider, paymentDetails = emptyMap())
                },
                onPaymentDetailsChange = { details ->
                    formState = formState.copy(paymentDetails = details)
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

    // Delete Confirmation Dialog
    showDeleteConfirmation?.let { index ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Delete Passenger") },
            text = { Text("Are you sure you want to delete this passenger?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedPassengers = formState.passengerDetails.toMutableList()
                        updatedPassengers.removeAt(index)
                        formState = formState.copy(
                            passengerDetails = updatedPassengers,
                            passengers = updatedPassengers.size
                        )
                        showDeleteConfirmation = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
