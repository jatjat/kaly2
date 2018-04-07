package ca.joelathiessen.android.kaly2android.ui.main

import ca.joelathiessen.android.kaly2android.repository.Repository
import ca.joelathiessen.android.kaly2android.repository.SimItr

class MainActivityViewModel(private val repository: Repository) {

    fun showDataFromApi(): SimItr = repository.getDataFromApi()
}