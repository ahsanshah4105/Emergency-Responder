package com.example.emergencyresponder.core.di

import android.content.Context
import com.example.emergencyresponder.modules.dashboard.data.datasource.EmergencyContactRemoteDataSource
import com.example.emergencyresponder.modules.dashboard.data.ml.TFLiteCrashMlAnalyzer
import com.example.emergencyresponder.modules.dashboard.data.provider.AndroidSensorProvider
import com.example.emergencyresponder.modules.dashboard.data.provider.YAMNetClassifier
import com.example.emergencyresponder.modules.dashboard.data.repositoryImpl.EmergencyContactRepositoryImpl
import com.example.emergencyresponder.modules.dashboard.domain.engine.CrashDetectionEngine
import com.example.emergencyresponder.modules.dashboard.domain.engine.CrashPredictor
import com.example.emergencyresponder.modules.dashboard.domain.notifier.AlertNotifier
import com.example.emergencyresponder.modules.dashboard.domain.notifier.AndroidAlertNotifier
import com.example.emergencyresponder.modules.dashboard.domain.notifier.VoiceAlertManager
import com.example.emergencyresponder.modules.dashboard.domain.repository.IEmergencyContactRepository
import com.example.emergencyresponder.modules.dashboard.domain.repository.SensorProvider
import com.example.emergencyresponder.modules.dashboard.domain.usecase.AddEmergencyContactUseCase
import com.example.emergencyresponder.modules.dashboard.domain.usecase.AudioAnalysisUseCase
import com.example.emergencyresponder.modules.dashboard.domain.usecase.CrashDetectionUseCase
import com.example.emergencyresponder.modules.dashboard.domain.usecase.DeleteEmergencyContactUseCase
import com.example.emergencyresponder.modules.dashboard.domain.usecase.ObserveEmergencyContactsUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DashboardModule {

    @Provides
    @Singleton
    fun provideEmergencyRemoteDataSource(firestore: FirebaseFirestore): EmergencyContactRemoteDataSource =
        EmergencyContactRemoteDataSource(firestore)

    @Provides
    @Singleton
    fun provideEmergencyRepository(remote: EmergencyContactRemoteDataSource): IEmergencyContactRepository =
        EmergencyContactRepositoryImpl(remote)

    @Provides
    @Singleton
    fun provideObserveContactsUseCase(repo: IEmergencyContactRepository): ObserveEmergencyContactsUseCase =
        ObserveEmergencyContactsUseCase(repo)

    @Provides
    @Singleton
    fun provideAddContactUseCase(repo: IEmergencyContactRepository): AddEmergencyContactUseCase =
        AddEmergencyContactUseCase(repo)

    @Provides
    @Singleton
    fun provideDeleteContactUseCase(repo: IEmergencyContactRepository): DeleteEmergencyContactUseCase =
        DeleteEmergencyContactUseCase(repo)

    @Provides
    @Singleton
    fun provideCrashPredictor(@ApplicationContext context: Context): CrashPredictor =
        TFLiteCrashMlAnalyzer(context)

    @Provides
    @Singleton
    fun provideSensorProvider(@ApplicationContext context: Context): SensorProvider =
        AndroidSensorProvider(context)

    @Provides
    @Singleton
    fun provideAlertNotifier(@ApplicationContext context: Context): AlertNotifier =
        AndroidAlertNotifier(context)

    @Provides
    @Singleton
    fun provideVoiceAlertManager(@ApplicationContext context: Context): VoiceAlertManager =
        VoiceAlertManager(context)

    @Provides
    @Singleton
    fun provideCrashEngine(
        crashPredictor: CrashPredictor
    ): CrashDetectionEngine {
        // Default sensitivity; later this can be injected from preferences.
        return CrashDetectionEngine(
            crashPredictor = crashPredictor,
            useCase = CrashDetectionUseCase(Sensitivity.MEDIUM)
        )
    }

    @Provides
    @Singleton
    fun provideYamNetClassifier(@ApplicationContext context: Context): YAMNetClassifier =
        YAMNetClassifier(context)

    @Provides
    @Singleton
    fun provideAudioAnalysisUseCase(classifier: YAMNetClassifier): AudioAnalysisUseCase =
        AudioAnalysisUseCase(classifier)
}

