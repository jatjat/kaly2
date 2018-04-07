package ca.joelathiessen.android.kaly2android.ui.main

import ca.joelathiessen.android.kaly2android.repository.Repository
import dagger.Module
import dagger.Provides

@Module
class MainActivityModule {

    @Provides
    fun provideViewModel(repository: Repository) = MainActivityViewModel(repository)
}