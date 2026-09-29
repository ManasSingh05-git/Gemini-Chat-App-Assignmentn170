package com.N170_Manassingh.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.N170_Manassingh.data.GeminiRepository
import com.N170_Manassingh.data.local.AppTheme
import com.N170_Manassingh.data.local.ChatMessageEntity
import com.N170_Manassingh.data.local.UserPreferencesRepository
import com.N170_Manassingh.data.repository.ChatRepository
import com.N170_Manassingh.security.ApiKeyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val geminiRepository: GeminiRepository,
    private val chatRepository: ChatRepository,
    private val apiKeyManager: ApiKeyManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            apiKeyManager.initializeKey()
            val decryptedKey = apiKeyManager.getDecryptedApiKeyInMemory()
            geminiRepository.initialize(decryptedKey)
        }

        viewModelScope.launch {
            chatRepository.chatHistory.collectLatest { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.themeFlow.collectLatest { theme ->
                _uiState.update { it.copy(appTheme = theme) }
            }
        }
    }

    fun onPromptChange(value: String) {
        _uiState.update { it.copy(prompt = value, promptError = null) }
    }

    fun setSpeechRecognizing(isRecognizing: Boolean) {
        _uiState.update { it.copy(isRecognizingSpeech = isRecognizing) }
    }

    fun onSpeechResult(spokenText: String) {
        _uiState.update { 
            it.copy(
                prompt = (it.prompt + " " + spokenText).trim(), 
                promptError = null,
                isRecognizingSpeech = false
            ) 
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatRepository.clearHistory()
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.saveTheme(theme)
        }
    }

    fun onSend() {
        val prompt = _uiState.value.prompt.trim()
        if (prompt.isEmpty()) {
            _uiState.update { it.copy(promptError = PromptError.EMPTY) }
            return
        }
        
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null, promptError = null, prompt = "") }
        
        viewModelScope.launch {
            if (!apiKeyManager.hasApiKey()) {
                _uiState.update { it.copy(errorMessage = MISSING_API_KEY_MESSAGE, isLoading = false) }
                return@launch
            }

            // Save user message immediately
            chatRepository.saveMessage(
                ChatMessageEntity(
                    prompt = prompt,
                    response = "",
                    isUser = true,
                    timestamp = System.currentTimeMillis()
                )
            )

            geminiRepository.generateText(prompt).fold(
                onSuccess = { text ->
                    chatRepository.saveMessage(
                        ChatMessageEntity(
                            prompt = prompt,
                            response = text,
                            isUser = false,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Something went wrong"
                        )
                    }
                    chatRepository.saveMessage(
                        ChatMessageEntity(
                            prompt = prompt,
                            response = error.message ?: "Error",
                            isUser = false,
                            isError = true,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            )
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        const val MISSING_API_KEY_MESSAGE =
            "GEMINI_API_KEY is missing. Add it to local.properties and rebuild."

        fun factory(
            geminiRepository: GeminiRepository, 
            chatRepository: ChatRepository,
            apiKeyManager: ApiKeyManager,
            userPreferencesRepository: UserPreferencesRepository
        ) = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ChatViewModel(geminiRepository, chatRepository, apiKeyManager, userPreferencesRepository) as T
            }
    }
}
