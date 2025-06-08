package com.amigo.ticketbooker.home

import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.navigation.Routes
import com.amigo.ticketbooker.auth.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.amigo.ticketbooker.home.cards.ElevatedColorCard
import com.amigo.ticketbooker.home.cards.ModernGradientCard
import com.amigo.ticketbooker.home.cards.OutlinedIconCard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.amigo.ticketbooker.R
import com.amigo.ticketbooker.fontFamily
import com.amigo.ticketbooker.home.cards.ElevatedColorCard
import com.amigo.ticketbooker.home.cards.ModernGradientCard
import com.amigo.ticketbooker.home.cards.OutlinedIconCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val navController = LocalNavController.current
    val authViewModel: AuthViewModel = viewModel()
    // Use TicketBookerTheme to match authentication screens
    Scaffold(
        topBar = { 
            HomeAppBar(
                onProfileClick = { 
                    navController.navigate(Routes.PROFILE) 
                },
                onSignOut = { 
                    authViewModel.signOut()
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        },
        bottomBar = { 
            BottomSection(navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MainContent()
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    onProfileClick: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Create a gradient background for the app bar to make it more expressive
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Quick Tatkal",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_train),
                    contentDescription = "Train Icon",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(28.dp)
                )
            },
            actions = {
                IconButton(onClick = { /* Notifications */ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Account",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Profile dropdown menu
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("My Profile", color = Color.Black) },
                        onClick = {
                            showMenu = false
                            onProfileClick()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.Black
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sign Out", color = Color.Black) },
                        onClick = {
                            showMenu = false
                            onSignOut()
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Sign Out",
                                tint = Color.Black
                            )
                        }
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }
}

@Composable
fun MainContent() {
    val navController = LocalNavController.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Featured banner
        FeaturedBanner(navController)

        Spacer(modifier = Modifier.height(24.dp))

        // Main options grid
        Text(
            text = "Quick Services",
            fontFamily = fontFamily,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ServiceCard(
                title = "Automatic Booking",
                icon = R.drawable.ic_automatic_booking,
                modifier = Modifier.weight(1f),
                index = 0, // Modern gradient design
                onClick = { navController.navigate(Routes.AUTOMATIC_BOOKING) }
            )
            ServiceCard(
                title = "Manual Booking",
                icon = R.drawable.ic_manual_booking,
                modifier = Modifier.weight(1f),
                index = 1, // Outlined design
                onClick = { navController.navigate(Routes.MANUAL_BOOKING) }
            )
            ServiceCard(
                title = "Order Food",
                icon = R.drawable.ic_food,
                modifier = Modifier.weight(1f),
                index = 2, // Elevated color design
                onClick = { navController.navigate(Routes.ORDER_FOOD) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ServiceCard(
                title = "Running Status",
                icon = R.drawable.ic_running_status,
                modifier = Modifier.weight(1f),
                index = 2, // Elevated color design
                onClick = { navController.navigate(Routes.RUNNING_STATUS) }
            )
            ServiceCard(
                title = "PNR Status",
                icon = R.drawable.ic_pnr_status,
                modifier = Modifier.weight(1f),
                index = 0, // Modern gradient design
                onClick = { navController.navigate(Routes.PNR_STATUS) }
            )
            ServiceCard(
                title = "Coach Position",
                icon = R.drawable.ic_coach_position,
                modifier = Modifier.weight(1f),
                index = 1, // Outlined design
                onClick = { navController.navigate(Routes.COACH_POSITION) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Third row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ServiceCard(
                title = "Platform Locator",
                icon = R.drawable.ic_platform_locator,
                modifier = Modifier.weight(1f),
                index = 1, // Outlined design
                onClick = { navController.navigate(Routes.PLATFORM_LOCATOR) }
            )
            ServiceCard(
                title = "Train on Map",
                icon = R.drawable.ic_train_map,
                modifier = Modifier.weight(1f),
                index = 2, // Elevated color design
                onClick = { navController.navigate(Routes.TRAIN_MAP) }
            )
            ServiceCard(
                title = "Master List",
                icon = R.drawable.ic_master_list,
                modifier = Modifier.weight(1f),
                index = 0, // Modern gradient design
                onClick = { navController.navigate(Routes.MASTER_LIST) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent bookings
        RecentBookingsSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Offers section
        OffersSection()

        Spacer(modifier = Modifier.height(70.dp)) // Space for bottom bar
    }
}

@Composable
fun FeaturedBanner(
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Modern gradient with the app's primary color
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.primaryContainer
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(
                                Float.POSITIVE_INFINITY,
                                Float.POSITIVE_INFINITY
                            )
                        )
                    )
            )

            // Decorative elements for expressive design
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = 120.dp, y = (-50).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = (-20).dp, y = 100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )

            // Content with modern typography
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Book Tatkal Tickets",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Get confirmed seats instantly",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { navController.navigate(Routes.AUTOMATIC_BOOKING) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        "Book Now",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceCard(
    title: String,
    icon: Int,
    modifier: Modifier = Modifier,
    index: Int = 0, // Use index to create different design variations
    onClick: () -> Unit = {}
) {
    // Different design variations based on index
    val designType = index % 3

    when (designType) {
        0 -> ModernGradientCard(title, icon, modifier, onClick)
        1 -> OutlinedIconCard(title, icon, modifier, onClick)
        2 -> ElevatedColorCard(title, icon, modifier, onClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentBookingsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Bookings",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            TextButton(onClick = { /* TODO */ }) {
                Text(
                    text = "View All",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sample recent booking
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO */ },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Rajdhani Express",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "PNR: 8574123698",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Delhi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "05:30",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Mumbai",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "22:15",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "30 May 2025",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    AssistChip(
                        onClick = { /* TODO */ },
                        label = {
                            Text(
                                text = "Confirmed",
                                fontSize = 12.sp
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            labelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun OffersSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Special Offers",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable { /* TODO */ },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_offer),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "10% OFF on First Booking",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "Use code: FIRSTRIDE",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomSection(navController : NavController) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // My Account section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AccountOption(
                    title = "Free Token",
                    icon = R.drawable.ic_ticket,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(Routes.FREE_TOKEN)
                    }
                )

                AccountOption(
                    title = "Profile",
                    icon = R.drawable.ic_profile,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(Routes.PROFILE)
                    }
                )

                AccountOption(
                    title = "Help & Support",
                    icon = R.drawable.ic_help,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate(Routes.HELP_SUPPORT)
                    }
                )
            }
        }
    }
}

@Composable
fun AccountOption(
    title: String,
    icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(34.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SocialIcon(icon: Int) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            .clickable { /* TODO */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}
