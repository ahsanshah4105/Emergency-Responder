package com.example.emergencyresponder.modules.dashboard.data.ml

import android.content.Context
import com.example.emergencyresponder.core.utils.TFLiteModelHelper

class TFLiteCrashMlAnalyzer(
    context: Context
) : CrashMlAnalyzer {

    private val helper = TFLiteModelHelper(context)

    override fun predict(features: FloatArray): Float {
        return helper.predict(features)
    }
}
