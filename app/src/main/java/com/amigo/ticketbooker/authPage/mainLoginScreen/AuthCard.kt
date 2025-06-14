package com.amigo.ticketbooker.authPage.mainLoginScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amigo.ticketbooker.authPage.authViewModel.AuthState
import com.amigo.ticketbooker.authPage.authViewModel.AuthViewModel
import com.amigo.ticketbooker.authPage.mainLoginScreen.loginPage.LoginMainScreen
import com.amigo.ticketbooker.authPage.mainLoginScreen.signUpPage.SignupMainScreen
import androidx.compose.material3.MaterialTheme

@Composable
fun AuthCard(
    authViewModel: AuthViewModel,
) {

    var isLogin by remember { mutableStateOf(true) }

    // Observe auth state
    val authState by authViewModel.authState.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Tab Selector
            TabSelector(
                isLogin = isLogin,
                onTabSelected = { selectedTab ->
                    isLogin = selectedTab
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLogin) {
                LoginMainScreen(
                    authViewModel = authViewModel,
                    isLoading = authState is AuthState.Loading
                )
            } else {
                SignupMainScreen(
                    authViewModel = authViewModel
                )
            }
        }
    }
}