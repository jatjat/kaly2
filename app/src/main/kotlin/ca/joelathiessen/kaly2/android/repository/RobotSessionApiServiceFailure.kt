package ca.joelathiessen.kaly2.android.repository

open class ApiServiceFailure
class NoSessionObtainedFailure: ApiServiceFailure()
class NoSessionPresentFailure: ApiServiceFailure()
class SubscribedToAnotherSessionFailure: ApiServiceFailure()