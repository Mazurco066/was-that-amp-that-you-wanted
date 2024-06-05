package io.mazurco066

import io.mazurco066.utils.SystemUtils
import javafx.application.Application
import javafx.beans.binding.Bindings
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.control.ToggleButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javax.sound.sampled.*

class MainApp: Application() {
    private var line: TargetDataLine? = null
    private var audioInputStream: AudioInputStream? = null
    private var speakerLine: SourceDataLine? = null

    override fun start(primaryStage: Stage) {
        // Create a ComboBox for microphone selection
        val micLabel = Label("Select Microphone")
        micLabel.style = "-fx-font-size: 14px; -fx-padding: 0px 0px 5px 0px;"

        val micComboBox = ComboBox<String>()
        micComboBox.promptText = "Select Microphone"
        micComboBox.items.addAll(SystemUtils.getConnectedMicrophones())
        micComboBox.style = "-fx-padding: 10px;"

        val micBox = VBox(5.0, micLabel, micComboBox)
        micBox.alignment = Pos.CENTER

        // Create a power ToggleButton (on/off switch)
        val powerToggle = ToggleButton("Power Off")
        powerToggle.style = "-fx-font-size: 16px; -fx-padding: 10px; -fx-background-color: black; -fx-text-fill: white;"
        powerToggle.setOnAction {
            if (powerToggle.isSelected) {
                powerToggle.text = "Power On"
                powerToggle.style = "-fx-font-size: 16px; -fx-padding: 10px; -fx-background-color: green; -fx-text-fill: white;"
                startAudioCapture(micComboBox.selectionModel.selectedItem)
            } else {
                powerToggle.text = "Power Off"
                powerToggle.style = "-fx-font-size: 16px; -fx-padding: 10px; -fx-background-color: black; -fx-text-fill: white;"
                stopAudioCapture()
            }
        }

        val powerBox = VBox(5.0, powerToggle)
        powerBox.alignment = Pos.CENTER

        // Create a volume label
        val volumeLabel = Label("Volume")
        volumeLabel.style = "-fx-font-size: 14px; -fx-padding: 0px 0px 5px 0px;"

        // Create a volume slider
        val volumeSlider = Slider(0.0, 100.0, 50.0)
        volumeSlider.style = "-fx-padding: 10px;"
        volumeSlider.disableProperty().bind(Bindings.not(powerToggle.selectedProperty()))

        val volumeBox = VBox(5.0, volumeLabel, volumeSlider)
        volumeBox.alignment = Pos.CENTER

        // Create optional sliders with checkboxes for Gain, Compression, and Distortion
        val gainLabel = Label("Gain")
        val gainCheckbox = CheckBox()
        val gainSlider = Slider(0.0, 100.0, 50.0)
        gainSlider.style = "-fx-padding: 10px;"
        gainSlider.disableProperty().bind(Bindings.or(
            Bindings.not(gainCheckbox.selectedProperty()),
            Bindings.not(powerToggle.selectedProperty()))
        )
        val gainBox = VBox(5.0, gainLabel, HBox(
            10.0,
            gainCheckbox,
            gainSlider
        ))
        gainBox.alignment = Pos.CENTER_LEFT
        HBox.setHgrow(gainSlider, Priority.ALWAYS)
        gainBox.children[1].apply {
            HBox.setHgrow(this, Priority.ALWAYS)
            (this as HBox).alignment = Pos.CENTER_LEFT
        }

        val compressionLabel = Label("Compression")
        val compressionCheckbox = CheckBox()
        val compressionSlider = Slider(0.0, 100.0, 50.0)
        compressionSlider.style = "-fx-padding: 10px;"
        compressionSlider.disableProperty().bind(Bindings.or(Bindings.not(
            compressionCheckbox.selectedProperty()),
            Bindings.not(powerToggle.selectedProperty()))
        )
        val compressionBox = VBox(5.0, compressionLabel, HBox(
            10.0,
            compressionCheckbox,
            compressionSlider
        ))
        compressionBox.alignment = Pos.CENTER_LEFT
        HBox.setHgrow(compressionSlider, Priority.ALWAYS)
        compressionBox.children[1].apply {
            HBox.setHgrow(this, Priority.ALWAYS)
            (this as HBox).alignment = Pos.CENTER_LEFT
        }

        val distortionLabel = Label("Distortion")
        val distortionCheckbox = CheckBox()
        val distortionSlider = Slider(0.0, 100.0, 50.0)
        distortionSlider.style = "-fx-padding: 10px;"
        distortionSlider.disableProperty().bind(Bindings.or(
            Bindings.not(distortionCheckbox.selectedProperty()),
            Bindings.not(powerToggle.selectedProperty()))
        )
        val distortionBox = VBox(5.0, distortionLabel, HBox(
            10.0,
            distortionCheckbox,
            distortionSlider
        ))
        distortionBox.alignment = Pos.CENTER_LEFT
        HBox.setHgrow(distortionSlider, Priority.ALWAYS)
        distortionBox.children[1].apply {
            HBox.setHgrow(this, Priority.ALWAYS)
            (this as HBox).alignment = Pos.CENTER_LEFT
        }

        // Compose main state
        val layout = VBox(20.0)
        layout.padding = Insets(20.0)
        layout.alignment = Pos.CENTER
        layout.children.addAll(
            micBox,
            powerBox,
            volumeBox,
            gainBox,
            compressionBox,
            distortionBox
        )

        // Display window
        val scene = Scene(layout, 350.0, 480.0)
        primaryStage.title = "PC Microphone Amp"
        primaryStage.scene = scene
        primaryStage.isResizable = false
        primaryStage.show()
    }

    private fun startAudioCapture(selectedMic: String?) {
        val mixerInfos = AudioSystem.getMixerInfo()
        var selectedMixer: Mixer? = null
        for (mixerInfo in mixerInfos) {
            val mixer = AudioSystem.getMixer(mixerInfo)
            val targetLines = mixer.targetLineInfo
            for (lineInfo in targetLines) {
                if (lineInfo.lineClass == TargetDataLine::class.java && mixerInfo.name == selectedMic) {
                    selectedMixer = mixer
                    break
                }
            }
        }

        if (selectedMixer == null) {
            println("Selected microphone not found")
            return
        }

        val audioFormat = AudioFormat(44100.0f, 16, 1, true, false)
        val info = DataLine.Info(TargetDataLine::class.java, audioFormat)

        if (!selectedMixer.isLineSupported(info)) {
            println("Line not supported")
            return
        }

        line = selectedMixer.getLine(info) as TargetDataLine
        line?.open(audioFormat)
        line?.start()

        val buffer = ByteArray(1024)

        Thread {
            try {
                while (line?.read(buffer, 0, buffer.size) != -1) {
                    playCapturedAudio(buffer)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }.start()
    }

    private fun playCapturedAudio(data: ByteArray) {
        if (speakerLine == null) {
            val audioFormat = AudioFormat(44100.0f, 16, 1, true, false)
            val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
            speakerLine = AudioSystem.getLine(info) as SourceDataLine
            speakerLine?.open(audioFormat)
            speakerLine?.start()
        }
        speakerLine?.write(data, 0, data.size)
    }

    private fun stopAudioCapture() {
        line?.stop()
        line?.close()
        speakerLine?.stop()
        speakerLine?.close()
    }
}

fun main() {
   Application.launch(MainApp::class.java)
}