package ca.joelathiessen.android.kaly2android.repository

sealed class RepositoryFailure
class RepositoryAlreadySubscribedFailure: RepositoryFailure()
class RepositoryApiServiceFailure(val apiServiceError: ApiServiceFailure): RepositoryFailure()
class RepositoryIsUnsubscribingFailure: RepositoryFailure()
