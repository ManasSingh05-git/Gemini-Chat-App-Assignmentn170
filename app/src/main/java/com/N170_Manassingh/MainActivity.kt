package com.N170_Manassingh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.N170_Manassingh.data.GeminiRepositoryImpl
import com.N170_Manassingh.data.local.AppTheme
import com.N170_Manassingh.data.local.ChatDatabase
import com.N170_Manassingh.data.local.UserPreferencesRepository
import com.N170_Manassingh.data.repository.ChatRepository
import com.N170_Manassingh.security.ApiKeyManager
import com.N170_Manassingh.security.KeystoreManager
import com.N170_Manassingh.ui.chat.ChatRoute
import com.N170_Manassingh.ui.chat.ChatViewModel
import com.N170_Manassingh.ui.theme.GeminiApiComposeStarterTheme

class MainActivity : ComponentActivity() {

    private val userPreferencesRepository by lazy { UserPreferencesRepository(applicationContext) }
    private val keystoreManager by lazy { KeystoreManager() }
    private val apiKeyManager by lazy {
        ApiKeyManager(
            keystoreManager = keystoreManager,
            preferencesRepository = userPreferencesRepository,
            buildConfigApiKey = BuildConfig.GEMINI_API_KEY
        )
    }
    private val chatDatabase by lazy { ChatDatabase.getDatabase(applicationContext) }
    private val chatRepository by lazy { ChatRepository(chatDatabase.chatDao()) }
    private val geminiRepository by lazy { GeminiRepositoryImpl() }

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModel.factory(
            geminiRepository = geminiRepository,
            chatRepository = chatRepository,
            apiKeyManager = apiKeyManager,
            userPreferencesRepository = userPreferencesRepository
        )
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val windowSizeClass = calculateWindowSizeClass(this)
            
            val isDarkTheme = when (uiState.appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            GeminiApiComposeStarterTheme(darkTheme = isDarkTheme) {
                ChatRoute(viewModel = viewModel, windowSizeClass = windowSizeClass)
            }
        }
    }
}
