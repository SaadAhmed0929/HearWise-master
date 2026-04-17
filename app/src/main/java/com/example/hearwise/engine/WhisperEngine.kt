package com.example.hearwise.engine

/**
 * Kotlin singleton that wraps the native whisper-hearwise .so library.
 * The JNI method names must match:
 *   Java_com_example_hearwise_engine_WhisperEngine_<methodName>
 */
object WhisperEngine {

    init {
        System.loadLibrary("whisper-hearwise")
    }

    /** Load the GGML model from an absolute file path. Returns true on success. */
    external fun initModel(modelPath: String): Boolean

    /**
     * Transcribe 16 kHz mono PCM audio (float samples in [-1, 1]).
     * @param audioData FloatArray of 16000 samples/sec PCM
     * @param language  BCP-47 short code, e.g. "en", "ur", "es", "ar", or "auto"
     * @return Transcribed text string (may be empty if no speech detected)
     */
    external fun transcribe(audioData: FloatArray, language: String): String

    /** Free the native whisper context — call when the ViewModel is cleared. */
    external fun destroyModel()
}
