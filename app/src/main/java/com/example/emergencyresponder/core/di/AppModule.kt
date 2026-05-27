package com.example.emergencyresponder.core.di

import android.content.Context
import com.example.emergencyresponder.core.data.local.PreferenceProviderImpl
import com.example.emergencyresponder.core.data.coroutines.DefaultDispatcherProvider
import com.example.emergencyresponder.core.data.session.FirebaseAuthSession
import com.example.emergencyresponder.core.domain.coroutines.DispatcherProvider
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.core.domain.session.AuthSession
import com.example.emergencyresponder.core.manager.CrashCountdownManager
import com.example.emergencyresponder.modules.onboarding.data.repository.IOnboardingRepositoryImpl
import com.example.emergencyresponder.modules.onboarding.domain.repository.IOnboardingRepository
import com.example.emergencyresponder.modules.splash.data.repository.ISplashRepositoryImpl
import com.example.emergencyresponder.modules.splash.domain.repository.ISplashRepository
import com.example.emergencyresponder.modules.timestamp.data.repository.ICrashRepositoryImpl
import com.example.emergencyresponder.modules.timestamp.domain.repository.ICountdownManager
import com.example.emergencyresponder.modules.timestamp.domain.repository.ICrashRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideBasePreference(@ApplicationContext context: Context): IBasePreference =
        PreferenceProviderImpl(context.applicationContext)

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthSession(firebaseAuth: FirebaseAuth): AuthSession =
        FirebaseAuthSession(firebaseAuth)

    @Provides
    @Singleton
    fun provideSplashRepository(prefProvider: IBasePreference): ISplashRepository =
        ISplashRepositoryImpl(prefProvider)

    @Provides
    @Singleton
    fun provideOnboardingRepository(prefProvider: IBasePreference): IOnboardingRepository =
        IOnboardingRepositoryImpl(prefProvider)

    @Provides
    @Singleton
    fun provideCrashRepository(prefProvider: IBasePreference): ICrashRepository =
        ICrashRepositoryImpl(prefProvider)

    @Provides
    @Singleton
    fun provideCountdownManager(): ICountdownManager = CrashCountdownManager

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()


}
