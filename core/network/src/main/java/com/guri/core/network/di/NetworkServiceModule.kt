package com.guri.core.network.di

import com.guri.core.network.service.CanvasNetworkService
import com.guri.core.network.service.RoomNetworkService
import com.guri.core.network.service.UserNetworkService
import com.guri.core.network.service.impl.CanvasNetworkServiceImpl
import com.guri.core.network.service.impl.RoomNetworkServiceImpl
import com.guri.core.network.service.impl.UserNetworkServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkServiceModule {

    @Binds
    @Singleton
    abstract fun bindUserNetworkService(impl: UserNetworkServiceImpl): UserNetworkService

    @Binds
    @Singleton
    abstract fun bindRoomNetworkService(impl: RoomNetworkServiceImpl): RoomNetworkService

    @Binds
    @Singleton
    abstract fun bindCanvasNetworkService(impl: CanvasNetworkServiceImpl): CanvasNetworkService
} 