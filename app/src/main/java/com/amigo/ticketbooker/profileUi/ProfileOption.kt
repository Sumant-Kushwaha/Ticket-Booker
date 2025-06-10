package com.amigo.ticketbooker.profileUi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.amigo.ticketbooker.R
import com.amigo.ticketbooker.authPage.authViewModel.AuthViewModel
import com.amigo.ticketbooker.navigation.LocalNavController
import com.amigo.ticketbooker.navigation.Routes
import com.amigo.ticketbooker.ui.ConfirmationDialog

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
                    subtitle = "Manage your List of passengers",
                    onClick = {
                        navController.navigate(Routes.MASTER_LIST)
                    }
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
                    subtitle = "App preferences and notifications",
                    onClick = {
                        navController.navigate(Routes.SETTINGS)
                    }
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