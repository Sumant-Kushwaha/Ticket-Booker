package com.amigo.ticketbooker.services.masterListUi.passengerDetailsStorage

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import com.amigo.ticketbooker.services.masterListUi.BerthPreference
import com.amigo.ticketbooker.services.masterListUi.Gender
import com.amigo.ticketbooker.services.masterListUi.Passenger
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Handles saving and loading passenger data to/from external storage
 */
class PassengerFileStorage(private val context: Context) {
    
    private val tag = "PassengerFileStorage"
    
    // Get the directory where we'll store passenger data
    private fun getStorageDirectory(): File {
        val packageName = context.packageName
        Log.d(tag, "Package name: $packageName")
        
        // First try app's external files directory (always accessible without special permissions)
        val appExternalDir = context.getExternalFilesDir(null)
        Log.d(tag, "App external dir: ${appExternalDir?.absolutePath}")
        
        // Then try public directory if we have permissions
        val publicDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Log.d(tag, "Using external storage with MANAGE_EXTERNAL_STORAGE permission")
                try {
                    val dir = File(Environment.getExternalStorageDirectory(), "Android/media/$packageName")
                    Log.d(tag, "Public directory path: ${dir.absolutePath}")
                    dir
                } catch (e: Exception) {
                    Log.e(tag, "Error creating public directory: ${e.message}")
                    null
                }
            } else {
                Log.d(tag, "No MANAGE_EXTERNAL_STORAGE permission")
                null
            }
        } else {
            if (StoragePermissionHandler.hasStoragePermission(context)) {
                Log.d(tag, "Using standard external storage with READ/WRITE permissions")
                try {
                    val dir = File(Environment.getExternalStorageDirectory(), "Android/media/$packageName")
                    Log.d(tag, "Public directory path: ${dir.absolutePath}")
                    dir
                } catch (e: Exception) {
                    Log.e(tag, "Error creating public directory: ${e.message}")
                    null
                }
            } else {
                Log.d(tag, "No READ/WRITE permissions")
                null
            }
        }
        
        // Use public directory if available, otherwise fall back to app-specific directory
        val mediaDir = publicDir ?: appExternalDir ?: context.filesDir
        Log.d(tag, "Selected storage directory: ${mediaDir.absolutePath}")
        
        if (!mediaDir.exists()) {
            try {
                if (mediaDir.mkdirs()) {
                    Log.d(tag, "Created directory: ${mediaDir.absolutePath}")
                } else {
                    Log.e(tag, "Failed to create directory: ${mediaDir.absolutePath}")
                    // If we can't create the directory, fall back to app's files directory
                    if (mediaDir != context.filesDir) {
                        Log.d(tag, "Falling back to app's internal files directory")
                        return context.filesDir
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception creating directory: ${e.message}")
                // Fall back to app's files directory
                if (mediaDir != context.filesDir) {
                    Log.d(tag, "Falling back to app's internal files directory due to exception")
                    return context.filesDir
                }
            }
        }
        
        return mediaDir
    }
    
    // Get the file where we'll store passenger data
    private fun getPassengersFile(): File {
        return File(getStorageDirectory(), PASSENGERS_FILENAME)
    }
    
    /**
     * Save passengers to external storage
     */
    fun savePassengers(passengers: List<Passenger>): Boolean {
        // Always try to save, even without explicit permission, as we'll use app-specific storage as fallback
        try {
            // Get the file and ensure its parent directory exists
            val file = getPassengersFile()
            Log.d(tag, "Attempting to save passengers to: ${file.absolutePath}")
            
            // Ensure parent directory exists
            val parentDir = file.parentFile
            if (parentDir != null && !parentDir.exists()) {
                val dirCreated = parentDir.mkdirs()
                Log.d(tag, "Created parent directory: $dirCreated (${parentDir.absolutePath})")
            }
            
            val jsonArray = JSONArray()
            
            // Convert each passenger to JSON
            passengers.forEach { passenger ->
                val jsonObject = JSONObject().apply {
                    put("id", passenger.id)
                    put("name", passenger.name)
                    put("age", passenger.age)
                    put("gender", passenger.gender.name)
                    put("country", passenger.country)
                    put("berthPreference", passenger.berthPreference?.name)
                }
                jsonArray.put(jsonObject)
            }
            
            // Write JSON to file
            try {
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(jsonArray.toString().toByteArray())
                    outputStream.flush()
                }
                Log.d(tag, "Successfully saved ${passengers.size} passengers to ${file.absolutePath}")
                return true
            } catch (e: IOException) {
                Log.e(tag, "I/O error saving passengers to ${file.absolutePath}: ${e.message}")
                
                // Try fallback to internal storage if external fails
                try {
                    val internalFile = File(context.filesDir, PASSENGERS_FILENAME)
                    Log.d(tag, "Trying fallback to internal storage: ${internalFile.absolutePath}")
                    
                    FileOutputStream(internalFile).use { outputStream ->
                        outputStream.write(jsonArray.toString().toByteArray())
                        outputStream.flush()
                    }
                    Log.d(tag, "Successfully saved ${passengers.size} passengers to internal storage")
                    return true
                } catch (e2: Exception) {
                    Log.e(tag, "Failed to save to internal storage fallback: ${e2.message}")
                    return false
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Unexpected error saving passengers: ${e.message}")
            return false
        }
    }
    
    /**
     * Load passengers from external storage
     */
    fun loadPassengers(): List<Passenger> {
        // Try primary file location first
        val primaryFile = getPassengersFile()
        Log.d(tag, "Attempting to load passengers from: ${primaryFile.absolutePath}")
        
        // Check if primary file exists
        if (primaryFile.exists() && primaryFile.canRead()) {
            try {
                // Read JSON from primary file
                val jsonString = FileInputStream(primaryFile).bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val passengers = mutableListOf<Passenger>()
                
                // Convert JSON to passengers
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val passenger = Passenger(
                        id = jsonObject.getString("id"),
                        name = jsonObject.getString("name"),
                        age = jsonObject.getInt("age"),
                        gender = Gender.valueOf(jsonObject.getString("gender")),
                        country = jsonObject.getString("country"),
                        berthPreference = BerthPreference.valueOf(jsonObject.getString("berthPreference"))
                    )
                    passengers.add(passenger)
                }
                
                Log.d(tag, "Successfully loaded ${passengers.size} passengers from ${primaryFile.absolutePath}")
                return passengers
            } catch (e: Exception) {
                Log.e(tag, "Error loading from primary file: ${e.message}")
                // Continue to fallback options
            }
        } else {
            Log.d(tag, "Primary file doesn't exist or can't be read: ${primaryFile.absolutePath}")
        }
        
        // Try fallback to internal storage
        val internalFile = File(context.filesDir, PASSENGERS_FILENAME)
        if (internalFile.exists() && internalFile.canRead()) {
            Log.d(tag, "Trying fallback to internal storage: ${internalFile.absolutePath}")
            try {
                // Read JSON from internal file
                val jsonString = FileInputStream(internalFile).bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val passengers = mutableListOf<Passenger>()
                
                // Convert JSON to passengers
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val passenger = Passenger(
                        id = jsonObject.getString("id"),
                        name = jsonObject.getString("name"),
                        age = jsonObject.getInt("age"),
                        gender = Gender.valueOf(jsonObject.getString("gender")),
                        country = jsonObject.getString("country"),
                        berthPreference = BerthPreference.valueOf(jsonObject.getString("berthPreference"))
                    )
                    passengers.add(passenger)
                }
                
                Log.d(tag, "Successfully loaded ${passengers.size} passengers from internal storage")
                return passengers
            } catch (e: Exception) {
                Log.e(tag, "Error loading from internal storage: ${e.message}")
                // Continue to next fallback
            }
        }
        
        // If we get here, create an empty file and return empty list
        Log.d(tag, "No valid passenger file found, creating empty file")
        savePassengers(emptyList())
        return emptyList()
    }
    
    /**
     * Check if the passengers file exists
     */
    fun passengerFileExists(): Boolean {
        // Check primary file location
        val primaryFile = getPassengersFile()
        if (primaryFile.exists() && primaryFile.canRead()) {
            Log.d(tag, "Passenger file exists at primary location: ${primaryFile.absolutePath}")
            return true
        }
        
        // Check internal fallback location
        val internalFile = File(context.filesDir, PASSENGERS_FILENAME)
        val exists = internalFile.exists() && internalFile.canRead()
        Log.d(tag, "Passenger file ${if (exists) "exists" else "does not exist"} at internal location: ${internalFile.absolutePath}")
        return exists
    }
    
    /**
     * Create an empty passengers file if it doesn't exist
     */
    fun createEmptyFileIfNeeded(): Boolean {
        // Try to create file at primary location
        val primaryFile = getPassengersFile()
        Log.d(tag, "Checking if need to create empty file at: ${primaryFile.absolutePath}")
        
        if (!primaryFile.exists()) {
            try {
                // Ensure directory exists
                val directory = primaryFile.parentFile
                if (directory != null && !directory.exists()) {
                    val dirCreated = directory.mkdirs()
                    Log.d(tag, "Created directory: $dirCreated (${directory.absolutePath})")
                }
                
                // Create the file
                val fileCreated = primaryFile.createNewFile()
                if (fileCreated) {
                    try {
                        FileOutputStream(primaryFile).use { outputStream ->
                            outputStream.write("[]".toByteArray())
                            outputStream.flush()
                        }
                        Log.d(tag, "Successfully created empty passengers file at ${primaryFile.absolutePath}")
                        return true
                    } catch (e: IOException) {
                        Log.e(tag, "Error writing to new file: ${e.message}")
                        // Continue to fallback
                    }
                } else {
                    Log.e(tag, "Failed to create new file at ${primaryFile.absolutePath}")
                    // Continue to fallback
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception creating file at primary location: ${e.message}")
                // Continue to fallback
            }
        } else {
            // File already exists
            Log.d(tag, "File already exists at primary location")
            return true
        }
        
        // Try fallback to internal storage
        try {
            val internalFile = File(context.filesDir, PASSENGERS_FILENAME)
            Log.d(tag, "Trying to create empty file at internal location: ${internalFile.absolutePath}")
            
            if (!internalFile.exists()) {
                val fileCreated = internalFile.createNewFile()
                if (fileCreated) {
                    FileOutputStream(internalFile).use { outputStream ->
                        outputStream.write("[]".toByteArray())
                        outputStream.flush()
                    }
                    Log.d(tag, "Successfully created empty passengers file at internal location")
                    return true
                } else {
                    Log.e(tag, "Failed to create file at internal location")
                    return false
                }
            } else {
                Log.d(tag, "File already exists at internal location")
                return true
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception creating file at internal location: ${e.message}")
            return false
        }
    }
    
    companion object {
        private const val PASSENGERS_FILENAME = "passengers.json"
        
        /**
         * Get all possible file locations where passenger data might be stored
         */
        fun getAllPossibleFileLocations(context: Context): List<String> {
            val locations = mutableListOf<String>()
            
            // App-specific external storage
            context.getExternalFilesDir(null)?.let { dir ->
                locations.add(File(dir, PASSENGERS_FILENAME).absolutePath)
            }
            
            // Internal storage
            locations.add(File(context.filesDir, PASSENGERS_FILENAME).absolutePath)
            
            // Public storage locations
            val packageName = context.packageName
            locations.add(File(Environment.getExternalStorageDirectory(), 
                "Android/media/$packageName/$PASSENGERS_FILENAME").absolutePath)
            
            return locations
        }
    }
}
