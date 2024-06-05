package io.mazurco066.utils

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.TargetDataLine

object SystemUtils {
    /**
     * This function returns a list of microphones connected to the OS. It will be formatted according
     * to the OS you are using.
     */
    fun getConnectedMicrophones(): List<String> {
        val mixerInfo = AudioSystem.getMixerInfo()
        val microphones = mutableListOf<String>()
        for (mi in mixerInfo) {
            val mixer = AudioSystem.getMixer(mi)
            val targetLines = mixer.targetLineInfo
            for (lineInfo in targetLines) {
                if (lineInfo.lineClass == TargetDataLine::class.java) {
                    microphones.add(mi.name)
                }
            }
        }
        return microphones
    }
}