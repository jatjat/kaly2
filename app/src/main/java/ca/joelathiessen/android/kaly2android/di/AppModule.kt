package ca.joelathiessen.android.kaly2android.di

import ca.joelathiessen.android.kaly2android.repository.ApiService
import ca.joelathiessen.android.kaly2android.repository.WebSocketApiService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

    @Module
    class AppModule {

        @Provides
        @Singleton
        fun provideApiService(): ApiService {
            return WebSocketApiService()
        }
    }
