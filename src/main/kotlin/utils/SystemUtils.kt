package io.mazurco066.utils

import java.io.BufferedReader
import java.util.*

object SystemUtils {
    /**
     * Function to execute a system command and return the output as a list of strings.
     */
    private fun executeCommand(command: Array<String>): List<String> {
        return try {
            val process = ProcessBuilder(*command).start()
            process.inputStream.bufferedReader().use(BufferedReader::readLines)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * This function returns a list of formatted microphone names on Windows platforms.
     */
    private fun getMicrophonesWindows(): List<String> {
        val command = arrayOf(
            "powershell.exe",
            "Get-CimInstance -Namespace root/cimv2 -ClassName Win32_SoundDevice | Select-Object -ExpandProperty Name"
        )
        return executeCommand(command)
    }

    /**
     * This function returns a list of formatted microphone names on Linux platforms.
     */
    private fun getMicrophonesLinux(): List<String> {
        val command = arrayOf("arecord", "-l")
        return executeCommand(command).filter { it.contains("card") }
    }

    /**
     * This function returns a list of microphones connected to the OS. It will be formatted according
     * to the OS you are using.
     */
    fun getConnectedMicrophones(): List<String> {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        return if (os.contains("win")) {
            getMicrophonesWindows()
        } else if (os.contains("nix") || os.contains("nux")) {
            getMicrophonesLinux()
        } else {
            listOf("Microphone 1", "Microphone 2", "Microphone 3")
        }
    }
}