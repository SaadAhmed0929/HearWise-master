package com.example.hearwise.feature.dashboard

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearwise.engine.AssetExtractor
import com.example.hearwise.engine.WhisperEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

val SupportedLanguages = listOf(
    "Auto-Detect" to "auto",
    "English"     to "en",
    "Urdu"        to "ur",
    "Spanish"     to "es",
    "Arabic"      to "ar"
)

class CaptionsViewModel(application: Application) : AndroidViewModel(application) {

    private val _transcribedText  = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    private val _isListening      = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _statusMessage    = MutableStateFlow("Tap 'Start Listening' to begin")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private var transcriptionJob: Job? = null
    private var engineReady = false

    // ── Initialization ─────────────────────────────────────────────────────────

    fun initializeWhisper(context: Context) {
        if (engineReady) return
        viewModelScope.launch(Dispatchers.IO) {
            _statusMessage.value = "Loading model…"
            val modelPath = AssetExtractor.extractModelIfNeeded(context, "ggml-tiny.bin")
            if (modelPath.isEmpty()) {
                _statusMessage.value = "Error: ggml-tiny.bin not found in assets/!"
                return@launch
            }
            val ok = try {
                WhisperEngine.initModel(modelPath)
            } catch (t: Throwable) {
                _statusMessage.value = "Native error: ${t.message}"
                return@launch
            }
            engineReady = ok
            _statusMessage.value = if (ok) "Model ready. Tap Start Listening!"
                                   else    "Failed to load model."
        }
    }

    // ── Public controls ────────────────────────────────────────────────────────

    fun selectLanguage(isoCode: String) { _selectedLanguage.value = isoCode }

    fun toggleListening() {
        if (_isListening.value) stopListening() else startListening()
    }

    fun clearText() { _transcribedText.value = "" }

    // ── Audio capture + whisper inference loop ──────────────────────────────────

    @SuppressLint("MissingPermission")
    private fun startListening() {
        if (!engineReady) {
            _statusMessage.value = "Model not ready yet. Please wait."
            return
        }
        _isListening.value = true
        _statusMessage.value = "Listening…"

        transcriptionJob = viewModelScope.launch(Dispatchers.IO) {
            val sampleRate    = 16_000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat   = AudioFormat.ENCODING_PCM_16BIT

            // 3-second capture chunks (16000 samples/sec × 3 sec = 48000 shorts)
            val samplesPerChunk = 48_000
            val bufferSize = maxOf(
                AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat),
                samplesPerChunk * 2
            )

            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate, channelConfig, audioFormat, bufferSize
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                _statusMessage.value = "Error: could not open microphone."
                _isListening.value = false
                return@launch
            }

            recorder.startRecording()
            val shortBuf = ShortArray(samplesPerChunk)

            while (isActive && _isListening.value) {
                // Fill exactly one chunk
                var filled = 0
                while (filled < samplesPerChunk && isActive && _isListening.value) {
                    val n = recorder.read(shortBuf, filled, samplesPerChunk - filled)
                    if (n < 0) break
                    filled += n
                }
                if (!isActive || !_isListening.value) break

                // Convert Int16 → Float32 in [-1, 1]
                val floats = FloatArray(filled) { shortBuf[it] / 32768.0f }

                // Skip silent chunks (RMS < 0.008) to avoid empty results
                var sumSq = 0.0
                for (s in floats) sumSq += s * s.toDouble()
                val rms = Math.sqrt(sumSq / floats.size)
                if (rms < 0.008) continue

                // Run whisper inference
                val lang = _selectedLanguage.value
                val text = WhisperEngine.transcribe(floats, lang).trim()

                if (text.isNotEmpty()) {
                    _transcribedText.update { current ->
                        if (current.isEmpty()) text else "$current $text"
                    }
                }
            }

            recorder.stop()
            recorder.release()
            _statusMessage.value = "Stopped."
        }
    }

    private fun stopListening() {
        _isListening.value = false
        transcriptionJob?.cancel()
        transcriptionJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
        if (engineReady) {
            WhisperEngine.destroyModel()
            engineReady = false
        }
    }
}
