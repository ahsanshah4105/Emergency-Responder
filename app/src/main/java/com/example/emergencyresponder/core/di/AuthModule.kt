package com.example.emergencyresponder.core.di

import LoginRepositoryImpl
import com.example.emergencyresponder.core.data.local.UserPreferencesManager
import com.example.emergencyresponder.core.domain.repository.IBasePreference
import com.example.emergencyresponder.core.utils.SOSUtils.prefProvider
import com.example.emergencyresponder.modules.auth.data.dataSource.AuthRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.auth.data.repository.ForgotPasswordRepositoryImpl
import com.example.emergencyresponder.modules.auth.data.repository.SignUpRepositoryImpl
import com.example.emergencyresponder.modules.auth.data.repository.UserPreferencesImpl
import com.example.emergencyresponder.modules.auth.domain.repository.ForgotPasswordRepository
import com.example.emergencyresponder.modules.auth.domain.repository.LoginRepository
import com.example.emergencyresponder.modules.auth.domain.repository.SignUpRepository
import com.example.emergencyresponder.modules.auth.domain.repository.UserPreferences
import com.example.emergencyresponder.modules.auth.domain.usecase.ForgotPasswordUseCase
import com.example.emergencyresponder.modules.auth.domain.usecase.LoginUseCase
import com.example.emergencyresponder.modules.auth.domain.usecase.SignUpUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(): AuthRemoteDataSource = AuthRemoteDataSource()

    @Provides
    @Singleton
    fun provideUserRemoteDataSource(firestore: FirebaseFirestore): UserRemoteDataSource =
        UserRemoteDataSource(firestore)

    @Provides
    @Singleton
    fun provideUserPreferences(): UserPreferences = UserPreferencesImpl(prefProvider)

    @Provides
    @Singleton
    fun provideUserPreferencesManager(prefProvider: IBasePreference): UserPreferencesManager =
        UserPreferencesManager(prefProvider)

    @Provides
    @Singleton
    fun provideLoginRepository(
        authDataSource: AuthRemoteDataSource,
        userRemoteDataSource: UserRemoteDataSource,
        userPreferences: UserPreferences
    ): LoginRepository = LoginRepositoryImpl(authDataSource, userRemoteDataSource, userPreferences)

    @Provides
    @Singleton
    fun provideSignUpRepository(
        authDataSource: AuthRemoteDataSource,
        userRemoteDataSource: UserRemoteDataSource
    ): SignUpRepository = SignUpRepositoryImpl(authDataSource, userRemoteDataSource)

    @Provides
    @Singleton
    fun provideForgotPasswordRepository(
        authDataSource: AuthRemoteDataSource
    ): ForgotPasswordRepository = ForgotPasswordRepositoryImpl(authDataSource)

    @Provides
    @Singleton
    fun provideLoginUseCase(loginRepository: LoginRepository): LoginUseCase =
        LoginUseCase(loginRepository)

    @Provides
    @Singleton
    fun provideSignUpUseCase(signUpRepository: SignUpRepository): SignUpUseCase =
        SignUpUseCase(signUpRepository)

    @Provides
    @Singleton
    fun provideForgotPasswordUseCase(
        forgotPasswordRepository: ForgotPasswordRepository
    ): ForgotPasswordUseCase = ForgotPasswordUseCase(forgotPasswordRepository)
}
