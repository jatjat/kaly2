package ca.joelathiessen.android.kaly2android.repository

open class ApiServiceFailure
class NoSessionObtainedFailure: ApiServiceFailure()
class NoSessionPresentFailure: ApiServiceFailure()
class SubscribedToAnotherSessionFailure: ApiServiceFailure()