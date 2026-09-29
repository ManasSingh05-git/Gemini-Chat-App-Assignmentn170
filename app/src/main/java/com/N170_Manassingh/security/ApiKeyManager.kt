package com.N170_Manassingh.security

import com.N170_Manassingh.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.first

open class ApiKeyManager(
    private val keystoreManager: KeystoreManager,
    private val preferencesRepository: UserPreferencesRepository,
    private val buildConfigApiKey: String,
) {
    suspend fun initializeKey() {
        val storedEncryptedKey = preferencesRepository.encryptedApiKeyFlow.first()
        if (storedEncryptedKey.isBlank() && buildConfigApiKey.isNotBlank()) {
            val encryptedKey = keystoreManager.encrypt(buildConfigApiKey)
            if (encryptedKey.isNotBlank()) {
                preferencesRepository.saveEncryptedApiKey(encryptedKey)
            }
        }
    }

    /**
     * Decrypts the API key in memory only when required to instantiate the Gemini model.
     * The decrypted string is held temporarily in memory and never persisted.
     */
    suspend fun getDecryptedApiKeyInMemory(): String {
        val storedEncryptedKey = preferencesRepository.encryptedApiKeyFlow.first()
        if (storedEncryptedKey.isNotBlank()) {
            val decrypted = keystoreManager.decrypt(storedEncryptedKey)
            if (decrypted.isNotBlank()) return decrypted
        }
        // Fallback to BuildConfig key if datastore key is empty/uninitialized
        return buildConfigApiKey
    }

    open suspend fun hasApiKey(): Boolean {
        return getDecryptedApiKeyInMemory().isNotBlank()
    }
}
