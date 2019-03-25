package ca.joelathiessen.kaly2.core.ev3

interface SerialConnectionCreator {
    fun createConnection(): SerialConnection?
}