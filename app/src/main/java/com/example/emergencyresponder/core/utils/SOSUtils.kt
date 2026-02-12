package com.example.emergencyresponder.core.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.emergencyresponder.core.objects.SPreferenceManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

object SOSUtils {

    // --- Configuration ---
    private const val INSTANCE_ID = "7103508163"
    private const val API_TOKEN = "83614a390cd24a5388353b6b2057a91d0c61da30e8bc4d6092"

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startEmergencySequence(context: Context, phoneNumbers: List<String>) {

        // 1️⃣ SEND SMS IMMEDIATELY (SIM)
        sendSOSViaSMS(context, phoneNumbers)

        // 2️⃣ SEND WHATSAPP TEXT IMMEDIATELY (Green API)
        sendSOSViaGreenApi(context, phoneNumbers, null)

        // 3️⃣ START AUDIO RECORDING IN BACKGROUND
        startAudioRecording(context) { recordedFile ->

            if (recordedFile != null && recordedFile.exists() && recordedFile.length() > 0) {

                // 4️⃣ Upload voice independently
                sendSOSViaGreenApi(context, phoneNumbers, recordedFile)
            }
        }
    }



    private fun startAudioRecording(context: Context, onRecordingFinished: (File?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Microphone permission missing!", Toast.LENGTH_SHORT).show()
            onRecordingFinished(null)
            return
        }

        try {
            // Use internal cache to ensure write access
            val fileName = "sos_audio_${System.currentTimeMillis()}.m4a"
            audioFile = File(context.cacheDir, fileName)

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

// 🔥 WhatsApp Compatible Settings
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(16000)
                setAudioChannels(1)

                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()

            }

            Toast.makeText(context, "🎙️ Recording 15s Audio...", Toast.LENGTH_LONG).show()

            // Stop recording after 15 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                stopRecording() // ✅ CRITICAL: Stop and release BEFORE callback

                // Small delay to ensure file is saved
                Handler(Looper.getMainLooper()).postDelayed({
                    onRecordingFinished(audioFile)
                }, 500)

            }, 15000)

        } catch (e: Exception) {
            Log.e("SOSUtils", "Recording failed: ${e.message}")
            stopRecording()
            onRecordingFinished(null)
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
        }
    }

    private fun sendSOSViaGreenApi(context: Context, targetPhoneNumbers: List<String>, audioFile: File?) {
        getCurrentLocation(context) { lat, lng ->
            val messageText = getMessage(lat, lng)
            val feedback = if (audioFile != null) "Uploading Audio SOS..." else "Sending Text SOS..."
            Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()

            Thread {
                val client = OkHttpClient()
                var successCount = 0

                for (rawNumber in targetPhoneNumbers) {
                    try {
                        val cleanNumber = rawNumber.replace("+", "").replace(" ", "")
                        if (cleanNumber.isNotEmpty()) {
                            val chatId = "$cleanNumber@c.us"
                            val request: Request

                            // --- OPTION A: SEND AUDIO FILE WITH CAPTION ---
                            if (audioFile != null && audioFile.exists() && audioFile.length() > 0) {

                                val requestBody = MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("chatId", chatId)
                                    .addFormDataPart(
                                        "file",
                                        audioFile.name,
                                        audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull())

                                    )
                                    .build()

                                val url = "https://api.green-api.com/waInstance$INSTANCE_ID/sendFileByUpload/$API_TOKEN"
                                request = Request.Builder().url(url).post(requestBody).build()


                                // --- OPTION B: SEND TEXT ONLY (Fallback) ---
                            } else {
                                val jsonBody = JSONObject()
                                jsonBody.put("chatId", chatId)
                                jsonBody.put("message", messageText)
                                val mediaType = "application/json; charset=utf-8".toMediaType()
                                val requestBody = jsonBody.toString().toRequestBody(mediaType)

                                val url = "https://api.green-api.com/waInstance$INSTANCE_ID/sendMessage/$API_TOKEN"
                                request = Request.Builder().url(url).post(requestBody).build()
                            }

                            val response = client.newCall(request).execute()
                            if (response.isSuccessful) successCount++
                            response.close()
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }

                Handler(Looper.getMainLooper()).post {
                    if (successCount > 0) Toast.makeText(context, "✅ WhatsApp SOS Sent ($successCount)", Toast.LENGTH_LONG).show()
                }
            }.start()
        }
    }


    fun sendSOSViaSMS(context: Context, phoneNumbers: List<String>) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return

        getCurrentLocation(context) { lat, lng ->
            try {
                val smsManager = SmsManager.getDefault()
                val messageText = getMessage(lat, lng)
                val parts = smsManager.divideMessage(messageText)

                for (number in phoneNumbers) {
                    if (number.isNotEmpty()) smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun sendSOSOnWhatsApp(context: Context) {
        getCurrentLocation(context) { lat, lng ->
            val messageText = getMessage(lat, lng)
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage("com.whatsapp")
                    putExtra(Intent.EXTRA_TEXT, messageText)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendSOSToSpecificPerson(context: Context, phone: String) {
        getCurrentLocation(context) { lat, lng ->
            val messageText = getMessage(lat, lng)

            try {
                // WhatsApp expects phone number in international format without '+' and spaces, e.g., "923001234567"
                val cleanedPhone = phone.replace("[^\\d]".toRegex(), "")
                val url = "https://wa.me/$cleanedPhone?text=${Uri.encode(messageText)}"

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    setPackage("com.whatsapp") // Ensure it opens WhatsApp directly
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)

            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp not installed or number invalid", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getMessage(lat: Double, lng: Double): String {
        var name = SPreferenceManager.getUserName()
        return "🚨 Hi I am $name.* \nPlease contact me immediately\n\n📍 My Location:\nhttps://www.google.com/maps/search/?api=1&query=$lat,$lng"
    }

    private fun getCurrentLocation(context: Context, callback: (Double, Double) -> Unit) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        client.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) callback(loc.latitude, loc.longitude)
            else {
                val req = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
                client.requestLocationUpdates(req, object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(r: com.google.android.gms.location.LocationResult) {
                        r.lastLocation?.let { callback(it.latitude, it.longitude); client.removeLocationUpdates(this) }
                    }
                }, Looper.getMainLooper())
            }
        }
    }
}