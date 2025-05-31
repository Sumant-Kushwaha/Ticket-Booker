package com.amigo.ticketbooker.profile

import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.navigation.Routes
import com.amigo.ticketbooker.auth.AuthViewModel
import com.amigo.ticketbooker.ui.ConfirmationDialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.R

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ProfileScreen() {
    val navController = LocalNavController.current
    val authViewModel: AuthViewModel = viewModel()
    val name = authViewModel.getCurrentUserName() ?: "User"
    val phone = authViewModel.getCurrentUserPhone() ?: 0L
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            ProfileTopBar(onBackPressed = { navController.navigateUp() })
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
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile header with animation
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                    exit = fadeOut()
                ) {
                    ProfileHeaderCard(name = name, phone = phone)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Profile options with animation
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                    exit = fadeOut()
                ) {
                    ProfileOptions()
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(onBackPressed: () -> Unit) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "My Profile",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { /* TODO: Add settings action */ },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun ProfileHeaderCard(name: String, phone: Long) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Premium badge at top right corner
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(50)),
                shape = RoundedCornerShape(50),
                color = Color(0xFFFFC107).copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stylized crown using stacked elements
                    Box(contentAlignment = Alignment.Center) {
                        // Base of crown
                        Icon(
                            painter = painterResource(id = R.drawable.ic_offer),  // Using offer icon which might have points like a crown
                            contentDescription = "Premium",
                            tint = Color(0xFFFFD700),  // Gold color
                            modifier = Modifier.size(18.dp)
                        )
                        
                        // Small dot on top to simulate crown jewel
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .offset(y = (-2).dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD700))
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = "Premium Member",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),  // Gold color
                        letterSpacing = 0.5.sp
                    )
                }
            }
            
            // Main content with horizontal layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Profile header with image on left, info on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile image with border
                    Box(
                        modifier = Modifier
                            .padding(top = 40.dp)
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(width = 4.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(65.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // User info column
                    Column(
                        modifier = Modifier.padding(top = 32.dp)
                    ) {
                        // User name with larger font
                        Text(
                            text = name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Phone number with smaller font
                        if (phone > 0) {
                            Text(
                                text = "+91 $phone",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stats row with enhanced design
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EnhancedStatItem(count = "12", label = "Bookings", iconRes = R.drawable.ic_train)
                    Spacer(modifier = Modifier.width(8.dp))
                    EnhancedStatItem(count = "4", label = "Upcoming", iconRes = R.drawable.ic_setting)
                    Spacer(modifier = Modifier.width(8.dp))
                    EnhancedStatItem(count = "8", label = "Completed", iconRes = R.drawable.ic_person)
                }
            }
        }
    }
}

@Composable
fun EnhancedStatItem(count: String, label: String, iconRes: Int) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ProfileOptions() {
    val navController = LocalNavController.current
    val authViewModel: AuthViewModel = viewModel()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Section Title
        Text(
            text = "Account Options",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
        )
        
        // Account Options Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Premium Section
                EnhancedProfileOptionItem(
                    iconRes = R.drawable.ic_train,
                    title = "Get Premium Membership",
                    subtitle = "Remove ads and get unlimited token",
                    badgeText = "POPULAR",
                    badgeColor = MaterialTheme.colorScheme.primary
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                // Free Token Section
                EnhancedProfileOptionItem(
                    iconRes = R.drawable.ic_person,
                    title = "Free Token",
                    subtitle = "Watch ads to get free tokens",
                    onClick = {
                        navController.navigate(Routes.FREE_TOKEN)
                    }
                )
            }
        }
        
        // Section Title
        Text(
            text = "Bookings & Passengers",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 16.dp)
        )
        
        // Bookings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Master List Section
                EnhancedProfileOptionItem(
                    iconRes = R.drawable.ic_lock,
                    title = "Master List",
                    subtitle = "Manage your List of passengers"
                )
            }
        }
        
        // Section Title
        Text(
            text = "Support & Settings",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 16.dp)
        )
        
        // Support Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Help & Support Section
                EnhancedProfileOptionItem(
                    iconRes = R.drawable.ic_phone,
                    title = "Help & Support",
                    subtitle = "Contact us for assistance",
                    onClick = {
                        navController.navigate(Routes.HELP_SUPPORT)
                    }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                // Referral Section
                EnhancedProfileOptionItem(
                    iconRes = R.drawable.ic_sms,
                    title = "Referral",
                    subtitle = "Refer and earn free token",
                    badgeText = "NEW",
                    badgeColor = Color(0xFF4CAF50)
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                // Settings Section
                EnhancedProfileOptionItem(
                    iconRes = R.drawable.ic_setting,
                    title = "Settings",
                    subtitle = "App preferences and notifications"
                )
            }
        }
        
        // Logout Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 8.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            )
        ) {
            // Logout Section
            var showLogoutDialog by remember { mutableStateOf(false) }
            
            EnhancedProfileOptionItem(
                iconRes = R.drawable.ic_logout,
                title = "Logout",
                subtitle = "Logout or Change Account",
                textColor = MaterialTheme.colorScheme.error,
                onClick = {
                    showLogoutDialog = true
                }
            )
            
            // Logout confirmation dialog
            if (showLogoutDialog) {
                ConfirmationDialog(
                    title = "Confirm Logout",
                    message = "Are you sure you want to logout from your account?",
                    confirmText = "Logout",
                    dismissText = "Cancel",
                    confirmColor = MaterialTheme.colorScheme.error,
                    onConfirm = {
                        showLogoutDialog = false
                        authViewModel.signOut()
                        navController.navigate(Routes.AUTH) {
                            // Clear the back stack completely
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = false
                                inclusive = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = false
                        }
                    },
                    onDismiss = {
                        showLogoutDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun EnhancedProfileOptionItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with background
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    modifier = Modifier.weight(1f, fill = false)
                )
                
                // Optional badge
                if (badgeText != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "Go to $title",
            tint = textColor.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

// Old ProfileOptionItem has been replaced with EnhancedProfileOptionItem
