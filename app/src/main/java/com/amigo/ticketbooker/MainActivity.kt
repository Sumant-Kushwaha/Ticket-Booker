package com.amigo.ticketbooker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.amigo.ticketbooker.authPage.authViewModel.AuthViewModel
import com.amigo.ticketbooker.services.automaticBooking.anotherWeb.IrctcWebViewScreen
import com.amigo.ticketbooker.ui.theme.TicketBookerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize AuthViewModel with application context
        AuthViewModel.initialize(applicationContext)

        // Fix system bars to ensure they're properly displayed
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Ensure the status bar is visible and properly themed
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }

        setContent {
            TicketBookerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Single composable call that handles everything
//                    AppRoot(onExitApp = { finish() })
                    Column(Modifier.fillMaxSize()) {
                        Spacer(Modifier.height(50.dp))
//                        FlowRun()
                        IrctcWebViewScreen(p1Name="Abhishek",p1Seat="L")
                    }
                }
            }
        }
    }
}
