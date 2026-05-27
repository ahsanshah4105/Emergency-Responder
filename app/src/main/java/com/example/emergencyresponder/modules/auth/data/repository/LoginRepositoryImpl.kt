import com.example.emergencyresponder.core.network.AuthException
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.model.User as DataUser
import com.example.emergencyresponder.modules.auth.domain.model.AuthenticatedUser
import com.example.emergencyresponder.modules.auth.domain.repository.LoginRepository
import com.example.emergencyresponder.modules.auth.domain.repository.UserPreferences
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LoginRepositoryImpl(
    private val authDataSource: AuthRemoteDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val prefs: UserPreferences
) : LoginRepository {

    override suspend fun login(email: String, password: String): AuthenticatedUser {
        try {
            val result = authDataSource.loginUser(email, password)
            val firebaseUser = result.user ?: throw AuthException.UserNotFoundException()

            if (!firebaseUser.isEmailVerified) {
                authDataSource.logout()
                throw AuthException.EmailNotVerifiedException()
            }

            val userData = userRemoteDataSource.getUser(firebaseUser.uid)

            prefs.saveUserSession(userData.uid, userData.name, userData.email)
            prefs.setUserLoggedIn(true)

            return AuthenticatedUser(
                uid = userData.uid,
                name = userData.name,
                email = userData.email
            )
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw AuthException.InvalidCredentialsException()
        } catch (e: Exception) {
            throw e as? AuthException ?: AuthException.NetworkException()
        }
    }

    override suspend fun loginWithGoogle(idToken: String): AuthenticatedUser {
        try {
            val result = authDataSource.loginWithGoogle(idToken)
            val firebaseUser = result.user ?: throw AuthException.GoogleLoginException()

            val authUser = AuthenticatedUser(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: ""
            )

            val dataUser = DataUser(
                uid = authUser.uid,
                name = authUser.name,
                email = authUser.email
            )

            prefs.saveUserSession(authUser.uid, authUser.name, authUser.email)
            prefs.setUserLoggedIn(true)

            userRemoteDataSource.saveUserOnlyIfNew(dataUser)

            return authUser
        } catch (e: Exception) {
            throw when (e) {
                is AuthException -> e
                else -> AuthException.NetworkException()
            }
        }
    }
}