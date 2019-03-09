package ca.joelathiessen.kaly2.android.ui.main

import ca.joelathiessen.kaly2.android.repository.RobotSessionRepository
import dagger.Module
import dagger.Provides

@Module
class MainActivityModule {

    @Provides
    fun providePresenter(repository: RobotSessionRepository) = MainActivityPresenter(repository)
}