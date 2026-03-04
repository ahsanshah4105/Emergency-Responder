package com.example.emergencyresponder.core.di

import android.content.Context
import com.example.emergencyresponder.core.data.local.PreferenceProviderImpl
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.dashboard.data.repositoryImpl.ProfileRepositoryImpl
import com.example.emergencyresponder.modules.onboarding.data.repository.IOnboardingRepositoryImpl
import com.example.emergencyresponder.modules.onboarding.domain.repository.IOnboardingRepository
import com.example.emergencyresponder.modules.splash.data.repository.ISplashRepositoryImpl
import com.example.emergencyresponder.modules.timestamp.data.repository.ICrashRepositoryImpl
import com.example.emergencyresponder.modules.timestamp.domain.repository.ICrashRepository
import com.example.emergencyresponder.modules.splash.domain.repository.ISplashRepository
import com.example.emergencyresponder.modules.dashboard.domain.usecase.ChangeEmailUseCase
import com.example.emergencyresponder.modules.dashboard.domain.usecase.UpdateProfileUseCase
import com.example.emergencyresponder.modules.dashboard.domain.repository.IProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.example.emergencyresponder.modules.dashboard.data.datasource.EmergencyContactRemoteDataSource
import com.example.emergencyresponder.modules.dashboard.data.repositoryImpl.EmergencyContactRepositoryImpl
import com.example.emergencyresponder.modules.dashboard.domain.repository.IEmergencyContactRepository
import com.example.emergencyresponder.modules.dashboard.domain.usecase.DeleteEmergencyContactUseCase
import ObserveEmergencyContactsUseCase
import AddEmergencyContactUseCase
import com.example.emergencyresponder.core.common.PrefKeys
import com.example.emergencyresponder.core.manager.SPreferenceManager
import com.example.emergencyresponder.modules.dashboard.data.ml.CrashMlAnalyzer
import com.example.emergencyresponder.modules.dashboard.data.ml.TFLiteCrashMlAnalyzer
import com.example.emergencyresponder.modules.dashboard.data.provider.AndroidSensorProvider
import com.example.emergencyresponder.modules.dashboard.domain.engine.CrashDetectionEngine
import com.example.emergencyresponder.modules.dashboard.domain.notifier.AlertNotifier
import com.example.emergencyresponder.modules.dashboard.domain.notifier.AndroidAlertNotifier
import com.example.emergencyresponder.modules.dashboard.domain.notifier.VoiceAlertManager
import com.example.emergencyresponder.modules.dashboard.domain.repository.SensorProvider
import com.example.emergencyresponder.modules.dashboard.domain.usecase.CrashDetectionUseCase
import com.example.emergencyresponder.modules.dashboard.data.provider.YAMNetClassifier
import com.example.emergencyresponder.core.data.local.UserPreferencesManager
import com.example.emergencyresponder.modules.dashboard.domain.usecase.AudioAnalysisUseCase
class AppContainer(private val context: Context) {

    val prefProvider: IBasePreference by lazy {
        PreferenceProviderImpl(context.applicationContext)
    }

    val userPrefs: UserPreferencesManager by lazy {
        UserPreferencesManager(prefProvider)
    }
    val crashRepository: ICrashRepository by lazy {
        ICrashRepositoryImpl(prefProvider)
    }

    val splashRepository: ISplashRepository by lazy {
        ISplashRepositoryImpl(prefProvider)
    }

    val onboardingRepository: IOnboardingRepository by lazy {
        IOnboardingRepositoryImpl(prefProvider)
    }
    private val userRemoteDataSource by lazy { UserRemoteDataSource() }

    val profileRepository: IProfileRepository by lazy {
        ProfileRepositoryImpl(userRemoteDataSource)
    }
    val updateProfileUseCase by lazy { UpdateProfileUseCase(profileRepository) }
    val changeEmailUseCase by lazy { ChangeEmailUseCase(profileRepository) }

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val emergencyDataSource by lazy { EmergencyContactRemoteDataSource(firestore) }
    val emergencyRepository: IEmergencyContactRepository by lazy {
        EmergencyContactRepositoryImpl(emergencyDataSource)
    }

    val observeContactsUseCase by lazy { ObserveEmergencyContactsUseCase(emergencyRepository) }
    val addContactUseCase by lazy { AddEmergencyContactUseCase(emergencyRepository) }
    val deleteContactUseCase by lazy { DeleteEmergencyContactUseCase(emergencyRepository) }

    val mlAnalyzer: CrashMlAnalyzer by lazy {
        TFLiteCrashMlAnalyzer(context)
    }

    val sensorProvider: SensorProvider by lazy {
        AndroidSensorProvider(context)
    }

    val notifier: AlertNotifier by lazy {
        AndroidAlertNotifier(context)
    }

    val voiceAlertManager: VoiceAlertManager by lazy {
        VoiceAlertManager(context)
    }

    private val _crashEngine: CrashDetectionEngine by lazy {
        val savedSens = prefProvider.getString(PrefKeys.SENSITIVITY, "MEDIUM")
        val sensitivityEnum = when(savedSens) {
            "HIGH" -> Sensitivity.HIGH
            "MEDIUM" -> Sensitivity.MEDIUM
            else -> Sensitivity.LOW
        }

        CrashDetectionEngine(
            mlAnalyzer = mlAnalyzer,
            useCase = CrashDetectionUseCase(sensitivityEnum)
        )
    }

    fun getCrashEngine(): CrashDetectionEngine {
        return _crashEngine
    }

    val audioClassifier: YAMNetClassifier by lazy {
        YAMNetClassifier(context)
    }

    val audioAnalysisUseCase: AudioAnalysisUseCase by lazy {
        AudioAnalysisUseCase(audioClassifier)
    }

}