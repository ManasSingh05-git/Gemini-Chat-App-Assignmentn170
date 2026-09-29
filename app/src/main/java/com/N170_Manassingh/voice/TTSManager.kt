package com.N170_Manassingh.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TTSManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSManager", "The Language specified is not supported!")
            } else {
                isInitialized = true
            }
        } else {
            Log.e("TTSManager", "Initilization Failed!")
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            // Strip out markdown bold syntax (**) before speaking
            val cleanText = text.replace("**", "")
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
        }
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}