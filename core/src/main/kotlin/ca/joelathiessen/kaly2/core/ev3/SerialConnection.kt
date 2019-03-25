package ca.joelathiessen.kaly2.core.ev3

import java.io.BufferedReader
import java.io.BufferedWriter

interface SerialConnection {
    fun getOutputWriter(): BufferedWriter
    fun getInputReader(): BufferedReader
    fun close()
}