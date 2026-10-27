package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.HabitCategoryEntity
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.UserProfile
import com.example.data.repository.HabitCategoryRepository
import com.example.data.repository.HabitRepository
import com.example.notification.NotificationHelper
import com.example.ui.theme.ThemeMode
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HabitUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null,
    val habits: List<Habit> = emptyList(),
    val categories: List<HabitCategoryEntity> = emptyList(),
    val logsForDate: Map<String, HabitLog> = emptyMap(),
    val allLogs: List<HabitLog> = emptyList(),
    val selectedDate: String = getTodayDateString(),
    val selectedCategory: String = "All",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val feedbackMessage: String? = null
)

fun getTodayDateString(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return formatter.format(Date())
}

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HabitRepository(application)
    private val categoryRepository = HabitCategoryRepository(
        AppDatabase.getDatabase(application).habitCategoryDao()
    )

    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.ensureDefaultCategories()
            categoryRepository.allCategories.collect { cats ->
                _uiState.value = _uiState.value.copy(categories = cats)
            }
        }

        val user = Firebase.auth.currentUser
        if (user != null) {
            initializeUserSession(user.uid)
        }
    }

    fun initializeUserSession(userId: String) {
        viewModelScope.launch {
            val user = Firebase.auth.currentUser
            if (user != null) {
                repository.ensureUserProfile(user)
            }

            // Observe UserProfile
            launch {
                repository.observeUserProfile(userId)
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(feedbackMessage = "Error loading profile: ${e.message}")
                    }
                    .collect { profile ->
                        _uiState.value = _uiState.value.copy(userProfile = profile)
                    }
            }

            // Observe Habits
            launch {
                repository.observeHabits(userId)
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(feedbackMessage = "Error loading habits: ${e.message}")
                    }
                    .collect { habitsList ->
                        _uiState.value = _uiState.value.copy(
                            habits = habitsList,
                            isLoading = false
                        )
                    }
            }

            // Observe Logs for today
            launch {
                repository.observeLogsForDate(userId, _uiState.value.selectedDate)
                    .catch { }
                    .collect { logs ->
                        val logMap = logs.associateBy { it.habitId }
                        _uiState.value = _uiState.value.copy(logsForDate = logMap)
                    }
            }

            // Observe All Logs for analytics and calendar
            launch {
                repository.observeAllLogs(userId)
                    .catch { }
                    .collect { allLogs ->
                        _uiState.value = _uiState.value.copy(allLogs = allLogs)
                    }
            }
        }
    }

    fun setSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null)
    }

    fun toggleHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                val currentLog = _uiState.value.logsForDate[habit.id]
                val date = _uiState.value.selectedDate
                repository.toggleHabitCompletion(habit, date, currentLog)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(feedbackMessage = e.message ?: "Failed to update habit")
            }
        }
    }

    fun updateHabitProgress(habit: Habit, newCount: Long) {
        viewModelScope.launch {
            try {
                val currentLog = _uiState.value.logsForDate[habit.id]
                val date = _uiState.value.selectedDate
                repository.updateHabitProgress(habit, date, currentLog, newCount)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(feedbackMessage = e.message ?: "Failed to update count")
            }
        }
    }

    fun saveHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                repository.saveHabit(habit)
                if (habit.reminderTime.isNotBlank()) {
                    val parts = habit.reminderTime.split(":")
                    if (parts.size == 2) {
                        val hour = parts[0].toIntOrNull() ?: 8
                        val minute = parts[1].toIntOrNull() ?: 0
                        NotificationHelper.scheduleHabitReminder(
                            getApplication(),
                            habit.id.ifBlank { "new_habit" },
                            habit.title,
                            hour,
                            minute
                        )
                    }
                } else {
                    NotificationHelper.cancelHabitReminder(getApplication(), habit.id)
                }
                _uiState.value = _uiState.value.copy(feedbackMessage = "Goal saved successfully!")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(feedbackMessage = e.message ?: "Failed to save habit")
            }
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            try {
                repository.deleteHabit(habitId)
                NotificationHelper.cancelHabitReminder(getApplication(), habitId)
                _uiState.value = _uiState.value.copy(feedbackMessage = "Habit deleted")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(feedbackMessage = e.message ?: "Failed to delete habit")
            }
        }
    }

    fun purchaseStreakFreeze() {
        viewModelScope.launch {
            try {
                repository.purchaseStreakFreeze()
                _uiState.value = _uiState.value.copy(feedbackMessage = "Streak Freeze acquired! 🛡️")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(feedbackMessage = e.message ?: "Could not purchase freeze")
            }
        }
    }

    fun sendTestReminder() {
        NotificationHelper.sendDirectReminder(
            getApplication(),
            "HabitPulse Reminder",
            "Keep up the momentum! You are 1 step closer to your goals today."
        )
        _uiState.value = _uiState.value.copy(feedbackMessage = "Test reminder notification sent!")
    }

    fun addCategory(name: String, colorHex: String = "#6366F1") {
        viewModelScope.launch {
            try {
                categoryRepository.addCategory(name, colorHex)
                _uiState.value = _uiState.value.copy(feedbackMessage = "Category \"$name\" added!")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(feedbackMessage = e.message ?: "Failed to add category")
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(id)
                _uiState.value = _uiState.value.copy(feedbackMessage = "Category removed")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(feedbackMessage = e.message ?: "Failed to delete category")
            }
        }
    }
}
