package com.example.hearwise.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.sin

class SineWaveGenerator {
    private var audioTrack: AudioTrack? = null
    private var generatorJob: Job? = null
    private val sampleRate = 44100
    
    @Volatile private var isPlaying = false
    @Volatile private var currentVolume = 0.0f

    fun startTone(frequency: Double, panLeft: Boolean) {
        stopTone()
        isPlaying = true
        currentVolume = 0.0f

        val minSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = if (minSize > 0) minSize else sampleRate // Safety fallback

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        generatorJob = CoroutineScope(Dispatchers.Default).launch {
            val chunkFrames = sampleRate / 10 // roughly 100ms segments
            val buffer = ShortArray(chunkFrames * 2) // 2 channels per frame
            var angle = 0.0
            val angleIncrement = 2.0 * Math.PI * frequency / sampleRate

            while (isActive && isPlaying) {
                // Snapshot the volatile volume for this chunk chunk
                val vol = currentVolume 
                for (i in 0 until chunkFrames) {
                    val sample = (sin(angle) * Short.MAX_VALUE * vol).toInt().toShort()
                    
                    if (panLeft) {
                        buffer[i * 2] = sample      // L
                        buffer[i * 2 + 1] = 0       // R
                    } else {
                        buffer[i * 2] = 0           // L
                        buffer[i * 2 + 1] = sample  // R
                    }
                    
                    angle += angleIncrement
                }
                
                // Prevent floating-point degradation
                if (angle > 2.0 * Math.PI) {
                    angle %= (2.0 * Math.PI)
                }
                
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun setVolume(volume: Float) {
        // Clamp 0.0f to 1.0f
        currentVolume = volume.coerceIn(0.0f, 1.0f)
    }

    fun stopTone() {
        isPlaying = false
        generatorJob?.cancel()
        generatorJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore state exceptions on rapid toggle
        }
        audioTrack = null
    }
}
