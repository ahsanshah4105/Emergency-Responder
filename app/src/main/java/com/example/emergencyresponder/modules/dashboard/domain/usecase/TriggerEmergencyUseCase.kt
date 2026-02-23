package com.example.emergencyresponder.modules.dashboard.domain.usecase


import com.example.emergencyresponder.modules.dashboard.domain.notifier.EmergencyTrigger

class TriggerEmergencyUseCase(
    private val emergencyTrigger: EmergencyTrigger
) {
    fun execute() {
        emergencyTrigger.trigger()
    }
}
