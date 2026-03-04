package com.example.emergencyresponder.modules.dashboard.data.provider

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.channels.FileChannel

class YAMNetClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val labels = mutableMapOf<Int, String>()

    init {
        loadModel()
        loadLabels()
    }

    private fun loadModel() {
        val model = context.assets.openFd("yamnet.tflite").run {
            createInputStream().channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
        interpreter = Interpreter(model).apply { allocateTensors() }
    }

    private fun loadLabels() {
        context.assets.open("yamnet_class_map.csv").bufferedReader().useLines { lines ->
            lines.drop(1).forEachIndexed { index, line ->
                val parts = line.split(",")
                if (parts.size > 2) labels[index] = parts[2].trim()
            }
        }
    }

    fun classify(audioData: FloatArray): FloatArray {
        val output = Array(1) { FloatArray(521) }
        interpreter?.run(arrayOf(audioData), output)
        return output[0]
    }

    fun getIndexFor(label: String): Int = labels.entries.find { it.value.equals(label, true) }?.key ?: -1
}