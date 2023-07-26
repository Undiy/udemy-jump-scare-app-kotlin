package com.example.countershock

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AudioStorer(val context: Context) {
    val preferences: SharedPreferences
    val editor: SharedPreferences.Editor

    init {
        preferences = context.getSharedPreferences(ShockUtils.SHOCK_SHARED_PREFS, Context.MODE_PRIVATE)
        editor = preferences.edit()
    }

    fun storeAudios(audios: List<AudioModel>) {
        val gson = Gson()
        editor.putString(context.getString(R.string.key_stored_audios), gson.toJson(audios))
        editor.commit()
    }

    fun addAudio(audio: AudioModel) {
        val audios = getStoredAudios()
        storeAudios(audios + audio)
    }

    fun getStoredAudios(): List<AudioModel> {
        val audiosAsJson = preferences.getString(context.getString(R.string.key_stored_audios), null)
        if ((audiosAsJson ?: "").isEmpty()) {
            return listOf()
        }
        val gson = Gson()
        val type = object : TypeToken<List<AudioModel>>(){}.type
        return gson.fromJson(audiosAsJson, type)
    }

    fun getAllAudios(): List<AudioModel> {
        val assetAudios = mutableListOf(
            AudioModel(0,"behind_you", "What's that behind you?",true),
            AudioModel(1, "see_you", "I see what you're doing", true),
            AudioModel(2, "watching_you", "I'm watching You", true),
            AudioModel(3, "scream1", "Scream 1", true),
            AudioModel(4, "scream2", "Scream 2", true)
        )

        assetAudios.addAll(getStoredAudios())
        return assetAudios
    }

    fun getSelectedAudio(): AudioModel {
        val audios = getAllAudios()

        val defaultAudio = audios.first()

        val audioId = preferences.getInt(context.getString(R.string.key_audio_id), 0)

        return audios.find { it.id == audioId } ?: defaultAudio
    }
 }