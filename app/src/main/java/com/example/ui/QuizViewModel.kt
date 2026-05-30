package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.*

enum class QuizType { SOLO, ARENA }

data class ArenaPlayer(
    val name: String,
    val avatar: String,
    var score: Int,
    var currentQuestionIndex: Int,
    val speedFactor: Float // standard answer delay multiplier
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val database = QuizDatabase.getDatabase(application)
    private val profileDao = database.userProfileDao()
    private val historyDao = database.quizHistoryDao()
    private val leaderboardDao = database.leaderboardDao()

    // Configuration and States
    val allProfiles: StateFlow<List<UserProfile>> = profileDao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()

    // History and Leaderboards
    private val _historyList = MutableStateFlow<List<QuizHistory>>(emptyList())
    val historyList: StateFlow<List<QuizHistory>> = _historyList.asStateFlow()

    val leaderboard: StateFlow<List<LeaderboardEntry>> = leaderboardDao.getTopLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Quiz states
    private val _activeQuestions = MutableStateFlow<List<Question>>(emptyList())
    val activeQuestions: StateFlow<List<Question>> = _activeQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizTimer = MutableStateFlow(0)
    val quizTimer: StateFlow<Int> = _quizTimer.asStateFlow()

    private val _isGenerationLoading = MutableStateFlow(false)
    val isGenerationLoading: StateFlow<Boolean> = _isGenerationLoading.asStateFlow()

    private val _quizType = MutableStateFlow(QuizType.SOLO)
    val quizType: StateFlow<QuizType> = _quizType.asStateFlow()

    private val _arenaOpponents = MutableStateFlow<List<ArenaPlayer>>(emptyList())
    val arenaOpponents: StateFlow<List<ArenaPlayer>> = _arenaOpponents.asStateFlow()

    // Visual preferences
    private val _darkThemeOverride = MutableStateFlow(true)
    val darkThemeOverride: StateFlow<Boolean> = _darkThemeOverride.asStateFlow()

    // Notification Sim System
    private val _simulatedNotification = MutableStateFlow<String?>(null)
    val simulatedNotification: StateFlow<String?> = _simulatedNotification.asStateFlow()

    private var quizStartTime: Long = 0
    private var isQuizActive = false

    init {
        // Initialize active profile or seed a default one
        viewModelScope.launch(Dispatchers.IO) {
            val defaultEntries = listOf(
                LeaderboardEntry(username = "CyberRider 🦾", avatar = "🤖", score = 2400, category = "Science", isMock = true),
                LeaderboardEntry(username = "NeoSamurai", avatar = "⚡", score = 2250, category = "Gaming", isMock = true),
                LeaderboardEntry(username = "QuantumLoris", avatar = "🛸", score = 2100, category = "History", isMock = true),
                LeaderboardEntry(username = "CodeGlitch ☠", avatar = "👽", score = 1950, category = "Gaming", isMock = true),
                LeaderboardEntry(username = "HackerMax", avatar = "🚀", score = 1800, category = "Pop Culture", isMock = true),
                LeaderboardEntry(username = "AI_Synapse", avatar = "💻", score = 1600, category = "Science", isMock = true),
                LeaderboardEntry(username = "NekoPixel", avatar = "🐱", score = 1450, category = "Gaming", isMock = true)
            )
            
            // Seed leaderboard entries if empty
            leaderboardDao.getTopLeaderboard().first().let { current ->
                if (current.isEmpty()) {
                    defaultEntries.forEach { leaderboardDao.insertLeaderboardEntry(it) }
                }
            }

            // Load last active profile or create a default one
            val active = profileDao.getActiveProfileSync()
            if (active != null) {
                _currentProfile.value = active
                refreshUserHistory(active.id)
            } else {
                // Pre-seed a default user
                val defaultUser = UserProfile(
                    username = "CyberRunner",
                    avatar = "🌐"
                )
                val id = profileDao.insertProfile(defaultUser).toInt()
                _currentProfile.value = defaultUser.copy(id = id)
                refreshUserHistory(id)
            }
        }
    }

    fun toggleTheme() {
        _darkThemeOverride.value = !_darkThemeOverride.value
    }

    private fun refreshUserHistory(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.getHistoryForUser(userId).collect {
                _historyList.value = it
            }
        }
    }

    fun selectProfile(profile: UserProfile) {
        _currentProfile.value = profile
        refreshUserHistory(profile.id)
    }

    fun createProfile(username: String, avatar: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newProfile = UserProfile(
                username = username.ifBlank { "AnonUser" },
                avatar = avatar
            )
            val id = profileDao.insertProfile(newProfile).toInt()
            val savedProfile = newProfile.copy(id = id)
            _currentProfile.value = savedProfile
            
            // Trigger automatic BADGE unlock for signing up
            unlockBadge("CYBER_NOMAD")
            refreshUserHistory(id)
        }
    }

    fun deleteProfile(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            profileDao.deleteProfile(profile)
            // Switch to any other profile
            val list = profileDao.getAllProfiles().first()
            if (list.isNotEmpty()) {
                selectProfile(list.first())
            } else {
                // If empty, create default
                createProfile("CyberRunner", "🌐")
            }
        }
    }

    // Quiz Control Loop
    fun startQuiz(category: String, difficulty: String, type: QuizType) {
        _quizType.value = type
        _isGenerationLoading.value = true
        _quizScore.value = 0
        _currentQuestionIndex.value = 0
        _quizTimer.value = 0
        isQuizActive = true
        quizStartTime = System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.IO) {
            // Attempt Gemini Generation
            var questions = GeminiQuizClient.generateCustomQuiz(category, difficulty)
            
            if (questions == null) {
                // Graceful fallback to rich offline database
                questions = DefaultQuizzes.getQuestions(category, difficulty)
                // If still empty (custom category offline), default to Science
                if (questions.isEmpty()) {
                    questions = DefaultQuizzes.getQuestions("Science", difficulty)
                }
            } else {
                // Gemini custom generation successful! Trigger unlock check.
                unlockBadge("GEMINI_PIONEER")
            }

            _activeQuestions.value = questions
            _isGenerationLoading.value = false

            // Set up arena opponents if in Arena mode
            if (type == QuizType.ARENA) {
                _arenaOpponents.value = listOf(
                    ArenaPlayer("ZenithByte ✦", "🤖", 0, 0, 1.2f),
                    ArenaPlayer("QuantumRider ❖", "⚡", 0, 0, 1.5f),
                    ArenaPlayer("PixelNinja ★", "🐱", 0, 0, 0.9f)
                )
                startArenaGameLoop()
            }

            // Start clock timer
            startTimer()
        }
    }

    private fun startTimer() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isQuizActive) {
                delay(1000)
                if (isQuizActive) {
                    _quizTimer.value += 1
                }
            }
        }
    }

    // Engine modeling other online participants answering questions reactively
    private fun startArenaGameLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isQuizActive) {
                delay(1500) // update opponents every 1.5 seconds
                if (!isQuizActive) break

                val opponents = _arenaOpponents.value.map { player ->
                    // Calculate chance of answering correctly based on difficulty/randomizer
                    val currentProgressTime = _quizTimer.value
                    val expectedAnswersCount = (currentProgressTime / (5 * player.speedFactor)).toInt().coerceIn(0, 5)

                    if (expectedAnswersCount > player.currentQuestionIndex) {
                        player.currentQuestionIndex = expectedAnswersCount
                        // 75% accuracy
                        val correct = Math.random() < 0.75
                        if (correct) {
                            player.score += 100 + (Math.random() * 50).toInt() // speed bonus point
                        }
                    }
                    player
                }
                _arenaOpponents.value = opponents.toList()
            }
        }
    }

    fun submitAnswer(answer: String, onQuizFinished: () -> Unit) {
        val questions = _activeQuestions.value
        val currentIndex = _currentQuestionIndex.value
        if (questions.isEmpty() || currentIndex >= questions.size) return

        val currentQuestion = questions[currentIndex]
        
        // 100 points baseline + rapid speed bonus
        if (answer.trim() == currentQuestion.correctAnswer.trim()) {
            _quizScore.value += 400 // score per correct answer
        }

        val nextIndex = currentIndex + 1
        if (nextIndex < questions.size) {
            _currentQuestionIndex.value = nextIndex
        } else {
            // Quiz completed!
            isQuizActive = false
            saveQuizResultAndCheckAchievements(onQuizFinished)
        }
    }

    private fun saveQuizResultAndCheckAchievements(onQuizFinished: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentProfile.value ?: return@launch
            val score = _quizScore.value
            val correctCount = score / 400
            val totalQuestions = _activeQuestions.value.size
            val durationSeconds = _quizTimer.value
            
            val category = _activeQuestions.value.firstOrNull()?.let {
                if (it.id >= 1000) "AI Custom" else "Standard" 
            } ?: "Standard"

            // Save to local history
            val history = QuizHistory(
                userId = user.id,
                category = _activeQuestions.value.firstOrNull()?.question?.substringBefore("?") ?: "Custom Topic",
                difficulty = if (score == 2000) "Legendary" else "Standard",
                score = correctCount,
                totalQuestions = totalQuestions,
                timeTakenSeconds = durationSeconds
            )
            historyDao.insertHistory(history)

            // Submit score to global leaderboard automatically
            val entry = LeaderboardEntry(
                username = user.username,
                avatar = user.avatar,
                score = score,
                category = if (category == "AI Custom") "AI Arena" else "Cyber Battle"
            )
            leaderboardDao.insertLeaderboardEntry(entry)

            // Calculate level and XP rewards
            val xpGain = correctCount * 120 + (if (correctCount == totalQuestions) 150 else 0) // full score bonus
            val updatedXp = user.xp + xpGain
            val updatedLevel = 1 + (updatedXp / 1000)

            // Calculate streak day increments if last active > 20h ago
            var updatedStreak = user.streak
            val timeDiff = System.currentTimeMillis() - user.lastActiveTimestamp
            if (timeDiff > 20 * 60 * 60 * 1000) {
                updatedStreak += 1
            }

            val updatedUser = user.copy(
                xp = updatedXp,
                level = updatedLevel,
                streak = updatedStreak,
                lastActiveTimestamp = System.currentTimeMillis()
            )
            profileDao.updateProfile(updatedUser)
            _currentProfile.value = updatedUser

            // Evaluate Gamification Badges
            val currentBadges = getUnlockedBadgesList(user.badgesJson)
            val newlyUnlocked = mutableListOf<String>()

            // 1. PERFECT_SYNAPSE: Full score
            if (correctCount == totalQuestions && !currentBadges.contains("PERFECT_SYNAPSE")) {
                newlyUnlocked.add("PERFECT_SYNAPSE")
            }

            // 2. SPEED_DEMON: answered 5 questions under 30s
            if (durationSeconds < 30 && correctCount >= 4 && !currentBadges.contains("SPEED_DEMON")) {
                newlyUnlocked.add("SPEED_DEMON")
            }

            // 3. TRIVIA_SHERIFF: 5 quizzes completed
            val historyCount = _historyList.value.size + 1
            if (historyCount >= 5 && !currentBadges.contains("TRIVIA_SHERIFF")) {
                newlyUnlocked.add("TRIVIA_SHERIFF")
            }

            // 4. CHAMPION_CHIP: 1st place in Real-time Simulated multiplayer
            if (_quizType.value == QuizType.ARENA && !currentBadges.contains("CHAMPION_CHIP")) {
                val opponentMax = _arenaOpponents.value.maxOfOrNull { it.score } ?: 0
                if (score > opponentMax) {
                    newlyUnlocked.add("CHAMPION_CHIP")
                }
            }

            // 5. NIGHT_OWL: complete between 10 PM and 4 AM
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            if ((hour >= 22 || hour < 4) && !currentBadges.contains("NIGHT_OWL")) {
                newlyUnlocked.add("NIGHT_OWL")
            }

            if (newlyUnlocked.isNotEmpty()) {
                val combinedBadges = (currentBadges + newlyUnlocked).distinct()
                val jsonArr = JSONArray()
                combinedBadges.forEach { jsonArr.put(it) }
                val updatedWithBadges = updatedUser.copy(badgesJson = jsonArr.toString())
                profileDao.updateProfile(updatedWithBadges)
                _currentProfile.value = updatedWithBadges

                // Broadcast a simulated Android Push Notification alert!
                val badgeNameCapitalized = newlyUnlocked.first().replace("_", " ")
                triggerSimulatedNotification("🏆 Gamified Badge Unlocked: You earned '$badgeNameCapitalized' badge!")
            }

            // Refresh list
            refreshUserHistory(user.id)
            
            // Invoke completion handler back in Compose Main Thread
            viewModelScope.launch(Dispatchers.Main) {
                onQuizFinished()
            }
        }
    }

    private fun unlockBadge(badgeKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentProfile.value ?: return@launch
            val badges = getUnlockedBadgesList(user.badgesJson)
            if (!badges.contains(badgeKey)) {
                val combined = badges + badgeKey
                val jsonArr = JSONArray()
                combined.forEach { jsonArr.put(it) }
                
                val updated = user.copy(badgesJson = jsonArr.toString())
                profileDao.updateProfile(updated)
                _currentProfile.value = updated

                triggerSimulatedNotification("🏆 Badge Unlocked: Accomplished '$badgeKey'!")
            }
        }
    }

    fun getUnlockedBadgesList(json: String): List<String> {
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Simulated Top Banner daily notifications triggers
    fun triggerSimulatedNotification(text: String) {
        _simulatedNotification.value = text
        viewModelScope.launch {
            delay(5000) // dismiss notification banner after 5 seconds automatically
            if (_simulatedNotification.value == text) {
                _simulatedNotification.value = null
            }
        }
    }

    fun clearNotification() {
        _simulatedNotification.value = null
    }
}
