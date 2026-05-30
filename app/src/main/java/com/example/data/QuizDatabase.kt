package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- entities ---

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val avatar: String, // Emoji or key
    val level: Int = 1,
    val xp: Int = 0,
    val streak: Int = 1,
    val badgesJson: String = "[]", // JSON array of unlocked badges
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_histories")
data class QuizHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val category: String,
    val difficulty: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val timeTakenSeconds: Int
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val avatar: String,
    val score: Int,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isMock: Boolean = false
)

// --- DAOs ---

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getProfileById(id: Int): UserProfile?

    @Query("SELECT * FROM user_profiles ORDER BY xp DESC LIMIT 1")
    suspend fun getActiveProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Delete
    suspend fun deleteProfile(profile: UserProfile)
}

@Dao
interface QuizHistoryDao {
    @Query("SELECT * FROM quiz_histories WHERE userId = :userId ORDER BY timestamp DESC")
    fun getHistoryForUser(userId: Int): Flow<List<QuizHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: QuizHistory)
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard_entries ORDER BY score DESC, timestamp DESC LIMIT 50")
    fun getTopLeaderboard(): Flow<List<LeaderboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntry(entry: LeaderboardEntry)

    @Query("DELETE FROM leaderboard_entries WHERE isMock = 0")
    suspend fun clearUserEntries()
}

// --- Database Converter ---
class DatabaseConverters {
    // Room automatically converts primitive structures, we'll store badges as raw comma/JSON string and handle parsing manually
}

// --- AppDatabase ---

@Database(
    entities = [UserProfile::class, QuizHistory::class, LeaderboardEntry::class],
    version = 1,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun quizHistoryDao(): QuizHistoryDao
    abstract fun leaderboardDao(): LeaderboardDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null

        fun getDatabase(context: Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "quiz_cyber_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
