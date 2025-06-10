package com.amigo.ticketbooker.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.amigo.ticketbooker.authPage.mainLoginScreen.AuthScreen
import com.amigo.ticketbooker.authPage.authViewModel.AuthViewModel
import com.amigo.ticketbooker.authPage.mainLoginScreen.loginWithOtpPage.OtpLoginScreen
import com.amigo.ticketbooker.help.HelpAndSupportScreen
import com.amigo.ticketbooker.home.HomeScreen
import com.amigo.ticketbooker.profileUi.ProfileScreen
import com.amigo.ticketbooker.settingUi.SettingsScreen
import com.amigo.ticketbooker.services.automaticBooking.AutomaticBookingScreen
import com.amigo.ticketbooker.services.coachPositionUi.CoachPositionScreen
import com.amigo.ticketbooker.services.manualBookingUi.ManualBookingScreen
import com.amigo.ticketbooker.services.foodBookingUi.IRCTCCateringScreen
import com.amigo.ticketbooker.services.masterListUi.MasterListScreen
import com.amigo.ticketbooker.services.foodBookingUi.OrderFoodScreen
import com.amigo.ticketbooker.services.plateformLocator.PlatformLocatorScreen
import com.amigo.ticketbooker.services.pnrStatusCheckerUi.PnrStatusScreen
import com.amigo.ticketbooker.services.runningStatusUi.RunningStatusScreen
import com.amigo.ticketbooker.services.trainOnMap.TrainMapScreen
import com.amigo.ticketbooker.token.FreeTokenScreen

// Define navigation routes as constants for easier access
object Routes {
    const val AUTH = "auth"
    const val OTP_LOGIN = "otp_login"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val BOOKING_HISTORY = "booking_history"
    const val SETTINGS = "settings"
    const val HELP_SUPPORT = "help_support"
    const val FREE_TOKEN = "free_token"
    const val COMMON = "order_food"

    // Service card routes
    const val AUTOMATIC_BOOKING = "automatic_booking"
    const val MANUAL_BOOKING = "manual_booking"
    const val ORDER_FOOD = "order_food"
    const val IRCTC_CATERING = "irctc_catering"
    const val RUNNING_STATUS = "running_status"
    const val PNR_STATUS = "pnr_status"
    const val COACH_POSITION = "coach_position"
    const val PLATFORM_LOCATOR = "platform_locator"
    const val TRAIN_MAP = "train_map"
    const val MASTER_LIST = "master_list"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    startDestination: String = Routes.AUTH,
    navController: NavHostController
) {
    // Check if user is already logged in
    LaunchedEffect(key1 = Unit) {
        if (authViewModel.isUserLoggedIn()) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.AUTH) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.AUTH) {
            AuthScreen()
        }

        composable(Routes.OTP_LOGIN) {
            OtpLoginScreen()
        }

        composable(Routes.HOME) {
            HomeScreen()
        }

        composable(Routes.PROFILE) {
            ProfileScreen()
        }

        composable(Routes.HELP_SUPPORT) {
            HelpAndSupportScreen()
        }

        composable(Routes.FREE_TOKEN) {
            FreeTokenScreen()
        }

        // Service screen routes
        composable(Routes.AUTOMATIC_BOOKING) {
            AutomaticBookingScreen()
        }

        composable(Routes.MANUAL_BOOKING) {
            ManualBookingScreen()
        }

        composable(Routes.ORDER_FOOD) {
            OrderFoodScreen()
        }
        
        composable(Routes.IRCTC_CATERING) {
            IRCTCCateringScreen()
        }

        composable(Routes.RUNNING_STATUS) {
            RunningStatusScreen()
        }

        composable(Routes.PNR_STATUS) {
            PnrStatusScreen()
        }

        composable(Routes.COACH_POSITION) {
            CoachPositionScreen()
        }

        composable(Routes.PLATFORM_LOCATOR) {
            PlatformLocatorScreen()
        }

        composable(Routes.TRAIN_MAP) {
            TrainMapScreen()
        }

        composable(Routes.MASTER_LIST) {
            MasterListScreen()
        }
        
        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
    }
}

