package ca.joelathiessen.kaly2.android.di

import ca.joelathiessen.kaly2.android.ui.main.MainActivity
import ca.joelathiessen.kaly2.android.ui.main.MainActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = arrayOf(MainActivityModule::class))
    abstract fun bindMainActivity(): MainActivity
}