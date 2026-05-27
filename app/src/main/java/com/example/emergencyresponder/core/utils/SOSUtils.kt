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
import com.example.emergencyresponder.core.common.PrefKeys
import com.example.emergencyresponder.core.domain.coroutines.DispatcherProvider
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object SOSUtils {

    // --- Configuration ---
    private const val INSTANCE_ID = "7103514169"
    private const val API_TOKEN = "73f2ce51c6184439be5e9e7a413400709128d9f2e0b94f7b93"
    lateinit var prefProvider: IBasePreference
    lateinit var dispatchers: DispatcherProvider
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
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
            Toast.makeText(context, com.example.emergencyresponder.R.string.mic_permission_required, Toast.LENGTH_SHORT).show()
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

            Toast.makeText(context, com.example.emergencyresponder.R.string.sos_recording_in_progress, Toast.LENGTH_LONG).show()

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

            // Use IO dispatcher for network work
            CoroutineScope(dispatchers.io).launch {
                var successCount = 0

                for (rawNumber in targetPhoneNumbers) {
                    try {
                        val cleanNumber = rawNumber.replace("+", "").replace(" ", "")
                        if (cleanNumber.isEmpty()) continue

                        val chatId = "$cleanNumber@c.us"
                        val request: Request

                        if (audioFile != null && audioFile.length() > 0) {
                            val requestBody = MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("chatId", chatId)
                                .addFormDataPart(
                                    "file",
                                    audioFile.name,
                                    audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull())
                                )
                                .build()

                            request = Request.Builder()
                                .url("https://api.green-api.com/waInstance$INSTANCE_ID/sendFileByUpload/$API_TOKEN")
                                .post(requestBody)
                                .build()
                        } else {
                            val jsonBody = JSONObject().apply {
                                put("chatId", chatId)
                                put("message", messageText)
                            }
                            val body = jsonBody.toString().toRequestBody("application/json".toMediaType())
                            request = Request.Builder()
                                .url("https://api.green-api.com/waInstance$INSTANCE_ID/sendMessage/$API_TOKEN")
                                .post(body)
                                .build()
                        }

                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) successCount++
                            Log.d("SOSUtils", "Response: ${response.code}")
                        }
                    } catch (e: Exception) {
                        Log.e("SOSUtils", "GreenAPI Error: ${e.message}")
                    }
                }

                withContext(dispatchers.main) {
                    if (successCount > 0) {
                        Toast.makeText(
                            context,
                            context.getString(com.example.emergencyresponder.R.string.whatsapp_sos_sent, successCount),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
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
                Toast.makeText(context, com.example.emergencyresponder.R.string.whatsapp_not_installed, Toast.LENGTH_SHORT).show()
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
        val name = prefProvider.getString(PrefKeys.USER_NAME)
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