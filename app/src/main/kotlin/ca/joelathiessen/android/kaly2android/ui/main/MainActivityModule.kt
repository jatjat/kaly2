package ca.joelathiessen.android.kaly2android.ui.main

import ca.joelathiessen.android.kaly2android.repository.RobotSessionRepository
import dagger.Module
import dagger.Provides

@Module
class MainActivityModule {

    @Provides
    fun providePresenter(repository: RobotSessionRepository) = MainActivityPresenter(repository)
}