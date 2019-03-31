package ca.joelathiessen.kaly2.core.ev3

import java.io.BufferedReader
import java.io.BufferedWriter

interface SerialConnection {
    val outputWriter: BufferedWriter
    val inputReader: BufferedReader
    fun close()
}