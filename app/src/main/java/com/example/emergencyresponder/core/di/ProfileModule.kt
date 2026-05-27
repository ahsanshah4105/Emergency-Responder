package com.example.emergencyresponder.core.di

import com.example.emergencyresponder.modules.auth.data.dataSource.UserRemoteDataSource
import com.example.emergencyresponder.modules.dashboard.data.repositoryImpl.ProfileRepositoryImpl
import com.example.emergencyresponder.modules.dashboard.domain.repository.IProfileRepository
import com.example.emergencyresponder.modules.dashboard.domain.usecase.ChangeEmailUseCase
import com.example.emergencyresponder.modules.dashboard.domain.usecase.UpdateProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    @Provides
    @Singleton
    fun provideProfileRepository(
        userRemoteDataSource: UserRemoteDataSource // Hilt will look for this in your Auth/Data module
    ): IProfileRepository {
        return ProfileRepositoryImpl(userRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideUpdateProfileUseCase(repository: IProfileRepository): UpdateProfileUseCase {
        return UpdateProfileUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideChangeEmailUseCase(repository: IProfileRepository): ChangeEmailUseCase {
        return ChangeEmailUseCase(repository)
    }
}