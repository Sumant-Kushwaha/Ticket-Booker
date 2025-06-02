package com.amigo.ticketbooker.services.storage

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Handles storage permission requests for Android devices
 */
object StoragePermissionHandler {
    
    private const val TAG = "StoragePermissionHandler"
    
    /**
     * Checks if the app has storage permissions
     */
    fun hasStoragePermission(context: Context): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+ (API 30+), check if we have manage external storage permission
            val hasManageStorage = Environment.isExternalStorageManager()
            Log.d(TAG, "Android 11+: MANAGE_EXTERNAL_STORAGE permission granted: $hasManageStorage")
            hasManageStorage
        } else {
            // For Android 10 and below, check for read/write permissions
            val readPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            
            val writePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            
            val hasPermissions = readPermission && writePermission
            Log.d(TAG, "Android 10 and below: READ_EXTERNAL_STORAGE: $readPermission, WRITE_EXTERNAL_STORAGE: $writePermission")
            hasPermissions
        }
        
        return result
    }
    
    /**
     * Request storage permissions based on Android version
     */
    fun requestStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+ (API 30+), request manage external storage permission
            try {
                Log.d(TAG, "Requesting MANAGE_EXTERNAL_STORAGE permission for Android 11+")
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${activity.packageName}")
                activity.startActivityForResult(intent, STORAGE_PERMISSION_CODE)
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting permission: ${e.message}")
                // Fallback if the above intent doesn't work
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    activity.startActivityForResult(intent, STORAGE_PERMISSION_CODE)
                } catch (e2: Exception) {
                    Log.e(TAG, "Error with fallback permission request: ${e2.message}")
                    // Last resort - open app details settings
                    val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity.packageName, null)
                    settingsIntent.data = uri
                    activity.startActivity(settingsIntent)
                }
            }
        } else {
            // For Android 10 and below, request read/write permissions
            Log.d(TAG, "Requesting READ/WRITE permissions for Android 10 and below")
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
        }
    }
    
    /**
     * Composable to handle storage permission request and show dialogs
     */
    @Composable
    fun RequestStoragePermission(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        var showRationale by remember { mutableStateOf(false) }
        var showAndroid11Instructions by remember { mutableStateOf(false) }
        
        // Check if we already have permission
        LaunchedEffect(Unit) {
            if (hasStoragePermission(context)) {
                Log.d(TAG, "Storage permission already granted")
                onPermissionGranted()
            } else {
                Log.d(TAG, "Need to request storage permission")
                showRationale = true
            }
        }
        
        // Permission request launcher for Android 10 and below
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                Log.d(TAG, "All permissions granted via launcher")
                onPermissionGranted()
            } else {
                Log.d(TAG, "Some permissions denied: $permissions")
                onPermissionDenied()
            }
        }
        
        // Show rationale dialog
        if (showRationale) {
            AlertDialog(
                onDismissRequest = { 
                    showRationale = false
                    onPermissionDenied()
                },
                title = { Text("Storage Permission Required") },
                text = { Text("This app needs access to your storage to save and load passenger data. Without this permission, your passenger list won't be saved between app sessions.") },
                confirmButton = {
                    Button(onClick = {
                        showRationale = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            // For Android 11+, show additional instructions
                            showAndroid11Instructions = true
                        } else {
                            // For Android 10 and below, use the permission launcher
                            requestPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            )
                        }
                    }) {
                        Text("Grant Permission")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showRationale = false
                        onPermissionDenied()
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Show Android 11+ specific instructions
        if (showAndroid11Instructions) {
            AlertDialog(
                onDismissRequest = { 
                    showAndroid11Instructions = false
                    onPermissionDenied()
                },
                title = { Text("Enable 'All files access'" ) },
                text = { 
                    Text(
                        "For Android 11+ devices, you need to enable 'Allow access to manage all files' in the next screen.\n\n" +
                        "1. Tap 'Use this folder' in the next screen\n" +
                        "2. Find and enable the 'Allow access to manage all files' toggle\n" +
                        "3. Return to the app after enabling the permission"
                    ) 
                },
                confirmButton = {
                    Button(onClick = {
                        showAndroid11Instructions = false
                        // Launch the system settings
                        val activity = context as Activity
                        requestStoragePermission(activity)
                    }) {
                        Text("Continue")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showAndroid11Instructions = false
                        onPermissionDenied()
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // For Android 11+, we need to check if permission was granted after returning from settings
        DisposableEffect(Unit) {
            onDispose {
                if (hasStoragePermission(context)) {
                    Log.d(TAG, "Permission granted after returning from settings")
                    onPermissionGranted()
                }
            }
        }
    }
    
    private const val STORAGE_PERMISSION_CODE = 100
}
