import android.os.CountDownTimer

object CrashCountdownManager {

    private var timer: CountDownTimer? = null
    var remainingSeconds: Long = 60
        private set

    private val tickListeners = mutableListOf<(Long) -> Unit>()
    private val finishListeners = mutableListOf<() -> Unit>()

    // Keep a reference to the "final action" that sends the alert
    private var onFinishAction: (() -> Unit)? = null
    private var isCancelled = false

    fun startCountdown(onTick: (Long) -> Unit, onFinish: () -> Unit, finalAction: (() -> Unit)? = null) {
        timer?.cancel()
        remainingSeconds = 60
        isCancelled = false
        tickListeners.clear()
        finishListeners.clear()
        tickListeners.add(onTick)
        finishListeners.add(onFinish)
        onFinishAction = finalAction

        timer = object : CountDownTimer(60_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = millisUntilFinished / 1000
                tickListeners.forEach { it.invoke(remainingSeconds) }
            }

            override fun onFinish() {
                remainingSeconds = 0
                finishListeners.forEach { it.invoke() }

                if (!isCancelled) {
                    onFinishAction?.invoke() // trigger alert only if not cancelled
                }
            }
        }.start()
    }

    fun addListener(onTick: (Long) -> Unit, onFinish: () -> Unit) {
        tickListeners.add(onTick)
        finishListeners.add(onFinish)
    }

    fun cancel() {
        timer?.cancel()
        isCancelled = true
        remainingSeconds = 0
        tickListeners.clear()
        finishListeners.clear()
        onFinishAction = null
    }
}
