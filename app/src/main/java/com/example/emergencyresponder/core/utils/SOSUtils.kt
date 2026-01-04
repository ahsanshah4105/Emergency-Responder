package com.example.emergencyresponder.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object SOSUtils {

    fun sendSOSOnWhatsApp(context: Context, phoneNumber: String) {
        val message = "SOS! I need help. Please contact me immediately."
        val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"

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

    fun sendSOSViaSMS(context: Context, phoneNumber: String) {
        val message = "SOS! I need help. Please contact me immediately."

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No messaging app found", Toast.LENGTH_SHORT).show()
        }
    }
}
