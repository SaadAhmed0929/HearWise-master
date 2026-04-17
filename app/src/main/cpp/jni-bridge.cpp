#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "whisper.h"

#define LOG_TAG "HearWise-Whisper"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// One global context per app process — heap-allocated, properly freed on destroy
static whisper_context* g_ctx = nullptr;

extern "C" {

// ─────────────────────────────────────────────────────────────────────────────
// com.example.hearwise.engine.WhisperEngine.initModel(modelPath: String): Boolean
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL
Java_com_example_hearwise_engine_WhisperEngine_initModel(
        JNIEnv* env, jobject /* this */, jstring modelPathJava)
{
    const char* path = env->GetStringUTFChars(modelPathJava, nullptr);
    LOGI("Loading model from: %s", path);

    // Release any previous context
    if (g_ctx != nullptr) {
        whisper_free(g_ctx);
        g_ctx = nullptr;
    }

    whisper_context_params cparams = whisper_context_default_params();
    cparams.use_gpu = false; // CPU-only on Android

    g_ctx = whisper_init_from_file_with_params(path, cparams);
    env->ReleaseStringUTFChars(modelPathJava, path);

    if (g_ctx == nullptr) {
        LOGE("Failed to load whisper model!");
        return JNI_FALSE;
    }

    LOGI("Model loaded successfully. Encoder layers: %d", whisper_model_n_text_layer(g_ctx));
    return JNI_TRUE;
}

// ─────────────────────────────────────────────────────────────────────────────
// com.example.hearwise.engine.WhisperEngine.transcribe(audio: FloatArray, lang: String): String
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT jstring JNICALL
Java_com_example_hearwise_engine_WhisperEngine_transcribe(
        JNIEnv* env, jobject /* this */, jfloatArray audioDataJava, jstring languageJava)
{
    if (g_ctx == nullptr) {
        return env->NewStringUTF("[Error: whisper model not loaded]");
    }

    // Map the Java float[] into a C pointer without copying
    jsize     audioLen   = env->GetArrayLength(audioDataJava);
    jfloat*   audioData  = env->GetFloatArrayElements(audioDataJava, nullptr);
    const char* lang     = env->GetStringUTFChars(languageJava, nullptr);

    // Build inference params
    whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.language           = lang;
    params.translate          = false;       // keep original language
    params.no_context         = true;        // don't use previous segment context
    params.single_segment     = false;
    params.print_realtime     = false;
    params.print_progress     = false;
    params.print_timestamps   = false;
    params.print_special      = false;
    params.n_threads          = 4;           // 4 threads on modern Android cores

    int rc = whisper_full(g_ctx, params, audioData, (int)audioLen);

    env->ReleaseFloatArrayElements(audioDataJava, audioData, JNI_ABORT);
    env->ReleaseStringUTFChars(languageJava, lang);

    if (rc != 0) {
        LOGE("whisper_full() returned error %d", rc);
        return env->NewStringUTF("");
    }

    // Concatenate all segments into one string
    std::string result;
    int n = whisper_full_n_segments(g_ctx);
    for (int i = 0; i < n; ++i) {
        const char* seg = whisper_full_get_segment_text(g_ctx, i);
        if (seg) result += seg;
    }

    LOGI("Transcribed %d segment(s): %s", n, result.c_str());
    return env->NewStringUTF(result.c_str());
}

// ─────────────────────────────────────────────────────────────────────────────
// com.example.hearwise.engine.WhisperEngine.destroyModel()
// ─────────────────────────────────────────────────────────────────────────────
JNIEXPORT void JNICALL
Java_com_example_hearwise_engine_WhisperEngine_destroyModel(JNIEnv*, jobject)
{
    if (g_ctx != nullptr) {
        LOGI("Freeing whisper context");
        whisper_free(g_ctx);
        g_ctx = nullptr;
    }
}

} // extern "C"
