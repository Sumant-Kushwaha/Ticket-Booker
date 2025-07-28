package com.amigo.ticketbooker.services.plateformLocator

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amigo.ticketbooker.data.model.Station
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.ui.ServiceTopBar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformLocatorScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()

    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Check and request location permissions
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Fetch user location if permission is granted
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            coroutineScope.launch {
                fetchUserLocation(fusedLocationClient) { location ->
                    userLocation = location
                }
            }
        }
    }

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
            Text(
                text = if (locationPermissionGranted) {
                    userLocation?.let { "Your Location: ${it.first}, ${it.second}" } ?: "Fetching location..."
                } else {
                    "Location permission not granted"
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

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
                StationListCard(stations = stations, userLocation = userLocation)
            }
        }
    }
}

@Composable
fun StationListCard(stations: List<Station>, userLocation: Pair<Double, Double>?) {
    val context = LocalContext.current
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
            sortedStations.forEach { station ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable {
                            // Check if station has coordinates
                            if (station.latitude != null && station.longitude != null) {
                                try {
                                    // Try Google Maps app first
                                    val uri =
                                        "google.navigation:q=${station.latitude},${station.longitude}&mode=d".toUri()
                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }

                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        // Fallback to web Google Maps
                                        val fallbackUri =
                                            "https://www.google.com/maps/dir/?api=1&destination=${station.latitude},${station.longitude}".toUri()
                                        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
                                        context.startActivity(fallbackIntent)
                                    }
                                } catch (e: Exception) {
                                    // Last fallback - use address if available
                                    val destination = station.address?.takeIf { it.isNotBlank() }
                                        ?: "${station.name}, ${station.district}, ${station.state}"

                                    val searchUri = "geo:0,0?q=${Uri.encode(destination)}".toUri()
                                    val searchIntent = Intent(Intent.ACTION_VIEW, searchUri)
                                    try {
                                        context.startActivity(searchIntent)
                                    } catch (ex: Exception) {
                                        android.widget.Toast.makeText(context, "Unable to open navigation", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                // Use address-based navigation if coordinates are not available
                                val destination = station.address?.takeIf { it.isNotBlank() }
                                    ?: "${station.name}, ${station.district}, ${station.state}"

                                try {
                                    val searchUri = "geo:0,0?q=${Uri.encode(destination)}".toUri()
                                    val searchIntent = Intent(Intent.ACTION_VIEW, searchUri)
                                    context.startActivity(searchIntent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Unable to open navigation", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
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

@SuppressLint("MissingPermission")
private fun fetchUserLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationFetched: (Pair<Double, Double>) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        location?.let {
            onLocationFetched(Pair(it.latitude, it.longitude))
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
            name = rawStation["name"] as? String,
            code = rawStation["code"] as? String,
            district = rawStation["district"] as? String,
            state = rawStation["state"] as? String,
            trainCount = rawStation["trainCount"] as? String,
            latitude = when (val lat = rawStation["latitude"]) {
                is Number -> lat.toDouble()
                is String -> lat.toDoubleOrNull()
                else -> null
            },
            longitude = when (val lng = rawStation["longitude"]) {
                is Number -> lng.toDouble()
                is String -> lng.toDoubleOrNull()
                else -> null
            },
            address = rawStation["address"] as? String
        )
    }
}
