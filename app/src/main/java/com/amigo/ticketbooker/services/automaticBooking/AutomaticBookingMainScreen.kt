package com.amigo.ticketbooker.services.automaticBooking


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.ui.ServiceTopBar
import java.util.*
import com.amigo.ticketbooker.model.Passenger
import com.amigo.ticketbooker.services.automaticBooking.bookingForm.BookingFormScreen

enum class ClassType(val displayName: String) {
    SL("SL"),
    THREE_A("3A"),
    TWO_A("2A"),
    ONE_A("1A"),
    CC("CC"),
    EC("EC");

    override fun toString() = displayName
}


data class BookingForm(
    val id: String? = null,
    val name: String = "",
    val username: String = "",
    val password: String = "",
    val fromStation: String = "",
    val toStation: String = "",
    val date: String = "",
    val trainNumber: String = "",
    val classType: String = "",
    val quota: String = "",
    val passengers: Int = 0,
    val passengerDetails: List<Passenger> = emptyList(),
    val boardingStation: String? = null, // Added boardingStation property
    val mobileNumber: String = "", // Added mobile number field for journey details
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomaticBookingScreen() {
    val navController = LocalNavController.current

    // In a real app, this would come from a ViewModel
    var savedForms by remember { mutableStateOf<List<BookingForm>>(emptyList()) }
    var showFormScreen by remember { mutableStateOf<BookingForm?>(null) }

    // Show form screen if needed
    if (showFormScreen != null) {
        BookingFormScreen(
            formId = showFormScreen?.id,
            onSave = { form ->
                savedForms = if (showFormScreen?.id != null) {
                    // Update existing form
                    savedForms.map { if (it.id == showFormScreen?.id) form else it }
                } else {
                    // Add new form
                    savedForms + form
                }
                showFormScreen = null
            }
        )
        return
    }

    val showEmptyState = savedForms.isEmpty()

    Scaffold(
        topBar = {
            ServiceTopBar(
                title = "Automatic Booking",
                onBackPressed = { navController.navigateUp() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFormScreen = BookingForm(name = "", fromStation = "", toStation = "", date = "") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Form")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showEmptyState) {
                EmptyFormsList(
                    onAddNewClick = { showFormScreen = BookingForm(name = "", fromStation = "", toStation = "", date = "") }
                )
            } else {
                SavedFormsList(
                    forms = savedForms,
                    onFormClick = { _ -> /* Handle form click */ },
                    onEditClick = { form -> showFormScreen = form },
                    onDeleteClick = { form ->
                        savedForms = savedForms.filter { it.id != form.id }
                    },
                    onCloneClick = { form ->
                        showFormScreen = form.copy(id = UUID.randomUUID().toString())
                    },
                    onAutomationClick = {
                        // TODO: Implement automation logic here
                        // This will be called when the Start Automation button is clicked
                    }
                )
            }
        }
    }
}










