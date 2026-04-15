package com.example.hearwise.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.io.Serializable

data class HearingThresholds(
    var hz_250: Int = 0,
    var hz_500: Int = 0,
    var hz_1000: Int = 0,
    var hz_2000: Int = 0,
    var hz_4000: Int = 0,
    var hz_8000: Int = 0
) : Serializable {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("hz_250", hz_250)
        json.put("hz_500", hz_500)
        json.put("hz_1000", hz_1000)
        json.put("hz_2000", hz_2000)
        json.put("hz_4000", hz_4000)
        json.put("hz_8000", hz_8000)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): HearingThresholds {
            return HearingThresholds(
                hz_250 = json.optInt("hz_250", 0),
                hz_500 = json.optInt("hz_500", 0),
                hz_1000 = json.optInt("hz_1000", 0),
                hz_2000 = json.optInt("hz_2000", 0),
                hz_4000 = json.optInt("hz_4000", 0),
                hz_8000 = json.optInt("hz_8000", 0)
            )
        }
    }
}

data class HearingProfile(
    val userId: String = "local_user",
    var timestamp: String = "",
    val leftEar: HearingThresholds = HearingThresholds(),
    val rightEar: HearingThresholds = HearingThresholds()
) : Serializable {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("userId", userId)
        json.put("timestamp", timestamp)
        json.put("leftEar", leftEar.toJson())
        json.put("rightEar", rightEar.toJson())
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): HearingProfile {
            return HearingProfile(
                userId = json.optString("userId", "local_user"),
                timestamp = json.optString("timestamp", ""),
                leftEar = HearingThresholds.fromJson(json.optJSONObject("leftEar") ?: JSONObject()),
                rightEar = HearingThresholds.fromJson(json.optJSONObject("rightEar") ?: JSONObject())
            )
        }
    }
}

object ProfileManager {
    private const val PREFS_NAME = "HearWisePrefs"
    private const val PROFILE_KEY = "HearingProfile"

    fun saveProfile(context: Context, profile: HearingProfile) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PROFILE_KEY, profile.toJson().toString()).apply()
    }

    fun loadProfile(context: Context): HearingProfile? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(PROFILE_KEY, null)
        return if (jsonString != null) {
            try {
                HearingProfile.fromJson(JSONObject(jsonString))
            } catch (e: Exception) {
                null
            }
        } else null
    }
}
