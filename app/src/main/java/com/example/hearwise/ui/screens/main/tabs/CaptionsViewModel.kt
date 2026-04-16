package com.example.hearwise.ui.screens.main.tabs

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.whispercpp.whisper.WhisperContext
import com.example.hearwise.audio.AssetExtractor
import android.content.Context

// Curated supported languages matching requirements
val SupportedLanguages = listOf(
    "Auto-Detect" to "auto",
    "English" to "en",
    "Urdu" to "ur",
    "Spanish" to "es",
    "Arabic" to "ar"
)

class CaptionsViewModel : ViewModel() {

    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private var transcriptionJob: Job? = null
    
    // The JNI specific Whisper wrapper engine
    private var whisperContext: WhisperContext? = null
    private val isWhisperReady = MutableStateFlow(false)

    fun initializeWhisper(context: Context) {
        if (isWhisperReady.value) return
        viewModelScope.launch(Dispatchers.IO) {
            val modelPath = AssetExtractor.extractModelIfNeeded(context, "ggml-tiny.bin")
            if (modelPath.isNotEmpty()) {
                whisperContext = WhisperContext.createContextFromFile(modelPath)
                isWhisperReady.value = true
            }
        }
    }

    fun selectLanguage(isoCode: String) {
        _selectedLanguage.value = isoCode
    }

    fun toggleListening() {
        if (_isListening.value) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun clearText() {
        _transcribedText.value = ""
    }

    private fun startListening() {
        _isListening.value = true
        startTranscriptionLoop()
    }

    private fun stopListening() {
        _isListening.value = false
        transcriptionJob?.cancel()
        transcriptionJob = null
    }
    
    override fun onCleared() {
        super.onCleared()
        whisperContext?.release()
    }

    @SuppressLint("MissingPermission") // Handled implicitly outside by Jetpack Compose NavGraph flow
    private fun startTranscriptionLoop() {
        transcriptionJob = viewModelScope.launch(Dispatchers.IO) {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT

            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            
            // Exactly 3 seconds of audio at 16kHz
            // 1 sample = 1 short. 16,000 samples/sec * 3 sec = 48,000 shorts
            val samplesToRead = 48000
            
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                maxOf(minBufferSize, samplesToRead * 2) // Ensure buffer can hold at least 3 seconds
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                _transcribedText.update { it + "\n[System: AudioRecord failed to initialize]" }
                stopListening()
                return@launch
            }
            if (!isWhisperReady.value) {
                _transcribedText.update { it + "\n[System: Whisper Backend Not Loaded]" }
                stopListening()
                return@launch
            }

            try {
                audioRecord.startRecording()
                val chunkBuffer = ShortArray(samplesToRead)

                while (isActive && _isListening.value) {
                    var readSum = 0
                    
                    // Read until we have exactly 3 seconds of continuous audio framing
                    while (readSum < samplesToRead && isActive && _isListening.value) {
                        val readResult = audioRecord.read(
                            chunkBuffer, 
                            readSum, 
                            samplesToRead - readSum
                        )
                        if (readResult < 0) break
                        readSum += readResult
                    }

                    if (!isActive || !_isListening.value) break

                    // Convert ShortArray to FloatArray (-1.0f to +1.0f) for Whisper natively
                    val floatArray = FloatArray(samplesToRead)
                    for (i in 0 until samplesToRead) {
                        // Max value of Int16 is 32768.0f
                        floatArray[i] = chunkBuffer[i] / 32768.0f
                    }

                    // Standard Native JNI Call mapped to the FloatArray buffer
                    val transcription = whisperEngineTranscribe(floatArray, _selectedLanguage.value)
                    
                    if (transcription.isNotBlank()) {
                        _transcribedText.update { currentText ->
                            if (currentText.isEmpty()) transcription else "$currentText $transcription"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                audioRecord.stop()
                audioRecord.release()
            }
        }
    }

    /**
     * Executes actual native C++ layer logic bridging to Whisper context processing
     */
    private fun whisperEngineTranscribe(audioData: FloatArray, languageIso: String): String {
        return whisperContext?.transcribeData(audioData) 
            ?: "[System: Engine Failed to Read Data]"
    }
}
