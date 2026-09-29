package com.N170_Manassingh.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val prompt: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUser: Boolean = false,
    val isError: Boolean = false,
)
