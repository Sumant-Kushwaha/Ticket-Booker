package com.amigo.ticketbooker.profileUi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amigo.ticketbooker.R

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
                    EnhancedStatItem(count = "12", label = "Bookings")
                    Spacer(modifier = Modifier.width(8.dp))
                    EnhancedStatItem(count = "4", label = "Upcoming")
                    Spacer(modifier = Modifier.width(8.dp))
                    EnhancedStatItem(count = "8", label = "Completed")
                }
            }
        }
    }
}