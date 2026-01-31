package com.example.emergencyresponder.core.utils

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices



object SOSUtils  {
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val locationLink: String
        get() = "https://maps.google.com/?q=$latitude,$longitude"

    private val message: String
        get() = "🚨 SOS! I need help. Please contact me immediately.\nMy location: $locationLink"


    fun sendSOSViaSMS(context: Context, phoneNumber: String) {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "SMS permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        getCurrentLocation(context) { lat, lng ->

            latitude = lat
            longitude = lng

            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)

                Toast.makeText(context, "🚨 SOS SMS Sent!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendSOSOnWhatsApp(context: Context, phoneNumber: String) {

        getCurrentLocation(context) { lat, lng ->

            latitude = lat
            longitude = lng

            val cleanNumber = phoneNumber.replace("+", "")

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


//    fun sendSOSViaSMS(context: Context, phoneNumber: String) {
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.SEND_SMS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            Toast.makeText(context, "SMS permission not granted", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        try {
//            val sentPI = PendingIntent.getBroadcast(
//                context, 0, Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE
//            )
//            val deliveredPI = PendingIntent.getBroadcast(
//                context, 0, Intent("SMS_DELIVERED"), PendingIntent.FLAG_IMMUTABLE
//            )
//
//            // Register BroadcastReceivers to check status
//            ContextCompat.registerReceiver(context, object : BroadcastReceiver() {
//                override fun onReceive(ctx: Context?, intent: Intent?) {
//                    when (resultCode) {
//                        Activity.RESULT_OK -> Toast.makeText(
//                            context,
//                            "SMS sent",
//                            Toast.LENGTH_SHORT
//                        ).show()
//
//                        else -> Toast.makeText(context, "SMS failed to send", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                    context.unregisterReceiver(this)
//                }
//            }, IntentFilter("SMS_SENT"), ContextCompat.RECEIVER_NOT_EXPORTED)
//
//            ContextCompat.registerReceiver(context, object : BroadcastReceiver() {
//                override fun onReceive(ctx: Context?, intent: Intent?) {
//                    when (resultCode) {
//                        Activity.RESULT_OK -> Toast.makeText(
//                            context,
//                            "SMS delivered",
//                            Toast.LENGTH_SHORT
//                        ).show()
//
//                        else -> Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                    context.unregisterReceiver(this)
//                }
//            } as BroadcastReceiver?, IntentFilter("SMS_DELIVERED"), ContextCompat.RECEIVER_NOT_EXPORTED)
//
//            val smsManager = SmsManager.getDefault()
//            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI)
//
//        } catch (e: Exception) {
//            Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show()
//            e.printStackTrace()
//        }
//    }


    private fun getCurrentLocation(context: Context, callback: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000L
        ).setMinUpdateIntervalMillis(1000L)
            .build()

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    callback(location.latitude, location.longitude)
                    fusedLocationClient.removeLocationUpdates(this) // stop after getting one
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
}
