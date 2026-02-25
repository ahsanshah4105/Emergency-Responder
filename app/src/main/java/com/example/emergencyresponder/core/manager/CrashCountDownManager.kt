import android.os.CountDownTimer
import com.example.emergencyresponder.modules.timestamp.domain.repository.ICountdownManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object CrashCountdownManager : ICountdownManager {
    override val totalTimeSec = 30
    private var timer: CountDownTimer? = null

    private val _remainingSeconds = MutableStateFlow(totalTimeSec.toLong())
    override val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()
    private val _timerFlow = MutableSharedFlow<Long>(replay = 1)
    val timerFlow = _timerFlow.asSharedFlow()

    private val tickListeners = mutableListOf<(Long) -> Unit>()
    private val finishListeners = mutableListOf<() -> Unit>()

    // The actual SOS action (SMS/API trigger)
    private var sosAction: (() -> Unit)? = null

    fun addListener(onTick: (Long) -> Unit, onFinish: () -> Unit) {
        tickListeners.add(onTick)
        finishListeners.add(onFinish)
    }
    override fun startCountdown(onSosAction: (() -> Unit)?) {
        cancel()
        sosAction = onSosAction

        timer = object : CountDownTimer((totalTimeSec * 1000).toLong(), 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingSeconds.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                _remainingSeconds.value = 0
                sosAction?.invoke()
            }
        }.start()
    }

    override fun cancel() {
        timer?.cancel()
        timer = null
        sosAction = null
        _remainingSeconds.value = totalTimeSec.toLong()
    }

}