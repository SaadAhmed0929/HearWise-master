package com.example.hearwise.audio

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object AssetExtractor {

    /**
     * Copies the whisper model file from the APK's assets folder into the internal
     * files directory so that the C++ engine can load it using absolute file paths.
     */
    fun extractModelIfNeeded(context: Context, assetName: String = "ggml-tiny.bin"): String {
        val targetFile = File(context.filesDir, assetName)

        if (!targetFile.exists()) {
            try {
                context.assets.open(assetName).use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        val buffer = ByteArray(1024 * 4)
                        var read: Int
                        while (inputStream.read(buffer).also { read = it } != -1) {
                            outputStream.write(buffer, 0, read)
                        }
                        outputStream.flush()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // In a production app, handle robustly. Returning empty path if failed.
                return ""
            }
        }
        
        return targetFile.absolutePath
    }
}
