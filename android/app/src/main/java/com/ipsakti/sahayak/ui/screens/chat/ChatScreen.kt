package com.ipsakti.sahayak.ui.screens.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipsakti.sahayak.data.manager.LanguageManager
import com.ipsakti.sahayak.data.model.ChatMessage
import com.ipsakti.sahayak.data.model.SuggestedAction
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFeature: (targetScreen: String, payload: String?) -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = LanguageManager.getString("chat_title"),
                subtitle = LanguageManager.getString("chat_subtitle"),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Message conversation list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(uiState.messages) { message ->
                    MessageBubble(
                        message = message,
                        onActionClick = { action ->
                            onNavigateToFeature(action.targetScreen, action.payload)
                        }
                    )
                }

                if (uiState.isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(RoyalBlue800),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Gold600,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = RoyalBlue800
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AyurSakti is evaluating Indian IP & AYUSH provisions...",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick suggestion prompt chips
            if (uiState.suggestedPrompts.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.suggestedPrompts) { prompt ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CardBackground,
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.clickable {
                                viewModel.sendMessage(prompt)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = RoyalBlue800,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Navy900,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = CardBackground,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.onInputChanged(it) },
                        placeholder = {
                            Text(
                                text = LanguageManager.getString("chat_placeholder"),
                                fontSize = 13.sp,
                                color = Slate400
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue800,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = Slate50,
                            unfocusedContainerColor = Slate50
                        ),
                        singleLine = true,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.sendMessage() },
                        enabled = uiState.inputText.trim().isNotEmpty() && !uiState.isLoading,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (uiState.inputText.trim().isNotEmpty() && !uiState.isLoading) RoyalBlue800 else Slate300)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onActionClick: (SuggestedAction) -> Unit
) {
    val isUser = message.sender == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Navy900),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Gold600,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column {
                Card(
                    shape = RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 14.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUser) RoyalBlue800 else CardBackground
                    ),
                    border = if (isUser) null else BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isUser) Color.White else Navy900,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                // Interactive App Feature Recommendation Chips
                if (!isUser && message.actions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "RECOMMENDED APP ACTIONS:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        message.actions.forEach { action ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = RoyalBlue800.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, RoyalBlue800.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable { onActionClick(action) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = action.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = RoyalBlue800,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = RoyalBlue800,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate400,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
