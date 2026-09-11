package com.smartspend.app.di

import com.smartspend.app.core.common.DefaultDispatcherProvider
import com.smartspend.app.core.common.DispatcherProvider
import com.smartspend.app.core.security.KeystoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideKeystoreManager(): KeystoreManager {
        return KeystoreManager()
    }

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider {
        return DefaultDispatcherProvider()
    }
}
