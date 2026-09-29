package com.N170_Manassingh.data

/** Abstraction over the Gemini text generation call so the ViewModel can be unit tested. */
interface GeminiRepository {
    suspend fun generateText(prompt: String): Result<String>
    
    /**
     * Initializes the generative model using the decrypted API key
     * and starts the multi-turn chat session.
     */
    suspend fun initialize(apiKey: String)
}
