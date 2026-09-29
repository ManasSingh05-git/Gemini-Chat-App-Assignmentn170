package com.N170_Manassingh.data

import android.util.Log
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CancellationException

private const val TAG = "GeminiRepository"
private const val DEFAULT_MODEL = "gemini-1.5-flash-latest"

class GeminiRepositoryImpl(
    private val modelName: String = DEFAULT_MODEL,
) : GeminiRepository {

    private var model: GenerativeModel? = null
    private var chatSession: Chat? = null

    override suspend fun initialize(apiKey: String) {
        if (apiKey.isNotBlank()) {
            model = GenerativeModel(modelName = modelName, apiKey = apiKey)
            chatSession = model?.startChat()
        }
    }

    override suspend fun generateText(prompt: String): Result<String> = try {
        val chat = chatSession ?: throw IllegalStateException("Gemini chat session not initialized with API key")
        val response = chat.sendMessage(prompt)
        val text = response.text?.takeIf { it.isNotBlank() }
        if (text != null) {
            Result.success(text)
        } else {
            Result.failure(IllegalStateException("Empty response from Gemini"))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "sendMessage failed", e)
        Result.failure(e)
    }
}
