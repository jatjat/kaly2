package ca.joelathiessen.kaly2.android.repository

sealed class RepositoryFailure
class RepositoryAlreadySubscribedFailure: RepositoryFailure()
class RepositoryApiServiceFailure(val apiServiceError: ApiServiceFailure): RepositoryFailure()
class RepositoryIsUnsubscribingFailure: RepositoryFailure()
