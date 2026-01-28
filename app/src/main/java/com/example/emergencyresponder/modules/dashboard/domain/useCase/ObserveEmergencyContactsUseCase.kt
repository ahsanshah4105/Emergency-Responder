import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.domain.repository.EmergencyContactRepository

class ObserveEmergencyContactsUseCase(
    private val repository: EmergencyContactRepository
) {
    operator fun invoke(
        uid: String,
        onUpdate: (List<EmergencyContact>) -> Unit,
        onError: (String) -> Unit
    ) {
        repository.observeContacts(uid, onUpdate, onError)
    }
}
