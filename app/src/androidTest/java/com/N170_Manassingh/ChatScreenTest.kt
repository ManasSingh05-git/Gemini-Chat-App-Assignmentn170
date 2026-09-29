package com.N170_Manassingh

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.N170_Manassingh.data.local.AppTheme
import com.N170_Manassingh.data.local.ChatMessageEntity
import com.N170_Manassingh.ui.chat.ChatScreen
import com.N170_Manassingh.ui.chat.ChatUiState
import com.N170_Manassingh.ui.theme.GeminiApiComposeStarterTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val testWindowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp))

    @Test
    fun chatScreen_displaysInitialUI() {
        composeTestRule.setContent {
            GeminiApiComposeStarterTheme {
                ChatScreen(
                    state = ChatUiState(),
                    windowSizeClass = testWindowSizeClass,
                    onPromptChange = {},
                    onSend = {},
                    onClearHistory = {},
                    onThemeChange = {},
                    onSpeechRecognized = {},
                    onClearError = {}
                )
            }
        }

        // Verify prompt input field is displayed
        composeTestRule.onNodeWithText("Enter your prompt here").assertExists()
        
        // Verify send button is displayed
        composeTestRule.onNodeWithContentDescription("Send").assertExists()
        
        // Verify placeholder text is displayed when no messages
        composeTestRule.onNodeWithText("Waiting for you to ask something...").assertExists()
    }

    @Test
    fun chatScreen_displaysMessages() {
        val messages = listOf(
            ChatMessageEntity(id = 1, prompt = "Hello", response = "", isUser = true),
            ChatMessageEntity(id = 2, prompt = "Hello", response = "Hi there!", isUser = false)
        )
        
        composeTestRule.setContent {
            GeminiApiComposeStarterTheme {
                ChatScreen(
                    state = ChatUiState(messages = messages),
                    windowSizeClass = testWindowSizeClass,
                    onPromptChange = {},
                    onSend = {},
                    onClearHistory = {},
                    onThemeChange = {},
                    onSpeechRecognized = {},
                    onClearError = {}
                )
            }
        }

        // Verify both user prompt and assistant response are displayed
        composeTestRule.onNodeWithText("Hello").assertExists()
        composeTestRule.onNodeWithText("Hi there!").assertExists()
    }
    
    @Test
    fun chatScreen_typingInPromptField_callsOnPromptChange() {
        var typedText = ""
        
        composeTestRule.setContent {
            GeminiApiComposeStarterTheme {
                ChatScreen(
                    state = ChatUiState(),
                    windowSizeClass = testWindowSizeClass,
                    onPromptChange = { typedText = it },
                    onSend = {},
                    onClearHistory = {},
                    onThemeChange = {},
                    onSpeechRecognized = {},
                    onClearError = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Enter your prompt here")
            .performTextInput("Test message")
            
        assertTrue(typedText == "Test message")
    }
}
