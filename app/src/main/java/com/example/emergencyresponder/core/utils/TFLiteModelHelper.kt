package com.example.emergencyresponder.core.utils


import android.content.Context
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder


class TFLiteModelHelper(context: Context) {

    private val interpreter: Interpreter
    private val mean: FloatArray
    private val scale: FloatArray

    init {
        // Load model
        val model = context.assets.open("model_fall_detection.tflite")
        val modelBytes = model.readBytes()
        val bb = ByteBuffer.allocateDirect(modelBytes.size)
        bb.order(ByteOrder.nativeOrder())
        bb.put(modelBytes)
        interpreter = Interpreter(bb)

        // Load scaler
        val scalerJson = context.assets.open("scaler_fall.json").bufferedReader().use { it.readText() }
        val scaler = JSONObject(scalerJson)

        mean = scaler.getJSONArray("mean").let { arr ->
            FloatArray(arr.length()) { i -> arr.getDouble(i).toFloat() }
        }

        scale = scaler.getJSONArray("scale").let { arr ->
            FloatArray(arr.length()) { i -> arr.getDouble(i).toFloat() }
        }
    }

    fun predict(input: FloatArray): Float {
        val scaledInput = FloatArray(input.size)
        for (i in input.indices) {
            scaledInput[i] = (input[i] - mean[i]) / scale[i]
        }

        val inputBuffer = ByteBuffer.allocateDirect(4 * scaledInput.size)
        inputBuffer.order(ByteOrder.nativeOrder())
        scaledInput.forEach { inputBuffer.putFloat(it) }
        inputBuffer.rewind()

        val outputBuffer = ByteBuffer.allocateDirect(4)
        outputBuffer.order(ByteOrder.nativeOrder())
        outputBuffer.rewind()

        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()

        val confidence = outputBuffer.float
        return confidence
    }
}
