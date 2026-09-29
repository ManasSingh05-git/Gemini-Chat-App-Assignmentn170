package com.N170_Manassingh.ui.chat

import com.N170_Manassingh.data.local.ChatMessageEntity
import com.N170_Manassingh.data.local.AppTheme

/** Immutable UI state for the chat flow. */
data class ChatUiState(
    val prompt: String = "",
    val isLoading: Boolean = false,
    val isRecognizingSpeech: Boolean = false,
    val promptError: PromptError? = null,
    val errorMessage: String? = null,
    val messages: List<ChatMessageEntity> = emptyList(),
    val appTheme: AppTheme = AppTheme.SYSTEM,
)

enum class PromptError { EMPTY }
