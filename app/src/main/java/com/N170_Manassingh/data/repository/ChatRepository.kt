package com.N170_Manassingh.data.repository

import com.N170_Manassingh.data.local.ChatDao
import com.N170_Manassingh.data.local.ChatMessageEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChatRepository(
    private val chatDao: ChatDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val chatHistory: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    suspend fun saveMessage(message: ChatMessageEntity) {
        withContext(ioDispatcher) {
            chatDao.insertMessage(message)
        }
    }

    suspend fun clearHistory() {
        withContext(ioDispatcher) {
            chatDao.clearHistory()
        }
    }
}
