package com.example.ui

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DefaultQuizzes
import com.example.data.UserProfile
import com.example.ui.theme.*

// --- Navigation Destinations ---
enum class Screen {
    LOBBY, QUIZ, RESULTS, ARCHIVE_STATS
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizMainApp(viewModel: QuizViewModel) {
    val currentScreenState = remember { mutableStateFlowOf(Screen.LOBBY) }
    val currentScreen by currentScreenState.collectAsState()
    
    val profile by viewModel.currentProfile.collectAsState()
    val isDarkTheme by viewModel.darkThemeOverride.collectAsState()
    val notificationText by viewModel.simulatedNotification.collectAsState()

    var selectedCategory by remember { mutableStateOf("Science") }
    var selectedDifficulty by remember { mutableStateOf("Easy") }
    var selectedQuizType by remember { mutableStateOf(QuizType.SOLO) }
    var customCategoryText by remember { mutableStateOf("") }

    MyApplicationTheme(darkTheme = isDarkTheme) {
        val backgroundBrush = if (isDarkTheme) {
            Brush.verticalGradient(listOf(MidnightDark, Color(0xFF0F1528)))
        } else {
            Brush.verticalGradient(listOf(CyberLight, Color(0xFFE2EAF5)))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .safeDrawingPadding()
        ) {
            // Main Navigation Controller Routing
            when (currentScreen) {
                Screen.LOBBY -> {
                    LobbyScreen(
                        viewModel = viewModel,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it },
                        customCategoryText = customCategoryText,
                        onCustomCategoryChange = { customCategoryText = it },
                        selectedDifficulty = selectedDifficulty,
                        onDifficultySelect = { selectedDifficulty = it },
                        selectedQuizType = selectedQuizType,
                        onQuizTypeSelect = { selectedQuizType = it },
                        onStartQuiz = {
                            val appliedCategory = if (customCategoryText.isNotBlank()) {
                                customCategoryText
                            } else {
                                selectedCategory
                            }
                            viewModel.startQuiz(appliedCategory, selectedDifficulty, selectedQuizType)
                            currentScreenState.value = Screen.QUIZ
                        },
                        onNavigateToArchive = {
                            currentScreenState.value = Screen.ARCHIVE_STATS
                        }
                    )
                }
                Screen.QUIZ -> {
                    ActiveQuizScreen(
                        viewModel = viewModel,
                        onQuizFinished = {
                            currentScreenState.value = Screen.RESULTS
                        },
                        onQuit = {
                            currentScreenState.value = Screen.LOBBY
                        }
                    )
                }
                Screen.RESULTS -> {
                    QuizResultsScreen(
                        viewModel = viewModel,
                        selectedCategory = if (customCategoryText.isNotBlank()) customCategoryText else selectedCategory,
                        selectedDifficulty = selectedDifficulty,
                        onNavigateHome = {
                            customCategoryText = "" // reset
                            currentScreenState.value = Screen.LOBBY
                        },
                        onNavigateToStats = {
                            currentScreenState.value = Screen.ARCHIVE_STATS
                        }
                    )
                }
                Screen.ARCHIVE_STATS -> {
                    StatsAndArchiveScreen(
                        viewModel = viewModel,
                        onBack = {
                            currentScreenState.value = Screen.LOBBY
                        }
                    )
                }
            }

            // Simulated Daily Engagement/Badge Push Notification floating alert
            notificationText?.let { text ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkTheme) Color(0xEA111726) else Color(0xEAFFFFFF))
                        .border(1.dp, CyberCyan, RoundedCornerShape(16.dp))
                        .clickable { viewModel.clearNotification() }
                        .animateContentSize()
                        .padding(16.dp)
                        .align(Alignment.TopCenter)
                        .testTag("notification_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alert Notify",
                                tint = CyberPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = text,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.0f)
                        )
                        IconButton(onClick = { viewModel.clearNotification() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss Notification",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Welcome Lobby Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: QuizViewModel,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    customCategoryText: String,
    onCustomCategoryChange: (String) -> Unit,
    selectedDifficulty: String,
    onDifficultySelect: (String) -> Unit,
    selectedQuizType: QuizType,
    onQuizTypeSelect: (QuizType) -> Unit,
    onStartQuiz: () -> Unit,
    onNavigateToArchive: () -> Unit
) {
    val activeProfile by viewModel.currentProfile.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val isDarkTheme by viewModel.darkThemeOverride.collectAsState()

    var showProfilePicker by remember { mutableStateOf(false) }
    var showCreateProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "App Logo",
                            tint = CyberCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CyberTrivia",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.NightsStay,
                            contentDescription = "Theme Switcher",
                            tint = CyberCyan
                        )
                    }
                    IconButton(
                        onClick = { viewModel.triggerSimulatedNotification("🔔 Daily Streak Reminder: Tap to enter the Cyber Arena & retain your ${activeProfile?.streak ?: 1}x streak multiplier!") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Fake Push Notification Trigger",
                            tint = CyberPurple
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Active Profile Banner Card
            activeProfile?.let { user ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isDarkTheme) BorderMuted else BorderActive,
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFD0BCFF), Color(0xFF381E72))
                                        ),
                                        CircleShape
                                    )
                                    .border(2.dp, Color(0xFFD0BCFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = user.avatar, fontSize = 28.sp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "LEVEL ${user.level}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MutedLabel,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Hi, ${user.username}!",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF313033), RoundedCornerShape(50))
                                        .border(1.dp, Color(0xFF49454F), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = "${user.xp}", 
                                            color = Color(0xFFD0BCFF), 
                                            fontWeight = FontWeight.Black, 
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp
                                        )
                                        Text(text = "XP", color = Color(0xFFCAC4D0), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF49E3B4), CircleShape)
                                    )
                                    Text(
                                        text = "${user.streak} Day Streak",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF49E3B4),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progression: ${user.xp % 1000}/1000 XP",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                            
                            Text(
                                text = "⇄ Switch Profile",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { showProfilePicker = true }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        LinearProgressIndicator(
                            progress = { (user.xp % 1000).toFloat() / 1000f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = CyberCyan,
                            trackColor = if (isDarkTheme) Color(0xFF2D2F33) else Color(0xFFDEE5EE)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Beautiful Hero Daily Challenge Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD0BCFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_challenge_card")
                        .clickable {
                            onCategorySelect("Cosmic History")
                            onCustomCategoryChange("")
                            onDifficultySelect("Medium")
                            onStartQuiz()
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawCircle(
                                    color = Color(0xFF381E72).copy(alpha = 0.15f),
                                    radius = 120.dp.toPx(),
                                    center = Offset(size.width - 20.dp.toPx(), -20.dp.toPx())
                                )
                            }
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF381E72), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "DAILY CHALLENGE",
                                        color = Color(0xFFD0BCFF),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text(
                                text = "Cosmic\nHistory",
                                color = Color(0xFF1D1B20),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 34.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    onCategorySelect("Cosmic History")
                                    onCustomCategoryChange("")
                                    onDifficultySelect("Medium")
                                    onStartQuiz()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381E72)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "START QUIZ",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Nave button to stats and grid locker
            TextButton(
                onClick = onNavigateToArchive,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_dashboard_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = "Unlocked Badges Grid", tint = CyberCyan)
                    Text(text = "View Performance Stats & Achievements Tracker", fontWeight = FontWeight.ExtraBold, color = CyberCyan)
                    Icon(imageVector = Icons.Default.ShowChart, contentDescription = "View", tint = CyberCyan)
                }
            }

            // Category Selection Block
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SELECT CATEGORY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DefaultQuizzes.categories) { category ->
                        val isSelected = selectedCategory == category && customCategoryText.isBlank()
                        val color = if (isSelected) CyberCyan else (if (isDarkTheme) Color(0xFF2D2F33) else SurfaceLight)
                        val textColor = if (isSelected) Color(0xFF1D1B20) else MaterialTheme.colorScheme.onBackground
                        val borderMod = if (isSelected) Modifier.border(1.dp, CyberCyan, RoundedCornerShape(12.dp)) else Modifier.border(1.dp, Color(0xFF44474F), RoundedCornerShape(12.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(color)
                                .clickable {
                                    onCategorySelect(category)
                                    onCustomCategoryChange("") // clear custom text field
                                }
                                .then(borderMod)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("category_pill_$category")
                        ) {
                            Text(
                                text = category,
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Categories text field
                OutlinedTextField(
                    value = customCategoryText,
                    onValueChange = onCustomCategoryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_category_input"),
                    label = { Text("Generate Custom Category via AI 🤖") },
                    placeholder = { Text("e.g. Kotlin Coroutines, Nuclear Physics...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        focusedLabelColor = CyberCyan
                    )
                )
                if (customCategoryText.isNotBlank()) {
                    Text(
                        text = "🚀 Gemini AI will model 5 bespoke questions regarding '$customCategoryText'",
                        color = CyberCyan,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }

            // Difficulty levels Block
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SELECT DIFFICULTY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                        val isSelected = selectedDifficulty == diff
                        val color = if (isSelected) CyberCyan else (if (isDarkTheme) Color(0xFF2D2F33) else SurfaceLight)
                        val textColor = if (isSelected) Color(0xFF1D1B20) else MaterialTheme.colorScheme.onBackground
                        val borderMod = if (isSelected) Modifier.border(1.dp, CyberCyan, RoundedCornerShape(12.dp)) else Modifier.border(1.dp, Color(0xFF44474F), RoundedCornerShape(12.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(color)
                                .clickable { onDifficultySelect(diff) }
                                .then(borderMod)
                                .padding(vertical = 12.dp)
                                .testTag("difficulty_button_$diff"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = diff,
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Challenge Game Mode selection
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SELECT ARENA GAME MODE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Solo mode card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onQuizTypeSelect(QuizType.SOLO) }
                            .border(
                                width = if (selectedQuizType == QuizType.SOLO) 2.dp else 0.dp,
                                brush = if (selectedQuizType == QuizType.SOLO) Brush.linearGradient(listOf(CyberCyan, CyberPurple)) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Solo Quest",
                                tint = if (selectedQuizType == QuizType.SOLO) CyberCyan else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Solo Sprint", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Self pace trivia run", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                    }

                    // Simulated live Arena multiplayer mode card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onQuizTypeSelect(QuizType.ARENA) }
                            .border(
                                width = if (selectedQuizType == QuizType.ARENA) 2.dp else 0.dp,
                                brush = if (selectedQuizType == QuizType.ARENA) Brush.linearGradient(listOf(CyberCyan, CyberPurple)) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Cooperative Arena",
                                tint = if (selectedQuizType == QuizType.ARENA) CyberPurple else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Real-Time Arena", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Live battle v/s 3 bots!", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Run Quiz!
            Button(
                onClick = onStartQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_arena_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start Quiz", tint = Color.White)
                    Text(
                        text = if (customCategoryText.isNotBlank()) "GENERATE AI MATCH" else "ENTER ARENA",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }

    // Interactive Profile Picker/Switcher Dialog
    if (showProfilePicker) {
        Dialog(onDismissRequest = { showProfilePicker = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Profiles Panel",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        items(allProfiles) { profile ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (profile.id == activeProfile?.id) CyberCyan.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable {
                                        viewModel.selectProfile(profile)
                                        showProfilePicker = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White.copy(alpha = 0.05f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = profile.avatar, fontSize = 24.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = profile.username, fontWeight = FontWeight.Bold)
                                    Text(text = "Level ${profile.level} • ${profile.xp} XP", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                }
                                if (allProfiles.size > 1) {
                                    IconButton(onClick = { viewModel.deleteProfile(profile) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Trash", tint = CyberRed, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showCreateProfileDialog = true
                            showProfilePicker = false
                        }) {
                            Text("Create Profile", color = CyberCyan, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { showProfilePicker = false }) {
                            Text("Close", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }

    // New Profile creation Dialog with Avatar choice list
    if (showCreateProfileDialog) {
        var newUsername by remember { mutableStateOf("") }
        var chosenAvatar by remember { mutableStateOf("🤖") }
        val avatars = listOf("🤖", "⚡", "🛸", "👽", "🚀", "💻", "🌐", "🐱", "🎧", "🐯")

        Dialog(onDismissRequest = { showCreateProfileDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Establish New Cyber Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("Cyber Alias Name") },
                        placeholder = { Text("e.g. LambdaCoder") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Choose Cyber Badge Emblem:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(avatars) { avatar ->
                            val isSelected = chosenAvatar == avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(1.dp, if (isSelected) CyberCyan else Color.Transparent, CircleShape)
                                    .clickable { chosenAvatar = avatar },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = avatar, fontSize = 24.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateProfileDialog = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                        Button(
                            onClick = {
                                if (newUsername.isNotBlank()) {
                                    viewModel.createProfile(newUsername, chosenAvatar)
                                    showCreateProfileDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Text("Create Profile", color = MidnightDark)
                        }
                    }
                }
            }
        }
    }
}

// --- Active Quiz Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveQuizScreen(
    viewModel: QuizViewModel,
    onQuizFinished: () -> Unit,
    onQuit: () -> Unit
) {
    val questions by viewModel.activeQuestions.collectAsState()
    val isGenerating by viewModel.isGenerationLoading.collectAsState()
    
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val timerSeconds by viewModel.quizTimer.collectAsState()
    val quizScore by viewModel.quizScore.collectAsState()
    val arenaPlayers by viewModel.arenaOpponents.collectAsState()
    val quizType by viewModel.quizType.collectAsState()
    val isDarkTheme by viewModel.darkThemeOverride.collectAsState()

    var selectedAnswerOption by remember { mutableStateOf<String?>(null) }
    var answerSubmittedLocked by remember { mutableStateOf(false) }

    // Auto trigger completion on indexing out of bounds
    LaunchedEffect(currentIndex, questions) {
        if (questions.isNotEmpty() && currentIndex >= questions.size) {
            onQuizFinished()
        }
    }

    if (isGenerating) {
        // High fidelity loading state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(64.dp))
                Text(
                    text = "GENERATING CHALLENGE...",
                    fontFamily = FontFamily.Monospace,
                    color = CyberCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Aligning neural nodes via Gemini AI",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    } else if (questions.isNotEmpty() && currentIndex < questions.size) {
        val currentQuestion = questions[currentIndex]

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Text(
                            text = "QUEST INDEX: ${currentIndex + 1}/${questions.size}",
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberCyan
                        )
                    },
                    actions = {
                        IconButton(onClick = onQuit) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Exit Quiz", tint = CyberRed)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time & Live Score Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCyan.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = "Timer clock", tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Text(
                                text = "⏱ ${timerSeconds}s",
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberPurple.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = "Active Score", tint = CyberPurple, modifier = Modifier.size(16.dp))
                            Text(
                                text = "$quizScore PTS",
                                color = CyberPurple,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Question Box Canvas Display Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = currentQuestion.question,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Live Arena standins progress bars (Satisfies "real-time leaderboards")
                if (quizType == QuizType.ARENA && arenaPlayers.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0x38111726) else Color(0x42E2EAF5))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "LIVE MULTIPLAYER CHALLENGERS",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyberPurple
                            )

                            // Show YOUR bar first
                            val yourPercent = (currentIndex).toFloat() / 5f
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "YOU 😎", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
                                LinearProgressIndicator(
                                    progress = { yourPercent },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = CyberCyan
                                )
                                Text(text = "$quizScore pts", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            // Show bots bars
                            arenaPlayers.forEach { player ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "${player.name} ${player.avatar}", fontSize = 11.sp, modifier = Modifier.width(70.dp))
                                    LinearProgressIndicator(
                                        progress = { player.currentQuestionIndex.toFloat() / 5f },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .clip(CircleShape),
                                        color = CyberPurple
                                    )
                                    Text(text = "${player.score} pts", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }

                // Answer Options Block Layout
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currentQuestion.options.forEachIndexed { optIndex, option ->
                        val isSelected = selectedAnswerOption == option
                        val isCorrect = option == currentQuestion.correctAnswer
                        
                        val containerColor = when {
                            answerSubmittedLocked && isSelected && isCorrect -> CyberGreen.copy(alpha = 0.2f)
                            answerSubmittedLocked && isSelected && !isCorrect -> CyberRed.copy(alpha = 0.2f)
                            answerSubmittedLocked && isCorrect -> CyberGreen.copy(alpha = 0.15f)
                            isSelected -> CyberCyan.copy(alpha = 0.15f)
                            else -> if (isDarkTheme) SurfaceDark else SurfaceLight
                        }

                        val borderColor = when {
                            answerSubmittedLocked && isSelected && isCorrect -> CyberGreen
                            answerSubmittedLocked && isSelected && !isCorrect -> CyberRed
                            answerSubmittedLocked && isCorrect -> CyberGreen
                            isSelected -> CyberCyan
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(containerColor)
                                .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                                .clickable(enabled = !answerSubmittedLocked) {
                                    selectedAnswerOption = option
                                }
                                .padding(16.dp)
                                .testTag("quiz_option_$optIndex")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                if (answerSubmittedLocked) {
                                    if (isCorrect) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Correct", tint = CyberGreen)
                                    } else if (isSelected) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Incorrect", tint = CyberRed)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action validation buttons
                Button(
                    onClick = {
                        if (!answerSubmittedLocked) {
                            if (selectedAnswerOption != null) {
                                answerSubmittedLocked = true
                            }
                        } else {
                            viewModel.submitAnswer(selectedAnswerOption ?: "") {
                                onQuizFinished()
                            }
                            selectedAnswerOption = null
                            answerSubmittedLocked = false
                        }
                    },
                    enabled = selectedAnswerOption != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_next_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (answerSubmittedLocked) CyberCyan else CyberPurple
                    )
                ) {
                    Text(
                        text = if (!answerSubmittedLocked) "VERIFY SYNAPSE" else "PROCEED TO NEXT",
                        color = if (answerSubmittedLocked) MidnightDark else Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// --- Quiz Results Grid/Podium & Sharing ---
@Composable
fun QuizResultsScreen(
    viewModel: QuizViewModel,
    selectedCategory: String,
    selectedDifficulty: String,
    onNavigateHome: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val score by viewModel.quizScore.collectAsState()
    val timerSeconds by viewModel.quizTimer.collectAsState()
    val arenaPlayers by viewModel.arenaOpponents.collectAsState()
    val quizType by viewModel.quizType.collectAsState()
    val activeProfile by viewModel.currentProfile.collectAsState()
    val isDarkTheme by viewModel.darkThemeOverride.collectAsState()

    val context = LocalContext.current

    // Prepare Custom social platform share formats
    val rawShareText = """
        🚀 Dominated the CyberTrivia Arena!
        Category: $selectedCategory
        Difficulty: $selectedDifficulty
        Score: $score pts ($timerSeconds seconds)
        🔥 Streak Check: ${activeProfile?.streak ?: 1} Days
        
        Can you withstand the neural latency of the Leaderboards? Play CyberTrivia!
    """.trimIndent()

    // Trigger Social Share system sheets
    fun triggerSocialSharing() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, rawShareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Cyber Score")
        context.startActivity(shareIntent)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Victory Medal Badge",
                tint = CyberAmber,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = "QUIZ BATTLE ENGAGEMENT READY",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = CyberCyan,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Total Final Points: $score PTS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Result Podium Cards overlay (IF in Arena Mode)
            if (quizType == QuizType.ARENA) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "OFFICIAL ARENA PODIUM STANDINGS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyberPurple,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        // Sort list of challengers & you
                        val finalScoresList = (arenaPlayers.map { it.name to it.score } + ("YOU" to score))
                            .sortedByDescending { it.second }

                        finalScoresList.forEachIndexed { rank, playerTuple ->
                            val isUser = playerTuple.first == "YOU"
                            val rankSymbol = when(rank) {
                                0 -> "🥇"
                                1 -> "🥈"
                                2 -> "🥉"
                                else -> "🏁"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isUser) CyberCyan.copy(alpha = 0.15f) else Color.Transparent)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$rankSymbol ${rank + 1}. ${playerTuple.first}",
                                    fontWeight = if (isUser) FontWeight.ExtraBold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${playerTuple.second} pts",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Historical achievements tracker text notice
            Text(
                text = "📊 Results saved to database and global ranking has been refreshed!",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            // Dynamic Social Channels Platform Integration buttons
            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "SOCIAL BROADCAST SCORE", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { triggerSocialSharing() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DA1F2)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Share", color = Color.White)
                        }
                        
                        Button(
                            onClick = { triggerSocialSharing() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "WhatsApp", color = Color.White)
                        }
                    }
                }
            }

            // Options buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToStats,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.Assessment, contentDescription = "History")
                        Text(text = "Stats & Badges", fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = onNavigateHome,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("results_back_home_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Retreat Home", tint = MidnightDark)
                        Text(text = "Close Arena", color = MidnightDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// --- Historical Stats, Global Leaderboards, and Grid Gamification Locker ---
@Composable
fun StatsAndArchiveScreen(
    viewModel: QuizViewModel,
    onBack: () -> Unit
) {
    val activeProfile by viewModel.currentProfile.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val globalLeaderboard by viewModel.leaderboard.collectAsState()
    val isDarkTheme by viewModel.darkThemeOverride.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Performance & Badges, 1 = Global Tournament leaderboard

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simple Top Navigation header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = CyberCyan)
            }
            Text(
                text = "Cyber Intelligence Terminal",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Box(modifier = Modifier.size(48.dp)) // layout spacer
        }

        // Tab Row Switcher
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = CyberCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = CyberCyan
                )
            }
        ) {
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                Text(text = "Stats & Badges", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                Text(text = "Tournament", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
        }

        if (activeTab == 0) {
            // Stats panel & Badge grids
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Key metrics box row
                item {
                    val averagePercent = if (historyList.isNotEmpty()) {
                        (historyList.sumOf { it.score }.toFloat() / historyList.sumOf { it.totalQuestions }.toFloat()) * 100f
                    } else 0f

                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "LVL", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                Text(text = "${activeProfile?.level ?: 1}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = CyberCyan)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "RUNS", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                Text(text = "${historyList.size}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = CyberPurple)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "AVG ACC", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                Text(text = "${averagePercent.toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = CyberGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "XP", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                Text(text = "${activeProfile?.xp ?: 0}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = CyberAmber)
                            }
                        }
                    }
                }

                // Gamification Badge Grid Block
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "CYBERNETIC UNLOCKED BADGES",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )

                        // Badges collection definitions
                        val badgeDefinitions = listOf(
                            Triple("CYBER_NOMAD", "🌐 Nomad Chip", "Create a profile and signup!"),
                            Triple("TRIVIA_SHERIFF", "👮 High Sheriff", "Complete 5 distinct Trivia arena runs."),
                            Triple("PERFECT_SYNAPSE", "⚡ Max Synapse", "Achieve 5 out of 5 correct on any quiz run."),
                            Triple("SPEED_DEMON", "🏎 Speed Demon", "Annihilate a quiz run in less than 30 seconds!"),
                            Triple("GEMINI_PIONEER", "🤖 AI Pioneer", "Generate a custom category using Gemini AI."),
                            Triple("CHAMPION_CHIP", "🏆 Arena Overlord", "Win First Place in Real-time Arena!"),
                            Triple("NIGHT_OWL", "🦉 Night Owl", "Unlock a badge between 10PM and 4AM.")
                        )

                        val userBadges = viewModel.getUnlockedBadgesList(activeProfile?.badgesJson ?: "[]")

                        // Render badge container cards
                        badgeDefinitions.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { badge ->
                                    val isUnlocked = userBadges.contains(badge.first)
                                    val cardColor = if (isUnlocked) CyberCyan.copy(alpha = 0.1f) else (if (isDarkTheme) SurfaceDark else SurfaceLight)
                                    val borderColor = if (isUnlocked) CyberCyan else Color.Transparent

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                                        colors = CardDefaults.cardColors(containerColor = cardColor)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(text = badge.second, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                if (isUnlocked) {
                                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Unlocked", tint = CyberCyan, modifier = Modifier.size(14.dp))
                                                } else {
                                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), modifier = Modifier.size(12.dp))
                                                }
                                            }
                                            Text(
                                                text = badge.third,
                                                fontSize = 10.sp,
                                                lineHeight = 13.sp,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                                // if odd, pad empty space
                                if (pair.size < 2) {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Recent performance logs lists
                item {
                    Text(
                        text = "HISTORIAL RUN LOGS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                if (historyList.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No active records found. Enter a quiz match to build logs!",
                                modifier = Modifier.padding(24.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(historyList) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = log.category, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Timed duration: ${log.timeTakenSeconds} seconds", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = if (log.score == log.totalQuestions) CyberGreen.copy(alpha = 0.2f) else CyberPurple.copy(alpha = 0.1f))
                                ) {
                                    Text(
                                        text = "${log.score}/${log.totalQuestions}",
                                        fontWeight = FontWeight.Black,
                                        color = if (log.score == log.totalQuestions) CyberGreen else CyberPurple,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Global Tournament leaderboard (Simulated live updates/results)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "GLOBAL SYNAPSIS TOURNAMENT RANKINGS",
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                globalLeaderboard.forEachIndexed { index, entry ->
                    item {
                        val isYou = entry.username == activeProfile?.username
                        val cardBgColor = if (isYou) Color(0xFF313033) else (if (isDarkTheme) SurfaceDark else SurfaceLight)
                        val borderColor = if (isYou) Color(0xFF49454F) else Color.Transparent

                        val avatarBgColor = when (index) {
                            0 -> Color(0xFFF2B8B5)
                            1 -> Color(0xFFB2EEB1)
                            2 -> Color(0xFF91D1FF)
                            else -> if (isDarkTheme) Color(0xFF2D2F33) else Color(0x1A000000)
                        }

                        val rankColor = when (index) {
                            0 -> Color(0xFFF2B8B5)
                            1 -> Color(0xFFB2EEB1)
                            2 -> Color(0xFF91D1FF)
                            else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.width(36.dp),
                                    color = rankColor
                                )

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(avatarBgColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = entry.avatar, fontSize = 20.sp)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.username + if(isYou) " (YOU)" else "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = entry.category,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                }

                                Text(
                                    text = "${entry.score} pts",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    color = if (isYou) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Composable State Binding helper for standard Kotlin StateFlows ---
@Composable
fun <T> rememberStateFlow(flow: StateFlow<T>): State<T> {
    return flow.collectAsState()
}

// custom dynamic state helper for StateFlow mutable
fun <T> mutableStateFlowOf(initialValue: T): MutableStateFlow<T> {
    return MutableStateFlow(initialValue)
}
