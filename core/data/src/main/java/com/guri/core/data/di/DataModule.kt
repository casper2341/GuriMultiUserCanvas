package com.guri.core.data.di

import com.guri.core.data.repository.UserRepository
import com.guri.core.data.repository.impl.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    
    // We'll add more repository bindings as we implement them
    /*
    @Binds
    @Singleton
    abstract fun bindRoomRepository(impl: RoomRepositoryImpl): RoomRepository
    
    @Binds
    @Singleton
    abstract fun bindCanvasRepository(impl: CanvasRepositoryImpl): CanvasRepository
    */
} 