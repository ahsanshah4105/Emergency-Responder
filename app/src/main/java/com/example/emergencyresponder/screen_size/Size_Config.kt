package com.example.emergencyresponder.screen_size
import android.content.Context
import android.util.DisplayMetrics

object SizeConfig {
    var screenWidth: Float = 0f
    var screenHeight: Float = 0f
    var scale: Float = 0f

    fun init(context: Context) {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        screenWidth = metrics.widthPixels.toFloat()
        screenHeight = metrics.heightPixels.toFloat()
        scale = metrics.density
    }
}

val Number.h: Float
    get() = (this.toFloat() / 853f) * SizeConfig.screenHeight

val Number.w: Float
    get() = (this.toFloat() / 393f) * SizeConfig.screenWidth

val Number.sp: Float
    get() = (this.toFloat() / 393f) * SizeConfig.screenWidth

val Number.r: Float
    get() = (this.toFloat() / 393f) * SizeConfig.screenWidth

fun scaleImageSize(pxSize: Float, context: Context): Float {
    val density = context.resources.displayMetrics.density
    return pxSize / density
}
