package com.amigo.ticketbooker.services.automaticBooking


import android.content.Context
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import android.os.Environment
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File

enum class ClassType(val displayName: String) {
    SL("SL"),
    THREE_A("3A"),
    TWO_A("2A"),
    ONE_A("1A"),
    CC("CC"),
    EC("EC");

    override fun toString() = displayName
}



@Serializable
// Extended to include payment details
 data class BookingForm(
    val id: String? = null,
    val formName: String = "", // <-- Add this line
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
    // Payment Details
    val paymentMode: String = "", // UPI, CARDS, NET_BANKING, WALLETS
    val paymentProvider: String = "",
    val paymentDetails: Map<String, String> = emptyMap() // Generic map for flexibility
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomaticBookingScreen() {
    var showCloneFormNameDialog by remember { mutableStateOf(false) }
    var cloneFormName by remember { mutableStateOf("") }
    var cloneSourceForm by remember { mutableStateOf<BookingForm?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current

    // In a real app, this would come from a ViewModel
    val context = LocalNavController.current.context
    var savedForms by remember { mutableStateOf<List<BookingForm>>(emptyList()) }
    var showFormScreen by remember { mutableStateOf<BookingForm?>(null) }
    var showFormNameDialog by remember { mutableStateOf(false) }
    var pendingFormName by remember { mutableStateOf("") }

    // Helper to get next available 3-digit numeric ID
    fun getNextFormId(forms: List<BookingForm>): String {
        val maxId = forms.mapNotNull { it.id?.toIntOrNull() }.maxOrNull() ?: 0
        return String.format("%03d", maxId + 1)
    }

    // Case-insensitive check for duplicate form names
    fun formNameExists(name: String, forms: List<BookingForm>): Boolean {
        return forms.any { it.formName.trim().equals(name.trim(), ignoreCase = true) }
    }

    // Load saved form if exists
    LaunchedEffect(Unit) {
        val loadedForms = loadAllBookingFormsFromStorage(context)
        savedForms = loadedForms
    }

    // Show form screen if needed
    if (showFormScreen != null) {
        BookingFormScreen(
            bookingForm = showFormScreen,
            onSave = { form ->
                val isFormValid =
                    form.username.isNotBlank() &&
                    form.password.isNotBlank() &&
                    form.fromStation.isNotBlank() &&
                    form.toStation.isNotBlank() &&
                    form.date.isNotBlank() &&
                    form.passengerDetails.size == form.passengers
                if (isFormValid) {
                    // If form name changed, delete old file
                    if (pendingFormName != form.formName && pendingFormName.isNotBlank()) {
                        val oldFile = getBookingFormFile(context, pendingFormName)
                        if (oldFile.exists()) oldFile.delete()
                    }
                    saveBookingFormToStorage(context, form, form.formName)
                    savedForms = loadAllBookingFormsFromStorage(context)
                    // Keep the form open; just update its state reference
                    showFormScreen = form
                    pendingFormName = ""
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Form saved successfully")
                    }
                }
                // else: validation and snackbar handled in BookingFormScreen
            },
            snackbarHostState = snackbarHostState
        )
        return
    }

    val showEmptyState = savedForms.isEmpty()


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ServiceTopBar(
                title = "Automatic Booking",
                onBackPressed = { navController.navigateUp() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFormNameDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Form")
            }

            if (showFormNameDialog) {
                AlertDialog(
                    onDismissRequest = { showFormNameDialog = false },
                    title = { Text("Enter Form Name") },
                    text = {
                        OutlinedTextField(
                            value = pendingFormName,
                            onValueChange = { pendingFormName = it },
                            label = { Text("Form Name") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (pendingFormName.isNotBlank()) {
                                    if (formNameExists(pendingFormName, savedForms)) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Please enter a unique name for the form.",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        val newId = getNextFormId(savedForms)
                                        showFormScreen = BookingForm(id = newId, formName = pendingFormName)
                                        showFormNameDialog = false
                                    }
                                }
                            },
                            enabled = pendingFormName.isNotBlank()
                        ) { Text("Create") }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showFormNameDialog = false }) { Text("Cancel") }
                    }
                )
            }
            if (showCloneFormNameDialog) {
                AlertDialog(
                    onDismissRequest = { showCloneFormNameDialog = false },
                    title = { Text("Enter Name for Cloned Form") },
                    text = {
                        OutlinedTextField(
                            value = cloneFormName,
                            onValueChange = { cloneFormName = it },
                            label = { Text("Cloned Form Name") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (cloneFormName.isNotBlank() && cloneSourceForm != null) {
                                    if (formNameExists(cloneFormName, savedForms)) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Please enter a unique name for the form.",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        val newId = getNextFormId(savedForms)
                                        val cloned = cloneSourceForm!!.copy(id = newId, formName = cloneFormName)
                                        saveBookingFormToStorage(context, cloned, cloneFormName)
                                        savedForms = loadAllBookingFormsFromStorage(context)
                                        showCloneFormNameDialog = false
                                        cloneSourceForm = null
                                        cloneFormName = ""
                                    }
                                }
                            },
                            enabled = cloneFormName.isNotBlank()
                        ) { Text("Clone") }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showCloneFormNameDialog = false; cloneSourceForm = null; cloneFormName = "" }) { Text("Cancel") }
                    }
                )
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
                    onAddNewClick = { showFormNameDialog = true }
                )
            } else {
                SavedFormsList(
                    forms = savedForms,
                    onFormClick = { _ -> /* Handle form click */ },
                    onEditClick = { form ->
    showFormScreen = form
    pendingFormName = form.formName // Track the original name for editing
},
                    onDeleteClick = { form ->
                        // Remove file from storage
                        deleteBookingFormFromStorage(context, form)
                        // Reload all forms from storage so only the deleted form disappears
                        savedForms = loadAllBookingFormsFromStorage(context)
                    },
                    onCloneClick = { form ->
                        cloneSourceForm = form
                        cloneFormName = ""
                        showCloneFormNameDialog = true
                    }
                )
            }
        }
    }
}


fun getBookingFormFile(context: android.content.Context, formName: String): java.io.File {
    val mediaDir = java.io.File(android.os.Environment.getExternalStoragePublicDirectory("Android/media"), context.packageName)
    if (!mediaDir.exists()) mediaDir.mkdirs()
    return java.io.File(mediaDir, "$formName.json")
}

@OptIn(ExperimentalSerializationApi::class)
val jsonFormatter = Json {
    prettyPrint = true
    prettyPrintIndent = "  " // 2-space indentation, optional
}

fun saveBookingFormToStorage(context: Context, form: BookingForm, formName: String) {
    try {
        val file = getBookingFormFile(context, formName)
        val formWithName = form.copy(formName = formName)
        val json = jsonFormatter.encodeToString(formWithName)
        file.writeText(json)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun deleteBookingFormFromStorage(context: android.content.Context, form: BookingForm) {
    // Try to find the file by matching BookingForm content
    val dir = java.io.File(android.os.Environment.getExternalStoragePublicDirectory("Android/media"), context.packageName)
    val files = dir.listFiles()?.filter { it.extension == "json" } ?: return
    for (file in files) {
        try {
            val json = file.readText()
            val loadedForm = Json.decodeFromString<BookingForm>(json)
            if (loadedForm.id == form.id) {
                file.delete()
                break
            }
        } catch (_: Exception) {}
    }
}

fun loadAllBookingFormsFromStorage(context: android.content.Context): List<BookingForm> {
    val dir = java.io.File(android.os.Environment.getExternalStoragePublicDirectory("Android/media"), context.packageName)
    val files = dir.listFiles()?.filter { it.extension == "json" } ?: return emptyList()
    return files.mapNotNull { file ->
        try {
            val json = file.readText()
            Json.decodeFromString<BookingForm>(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}