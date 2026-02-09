import android.os.CountDownTimer

object CrashCountdownManager {

    private var timer: CountDownTimer? = null

    // Read-only property for UI to check current state
    var remainingSeconds: Long = 30
        private set

    // Support multiple listeners (e.g., Service voice + Activity UI)
    private val tickListeners = mutableListOf<(Long) -> Unit>()
    private val finishListeners = mutableListOf<() -> Unit>()

    // The actual SOS action (SMS/API trigger)
    private var sosAction: (() -> Unit)? = null

    private const val TOTAL_TIME_MS = 30_000L
    private const val INTERVAL_MS = 1_000L

    fun startCountdown(
        onTick: (Long) -> Unit,
        onFinish: () -> Unit,
        onSosAction: (() -> Unit)? = null
    ) {
        // 1. Stop any existing timer first
        cancel()

        // 2. Reset state
        remainingSeconds = TOTAL_TIME_MS / 1000

        // 3. Add initial listeners (from Service)
        tickListeners.add(onTick)
        finishListeners.add(onFinish)
        sosAction = onSosAction

        // 4. Start Timer
        timer = object : CountDownTimer(TOTAL_TIME_MS, INTERVAL_MS) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = millisUntilFinished / 1000
                // Notify all active listeners
                tickListeners.forEach { it.invoke(remainingSeconds) }
            }

            override fun onFinish() {
                remainingSeconds = 0
                // Notify listeners that time is up
                finishListeners.forEach { it.invoke() }

                // Trigger the SOS (Only if not cancelled)
                sosAction?.invoke()
            }
        }.start()
    }

    // Called by Activity to sync UI with the running timer
    fun addListener(onTick: (Long) -> Unit, onFinish: () -> Unit) {
        tickListeners.add(onTick)
        finishListeners.add(onFinish)
    }

    fun cancel() {
        // 1. Stop the native timer
        timer?.cancel()
        timer = null

        // 2. Nullify the SOS action so it CANNOT fire
        sosAction = null

        // 3. Clear listeners to stop UI updates
        tickListeners.clear()
        finishListeners.clear()

        remainingSeconds = 0
    }
}