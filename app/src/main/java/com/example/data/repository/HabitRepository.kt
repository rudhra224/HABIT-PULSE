package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.GamificationConfig
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class HabitRepository(
    val db: FirebaseFirestore,
    private val auth: com.google.firebase.auth.FirebaseAuth = Firebase.auth
) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(user: FirebaseUser) {
        val uid = user.uid
        val docRef = db.collection("users").document(uid)
        try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val profile = UserProfile(
                    id = uid,
                    userId = uid,
                    displayName = user.displayName ?: "Habit Builder",
                    email = user.email ?: "",
                    level = 1L,
                    xp = 0L,
                    streakFreezes = 1L,
                    badges = emptyList()
                )
                docRef.set(profile).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
        }
    }

    fun observeHabits(userId: String): Flow<List<Habit>> = flow {
        val path = "users/$userId/habits"
        emitAll(
            db.collection("users").document(userId).collection("habits")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Habit::class.java).filter { !it.isArchived }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeLogsForDate(userId: String, date: String): Flow<List<HabitLog>> = flow {
        val path = "users/$userId/logs"
        emitAll(
            db.collection("users").document(userId).collection("logs")
                .whereEqualTo("date", date)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(HabitLog::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAllLogs(userId: String): Flow<List<HabitLog>> = flow {
        val path = "users/$userId/logs"
        emitAll(
            db.collection("users").document(userId).collection("logs")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(HabitLog::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveHabit(habit: Habit) {
        val uid = requireUserId()
        val habitId = if (habit.id.isBlank()) UUID.randomUUID().toString() else habit.id
        val targetHabit = habit.copy(id = habitId, userId = uid)
        val docRef = db.collection("users").document(uid).collection("habits").document(habitId)

        try {
            if (habit.id.isBlank() || habit.createdAt == null) {
                // New habit: set will generate server timestamps
                docRef.set(targetHabit).await()
            } else {
                // Update habit: reset updatedAt to null for @ServerTimestamp
                docRef.set(targetHabit.copy(updatedAt = null)).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteHabit(habitId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("habits").document(habitId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun toggleHabitCompletion(habit: Habit, date: String, currentLog: HabitLog?) {
        val uid = requireUserId()
        val logId = currentLog?.id?.ifBlank { null } ?: "${habit.id}_$date"
        val isNowCompleted = !(currentLog?.completed ?: false)
        val count = if (isNowCompleted) habit.targetCount else 0L

        val logRef = db.collection("users").document(uid).collection("logs").document(logId)
        val habitRef = db.collection("users").document(uid).collection("habits").document(habit.id)
        val userRef = db.collection("users").document(uid)

        try {
            val newLog = HabitLog(
                id = logId,
                userId = uid,
                habitId = habit.id,
                date = date,
                completed = isNowCompleted,
                count = count,
                notes = currentLog?.notes ?: "",
                createdAt = currentLog?.createdAt
            )
            logRef.set(newLog).await()

            // Update streak and completion counts on Habit
            val newCurrentStreak = if (isNowCompleted) habit.currentStreak + 1 else (habit.currentStreak - 1).coerceAtLeast(0L)
            val newLongestStreak = maxOf(habit.longestStreak, newCurrentStreak)
            val newTotalCompletions = if (isNowCompleted) habit.totalCompletions + 1 else (habit.totalCompletions - 1).coerceAtLeast(0L)

            habitRef.update(
                mapOf(
                    "currentStreak" to newCurrentStreak,
                    "longestStreak" to newLongestStreak,
                    "totalCompletions" to newTotalCompletions,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Update user XP & Gamification
            val xpDelta = if (isNowCompleted) GamificationConfig.XP_PER_COMPLETION else -GamificationConfig.XP_PER_COMPLETION
            val userSnap = userRef.get().await()
            val currentProfile = userSnap.toObject(UserProfile::class.java) ?: UserProfile(id = uid, userId = uid)
            val newXp = (currentProfile.xp + xpDelta).coerceAtLeast(0L)
            val (newLevel, _) = GamificationConfig.calculateLevel(newXp)

            // Check badges
            val currentBadges = currentProfile.badges.toMutableSet()
            if (newTotalCompletions >= 1L) currentBadges.add("first_step")
            if (newCurrentStreak >= 3L) currentBadges.add("three_day_streak")
            if (newCurrentStreak >= 7L) currentBadges.add("week_warrior")
            if (newCurrentStreak >= 14L) currentBadges.add("fortnight_fortitude")
            if (newCurrentStreak >= 30L) currentBadges.add("month_master")
            if (newLevel >= 5L) currentBadges.add("level_five")

            userRef.update(
                mapOf(
                    "xp" to newXp,
                    "level" to newLevel,
                    "badges" to currentBadges.toList(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, logRef.path)
            throw e
        }
    }

    suspend fun updateHabitProgress(habit: Habit, date: String, currentLog: HabitLog?, newCount: Long) {
        val uid = requireUserId()
        val logId = currentLog?.id?.ifBlank { null } ?: "${habit.id}_$date"
        val isNowCompleted = newCount >= habit.targetCount
        val wasCompleted = currentLog?.completed ?: false

        val logRef = db.collection("users").document(uid).collection("logs").document(logId)
        val habitRef = db.collection("users").document(uid).collection("habits").document(habit.id)
        val userRef = db.collection("users").document(uid)

        try {
            val updatedLog = HabitLog(
                id = logId,
                userId = uid,
                habitId = habit.id,
                date = date,
                completed = isNowCompleted,
                count = newCount,
                notes = currentLog?.notes ?: "",
                createdAt = currentLog?.createdAt
            )
            logRef.set(updatedLog).await()

            if (isNowCompleted != wasCompleted) {
                val newStreak = if (isNowCompleted) habit.currentStreak + 1 else (habit.currentStreak - 1).coerceAtLeast(0L)
                val newLongestStreak = maxOf(habit.longestStreak, newStreak)
                val newTotal = if (isNowCompleted) habit.totalCompletions + 1 else (habit.totalCompletions - 1).coerceAtLeast(0L)

                habitRef.update(
                    mapOf(
                        "currentStreak" to newStreak,
                        "longestStreak" to newLongestStreak,
                        "totalCompletions" to newTotal,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                val xpDelta = if (isNowCompleted) GamificationConfig.XP_PER_COMPLETION else -GamificationConfig.XP_PER_COMPLETION
                val userSnap = userRef.get().await()
                val currentProfile = userSnap.toObject(UserProfile::class.java) ?: UserProfile(id = uid, userId = uid)
                val newXp = (currentProfile.xp + xpDelta).coerceAtLeast(0L)
                val (newLevel, _) = GamificationConfig.calculateLevel(newXp)

                val currentBadges = currentProfile.badges.toMutableSet()
                if (newTotal >= 1L) currentBadges.add("first_step")
                if (newStreak >= 3L) currentBadges.add("three_day_streak")
                if (newStreak >= 7L) currentBadges.add("week_warrior")
                if (newLevel >= 5L) currentBadges.add("level_five")

                userRef.update(
                    mapOf(
                        "xp" to newXp,
                        "level" to newLevel,
                        "badges" to currentBadges.toList(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, logRef.path)
            throw e
        }
    }

    suspend fun purchaseStreakFreeze() {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        try {
            val userSnap = userRef.get().await()
            val profile = userSnap.toObject(UserProfile::class.java) ?: return
            if (profile.xp >= GamificationConfig.STREAK_FREEZE_COST_XP) {
                userRef.update(
                    mapOf(
                        "xp" to profile.xp - GamificationConfig.STREAK_FREEZE_COST_XP,
                        "streakFreezes" to profile.streakFreezes + 1,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            throw e
        }
    }
}
