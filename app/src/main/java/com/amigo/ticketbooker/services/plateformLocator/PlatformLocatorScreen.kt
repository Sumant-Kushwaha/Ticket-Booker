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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.amigo.ticketbooker.data.model.Station
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.ui.ServiceTopBar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlin.math.*

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

    // Calculate nearby stations
    val nearbyStations = remember(userLocation, stationList) {
        if (userLocation != null) {
            stationList.filter { station ->
                station.latitude != null && station.longitude != null
            }.map { station ->
                val distance = calculateDistance(
                    userLocation!!.first, userLocation!!.second,
                    station.latitude!!, station.longitude!!
                )
                station to distance
            }.filter { it.second <= 50 } // Within 50 km
                .sortedBy { it.second }
                .take(20)
                .map { it.first }
        } else {
            emptyList()
        }
    }

    // Bottom sheet state with only collapsed and fully expanded
    var bottomSheetState by remember { mutableStateOf(BottomSheetState.COLLAPSED) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current

    val collapsedHeight = with(density) { 90.dp.toPx() }
    val fullyExpandedHeight = with(density) { (screenHeight * 0.5f).toPx() }

    val targetHeight = when (bottomSheetState) {
        BottomSheetState.COLLAPSED -> collapsedHeight
        BottomSheetState.FULLY_EXPANDED -> fullyExpandedHeight
    }

    val animatedHeight by animateFloatAsState(
        targetValue = targetHeight,
        label = "bottomSheetHeight"
    )

    Box(modifier = Modifier.fillMaxSize()) {
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
                    .padding(bottom = with(density) { collapsedHeight.toDp() + 16.dp }) // Leave space for bottom sheet
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

        // Bottom Sheet with gradient background
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { animatedHeight.toDp() })
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .pointerInput(bottomSheetState) {
                    detectDragGestures(
                        onDragEnd = {
                            // Snap to nearest state after drag
                            val currentHeight = animatedHeight
                            val midPoint = (collapsedHeight + fullyExpandedHeight) / 2
                            bottomSheetState = if (currentHeight <= midPoint) {
                                BottomSheetState.COLLAPSED
                            } else {
                                BottomSheetState.FULLY_EXPANDED
                            }
                        }
                    ) { _, dragAmount ->
                        // Respond to swipe up/down
                        val dragThreshold = 20f
                        if (dragAmount.y > dragThreshold) { // Drag down
                            bottomSheetState = BottomSheetState.COLLAPSED
                        } else if (dragAmount.y < -dragThreshold) { // Drag up
                            bottomSheetState = BottomSheetState.FULLY_EXPANDED
                        }
                    }
                },
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Handle bar - clickable to toggle states
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(6.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                bottomSheetState = when (bottomSheetState) {
                                    BottomSheetState.COLLAPSED -> BottomSheetState.FULLY_EXPANDED
                                    BottomSheetState.FULLY_EXPANDED -> BottomSheetState.COLLAPSED
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Header
                    Text(
                        text = "Nearby Stations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content - only show when expanded
                    if (bottomSheetState == BottomSheetState.FULLY_EXPANDED) {
                        LazyColumn(nearbyStations, userLocation)
                    }
                }
            }
        }
    }
}

// Enum for bottom sheet states - removed HALF_EXPANDED
enum class BottomSheetState {
    COLLAPSED,
    FULLY_EXPANDED
}

@Composable
private fun LazyColumn(nearbyStations: List<Station>, userLocation: Pair<Double, Double>?) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (nearbyStations.isEmpty()) {
            Text(
                text = if (userLocation != null) "No nearby stations found" else "Location not available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            nearbyStations.forEach { station ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            // Navigate to station
                            if (station.latitude != null && station.longitude != null) {
                                try {
                                    val uri = "google.navigation:q=${station.latitude},${station.longitude}&mode=d".toUri()
                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        val fallbackUri = "https://www.google.com/maps/dir/?api=1&destination=${station.latitude},${station.longitude}".toUri()
                                        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
                                        context.startActivity(fallbackIntent)
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Unable to open navigation", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = station.name ?: "Unknown Station",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${station.district}, ${station.state}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (userLocation != null && station.latitude != null && station.longitude != null) {
                            val distance = calculateDistance(
                                userLocation.first, userLocation.second,
                                station.latitude, station.longitude
                            )
                            Text(
                                text = "${String.format("%.1f", distance)} km away",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.LocationOn,
                        contentDescription = "Navigate",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

// Helper function to calculate distance between two coordinates
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0 // Earth's radius in kilometers

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationListCard(stations: List<Station>, userLocation: Pair<Double, Double>?) {
    val context = LocalContext.current
    val sortedStations = stations.sortedByDescending { it.trainCount?.toIntOrNull() ?: 0 }
    val stationCount = sortedStations.size

    // Card height logic
    val cardHeight = when {
        stationCount <= 1 -> Dp.Unspecified // Wrap content
        stationCount > 5 -> 5 * 44.dp + 32.dp // 5 items + padding
        else -> (stationCount * 44).dp + 32.dp // n items + padding
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .let { if (cardHeight != Dp.Unspecified) it.height(cardHeight) else it },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .let {
                    if (stationCount > 5) it.verticalScroll(rememberScrollState())
                    else it
                }
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
