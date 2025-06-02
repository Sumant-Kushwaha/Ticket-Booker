package com.amigo.ticketbooker.auth

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amigo.ticketbooker.home.HomeScreen
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit


class AuthViewModel : ViewModel() {
    // TAG for logging
    private val TAG = "AuthViewModel"
    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _otpState = MutableStateFlow<OtpState>(OtpState.Idle)
    val otpState: StateFlow<OtpState> = _otpState

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // Email and Password Authentication
    fun loginWithEmailPassword(email: String, password: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                when (e) {
                    is FirebaseAuthInvalidUserException -> {
                        _authState.value = AuthState.Error("User does not exist. Please sign up.")
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        _authState.value = AuthState.Error("Invalid email or password.")
                    }
                    else -> {
                        _authState.value = AuthState.Error("Login failed: ${e.message}")
                    }
                }
            }
        }
    }

    fun signUpWithEmailPassword(email: String, password: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                when (e) {
                    is FirebaseAuthWeakPasswordException -> {
                        _authState.value = AuthState.Error("Password is too weak. Please use a stronger password.")
                    }
                    is FirebaseAuthUserCollisionException -> {
                        _authState.value = AuthState.Error("User already exists. Please login.")
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        _authState.value = AuthState.Error("Invalid email format.")
                    }
                    else -> {
                        _authState.value = AuthState.Error("Sign up failed: ${e.message}")
                    }
                }
            }
        }
    }

    // Phone Authentication
    fun sendOtp(phoneNumber: String, activity: Activity) {
        _otpState.value = OtpState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification completed (in some devices)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _otpState.value = OtpState.Error("Verification failed: ${e.message}")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@AuthViewModel.verificationId = verificationId
                this@AuthViewModel.resendToken = token
                _otpState.value = OtpState.CodeSent
            }
        }

        val formattedNumber = if (phoneNumber.startsWith("+")) {
            phoneNumber
        } else {
            "+91$phoneNumber" // Assuming Indian phone numbers
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun resendOtp(phoneNumber: String, activity: Activity) {
        _otpState.value = OtpState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _otpState.value = OtpState.Error("Verification failed: ${e.message}")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@AuthViewModel.verificationId = verificationId
                this@AuthViewModel.resendToken = token
                _otpState.value = OtpState.CodeSent
            }
        }

        val formattedNumber = if (phoneNumber.startsWith("+")) {
            phoneNumber
        } else {
            "+91$phoneNumber" // Assuming Indian phone numbers
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken!!)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    fun verifyOtp(otp: String) {
        _otpState.value = OtpState.Loading

        if (verificationId == null) {
            _otpState.value = OtpState.Error("Verification ID is null. Please request OTP again.")
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                _otpState.value = OtpState.Success
            } catch (e: Exception) {
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        _otpState.value = OtpState.Error("Invalid OTP. Please try again.")
                    }
                    else -> {
                        _otpState.value = OtpState.Error("Verification failed: ${e.message}")
                    }
                }
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
        _otpState.value = OtpState.Idle
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
        _otpState.value = OtpState.Idle
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    companion object {
        // Static context reference for SharedPreferences
        private var appContext: Context? = null
        
        // Shared preferences key constants
        private const val PREF_NAME = "user_prefs"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        
        // Initialize context - call this from MainActivity or Application class
        fun initialize(context: Context) {
            if (appContext == null) {
                appContext = context.applicationContext
            }
        }
        
        // Get shared preferences
        private fun getPrefs(): SharedPreferences? {
            return appContext?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }
    
    /**
     * Get the current user's name from SharedPreferences or Firebase Auth
     * Returns a default name if neither is available
     */
    fun getCurrentUserName(): String {
        val prefs = getPrefs()
        
        // First try to get from SharedPreferences
        val savedName = prefs?.getString(KEY_USER_NAME, null)
        if (!savedName.isNullOrBlank()) {
            Log.d(TAG, "Using name from SharedPreferences: $savedName")
            return savedName
        }
        
        // Then try Firebase Auth sources
        val authName = auth.currentUser?.displayName 
            ?: auth.currentUser?.email?.substringBefore('@')
        
        if (!authName.isNullOrBlank()) {
            Log.d(TAG, "Using name from Firebase Auth: $authName")
            // Save to preferences for future use
            prefs?.edit()?.putString(KEY_USER_NAME, authName)?.apply()
            return authName
        }
        
        // Default fallback
        Log.d(TAG, "Using default name: IRCTC User")
        return "IRCTC User"
    }
    
    /**
     * Get the current user's phone number from SharedPreferences or Firebase Auth
     * Returns a default number if neither is available
     */
    fun getCurrentUserPhone(): Long {
        val prefs = getPrefs()
        
        // First try to get from SharedPreferences
        val savedPhone = prefs?.getLong(KEY_USER_PHONE, 0L) ?: 0L
        if (savedPhone > 0) {
            Log.d(TAG, "Using phone from SharedPreferences: $savedPhone")
            return savedPhone
        }
        
        // Then try to get from Firebase Auth
        val phoneNumber = auth.currentUser?.phoneNumber
        Log.d(TAG, "Firebase phone number: $phoneNumber")
        
        if (!phoneNumber.isNullOrBlank()) {
            try {
                // Remove country code and convert to Long
                val parsedPhone = phoneNumber.replace("+91", "").toLongOrNull() ?: 0L
                if (parsedPhone > 0) {
                    Log.d(TAG, "Using phone from Firebase Auth: $parsedPhone")
                    // Save to preferences for future use
                    prefs?.edit()?.putLong(KEY_USER_PHONE, parsedPhone)?.apply()
                    return parsedPhone
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing phone number: ${e.message}")
                // Log error but continue to fallback
            }
        }
        
        // Default fallback
        Log.d(TAG, "Using default phone: 9876543210")
        return 9876543210L
    }
    
    /**
     * Save user profile information to SharedPreferences
     * Call this method after successful login/signup
     */
    fun saveUserInfo(name: String, phone: Long) {
        val prefs = getPrefs()
        prefs?.edit()
            ?.putString(KEY_USER_NAME, name)
            ?.putLong(KEY_USER_PHONE, phone)
            ?.apply()
        
        Log.d(TAG, "Saved user info - Name: $name, Phone: $phone")
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class OtpState {
    object Idle : OtpState()
    object Loading : OtpState()
    object CodeSent : OtpState()
    object Success : OtpState()
    data class Error(val message: String) : OtpState()
}
