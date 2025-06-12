package com.amigo.ticketbooker.services.foodBookingUi.orderFoodScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.R
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.navigation.Routes
import com.amigo.ticketbooker.services.foodBookingUi.orderFoodScreen.components.FoodOrderHeader
import com.amigo.ticketbooker.services.foodBookingUi.orderFoodScreen.components.FoodServiceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderFoodScreen() {
    val navController = LocalNavController.current

    // State for tracking which option is selected
    var selectedOption by remember { mutableStateOf<FoodServiceOption?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Order Food",
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
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background gradient for the top portion
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header section
                FoodOrderHeader()

                Spacer(modifier = Modifier.height(24.dp))

                // Food service options
                Text(
                    text = "Select a Food Service",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Zomato Option
                FoodServiceCard(
                    title = "Zomato",
                    description = "Order from a wide variety of \nrestaurants near your station",
                    logoResId = R.drawable.ic_zomato,
                    backgroundColor = Color(0xFFF5F5F5),
                    accentColor = Color(0xFFE23744),
                    isSelected = selectedOption == FoodServiceOption.ZOMATO,
                    onClick = { selectedOption = FoodServiceOption.ZOMATO }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // IRCTC Food Catering Option
                FoodServiceCard(
                    title = "IRCTC Food Catering",
                    description = "Order official railway catering \ndelivered to your seat",
                    logoResId = R.drawable.ic_irctc,
                    backgroundColor = Color(0xFFF5F5F5),
                    accentColor = Color(0xFF0056A8),
                    isSelected = selectedOption == FoodServiceOption.IRCTC,
                    onClick = { selectedOption = FoodServiceOption.IRCTC }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Continue button
                Button(
                    onClick = {
                        // Navigate to the appropriate food ordering flow
                        when (selectedOption) {
                            FoodServiceOption.ZOMATO -> {
                                // Navigate to Zomato ordering flow
                                // For now, just show a toast or message
                            }
                            FoodServiceOption.IRCTC -> {
                                // Navigate to IRCTC catering screen
                                navController.navigate(Routes.IRCTC_CATERING)
                            }
                            null -> {
                                // No option selected, show a message or do nothing
                            }
                        }
                    },
                    enabled = selectedOption != null,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

enum class FoodServiceOption {
    ZOMATO, IRCTC
}



