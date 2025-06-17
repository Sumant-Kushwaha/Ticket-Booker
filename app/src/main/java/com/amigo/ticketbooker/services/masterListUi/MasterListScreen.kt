package com.amigo.ticketbooker.services.masterListUi

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.services.masterListUi.passengerDetailsStorage.PassengerFileStorage
import com.amigo.ticketbooker.services.masterListUi.passengerDetailsStorage.StoragePermissionHandler
import com.amigo.ticketbooker.ui.DeleteConfirmationDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import java.util.UUID
import com.amigo.ticketbooker.model.Passenger
import com.amigo.ticketbooker.model.Gender
import com.amigo.ticketbooker.model.BerthPreference
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

typealias Passenger = Passenger
typealias Gender = Gender
typealias BerthPreference = BerthPreference

// ViewModel for managing the master list of passengers
class MasterListViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val fileStorage = PassengerFileStorage(context)
    
    // State for storage permission status
    private val _storagePermissionGranted = MutableStateFlow(false)
    val storagePermissionGranted: StateFlow<Boolean> = _storagePermissionGranted.asStateFlow()
    
    // State for storage locations
    private val _storageLocations = MutableStateFlow<List<String>>(emptyList())
    val storageLocations: StateFlow<List<String>> = _storageLocations
    
    // State for passengers list
    private val _passengers = MutableStateFlow<List<Passenger>>(emptyList())
    val passengers: StateFlow<List<Passenger>> = _passengers.asStateFlow()
    
    init {
        // Check if we have storage permission and load data if available
        checkStoragePermission()
        
        // Get all possible storage locations
        _storageLocations.value = PassengerFileStorage.getAllPossibleFileLocations(getApplication())
    }
    
    /**
     * Check if we have storage permission and update state
     */
    fun checkStoragePermission() {
        val hasPermission = StoragePermissionHandler.hasStoragePermission(getApplication())
        _storagePermissionGranted.value = hasPermission
        
        if (hasPermission) {
            // If we have permission, load passengers from file
            loadPassengersFromFile()
        }
    }
    
    /**
     * Set storage permission granted and load/save data
     */
    fun onStoragePermissionResult(granted: Boolean) {
        _storagePermissionGranted.value = granted
        
        // Update storage locations
        _storageLocations.value = PassengerFileStorage.getAllPossibleFileLocations(getApplication())
        
        if (granted) {
            // If permission was just granted, load passengers from file
            loadPassengersFromFile()
            // And save current passengers to file
            savePassengersToFile(_passengers.value)
        }
    }
    
    /**
     * Load passengers from file
     */
    private fun loadPassengersFromFile() {
        viewModelScope.launch {
            val loadedPassengers = fileStorage.loadPassengers()
            if (loadedPassengers.isNotEmpty()) {
                _passengers.value = loadedPassengers
            }
            
            // Update storage locations after load attempt
            _storageLocations.value = PassengerFileStorage.getAllPossibleFileLocations(getApplication())
        }
    }
    
    /**
     * Save passengers to file
     */
    private fun savePassengersToFile(passengerList: List<Passenger>) {
        fileStorage.savePassengers(passengerList)
    }
    
    /**
     * Get sample passenger data
     * @return List of sample passengers
     */
    private fun loadSampleData(): List<Passenger> {
        val samplePassengers = listOf(
            Passenger(
                id = UUID.randomUUID().toString(),
                name = "Raj Kumar",
                age = 35,
                gender = Gender.MALE,
                country = "India",
                berthPreference = BerthPreference.LOWER
            ),
            Passenger(
                id = UUID.randomUUID().toString(),
                name = "Priya Singh",
                age = 28,
                gender = Gender.FEMALE,
                country = "India",
                berthPreference = BerthPreference.SIDE_LOWER
            ),
            Passenger(
                id = UUID.randomUUID().toString(),
                name = "Anil Sharma",
                age = 42,
                gender = Gender.MALE,
                country = "India",
                berthPreference = BerthPreference.UPPER
            )
        )
        
        // Also update the state
        _passengers.value = samplePassengers
        
        return samplePassengers
    }
    
    // State for currently edited passenger (null when not editing)
    private val _currentPassenger = MutableStateFlow<Passenger?>(null)
    val currentPassenger: StateFlow<Passenger?> = _currentPassenger.asStateFlow()
    
    // State for add/edit form visibility
    private val _isFormVisible = MutableStateFlow(false)
    val isFormVisible: StateFlow<Boolean> = _isFormVisible.asStateFlow()
    
    // Function to start adding a new passenger
    fun startAddPassenger() {
        _currentPassenger.value = null // Clear current passenger for adding new
        _isFormVisible.value = true
    }
    
    // Function to start editing an existing passenger
    fun startEditPassenger(passenger: Passenger) {
        _currentPassenger.value = passenger
        _isFormVisible.value = true
    }
    
    // Function to cancel form
    fun cancelForm() {
        _isFormVisible.value = false
        _currentPassenger.value = null
    }
    
    // Function to save passenger (add new or update existing)
    fun savePassenger(passenger: Passenger) {
        _passengers.update { currentList ->
            val isEdit = currentList.any { it.id == passenger.id }
            val updatedList = if (isEdit) {
                // Update existing passenger
                currentList.map { if (it.id == passenger.id) passenger else it }
            } else {
                // Add new passenger
                currentList + passenger
            }
            
            // Save to file if permission granted
            if (_storagePermissionGranted.value) {
                savePassengersToFile(updatedList)
            }
            
            updatedList
        }
        _isFormVisible.value = false
        _currentPassenger.value = null
    }
    
    // Function to request passenger deletion (shows confirmation dialog)
    private val _showDeleteConfirmation = MutableStateFlow<String?>(null) // Stores ID of passenger to delete, null when dialog not shown
    val showDeleteConfirmation: StateFlow<String?> = _showDeleteConfirmation.asStateFlow()
    
    fun requestDeletePassenger(id: String) {
        _showDeleteConfirmation.value = id
    }
    
    // Function to confirm passenger deletion
    fun confirmDeletePassenger() {
        val idToDelete = _showDeleteConfirmation.value ?: return
        _passengers.update { currentList ->
            val updatedList = currentList.filter { it.id != idToDelete }
            
            // Save to file if permission granted
            if (_storagePermissionGranted.value) {
                savePassengersToFile(updatedList)
            }
            
            updatedList
        }
        _showDeleteConfirmation.value = null // Hide confirmation dialog
    }
    
    // Function to cancel passenger deletion
    fun cancelDeletePassenger() {
        _showDeleteConfirmation.value = null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterListScreen(viewModel: MasterListViewModel = viewModel()) {
    val navController = LocalNavController.current
    val passengers by viewModel.passengers.collectAsState()
    val isFormVisible by viewModel.isFormVisible.collectAsState()
    val currentPassenger by viewModel.currentPassenger.collectAsState()
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()
    var showPermissionRequest by remember { mutableStateOf(false) }
    var showStorageLocationsDialog by remember { mutableStateOf(false) }
    
    if (showPermissionRequest) {
        StoragePermissionHandler.RequestStoragePermission(
            onPermissionGranted = {
                showPermissionRequest = false
                viewModel.onStoragePermissionResult(true)
            },
            onPermissionDenied = {
                showPermissionRequest = false
                viewModel.onStoragePermissionResult(false)
            }
        )
    }
    
    // Dialog to show storage locations
    if (showStorageLocationsDialog) {
        AlertDialog(
            onDismissRequest = { showStorageLocationsDialog = false },
            title = { Text("Passenger Data Storage Location") },
            text = { 
                Column {
                    Text(
                        "Your passenger data is stored at Android/media/com.amigo.ticketbooker",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showStorageLocationsDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Master List of Passengers",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Storage permission icon
                    val permissionGranted by viewModel.storagePermissionGranted.collectAsState()
                    if (permissionGranted) {
                        // Show save icon if permission is granted
                        IconButton(onClick = {
                            // Show storage locations dialog
                            showStorageLocationsDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = "Storage Permission Granted (Tap for locations)",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        // Show error icon if permission is not granted
                        IconButton(onClick = {
                            // Request storage permission
                            showPermissionRequest = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = "Storage Permission Required",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isFormVisible) {
                FloatingActionButton(
                    onClick = { viewModel.startAddPassenger() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Passenger"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            // Delete confirmation dialog
            if (showDeleteConfirmation != null) {
                DeleteConfirmationDialog(
                    onConfirm = { viewModel.confirmDeletePassenger() },
                    onDismiss = { viewModel.cancelDeletePassenger() }
                )
            }
            
            // Animated content container
            Box(modifier = Modifier.fillMaxSize()) {
                // Passenger list view
                AnimatedVisibility(
                    visible = !isFormVisible,
                    enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
                    exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
                ) {
                    PassengerListView(
                        passengers = passengers,
                        onEditPassenger = { viewModel.startEditPassenger(it) },
                        onDeletePassenger = { viewModel.requestDeletePassenger(it) }
                    )
                }
                
                // Add/Edit passenger form
                AnimatedVisibility(
                    visible = isFormVisible,
                    enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                    exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
                ) {
                    PassengerForm(
                        passenger = currentPassenger,
                        onSave = { viewModel.savePassenger(it) },
                        onCancel = { viewModel.cancelForm() }
                    )
                }
            }
        }
    }
}

@Composable
fun PassengerListView(
    passengers: List<Passenger>,
    onEditPassenger: (Passenger) -> Unit,
    onDeletePassenger: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with passenger count
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Text(
                text = "${passengers.size} Passengers Saved",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (passengers.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No passengers added yet",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add passengers to your master list for quick booking",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // Passenger list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(passengers, key = { it.id }) { passenger ->
                    PassengerCard(
                        passenger = passenger,
                        onEdit = { onEditPassenger(passenger) },
                        onDelete = { onDeletePassenger(passenger.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PassengerCard(
    passenger: Passenger,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Passenger name with gender icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when(passenger.gender) {
                            Gender.MALE -> Icons.Filled.Man
                            Gender.FEMALE -> Icons.Filled.Woman
                            Gender.OTHER -> Icons.Filled.Person
                            Gender.UNSELECTED -> TODO()
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = passenger.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Age
                Text(
                    text = "${passenger.age} yrs",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            // Additional passenger details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Country",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = passenger.country,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Berth Preference",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = passenger.berthPreference.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Edit button
                TextButton(
                    onClick = { onEdit() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                // Delete button
                TextButton(
                    onClick = { onDelete() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerForm(
    passenger: Passenger?,
    onSave: (Passenger) -> Unit,
    onCancel: () -> Unit
) {
    val isEditMode = passenger != null
    
    // Form state
    var name by remember { mutableStateOf(passenger?.name ?: "") }
    var age by remember { mutableStateOf(passenger?.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(passenger?.gender ?: Gender.MALE) }
    var country by remember { mutableStateOf(passenger?.country ?: "India") }
    var berthPreference by remember { mutableStateOf(passenger?.berthPreference ?: BerthPreference.NO_PREFERENCE) }
    
    // Calendar dialog state
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Country dropdown state
    var isCountryMenuExpanded by remember { mutableStateOf(false) }
    
    // Common countries list
    val countries = listOf(
        "India", "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda", "Argentina",
        "Armenia", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh",
        "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bhutan", "Bolivia",
        "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", "Bulgaria", "Burkina Faso", "Burundi",
        "Cabo Verde", "Cambodia", "Cameroon", "Canada", "Central African Republic", "Chad", "Chile",
        "China", "Colombia", "Comoros", "Congo (Brazzaville)", "Congo (Kinshasa)", "Costa Rica",
        "Croatia", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica",
        "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea",
        "Estonia", "Eswatini", "Ethiopia", "Fiji", "Finland", "France", "Gabon", "Gambia", "Georgia",
        "Germany", "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana",
        "Haiti", "Honduras", "Hungary", "Iceland", "Indonesia", "Iran", "Iraq", "Ireland",
        "Israel", "Italy", "Ivory Coast", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya",
        "Kiribati", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia",
        "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Madagascar", "Malawi", "Malaysia",
        "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico",
        "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique",
        "Myanmar", "Namibia", "Nauru", "Nepal", "Netherlands", "New Zealand", "Nicaragua", "Niger",
        "Nigeria", "North Korea", "North Macedonia", "Norway", "Oman", "Pakistan", "Palau", "Panama",
        "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Qatar",
        "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia",
        "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe",
        "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Slovakia",
        "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Korea", "South Sudan",
        "Spain", "Sri Lanka", "Sudan", "Suriname", "Sweden", "Switzerland", "Syria", "Taiwan",
        "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago",
        "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates",
        "United Kingdom", "United States", "Uruguay", "Uzbekistan", "Vanuatu", "Vatican City",
        "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe"
    )


    // Validation state
    var nameError by remember { mutableStateOf("") }
    var ageError by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Form header
        Text(
            text = if (isEditMode) "Edit Passenger" else "Add New Passenger",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                if (it.isNotEmpty()) nameError = ""
            },
            label = { Text("Full Name") },
            placeholder = { Text("Enter passenger's full name") },
            supportingText = { 
                if (nameError.isNotEmpty()) Text(nameError, color = MaterialTheme.colorScheme.error) 
            },
            isError = nameError.isNotEmpty(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        // Age field with calendar picker button
        OutlinedTextField(
            value = age,
            onValueChange = { 
                if (it.isEmpty() || it.toIntOrNull() != null) {
                    age = it
                    if (it.isNotEmpty()) ageError = ""
                }
            },
            label = { Text("Age") },
            placeholder = { Text("Enter age or select date of birth") },
            supportingText = { 
                if (ageError.isNotEmpty()) Text(ageError, color = MaterialTheme.colorScheme.error) 
            },
            isError = ageError.isNotEmpty(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Select Birth Date"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Date picker dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            val today = LocalDate.now()
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dateMillis ->
                            // Calculate age from selected date
                            val birthDate = Instant.ofEpochMilli(dateMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val calculatedAge = Period.between(birthDate, today).years
                            age = calculatedAge.toString()
                        }
                        showDatePicker = false
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        // Gender selection
        Text(
            text = "Gender",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Gender.values().forEach { genderOption ->
                FilterChip(
                    selected = gender == genderOption,
                    onClick = { gender = genderOption },
                    label = { Text(genderOption.toString()) },
                    leadingIcon = {
                        if (gender == genderOption) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Country dropdown
        ExposedDropdownMenuBox(
            expanded = isCountryMenuExpanded,
            onExpandedChange = { isCountryMenuExpanded = it }
        ) {
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("Country") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCountryMenuExpanded) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = isCountryMenuExpanded,
                onDismissRequest = { isCountryMenuExpanded = false }
            ) {
                countries.forEach { countryOption ->
                    DropdownMenuItem(
                        text = { Text(countryOption) },
                        onClick = {
                            country = countryOption
                            isCountryMenuExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Berth preference
        Text(
            text = "Berth Preference",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(1.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BerthPreference.values().forEach { preference ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { berthPreference = preference }
                ) {
                    RadioButton(
                        selected = berthPreference == preference,
                        onClick = { berthPreference = preference }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(preference.toString())
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cancel button
            OutlinedButton(
                onClick = { onCancel() },
                modifier = Modifier
                    .weight(1f)
            ) {
                Text("Cancel")
            }
            
            // Save button
            Button(
                onClick = {
                    // Validate form
                    var isValid = true
                    
                    if (name.isBlank()) {
                        nameError = "Name is required"
                        isValid = false
                    }
                    
                    if (age.isBlank()) {
                        ageError = "Age is required"
                        isValid = false
                    } else if (age.toIntOrNull() == null || age.toInt() <= 0 || age.toInt() > 120) {
                        ageError = "Enter a valid age between 1-120"
                        isValid = false
                    }
                    
                    if (isValid) {
                        onSave(
                            Passenger(
                                id = passenger?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                age = age.toInt(),
                                gender = gender,
                                country = country.trim().ifEmpty { "India" },
                                berthPreference = berthPreference
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}
