package com.N170_Manassingh.ui.chat

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.N170_Manassingh.R
import com.N170_Manassingh.data.local.AppTheme
import com.N170_Manassingh.data.local.ChatMessageEntity
import com.N170_Manassingh.ui.text.toBoldAnnotatedString
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoute(viewModel: ChatViewModel, windowSizeClass: WindowSizeClass) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    ChatScreen(
        state = state,
        windowSizeClass = windowSizeClass,
        onPromptChange = viewModel::onPromptChange,
        onSend = viewModel::onSend,
        onClearHistory = viewModel::clearHistory,
        onSpeechRecognized = viewModel::onSpeechResult,
        onClearError = viewModel::clearErrorMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: ChatUiState,
    windowSizeClass: WindowSizeClass,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
    onClearHistory: () -> Unit,
    onSpeechRecognized: (String) -> Unit,
    onClearError: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { 
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val horizontalPadding = if (isExpanded) 64.dp else 16.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "J.A.R.V.I.S. INTERFACE",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = onClearHistory) {
                        Icon(
                            imageVector = Icons.Default.Delete, 
                            contentDescription = "Clear History",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = Modifier.fillMaxSize().imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Subtle tech grid background could be drawn here
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = 16.dp)
            ) {
                ConversationArea(
                    messages = state.messages,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
                PromptBar(
                    prompt = state.prompt,
                    promptError = state.promptError,
                    enabled = !state.isLoading,
                    onPromptChange = onPromptChange,
                    onSend = onSend,
                    onSpeechRecognized = onSpeechRecognized
                )
            }
            if (state.isLoading) {
                ArcReactorLoader(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun ArcReactorLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactor")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ArcRotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ArcPulse"
    )

    val neonCyan = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(100.dp)
            .rotate(rotation)
            .drawBehind {
                drawCircle(
                    color = neonCyan.copy(alpha = 0.2f * pulse),
                    radius = size.minDimension / 2
                )
                drawCircle(
                    color = neonCyan,
                    radius = size.minDimension / 2.5f,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )
                // Inner core
                drawCircle(
                    color = neonCyan.copy(alpha = 0.8f * pulse),
                    radius = size.minDimension / 4f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Memory,
            contentDescription = "Processing",
            tint = MaterialTheme.colorScheme.background,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ConversationArea(messages: List<ChatMessageEntity>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            HudMessagePanel(message)
        }
        
        if (messages.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "AWAITING COMMAND...",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HudMessagePanel(message: ChatMessageEntity) {
    val isUser = message.isUser
    val borderColor = if (message.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val backgroundColor = if (isUser) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val df = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeString = df.format(Date(message.timestamp))
    
    // Sharp HUD cuts
    val hudShape = CutCornerShape(
        topStart = if (isUser) 16.dp else 0.dp,
        topEnd = if (isUser) 0.dp else 16.dp,
        bottomStart = 4.dp,
        bottomEnd = 4.dp
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp).padding(end = 8.dp, top = 4.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(hudShape)
                    .background(backgroundColor)
                    .border(1.dp, borderColor.copy(alpha = 0.5f), hudShape)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = if (isUser) "[USER_INPUT] : " else "[SYSTEM_RESPONSE] : ",
                        color = borderColor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = if (isUser) androidx.compose.ui.text.AnnotatedString(message.prompt) else message.response.toBoldAnnotatedString(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "TS: $timeString",
                        color = borderColor.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                    )
                }
            }
            
            if (isUser) {
                Spacer(modifier = Modifier.width(28.dp)) // padding placeholder to match icon side
            }
        }
    }
}

@Composable
private fun PromptBar(
    prompt: String,
    promptError: PromptError?,
    enabled: Boolean,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
    onSpeechRecognized: (String) -> Unit
) {
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull()
            if (!spokenText.isNullOrEmpty()) {
                onSpeechRecognized(spokenText)
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                }
                try {
                    speechRecognizerLauncher.launch(intent)
                } catch (e: Exception) {
                    // Handle case where speech recognition is not available
                }
            },
            modifier = Modifier
                .padding(end = 8.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CutCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice Input",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            label = { 
                Text(
                    "> ENTER_COMMAND_",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                ) 
            },
            minLines = 1,
            maxLines = 4,
            enabled = enabled,
            isError = promptError != null,
            supportingText = promptError?.let {
                { Text(stringResource(R.string.field_cannot_be_empty), fontFamily = FontFamily.Monospace) }
            },
            shape = CutCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
        
        IconButton(
            onClick = onSend, 
            enabled = enabled,
            modifier = Modifier
                .size(56.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary, CutCornerShape(8.dp))
                .background(if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.send),
                tint = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}
