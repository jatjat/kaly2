package ca.joelathiessen.android.kaly2android.di

import ca.joelathiessen.android.kaly2android.ui.main.MainActivity
import ca.joelathiessen.android.kaly2android.ui.main.MainActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = arrayOf(MainActivityModule::class))
    abstract fun bindMainActivity(): MainActivity
}