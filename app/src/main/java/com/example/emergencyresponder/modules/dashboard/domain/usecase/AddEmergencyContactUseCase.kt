import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.domain.repository.EmergencyContactRepository

class AddEmergencyContactUseCase(
    private val repository: EmergencyContactRepository
) {
    operator fun invoke(
        uid: String,
        contact: EmergencyContact,
        onResult: (Boolean) -> Unit
    ) {
        repository.addContact(uid, contact, onResult)
    }
}
