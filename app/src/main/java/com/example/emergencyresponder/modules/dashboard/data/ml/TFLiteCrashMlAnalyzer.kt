package com.example.emergencyresponder.modules.dashboard.data.ml

import android.content.Context
import com.example.emergencyresponder.core.utils.TFLiteModelHelper
import com.example.emergencyresponder.modules.dashboard.domain.engine.CrashPredictor

class TFLiteCrashMlAnalyzer(
    context: Context
) : CrashPredictor {

    private val helper = TFLiteModelHelper(context)

    override fun predict(features: FloatArray): Float {
        return helper.predict(features)
    }
}
