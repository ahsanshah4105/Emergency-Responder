package com.example.emergencyresponder.modules.dashboard.data.model


data class CameraState(
    val suddenMotion: Boolean,
    val heavyBlur: Boolean,
    val orientationFlip: Boolean,
    val darkness: Boolean
)
