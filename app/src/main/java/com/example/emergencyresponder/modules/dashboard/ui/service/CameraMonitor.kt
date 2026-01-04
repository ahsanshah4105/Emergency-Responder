package com.example.emergencyresponder.modules.dashboard.ui.service

import com.example.emergencyresponder.modules.dashboard.data.model.CameraState


import android.content.Context
import androidx.camera.core.ImageAnalysis
import com.example.emergencyresponder.modules.dashboard.data.model.CameraAnalysisResult
import com.example.emergencyresponder.modules.dashboard.domain.useCase.CameraAnalysisUseCase
import java.nio.ByteBuffer
import kotlin.math.abs
//
//class CameraMonitor(
//    private val context: Context,
//    private val onResult: (CameraAnalysisResult) -> Unit
//) : ImageAnalysis.Analyzer {
//
//    private val analysisUseCase = CameraAnalysisUseCase()
//    private var lastFrame: ByteArray? = null
//
//    override fun analyze(image: ImageProxy) {
//        val buffer = image.planes[0].buffer
//        val currentFrame = buffer.toByteArray()
//
//        val frameDiff = lastFrame?.let {
//            calculateFrameDifference(it, currentFrame)
//        } ?: 0.0
//
//        val blurScore = calculateBlur(currentFrame)
//        val brightness = calculateBrightness(currentFrame)
//        val rotation = image.imageInfo.rotationDegrees.toFloat()
//
//        val state = analysisUseCase.analyze(
//            frameDiff,
//            blurScore,
//            rotation,
//            brightness
//        )
//
//        onResult(CameraAnalysisResult(state))
//
//        lastFrame = currentFrame
//        image.close()
//    }
//
//    private fun ByteBuffer.toByteArray(): ByteArray {
//        rewind()
//        return ByteArray(remaining()).also { get(it) }
//    }
//
//    private fun calculateFrameDifference(a: ByteArray, b: ByteArray): Double {
//        var diff = 0L
//        val size = minOf(a.size, b.size)
//        for (i in 0 until size step 10) {
//            diff += abs(a[i] - b[i])
//        }
//        return diff.toDouble() / (size * 255)
//    }
//
//    private fun calculateBlur(data: ByteArray): Double {
//        // simple variance approximation
//        var sum = 0.0
//        for (i in data.indices step 20) sum += data[i]
//        return sum / data.size
//    }
//
//    private fun calculateBrightness(data: ByteArray): Int {
//        var sum = 0
//        for (i in data.indices step 10) sum += data[i].toInt() and 0xFF
//        return sum / (data.size / 10)
//    }
//}

