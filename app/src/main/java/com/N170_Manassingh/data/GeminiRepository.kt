package com.N170_Manassingh.data

import com.google.ai.client.generativeai.GenerativeModel

/** Abstraction over the Gemini text generation call so the ViewModel can be unit tested. */
interface GeminiRepository {
    suspend fun generateText(prompt: String): Result<String>
    
    /**
     * Initializes the generative model using the decrypted API key.
     * This avoids keeping the plaintext key in memory for longer than necessary.
     */
    suspend fun initialize(apiKey: String)
}
