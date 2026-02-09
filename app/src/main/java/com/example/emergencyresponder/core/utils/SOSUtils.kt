package com.example.emergencyresponder.core.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object SOSUtils {

    // --- Configuration ---
    // Note: In production, consider moving these to local.properties or BuildConfig
    private const val INSTANCE_ID = "7103508163"
    private const val API_TOKEN = "83614a390cd24a5388353b6b2057a91d0c61da30e8bc4d6092"

    // --- Helper: Generate Message with Dynamic Location ---
    private fun getMessage(lat: Double, lng: Double): String {
        // Creates a clickable Google Maps link with the exact coordinates
        val mapsLink = "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
        return "🚨 *SOS! I need help.* \nPlease contact me immediately.\n\n📍 My Location:\n$mapsLink"
    }

    // =========================================================================
    // 1. SMS (Background Sending to Multiple Users)
    // =========================================================================
    fun sendSOSViaSMS(context: Context, phoneNumbers: List<String>) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "SMS permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        getCurrentLocation(context) { lat, lng ->
            try {
                val smsManager = SmsManager.getDefault()
                val messageText = getMessage(lat, lng)
                val parts = smsManager.divideMessage(messageText)

                var successCount = 0
                var failCount = 0

                // Loop through the list of phone numbers
                for (number in phoneNumbers) {
                    try {
                        if (number.isNotEmpty()) {
                            smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                            successCount++
                        }
                    } catch (e: Exception) {
                        failCount++
                        e.printStackTrace()
                    }
                }

                // Show summary toast
                if (successCount > 0) {
                    Toast.makeText(context, "🚨 SMS Sent to $successCount contacts!", Toast.LENGTH_SHORT).show()
                }
                if (failCount > 0) {
                    Toast.makeText(context, "⚠️ SMS Failed for $failCount contacts", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Failed to initialize SMS Manager", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // =========================================================================
    // 2. WhatsApp Intent (Opens App for User to Select Contacts)
    // =========================================================================
    fun sendSOSOnWhatsApp(context: Context) {
        getCurrentLocation(context) { lat, lng ->

            val messageText = getMessage(lat, lng)

            try {
                // Use ACTION_SEND to share text to multiple people via WhatsApp UI
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage("com.whatsapp") // Restricts to WhatsApp only
                    putExtra(Intent.EXTRA_TEXT, messageText)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // =========================================================================
    // 3. Green API (Background Sending to Multiple Users)
    // =========================================================================
    fun sendSOSViaGreenApi(context: Context, targetPhoneNumbers: List<String>) {
        // 1. Get location ONCE
        getCurrentLocation(context) { lat, lng ->

            val messageText = getMessage(lat, lng)

            Toast.makeText(context, "Sending WhatsApp SOS to ${targetPhoneNumbers.size} contacts...", Toast.LENGTH_SHORT).show()

            Thread {
                val client = OkHttpClient() // Create client once
                var successCount = 0
                var failCount = 0

                // 2. Loop through each number
                for (rawNumber in targetPhoneNumbers) {
                    try {
                        // Format number: remove + and spaces
                        val cleanNumber = rawNumber.replace("+", "").replace(" ", "")

                        if (cleanNumber.isNotEmpty()) {
                            val chatId = "$cleanNumber@c.us"

                            val jsonBody = JSONObject()
                            jsonBody.put("chatId", chatId)
                            jsonBody.put("message", messageText)

                            val mediaType = "application/json; charset=utf-8".toMediaType()
                            val requestBody = jsonBody.toString().toRequestBody(mediaType)

                            val url = "https://api.green-api.com/waInstance$INSTANCE_ID/sendMessage/$API_TOKEN"

                            val request = Request.Builder()
                                .url(url)
                                .post(requestBody)
                                .build()

                            val response = client.newCall(request).execute()

                            if (response.isSuccessful) {
                                successCount++
                            } else {
                                failCount++
                                Log.e("GreenAPI", "Failed for $cleanNumber: ${response.code}")
                            }
                            response.close() // Always close response
                        }
                    } catch (e: Exception) {
                        failCount++
                        e.printStackTrace()
                    }
                }

                // 3. Show a Summary Toast on UI Thread
                Handler(Looper.getMainLooper()).post {
                    if (successCount > 0) {
                        Toast.makeText(context, "✅ WhatsApp Sent to $successCount contacts", Toast.LENGTH_LONG).show()
                    }
                    if (failCount > 0) {
                        Toast.makeText(context, "⚠️ WhatsApp Failed for $failCount contacts", Toast.LENGTH_SHORT).show()
                    }
                }

            }.start()
        }
    }

    // =========================================================================
    // Helper: Get Current Location
    // =========================================================================
    private fun getCurrentLocation(context: Context, callback: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission missing", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                callback(location.latitude, location.longitude)
            } else {
                // If last location is null, request a fresh update
                val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 1000
                ).build()

                val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                        result.lastLocation?.let {
                            callback(it.latitude, it.longitude)
                            fusedLocationClient.removeLocationUpdates(this)
                        }
                    }
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            }
        }
    }
}