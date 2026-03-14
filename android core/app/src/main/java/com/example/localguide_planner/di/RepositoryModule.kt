package com.example.localguide_planner.di

import com.example.localguide_planner.data.repository.PlacesRepositoryImpl
import com.example.localguide_planner.data.repository.UserProfileRepositoryImpl
import com.example.localguide_planner.domain.repository.PlacesRepository
import com.example.localguide_planner.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlacesRepository(impl: PlacesRepositoryImpl): PlacesRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository
}
