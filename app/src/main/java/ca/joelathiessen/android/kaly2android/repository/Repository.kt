package ca.joelathiessen.android.kaly2android.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(private val apiService: ApiService) {

    fun getDataFromApi(): SimItr = apiService.getJsonResponse()

}