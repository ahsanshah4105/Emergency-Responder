package com.example.emergencyresponder.core.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import android.os.Handler
import com.google.android.gms.location.Priority
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


object SOSUtils {

    private const val INSTANCE_ID = "7103508163" // e.g., 1101823842
    private const val API_TOKEN = "83614a390cd24a5388353b6b2057a91d0c61da30e8bc4d6092"     // e.g., 897d8f...

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val locationLink: String
        get() = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    private val message: String
        get() = "🚨 *SOS! I need help.* \nPlease contact me immediately.\n\n📍 My Location:\n$locationLink"

    fun sendSOSViaSMS(context: Context, phoneNumber: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "SMS permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        getCurrentLocation(context) { lat, lng ->
            latitude = lat
            longitude = lng

            try {
                val smsManager = SmsManager.getDefault()
                // Message ko parts mein divide krta hai agr lamba ho
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)

                Toast.makeText(context, "🚨 SMS Sent via Network!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    fun sendSOSOnWhatsApp(context: Context, phoneNumber: String) {
        getCurrentLocation(context) { lat, lng ->
            latitude = lat
            longitude = lng
            val cleanNumber = phoneNumber.replace("+", "").replace(" ", "")
            val url = "https://wa.me/$cleanNumber?text=${Uri.encode(message)}"

            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendSOSViaGreenApi(context: Context, targetPhoneNumber: String) {
        getCurrentLocation(context) { lat, lng ->
            latitude = lat
            longitude = lng

            val cleanNumber = targetPhoneNumber.replace("+", "").replace(" ", "")
            val chatId = "$cleanNumber@c.us" // Green API requirement

            Thread {
                try {
                    val client = OkHttpClient()

                    val jsonBody = JSONObject()
                    jsonBody.put("chatId", chatId)
                    jsonBody.put("message", message)

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = jsonBody.toString().toRequestBody(mediaType)

                    val url = "https://api.green-api.com/waInstance$INSTANCE_ID/sendMessage/$API_TOKEN"

                    val request = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()

                    //val responseBody = response.body?.string()
                    Handler(Looper.getMainLooper()).post {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "✅ WhatsApp Sent Automatically!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "❌ API Error: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "❌ Internet Connection Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }

    // --- Helper: Get Location ---
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
                // Agar last location null ho, toh new request bhejo
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
