package com.amigo.ticketbooker.services.plateformLocator

import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.data.model.Station
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.ui.ServiceTopBar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformLocatorScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current

    var stationList by remember { mutableStateOf(emptyList<Station>()) }

    // Load station list asynchronously
    LaunchedEffect(Unit) {
        stationList = loadStationsFromAssets(context)
    }

    val states = remember(stationList) {
        stationList.mapNotNull { it.state?.takeIf { state -> state.isNotBlank() } }.distinct()
            .sorted()
    }

    var selectedState by remember { mutableStateOf<String?>(null) }
    var selectedDistrict by remember { mutableStateOf<String?>(null) }

    val districts = remember(selectedState) {
        stationList
            .filter { it.state == selectedState }
            .mapNotNull { it.district?.takeIf { district -> district.isNotBlank() } }
            .distinct()
            .sorted()
    }

    val stations = remember(selectedDistrict) {
        stationList
            .filter { it.district == selectedDistrict }
            .sortedBy { it.name }
    }

    Scaffold(
        topBar = {
            ServiceTopBar(
                title = "Station Locator",
                onBackPressed = { navController.navigateUp() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DropdownSelector(
                    label = "Select State",
                    options = states,
                    selectedOption = selectedState,
                    onOptionSelected = {
                        selectedState = it
                        selectedDistrict = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                DropdownSelector(
                    label = "Select District",
                    options = districts,
                    selectedOption = selectedDistrict,
                    onOptionSelected = { selectedDistrict = it },
                    enabled = selectedState != null,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedDistrict != null) {
                StationListCard(stations = stations)
            }
        }
    }
}

@Composable
fun StationListCard(stations: List<Station>) {
    val sortedStations = stations.sortedByDescending { it.trainCount?.toIntOrNull() ?: 0 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(200.dp), // Limit height to show scroll
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Add vertical scrolling
        ) {
            sortedStations.forEach { station -> // Remove `take(5)` to show all stations
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 ${station.name ?: "Unknown"} ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 20.sp
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Function to truncate text if it exceeds a certain length
    fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) text.take(maxLength) + "..." else text
    }

    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            TextField(
                readOnly = true,
                value = selectedOption?.let { truncateText(it, 10) } ?: "",
                onValueChange = {},
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                enabled = enabled
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onOptionSelected(selectionOption)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


fun loadStationsFromAssets(context: Context): List<Station> {
    val jsonString = context.assets.open("stationList.json").bufferedReader().use { it.readText() }
    val gson = Gson()
    val listType = object : TypeToken<List<Map<String, Any?>>>() {}.type
    val rawList: List<Map<String, Any?>> = gson.fromJson(jsonString, listType)

    return rawList.map { rawStation ->
        Station(
            name = rawStation["name"] as String,
            code = rawStation["code"] as String,
            district = rawStation["district"] as String,
            state = rawStation["state"] as String,
            trainCount = rawStation["trainCount"] as String,
            latitude = (rawStation["latitude"] as? String)?.toDoubleOrNull(),
            longitude = (rawStation["longitude"] as? String)?.toDoubleOrNull(),
            address = rawStation["address"] as String
        )
    }
}
