package com.N170_Manassingh

import android.content.Context
import com.N170_Manassingh.data.GeminiRepository
import com.N170_Manassingh.data.local.AppTheme
import com.N170_Manassingh.data.local.ChatDao
import com.N170_Manassingh.data.local.ChatMessageEntity
import com.N170_Manassingh.data.local.UserPreferencesRepository
import com.N170_Manassingh.data.repository.ChatRepository
import com.N170_Manassingh.security.ApiKeyManager
import com.N170_Manassingh.security.KeystoreManager
import com.N170_Manassingh.ui.chat.ChatViewModel
import com.N170_Manassingh.ui.chat.PromptError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var fakeGeminiRepository: FakeGeminiRepository
    private lateinit var fakeChatRepository: ChatRepository
    private lateinit var fakeApiKeyManager: ApiKeyManager
    private lateinit var fakeUserPreferencesRepository: UserPreferencesRepository
    
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        fakeGeminiRepository = FakeGeminiRepository()
        fakeChatRepository = ChatRepository(FakeChatDao())
        
        val fakeContext = FakeContext()
        fakeUserPreferencesRepository = FakeUserPreferencesRepository(fakeContext)
        fakeApiKeyManager = FakeApiKeyManager(KeystoreManager(), fakeUserPreferencesRepository)

        viewModel = ChatViewModel(
            geminiRepository = fakeGeminiRepository,
            chatRepository = fakeChatRepository,
            apiKeyManager = fakeApiKeyManager,
            userPreferencesRepository = fakeUserPreferencesRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.prompt)
        assertFalse(state.isLoading)
        assertTrue(state.messages.isEmpty())
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `onPromptChange updates prompt in state`() = runTest {
        viewModel.onPromptChange("Hello Gemini")
        assertEquals("Hello Gemini", viewModel.uiState.value.prompt)
    }

    @Test
    fun `onSend with empty prompt sets PromptError`() = runTest {
        viewModel.onPromptChange("   ")
        viewModel.onSend()
        assertEquals(PromptError.EMPTY, viewModel.uiState.value.promptError)
    }

    @Test
    fun `onSend success updates loading state and clears prompt`() = runTest {
        (fakeApiKeyManager as FakeApiKeyManager).hasKey = true
        fakeGeminiRepository.response = Result.success("Hello from Fake Gemini")
        
        viewModel.onPromptChange("Hi")
        viewModel.onSend()
        
        assertTrue(viewModel.uiState.value.isLoading)
        
        advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("", viewModel.uiState.value.prompt) // Prompt is cleared on send
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onSend missing api key shows error message`() = runTest {
        (fakeApiKeyManager as FakeApiKeyManager).hasKey = false
        
        viewModel.onPromptChange("Hi")
        viewModel.onSend()
        
        advanceUntilIdle()
        
        assertEquals(ChatViewModel.MISSING_API_KEY_MESSAGE, viewModel.uiState.value.errorMessage)
    }
}

class FakeGeminiRepository : GeminiRepository {
    var response: Result<String> = Result.success("Default response")
    
    override suspend fun generateText(prompt: String): Result<String> {
        return response
    }

    override suspend fun initialize(apiKey: String) {
        // No-op for fake
    }
}

class FakeChatDao : ChatDao {
    private val messages = mutableListOf<ChatMessageEntity>()
    private val flow = MutableStateFlow(messages.toList())
    
    override fun getAllMessages(): Flow<List<ChatMessageEntity>> = flow

    override suspend fun insertMessage(message: ChatMessageEntity): Long {
        messages.add(message)
        flow.value = messages.toList()
        return 1L
    }

    override suspend fun clearHistory() {
        messages.clear()
        flow.value = emptyList()
    }

    override suspend fun deleteMessage(id: Long) {
        messages.removeIf { it.id == id }
        flow.value = messages.toList()
    }
}

open class FakeContext : android.content.ContextWrapper(null) {
    override fun getApplicationContext(): Context {
        return this
    }
}

class FakeUserPreferencesRepository(context: Context) : UserPreferencesRepository(context) {
    private val theme = MutableStateFlow(AppTheme.SYSTEM)
    override val themeFlow = theme
}

class FakeApiKeyManager(
    keystoreManager: KeystoreManager,
    repo: UserPreferencesRepository
) : ApiKeyManager(keystoreManager, repo, "fake") {
    var hasKey = true
    
    override suspend fun hasApiKey(): Boolean {
        return hasKey
    }
}
