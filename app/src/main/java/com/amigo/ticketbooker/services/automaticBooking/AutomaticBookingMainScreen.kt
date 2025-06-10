package com.amigo.ticketbooker.services.automaticBooking


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.fontFamily
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
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val username: String = "",
    val password: String = "",
    val fromStation: String,
    val toStation: String,
    val date: String,
    val trainNumber: String = "",
    val classType: String = ClassType.SL.name,
    val quota: String = "GENERAL",
    val passengers: Int = 1,
    val lastModified: Long = System.currentTimeMillis(),
    val passengerDetails: List<Passenger> = emptyList()
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
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyFormsList(
    onAddNewClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Booking Forms",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = fontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create your first booking form to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddNewClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Form")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedFormsList(
    forms: List<BookingForm>,
    onFormClick: (BookingForm) -> Unit,
    onEditClick: (BookingForm) -> Unit,
    onDeleteClick: (BookingForm) -> Unit,
    onCloneClick: (BookingForm) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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

        items(forms, key = { it.id }) { form ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingFormCard(
    form: BookingForm,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClone: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = form.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                // Last modified time
                Text(
                    text = "${form.passengers} ${if (form.passengers == 1) "Passenger" else "Passengers"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route info
            FormDetailRow(icon = Icons.Default.Train, text = "${form.fromStation} → ${form.toStation}")
            FormDetailRow(icon = Icons.Default.DateRange, text = form.date)

            if (form.trainNumber.isNotEmpty()) {
                FormDetailRow(icon = Icons.Default.DirectionsRailway, text = "Train: ${form.trainNumber}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onClone() }) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Clone",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onEdit() }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onDelete() }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun FormDetailRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


